/*
 *  MatchRecordFragment.java
 *
 *  Written by: Fintan Mahon 12101524
 *  
 *  Description: GUI to get input and display output for
 *  1. match timer
 *  2. match score
 *  3. match statistics
 *  
 * store data to database tables and pass relevant details into MatchReviewFragment
 *  
 *  Written on: Jan 2013
 *  
 * 
 */
package fm.gaa_scores.plus;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ScoresFragment extends Fragment {

	// declare and initialise variables
	public int minsPerHalf = 30;
	private int homeGoals = 0, homePoints = 0, oppGoals = 0, oppPoints = 0;
	// private Timer timer;
	private TextView tStartTime, tTimeGone, tTimeToGo, tTimeLeft;
	private TextView tHomeTotal, tUpDownDrawText, tHomeDifference, tOppTotal;
	private TextView tStats;
	private TextView tOurTeam, tOppTeam;
	private boolean pause = false;
	private Button bResetAll;
	private Button bStartStop, bDecreaseTime, bIncreaseTime;
	private Button bDecHomeGoals, bHomeGoals, bHomePoints, bDecHomePoints;
	private Button bDecOppGoals, bOppGoals, bOppPoints, bDecOppPoints;
	private Button bShotHome, bShotOpp, bMinsPerHalf;
	public Button bPeriod, bPause;
	private Button bUndo, btweetScore, btweetRecent, bTweetLast;
	private Button btextScore, btextRecent, bTextLast;
	private int statsButton, txtButton, periodInt = 0;
	private String player, stats1, stats2, team, phone, periodStr,
			teamNameInput;
	private EditText tLoc, input;
	private Handler h = new Handler();
	private long starttime = 0;
	private Date currentDate;
	private SimpleDateFormat sdf, sdftime;
	private AlertDialog alertshot = null, alertpitch = null;
	private String[] teamLineUp = new String[26];
	private String[] minsList, tList;
	private boolean[] tListChecked;
	private String[] undoString = new String[6];
	private HashMap<String, Integer> playerIDLookUp = new HashMap<String, Integer>();
	// setup uri to read panel from database
	private ArrayList<String> undoList = new ArrayList();
	private ArrayList<String> playerList = new ArrayList();
	private ArrayList<String> txtList = new ArrayList<String>();
	private ArrayList txtListOut = new ArrayList<String>();
	private long rowId;
	private Uri allTitles = TeamContentProvider.CONTENT_URI_2;
	private Context context;
	private int PLUS = 1, MINUS = -1;
	private int yellow = 0, red = 0;

	@Override
	// start main method to display screen
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.scores_layout, container, false);
		// register tag name
		String myTag = getTag();
		((Startup) getActivity()).setTagFragmentScore(myTag);
		this.setHasOptionsMenu(true);
		context = getActivity();

		// open sharedpreferences file to read in saved persisted data on
		// startup

		SharedPreferences sharedPref = getActivity().getSharedPreferences(
				"team_stats_record_data", Context.MODE_PRIVATE);
		phone = sharedPref.getString("PHONE", "");
		pause = sharedPref.getBoolean("PAUSE", false);

		// get the tag name of this Fragment and pass it up to the parent
		// activity MatchApplication so that this Fragment may be accessed
		// by other fragments through using a reference created from tag name

		// set up text buttons edittexts etc.
		tStartTime = (TextView) v.findViewById(R.id.start_time);
		tTimeGone = (TextView) v.findViewById(R.id.time_gone);
		tTimeToGo = (TextView) v.findViewById(R.id.time_to_go);
		tTimeLeft = (TextView) v.findViewById(R.id.time_left);
		tHomeTotal = (TextView) v.findViewById(R.id.home_total);
		tUpDownDrawText = (TextView) v.findViewById(R.id.up_down_draw_text);
		tHomeDifference = (TextView) v.findViewById(R.id.home_difference);
		tOppTotal = (TextView) v.findViewById(R.id.opp_total);
		tStats = (TextView) v.findViewById(R.id.textViewStats);
		tOurTeam = (TextView) v.findViewById(R.id.ourTeam);
		tOppTeam = (TextView) v.findViewById(R.id.oppTeam);
		tLoc = (EditText) v.findViewById(R.id.etLoc);

		bStartStop = (Button) v.findViewById(R.id.start_stop_timer);
		bPause = (Button) v.findViewById(R.id.pause_timer);
		bDecreaseTime = (Button) v.findViewById(R.id.decrease_timer);
		bIncreaseTime = (Button) v.findViewById(R.id.increase_timer);

		bDecHomeGoals = (Button) v.findViewById(R.id.dec_home_goals);
		bHomeGoals = (Button) v.findViewById(R.id.home_goals);
		bHomePoints = (Button) v.findViewById(R.id.home_points);
		bDecHomePoints = (Button) v.findViewById(R.id.dec_home_points);
		bDecOppGoals = (Button) v.findViewById(R.id.dec_opp_goals);
		bOppGoals = (Button) v.findViewById(R.id.opp_goals);
		bOppPoints = (Button) v.findViewById(R.id.opp_points);
		bDecOppPoints = (Button) v.findViewById(R.id.dec_opp_points);
		bResetAll = (Button) v.findViewById(R.id.reset_all);
		bUndo = (Button) v.findViewById(R.id.buttonUndo);
		bUndo.setOnClickListener(undoOnClickListener);

		// //////////////////////set Team Names//////////////////////////
		// use persisted data if it exists else use default data
		// get team names first
		SharedPreferences sharedPref2 = getActivity().getSharedPreferences(
				"home_team_data", Context.MODE_PRIVATE);
		SharedPreferences sharedPref3 = getActivity().getSharedPreferences(
				"opp_team_data", Context.MODE_PRIVATE);

		tOurTeam.setText(sharedPref2.getString("PANELNAME", "OWN TEAM"));
		tOppTeam.setText(sharedPref3.getString("PANELNAME", "OPPOSITION"));
		if (!sharedPref.getString("LOCATION", tLoc.toString()).equals(""))
			tLoc.setText(sharedPref.getString("LOCATION", ""));

		// ///////////////////MINUTES PER HALF SECTION////////////////////////
		bMinsPerHalf = (Button) v.findViewById(R.id.mins_per_half);
		// set mins per half from saved value if it exists, else default to 30
		bMinsPerHalf.setText(String.valueOf(sharedPref
				.getInt("MINSPERHALF", 30)));
		minsPerHalf = sharedPref.getInt("MINSPERHALF", 30);
		// set click listener for mins per half button
		bMinsPerHalf.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View w) {
				Button b = (Button) w;
				// read list of allowable times from array in assets and put in
				// adapter to display in alertdialog for selection
				minsList = getResources().getStringArray(R.array.minsPerHalf);
				ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(
						getActivity(), R.layout.single_row_layout, minsList);
				new AlertDialog.Builder(getActivity())
						.setTitle("set minutes per half")
						.setAdapter(adapter1,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// convert string input to integer
										minsPerHalf = Integer
												.valueOf(minsList[which]);
										// put new value on button
										bMinsPerHalf.setText(minsList[which]);
										dialog.dismiss();
									}
								}).create().show();
			}
		});

		// //////////////////SET TIME PERIOD/////////////////////////////////
		bPeriod = (Button) v.findViewById(R.id.bPeriod);
		bPeriod.setOnClickListener(periodClickListener);

		// ///////////////////////////TIMER SETUP///////////////////////////
		// retrieve saved value if its there
		tStartTime.setText(sharedPref
				.getString("STARTTEXT", "start time 00:00"));
		tTimeLeft.setText(sharedPref.getString("TIMELEFT", "time left"));

		starttime = sharedPref.getLong("STARTTIME", 0);
		String[] str = new String[2];// stores display text for 1st/2nd half
		// set text on screen according to whether in first half or 2nd half
		// and whether timer is running or not
		if ((sharedPref.getString("TIMERBUTTON", "start").equals("stop"))
				&& (sharedPref.getString("PERIOD", "1st half")
						.equals("1st half"))) {
			str = settTimer("start", "1st half");
			bStartStop.setText(str[0]);
			bPeriod.setText(str[1]);
		} else if ((sharedPref.getString("TIMERBUTTON", "start").equals("stop"))
				&& (sharedPref.getString("PERIOD", "1st half")
						.equals("2nd half"))) {
			str = settTimer("start", "2nd half");
			bStartStop.setText(str[0]);
			bPeriod.setText(str[1]);

		} else if ((sharedPref.getString("TIMERBUTTON", "start").equals("stop"))
				&& (sharedPref.getString("PERIOD", "1st half").equals("ET 1st"))) {
			periodInt = 2;
			str = settTimer("start", "ET 1st");
			bStartStop.setText(str[0]);
			bPeriod.setText(str[1]);

		} else if ((sharedPref.getString("TIMERBUTTON", "start").equals("stop"))
				&& (sharedPref.getString("PERIOD", "1st half").equals("ET 2nd"))) {
			periodInt = 2;
			str = settTimer("start", "ET 2nd");
			bStartStop.setText(str[0]);
			bPeriod.setText(str[1]);

		} else if (sharedPref.getString("TIMERBUTTON", "start").equals("start")) {
			str = settTimer("stop", "2nd half");
			bStartStop.setText(str[0]);
			bPeriod.setText(sharedPref.getString("PERIOD", "1st half"));
		}

		// clicklistener for start/stop button toggle
		bStartStop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (pause) {
					Toast.makeText(getActivity(),
							"Press RESUME to restart timer first",
							Toast.LENGTH_LONG).show();
				} else {
					Button b = (Button) v;
					String sPeriod;
					String locn = "";
					if (tLoc.getText().length() > 0) {
						locn = tLoc.getText() + ". ";
					}
					String[] str = new String[2];
					// write start/time to database
					str = settTimer(b.getText().toString(), bPeriod.getText()
							.toString());

					sdf = new SimpleDateFormat("HH:mm   dd-MM-yy");
					ContentValues values = new ContentValues();
					values.put("sort", System.currentTimeMillis());
					if (starttime > 10) {
						values.put("period", bPeriod.getText().toString());
						values.put("time", getTime());
					}
					if (b.getText().equals("start")) {
						// add to database
						values.put(
								"line",
								locn + bPeriod.getText() + " start: "
										+ sdf.format(starttime) + " "
										+ tOurTeam.getText() + " v. "
										+ tOppTeam.getText());
						values.put("type", "s");

					} else {
						if (bPeriod.getText().equals("1st half")) {
							sPeriod = "Half time score: ";
						} else if (bPeriod.getText().equals("2nd half")) {
							sPeriod = "Full time score: ";
						} else if (bPeriod.getText().equals("ET 1st")) {
							sPeriod = "Score after 1st half extra time: ";
						} else {
							sPeriod = "Score after 2nd half extra time: ";
						}
						values.put(
								"line",
								locn
										+ sPeriod
										+ tOurTeam.getText()
										+ " "
										+ (bHomeGoals.getText().equals("+") ? "0"
												: bHomeGoals.getText())
										+ "-"
										+ (bHomePoints.getText().equals("+") ? "0"
												: bHomePoints.getText())
										+ tHomeTotal.getText()
										+ "  "
										+ tOppTeam.getText()
										+ " "
										+ (bOppGoals.getText().equals("+") ? "0"
												: bOppGoals.getText())
										+ "-"
										+ (bOppPoints.getText().equals("+") ? "0"
												: bOppPoints.getText())
										+ tOppTotal.getText());
						values.put("type", "s");
					}
					getActivity().getContentResolver().insert(
							TeamContentProvider.CONTENT_URI_2, values);
					updateStatsList(true);
					((Startup) getActivity()).getFragmentEvent().fillData();
					b.setText(str[0]);
					bPeriod.setText(str[1]);
				}
			}
		});

		// clicklistener for increment time button
		// if clicked add a minute to the timer be subtracting a minute from the
		// timer starttime. Update starttime text too
		bIncreaseTime.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (starttime != 0) {
					starttime = starttime - 30000;
					currentDate = new Date(starttime);
					sdf = new SimpleDateFormat("HH:mm:ss   dd-MM-yy");
					tStartTime.setText("Start Time: " + sdf.format(currentDate));
				}
			}
		});

		// clicklistener for decrement time button
		// if clicked take a minute to the timer be subtracting a minute from
		// the
		// timer starttime. Update starttime text too
		bDecreaseTime.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if ((starttime != 0)
						&& (System.currentTimeMillis() - starttime > 30000)) {
					starttime = starttime + 30000;
					currentDate = new Date(starttime);
					sdf = new SimpleDateFormat("HH:mm:ss   dd-MM-yyyy");
					tStartTime.setText("Start Time: " + sdf.format(currentDate));
				}
			}
		});

//		bPause.setOnClickListener(pauseOnClickListener);

		// /////////////////////////////SCORE///////////////////////////////////////
		// one clickListener handles all input from score buttons
		bDecHomeGoals.setOnClickListener(scoreAddClickListener);
		bHomeGoals.setOnClickListener(scoreAddClickListener);
		bHomePoints.setOnClickListener(scoreAddClickListener);
		bDecHomePoints.setOnClickListener(scoreAddClickListener);
		bDecOppGoals.setOnClickListener(scoreAddClickListener);
		bOppGoals.setOnClickListener(scoreAddClickListener);
		bOppPoints.setOnClickListener(scoreAddClickListener);
		bDecOppPoints.setOnClickListener(scoreAddClickListener);

		// ///////HANDLE SCORES FROM PERSISTED SHARED PREFERENCES////
		homeGoals = sharedPref.getInt("HOMEGOALS", 0);
		homePoints = sharedPref.getInt("HOMEPOINTS", 0);
		oppGoals = sharedPref.getInt("OPPGOALS", 0);
		oppPoints = sharedPref.getInt("OPPPOINTS", 0);

		if (homeGoals + homePoints + oppGoals + oppPoints > 0) {
			bHomeGoals.setText(String.valueOf(homeGoals));
			bHomePoints.setText(String.valueOf(homePoints));
			bOppGoals.setText(String.valueOf(oppGoals));
			bOppPoints.setText(String.valueOf(oppPoints));
			setTotals();
		}

		// stats button click listener just diplays message to longpress
		bResetAll.setOnClickListener(resetClickListener);

		// reset stats button click listener
		// set all stats back to zero on REVIEW fragment screen
		bResetAll.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				resetStats();
				resetTime();
				resetScore();
				tLoc.setText("");
				v.playSoundEffect(SoundEffectConstants.CLICK);
				return true;
			}
		});

		// ////////////////////SHOT STATS SETUP///////////////////
		tStats = (TextView) v.findViewById(R.id.textViewStats);
		bShotHome = (Button) v.findViewById(R.id.buttonShotHome);
		bShotHome.setOnClickListener(statsClickListener);
		bShotOpp = (Button) v.findViewById(R.id.buttonShotOpp);
		bShotOpp.setOnClickListener(statsClickListener);

		// populate undolist
		String[] projection = { TeamContentProvider.STATSID,
				TeamContentProvider.STATSLINE, TeamContentProvider.STATSSORT };
		CursorLoader cL;
		cL = new CursorLoader(getActivity(), allTitles, projection, null, null,
				TeamContentProvider.STATSSORT);
		Cursor c1 = cL.loadInBackground();
		if (c1.getCount() > 0) {
			c1.moveToFirst();
			do {
				// read in player nicknames
				undoList.add(c1.getString(c1
						.getColumnIndexOrThrow(TeamContentProvider.STATSLINE)));
				// insert players into positions
			} while (c1.moveToNext());
		} else {
			tStats.setText("");
		}
		String undo1 = "", undo2 = "", undo3 = "", undo4 = "";
		if (c1.getCount() >= 4) {
			undo1 = undoList.get(c1.getCount() - 1);
			undo2 = undoList.get(c1.getCount() - 2);
			undo3 = undoList.get(c1.getCount() - 3);
			undo4 = undoList.get(c1.getCount() - 4);
			tStats.setText(undo1 + "\n" + undo2 + "\n" + undo3 + "\n" + undo4);
		} else if (c1.getCount() == 3) {
			undo1 = undoList.get(c1.getCount() - 1);
			undo2 = undoList.get(c1.getCount() - 2);
			undo3 = undoList.get(c1.getCount() - 3);
			tStats.setText(undo1 + "\n" + undo2 + "\n" + undo3);
		} else if (c1.getCount() == 2) {
			undo1 = undoList.get(c1.getCount() - 1);
			undo2 = undoList.get(c1.getCount() - 2);
			tStats.setText(undo1 + "\n" + undo2);
		} else if (c1.getCount() == 1) {
			undo1 = undoList.get(c1.getCount() - 1);
			tStats.setText(undo1);
		} // call update in review
		c1.close();

		btweetScore = (Button) v.findViewById(R.id.bTweetScore);
		btweetScore.setOnClickListener(tweetScoreListener);
		bTweetLast = (Button) v.findViewById(R.id.bTweetLast);
		bTweetLast.setOnClickListener(tweetLastListener);
		btweetRecent = (Button) v.findViewById(R.id.bTweetRecent);
		btweetRecent.setOnClickListener(tweetRecentListener);
		btextScore = (Button) v.findViewById(R.id.bTextScore);
		btextScore.setOnClickListener(tweetScoreListener);
		bTextLast = (Button) v.findViewById(R.id.bTextLast);
		bTextLast.setOnClickListener(tweetLastListener);
		btextRecent = (Button) v.findViewById(R.id.bTextRecent);
		btextRecent.setOnClickListener(tweetRecentListener);
		return v;
	}

	// ********************************************************************//
	// ///////////////////////////END OF ONCREATE SECTION //////////////////
	// ********************************************************************//

	@Override
	public void onPause() {
		// Save/persist data to be used on reopen
		super.onPause(); // Always call the superclass method first
		SharedPreferences sharedPref = getActivity().getSharedPreferences(
				"team_stats_record_data", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putInt("MINSPERHALF", minsPerHalf);
		editor.putLong("STARTTIME", starttime);
		editor.putString("PERIOD", bPeriod.getText().toString());
		editor.putInt("HOMEGOALS", homeGoals);
		editor.putInt("HOMEPOINTS", homePoints);
		editor.putInt("OPPGOALS", oppGoals);
		editor.putInt("OPPPOINTS", oppPoints);
		editor.putString("LOCATION", tLoc.getText().toString());
		editor.putString("TIMELEFT", tTimeLeft.getText().toString());
		editor.putString("TIMERBUTTON", bStartStop.getText().toString());
		editor.putString("OWNTEAM", tOurTeam.getText().toString());
		editor.putString("OPPTEAM", tOppTeam.getText().toString());
		editor.putString("STARTTEXT", tStartTime.getText().toString());
		editor.putString("PHONE", phone);
		editor.putBoolean("PAUSE", pause);
		editor.commit();
	}

	public String getScore(boolean fromTeam) {
		// String str = getTime().equals("") ? "" : getTime() + "mins ";
		String str = "";
		String str1 = bPeriod.getText().toString();
		String str2 = tStartTime.getText().toString();
		if (getTime().equals("")) {
			if (str2.contains("Start T")) {
				str = "";
			} else if ((str2.contains("1st Half")) && (!str2.contains("Extra"))) {
				str = "Half time ";
			} else if ((str2.contains("2nd Half")) && (!str2.contains("Extra"))) {
				str = "Full time ";

			} else if (str2.contains("Time-1")) {
				str = "Extra time, half time ";
			} else if (str2.contains("Time-2")) {
				str = "Extra fime, full time ";
			}

		} else {
			str = getTime() + "mins " + bPeriod.getText() + " ";
		}

		String comment = tLoc.getText().length() <= 1 ? "" : (tLoc.getText()
				.toString() + "\n");
		if (fromTeam) {
			return str;
		} else {
			return comment
					+ str
					+ tOurTeam.getText()
					+ ":"
					+ (bHomeGoals.getText().equals("+") ? "0" : bHomeGoals
							.getText())
					+ "-"
					+ (bHomePoints.getText().equals("+") ? "0" : bHomePoints
							.getText())
					+ tHomeTotal.getText()
					+ "  "
					+ tOppTeam.getText()
					+ ":"
					+ (bOppGoals.getText().equals("+") ? "0" : bOppGoals
							.getText())
					+ "-"
					+ (bOppPoints.getText().equals("+") ? "0" : bOppPoints
							.getText()) + tOppTotal.getText() + ".";
		}
	}

	OnClickListener tweetRecentListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			txtButton = ((Button) v).getId();
			// get list of stuff to tweet from dialog

			Uri allTitles = TeamContentProvider.CONTENT_URI_2;
			String[] projection = { TeamContentProvider.STATSLINE };
			CursorLoader cL;
			cL = new CursorLoader(getActivity(), allTitles, projection, null,
					null, TeamContentProvider.STATSSORT + " desc");
			Cursor c1 = cL.loadInBackground();
			txtList.clear();// ////////////??????????
			txtListOut.clear();
			if (c1.getCount() > 0) {
				c1.moveToFirst();
				do {
					// read in events
					txtList.add(c1.getString(c1
							.getColumnIndexOrThrow(TeamContentProvider.STATSLINE)));
				} while (c1.moveToNext());
			}
			c1.close();
			// add in score
			int j = 1;
			txtList.add(
					0,
					getScore(false).replace((tLoc.getText().toString() + "\n"),
							""));
			// add in comment if it exists
			if (tLoc.getText().length() > 1) {
				txtList.add(0, tLoc.getText().toString());
				j = 2;
			}
			// just get last 10 events
			if (txtList.size() >= 10) {
				tList = new String[10];
				tListChecked = new boolean[10];
			} else {
				tList = new String[txtList.size()];
				tListChecked = new boolean[txtList.size()];
			}

			if (j == 1) {
				tList[0] = txtList.get(0);
			} else {
				tList[0] = txtList.get(0);
				tList[1] = txtList.get(1);
			}

			String strA;
			for (int i = 0 + j; i < (txtList.size() > 10 ? 10 : txtList.size()); i++) {
				// parse string to get rid of time stamp
				strA = txtList.get(i);
				if (((strA.contains("1st half")) || (strA.contains("2nd half")))
						&& (strA.contains("mins "))) {
					tList[i] = strA.substring(strA.indexOf("half") + 5,
							strA.length());
				} else if (((strA.contains("ET 1st")) || (strA
						.contains("ET 2nd")))) {
					tList[i] = strA.substring(strA.indexOf("ET") + 7,
							strA.length());
				} else {
					tList[i] = strA;
				}

				tListChecked[i] = false;
			}

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle("select what to tweet/text");
			builder.setMultiChoiceItems(tList, tListChecked,
					new DialogInterface.OnMultiChoiceClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which,
								boolean isChecked) {
							if (isChecked) {
								// If the user checked the item, add it
								// to the selected items
								txtListOut.add(which);
								tListChecked[which] = true;
							} else if (txtListOut.contains(which)) {
								// Else, if the item is already in the
								// array, remove it
								txtListOut.remove(Integer.valueOf(which));
								tListChecked[which] = false;
							}
						}
					});
			builder.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							// call method to display list of players
							StringBuilder str = new StringBuilder("");
							for (int i = 0; i < txtListOut.size(); i++) {
								str = str.append(tList[(Integer) txtListOut
										.get(i)] + " ");
							}
							switch (txtButton) {
							case R.id.bTweetRecent:
								try {
									Intent shareIntent = findTwitterClient();
									shareIntent.putExtra(Intent.EXTRA_TEXT,
											str.toString());
									startActivity(Intent.createChooser(
											shareIntent, "Share"));
								} catch (Exception ex) {
									Log.e("Error in Tweet1", ex.toString());
									Toast.makeText(
											getActivity(),
											"Can't find twitter client\n"
													+ "Please install Twitter App\nand login to Twitter",
											Toast.LENGTH_LONG).show();
								}
								break;
							case R.id.bTextRecent:
								try {

									Intent intentText = new Intent(
											Intent.ACTION_VIEW);
									intentText.putExtra("sms_body",
											str.toString());
									intentText.setData(Uri
											.parse("sms:" + phone));
									startActivity(intentText);
								} catch (Exception ex) {
									Log.e("Error in Text", ex.toString());
									Toast.makeText(getActivity(),
											"Unable to send text message",
											Toast.LENGTH_LONG).show();
								}
								break;
							}
						}
					});
			AlertDialog alert = builder.create();
			alert.show();

		}
	};

	OnClickListener tweetLastListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			txtButton = ((Button) v).getId();
			String strA = "", str = "";
			Uri allTitles = TeamContentProvider.CONTENT_URI_2;
			String[] projection = { TeamContentProvider.STATSLINE };
			CursorLoader cL;
			cL = new CursorLoader(getActivity(), allTitles, projection, null,
					null, TeamContentProvider.STATSID + " desc");
			Cursor c1 = cL.loadInBackground();
			txtList.clear();// ////////////??????????
			txtListOut.clear();
			if (c1.getCount() > 0) {
				c1.moveToFirst();
				// read in player nicknames
				strA = " "
						+ c1.getString(c1
								.getColumnIndexOrThrow(TeamContentProvider.STATSLINE));
			}

			if (((strA.contains("1st half")) || (strA.contains("2nd half")))
					&& (strA.contains("mins "))) {
				str = strA.substring(strA.indexOf("half") + 5, strA.length());
			} else if (((strA.contains("ET 1st")) || (strA.contains("ET 2nd")))) {
				str = strA.substring(strA.indexOf("ET") + 7, strA.length());
			} else {
				str = strA;
			}

			switch (txtButton) {
			case R.id.bTweetLast:
				try {
					Intent shareIntent = findTwitterClient();
					shareIntent.putExtra(Intent.EXTRA_TEXT, getScore(false)
							+ str);
					startActivity(Intent.createChooser(shareIntent, "Share"));
				} catch (Exception ex) {
					Log.e("Error in Tweet", ex.toString());
					Toast.makeText(
							getActivity(),
							"Can't find twitter client\n"
									+ "Please install Twitter App\nand login to Twitter",
							Toast.LENGTH_LONG).show();
				}
				break;
			case R.id.bTextLast:
				try {
					Intent intentText = new Intent(Intent.ACTION_VIEW);
					intentText.setType("vnd.android-dir/mms-sms");
					intentText.putExtra("sms_body", getScore(false) + str);
					intentText.setData(Uri.parse("sms: " + phone));
					startActivity(intentText);
				} catch (Exception ex) {
					Log.e("Error in Text", ex.toString());
					Toast.makeText(getActivity(),
							"Unable to send text message", Toast.LENGTH_LONG)
							.show();
				}
				break;
			}
		}
	};

	OnClickListener tweetScoreListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			txtButton = ((Button) v).getId();

			switch (txtButton) {
			case R.id.bTweetScore:
				try {
					Intent shareIntent = findTwitterClient();
					shareIntent.putExtra(Intent.EXTRA_TEXT, getScore(false));
					startActivity(Intent.createChooser(shareIntent, "Share"));
				} catch (Exception ex) {
					Log.e("Error in Tweet", ex.toString());
					Toast.makeText(
							getActivity(),
							"Can't find twitter client\n"
									+ "Please install Twitter App\nand login to Twitter",
							Toast.LENGTH_LONG).show();
				}
				break;
			case R.id.bTextScore:
				try {
					Intent intentText = new Intent(Intent.ACTION_VIEW);
					intentText.setType("vnd.android-dir/mms-sms");
					intentText.putExtra("sms_body", getScore(false));
					intentText.setData(Uri.parse("sms: " + phone));
					startActivity(intentText);
				} catch (Exception ex) {
					Log.e("Error in Text", ex.toString());
					Toast.makeText(getActivity(),
							"Unable to send text message", Toast.LENGTH_LONG)
							.show();
				}
				break;
			}
		}
	};

	public Intent findTwitterClient() {
		final String[] twitterApps = {
				// package // name - nb installs (thousands)
				"com.twitter.android", // official - 10 000
				"com.twidroid", // twidroid - 5 000
				"com.handmark.tweetcaster", // Tweecaster - 5 000
				"com.thedeck.android" }; // TweetDeck - 5 000 };
		Intent tweetIntent = new Intent();
		tweetIntent.setType("text/plain");
		final PackageManager packageManager = this.context.getPackageManager();
		List<ResolveInfo> list = packageManager.queryIntentActivities(
				tweetIntent, PackageManager.MATCH_DEFAULT_ONLY);

		for (int i = 0; i < twitterApps.length; i++) {
			for (ResolveInfo resolveInfo : list) {
				String p = resolveInfo.activityInfo.packageName;
				if (p != null && p.startsWith(twitterApps[i])) {
					tweetIntent.setPackage(p);
					return tweetIntent;
				}
			}
		}
		return null;
	}

	// for reset buttons diplay message to long click, won't work with ordinary
	// click
	OnClickListener resetClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// get reference to REVIEW fragment from parent activity
			// MatchApplication and use reference to execute resetStats
			// method in REVIEW fragment which will reset stats there to 0
			Toast.makeText(getActivity(), "Long Press to Reset",
					Toast.LENGTH_SHORT).show();
		}
	};

	private void resetTime() {

		h.removeCallbacks(run);
		pauseOff();
		tTimeLeft.setText("time left");
		bStartStop.setText("start");
		tTimeGone.setText("00:00");
		tTimeToGo.setText("00:00");
		tStartTime.setText("Start Time: 00:00");
		bPeriod.setText(getResources().getStringArray(R.array.periodShort)[0]);
		starttime = 0;
		periodInt = 0;
	}

	private void resetScore() {
		// reset score in this fragment and also on REVIEW fragment
		bHomeGoals.setText("+");
		homeGoals = homePoints = oppGoals = oppPoints = 0;
		// reset score in REVIEW fragment
		// get reference to REVIEW fragment from parent activity
		// MatchApplication and use reference to execute setHomeGoals
		// method in REVIEW fragment which will reset score there to 0
		((Startup) getActivity()).getFragmentReview().settHomeGoals(0);
		bHomePoints.setText("+");
		((Startup) getActivity()).getFragmentReview().settHomePoints(0);
		bOppGoals.setText("+");
		((Startup) getActivity()).getFragmentReview().settOppGoals(0);
		bOppPoints.setText("+");
		((Startup) getActivity()).getFragmentReview().settOppPoints(0);
		tHomeTotal.setText("(0)");
		tOppTotal.setText("(0)");
		tUpDownDrawText.setText("drawn game. ");
		tHomeDifference.setText(" ");
	}

	private void resetStats() {
		// get reference to REVIEW fragment from parent activity
		// MatchApplication and use reference to execute resetStats
		// method in REVIEW fragment which will reset stats there to 0

		Toast.makeText(getActivity(), "Stats Reset", Toast.LENGTH_SHORT).show();
		// delete stats in database
		getActivity().getContentResolver().delete(
				TeamContentProvider.CONTENT_URI_2, null, null);
		getActivity().getContentResolver().delete(
				TeamContentProvider.CONTENT_URI_3, null, null);
		((Startup) getActivity()).getFragmentScorers().fillData();
		((Startup) getActivity()).getFragmentReview().resetStats();
		// ((Startup) getActivity()).getFragmentTeamOne().resetCardsSubs();
		// ((Startup) getActivity()).getFragmentTeamTwo().resetCardsSubs();
		((Startup) getActivity()).getFragmentEvent().fillData();
		updateStatsList(true);
		tStats.setText("");
		// delete image files in dir
		File dir = new File(Environment.getExternalStorageDirectory(),
				"gaa_app_sysfiles");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		File files[] = dir.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().contains("GAAScoresStatsTeam_")
					|| files[i].getName().contains(
							"GAAScoresStatsTeamSelection_")) {
				files[i].delete();
			}
		}
	}

	// Run Match timer section. Set text strings and timer based on 4
	// possibilities:
	// 1. ready to start first half
	// 2. first half running
	// 3. first half ended ready to start second half
	// 4. second half running
	private String[] settTimer(String bStr, String bHalf) {
		String[] str = new String[2];
		sdf = new SimpleDateFormat("HH:mm:ss   dd-MM-yyyy");
		sdftime = new SimpleDateFormat("HH:mm:ss");

		if (bStr.equals("start") && bHalf.equals("2nd half")) {
			// 3. first half ended ready to start second half
			if (starttime == 0)
				starttime = System.currentTimeMillis();
			currentDate = new Date(starttime);
			if (periodInt < 2) {
				tStartTime
						.setText("2nd Half Start: " + sdf.format(currentDate));
			} else {
				tStartTime.setText("Extra Time-2nd Half Start: "
						+ sdf.format(currentDate));
			}

			tTimeLeft.setText("time left");// new
			h.postDelayed(run, 0);
			str[0] = "stop";
			str[1] = "2nd half";
			bPeriod.setOnClickListener(nullPeriodClickListener);
			bPause.setOnClickListener(pauseOnClickListener);
			return str;

		} else if (bStr.equals("start") && bHalf.equals("ET 2nd")) {
			// 3. first half ET ended ready to start second half
			if (starttime == 0)
				starttime = System.currentTimeMillis();
			currentDate = new Date(starttime);
			if (periodInt < 2) {
				tStartTime
						.setText("2nd Half Start: " + sdf.format(currentDate));
			} else {
				tStartTime.setText("Extra Time-2nd Half Start: "
						+ sdf.format(currentDate));
			}

			tTimeLeft.setText("time left");// new
			h.postDelayed(run, 0);
			str[0] = "stop";
			str[1] = "ET 2nd";
			bPeriod.setOnClickListener(nullPeriodClickListener);
			bPause.setOnClickListener(pauseOnClickListener);
			return str;

		} else if ((bStr.equals("stop") && (bHalf.equals("2nd half") || bHalf
				.equals("ET 2nd")))) {
			// 4. second half running

			h.removeCallbacks(run);
			starttime = 0;
			str[0] = "start";
			str[1] = "1st half";
			bPeriod.setOnClickListener(periodClickListener);
			bPause.setOnClickListener(null);
			return str;

		} else if (bStr.equals("stop") && bHalf.equals("1st half")) {
			// 2. first half running
			h.removeCallbacks(run);
			starttime = 0;
			str[0] = "start";
			str[1] = "2nd half";
			bPeriod.setOnClickListener(periodClickListener);
			bPause.setOnClickListener(null);
			return str;

		} else if (bStr.equals("stop") && bHalf.equals("ET 1st")) {
			// 2. first half running
			h.removeCallbacks(run);
			starttime = 0;
			str[0] = "start";
			str[1] = "ET 2nd";
			bPeriod.setOnClickListener(periodClickListener);
			bPause.setOnClickListener(null);
			return str;

		} else {
			// 1. ready to start first half
			if (starttime == 0)
				starttime = System.currentTimeMillis();
			currentDate = new Date(starttime);
			sdf = new SimpleDateFormat("HH:mm:ss   dd-MM-yyyy");
			if (periodInt < 2) {
				tStartTime
						.setText("1st Half Start: " + sdf.format(currentDate));
			} else {
				tStartTime.setText("Extra Time-1st Half Start: "
						+ sdf.format(currentDate));
			}

			tTimeLeft.setText("time left");// new

			h.postDelayed(run, 0);
			str[0] = "stop";
			if (bHalf.equals("1st half")) {
				str[1] = "1st half";
			} else {
				str[1] = "ET 1st";
			}
			bPeriod.setOnClickListener(nullPeriodClickListener);
			bPause.setOnClickListener(pauseOnClickListener);
			return str;
		}
	}

	// //////////////////////////////////////////////////////////////////////
	// method to update score and update shots data in review scrreen
	// input=1 is from eventslistfragment
	public void updateStatsDatabase(String teamName, String stats1In,
			String stats2In, String playerIn, int count, int input) {
		stats1 = stats1In;
		stats2 = stats2In;
		player = playerIn;
		if (teamName.equals(tOurTeam.getText().toString())) {
			// for home team commit
			// WRITE TO REVIEW PAGE///////////////////////////////////
			if (stats1.equals("goal")) {
				// increment goal counter
				if (homeGoals + count >= 0) {
					homeGoals = homeGoals + count;
					bHomeGoals.setText(String.valueOf(homeGoals));
					// update totals
					setTotals();
					// increment score in REVIEW fragment
					((Startup) getActivity()).getFragmentReview()
							.settHomeGoals(homeGoals);
					// change display from + to 0 if first score
					if (bHomePoints.getText().equals("+"))
						bHomePoints.setText("0");
					// remind user score is updated in case they try and do it
					// manually
					Toast.makeText(getActivity(), "Score Updated",
							Toast.LENGTH_SHORT).show();
				}
			} else if (stats1.equals("point")) {
				// increment points counter
				if (homePoints + count >= 0) {
					homePoints = homePoints + count;
					bHomePoints.setText(String.valueOf(homePoints));
					// update totals
					setTotals();
					// increment score in REVIEW fragment
					((Startup) getActivity()).getFragmentReview()
							.settHomePoints(homePoints);
					// change display from + to 0 if first score
					if (bHomeGoals.getText().equals("+"))
						bHomeGoals.setText("0");
					// remind user score is updated in case they try and do it
					// manually
					Toast.makeText(getActivity(), "Score Updated",
							Toast.LENGTH_SHORT).show();
				}
			}
		} else if (teamName.equals(tOppTeam.getText().toString())) {
			// for opposition team
			// WRITE TO REVIEW PAGE///////////////////////////////////
			if (stats1.equals("goal")) {
				// increment goal counter
				if (oppGoals + count >= 0) {
					oppGoals = oppGoals + count;
					bOppGoals.setText(String.valueOf(oppGoals));
					// update totals
					setTotals();
					// increment score in REVIEW fragment
					((Startup) getActivity()).getFragmentReview().settOppGoals(
							oppGoals);
					if (bOppPoints.getText().equals("+"))
						bOppPoints.setText("0");
					Toast.makeText(getActivity(), "Score Updated",
							Toast.LENGTH_SHORT).show();
				}
			} else if (stats1.equals("point")) {
				// increment points counter
				if (oppPoints + count >= 0) {
					oppPoints = oppPoints + count;
					bOppPoints.setText(String.valueOf(oppPoints));
					// update totals
					setTotals();
					// increment score in REVIEW fragment
					((Startup) getActivity()).getFragmentReview()
							.settOppPoints(oppPoints);
					if (bOppGoals.getText().equals("+"))
						bOppGoals.setText("0");
					Toast.makeText(getActivity(), "Score Updated",
							Toast.LENGTH_SHORT).show();
				}
			}

		}
		// add to stats database
		// input=1 is from eventslistfragment
		if (!(stats1.equals("") && stats2.equals("") && player.equals(""))
				&& input == 0) {
			ContentValues values = new ContentValues();
			String temp1 = "", temp2 = "";
			if (starttime > 10) {
				temp1 = getTime();
				temp2 = bPeriod.getText().toString();
				values.put("line", temp1 + "mins " + temp2 + " " + teamName
						+ " " + stats1 + " " + stats2 + " " + player);
			} else {
				values.put("line", teamName + " " + stats1 + " " + stats2 + " "
						+ player);
			}
			values.put("type", "t");
			values.put("sort", System.currentTimeMillis());
			values.put("time", temp1);
			values.put("team", teamName);
			values.put("player", player);
			values.put("period", temp2);
			values.put("stats1", stats1);
			values.put("stats2", stats2);
			getActivity().getContentResolver().insert(
					TeamContentProvider.CONTENT_URI_2, values);
		}
		if ((stats2.equals("red card")) || (stats2.equals("yellow card"))
				|| (stats2.equals("black card"))) {
			((Startup) getActivity()).getFragmentReview().updateCardsSubs();
		}
		// add to scorers database
		if (!teamName.equals("")) {
			updateScorers(stats1, stats2, player, teamName, PLUS);
		}

		// update display list
		((Startup) getActivity()).getFragmentReview().fillData();
		((Startup) getActivity()).getFragmentEvent().fillData();
		updateStatsList(true);
	}

	private void updateScorers(String stats1Temp, String stats2Temp,
			String playerIn, String teamTemp, int NUMBER) {
		String playerThis = (playerIn.equals("")) ? "---" : playerIn;
		// update scores and misses
		if ((stats1Temp.equals("goal")) || (stats1Temp.equals("point"))
				|| (stats1Temp.equals("wide")) || (stats1Temp.equals("saved"))
				|| (stats1Temp.equals("short"))
				|| (stats1Temp.equals("off posts"))
				|| (stats1Temp.equals("out for 45/65"))) {
			int goal = 0, point = 0, goalF = 0, pointF = 0, miss = 0, missF = 0, id;
			// deal with goal
			if (stats1Temp.equals("goal")) {
				goal = NUMBER;
				if ((stats2Temp.equals("from free"))
						|| (stats2Temp.equals("from penalty"))
						|| (stats2Temp.equals("from sideline"))
						|| (stats2Temp.equals("from 45/65"))) {
					goalF = NUMBER;
				}
			}
			// deal with point
			else if (stats1Temp.equals("point")) {
				point = NUMBER;
				if ((stats2Temp.equals("from free"))
						|| (stats2Temp.equals("from penalty"))
						|| (stats2Temp.equals("from sideline"))
						|| (stats2Temp.equals("from 45/65"))) {
					pointF = NUMBER;
				}
			} else if ((stats1Temp.equals("wide"))
					|| (stats1Temp.equals("off posts"))
					|| (stats1Temp.equals("saved"))
					|| (stats1Temp.equals("short"))
					|| (stats1Temp.equals("out for 45/65"))) {
				miss = NUMBER;
				if ((stats2Temp.equals("from free"))
						|| (stats2Temp.equals("from penalty"))
						|| (stats2Temp.equals("from sideline"))
						|| (stats2Temp.equals("from 45/65"))) {
					missF = NUMBER;
				}
			}

			// check if entry in database for player name and team
			Uri allTitles = TeamContentProvider.CONTENT_URI_3;
			String[] args = { playerThis, teamTemp };
			Cursor c1 = getActivity().getContentResolver().query(allTitles,
					null, "name=? AND team=?", args, null);
			if (c1.getCount() <= 0) {
				// add new entry to database
				ContentValues values = new ContentValues();
				values.put(TeamContentProvider.SCORESNAME, playerThis);
				values.put(TeamContentProvider.SCORESTEAM, teamTemp);
				values.put(TeamContentProvider.SCORESGOALS, goal);
				values.put(TeamContentProvider.SCORESPOINTS, point);
				values.put(TeamContentProvider.SCORESTOTAL, (goal * 3) + point);
				values.put(TeamContentProvider.SCORESGOALSFREE, goalF);
				values.put(TeamContentProvider.SCORESPOINTSFREE, pointF);
				values.put(TeamContentProvider.SCORESMISS, miss);
				values.put(TeamContentProvider.SCORESMISSFREE, missF);
				getActivity().getContentResolver().insert(
						TeamContentProvider.CONTENT_URI_3, values);
				((Startup) getActivity()).getFragmentScorers().fillData();
			} else if (c1.getCount() == 1) {
				// update entry to database
				c1.moveToFirst();
				goal = goal
						+ c1.getInt(c1
								.getColumnIndexOrThrow(TeamContentProvider.SCORESGOALS));
				point = point
						+ c1.getInt(c1
								.getColumnIndexOrThrow(TeamContentProvider.SCORESPOINTS));
				goalF = goalF
						+ c1.getInt(c1
								.getColumnIndexOrThrow(TeamContentProvider.SCORESGOALSFREE));
				pointF = pointF
						+ c1.getInt(c1
								.getColumnIndexOrThrow(TeamContentProvider.SCORESPOINTSFREE));
				miss = miss
						+ c1.getInt(c1
								.getColumnIndexOrThrow(TeamContentProvider.SCORESMISS));
				missF = missF
						+ c1.getInt(c1
								.getColumnIndexOrThrow(TeamContentProvider.SCORESMISSFREE));
				id = c1.getInt(c1
						.getColumnIndexOrThrow(TeamContentProvider.SCORESID));
				ContentValues values = new ContentValues();
				values.put(TeamContentProvider.SCORESNAME, playerThis);
				values.put(TeamContentProvider.SCORESTEAM, teamTemp);
				values.put(TeamContentProvider.SCORESGOALS, goal);
				values.put(TeamContentProvider.SCORESPOINTS, point);
				values.put(TeamContentProvider.SCORESTOTAL, (goal * 3) + point);
				values.put(TeamContentProvider.SCORESGOALSFREE, goalF);
				values.put(TeamContentProvider.SCORESPOINTSFREE, pointF);
				values.put(TeamContentProvider.SCORESMISS, miss);
				values.put(TeamContentProvider.SCORESMISSFREE, missF);
				Uri uri;
				if (goal + point + goalF + pointF + miss + missF == 0) {
					uri = Uri.parse(TeamContentProvider.CONTENT_URI_3 + "/"
							+ id);
					getActivity().getContentResolver().delete(uri, null, null);
				} else {
					uri = Uri.parse(TeamContentProvider.CONTENT_URI_3 + "/"
							+ id);
					getActivity().getContentResolver().update(uri, values,
							null, null);
				}
				((Startup) getActivity()).getFragmentScorers().fillData();
			} else {
				Toast.makeText(getActivity(),
						"error accessing scorers database", Toast.LENGTH_LONG)
						.show();
			}
			c1.close();
		}
	}

	public void updateStatsList(boolean updateOthers) {
		String line_ = "";
		String stats1_ = "";
		String stats2_ = "";
		String player_ = "";
		String type = "";
		String time = "";
		String period = "";
		String teamm = "";
		String subon = "";
		String suboff = "";
		String blood = "";
		// load panel from database and assign to arraylist
		Uri allTitles = TeamContentProvider.CONTENT_URI_2;
		String[] projection = { TeamContentProvider.STATS1,
				TeamContentProvider.STATS2, TeamContentProvider.STATSPLAYER,
				TeamContentProvider.STATSTYPE, TeamContentProvider.STATSTEAM,
				TeamContentProvider.STATSTIME, TeamContentProvider.STATSPERIOD,
				TeamContentProvider.STATSSUBON, TeamContentProvider.STATSLINE,
				TeamContentProvider.STATSSUBOFF, TeamContentProvider.STATSBLOOD };
		CursorLoader cL;
		cL = new CursorLoader(getActivity(), allTitles, projection, null, null,
				TeamContentProvider.STATSSORT + " desc");
		Cursor c1 = cL.loadInBackground();
		undoList.clear();
		if (c1.getCount() > 0) {
			c1.moveToFirst();
			int i = 0;
			do {
				teamm = c1.getString(c1
						.getColumnIndexOrThrow(TeamContentProvider.STATSTEAM));
				line_ = c1.getString(c1
						.getColumnIndexOrThrow(TeamContentProvider.STATSLINE));
				stats1_ = c1.getString(c1
						.getColumnIndexOrThrow(TeamContentProvider.STATS1));
				stats2_ = c1.getString(c1
						.getColumnIndexOrThrow(TeamContentProvider.STATS2));
				player_ = c1
						.getString(c1
								.getColumnIndexOrThrow(TeamContentProvider.STATSPLAYER));
				type = c1.getString(c1
						.getColumnIndexOrThrow(TeamContentProvider.STATSTYPE));
				time = c1.getString(c1
						.getColumnIndexOrThrow(TeamContentProvider.STATSTIME));
				period = c1
						.getString(c1
								.getColumnIndexOrThrow(TeamContentProvider.STATSPERIOD));
				subon = c1.getString(c1
						.getColumnIndexOrThrow(TeamContentProvider.STATSSUBON));
				suboff = c1
						.getString(c1
								.getColumnIndexOrThrow(TeamContentProvider.STATSSUBOFF));
				blood = c1.getString(c1
						.getColumnIndexOrThrow(TeamContentProvider.STATSBLOOD));

				if (type != null && type.equals("u")) {
					String temp1 = time, temp2 = period, temp3 = "";
					if (blood.equals("true")) {
						temp3 = " blood sub ";
					} else {
						temp3 = " substitution ";
					}
					if (temp1 == null || temp1.equals("")) {
						line_ = temp3 + teamm + "--> off: " + suboff + "  on: "
								+ subon;
					} else {
						line_ = temp1 + "mins " + temp2 + temp3 + teamm
								+ "--> off: " + suboff + "  on: " + subon;
					}
				} else if (type != null && type.equals("t")) {

					if (time != null && !time.equals("")) {
						line_ = time + "mins " + period + " " + teamm + " "
								+ stats1_ + " " + stats2_ + " " + player_;
					} else {
						line_ = teamm + " " + stats1_ + " " + stats2_ + " "
								+ player_;
					}
				}
				undoList.add(line_);
			} while (c1.moveToNext() && undoList.size() < 4);
		} else {
			tStats.setText("");
		}

		String undo1 = "", undo2 = "", undo3 = "", undo4 = "";
		if (c1.getCount() >= 4) {
			undo1 = undoList.get(0);
			undo2 = undoList.get(1);
			undo3 = undoList.get(2);
			undo4 = undoList.get(3);
			tStats.setText(undo1 + "\n" + undo2 + "\n" + undo3 + "\n" + undo4);
		} else if (c1.getCount() == 3) {
			undo1 = undoList.get(0);
			undo2 = undoList.get(1);
			undo3 = undoList.get(2);
			tStats.setText(undo1 + "\n" + undo2 + "\n" + undo3);
		} else if (c1.getCount() == 2) {
			undo1 = undoList.get(0);
			undo2 = undoList.get(1);
			tStats.setText(undo1 + "\n" + undo2);
		} else if (c1.getCount() == 1) {
			undo1 = undoList.get(0);
			tStats.setText(undo1);
		}
		undoList.clear();
		// call update in review
		c1.close();
		if (updateOthers) {
			updateOtherFragments();
		}
	}

	private void updateOtherFragments() {
		// ((Startup) getActivity()).getFragmentReview().updateListView();
		((Startup) getActivity()).getFragmentReview().updateCardsSubs();
		((Startup) getActivity()).getFragmentTeamOne().updateCards();
		((Startup) getActivity()).getFragmentTeamOne().updateSubsList();
		((Startup) getActivity()).getFragmentTeamTwo().updateCards();
		((Startup) getActivity()).getFragmentTeamTwo().updateSubsList();
	}

	private void getTeam(String teamName) {
		// load panel from database and assign to arraylist
		Uri allTitles = TeamContentProvider.CONTENT_URI;
		String[] projection = { TeamContentProvider.PANELID,
				TeamContentProvider.NAME, TeamContentProvider.POSN };
		CursorLoader cL;
		int posn;

		// reset line up and read from database
		for (int j = 1; j <= 25; j++) {
			teamLineUp[j] = String.valueOf(j);
		}
		String[] args = { teamName };
		cL = new CursorLoader(getActivity(), allTitles, projection, "team=?",
				args, TeamContentProvider.NAME);
		Cursor c1 = cL.loadInBackground();
		playerIDLookUp.clear();
		if (c1.getCount() > 0) {
			c1.moveToFirst();
			do {
				// insert players into positions
				posn = c1.getInt(c1
						.getColumnIndexOrThrow(TeamContentProvider.POSN));
				if (posn > 0) {
					teamLineUp[posn] = c1.getString(c1
							.getColumnIndexOrThrow(TeamContentProvider.NAME));
				}

			} while (c1.moveToNext());
		}
		c1.close();
	}

	// **********************************************************************//
	/*-------------------DIALOG FOR STATS INPUT*---------------------------*///
	// handles button clicks for shot / free / puckout ///
	// **********************************************************************//

	OnClickListener statsClickListener = new OnClickListener() {

		@Override
		public void onClick(View w) {
			int tempButton = ((Button) w).getId();
			switch (tempButton) {
			case R.id.buttonShotHome:
				getTeam(tOurTeam.getText().toString());
				teamNameInput = tOurTeam.getText().toString();
				break;
			case R.id.buttonShotOpp:
				getTeam(tOppTeam.getText().toString());
				teamNameInput = tOppTeam.getText().toString();
				break;
			}
			Intent input = new Intent(getActivity(), InputActivity.class);
			input.putExtra("teamLineUpHome", teamLineUp);
			input.putExtra("teamOriginal", teamNameInput);
			startActivityForResult(input, 9);
		}
	};

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 9) {
			// A contact was picked. Here we will just display it
			// to the user.
			if (data != null && !data.getBooleanExtra("backPressed", false)) {
				stats1 = data.getStringExtra("stats1");
				stats2 = data.getStringExtra("stats2");
				player = data.getStringExtra("player");
				stats1 = (stats1 == null) ? "" : stats1;
				stats2 = (stats2 == null) ? "" : stats2;
				player = (player == null) ? "" : player;
				if (!(stats1.equals("") && stats2.equals("") && player
						.equals(""))&&teamNameInput!=null) {
					updateStatsDatabase(teamNameInput, stats1, stats2, player,
							1, 0);
				}
			}
		}
	}

	// *******************************************************************//
	// *******************************************************************//
	// *******************************************************************//
	// *******************************************************************//
	// *******************************************************************//

	// ///////////////////SCORE CLICK LISTENER//////////////////////////////////
	OnClickListener scoreAddClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {

			// for each case update score display in this fragment
			// and update score display in REVIEW fragment
			switch (v.getId()) {
			case R.id.home_goals:
				homeGoals++;
				bHomeGoals.setText(String.valueOf(homeGoals));
				((Startup) getActivity()).getFragmentReview().settHomeGoals(
						homeGoals);
				// if first score change + on buttons to 0
				if (bHomePoints.getText().equals("+"))
					bHomePoints.setText("0");
				break;
			case R.id.home_points:
				homePoints++;
				bHomePoints.setText(String.valueOf(homePoints));
				((Startup) getActivity()).getFragmentReview().settHomePoints(
						homePoints);
				// if first score change + on buttons to 0
				if (bHomeGoals.getText().equals("+"))
					bHomeGoals.setText("0");
				break;
			case R.id.opp_goals:
				oppGoals++;
				bOppGoals.setText(String.valueOf(oppGoals));
				((Startup) getActivity()).getFragmentReview().settOppGoals(
						oppGoals);
				// if first score change + on buttons to 0
				if (bOppPoints.getText().equals("+"))
					bOppPoints.setText("0");
				break;
			case R.id.opp_points:
				oppPoints++;
				bOppPoints.setText(String.valueOf(oppPoints));
				((Startup) getActivity()).getFragmentReview().settOppPoints(
						oppPoints);
				// if first score change + on buttons to 0
				if (bOppGoals.getText().equals("+"))
					bOppGoals.setText("0");
				break;
			case R.id.dec_home_goals:
				if (homeGoals > 0) {
					homeGoals--;
					bHomeGoals.setText(String.valueOf(homeGoals));
					((Startup) getActivity()).getFragmentReview()
							.settHomeGoals(homeGoals);
					break;
				} else
					return;
			case R.id.dec_home_points:
				if (homePoints > 0) {
					homePoints--;
					bHomePoints.setText(String.valueOf(homePoints));
					((Startup) getActivity()).getFragmentReview()
							.settHomePoints(homePoints);
					break;
				} else
					return;
			case R.id.dec_opp_goals:
				if (oppGoals > 0) {
					oppGoals--;
					bOppGoals.setText(String.valueOf(oppGoals));
					((Startup) getActivity()).getFragmentReview().settOppGoals(
							oppGoals);
					break;
				} else
					return;
			case R.id.dec_opp_points:
				if (oppPoints > 0) {
					oppPoints--;
					bOppPoints.setText(String.valueOf(oppPoints));
					((Startup) getActivity()).getFragmentReview()
							.settOppPoints(oppPoints);
					break;
				} else
					return;
			}
			// update totals values and text
			setTotals();
		}
	};

	OnClickListener periodClickListener = new OnClickListener() {
		@Override
		public void onClick(View w) {
			Button b = (Button) w;
			// read list of allowable times from array in assets and put in
			// adapter to display in alertdialog for selection
			minsList = getResources().getStringArray(R.array.period);
			ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(
					getActivity(), R.layout.single_row_layout, minsList);
			new AlertDialog.Builder(getActivity())
					.setTitle("set time period")
					.setAdapter(adapter1,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// convert string input to integer
									periodInt = which;
									bPeriod.setText(getResources()
											.getStringArray(R.array.periodShort)[which]);
									periodStr = minsList[which];
									dialog.dismiss();
								}
							}).create().show();
		}
	};

	OnClickListener nullPeriodClickListener = new OnClickListener() {
		@Override
		public void onClick(View w) {
			Toast.makeText(getActivity(), "stop timer first",
					Toast.LENGTH_SHORT).show();
		}
	};

	// method to calculate total score from goals and points
	// and update if home team is ahead or behind or if game is a draw
	private void setTotals() {
		int homeTotal = (homeGoals * 3) + homePoints;
		tHomeTotal.setText("(" + String.valueOf(homeTotal) + ")");
		int oppTotal = (oppGoals * 3) + oppPoints;
		tOppTotal.setText("(" + String.valueOf(oppTotal) + ")");

		if (homeTotal > oppTotal) {
			tUpDownDrawText.setText("up by: ");
			tHomeDifference.setText("(" + String.valueOf(homeTotal - oppTotal)
					+ ")");
		} else if (homeTotal < oppTotal) {
			tUpDownDrawText.setText("down by: ");
			tHomeDifference.setText("(" + String.valueOf(-homeTotal + oppTotal)
					+ ")");
		} else {
			tUpDownDrawText.setText("drawn game. ");
			tHomeDifference.setText(" ");
		}
	}

	OnClickListener pauseOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			pause = pause ? false : true;
			if (pause) {
				pauseOn();
			} else {
				pauseOff();
			}
		}
	};

	private void pauseOn() {

		bPause.setBackground(getResources().getDrawable(R.drawable.btn_red));
		bStartStop
				.setBackground(getResources().getDrawable(R.drawable.btn_red));
		bPause.setText("resume");
	}

	private void pauseOff() {
		pause = false;
		bPause.setBackground(getResources().getDrawable(R.drawable.btn_blue));
		bStartStop.setBackground(getResources()
				.getDrawable(R.drawable.btn_blue));
		bPause.setText("pause");
	}

	// //////////////////////////TIMER///////////////////////////////
	// set up thread to run match timer
	private Runnable run = new Runnable() {
		@Override
		public void run() {
			if (pause) {
				starttime = starttime + 1000;

			} else {
				long millis = System.currentTimeMillis() - starttime;
				int seconds = (int) (millis / 1000);
				int minutes = seconds / 60;
				seconds = seconds % 60;
				tTimeGone.setText(String.format("%02d:%02d", minutes, seconds));
				if (minsPerHalf - minutes > 0) {
					tTimeToGo.setText(String.format("%02d:%02d", minsPerHalf
							- 1 - minutes, 60 - seconds));
				} else {
					tTimeToGo.setText(String.format("%02d:%02d", minutes
							- minsPerHalf, seconds));
					tTimeLeft.setText("extra time");

				}
			}
			h.postDelayed(this, 1000);
		}
	};

	public String getTime() {
		if (starttime > 0) {
			long millis = System.currentTimeMillis() - starttime;
			int seconds = (int) (millis / 1000);
			int minutes = seconds / 60;
			return String.format("%02d", minutes);
		} else {
			return "";
		}
	}

	// this method is called from the SETUP fragment to update the names of the
	// home and away teams and to receive team line and teams from setup screen
	public void setTeamLineUp(String homeTeam, String oppTeam) {
		if (!homeTeam.equals(""))
			tOurTeam.setText(homeTeam);
		if (!oppTeam.equals(""))
			tOppTeam.setText(oppTeam);
	}

	// Undo stats entries
	OnClickListener undoOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Uri allTitles = TeamContentProvider.CONTENT_URI_2;
			String teamTemp = "", stats1Temp = "", stats2Temp = "";
			String playerTemp = "", typeTemp;
			long rowIdTemp = 0;
			String[] projection = { TeamContentProvider.STATSID,
					TeamContentProvider.STATS1, TeamContentProvider.STATS2,
					TeamContentProvider.STATSPLAYER,
					TeamContentProvider.STATSTYPE,
					TeamContentProvider.STATSTEAM };
			CursorLoader cL;
			cL = new CursorLoader(getActivity(), allTitles, projection, null,
					null, TeamContentProvider.STATSID);
			Cursor c1 = cL.loadInBackground();
			if (c1.getCount() > 0) {
				c1.moveToLast();
				rowIdTemp = (c1.getLong(c1
						.getColumnIndexOrThrow(TeamContentProvider.STATSID)));
				teamTemp = (c1.getString(c1
						.getColumnIndexOrThrow(TeamContentProvider.STATSTEAM)));
				typeTemp = (c1.getString(c1
						.getColumnIndexOrThrow(TeamContentProvider.STATSTYPE)));
				stats1Temp = (c1.getString(c1
						.getColumnIndexOrThrow(TeamContentProvider.STATS1)));
				stats2Temp = (c1.getString(c1
						.getColumnIndexOrThrow(TeamContentProvider.STATS2)));
				playerTemp = (c1
						.getString(c1
								.getColumnIndexOrThrow(TeamContentProvider.STATSPLAYER)));

				c1.close();
				getActivity().getContentResolver().delete(
						Uri.parse(TeamContentProvider.CONTENT_URI_2 + "/"
								+ rowIdTemp), null, null);

				((Startup) getActivity()).getFragmentReview().fillData();
				((Startup) getActivity()).getFragmentEvent().fillData();
				undo(teamTemp, stats1Temp, stats2Temp, playerTemp, typeTemp);
			}
		}
	};

	public void undo(String teamTemp, String stats1Temp, String stats2Temp,
			String playerTemp, String typeTemp) {
		// undo scores on this page first

		if (typeTemp.equals("t")) {
			// check for goal
			if (stats1Temp.equals("goal")) {
				// check which team
				if (teamTemp.equals(tOurTeam.getText().toString())) {
					if (homeGoals - 1 >= 0) {
						homeGoals = homeGoals - 1;
						bHomeGoals.setText(String.valueOf(homeGoals));
						// update totals
						setTotals();
						// increment score in REVIEW fragment
						((Startup) getActivity()).getFragmentReview()
								.settHomeGoals(homeGoals);
						// remind user score is updated in case they try and
						// do it manually
						Toast.makeText(getActivity(), "Score Updated",
								Toast.LENGTH_SHORT).show();
					}
				} else if (teamTemp.equals(tOppTeam.getText().toString())) {
					if (oppGoals - 1 >= 0) {
						oppGoals = oppGoals - 1;
						bOppGoals.setText(String.valueOf(oppGoals));
						// update totals
						setTotals();
						// increment score in REVIEW fragment
						((Startup) getActivity()).getFragmentReview()
								.settOppGoals(oppGoals);
						// remind user score is updated in case they try and
						// do it manually
						Toast.makeText(getActivity(), "Score Updated",
								Toast.LENGTH_SHORT).show();
					}
				}
			}
			// check for point
			else if (stats1Temp.equals("point")) {
				// check which team
				if (teamTemp.equals(tOurTeam.getText().toString())) {
					// decrement puckout total
					if (homePoints - 1 >= 0) {
						homePoints = homePoints - 1;
						bHomePoints.setText(String.valueOf(homePoints));
						// update totals
						setTotals();
						// increment score in REVIEW fragment
						((Startup) getActivity()).getFragmentReview()
								.settHomePoints(homePoints);
						// remind user score is updated in case they try and
						// do it manually
						Toast.makeText(getActivity(), "Score Updated",
								Toast.LENGTH_SHORT).show();
					}
				} else if (teamTemp.equals(tOppTeam.getText().toString())) {
					if (oppPoints - 1 >= 0) {
						oppPoints = oppPoints - 1;
						bOppPoints.setText(String.valueOf(oppPoints));
						// update totals
						setTotals();
						// increment score in REVIEW fragment
						((Startup) getActivity()).getFragmentReview()
								.settOppPoints(oppPoints);
						// remind user score is updated in case they try and
						// do it manually
						Toast.makeText(getActivity(), "Score Updated",
								Toast.LENGTH_SHORT).show();
					}
				}
			}
			updateScorers(stats1Temp, stats2Temp, playerTemp, teamTemp, MINUS);
		}
		updateStatsList(true);
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		inflater.inflate(R.menu.scores_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	// set up help menu in action bar
	// @Override

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent ihelp;

		switch (item.getItemId()) {
		case 0:
			// menu pointer do nothing
		case R.id.helpTeam:
			ihelp = new Intent(getActivity(), HelpActivity.class);
			ihelp.putExtra("HELP_ID", R.string.scoresHelp);
			startActivity(ihelp);
			return true;
		case R.id.resetTimer:
			resetTime();
			return true;
		case R.id.resetScore:
			resetScore();
			return true;
		case R.id.resetStats:
			resetStats();
			return true;
		case R.id.resetAll:
			tLoc.setText("");
			resetTime();
			resetScore();
			resetStats();
			return true;
		case R.id.phone:
			AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
			input = new EditText(getActivity());
			input.setId(991);
			phone = phone.replace(",", "-");
			input.setText(phone);
			input.setSingleLine(false);
			input.setInputType(InputType.TYPE_CLASS_PHONE);
			alert.setTitle("Enter Phone Number(s) for Texts");
			alert.setMessage("Format 0001234567 (no spaces)\n"
					+ "Use - (dash) or , (comma) to separate numbers if there's more than one. For example:\n"
					+ "0871234567-0861234567,03538512345678\n");
			alert.setView(input);
			alert.setNegativeButton("Reset",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface indialog, int which) {
							phone = "";

							// update title and panelname
							SharedPreferences sharedPref = getActivity()
									.getSharedPreferences(
											"team_stats_record_data",
											Context.MODE_PRIVATE);
							SharedPreferences.Editor editor = sharedPref.edit();
							editor.putString("PHONE", phone);
							editor.commit();
						}
					});
			alert.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface indialog, int which) {
							Pattern p = Pattern.compile("^[0-9]");
							Matcher m;
							int check = 0;
							String inName = input.getText().toString()
									.replace(" ", "");
							inName.replace(",", "-");
							String[] separated = inName.split("-");
							for (int i = 0; i < separated.length; i++) {
								m = p.matcher(separated[i].replace(" ", ""));
								if (m.find()) {
									check = check + 1;
								}
							}
							if (check == separated.length) {
								phone = input.getText().toString()
										.replace(" ", "");
								phone = phone.replace("-", ",");

								// update title and panelname
								SharedPreferences sharedPref = getActivity()
										.getSharedPreferences(
												"team_stats_record_data",
												Context.MODE_PRIVATE);
								SharedPreferences.Editor editor = sharedPref
										.edit();
								editor.putString("PHONE", phone);
								editor.commit();
							} else {
								Toast.makeText(getActivity(),
										"Invalid Number(s), Try Again",
										Toast.LENGTH_SHORT).show();
							}
						}
					});
			alert.create();
			alert.show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public String getLocText() {
		return tLoc.getText().toString();
	}

	public String getPhone() {
		return phone;
	}

}
