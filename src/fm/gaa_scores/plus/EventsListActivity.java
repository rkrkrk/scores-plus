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

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;



public class EventsListActivity extends ListActivity {
	private ArrayList<String> eventsUndoList = new ArrayList<String>();
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_list_layout);
		eventsUndoList.clear();
		Button back = (Button) findViewById(R.id.bBackL);
		back.setOnClickListener(goBack);

		fillData();
		registerForContextMenu(getListView());
	}

	// method to read in panel list from database and list contents on screen
	private void fillData() {
		// retreive shots info from database
		Uri allTitles = TeamContentProvider.CONTENT_URI_2;
		String[] from = new String[] { TeamContentProvider.STATSLINE };
		String[] projection = { TeamContentProvider._ID,
				TeamContentProvider.STATSLINE };

		int[] to = new int[] { R.id.listrtxt };

		CursorLoader cL = new CursorLoader(this, allTitles,
				projection, null, null, TeamContentProvider.STATSID + " desc");
		Cursor c1 = cL.loadInBackground();

		SimpleCursorAdapter reminders = new SimpleCursorAdapter(this,
				R.layout.event_row_layout, c1, from, to, 0);

		setListAdapter(reminders);
	}
	
	OnClickListener goBack = new OnClickListener() {
		@Override
		public void onClick(View v) {
			EventsListActivity.this.finish();
		}
	};

	// set up long press menu
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater mi = getMenuInflater();
		mi.inflate(R.menu.list_menu_longpress, menu);
	}

	@Override
	// deal with selection from long press menu
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_delete1:
			// // Delete a row / player
			String strTemp = "";
			Uri uri = TeamContentProvider.CONTENT_URI_2;
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
					.getMenuInfo();
			String[] args = { Long.toString(info.id) };
			Cursor c1 = this.getContentResolver().query(uri, null,
					"_id=?", args, null);
			if (c1.getCount() > 0) {
				c1.moveToFirst();
				strTemp = c1.getString(c1
						.getColumnIndexOrThrow(TeamContentProvider.STATSLINE));
			}
			uri = Uri.parse(TeamContentProvider.CONTENT_URI_2 + "/" + info.id);
			this.getContentResolver().delete(uri, null, null);
			Toast.makeText(this, "stats entry deleted",
					Toast.LENGTH_LONG).show();
			fillData();
			eventsUndoList.add(strTemp);
//			((Startup) getActivity()).getFragmentScore().undo(strTemp);
			return true;
		}
		return super.onContextItemSelected(item);
	}
	
	public void finish() {
		  // Prepare data intent 
		  Intent i = new Intent();
		  i.putStringArrayListExtra("eventsUndoList", eventsUndoList);
		  // Activity finished ok, return the data
		  setResult(RESULT_OK, i);
		  super.finish();
		} 

}
