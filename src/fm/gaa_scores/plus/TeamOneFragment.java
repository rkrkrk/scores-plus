/*
 *  MatchSetupFragment.java
 *
 *  Written by: Fintan Mahon 12101524
 *  
 *  Description: GUI to get input re match details and team lineup
 *  store details to database
 *  pass relevant details into MatchRecordFragment
 *  
 *  Written on: Jan 2013
 *  
 * 
 */
package fm.gaa_scores.plus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import fm.gaa_scores.plus.R;

import android.app.AlertDialog;

import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import android.support.v4.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.WindowManager;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class TeamOneFragment extends Fragment {
	// ArrayList to store panel from database
	private ArrayList<String> panelList = new ArrayList<String>();
	private ArrayList<String> subsList = new ArrayList<String>();
	private ArrayList<Integer> posnList = new ArrayList<Integer>();

	// HashMap to Store Player Name and ID for lookup on saving.
	private HashMap<String, Integer> playerIDLookUp = new HashMap<String, Integer>();

	private String panel[], strTemp[], strTemp2[];
	private String[] teamLineUpCurrent = new String[16];// stores selected team
	private Button[] bTeam = new Button[16];// array of buttons for team
											// selection
	private Button b;
	// private MatchRecordFragment fragmentRecord;//referenence
	private TextView tTeamHome;
	long matchID;
	private String panelName, player, team, playerOff, playerOn, oppTeamName;
	private Date currentDate;
	private SimpleDateFormat sdfdate;
	private EditText input;
	private int  index, indexOff, indexOn;
	private TextView tCards, tSubs;

//	 setup uri to read panel from database using content provider
	Uri allTitles = TeamContentProvider.CONTENT_URI;
	

	@Override
	// start main method to display screen
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.team_layout, container, false);
		// get the tag name of this Fragment and pass it up to the parent
		// activity MatchApplication so that this Fragment may be accessed
		// by other fragments through using a reference created from tag name

		String myTag = getTag();
		((Startup) getActivity()).setTagFragmentTeamOne(myTag);
		this.setHasOptionsMenu(true);
		v.setBackgroundColor(Color.rgb(204,255,204));
		
		//hide softkeyboard after entry
//		getActivity().getWindow().setSoftInputMode(
//			      WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


		// set up text view and buttons
		tTeamHome = (TextView)  v.findViewById(R.id.homeTeamName);
		Button bButtonReset = (Button) v.findViewById(R.id.button_setup_reset);
		bButtonReset.setOnClickListener(resetTeamListener);
		Button bSub = (Button) v.findViewById(R.id.bSub);
		bSub.setOnClickListener(recordSub);
		Button bButtonChange = (Button) v.findViewById(R.id.homeTeam);
		bButtonChange.setOnClickListener(changeNameListener);
		// read persisted stored data to set up screen on restart
		SharedPreferences sharedPref = getActivity().getSharedPreferences(
				"home_team_data", Context.MODE_PRIVATE);
			
		// setup input edittext boxes
		panelName = sharedPref.getString("PANELNAME", "OWN TEAM"); 
		sharedPref = getActivity().getSharedPreferences(
				"opp_team_data", Context.MODE_PRIVATE);
		oppTeamName = sharedPref.getString("PANELNAME", "OPPOSITION");
		tTeamHome.setText(panelName);		
		setButtons(v);
		getTeam(panelName);
		
		tCards=(TextView) v.findViewById(R.id.tCards);
		tSubs=(TextView) v.findViewById(R.id.tSubs);
		updateCards();
		updateSubsList();
		
			// Listener for reset team button
		// resets team lineup and edittext fields
		bButtonReset.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				resetTeam();
				v.playSoundEffect(SoundEffectConstants.CLICK);
				getTeam(panelName);
				return true;
			}
		});
		return v;
	}
	
	@Override
	public void onPause() {
		// Save out the details so that they are available on restart
		super.onPause(); // Always call the superclass method first
		SharedPreferences sharedPref = getActivity().getSharedPreferences(
				"home_team_data", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString("PANELNAME", panelName);
		editor.commit();
	}
	
	private void getTeam(String teamName) {
		// load panel from database and assign to arraylist
		String[] projection = { TeamContentProvider.PANELID,
				TeamContentProvider.NAME,TeamContentProvider.POSN };
		CursorLoader cL;
		int posn;
		//reset line up and read from database
		for (int j = 1; j <= 15; j++) {
			teamLineUpCurrent[j] = null; 
			}
		cL = new CursorLoader(getActivity(), allTitles, projection,
				TeamContentProvider.TEAM + " = '" + teamName + "'", null,
				TeamContentProvider.NAME);
		Cursor c1 = cL.loadInBackground();
		panelList.clear();
		playerIDLookUp.clear();
		if (c1.getCount() > 0) {
			c1.moveToFirst();
			do {
				// read in player nicknames
				panelList.add(c1.getString(c1
						.getColumnIndexOrThrow(TeamContentProvider.NAME)));
				// insert players into positions
				posn=c1.getInt(c1
						.getColumnIndexOrThrow(TeamContentProvider.POSN));
				if (posn>0){
					teamLineUpCurrent[posn]=c1.getString(c1
							.getColumnIndexOrThrow(TeamContentProvider.NAME));				
				}
											
				posnList.add(c1.getInt(c1
						.getColumnIndexOrThrow(TeamContentProvider.POSN)));

				playerIDLookUp
						.put(c1.getString(c1
								.getColumnIndexOrThrow(TeamContentProvider.NAME)),
								c1.getInt(c1
										.getColumnIndexOrThrow(TeamContentProvider.PANELID)));
			} while (c1.moveToNext());
	
		} 
		
		// remove from panellist names of players that are already selected and
		// assigned to a button onscreen
		for (int j = 1; j <= 15; j++) {
			if (panelList.indexOf(teamLineUpCurrent[j]) != -1) {
				panelList.remove(teamLineUpCurrent[j]);
			}			
		}
		
		// assign default number to rest
		for (int j = 1; j <= 15; j++) {
			if (teamLineUpCurrent[j] == null) {
				teamLineUpCurrent[j] = String.valueOf(j);
			}
			bTeam[j].setText(teamLineUpCurrent[j]);
		}
		// insert SWAP into panelist in 1st position to facilitate position
		// changes and substitutions
		panelList.remove("...");
		panelList.add(0, "RESET POSITION TO NUMBER");
		panelList.add(0, "ENTER NEW PLAYER NAME");
		c1.close();
	}
	
	private void setButtons(View w) {
		// Set buttonlisteners and use position numbers as default team lineup
		for (int i = 1; i <= 15; i++) {
			// set listener on team buttons
			bTeam[i] = (Button) w.findViewById(getResources().getIdentifier(
					"ButtonP" + String.format("%02d", i), "id",
					"fm.gaa_scores.plus"));
			bTeam[i].setOnClickListener(teamSetupClickListener);
		}
	}	
	
	OnClickListener recordSub = new OnClickListener() {
		@Override
		public void onClick(View v) {
			//set up panelist 
			strTemp2 = new String[panelList.size()-2];
			for (int i = 0; i < panelList.size()-2; i++) {
				strTemp2[i] = panelList.get(i+2);
			}
			strTemp=new String[15];
			for (int i = 1; i <= 15; i++) {
				strTemp[i - 1] = i + ": " + teamLineUpCurrent[i];
			}
			teamLineUpCurrent[0] = "0";
			// Get whois coming off swap with going on and write change to
			// databse
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle("select who is coming off");
			builder.setSingleChoiceItems(strTemp, 0,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							indexOff = which + 1;
							playerOff = teamLineUpCurrent[indexOff];
							// dialog to see who is goping on
							
							// Get whois coming off swap with going on and write
							// change to databse
							AlertDialog.Builder builder1 = new AlertDialog.Builder(
									getActivity());
							builder1.setTitle("select who is going on");
							builder1.setSingleChoiceItems(strTemp2, 0,
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											indexOn = which+2;// text in first 2
											playerOn = panelList.get(which+2);
												makeSub();
								
											dialog.dismiss();
										}
									});
							AlertDialog alert1 = builder1.create();
							alert1.show();
							dialog.dismiss();
						}
					});
			AlertDialog alert = builder.create();
			alert.show();
		}
	};
	
	
	private void makeSub() {
		// update database
		// Where a Player is not already selected
		// for team the button text will be just the
		// position number and so length < 3.
		// assign player to button/teamlineup and
	
		if (playerOff.length() < 3) {
			bTeam[indexOff].setText(playerOn);
			teamLineUpCurrent[indexOff] = playerOn;
			panelList.remove(playerOn);
			// write position to database
			ContentValues values = new ContentValues();
			values.put("posn", indexOff);
			Uri uri = Uri.parse(TeamContentProvider.CONTENT_URI + "/"
					+ playerIDLookUp.get(playerOn));
			getActivity().getContentResolver().update(uri, values, null, null);
		}
		// where Player already selected in position
		// need to swap. Assign new player to
		// button/teamlineup. Add swapped out player
		// back into panelList and Sort
		//
		else {
			bTeam[indexOff].setText(playerOn);
			teamLineUpCurrent[indexOff] = playerOn;
			panelList.remove(playerOn);
			panelList.add(playerOff);
			panelList.remove("RESET POSITION TO NUMBER");
			panelList.remove("ENTER NEW PLAYER NAME");
			Collections.sort(panelList);
			panelList.add(0, "RESET POSITION TO NUMBER");
			panelList.add(0, "ENTER NEW PLAYER NAME");
			// update position of selected player in database
			ContentValues values = new ContentValues();
			values.put("posn", indexOff);
			Uri uri = Uri.parse(TeamContentProvider.CONTENT_URI + "/"
					+ playerIDLookUp.get(playerOn));
	
			getActivity().getContentResolver().update(uri, values, null, null);
			// update position of removed player in database
			values = new ContentValues();
			values.put("posn", -1);
			uri = Uri.parse(TeamContentProvider.CONTENT_URI + "/"
					+ playerIDLookUp.get(playerOff));
			getActivity().getContentResolver().update(uri, values, null, null);
		}
		getTeam(panelName);
		// write to stats
		String temp = (((Startup) getActivity()).getFragmentScore().getTime() == "") ? ""
				: ((Startup) getActivity()).getFragmentScore().getTime()
						+ " mins "+ ((Startup) getActivity()).getFragmentScore().bPeriod
						.getText();
		ContentValues values = new ContentValues();
		values.put(
				"line",
				temp+" substitution "+panelName + "--> off: " + playerOff + "  on: "
						+ playerOn);
		getActivity().getContentResolver().insert(
				TeamContentProvider.CONTENT_URI_2, values);
		updateSubsList();
		((Startup) getActivity()).getFragmentScore().updateStats();
		((Startup) getActivity()).getFragmentReview().updateListView();
	}

	//reset team positions to numbers
	OnClickListener resetTeamListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// get reference to REVIEW fragment from parent activity
			// MatchApplication and use reference to execute resetStats
			// method in REVIEW fragment which will reset stats there to 0
			Toast.makeText(getActivity(), "Long Press to Reset",
					Toast.LENGTH_SHORT).show();
		}
	};
	
	private void resetTeam() {
		// Reset team lineup to default position numbers
		// and assign numbers ot buttons on screen
		for (int i = 1; i <= 15; i++) {
			teamLineUpCurrent[i] = String.valueOf(i);
			bTeam[i].setText(String.valueOf(i));
		}
		// Reset positions to -1 in database
		ContentValues values = new ContentValues();
		int count;
		values.put("posn", -1);
		// add to panel database
		count = getActivity().getContentResolver().update(
				TeamContentProvider.CONTENT_URI, values,
				TeamContentProvider.TEAM + " = '" + panelName + "'", null);

		// which will set team names and team lineup
	}

	//change name of current team
	OnClickListener changeNameListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			changeName();
		}
	};
	
	private void changeName() {
		AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
		input = new EditText(getActivity());
		input.setId(997);
		alert.setTitle("Enter New Team Name");
		alert.setMessage("Name:");
		alert.setView(input);
		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface indialog, int which) {
				String inName = input.getText().toString();
				if (inName.length() > 2) {
					// Update name in database
					ContentValues values = new ContentValues();
					int count;
					values.put("team", inName);
					// add to panel database
					count = getActivity().getContentResolver()
							.update(TeamContentProvider.CONTENT_URI,
									values,
									TeamContentProvider.TEAM + " = '"
											+ panelName + "'", null);
					// if team doesnt exist, create it
					if (count == 0) {
						values = new ContentValues();
						values.put("name", "...");
						values.put("posn", 0);
						values.put("team", inName);
						getActivity().getContentResolver().insert(
								TeamContentProvider.CONTENT_URI, values);
					}
					
					
					
					Toast.makeText(getActivity(), "panel renamed",
							Toast.LENGTH_LONG).show();
					// update title and panelname
					panelName = inName;
					tTeamHome.setText(panelName);
					((Startup) getActivity()).getFragmentScore().setTeamLineUp(
							panelName, "");
					((Startup) getActivity()).getFragmentReview().setTeamNames(
							panelName, "");
					((Startup) getActivity()).getFragmentReview().updateListView();
					((Startup) getActivity()).getFragmentScore().updateStats();
					((Startup) getActivity()).getFragmentTeamTwo().setTeam(panelName);

					
				} else {
					Toast.makeText(
							getActivity(),
							"Invalid Name, Try Again\n"
									+ "Must be at least 3 characters long",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		alert.create();
		alert.show();
	}
	

	
	//create new team
	OnClickListener createNewTeamListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			createNewTeam();
		}
	};
	
	private void createNewTeam() {
		AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
		input = new EditText(getActivity());
		input.setId(996);
		alert.setTitle("Enter New Team Name");			
		alert.setMessage("Name:");
		alert.setView(input);
		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface indialog, int which) {
				String inName = input.getText().toString();
				if(inName.length()>2){
					//Update name in database
					// Reset team lineup to default position numbers
					// and assign numbers ot buttons on screen
					for (int i = 1; i <= 15; i++) {
						teamLineUpCurrent[i] = String.valueOf(i);
						bTeam[i].setText(String.valueOf(i));
					}
					panelName=inName;
					tTeamHome.setText(panelName);	
					panelList.clear();
					panelList.add(0, "RESET POSITION TO NUMBER");
					panelList.add(0, "ENTER NEW PLAYER NAME");
					playerIDLookUp.clear();		
					// add to database
					ContentValues values = new ContentValues();
					values.put("name", "...");
					values.put("posn", 0);
					values.put("team", panelName);
					getActivity().getContentResolver().insert(
							TeamContentProvider.CONTENT_URI, values);	
	
					//update other fragments
					((Startup) getActivity()).getFragmentScore().setTeamLineUp(
							 panelName, "");
					((Startup) getActivity()).getFragmentReview().setTeamNames(panelName,
							"");
					((Startup) getActivity()).getFragmentTeamTwo().setTeam(panelName);

					
				} else {
					Toast.makeText(getActivity(), "Invalid Name, Try Again\n"+
							"Must be at least 3 characters long",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		alert.create();
		alert.show();	
	}
	
	
	
	// Load existing team
	OnClickListener loadTeamListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// get list of team names
			loadTeam();
		}
	};
	
	private void loadTeam() {
		ArrayList<String> panelList = new ArrayList<String>();
		String str;
		String[] projection = { TeamContentProvider.TEAM };
		CursorLoader cL = new CursorLoader(getActivity(), allTitles,
				projection, null, null, TeamContentProvider.TEAM);
		Cursor c1 = cL.loadInBackground();
		if (c1.getCount() > 0) {
			c1.moveToFirst();
			do {
				str = c1.getString(c1
						.getColumnIndexOrThrow(TeamContentProvider.TEAM));
				if (!panelList.contains(str))
					panelList.add(str);
			} while (c1.moveToNext());
		}
		//take out team in other page
		panelList.remove(oppTeamName);
		if (panelList.size() > 0) {
			panel = new String[panelList.size()];
			for (int i = 0; i < panelList.size(); i++) {
				panel[i] = panelList.get(i);
			}
			c1.close();
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle("select team to load");
			builder.setSingleChoiceItems(panel, 0,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							panelName = panel[which];
							tTeamHome.setText(panelName);
							for (int j = 1; j <= 15; j++) {
								teamLineUpCurrent[j] = String.valueOf(j);
								bTeam[j].setText(teamLineUpCurrent[j]);
							}

							getTeam(panelName);
							((Startup) getActivity()).getFragmentScore()
									.setTeamLineUp(panelName, "");
							((Startup) getActivity()).getFragmentReview()
									.setTeamNames(panelName, "");
							((Startup) getActivity()).getFragmentTeamTwo().setTeam(panelName);
							dialog.dismiss();
						}
					});
			AlertDialog alert = builder.create();
			alert.show();
		} else {
			// error no teams available
			Toast.makeText(getActivity(), "There are no saved teams to load",
					Toast.LENGTH_SHORT).show();
		}
	}
	
	//delete player 
	OnClickListener deletePlayerListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			deletePlayer();
		}
	};
	
	private void deletePlayer() {
		// get list of player names
					ArrayList<String> panelList = new ArrayList<String>();
					String[] projection = { TeamContentProvider.NAME };
					CursorLoader cL = new CursorLoader(getActivity(), allTitles,
							projection, TeamContentProvider.TEAM + " = '" + panelName + "'", null,
							TeamContentProvider.NAME);
					Cursor c1 = cL.loadInBackground();
					if (c1.getCount() > 0) {
						c1.moveToFirst();
						do {
							panelList.add(c1.getString(c1
									.getColumnIndexOrThrow(TeamContentProvider.NAME)));
						} while (c1.moveToNext());
						panelList.remove("...");
						panel = new String[panelList.size()];
						for (int i = 0; i < panelList.size(); i++) {
							panel[i] = panelList.get(i);
						}
						c1.close();		
						AlertDialog.Builder builder = new AlertDialog.Builder(
								getActivity());
						builder.setTitle("select player to delete");
						builder.setSingleChoiceItems(panel, 0,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										player = panel[which];
										getActivity()
										.getContentResolver()
										.delete(Uri.parse(TeamContentProvider.CONTENT_URI
												+ "/"
												+ playerIDLookUp
												.get(player)), null, null);
								
										getTeam(panelName);
										dialog.dismiss();
									}
								});
						AlertDialog alert = builder.create();
						alert.show();
					} else {
						// error no teams available
						Toast.makeText(getActivity(),
								"There are no players to delete", Toast.LENGTH_SHORT)
								.show();
					}
	}
	
	//delete team
	OnClickListener deleteTeamListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// get list of player names
			deleteTeam();
		}
	};
		
	private void deleteTeam() {
		ArrayList<String> panelList = new ArrayList<String>();
		String str;
		String[] projection = { TeamContentProvider.TEAM };
		CursorLoader cL = new CursorLoader(getActivity(), allTitles,
				projection, null, null, TeamContentProvider.TEAM);
		Cursor c1 = cL.loadInBackground();
		if (c1.getCount() > 0) {
			c1.moveToFirst();
			do {
				str = c1.getString(c1
						.getColumnIndexOrThrow(TeamContentProvider.TEAM));
				if (!panelList.contains(str))
					panelList.add(str);
			} while (c1.moveToNext());
		}
		// don't delete current so remove from list
		panelList.remove(panelName);
		panelList.remove(oppTeamName);
		if (panelList.size() > 0) {
			panel = new String[panelList.size()];
			for (int i = 0; i < panelList.size(); i++) {
				panel[i] = panelList.get(i);
			}
			c1.close();
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle("select team to delete");
			builder.setSingleChoiceItems(panel, 0,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							int count;
							team = panel[which];
							count = getActivity().getContentResolver().delete(
									TeamContentProvider.CONTENT_URI,
									TeamContentProvider.TEAM + " = '" + team
											+ "'", null);
							Toast.makeText(
									getActivity(),
									team + " and " + count + " players deleted",
									Toast.LENGTH_LONG).show();

							dialog.dismiss();
						}
					});
			AlertDialog alert = builder.create();
			alert.show();
		} else {
			// error no teams available
			Toast.makeText(getActivity(), "There are no teams to delete \n\n"+"note: you can't delete teams\n which are currently loaded",
					Toast.LENGTH_LONG).show();
		}
	}
	
	
	// Listener to select team lineup
	OnClickListener teamSetupClickListener = new OnClickListener() {
		@Override
		public void onClick(View w) {
			b = (Button) w;
			ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(
					getActivity(), R.layout.single_row_layout, panelList);
			new AlertDialog.Builder(getActivity())
					.setTitle("select player")
					.setAdapter(adapter1,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// find which position number button has
									// been clicked
									ContentValues values;
									Uri uri;
									String posnNo = getResources()
											.getResourceName(b.getId());
									index = Integer.parseInt(posnNo
											.substring(posnNo.length() - 2,
													posnNo.length()));
									//
									// Deal with Enter New Player
									if (which==0){
										//enter new player dialog
										
											// set up dialog to get filename use edittext in an alertdialog to
											// Prompt for filename											
											AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
											input = new EditText(getActivity());
											input.setId(999);										
											alert.setTitle("enter name of new player");										
											alert.setMessage("Enter Name:");
											alert.setView(input);
											alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
												@Override
												public void onClick(DialogInterface indialog, int which) {
													String inName = input.getText().toString();
													if (inName.length()>2) {													
														//write to database
														ContentValues values = new ContentValues();
														values.put("name", inName);
														values.put("posn", index);
														values.put("team", panelName);
														getActivity().getContentResolver().insert(
																TeamContentProvider.CONTENT_URI, values);	
														//write to teamlist
														teamLineUpCurrent[index] = inName;
														//write to button
														if (b.getText().length() < 3) {
															b.setText(inName);
														}
														else {
															String s = (String) b.getText();
															b.setText(inName);
															panelList.add(s);
															panelList.remove("RESET POSITION TO NUMBER");
															panelList.remove("ENTER NEW PLAYER NAME");
															Collections.sort(panelList);
															panelList.add(0, "RESET POSITION TO NUMBER");
															panelList.add(0, "ENTER NEW PLAYER NAME");		
															//update position of removed player in database
															values = new ContentValues();
															values.put("posn", -1);
															getActivity()
																.getContentResolver()
																.update(Uri.parse(TeamContentProvider.CONTENT_URI
																		+ "/"
																		+ playerIDLookUp
																		.get(s)), values, null, null);
														}	
														getTeam(panelName);
													} else {
														Toast.makeText(getActivity(), "Invalid Name, Try Again\n"+
																"Must be at least 3 characters long",
																Toast.LENGTH_SHORT).show();
													}
												}
											});
											alert.create();
											alert.show();
									}
									
									//Deal with reset
									else if (which==1) {  
										// if its just the number do nothing
										if (b.getText().length() > 2){
											String s = (String) b.getText();
											b.setText(String.valueOf(index));
											teamLineUpCurrent[index]=String.valueOf(index);
												panelList.add(s);
												panelList.remove("RESET POSITION TO NUMBER");
												panelList.remove("ENTER NEW PLAYER NAME");
												Collections.sort(panelList);
												panelList.add(0, "RESET POSITION TO NUMBER");
												panelList.add(0, "ENTER NEW PLAYER NAME");
												values = new ContentValues();
												values.put("posn", -1);
												getActivity()
														.getContentResolver()
														.update(Uri.parse(TeamContentProvider.CONTENT_URI
																+ "/"
																+ playerIDLookUp
																.get(s)), values, null, null);
							
										}
									}
									
									// Where a Player is not already selected
									// for team the button text will be just the
									// position number and so length < 3.
									// assign player to button/teamlineup and
									// remove from panelList
									else if (b.getText().length() < 3) {
										b.setText(panelList.get(which));
										teamLineUpCurrent[index] = panelList
												.get(which);
										panelList.remove(which);
										// write position to database
										values = new ContentValues();
										values.put("posn", index);
										uri = Uri.parse(TeamContentProvider.CONTENT_URI
												+ "/"
												+ playerIDLookUp
														.get(teamLineUpCurrent[index]));
										getActivity()
												.getContentResolver()
												.update(uri, values, null, null);

									}
									// where Player already selected in position
									// need to swap. Assign new player to
									// button/teamlineup. Add swapped out player
									// back into panelList and Sort
									//
									else {
										String s = (String) b.getText();
										b.setText(panelList.get(which));
										teamLineUpCurrent[index] = panelList
												.get(which);
										panelList.remove(which);
										panelList.add(s);
										panelList.remove("RESET POSITION TO NUMBER");
										panelList.remove("ENTER NEW PLAYER NAME");
										Collections.sort(panelList);
										panelList.add(0, "RESET POSITION TO NUMBER");
										panelList.add(0, "ENTER NEW PLAYER NAME");
										//update position of selected player in database
										values = new ContentValues();
										values.put("posn", index);
										uri = Uri.parse(TeamContentProvider.CONTENT_URI
												+ "/"
												+ playerIDLookUp
														.get(teamLineUpCurrent[index]));
										getActivity()
												.getContentResolver()
												.update(uri, values, null, null);
										//update position of removed player in database
										values = new ContentValues();
										values.put("posn", -1);
										uri = Uri.parse(TeamContentProvider.CONTENT_URI
												+ "/"
												+ playerIDLookUp
														.get(s));
										getActivity()
												.getContentResolver()
												.update(uri, values, null, null);
									}
									dialog.dismiss();
								}
							}).create().show();
		}
	};
	
	public void updateCards() {		
		Uri allTitles = TeamContentProvider.CONTENT_URI_2;
		String[] projection = { TeamContentProvider.STATSID,
				TeamContentProvider.STATSLINE };
		CursorLoader cL;
		StringBuilder strBuilder = new StringBuilder();
		cL = new CursorLoader(getActivity(), allTitles, projection, null, null,
				TeamContentProvider.STATSID);
		Cursor c1 = cL.loadInBackground();
		if (c1.getCount() > 0) {
			String str[] = new String[c1.getCount()];
			int i = 0;
			c1.moveToFirst();
			do {
				// insert players into positions
				str[i] = c1.getString(c1
						.getColumnIndexOrThrow(TeamContentProvider.STATSLINE));
					if (((str[i].indexOf("red card") >= 0)
						|| (str[i].indexOf("black card") >= 0) 
						|| (str[i].indexOf("yellow card") >= 0))
						&& (str[i].indexOf(tTeamHome.getText().toString()) >= 0))
				{
					strBuilder.append("\n" + str[i]);
				}
				i++;
			} while (c1.moveToNext());
			// ermove leading line feed
			strBuilder.delete(0, 1);
			tCards.setText(strBuilder.toString());
		}
	}
	
	public void updateSubsList() {		
		Uri allTitles = TeamContentProvider.CONTENT_URI_2;
		String[] projection = { TeamContentProvider.STATSID,
				TeamContentProvider.STATSLINE };
		CursorLoader cL;
		StringBuilder strBuilder = new StringBuilder();
		cL = new CursorLoader(getActivity(), allTitles, projection, null, null,
				TeamContentProvider.STATSID);
		Cursor c1 = cL.loadInBackground();
		if (c1.getCount() > 0) {
			String str[] = new String[c1.getCount()];
			int i = 0;
			c1.moveToFirst();
			do {
				// insert players into positions
				str[i] = c1.getString(c1
						.getColumnIndexOrThrow(TeamContentProvider.STATSLINE));
					if ((str[i].indexOf("--> off:") >= 0) && (str[i].indexOf(panelName)>=0))					
					{
					strBuilder.append("\n" + str[i]);
				}
				i++;
			} while (c1.moveToNext());
			// ermove leading line feed
			strBuilder.delete(0, 1);
			tSubs.setText(strBuilder.toString());
		}
	}
	
	public void setTeam(String team){
		oppTeamName=team;		
	}
	
	public void resetCardsSubs(){
		tCards.setText("");		
		tSubs.setText("");		
	}
	
	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.team1_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
	// set up help menu in action bar
	//@Override
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)	{
		Intent ihelp;

		switch (item.getItemId()) {
		case 0:
			//menu pointer do nothing
		case R.id.helpTeam:
			ihelp = new Intent(getActivity(), HelpActivity.class);
			ihelp.putExtra("HELP_ID", R.string.teamHelp);
			startActivity(ihelp);
			return true;	
			case R.id.deletePlayer:
			deletePlayer();
			return true;
		case R.id.createNewTeam:
			createNewTeam();
			return true;
		case R.id.loadSavedTeam:
			loadTeam();
			return true;
		case R.id.deleteTeam:
			deleteTeam();
			return true;
		case R.id.resetTeam:
			resetTeam();
			return true;
		}	
	    return super.onOptionsItemSelected(item);	
	}
	

}
