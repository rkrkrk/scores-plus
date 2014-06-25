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
	private Long ID;

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
		String teamTemp = "", stats1Temp = "", stats2Temp = "";
		String playerTemp = "", typeTemp = "";
		Uri uri = TeamContentProvider.CONTENT_URI_2;
		String[] projection = { TeamContentProvider.STATS1,
				TeamContentProvider.STATS2, TeamContentProvider.STATSPLAYER,
				TeamContentProvider.STATSTYPE, TeamContentProvider.STATSTEAM };
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		ID = info.id;
		String[] args = { Long.toString(ID) };
		Cursor c1 = getActivity().getContentResolver().query(uri, projection,
				"_id=?", args, null);
		if (c1.getCount() > 0) {
			c1.moveToFirst();
			teamTemp = c1.getString(c1
					.getColumnIndexOrThrow(TeamContentProvider.STATSTEAM));
			stats1Temp = c1.getString(c1
					.getColumnIndexOrThrow(TeamContentProvider.STATS1));
			stats2Temp = c1.getString(c1
					.getColumnIndexOrThrow(TeamContentProvider.STATS2));
			playerTemp = c1.getString(c1
					.getColumnIndexOrThrow(TeamContentProvider.STATSPLAYER));
			typeTemp = c1.getString(c1
					.getColumnIndexOrThrow(TeamContentProvider.STATSTYPE));
		}
		switch (item.getItemId()) {
		case R.id.menu_delete:
			// // Delete a row / player
			Log.e("del", " ");
			Toast.makeText(getActivity(), "wtf", Toast.LENGTH_LONG).show();

			uri = Uri.parse(TeamContentProvider.CONTENT_URI_2 + "/" + ID);
			getActivity().getContentResolver().delete(uri, null, null);
			Toast.makeText(getActivity(), "stats entry deleted",
					Toast.LENGTH_LONG).show();
			fillData();
			((Startup) getActivity()).getFragmentReview().fillData();
			((Startup) getActivity()).getFragmentScore().undo(teamTemp,
					stats1Temp, stats2Temp, playerTemp, typeTemp);
			return true;

		case R.id.menu_edit:
			getTeam(teamTemp);
			Intent input = new Intent(getActivity(), InputActivity.class);
			input.putExtra("teamLineup", teamLineUp);
			input.putExtra("stats1", stats1Temp);
			input.putExtra("stats2", stats2Temp);
			input.putExtra("player", playerTemp);
			input.putExtra("teamName", teamTemp);
			startActivityForResult(input, 9);
			return true;
		case R.id.menu_insert:
			Log.e("inserty", " ");
			Toast.makeText(getActivity(), "insert", Toast.LENGTH_LONG).show();
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		//handle edit
		if (requestCode == 9) {
			// A contact was picked. Here we will just display it
			// to the user.
			if (data != null) {
				String stats1 = data.getStringExtra("stats1");
				String stats2 = data.getStringExtra("stats2");
				String player = data.getStringExtra("player");
				String teamName = data.getStringExtra("teamName");
				if (!(stats1.equals("") && stats2.equals("") && player
						.equals(""))) {
					Log.e("eventback", stats1 + " - " + stats2 + " - " + player
							+ " - " + teamName + " - "+ID);
					ContentValues values = new ContentValues();
					values.put("team", teamName);
					values.put("player", player);
					values.put("stats1", stats1);
					values.put("stats2", stats2);
					Uri uri = Uri.parse(TeamContentProvider.CONTENT_URI_2 + "/"
							+ ID);
					getActivity().getContentResolver().update(uri, values,
							null, null);
				}
				fillData();
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
