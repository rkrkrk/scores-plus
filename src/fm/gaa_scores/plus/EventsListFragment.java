/*
 *  HelpActivity.java
 *
 *  Written by: Fintan Mahon 12101524
 *  Description: Displays relevant help screen
 *  
 *  Written on: Jan 2013
 *  
 * 
 *  
 */
package fm.gaa_scores.plus;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class EventsListFragment extends ListFragment {
	private ArrayList<String> panelList = new ArrayList<String>();

	private String[] teamLineUpHome = new String[26], panelCurrent;
	private String[] teamLineUpOpp = new String[26];
	private String teamBefore, typeTemp, timeTemp, homeTeam, oppTeam;
	private String periodTemp, playerOn, playerOff, filter;
	private String stats1Before, stats2Before, teamBack;
	private String playerBefore, subonTemp, suboffTemp, bloodTemp;
	private String[] inSetup;
	private Button bHome, bAll, bOpp;
	private long ID;
	private long sortTemp = 0;
	private Intent input;
	private boolean bloodSub, subMade = false;
	private boolean[] checked = { false, false, false };
	private long[] idArray;
	private String[] lineArray;
	private ListView listView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.event_list_layout, container, false);

		String myTag = getTag();
		((Startup) getActivity()).setTagFragmentEvents(myTag);
		this.setHasOptionsMenu(true);
		SharedPreferences sharedPref = getActivity().getSharedPreferences(
				"team_stats_review_data", Context.MODE_PRIVATE);
		homeTeam = sharedPref.getString("OWNTEAM", "OWN TEAM");
		oppTeam = sharedPref.getString("OPPTEAM", "OPPOSITION");
		teamLineUpHome = getTeam(homeTeam);
		teamLineUpOpp = getTeam(oppTeam);
		filter = "all";

		bHome = (Button) v.findViewById(R.id.bHome);
		bAll = (Button) v.findViewById(R.id.bAll);
		bOpp = (Button) v.findViewById(R.id.bOpp);
		bHome.setText(homeTeam + " only");
		bOpp.setText(oppTeam + " only");
		bHome.setOnClickListener(getFilter);
		bAll.setOnClickListener(getFilter);
		bOpp.setOnClickListener(getFilter);

		return v;
	}

	OnClickListener getFilter = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (((Button) v).getId()) {
			case R.id.bHome:
				filter = homeTeam;
				break;
			case R.id.bAll:
				filter = "all";
				break;
			case R.id.bOpp:
				filter = oppTeam;
				break;
			}
			fillData();
		}
	};

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		registerForContextMenu(getListView());
		listView = getListView();
		fillData();
	}

	// method to read in panel list from database and list contents on screen
	public void fillData() {
		// // retreive shots info from database

		String line = "";
		String stats1 = "";
		String stats2 = "";
		String player = "";
		String type = "";
		long sort = 0, id = 0;
		String time = "";
		String period = "";
		String teamm = "";
		String subon = "";
		String suboff = "";
		String blood = "";
		Uri allTitles = TeamContentProvider.CONTENT_URI_2;
		Cursor c1;
		String[] projection = { TeamContentProvider.STATS1,
				TeamContentProvider.STATSID, TeamContentProvider.STATS2,
				TeamContentProvider.STATSPLAYER, TeamContentProvider.STATSTYPE,
				TeamContentProvider.STATSTEAM, TeamContentProvider.STATSSORT,
				TeamContentProvider.STATSTIME, TeamContentProvider.STATSPERIOD,
				TeamContentProvider.STATSSUBON, TeamContentProvider.STATSLINE,
				TeamContentProvider.STATSSUBOFF, TeamContentProvider.STATSBLOOD };
		String[] args = { filter };
		if (filter.equals("all")) {
			c1 = getActivity().getContentResolver().query(allTitles,
					projection, null, null,
					TeamContentProvider.STATSSORT + " DESC");
		} else {
			c1 = getActivity().getContentResolver().query(allTitles,
					projection, "team=?", args,
					TeamContentProvider.STATSSORT + " DESC");
		}
		if (c1.getCount() > 0) {
			c1.moveToFirst();
			idArray = new long[c1.getCount()];
			lineArray = new String[c1.getCount()];
			int i = 0;
			do {
				id = c1.getLong(c1
						.getColumnIndexOrThrow(TeamContentProvider.STATSID));
				teamm = c1.getString(c1
						.getColumnIndexOrThrow(TeamContentProvider.STATSTEAM));
				line = c1.getString(c1
						.getColumnIndexOrThrow(TeamContentProvider.STATSLINE));
				stats1 = c1.getString(c1
						.getColumnIndexOrThrow(TeamContentProvider.STATS1));
				stats2 = c1.getString(c1
						.getColumnIndexOrThrow(TeamContentProvider.STATS2));
				player = c1
						.getString(c1
								.getColumnIndexOrThrow(TeamContentProvider.STATSPLAYER));
				type = c1.getString(c1
						.getColumnIndexOrThrow(TeamContentProvider.STATSTYPE));
				sort = c1.getLong(c1
						.getColumnIndexOrThrow(TeamContentProvider.STATSSORT));
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

				idArray[i] = id;
				if (type != null && type.equals("s")) {
					lineArray[i] = line;
				} else if (type != null && type.equals("u")) {
					String temp1 = time, temp2 = period, temp3 = "";
					if (blood.equals("true")) {
						temp3 = " blood sub ";
					} else {
						temp3 = " substitution ";
					}
					if (temp1==null || temp1.equals("")) {
						lineArray[i] = temp3 + teamm + "--> off: " + suboff
								+ "  on: " + subon;
					} else {
						lineArray[i] = temp1 + "mins " + temp2 + temp3 + teamm
								+ "--> off: " + suboff + "  on: " + subon;
					}
				} else if (type != null && type.equals("t")) {

					if (time != null && !time.equals("")) {
						lineArray[i] = time + "mins " + period + " " + teamm
								+ " " + stats1 + " " + stats2 + " " + player;
					} else {
						lineArray[i] = teamm + " " + stats1 + " " + stats2
								+ " " + player;
					}
				} else {
					lineArray[i] = "error old event data incompatible with this new version,"
							+ " reset stats and start new match and you should be good "
							+ "to go. If you still get this error uninstall the app "
							+ "and reinstall from Play Store. Note that any teams in"
							+ " the App will be lost when you do this so export them to"
							+ " text file first "
							+ "so you can reload them in the new app  ";
				}
				i++;
			} while (c1.moveToNext());
		} else {
			lineArray = new String[] { "No events recorded yet" };
		}
		c1.close();
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
				getActivity(), R.layout.event_row_layout, lineArray);
		listView.setAdapter(arrayAdapter);
	}

	@Override
	// method to deal with user touching a row/player on the list
	// launch PanelEditActivity with an intent and passing in the the row/player
	// id do that the player details can be edited
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		l.showContextMenuForChild(v);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater mi = getActivity().getMenuInflater();
		mi.inflate(R.menu.list_menu_shortpress, menu);
	}

	@Override
	// deal with selection from long press menu
	public boolean onContextItemSelected(MenuItem item) {
		stats1Before = "";
		stats2Before = "";
		playerBefore = "";
		typeTemp = "";
		sortTemp = 0;
		timeTemp = "";
		periodTemp = "";
		teamBack = "";
		Uri uri = TeamContentProvider.CONTENT_URI_2;
		String[] projection = { TeamContentProvider.STATS1,
				TeamContentProvider.STATS2, TeamContentProvider.STATSPLAYER,
				TeamContentProvider.STATSTYPE, TeamContentProvider.STATSTEAM,
				TeamContentProvider.STATSSORT, TeamContentProvider.STATSTIME,
				TeamContentProvider.STATSPERIOD,
				TeamContentProvider.STATSSUBON,
				TeamContentProvider.STATSSUBOFF, TeamContentProvider.STATSBLOOD };
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		ID = idArray[info.position];
		String[] args = { Long.toString(ID) };
		Cursor c1 = getActivity().getContentResolver().query(uri, projection,
				"_id=?", args, TeamContentProvider.STATSSORT);
		if (c1.getCount() > 0) {
			c1.moveToFirst();
			teamBefore = c1.getString(c1
					.getColumnIndexOrThrow(TeamContentProvider.STATSTEAM));
			stats1Before = c1.getString(c1
					.getColumnIndexOrThrow(TeamContentProvider.STATS1));
			stats2Before = c1.getString(c1
					.getColumnIndexOrThrow(TeamContentProvider.STATS2));
			playerBefore = c1.getString(c1
					.getColumnIndexOrThrow(TeamContentProvider.STATSPLAYER));
			typeTemp = c1.getString(c1
					.getColumnIndexOrThrow(TeamContentProvider.STATSTYPE));
			sortTemp = c1.getLong(c1
					.getColumnIndexOrThrow(TeamContentProvider.STATSSORT));
			timeTemp = c1.getString(c1
					.getColumnIndexOrThrow(TeamContentProvider.STATSTIME));
			periodTemp = c1.getString(c1
					.getColumnIndexOrThrow(TeamContentProvider.STATSPERIOD));
			subonTemp = c1.getString(c1
					.getColumnIndexOrThrow(TeamContentProvider.STATSSUBON));
			suboffTemp = c1.getString(c1
					.getColumnIndexOrThrow(TeamContentProvider.STATSSUBOFF));
			bloodTemp = c1.getString(c1
					.getColumnIndexOrThrow(TeamContentProvider.STATSBLOOD));
		}
		c1.close();

		switch (item.getItemId()) {
		case R.id.menu_delete:
			if (typeTemp.equals("s")) {
				Toast.makeText(getActivity(),
						"Start / End times cannot be changed",
						Toast.LENGTH_LONG).show();
			} else {
				delete(stats1Before, stats2Before, playerBefore);
			}
			return true;

		case R.id.menu_edit:
			if (typeTemp.equals("s")) {
				Toast.makeText(getActivity(),
						"Start / End times cannot be changed",
						Toast.LENGTH_LONG).show();
				return true;
			} else if (typeTemp.equals("t")) {
				input = new Intent(getActivity(), InputActivity.class);
				input.putExtra("teamLineUpHome", teamLineUpHome);
				input.putExtra("teamLineUpOpp", teamLineUpOpp);
				input.putExtra("homeTeam", homeTeam);
				input.putExtra("oppTeam", oppTeam);
				input.putExtra("call", 1);

				input.putExtra("teamOriginal", teamBefore);
				input.putExtra("stats1", stats1Before);
				input.putExtra("stats2", stats2Before);
				input.putExtra("player", playerBefore);
				// 9 is for edit
				startActivityForResult(input, 9);
			} else if (typeTemp.equals("u")) {
				recordSub(true);
			}
			return true;

		case R.id.menu_insert_event:

			input = new Intent(getActivity(), InputActivity.class);
			input.putExtra("teamLineUpHome", teamLineUpHome);
			input.putExtra("teamLineUpOpp", teamLineUpOpp);
			input.putExtra("homeTeam", homeTeam);
			input.putExtra("oppTeam", oppTeam);
			input.putExtra("call", 1);

			// 10 is for insert
			startActivityForResult(input, 10);

			return true;

		case R.id.menu_insert_sub:
			recordSub(false);
			return true;

		case R.id.menu_cancel:
			return true;
		}

		return super.onContextItemSelected(item);
	}

	private void delete(String stats1Temp, String stats2Temp, String playerTemp) {
		// // Delete a row / player
		Uri uri = Uri.parse(TeamContentProvider.CONTENT_URI_2 + "/" + ID);
		getActivity().getContentResolver().delete(uri, null, null);
		Toast.makeText(getActivity(), "stats entry deleted", Toast.LENGTH_LONG)
				.show();
		fillData();
		((Startup) getActivity()).getFragmentReview().fillData();
		if (typeTemp.equals("t")) {
			((Startup) getActivity()).getFragmentScore().undo(teamBefore,
					stats1Temp, stats2Temp, playerTemp, typeTemp);
		} else {
			((Startup) getActivity()).getFragmentScore().updateStatsList(false);
			;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		// handle edit
		if (data != null) {
			String stats1, stats2, player;
			stats1 = data.getStringExtra("stats1");
			stats2 = data.getStringExtra("stats2");
			player = data.getStringExtra("player");
			teamBack = data.getStringExtra("teamBack");
			stats1 = (stats1 == null) ? "" : stats1;
			stats2 = (stats2 == null) ? "" : stats2;
			player = (player == null) ? "" : player;
			teamBack = (teamBack == null) ? teamBefore : teamBack;

			ContentValues values = new ContentValues();
			if (timeTemp != null && !timeTemp.isEmpty()) {
				values.put("line", timeTemp + "mins " + periodTemp + " "
						+ teamBack + " " + stats1 + " " + stats2 + " " + player);
			} else {
				values.put("line", teamBack + " " + stats1 + " " + stats2 + " "
						+ player);
			}
			values.put("team", teamBack);
			values.put("player", player);
			values.put("stats1", stats1);
			values.put("stats2", stats2);
			if (requestCode == 9) {
				// edit if there's a change
				if (!stats1.equals(stats1Before)
						|| !stats2.equals(stats2Before)
						|| !player.equals(playerBefore)
						|| !teamBack.equals(teamBefore)) {
					// undo first then add
					((Startup) getActivity()).getFragmentScore().undo(
							teamBefore, stats1Before, stats2Before,
							playerBefore, typeTemp);
					Uri uri = Uri.parse(TeamContentProvider.CONTENT_URI_2 + "/"
							+ ID);
					getActivity().getContentResolver().update(uri, values,
							null, null);
					((Startup) getActivity()).getFragmentScore()
							.updateStatsDatabase(teamBack, stats1, stats2,
									player, 1, 1);
					fillData();
				}
			} else if (requestCode == 10) {
				// insert
				if (!(stats1.equals("") && stats2.equals("") && player
						.equals(""))) {
					sortTemp = sortTemp + 10;
					values.put("type", "t");
					values.put("sort", sortTemp);
					values.put("time", timeTemp);
					values.put("period", periodTemp);
					getActivity().getContentResolver().insert(
							TeamContentProvider.CONTENT_URI_2, values);
					((Startup) getActivity()).getFragmentScore()
							.updateStatsDatabase(teamBack, stats1, stats2,
									player, 1, 1);
					fillData();
				}
			}
		}
	}

	private String[] getTeam(String teamName) {
		String[] teamLineUp = new String[26];

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
		cL = new CursorLoader(getActivity(), allTitles, projection,
				TeamContentProvider.TEAM + " = '" + teamName + "'", null,
				TeamContentProvider.NAME);
		Cursor c1 = cL.loadInBackground();
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
		return teamLineUp;
	}

	private void recordSub(final boolean edit) {
		// take out team in other page
		inSetup = new String[3];
		inSetup[0] = homeTeam;
		inSetup[1] = oppTeam;
		inSetup[2] = "Blood Sub?";
		bloodSub = false;
		teamBack = "";
		subMade = false;
		checked[0] = false;
		checked[1] = false;
		checked[2] = false;
		if (edit) {
			if (teamBefore.equals(homeTeam)) {
				checked[0] = true;
				teamBack = homeTeam;
			} else if (teamBefore.equals(oppTeam)) {
				checked[1] = true;
				teamBack = oppTeam;
			}
			if (bloodTemp.equals("true")) {
				checked[2] = true;
				bloodSub = true;
			}
		}

		final Builder builder = new Builder(getActivity());
		builder.setTitle("SUBSTITUTION - select team\n(read ? help file for limitations)");
		// builder.setCancelable(true);
		builder.setMultiChoiceItems(inSetup, checked,
				new DialogInterface.OnMultiChoiceClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which,
							boolean isChecked) {
						// if (isChecked) {
						// If the user checked the item, add it
						// to the selected items
						if (which == 0) {
							final AlertDialog alert = (AlertDialog) dialog;
							ListView list = alert.getListView();
							list.setItemChecked(1, false);
							// checked[0] = !checked[0];
							if (checked[1]) {
								checked[1] = false;
							}
							if (checked[0]) {
								teamBack = inSetup[0];
							} else {
								teamBack = "";
							}
						} else if (which == 1) {
							final AlertDialog alert = (AlertDialog) dialog;
							ListView list = alert.getListView();
							list.setItemChecked(0, false);
							// checked[1] = !checked[1];
							if (checked[0]) {
								checked[0] = false;
							}
							if (checked[1]) {
								teamBack = inSetup[1];
							} else {
								teamBack = "";
							}
						} else if (which == 2) {
							bloodSub = !bloodSub;
						}
						// }
					}
				});
		builder.setNegativeButton("CANCEL", null);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int id) {
				// call method to display list of players
				int default1 = 0, default2 = 0;
				if (!teamBack.equals("")) {
					if (!teamBack.equals("")) {
						getPanel(teamBack);
						// set default
						if (edit) {
							for (int i = 0; i < panelCurrent.length; i++) {
								if (panelCurrent[i].equals(subonTemp)) {
									default1 = i;
									break;
								}
							}
							for (int i = 0; i < panelCurrent.length; i++) {
								if (panelCurrent[i].equals(suboffTemp)) {
									default2 = i;
									break;
								}
							}

						}
					}
					final int def1 = default1;
					final int def2 = default2;
					subMade = true;
					AlertDialog.Builder builder1 = new AlertDialog.Builder(
							getActivity());
					builder1.setTitle("select who is going on");
					// builder.setCancelable(true);
					builder1.setSingleChoiceItems(panelCurrent, def1,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									playerOn = panelCurrent[which];
									// dialog to see who is goping on

									// Get whois coming off swap with going on
									// and write
									// change to databse
									AlertDialog.Builder builder2 = new AlertDialog.Builder(
											getActivity());
									// builder1.setCancelable(true);
									builder2.setTitle("select who is coming off");
									builder2.setSingleChoiceItems(
											panelCurrent,
											def2,
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {
													playerOff = panelCurrent[which];
													dialog.dismiss();
													panelList.clear();
													handleSubInfo(edit);
												}
											});
									AlertDialog alert2 = builder2.create();
									alert2.show();
									dialog.dismiss();
								}
							});
					AlertDialog alert1 = builder1.create();
					alert1.show();
				}
			}
		});
		// alert = builder.create();
		builder.show();
	}

	public void handleSubInfo(boolean edit) {
		if (subMade && !edit) {
			// new entry
			String temp = (bloodSub) ? " blood sub " : " substitution ";
			String temp1 = (bloodSub) ? "true" : "false";

			ContentValues values = new ContentValues();
			if (timeTemp == null || timeTemp.isEmpty()) {
				values.put("line", temp + teamBack + "--> off: " + playerOff
						+ "  on: " + playerOn);
			} else {
				values.put("line", timeTemp + "mins " + periodTemp + temp
						+ teamBack + "--> off: " + playerOff + "  on: "
						+ playerOn);
			}
			values.put("sort", sortTemp + 10);
			values.put("type", "u");
			values.put("time", timeTemp);
			values.put("team", teamBack);
			values.put("period", periodTemp);
			values.put("blood", temp1);
			values.put("subon", playerOn);
			values.put("suboff", playerOff);
			getActivity().getContentResolver().insert(
					TeamContentProvider.CONTENT_URI_2, values);

			// increment subsused counter
			if (!bloodSub) {
				((Startup) getActivity()).getFragmentReview().updateCardsSubs();
			}

			fillData();
			((Startup) getActivity()).getFragmentScore().updateStatsList(false);
		} else if (subMade && edit) {
			// new entry
			String temp = (bloodSub) ? " blood sub " : " substitution ";
			String temp1 = (bloodSub) ? "true" : "false";
			ContentValues values = new ContentValues();
			if (timeTemp == null || timeTemp.isEmpty()) {
				values.put("line", temp + teamBack + "--> off: " + playerOff
						+ "  on: " + playerOn);
			} else {
				values.put("line", timeTemp + "mins " + periodTemp + temp
						+ teamBack + "--> off: " + playerOff + "  on: "
						+ playerOn);
			}
			values.put("team", teamBack);
			values.put("blood", temp1);
			values.put("subon", playerOn);
			values.put("suboff", playerOff);
			Uri uri = Uri.parse(TeamContentProvider.CONTENT_URI_2 + "/" + ID);
			getActivity().getContentResolver().update(uri, values, null, null);
			((Startup) getActivity()).getFragmentReview().updateCardsSubs();
			fillData();
			((Startup) getActivity()).getFragmentScore().updateStatsList(false);
		}
	}

	private void getPanel(String panelName) {
		// load panel from database and assign to arraylist
		String[] projection = { TeamContentProvider.NAME };
		CursorLoader cL;
		Uri allTitles = TeamContentProvider.CONTENT_URI;
		// reset line up and read from database

		cL = new CursorLoader(getActivity(), allTitles, projection, "team=?",
				new String[] { panelName }, TeamContentProvider.NAME);
		// cL = new CursorLoader(getActivity(), allTitles, projection,
		// TeamContentProvider.TEAM + " = '" + panelName + "'", null,
		// TeamContentProvider.NAME);
		Cursor c1 = cL.loadInBackground();
		panelList.clear();
		if (c1.getCount() > 0) {
			c1.moveToFirst();
			do {
				// read in player nicknames
				panelList.add(c1.getString(c1
						.getColumnIndexOrThrow(TeamContentProvider.NAME)));

			} while (c1.moveToNext());
			panelCurrent = new String[panelList.size()];
			panelCurrent[0] = "---";
			for (int j = 1; j < panelList.size(); j++) {
				panelCurrent[j] = panelList.get(j);
			}
		} else {
			panelCurrent = new String[1];
			panelCurrent[0] = "---";
		}
		c1.close();
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		inflater.inflate(R.menu.events_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent ihelp;
		switch (item.getItemId()) {
		case 0:
			// menu pointer do nothing
		case R.id.helpTeam:
			ihelp = new Intent(getActivity(), HelpActivity.class);
			ihelp.putExtra("HELP_ID", R.string.eventsListHelp);
			startActivity(ihelp);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
