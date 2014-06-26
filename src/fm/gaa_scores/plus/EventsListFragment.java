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
import android.content.CursorLoader;
import android.content.Intent;
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
	private String[] teamLineUp = new String[26];
	private String teamTemp, typeTemp, timeTemp;
	private String periodTemp;
	private String stats1Before, stats2Before;
	private String playerBefore;
	private long ID;
	private long sortTemp = (long) 0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.event_list_layout, container, false);

		String myTag = getTag();
		((Startup) getActivity()).setTagFragmentEvents(myTag);

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
		Cursor c1 = getActivity().getContentResolver().query(allTitles,
				projection, null, null, TeamContentProvider.STATSID + " DESC");
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
			teamTemp = c1.getString(c1
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
		switch (item.getItemId()) {
		case R.id.menu_delete:
			// // Delete a row / player
			uri = Uri.parse(TeamContentProvider.CONTENT_URI_2 + "/" + ID);
			getActivity().getContentResolver().delete(uri, null, null);
			Toast.makeText(getActivity(), "stats entry deleted",
					Toast.LENGTH_LONG).show();
			fillData();
			((Startup) getActivity()).getFragmentReview().fillData();
			((Startup) getActivity()).getFragmentScore().undo(teamTemp,
					stats1Before, stats2Before, playerBefore, typeTemp);
			return true;

		case R.id.menu_edit:
			getTeam(teamTemp);
			Intent input = new Intent(getActivity(), InputActivity.class);
			input.putExtra("teamLineup", teamLineUp);
			input.putExtra("stats1", stats1Before);
			input.putExtra("stats2", stats2Before);
			input.putExtra("player", playerBefore);
			startActivityForResult(input, 9);
			return true;
		case R.id.menu_insert:
			Log.e("inserty", " ");
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		// handle edit
		if (requestCode == 9) {
			// A contact was picked. Here we will just display it
			// to the user.
			if (data != null) {
				String stats1, stats2, player;
				stats1 = data.getStringExtra("stats1");
				stats2 = data.getStringExtra("stats2");
				player = data.getStringExtra("player");
				Log.e("back", stats1 + " - " + stats2 + " - " + player + " - ");
				stats1 = (stats1 == null) ? "" : stats1;
				stats2 = (stats2 == null) ? "" : stats2;
				player = (player == null) ? "" : player;
				if (!stats1.equals(stats1Before)
						|| !stats2.equals(stats2Before) || !player
							.equals(playerBefore)) {
					//undo first then add
					Log.e("eventback", stats1 + " - " + stats2 + " - " + player
							+ " - " + teamTemp + " - " + ID);
					((Startup) getActivity()).getFragmentScore().undo(teamTemp,
							stats1Before, stats2Before, playerBefore, typeTemp);
					ContentValues values = new ContentValues();
					if (!timeTemp.equals("")) {
						values.put("line", timeTemp + "mins " + periodTemp
								+ " " + teamTemp + " " + stats1 + " " + stats2
								+ " " + player);
					} else {
						values.put("line", teamTemp + " " + stats1 + " "
								+ stats2 + " " + player);
					}
					values.put("team", teamTemp);
					values.put("player", player);
					values.put("stats1", stats1);
					values.put("stats2", stats2);
					Uri uri = Uri.parse(TeamContentProvider.CONTENT_URI_2 + "/"
							+ ID);
					getActivity().getContentResolver().update(uri, values,
							null, null);
					((Startup) getActivity()).getFragmentScore().updateStatsDatabase(teamTemp,
							stats1, stats2, player, 1,1);
					fillData();
				}
				
			}
		}
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
	}

}
