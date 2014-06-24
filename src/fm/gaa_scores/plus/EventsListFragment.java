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
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class EventsListFragment extends ListFragment {
	
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
		Cursor c1 = getActivity().getContentResolver().query(allTitles, projection,
				null, null, TeamContentProvider.STATSID + " DESC");	
		SimpleCursorAdapter reminders = new SimpleCursorAdapter(getActivity(),
		R.layout.event_row_layout, c1, from, to, 0);
		setListAdapter(reminders);
	}

	OnClickListener goBack = new OnClickListener() {
		@Override
		public void onClick(View v) {
//			EventsListActivity.this.finish();
		}
	};

	// set up long press menu
	@Override
//	public void onCreateContextMenu(ContextMenu menu, View v,
//			ContextMenuInfo menuInfo) {
//		super.onCreateContextMenu(menu, v, menuInfo);
//		MenuInflater mi = getMenuInflater();
//		mi.inflate(R.menu.list_menu_longpress, menu);
//	}
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater mi = getActivity().getMenuInflater();
		mi.inflate(R.menu.list_menu_longpress, menu);
	}

	@Override
	// deal with selection from long press menu
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_delete1:
			// // Delete a row / player
			String teamTemp = "",
			stats1Temp = "",
			stats2Temp = "";
			String playerTemp = "",
			typeTemp = "";
			Uri uri = TeamContentProvider.CONTENT_URI_2;
			String[] projection = { TeamContentProvider.STATS1,
					TeamContentProvider.STATS2,
					TeamContentProvider.STATSPLAYER,
					TeamContentProvider.STATSTYPE,
					TeamContentProvider.STATSTEAM };
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
					.getMenuInfo();
			String[] args = { Long.toString(info.id) };
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
				playerTemp = c1
						.getString(c1
								.getColumnIndexOrThrow(TeamContentProvider.STATSPLAYER));
				typeTemp = c1.getString(c1
						.getColumnIndexOrThrow(TeamContentProvider.STATSTYPE));
			}
			uri = Uri.parse(TeamContentProvider.CONTENT_URI_2 + "/" + info.id);
			getActivity().getContentResolver().delete(uri, null, null);
			Toast.makeText(getActivity(), "stats entry deleted", Toast.LENGTH_LONG)
					.show();
			fillData();
			((Startup) getActivity()).getFragmentReview().fillData();
			 ((Startup) getActivity()).getFragmentScore().undo(teamTemp, stats1Temp, stats2Temp, playerTemp, typeTemp);
			return true;
		}
		return super.onContextItemSelected(item);
	}

}
