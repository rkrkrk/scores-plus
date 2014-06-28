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

import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
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
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class EventsListFragment extends ListFragment {
	private String[] teamLineUpHome = new String[26];
	private String[] teamLineUpOpp = new String[26];
	private String teamBefore, typeTemp, timeTemp, homeTeam, oppTeam;
	private String periodTemp;
	private String stats1Before, stats2Before, teamBack;
	private String playerBefore;
	private long ID;
	private long sortTemp = (long) 0;
	private Intent input;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.event_list_layout, container, false);

		String myTag = getTag();
		((Startup) getActivity()).setTagFragmentEvents(myTag);

		SharedPreferences sharedPref = getActivity().getSharedPreferences(
				"team_stats_review_data", Context.MODE_PRIVATE);
		Log.e("getting teams in eventList", "");
		homeTeam = sharedPref.getString("OWNTEAM", "OWN TEAM");
		oppTeam = sharedPref.getString("OPPTEAM", "OPPOSITION");
		teamLineUpHome = getTeam(homeTeam);
		teamLineUpOpp = getTeam(oppTeam);

		return v;
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		registerForContextMenu(getListView());
		fillData();
	}

	// method to read in panel list from database and list contents on screen
	public void fillData() {
		// retreive shots info from database
		Uri allTitles = TeamContentProvider.CONTENT_URI_2;
		String[] from = new String[] { TeamContentProvider.STATSLINE };
		String[] projection = { TeamContentProvider._ID,
				TeamContentProvider.STATSLINE };
		int[] to = new int[] { R.id.listrtxt };
		Cursor c1 = getActivity().getContentResolver()
				.query(allTitles, projection, null, null,
						TeamContentProvider.STATSSORT + " DESC");
		SimpleCursorAdapter reminders = new SimpleCursorAdapter(getActivity(),
				R.layout.event_row_layout, c1, from, to, 0);
		setListAdapter(reminders);
	}

	@Override
	// method to deal with user touching a row/player on the list
	// launch PanelEditActivity with an intent and passing in the the row/player
	// id do that the player details can be edited
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		l.showContextMenuForChild(v);
	}

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
		Uri uri = TeamContentProvider.CONTENT_URI_2;
		String[] projection = { TeamContentProvider.STATS1,
				TeamContentProvider.STATS2, TeamContentProvider.STATSPLAYER,
				TeamContentProvider.STATSTYPE, TeamContentProvider.STATSTEAM,
				TeamContentProvider.STATSSORT, TeamContentProvider.STATSTIME,
				TeamContentProvider.STATSPERIOD };
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		ID = info.id;
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
		}
		c1.close();
		if (typeTemp.equals("t")) {
			switch (item.getItemId()) {
			case R.id.menu_delete:
				delete(null, null, null);
				return true;
			case R.id.menu_edit:
			case R.id.menu_insert:
				// getTeam(teamTemp);
				input = new Intent(getActivity(), InputActivity.class);
				input.putExtra("teamLineUpHome", teamLineUpHome);
				input.putExtra("teamLineUpOpp", teamLineUpOpp);
				input.putExtra("homeTeam", homeTeam);
				input.putExtra("oppTeam", oppTeam);	
				input.putExtra("call", 1);
				if (item.getItemId() == R.id.menu_edit) {
					input.putExtra("teamOriginal", teamBefore);
					input.putExtra("stats1", stats1Before);
					input.putExtra("stats2", stats2Before);
					input.putExtra("player", playerBefore);
					// 9 is for edit
					startActivityForResult(input, 9);
				} else {
					// 10 is for insert
					startActivityForResult(input, 10);
				}
				return true;
			}
		} else if (typeTemp.equals("u")) {
			switch (item.getItemId()) {
			case R.id.menu_delete:
				delete(null, null, null);
				return true;

			case R.id.menu_edit:
				Toast.makeText(getActivity(),
						"Edit option not available for Subs yet.",
						Toast.LENGTH_LONG).show();
				return true;
			case R.id.menu_insert:
				Toast.makeText(getActivity(),
						"Insert option not available for Subs yet.",
						Toast.LENGTH_LONG).show();
				return true;
			}
		} else if (typeTemp.equals("s")) {
			switch (item.getItemId()) {
			case R.id.menu_delete:
				delete(null, null, null);
			case R.id.menu_edit:
			case R.id.menu_insert:
				Toast.makeText(getActivity(),
						"Start / End times cannot be changed",
						Toast.LENGTH_LONG).show();
				return true;
			}
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
		if (typeTemp == "t") {
			((Startup) getActivity()).getFragmentScore().undo(teamBefore,
					stats1Temp, stats2Temp, playerTemp, typeTemp);
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
			Log.e("back from input", stats1 + " - " + stats2 + " - " + player + " - "+teamBack);
			stats1 = (stats1 == null) ? "" : stats1;
			stats2 = (stats2 == null) ? "" : stats2;
			player = (player == null) ? "" : player;
			teamBack = (player == null) ? teamBefore : teamBack;
			Log.e("eventback", stats1 + " - " + stats2 + " - " + player + " - "
					+ teamBack + " - " + ID);

			ContentValues values = new ContentValues();
			if (!timeTemp.equals("")) {
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
				if (!stats1.equals(stats1Before)
						|| !stats2.equals(stats2Before)
						|| !player.equals(playerBefore)) {
					// undo first then add
					((Startup) getActivity()).getFragmentScore().undo(teamBefore,
							stats1Before, stats2Before, playerBefore, typeTemp);
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
				if (!(stats1.equals("") && stats2.equals("") && player
						.equals(""))) {
					sortTemp = sortTemp + 10;
					values.put("type", typeTemp);
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

}
