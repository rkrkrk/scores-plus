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
	private ArrayList<String> teamu = new ArrayList<String>();
	private ArrayList<String> stats1u = new ArrayList<String>();
	private ArrayList<String> stats2u = new ArrayList<String>();
	private ArrayList<String> playeru = new ArrayList<String>();
	private ArrayList<String> typeu = new ArrayList<String>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_list_layout);
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

		CursorLoader cL = new CursorLoader(this, allTitles, projection, null,
				null, TeamContentProvider.STATSID + " desc");
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
			Cursor c1 = this.getContentResolver().query(uri, projection,
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
			this.getContentResolver().delete(uri, null, null);
			Toast.makeText(this, "stats entry deleted", Toast.LENGTH_LONG)
					.show();
			
			teamu.add(teamTemp);
			stats1u.add(stats1Temp);
			stats2u.add(stats2Temp);
			playeru.add(playerTemp);
			typeu.add(typeTemp);
			fillData();
//			 ((Startup) getActivity()).getFragmentScore().undo(strTemp);
			return true;
		}
		return super.onContextItemSelected(item);
	}

	public void finish() {
		// Prepare data intent
		if (teamu.size()>0){
			String[] team = new String[teamu.size()];
			String[] stats1 = new String[teamu.size()];
			String[] stats2 = new String[teamu.size()];
			String[] player = new String[teamu.size()];
			String[] type = new String[teamu.size()];
					
			for (int j = 0; j < teamu.size(); j++) {
				team[j]=teamu.get(j);
				stats1[j]=stats1u.get(j);
				stats2[j]=stats2u.get(j);
				player[j]=playeru.get(j);
				type[j]=typeu.get(j);		
			}
			Intent i = new Intent();
			i.putExtra("team",team);
			i.putExtra("stats1",stats1);
			i.putExtra("stats2",stats2);
			i.putExtra("player",player);
			i.putExtra("type",type);
			setResult(RESULT_OK, i);
		}
		else {
			setResult(RESULT_OK);
		}
		super.finish();
	}

}
