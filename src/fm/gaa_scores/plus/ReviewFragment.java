/*
 *  MatchReviewFragment.java
 *
 *  Written by: Fintan Mahon 12101524
 *  
 *  Description: GUI to display match score and match statistics data summary. 
 *  Also can start activities to view detailed tables of match statistics
 *  
 * store data to database tables and pass relevant details into MatchRecordReview
 *  
 *  Written on: Jan 2013
 *  
 * 
 */
package fm.gaa_scores.plus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class ReviewFragment extends Fragment {
	private int homeGoals, homePoints, homeTotal, oppGoals, oppPoints,
			oppTotal;
	private TextView tHomeGoals, tHomePoints, tHomeTotal, tOppGoals,
			tOppPoints;
	private TextView tShotGoalsHome;
	private TextView tShotWidesHome;
	private TextView tShotPointsHome;
	private TextView tShotSavedHome;
	private TextView tShotPostsHome;
	private TextView tShot45Home;
	private TextView tTotPuckHome;
	private TextView tShotGoalsPlayHome;
	private TextView tShotPointsPlayHome;
	private TextView tFreeConcededHome;
	private TextView tFreeConcededOpp;
	private TextView tShotPointsPlayOpp;
	private TextView tPuckWonCleanHome;
	private TextView tPuckLostCleanHome;
	private TextView tPuckWonCleanOpp;
	private TextView tPuckLostCleanOpp;
	private TextView tOwnTeam, tOppTeam;
	private TextView tCardHome, tCardOpp;
	private Button bSendAll, bTweetAll;
	private int red = 0, yellow = 0, sub = 0;

	private int shotGoalsHome = 0, shotPointsHome = 0;
	private int shotGoalsPlayHome = 0, shotPointsPlayHome = 0;
	private int shotGoalsPlayOpp = 0, shotPointsPlayOpp = 0;
	private int shotWidesHome = 0, shotSavedHome = 0, shotPostsHome = 0;
	private int freeConcededHome = 0;
	private int freeConcededOpp = 0;
	private int shot45Home = 0, shot45Opp = 0;
	private int totPHome = 0, totPOpp = 0;
	int puckWonCleanHome = 0, puckWonCleanHomePerCent = 0;
	int puckLostCleanHome = 0, puckLostCleanHomePerCent = 0;
	int puckWonBreakHome = 0, puckWonBreakHomePerCent = 0;
	int puckLostBreakHome = 0, puckLostBreakHomePerCent = 0;
	int puckWonCleanOpp = 0, puckWonCleanOppPerCent = 0;
	int puckLostCleanOpp = 0, puckLostCleanOppPerCen = 0;
	int puckWonBreakOpp = 0, puckWonBreakOppPerCent = 0;
	int puckLostBreakOpp = 0, puckLostBreakOppPerCent = 0;
	int puckOutTotalHome = 0, puckOutTotalOpp = 0;
	int puckOtherHome = 0, puckOtherHomePerCent = 0;
	int puckOtherOpp = 0, puckOtherOppPerCent = 0;

	private TextView tOppTotal;
	private TextView tShotGoalsOpp, tShotGoalsPlayOpp;
	private TextView tShotWidesOpp;
	private TextView tShotPointsOpp;
	private TextView tShotSavedOpp;
	private TextView tShotPostsOpp;
	private TextView tShot45Opp;
	private TextView tTotPuckOpp;
	private int shotGoalsOpp = 0, shotPointsOpp = 0;
	private int shotWidesOpp = 0, shotSavedOpp = 0, shotPostsOpp = 0;
	private ListView listViewStats;
	private String cardHome = "", subHome = "";
	private String cardOpp = "", subOpp = "";

	@Override
	// start main method to display screen
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.review_layout, container, false);
		// Open up shared preferences file to read in persisted data on startup
		SharedPreferences sharedPref = getActivity().getSharedPreferences(
				"team_stats_review_data", Context.MODE_PRIVATE);

		// get the tag name of this Fragment and pass it up to the parent
		// activity MatchApplication so that this Fragment may be accessed
		// by other fragments through using a reference created from tag name
		String myTag = getTag();
		((Startup) getActivity()).setTagFragmentReview(myTag);
		this.setHasOptionsMenu(true);

		// set up text buttons edittexts etc.
		tOwnTeam = (TextView) v.findViewById(R.id.textViewRevHome);
		tOppTeam = (TextView) v.findViewById(R.id.textViewRevOpp);

		tOwnTeam.setText(sharedPref.getString("OWNTEAM", "OWN TEAM"));
		tOppTeam.setText(sharedPref.getString("OPPTEAM", "OPPOSITION"));

		tHomeGoals = (TextView) v.findViewById(R.id.tVHomeGoals);
		tHomePoints = (TextView) v.findViewById(R.id.tVHomePoints);
		tHomeTotal = (TextView) v.findViewById(R.id.tVHomeTotal);
		tOppGoals = (TextView) v.findViewById(R.id.tVOppGoals);
		tOppPoints = (TextView) v.findViewById(R.id.tVOppPoints);
		tOppTotal = (TextView) v.findViewById(R.id.tVOppTotal);

		tShotGoalsHome = (TextView) v.findViewById(R.id.tVwShotsGoalsNo);
		tShotPointsHome = (TextView) v.findViewById(R.id.tVwShotsPointsNo);
		tShotWidesHome = (TextView) v.findViewById(R.id.tVwShotsWidesNo);
		tShotSavedHome = (TextView) v.findViewById(R.id.tVwShotsSavedNo);
		tShotPostsHome = (TextView) v.findViewById(R.id.tVwShotsPostsNo);

		tShotGoalsPlayHome = (TextView) v.findViewById(R.id.tVGoalsPlay);
		tShotPointsPlayHome = (TextView) v.findViewById(R.id.tVPointsPlay);
		tShotGoalsPlayOpp = (TextView) v.findViewById(R.id.tVGoalsOppPlay);
		tShotPointsPlayOpp = (TextView) v.findViewById(R.id.tVPointsOppPlay);

		tShotGoalsOpp = (TextView) v.findViewById(R.id.tVwShotsGoalsOppNo);
		tShotPointsOpp = (TextView) v.findViewById(R.id.tVwShotsPointsOppNo);
		tShotWidesOpp = (TextView) v.findViewById(R.id.tVwShotsWidesOppNo);
		tShotSavedOpp = (TextView) v.findViewById(R.id.tVwShotsSavedOppNo);
		tShotPostsOpp = (TextView) v.findViewById(R.id.tVwShotsPostsOppNo);

		tShot45Home = (TextView) v.findViewById(R.id.tVwHome45);
		tTotPuckHome = (TextView) v.findViewById(R.id.tVwHomeTotPuck);
		tShot45Opp = (TextView) v.findViewById(R.id.tVwOpp45);
		tTotPuckOpp = (TextView) v.findViewById(R.id.tVwOppTotPuck);

		tCardHome = (TextView) v.findViewById(R.id.cardsHome);
		tCardOpp = (TextView) v.findViewById(R.id.cardsOpp);

		// Set up output for frees
		tFreeConcededHome = (TextView) v.findViewById(R.id.tVwFreeWonHome);
		tFreeConcededOpp = (TextView) v.findViewById(R.id.tVwFreeWonOpp);

		// Set up output for puckouts
		tPuckWonCleanHome = (TextView) v.findViewById(R.id.tVwPuckWonCleanHome);
		tPuckLostCleanHome = (TextView) v
				.findViewById(R.id.tVPuckLostCleanHome);
		tPuckWonCleanOpp = (TextView) v.findViewById(R.id.tVwPuckWonCleanOpp);
		tPuckLostCleanOpp = (TextView) v.findViewById(R.id.tVPuckLostCleanOpp);

		// Read in score from persisted data
		homeGoals = sharedPref.getInt("HOMEGOALS", 0);
		homePoints = sharedPref.getInt("HOMEPOINTS", 0);
		oppGoals = sharedPref.getInt("OPPGOALS", 0);
		oppPoints = sharedPref.getInt("OPPPOINTS", 0);

		// update screen if persisted data exists
		if (homeGoals + homePoints + oppGoals + oppPoints > 0) {
			settHomeGoals(homeGoals);
			settHomePoints(homePoints);
			settOppGoals(oppGoals);
			settOppPoints(oppPoints);
		}

		// setup shots/frees/puckouts values from persisted data
		shotGoalsHome = sharedPref.getInt("SHOTGOALSHOME", 0);
		shotPointsHome = sharedPref.getInt("SHOTPOINTSHOME", 0);
		shotWidesHome = sharedPref.getInt("SHOTWIDESHOME", 0);
		shotSavedHome = sharedPref.getInt("SHOTSAVEDHOME", 0);
		shotPostsHome = sharedPref.getInt("SHOTPOSTSHOME", 0);
		shotGoalsPlayHome = sharedPref.getInt("SHOTGOALSPLAYHOME", 0);
		shotPointsPlayHome = sharedPref.getInt("SHOTPOINTSPLAYHOME", 0);
		shot45Home = sharedPref.getInt("SHOT45HOME", 0);
		addtShotGoalsHome(0);
		addtShotGoalsPlayHome(0);
		addtShotPointsHome(0);
		addtShotPointsPlayHome(0);
		addtShotWidesHome(0);
		addtShotSavedHome(0);
		addtShotPostsHome(0);
		addtShot45Home(0);

		shotGoalsOpp = sharedPref.getInt("SHOTGOALSOPP", 0);
		shotPointsOpp = sharedPref.getInt("SHOTPOINTSOPP", 0);
		shotWidesOpp = sharedPref.getInt("SHOTWIDESOPP", 0);
		shotSavedOpp = sharedPref.getInt("SHOTSAVEDOPP", 0);
		shotPostsOpp = sharedPref.getInt("SHOTPOSTSOPP", 0);
		shotGoalsPlayOpp = sharedPref.getInt("SHOTGOALSPLAYOPP", 0);
		shotPointsPlayOpp = sharedPref.getInt("SHOTPOINTSPLAYOPP", 0);
		shot45Opp = sharedPref.getInt("SHOT45OPP", 0);
		addtShotGoalsOpp(0);
		addtShotGoalsPlayOpp(0);
		addtShotPointsOpp(0);
		addtShotPointsPlayOpp(0);
		addtShotWidesOpp(0);
		addtShotSavedOpp(0);
		addtShotPostsOpp(0);
		addtShot45Opp(0);

		freeConcededHome = sharedPref.getInt("FREEWONHOME", 0);
		freeConcededOpp = sharedPref.getInt("FREEWONOPP", 0);
		addFreeConcededHome(0);
		addFreeConcededOpp(0);

		totPHome = sharedPref.getInt("TOTPHOME", 0);
		puckWonCleanHome = sharedPref.getInt("PUCKWONCLEANHOME", 0);
		puckLostCleanHome = sharedPref.getInt("PUCKLOSTCLEANHOME", 0);

		totPOpp = sharedPref.getInt("TOTPOPP", 0);
		puckWonCleanOpp = sharedPref.getInt("PUCKWONCLEANOPP", 0);
		puckLostCleanOpp = sharedPref.getInt("PUCKLOSTCLEANOPP", 0);
		addPuckTotHome(0);
		addPuckWonCleanHome(0);
		addPuckLostCleanHome(0);
		addPuckTotOpp(0);
		addPuckWonCleanOpp(0);
		addPuckLostCleanOpp(0);

		bSendAll = (Button) v.findViewById(R.id.bSendAll);
		bSendAll.setOnClickListener(sendAllListener);
		bTweetAll = (Button) v.findViewById(R.id.bTweetAll);
		bTweetAll.setOnClickListener(tweetAllListener);

		// fill in list view with datavbase
		listViewStats = (ListView) v.findViewById(R.id.listView1);

		updateListView();
		updateCardsSubs();

		registerForContextMenu(listViewStats);

		return v;

	}

	// set up long press menu to delete entry from stats db
	@Override
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
			String strTemp = "";
			Uri uri = TeamContentProvider.CONTENT_URI_2;
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
					.getMenuInfo();
			String[] args = { Long.toString(info.id) };
			Cursor c1 = getActivity().getContentResolver().query(uri, null,
					"_id=?", args, null);
			if (c1.getCount() > 0) {
				c1.moveToFirst();
				strTemp = c1.getString(c1
						.getColumnIndexOrThrow(TeamContentProvider.STATSLINE));
			}
			uri = Uri.parse(TeamContentProvider.CONTENT_URI_2 + "/" + info.id);
			getActivity().getContentResolver().delete(uri, null, null);
			Toast.makeText(getActivity(), "stats entry deleted",
					Toast.LENGTH_LONG).show();
			updateListView();
			((Startup) getActivity()).getFragmentScore().undo(strTemp);
			return true;
		}
		return super.onContextItemSelected(item);
	}

	// *******************Home Shots********************///
	// increment counters for home team shots
	public void addtShotGoalsHome(int i) {
		shotGoalsHome = shotGoalsHome + i;
		tShotGoalsHome.setText(String.valueOf(shotGoalsHome));
	}

	public void addtShotGoalsPlayHome(int i) {
		shotGoalsPlayHome = shotGoalsPlayHome + i;
		tShotGoalsPlayHome.setText(String.valueOf(shotGoalsPlayHome));
	}

	public void addtShotPointsHome(int i) {
		shotPointsHome = shotPointsHome + i;
		tShotPointsHome.setText(String.valueOf(shotPointsHome));
	}

	public void addtShotPointsPlayHome(int i) {
		shotPointsPlayHome = shotPointsPlayHome + i;
		tShotPointsPlayHome.setText(String.valueOf(shotPointsPlayHome));
	}

	public void addtShotWidesHome(int i) {
		shotWidesHome = shotWidesHome + i;
		tShotWidesHome.setText(String.valueOf(shotWidesHome));
	}

	public void addtShotSavedHome(int i) {
		shotSavedHome = shotSavedHome + i;
		tShotSavedHome.setText(String.valueOf(shotSavedHome));
	}

	public void addtShotPostsHome(int i) {
		shotPostsHome = shotPostsHome + i;
		tShotPostsHome.setText(String.valueOf(shotPostsHome));
	}

	public void addtShot45Home(int i) {
		shot45Home = shot45Home + i;
		tShot45Home.setText(String.valueOf(shot45Home));
	}

	// *******************Opp Shots********************///
	// increment counters for opposition team shots
	public void addtShotGoalsOpp(int i) {
		shotGoalsOpp = shotGoalsOpp + i;
		tShotGoalsOpp.setText(String.valueOf(shotGoalsOpp));
	}

	public void addtShotGoalsPlayOpp(int i) {
		shotGoalsPlayOpp = shotGoalsPlayOpp + i;
		tShotGoalsPlayOpp.setText(String.valueOf(shotGoalsPlayOpp));
	}

	public void addtShotPointsOpp(int i) {
		shotPointsOpp = shotPointsOpp + i;
		tShotPointsOpp.setText(String.valueOf(shotPointsOpp));
	}

	public void addtShotPointsPlayOpp(int i) {
		shotPointsPlayOpp = shotPointsPlayOpp + i;
		tShotPointsPlayOpp.setText(String.valueOf(shotPointsPlayOpp));
	}

	public void addtShotWidesOpp(int i) {
		shotWidesOpp = shotWidesOpp + i;
		tShotWidesOpp.setText(String.valueOf(shotWidesOpp));
	}

	public void addtShotSavedOpp(int i) {
		shotSavedOpp = shotSavedOpp + i;
		tShotSavedOpp.setText(String.valueOf(shotSavedOpp));
	}

	public void addtShotPostsOpp(int i) {
		shotPostsOpp = shotPostsOpp + i;
		tShotPostsOpp.setText(String.valueOf(shotPostsOpp));
	}

	public void addtShot45Opp(int i) {
		shot45Opp = shot45Opp + i;
		tShot45Opp.setText(String.valueOf(shot45Opp));
	}

	// ////////////////Update Free Section////////////////////////////
	// increment counters for frees
	public void addFreeConcededHome(int i) {
		freeConcededHome = freeConcededHome + i;
		tFreeConcededHome.setText(String.valueOf(freeConcededHome));
	}

	public void addFreeConcededOpp(int i) {
		freeConcededOpp = freeConcededOpp + i;
		tFreeConcededOpp.setText(String.valueOf(freeConcededOpp));
	}

	// ////////////////Update PuckOuts Section////////////////////////////
	// increment counters for puck outs
	public void addPuckTotHome(int i) {// ///
		totPHome = totPHome + i;
		tTotPuckHome.setText(String.valueOf(totPHome));
	}

	public void addPuckWonCleanHome(int i) {// ///
		puckWonCleanHome = puckWonCleanHome + i;
		tPuckWonCleanHome.setText(String.valueOf(puckWonCleanHome));
	}

	public void addPuckLostCleanHome(int i) {
		puckLostCleanHome = puckLostCleanHome + i;
		tPuckLostCleanHome.setText(String.valueOf(puckLostCleanHome));
	}

	public void addPuckTotOpp(int i) {// ///
		totPOpp = totPOpp + i;
		tTotPuckOpp.setText(String.valueOf(totPOpp));
	}

	public void addPuckWonCleanOpp(int i) {
		puckWonCleanOpp = puckWonCleanOpp + i;
		tPuckWonCleanOpp.setText(String.valueOf(puckWonCleanOpp));
	}

	public void addPuckLostCleanOpp(int i) {
		puckLostCleanOpp = puckLostCleanOpp + i;
		tPuckLostCleanOpp.setText(String.valueOf(puckLostCleanOpp));
	}

	// ///////////UPDATE SCORES////////////////////////////
	// methods called from RECORD fragment to update score
	// and totals
	public void settHomeGoals(int i) {
		homeGoals = i;
		homeTotal = homeGoals * 3 + homePoints;
		tHomeGoals.setText(String.valueOf(homeGoals));
		tHomeTotal.setText(String.valueOf(homeTotal));
	}

	public void settHomePoints(int i) {
		homePoints = i;
		homeTotal = homeGoals * 3 + homePoints;
		tHomePoints.setText(String.valueOf(homePoints));
		tHomeTotal.setText(String.valueOf(homeTotal));
	}

	public void settOppGoals(int i) {
		oppGoals = i;
		oppTotal = oppGoals * 3 + oppPoints;
		tOppGoals.setText(String.valueOf(oppGoals));
		tOppTotal.setText(String.valueOf(oppTotal));
	}

	public void settOppPoints(int i) {
		oppPoints = i;
		oppTotal = oppGoals * 3 + oppPoints;
		tOppPoints.setText(String.valueOf(oppPoints));
		tOppTotal.setText(String.valueOf(oppTotal));
	}

	// method to reset all stats values to zero
	public void resetStats() {
		shotGoalsHome = 0;
		tShotGoalsHome.setText("0");
		shotPointsHome = 0;
		tShotPointsHome.setText("0");
		shotWidesHome = 0;
		tShotWidesHome.setText("0");
		shotSavedHome = 0;
		tShotSavedHome.setText("0");
		shotPostsHome = 0;
		tShotPostsHome.setText("0");
		shotGoalsPlayHome = 0;
		tShotGoalsPlayHome.setText("0");
		shotPointsPlayHome = 0;
		tShotPointsPlayHome.setText("0");
		shot45Home = 0;
		tShot45Home.setText("0");

		shotGoalsOpp = 0;
		tShotGoalsOpp.setText("0");
		shotPointsOpp = 0;
		tShotPointsOpp.setText("0");
		shotWidesOpp = 0;
		tShotWidesOpp.setText("0");
		shotSavedOpp = 0;
		tShotSavedOpp.setText("0");
		shotPostsOpp = 0;
		tShotPostsOpp.setText("0");
		shotGoalsPlayOpp = 0;
		tShotGoalsPlayOpp.setText("0");
		shotPointsPlayOpp = 0;
		tShotPointsPlayOpp.setText("0");
		shot45Opp = 0;
		tShot45Opp.setText("0");

		freeConcededHome = 0;
		tFreeConcededHome.setText("0");
		freeConcededOpp = 0;
		tFreeConcededOpp.setText("0");

		puckWonCleanHome = 0;
		tPuckWonCleanHome.setText("0");
		puckLostCleanHome = 0;
		tPuckLostCleanHome.setText("0");
		puckWonCleanOpp = 0;
		tPuckWonCleanOpp.setText("0");
		puckLostCleanOpp = 0;
		tPuckLostCleanOpp.setText("0");
		totPHome = 0;
		tTotPuckHome.setText("0");
		totPOpp = 0;
		tTotPuckOpp.setText("0");
		tCardHome.setText("");
		tCardOpp.setText("");
		updateCardsSubs();

	}

	// ///////////////////////////END OF ONCREATE///////////////////////////

	@Override
	public void onPause() {
		// persist data out to shared preferences file to be available for start
		// up
		super.onPause(); // Always call the superclass method first
		SharedPreferences sharedPref = getActivity().getSharedPreferences(
				"team_stats_review_data", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();

		editor.putString("OWNTEAM", tOwnTeam.getText().toString());
		editor.putString("OPPTEAM", tOppTeam.getText().toString());

		editor.putInt("HOMEGOALS", homeGoals);
		editor.putInt("HOMEPOINTS", homePoints);
		editor.putInt("OPPGOALS", oppGoals);
		editor.putInt("OPPPOINTS", oppPoints);

		editor.putInt("SHOTGOALSHOME", shotGoalsHome);
		editor.putInt("SHOTPOINTSHOME", shotPointsHome);
		editor.putInt("SHOTWIDESHOME", shotWidesHome);
		editor.putInt("SHOTSAVEDHOME", shotSavedHome);
		editor.putInt("SHOTPOSTSHOME", shotPostsHome);
		editor.putInt("SHOTGOALSPLAYHOME", shotGoalsPlayHome);
		editor.putInt("SHOTPOINTSPLAYHOME", shotPointsPlayHome);
		editor.putInt("SHOTGOALSOPP", shotGoalsOpp);
		editor.putInt("SHOTPOINTSOPP", shotPointsOpp);
		editor.putInt("SHOTWIDESOPP", shotWidesOpp);
		editor.putInt("SHOTSAVEDOPP", shotSavedOpp);
		editor.putInt("SHOTPOSTSOPP", shotPostsOpp);
		editor.putInt("SHOTGOALSPLAYOPP", shotGoalsPlayOpp);
		editor.putInt("SHOTPOINTSPLAYOPP", shotPointsPlayOpp);

		editor.putInt("FREEWONHOME", freeConcededHome);
		editor.putInt("FREEWONOPP", freeConcededOpp);

		editor.putInt("PUCKWONCLEANHOME", puckWonCleanHome);
		editor.putInt("PUCKLOSTCLEANHOME", puckLostCleanHome);
		editor.putInt("PUCKWONCLEANOPP", puckWonCleanOpp);
		editor.putInt("PUCKLOSTCLEANOPP", puckLostCleanOpp);

		editor.putInt("SHOT45HOME", shot45Home);
		editor.putInt("SHOT45OPP", shot45Opp);
		editor.putInt("TOTPHOME", totPHome);
		editor.putInt("TOTPOPP", totPOpp);

		editor.commit();
	}

	@Override
	public void onResume() {
		super.onResume();
		updateCardsSubs();
	}

	public void updateListView() {

		Uri allTitles = TeamContentProvider.CONTENT_URI_2;
		String[] from = new String[] { TeamContentProvider.STATSLINE };
		String[] projection = { TeamContentProvider._ID,
				TeamContentProvider.STATSLINE };

		int[] to = new int[] { R.id.listrtxt };

		CursorLoader cL = new CursorLoader(getActivity(), allTitles,
				projection, null, null, TeamContentProvider.STATSID + " desc");
		Cursor c1 = cL.loadInBackground();

		SimpleCursorAdapter reminders = new SimpleCursorAdapter(getActivity(),
				R.layout.single_row_list_layout, c1, from, to, 0);

		listViewStats.setAdapter(reminders);
	}

	// this method is called from the SETUP fragment to update the names of the
	// home and away teams and to receive team line and teams from setup screen
	public void setTeamNames(String homeTeam, String oppTeam) {
		if (!homeTeam.equals(""))
			tOwnTeam.setText(homeTeam);
		if (!oppTeam.equals(""))
			tOppTeam.setText(oppTeam);
	}

	public void updateCardsSubs() {
		int redHome = 0, redOpp = 0, yellowHome = 0, yellowOpp = 0, blackHome = 0, blackOpp = 0, subH = 0, subO = 0;
		cardHome = "";
		subHome = "";
		cardOpp = "";
		subOpp = "";
		Uri allTitles = TeamContentProvider.CONTENT_URI_2;
		String[] args = { tOwnTeam.getText().toString() };
		Cursor c1 = getActivity().getContentResolver().query(allTitles, null,
				null, null, TeamContentProvider.STATSID);
		if (c1.getCount() > 0) {
			String str;
			c1.moveToFirst();
			do {
				// insert players into positions
				str = c1.getString(c1
						.getColumnIndexOrThrow(TeamContentProvider.STATSLINE));
				if (str.indexOf("red card") >= 0) {
					if (str.indexOf(tOwnTeam.getText().toString()) >= 0) {
						redHome++;
					} else if (str.indexOf(tOppTeam.getText().toString()) >= 0) {
						redOpp++;
					}
				}
				if (str.indexOf("yellow card") >= 0) {
					if (str.indexOf(tOwnTeam.getText().toString()) >= 0) {
						yellowHome++;
					} else if (str.indexOf(tOppTeam.getText().toString()) >= 0) {
						yellowOpp++;
					}
				}
				if (str.indexOf("black card") >= 0) {
					if (str.indexOf(tOwnTeam.getText().toString()) >= 0) {
						blackHome++;
					} else if (str.indexOf(tOppTeam.getText().toString()) >= 0) {
						blackOpp++;
					}
				}
				if (str.indexOf("substitution") >= 0) {
					if (str.indexOf(tOwnTeam.getText().toString()) >= 0) {
						subH++;
					} else if (str.indexOf(tOppTeam.getText().toString()) >= 0) {
						subO++;
					}
				}
			} while (c1.moveToNext());
		}
		c1.close();

		if (redHome > 0 || yellowHome > 0 || blackHome > 0) {
			cardHome = "Cards: " + blackHome + "B  " + yellowHome + "Y  "
					+ redHome + "R    ";
		} else {
			cardHome = "";
		}
		if (redOpp > 0 || yellowOpp > 0 || blackOpp > 0) {
			cardOpp = "Cards: " + blackOpp + "B  " + yellowOpp + "Y  " + redOpp
					+ "R    ";
		} else {
			cardOpp = "";
		}
		if (subH > 0) {
			subHome = "Subs used: " + subH;
		} else {
			subHome = "";
		}
		if (subO > 0) {
			subOpp = "Subs used: " + subO;
		} else {
			subOpp = "";
		}
		tCardHome.setText(cardHome + subHome);
		tCardOpp.setText(cardOpp + subOpp);
	}

	// for reset buttons diplay message to long click, won't work with ordinary
	// click
	OnClickListener sendAllListener = new OnClickListener() {
		File root, outfile;

		@Override
		public void onClick(View v) {
			StringBuilder sb = new StringBuilder("");
			String[] projection1 = { TeamContentProvider.PANELID,
					TeamContentProvider.NAME, TeamContentProvider.POSN };
			CursorLoader cL;

			sb.append(((Startup) getActivity()).getFragmentScore().getLocText()
					+ "\n\n");

			Uri allTitles = TeamContentProvider.CONTENT_URI;

			sb.append("Team 1: " + tOwnTeam.getText() + "\n");

			// reset line up and read from database
			cL = new CursorLoader(getActivity(), allTitles, projection1,
					TeamContentProvider.TEAM + " = '" + tOwnTeam.getText()
							+ "'", null, TeamContentProvider.POSN);
			Cursor c1 = cL.loadInBackground();
			if (c1.getCount() > 0) {
				c1.moveToFirst();
				do {
					if (c1.getInt(c1
							.getColumnIndexOrThrow(TeamContentProvider.POSN)) > 0) {
						// read in player nicknames
						sb.append("position "
								+ c1.getInt(c1
										.getColumnIndexOrThrow(TeamContentProvider.POSN))
								+ " - "

								+ c1.getString(c1
										.getColumnIndexOrThrow(TeamContentProvider.NAME))
								+ "\n");
						// insert players into positions

					}
				} while (c1.moveToNext());

			}
			c1.close();

			sb.append("\nTeam 2: " + tOppTeam.getText() + "\n");

			cL = new CursorLoader(getActivity(), allTitles, projection1,
					TeamContentProvider.TEAM + " = '" + tOppTeam.getText()
							+ "'", null, TeamContentProvider.POSN);
			c1 = cL.loadInBackground();
			if (c1.getCount() > 0) {
				c1.moveToFirst();
				do {
					if (c1.getInt(c1
							.getColumnIndexOrThrow(TeamContentProvider.POSN)) > 0) {
						// read in player nicknames
						sb.append("position "
								+ c1.getInt(c1
										.getColumnIndexOrThrow(TeamContentProvider.POSN))
								+ " - "

								+ c1.getString(c1
										.getColumnIndexOrThrow(TeamContentProvider.NAME))
								+ "\n");
						// insert players into positions

					}
				} while (c1.moveToNext());

			}
			c1.close();

			// printout cards and subs
			allTitles = TeamContentProvider.CONTENT_URI_2;
			String[] projection = { TeamContentProvider.STATSID,
					TeamContentProvider.STATSLINE };
			cL = new CursorLoader(getActivity(), allTitles, projection, null,
					null, TeamContentProvider.STATSID);
			c1 = cL.loadInBackground();
			if (c1.getCount() > 0) {
				String str;
				int i = 0, cards = 0, subs = 0;
				c1.moveToFirst();
				do {
					str = c1.getString(c1
							.getColumnIndexOrThrow(TeamContentProvider.STATSLINE));
					if ((str.indexOf("--> off:") >= 0)
							&& (str.indexOf(tOwnTeam.getText().toString()) >= 0)) {
						if (subs == 0) {
							sb.append("\nSUBS:");
							subs = 1;
						}
						sb.append("\n" + str);
					}
					i++;
				} while (c1.moveToNext());
				c1.moveToFirst();
				do {
					str = c1.getString(c1
							.getColumnIndexOrThrow(TeamContentProvider.STATSLINE));
					if ((str.indexOf("--> off:") >= 0)
							&& (str.indexOf(tOppTeam.getText().toString()) >= 0)) {
						if (subs == 0) {
							sb.append("\nSUBS:");
							subs = 1;
						}
						sb.append("\n" + str);
					}
					i++;
				} while (c1.moveToNext());
				if (subs == 1) {
					sb.append("\n");
				}
				// subs done, get cards
				c1.moveToFirst();
				do {
					str = c1.getString(c1
							.getColumnIndexOrThrow(TeamContentProvider.STATSLINE));
					if (((str.indexOf("red card") >= 0)
							|| (str.indexOf("black card") >= 0) || (str
							.indexOf("yellow card") >= 0))
							&& (str.indexOf(tOwnTeam.getText().toString()) >= 0)) {
						if (cards == 0) {
							sb.append("\nCARDS:");
							cards = 1;
						}
						sb.append("\n" + str);
					}
					i++;
				} while (c1.moveToNext());
				c1.moveToFirst();
				do {
					str = c1.getString(c1
							.getColumnIndexOrThrow(TeamContentProvider.STATSLINE));
					if (((str.indexOf("red card") >= 0)
							|| (str.indexOf("black card") >= 0) || (str
							.indexOf("yellow card") >= 0))
							&& (str.indexOf(tOppTeam.getText().toString()) >= 0)) {
						if (cards == 0) {
							sb.append("\nCARDS:");
							cards = 1;
						}
						sb.append("\n" + str);
					}
					i++;
				} while (c1.moveToNext());

				c1.close();
			}

			sb.append("\n\nMATCH EVENTS\n");

			allTitles = TeamContentProvider.CONTENT_URI_2;
			String[] projection2 = { TeamContentProvider.STATSLINE };
			cL = new CursorLoader(getActivity(), allTitles, projection2, null,
					null, TeamContentProvider.STATSID);
			c1 = cL.loadInBackground();
			if (c1.getCount() > 0) {
				c1.moveToFirst();
				do {
					// read in player nicknames
					sb.append((c1.getString(c1
							.getColumnIndexOrThrow(TeamContentProvider.STATSLINE)))
							+ "\n");
				} while (c1.moveToNext());
			}
			c1.close();

			sb.append("\n\nMATCH STATS SUMMARY");

			sb.append("\nTeam 1: " + tOwnTeam.getText() + "\n");
			sb.append(shotGoalsHome + " Goals,  " + shotPointsHome
					+ " Points.  Total:" + tHomeTotal.getText() + "\n");
			sb.append(shotGoalsPlayHome + " goals from play  "
					+ shotPointsPlayHome + " points from play \n");
			sb.append("wides: " + shotWidesHome + "\n");
			sb.append("out for 45/65: " + shot45Home + "\n");
			sb.append("saved/short: " + shotSavedHome + "\n");
			sb.append("off posts: " + shotPostsHome + "\n");
			sb.append("frees conceded: " + freeConcededHome + "\n");
			sb.append("Total puckouts: " + totPHome + "\n");
			sb.append("own puckouts won: " + puckWonCleanHome + "\n");
			sb.append("own puckouts lost: " + puckLostCleanHome + "\n");
			sb.append(cardHome + subHome + "\n");

			sb.append("\nTeam 2: " + tOppTeam.getText() + "\n");
			sb.append(shotGoalsOpp + " Goals,  " + shotPointsOpp
					+ " Points.  Total:" + tOppTotal.getText() + "\n");
			sb.append(shotGoalsPlayOpp + " goals from play  "
					+ shotPointsPlayOpp + " points from play \n");
			sb.append("wides: " + shotWidesOpp + "\n");
			sb.append("out for 45/65: " + shot45Opp + "\n");
			sb.append("saved/short: " + shotSavedOpp + "\n");
			sb.append("off posts: " + shotPostsOpp + "\n");
			sb.append("frees conceded: " + freeConcededOpp + "\n");
			sb.append("Total puckouts: " + totPOpp + "\n");
			sb.append("own puckouts won: " + puckWonCleanOpp + "\n");
			sb.append("own puckouts lost: " + puckLostCleanOpp + "\n");
			sb.append(cardOpp + subOpp + "\n\n\n");

			sb.append("LIST OF SCORERS \n");

			allTitles = TeamContentProvider.CONTENT_URI_3;
			String[] from = new String[] {
					TeamContentProvider.SCORESNAME,
					// TeamContentProvider.SCORESTEAM,
					TeamContentProvider.SCORESGOALS,
					TeamContentProvider.SCORESPOINTS,
					TeamContentProvider.SCORESGOALSFREE,
					TeamContentProvider.SCORESPOINTSFREE,
					TeamContentProvider.SCORESMISS };

			// create array to map these fields to
			int[] to = new int[] { R.id.text1, R.id.text3, R.id.text4,
					R.id.text5, R.id.text6, R.id.text7 };

			// load database info from PanelContentProvider into a cursor and
			// use an
			// adapter to display on screen
			String[] args = { tOwnTeam.getText().toString() };
			c1 = getActivity().getContentResolver().query(allTitles, null,
					"team=?", args, TeamContentProvider.SCORESTOTAL + " DESC");

			String[] args1 = { tOppTeam.getText().toString() };
			Cursor c2 = getActivity().getContentResolver().query(allTitles,
					null, "team=?", args1,
					TeamContentProvider.SCORESTOTAL + " DESC");

			sb.append("player  **  Total Goals / Points  **  Goals/Points from frees/65s/45s/penalties/sidelines  **  wides/short/saved\n\n");
			sb.append(tOwnTeam.getText().toString() + " SCORERS \n\n");

			if (c1.getCount() > 0) {
				c1.moveToFirst();
				do {
					// read in player nicknames
					sb.append(c1.getString(c1
							.getColumnIndexOrThrow(TeamContentProvider.SCORESNAME))
							+ "  **   "
							+ c1.getString(c1
									.getColumnIndexOrThrow(TeamContentProvider.SCORESGOALS))
							+ "-"
							+ c1.getString(c1
									.getColumnIndexOrThrow(TeamContentProvider.SCORESPOINTS))
							+ "  **  "
							+ c1.getString(c1
									.getColumnIndexOrThrow(TeamContentProvider.SCORESGOALSFREE))
							+ "-"
							+ c1.getString(c1
									.getColumnIndexOrThrow(TeamContentProvider.SCORESPOINTSFREE))
							+ "  **  "
							+ c1.getString(c1
									.getColumnIndexOrThrow(TeamContentProvider.SCORESMISS))
							+ "\n\n");
					// insert players into positions

				} while (c1.moveToNext());
			}
			c1.close();
			sb.append("\n" + tOppTeam.getText().toString() + " SCORERS\n\n");
			if (c2.getCount() > 0) {
				c2.moveToFirst();
				do {
					// read in player nicknames
					sb.append(c2.getString(c2
							.getColumnIndexOrThrow(TeamContentProvider.SCORESNAME))
							+ "  **   "
							+ c2.getString(c2
									.getColumnIndexOrThrow(TeamContentProvider.SCORESGOALS))
							+ "-"
							+ c2.getString(c2
									.getColumnIndexOrThrow(TeamContentProvider.SCORESPOINTS))
							+ "  **  "
							+ c2.getString(c2
									.getColumnIndexOrThrow(TeamContentProvider.SCORESGOALSFREE))
							+ "-"
							+ c2.getString(c2
									.getColumnIndexOrThrow(TeamContentProvider.SCORESPOINTSFREE))
							+ "  **  "
							+ c2.getString(c2
									.getColumnIndexOrThrow(TeamContentProvider.SCORESMISS))
							+ "\n\n");
					// insert players into positions

				} while (c2.moveToNext());
			}
			c2.close();

			try {
				root = new File(Environment.getExternalStorageDirectory(),
						"GAA_APP_Export");
				if (!root.exists()) {
					root.mkdirs();
				}
				outfile = new File(root, "GAAScoresStatsMatchReview.txt");
				FileWriter writer = new FileWriter(outfile);
				String nl = System.getProperty("line.separator");
				writer.append("GAA Scores Stats App Match Data," + nl);
				writer.append(sb.toString());
				writer.flush();
				writer.close();
			} catch (IOException e) {
				Log.e("share file write failed", e.getMessage(), e);
				Toast.makeText(getActivity(),
						"Error: unable to write to share file\n",
						Toast.LENGTH_LONG).show();
			}
			
			Bitmap bitmap = createBitmap();

			File mPath = Environment
					.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
			OutputStream fout = null;
			File imageFile = new File(mPath, "GAAScoresStatsTweet.jpg");
			Uri uri = Uri.fromFile(imageFile);

			try {
				mPath.mkdirs();
				fout = new FileOutputStream(imageFile);
				bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fout);
				fout.flush();
				fout.close();

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
			emailIntent
					.putExtra(Intent.EXTRA_SUBJECT, "match report "
							+ ((Startup) getActivity()).getFragmentScore()
									.getLocText());
			emailIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
			emailIntent.setType("text/plain");
			String[] emailAttachments = new String[] { Environment
					.getExternalStorageDirectory()
					+ "/GAA_APP_Export/"
					+ "GAAScoresStatsMatchReview.txt" };
			// put email attachments into an ArrayList
			ArrayList<Uri> uris = new ArrayList<Uri>();
			for (String file : emailAttachments) {
				File uriFiles = new File(file);
				Uri u = Uri.fromFile(uriFiles);
				uris.add(u);
			}
			uris.add(uri);
			emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
			startActivity(Intent.createChooser(emailIntent, "Share Using:"));
		}
	};

	public Bitmap createBitmap() {
		// Create Bitmap to display team selection
		Bitmap bitmap = Bitmap.createBitmap(700, 440, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawColor(Color.rgb(204, 255, 204));
		Paint paint = new Paint();
		paint.setColor(Color.rgb(255, 255, 219));
		canvas.drawRect(350, 0, 700, 440, paint);
		paint.setColor(Color.BLACK);
		canvas.drawLine(350, 0, 350, 380, paint);
		paint.setAntiAlias(true);
		paint.setTextAlign(Align.CENTER);
		paint.setTextSize(22);
		// Write teams
		// sb.append("player  **  Total Goals / Points  **  Goals/Points from frees/65s/45s/penalties/sidelines  **  wides/short/saved\n\n");

		canvas.drawText(tOwnTeam.getText().toString(), 175, 25, paint);
		canvas.drawText(homeGoals + "-" + homePoints + " (" + homeTotal + ")",
				175, 50, paint);
		paint.setTextAlign(Align.LEFT);
		paint.setColor(Color.RED);
		canvas.drawText("No.", 200, 75, paint);
		paint.setTextSize(18);
		canvas.drawText("from play", 260, 75, paint);
		paint.setTextSize(22);
		paint.setColor(Color.BLACK);
		canvas.drawText("Goals", 5, 100, paint);
		canvas.drawText(homeGoals + " ", 210, 100, paint);
		canvas.drawText(shotGoalsPlayHome + " ", 285, 100, paint);

		canvas.drawText("Points", 5, 125, paint);
		canvas.drawText(homePoints + " ", 210, 125, paint);
		canvas.drawText(shotPointsPlayHome + " ", 285, 125, paint);

		canvas.drawText("Wides", 5, 150, paint);
		canvas.drawText(shotWidesHome + " ", 210, 150, paint);

		canvas.drawText("Out for 45/65", 5, 175, paint);
		canvas.drawText(shot45Home + " ", 210, 175, paint);

		canvas.drawText("Saved/Short", 5, 200, paint);
		canvas.drawText(shotSavedHome + " ", 210, 200, paint);

		canvas.drawText("Off Posts", 5, 225, paint);
		canvas.drawText(shotPostsHome + " ", 210, 225, paint);

		canvas.drawText("Frees Conceded", 5, 250, paint);
		canvas.drawText(freeConcededHome + " ", 210, 250, paint);

		canvas.drawText("Total Puckouts", 5, 275, paint);
		canvas.drawText(totPHome + " ", 210, 275, paint);

		canvas.drawText("Own Puckout Won", 5, 300, paint);
		canvas.drawText(puckWonCleanHome + " ", 210, 300, paint);

		canvas.drawText("Own Puckout Lost", 5, 325, paint);
		canvas.drawText(puckLostCleanHome + " ", 210, 325, paint);

		canvas.drawText(cardHome + subHome, 5, 350, paint);

		canvas.drawText(tOppTeam.getText().toString(), 525, 25, paint);
		canvas.drawText(oppGoals + "-" + oppPoints + " (" + oppTotal + ")",
				525, 50, paint);
		paint.setTextAlign(Align.LEFT);
		paint.setColor(Color.RED);
		canvas.drawText("No.", 550, 75, paint);
		paint.setTextSize(18);
		canvas.drawText("from play", 620, 75, paint);
		paint.setTextSize(22);
		paint.setColor(Color.BLACK);
		canvas.drawText("Goals", 355, 100, paint);
		canvas.drawText(oppGoals + " ", 560, 100, paint);
		canvas.drawText(shotGoalsPlayOpp + " ", 645, 100, paint);

		canvas.drawText("Points", 355, 125, paint);
		canvas.drawText(oppPoints + " ", 560, 125, paint);
		canvas.drawText(shotPointsPlayOpp + " ", 645, 125, paint);

		canvas.drawText("Wides", 355, 150, paint);
		canvas.drawText(shotWidesOpp + " ", 560, 150, paint);

		canvas.drawText("O" + "ut for 45/65", 355, 175, paint);
		canvas.drawText(shot45Opp + " ", 560, 175, paint);

		canvas.drawText("Saved/Short", 355, 200, paint);
		canvas.drawText(shotSavedOpp + " ", 560, 200, paint);

		canvas.drawText("Off Posts", 355, 225, paint);
		canvas.drawText(shotPostsOpp + " ", 560, 225, paint);

		canvas.drawText("Frees Conceded", 355, 250, paint);
		canvas.drawText(freeConcededOpp + " ", 560, 250, paint);

		canvas.drawText("Total Puckouts", 355, 275, paint);
		canvas.drawText(totPOpp + " ", 560, 275, paint);

		canvas.drawText("Own Puckout Won", 355, 300, paint);
		canvas.drawText(puckWonCleanOpp + " ", 560, 300, paint);

		canvas.drawText("Own Puckout Lost", 355, 325, paint);
		canvas.drawText(puckLostCleanOpp + " ", 560, 325, paint);

		canvas.drawText(cardOpp + subOpp, 355, 350, paint);

		paint.setColor(Color.GRAY);
		paint.setTextSize(16);
		paint.setTextAlign(Align.CENTER);
		canvas.drawText("GAA Scores Stats Plus - Android App.", 350, 405, paint);
		canvas.drawText("Available free from Google Play Store", 350, 425,
				paint);
		return bitmap;
	}

	OnClickListener tweetAllListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			
			Bitmap bitmap = createBitmap();

			File mPath = Environment
					.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
			OutputStream fout = null;
			File imageFile = new File(mPath, "GAAScoresStatsTweet.jpg");
			Uri uri = Uri.fromFile(imageFile);

			try {
				mPath.mkdirs();
				fout = new FileOutputStream(imageFile);
				bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fout);
				fout.flush();
				fout.close();

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				final Intent shareIntent = findTwitterClient();
				shareIntent.putExtra(Intent.EXTRA_TEXT, tOwnTeam.getText()
						.toString()
						+ " v. "
						+ tOppTeam.getText().toString()
						+ " Stats \n"
						+ ((Startup) getActivity()).getFragmentScore()
								.getLocText());
				shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
				// introduce delay to give time to read in bitmap before sending
				// tweet
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						startActivity(Intent
								.createChooser(shareIntent, "Share"));
					}
				}, 400);
			} catch (Exception ex) {
				Toast.makeText(
						getActivity(),
						"Can't find twitter client\n"
								+ "Please install Twitter App\nand login to Twitter",
						Toast.LENGTH_LONG).show();
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
		Intent tweetIntent = new Intent(Intent.ACTION_SEND);
		tweetIntent.setType("text/plain");
		final PackageManager packageManager = getActivity().getPackageManager();
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

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		inflater.inflate(R.menu.review_menu, menu);
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
			ihelp.putExtra("HELP_ID", R.string.reviewHelp);
			startActivity(ihelp);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
