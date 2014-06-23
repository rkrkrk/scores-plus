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
	private TextView tShotsTotalHome, tShotsPlayHome;
	private TextView tShotsTotalOpp, tShotsPlayOpp;
	private TextView tFreeConcededHome;
	private TextView tFreeConcededOpp;
	private TextView tShotPointsPlayOpp;
	private TextView tPuckWonCleanHome;
	private TextView tPuckLostCleanHome;
	private TextView tPuckWonCleanOpp;
	private TextView tPuckLostCleanOpp;
	private TextView tOwnTeam, tOppTeam;
	private TextView tCardHome, tCardOpp;
	private TextView tShotPointsPlayWidesHome, tShotPointsPlayWidesOpp;
	private TextView tShotPointsPlay45Home, tShotPointsPlay45Opp;
	private TextView tShotPointsPlaySavedHome, tShotPointsPlaySavedOpp;
	private TextView tShotPointsPlayPostsHome, tShotPointsPlayPostsOpp;
	private Button bSendAll, bTweetAll, bEvents;
	private int red = 0, yellow = 0, sub = 0;

	private int shotGoalsHome = 0, shotPointsHome = 0;
	private int shotGoalsPlayHome = 0, shotPointsPlayHome = 0;
	private int shotGoalsPlayOpp = 0, shotPointsPlayOpp = 0;
	private int shotWidesPlayHome = 0, shotWidesPlayOpp = 0;
	private int shot45PlayHome = 0, shot45PlayOpp = 0;
	private int shotSavedPlayHome = 0, shotSavedPlayOpp = 0;
	private int shotPostsPlayHome = 0, shotPostsPlayOpp = 0;
	private int shotWidesHome = 0, shotSavedHome = 0, shotPostsHome = 0;
	private int freeConcededHome = 0;
	private int freeConcededOpp = 0;
	private int shot45Home = 0, shot45Opp = 0;
	private int totPHome = 0, totPOpp = 0;
	private int puckWonCleanHome = 0, puckWonCleanHomePerCent = 0;
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

		tShotGoalsHome = (TextView) v.findViewById(R.id.tVwShotsGoalNo);
		tShotPointsHome = (TextView) v.findViewById(R.id.tVwShotsPointNo);
		tShotWidesHome = (TextView) v.findViewById(R.id.tVwShotsWideNo);
		tShot45Home = (TextView) v.findViewById(R.id.tVwHome45);
		tShotSavedHome = (TextView) v.findViewById(R.id.tVwShotsSavedNo);
		tShotPostsHome = (TextView) v.findViewById(R.id.tVwShotsPostsNo);

		tShotGoalsPlayHome = (TextView) v.findViewById(R.id.tVGoalsHomePlay);
		tShotPointsPlayHome = (TextView) v.findViewById(R.id.tVPointsHomePlay);
		tShotPointsPlayWidesHome = (TextView) v
				.findViewById(R.id.tVWidesHomePlay);
		tShotPointsPlay45Home = (TextView) v.findViewById(R.id.tV45HomePlay);
		tShotPointsPlaySavedHome = (TextView) v
				.findViewById(R.id.tVSavedHomePlay);
		tShotPointsPlayPostsHome = (TextView) v
				.findViewById(R.id.tVPostsHomePlay);

		tShotGoalsPlayOpp = (TextView) v.findViewById(R.id.tVGoalsOppPlay);
		tShotPointsPlayOpp = (TextView) v.findViewById(R.id.tVPointsOppPlay);
		tShotPointsPlayWidesOpp = (TextView) v
				.findViewById(R.id.tVWidesOppPlay);
		tShotPointsPlay45Opp = (TextView) v.findViewById(R.id.tV45OppPlay);
		tShotPointsPlaySavedOpp = (TextView) v
				.findViewById(R.id.tVSavedOppPlay);
		tShotPointsPlayPostsOpp = (TextView) v
				.findViewById(R.id.tVPostsOppPlay);

		tShotGoalsOpp = (TextView) v.findViewById(R.id.tVwShotsGoalsOppNo);
		tShotPointsOpp = (TextView) v.findViewById(R.id.tVwShotsPointsOppNo);
		tShotWidesOpp = (TextView) v.findViewById(R.id.tVwShotsWidesOppNo);
		tShot45Opp = (TextView) v.findViewById(R.id.tVwOpp45);
		tShotSavedOpp = (TextView) v.findViewById(R.id.tVwShotsSavedOppNo);
		tShotPostsOpp = (TextView) v.findViewById(R.id.tVwShotsPostsOppNo);

		tTotPuckHome = (TextView) v.findViewById(R.id.tVwHomeTotPuck);
		tTotPuckOpp = (TextView) v.findViewById(R.id.tVwOppTotPuck);

		tShotsTotalHome = (TextView) v.findViewById(R.id.tTotalShotsHome);
		tShotsPlayHome = (TextView) v.findViewById(R.id.tShotsPlayHome);
		tShotsTotalOpp = (TextView) v.findViewById(R.id.tTotalShotsOpp);
		tShotsPlayOpp = (TextView) v.findViewById(R.id.tShotsPlayOpp);

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

		bSendAll = (Button) v.findViewById(R.id.bSendAll);
		bSendAll.setOnClickListener(sendAllListener);
		bTweetAll = (Button) v.findViewById(R.id.bTweetAll);
		bTweetAll.setOnClickListener(tweetAllListener);
		bEvents = (Button) v.findViewById(R.id.bViewEvents);
		bEvents.setOnClickListener(listEvents);

		// fill in list view with datavbase
		listViewStats = (ListView) v.findViewById(R.id.listView1);

		updateListView();
		updateCardsSubs();
		updateShotsPerCent();

		registerForContextMenu(listViewStats);
		fillData();
		return v;

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

		editor.putInt("SHOTPOINTSWIDESHOME", shotWidesPlayHome);
		editor.putInt("SHOTPOINTS45HOME", shot45PlayHome);
		editor.putInt("SHOTPOINTSSAVEDHOME", shotSavedPlayHome);
		editor.putInt("SHOTPOINTSPOSTSHOME", shotPostsPlayHome);
		editor.putInt("SHOTPOINTSWIDESOPP", shotWidesPlayOpp);
		editor.putInt("SHOTPOINTS45OPP", shot45PlayOpp);
		editor.putInt("SHOTPOINTSSAVEDOPP", shotSavedPlayOpp);
		editor.putInt("SHOTPOINTSPOSTSOPP", shotPostsPlayOpp);

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
		fillData();
		updateCardsSubs();
		updateShotsPerCent();
	}

	public void fillData() {
		String team, stats1, stats2;
		totPOpp = 0;
		shotGoalsHome = 0;
		shotGoalsPlayHome = 0;
		shotPointsHome = 0;
		shotPointsPlayHome = 0;
		shotWidesHome = 0;
		shotWidesPlayHome = 0;
		shot45Home = 0;
		shot45PlayHome = 0;
		shotSavedHome = 0;
		shotSavedPlayHome = 0;
		shotPostsHome = 0;
		shotPostsPlayHome = 0;
		freeConcededHome = 0;
		puckWonCleanHome = 0;
		puckLostCleanHome = 0;
		totPHome = 0;
		shotGoalsOpp = 0;
		shotGoalsPlayOpp = 0;
		shotPointsOpp = 0;
		shotPointsPlayOpp = 0;
		shotWidesOpp = 0;
		shotWidesPlayOpp = 0;
		shot45Opp = 0;
		shot45PlayOpp = 0;
		shotSavedOpp = 0;
		shotSavedPlayOpp = 0;
		shotPostsOpp = 0;
		shotPostsPlayOpp = 0;
		freeConcededOpp = 0;
		puckWonCleanOpp = 0;
		puckLostCleanOpp = 0;

		Uri allTitles = TeamContentProvider.CONTENT_URI_2;
		// get home team first then opposition
		team = tOwnTeam.getText().toString();
		String[] args = { team, "t" };
		Cursor c1 = getActivity().getContentResolver().query(allTitles, null,
				"team=? AND type=?", args, null);
		if (c1.getCount() > 0) {
			c1.moveToFirst();
			do {
				stats1 = c1.getString(c1
						.getColumnIndexOrThrow(TeamContentProvider.STATS1));
				stats2 = c1.getString(c1
						.getColumnIndexOrThrow(TeamContentProvider.STATS2));

				if (stats1.equals("goal")) {
					totPOpp++;
					shotGoalsHome++;
					if ((!stats2.equals("from free"))
							&& (!stats2.equals("from 45/65"))
							&& (!stats2.equals("from penalty"))
							&& (!stats2.equals("from sideline"))) {
						shotGoalsPlayHome++;
					}
				} else if (stats1.equals("point")) {
					totPOpp++;
					shotPointsHome++;
					if ((!stats2.equals("from free"))
							&& (!stats2.equals("from 45/65"))
							&& (!stats2.equals("from penalty"))
							&& (!stats2.equals("from sideline"))) {
						shotPointsPlayHome++;
					}
				} else if (stats1.equals("wide")) {
					// increment counter in review page
					shotWidesHome++;
					totPOpp++;
					if ((!stats2.equals("from free"))
							&& (!stats2.equals("from 45/65"))
							&& (!stats2.equals("from penalty"))
							&& (!stats2.equals("from sideline"))) {
						shotWidesPlayHome++;
					}
				} else if (stats1.equals("out for 45/65")) {
					// increment counter in review page
					shot45Home++;
					if ((!stats2.equals("from free"))
							&& (!stats2.equals("from 45/65"))
							&& (!stats2.equals("from penalty"))
							&& (!stats2.equals("from sideline"))) {
						shot45PlayHome++;
					}
				} else if (stats1.equals("saved/short")) {
					// increment counter in review page
					shotSavedHome++;
					if ((!stats2.equals("from free"))
							&& (!stats2.equals("from 45/65"))
							&& (!stats2.equals("from penalty"))
							&& (!stats2.equals("from sideline"))) {
						shotSavedPlayHome++;
					}
				} else if (stats1.equals("off posts")) {
					// increment counter in review page
					shotPostsHome++;
					if ((!stats2.equals("from free"))
							&& (!stats2.equals("from 45/65"))
							&& (!stats2.equals("from penalty"))
							&& (!stats2.equals("from sideline"))) {
						shotPostsPlayHome++;
					}
				} else if (stats1.equals("free/pen conceded")) {
					// increment counter in review page
					freeConcededHome++;
				} else if (stats1.equals("own puck/kick out won")) {
					// increment counter in review page
					puckWonCleanHome++;
				} else if (stats1.equals("own puck/kick out lost")) {
					// increment counter in review page
					puckLostCleanHome++;
				}
			} while (c1.moveToNext());
			c1.close();
		}
		// OPPOSITON
		team = tOppTeam.getText().toString();
		String[] args2 = { team, "t" };
		c1 = getActivity().getContentResolver().query(allTitles, null,
				"team=? AND type=?", args2, null);
		if (c1.getCount() > 0) {
			c1.moveToFirst();
			do {
				stats1 = c1.getString(c1
						.getColumnIndexOrThrow(TeamContentProvider.STATS1));
				stats2 = c1.getString(c1
						.getColumnIndexOrThrow(TeamContentProvider.STATS2));

				if (stats1.equals("goal")) {
					totPHome++;
					shotGoalsOpp++;
					if ((!stats2.equals("from free"))
							&& (!stats2.equals("from 45/65"))
							&& (!stats2.equals("from penalty"))
							&& (!stats2.equals("from sideline"))) {
						shotGoalsPlayOpp++;
					}
				} else if (stats1.equals("point")) {
					totPHome++;
					shotPointsOpp++;
					if ((!stats2.equals("from free"))
							&& (!stats2.equals("from 45/65"))
							&& (!stats2.equals("from penalty"))
							&& (!stats2.equals("from sideline"))) {
						shotPointsPlayOpp++;
					}
				} else if (stats1.equals("wide")) {
					// increment counter in review page
					shotWidesOpp++;
					totPHome++;
					if ((!stats2.equals("from free"))
							&& (!stats2.equals("from 45/65"))
							&& (!stats2.equals("from penalty"))
							&& (!stats2.equals("from sideline"))) {
						shotWidesPlayOpp++;
					}
				} else if (stats1.equals("out for 45/65")) {
					// increment counter in review page
					shot45Opp++;
					if ((!stats2.equals("from free"))
							&& (!stats2.equals("from 45/65"))
							&& (!stats2.equals("from penalty"))
							&& (!stats2.equals("from sideline"))) {
						shot45PlayOpp++;
					}
				} else if (stats1.equals("saved/short")) {
					// increment counter in review page
					shotSavedOpp++;
					if ((!stats2.equals("from free"))
							&& (!stats2.equals("from 45/65"))
							&& (!stats2.equals("from penalty"))
							&& (!stats2.equals("from sideline"))) {
						shotSavedPlayOpp++;
					}
				} else if (stats1.equals("off posts")) {
					// increment counter in review page
					shotPostsOpp++;
					if ((!stats2.equals("from free"))
							&& (!stats2.equals("from 45/65"))
							&& (!stats2.equals("from penalty"))
							&& (!stats2.equals("from sideline"))) {
						shotPostsPlayOpp++;
					}
				} else if (stats1.equals("free/pen conceded")) {
					// increment counter in review page
					freeConcededOpp++;
				} else if (stats1.equals("own puck/kick out won")) {
					// increment counter in review page
					puckWonCleanOpp++;
				} else if (stats1.equals("own puck/kick out lost")) {
					// increment counter in review page
					puckLostCleanOpp++;
				}
			} while (c1.moveToNext());
			c1.close();
		}
		tShotGoalsHome.setText(String.valueOf(shotGoalsHome));
		tShotPointsHome.setText(String.valueOf(shotPointsHome));
		tShotWidesHome.setText(String.valueOf(shotWidesHome));
		tShotSavedHome.setText(String.valueOf(shotSavedHome));
		tShotPostsHome.setText(String.valueOf(shotPostsHome));
		tShot45Home.setText(String.valueOf(shot45Home));
		tShotGoalsPlayHome.setText(String.valueOf(shotGoalsPlayHome));
		tShotPointsPlayHome.setText(String.valueOf(shotPointsPlayHome));
		tShotPointsPlayWidesHome.setText(String.valueOf(shotWidesPlayHome));
		tShotPointsPlay45Home.setText(String.valueOf(shot45PlayHome));
		tShotPointsPlaySavedHome.setText(String.valueOf(shotSavedPlayHome));
		tShotPointsPlayPostsHome.setText(String.valueOf(shotPostsPlayHome));
		tShotGoalsOpp.setText(String.valueOf(shotGoalsOpp));
		tShotPointsOpp.setText(String.valueOf(shotPointsOpp));
		tShotWidesOpp.setText(String.valueOf(shotWidesOpp));
		tShotSavedOpp.setText(String.valueOf(shotSavedOpp));
		tShotPostsOpp.setText(String.valueOf(shotPostsOpp));
		tShot45Opp.setText(String.valueOf(shot45Opp));
		tShotGoalsPlayOpp.setText(String.valueOf(shotGoalsPlayOpp));
		tShotPointsPlayOpp.setText(String.valueOf(shotPointsPlayOpp));
		tShotPointsPlayWidesOpp.setText(String.valueOf(shotWidesPlayOpp));
		tShotPointsPlay45Opp.setText(String.valueOf(shot45PlayOpp));
		tShotPointsPlaySavedOpp.setText(String.valueOf(shotSavedPlayOpp));
		tShotPointsPlayPostsOpp.setText(String.valueOf(shotPostsPlayOpp));

		tFreeConcededHome.setText(String.valueOf(freeConcededHome));
		tPuckWonCleanHome.setText(String.valueOf(puckWonCleanHome));
		tPuckLostCleanHome.setText(String.valueOf(puckLostCleanHome));
		tTotPuckOpp.setText(String.valueOf(totPOpp));
		tFreeConcededOpp.setText(String.valueOf(freeConcededOpp));
		tPuckWonCleanOpp.setText(String.valueOf(puckWonCleanOpp));
		tPuckLostCleanOpp.setText(String.valueOf(puckLostCleanOpp));
		tTotPuckHome.setText(String.valueOf(totPHome));

		updateShotsPerCent();
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
			Cursor c1 = getActivity().getContentResolver().query(uri,
					projection, "_id=?", args, null);
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
			Toast.makeText(getActivity(), "stats entry deleted",
					Toast.LENGTH_LONG).show();
			updateListView();
			String[] teamu = { teamTemp };
			String[] stats1u = { stats1Temp };
			String[] stats2u = { stats2Temp };
			String[] playeru = { playerTemp };
			String[] typeu = { typeTemp };

			((Startup) getActivity()).getFragmentScore().undo(teamu, stats1u,
					stats2u, playeru, typeu);
			fillData();
			return true;
		}
		return super.onContextItemSelected(item);
	}

	public void updateShotsPerCent() {
		tShotsTotalHome.setText("Total shots:");
		tShotsPlayHome.setText("Shot from play:");
		tShotsTotalOpp.setText("Total shots:");
		tShotsPlayOpp.setText("Shot from play:");

		int totalShotsHome, totalShotsOpp, shotsPlayHome, shotsPlayOpp;
		int shotsScoredHome, shotsScoredOpp, shotsScoredPlayHome, shotsScoredPlayOpp;
		String perCent;
		totalShotsHome = shotGoalsHome + shotPointsHome + shotWidesHome
				+ shotSavedHome + shotPostsHome + shot45Home;
		totalShotsOpp = shotGoalsOpp + shotPointsOpp + shotWidesOpp
				+ shotSavedOpp + shotPostsOpp + shot45Opp;
		shotsPlayHome = shotGoalsPlayHome + shotPointsPlayHome
				+ shotWidesPlayHome + shot45PlayHome + shotSavedPlayHome
				+ shotPostsPlayHome;
		shotsPlayOpp = shotGoalsPlayOpp + shotPointsPlayOpp + shotWidesPlayOpp
				+ shot45PlayOpp + shotSavedPlayOpp + shotPostsPlayOpp;
		shotsScoredHome = shotGoalsHome + shotPointsHome;
		shotsScoredOpp = shotGoalsOpp + shotPointsOpp;
		shotsScoredPlayHome = shotGoalsPlayHome + shotPointsPlayHome;
		shotsScoredPlayOpp = shotGoalsPlayOpp + shotPointsPlayOpp;

		if (totalShotsHome > 0) {
			perCent = Integer
					.toString((shotsScoredHome * 100) / totalShotsHome);
			tShotsTotalHome.setText("Total shots:" + totalShotsHome
					+ "  Scored:" + shotsScoredHome + " (" + perCent + "%)");
		}
		if (totalShotsOpp > 0) {
			perCent = Integer.toString((shotsScoredOpp * 100) / totalShotsOpp);
			tShotsTotalOpp.setText("Total shots:" + totalShotsOpp + "  Scored:"
					+ shotsScoredOpp + " (" + perCent + "%)");
		}
		if (shotsPlayHome > 0) {
			perCent = Integer.toString((shotsScoredPlayHome * 100)
					/ shotsPlayHome);
			tShotsPlayHome
					.setText("Shot from play:" + shotsPlayHome + "  Scored:"
							+ shotsScoredPlayHome + " (" + perCent + "%)");
		}
		if (shotsPlayOpp > 0) {
			perCent = Integer.toString((shotsScoredPlayOpp * 100)
					/ shotsPlayOpp);
			tShotsPlayOpp.setText("Shot from play:" + shotsPlayOpp
					+ "  Scored:" + shotsScoredPlayOpp + " (" + perCent + "%)");
		}
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
		tShotGoalsHome.setText("0");
		tShotPointsHome.setText("0");
		tShotWidesHome.setText("0");
		tShotSavedHome.setText("0");
		tShotPostsHome.setText("0");
		tShot45Home.setText("0");
		tShotGoalsPlayHome.setText("0");
		tShotPointsPlayHome.setText("0");
		tShotPointsPlayWidesHome.setText("0");
		tShotPointsPlay45Home.setText("0");
		tShotPointsPlaySavedHome.setText("0");
		tShotPointsPlayPostsHome.setText("0");

		tShotGoalsOpp.setText("0");
		tShotPointsOpp.setText("0");
		tShotWidesOpp.setText("0");
		tShotSavedOpp.setText("0");
		tShotPostsOpp.setText("0");
		tShot45Opp.setText("0");
		tShotGoalsPlayOpp.setText("0");
		tShotPointsPlayOpp.setText("0");
		tShotPointsPlayWidesOpp.setText("0");
		tShotPointsPlay45Opp.setText("0");
		tShotPointsPlaySavedOpp.setText("0");
		tShotPointsPlayPostsOpp.setText("0");

		tFreeConcededHome.setText("0");
		tFreeConcededOpp.setText("0");

		tPuckWonCleanHome.setText("0");
		tPuckLostCleanHome.setText("0");
		tPuckWonCleanOpp.setText("0");
		tPuckLostCleanOpp.setText("0");
		tTotPuckHome.setText("0");
		tTotPuckOpp.setText("0");
		tCardHome.setText("");
		tCardOpp.setText("");

		tShotsTotalHome.setText("Total shots:");
		tShotsPlayHome.setText("Shot from play:");
		tShotsTotalOpp.setText("Total shots:");
		tShotsPlayOpp.setText("Shot from play:");
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
		String strTeam = "", strCard = "";
		// get home team first then opposition
		String[] args = { "t", "%card%" };
		Cursor c1 = getActivity().getContentResolver().query(allTitles, null,
				"type=? AND stats2 LIKE ? ", args, null);
		if (c1.getCount() > 0) {
			c1.moveToFirst();
			do {
				strTeam = c1.getString(c1
						.getColumnIndexOrThrow(TeamContentProvider.STATSTEAM));
				strCard = c1.getString(c1
						.getColumnIndexOrThrow(TeamContentProvider.STATS2));

				if (strCard.indexOf("red card") >= 0) {
					if (strTeam.indexOf(tOwnTeam.getText().toString()) >= 0) {
						redHome++;
					} else if (strTeam.indexOf(tOppTeam.getText().toString()) >= 0) {
						redOpp++;
					}
				}
				if (strCard.indexOf("yellow card") >= 0) {
					if (strTeam.indexOf(tOwnTeam.getText().toString()) >= 0) {
						yellowHome++;
					} else if (strTeam.indexOf(tOppTeam.getText().toString()) >= 0) {
						yellowOpp++;
					}
				}
				if (strCard.indexOf("black card") >= 0) {
					if (strTeam.indexOf(tOwnTeam.getText().toString()) >= 0) {
						blackHome++;
					} else if (strTeam.indexOf(tOppTeam.getText().toString()) >= 0) {
						blackOpp++;
					}
				}
			} while (c1.moveToNext());
			c1.close();
		}

		String[] args2 = { "u", " substitution " };
		c1 = getActivity().getContentResolver().query(allTitles, null,
				"type=? AND blood=?", args2, null);
		if (c1.getCount() > 0) {
			c1.moveToFirst();
			do {
				strTeam = c1.getString(c1
						.getColumnIndexOrThrow(TeamContentProvider.STATSTEAM));
				if (strTeam.indexOf(tOwnTeam.getText().toString()) >= 0) {
					subH++;
				} else if (strTeam.indexOf(tOppTeam.getText().toString()) >= 0) {
					subO++;
				}
			} while (c1.moveToNext());
			c1.close();
		}

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

	OnClickListener listEvents = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent events = new Intent(getActivity(), EventsListActivity.class);
			startActivityForResult(events, 1);

		};
	};

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {
			// A contact was picked. Here we will just display it
			// to the user.
			if (data != null && data.hasExtra("team")) {
				String[] teamu = data.getStringArrayExtra("team");
				String[] stats1u = data.getStringArrayExtra("stats1");
				String[] stats2u = data.getStringArrayExtra("stats2");
				String[] playeru = data.getStringArrayExtra("player");
				String[] typeu = data.getStringArrayExtra("type");					
				((Startup) getActivity()).getFragmentScore().undo(teamu,
						stats1u, stats2u, playeru, typeu);
			}
		}
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
			sb.append(shotWidesHome + " total wides. " + shotWidesPlayHome
					+ " wides from play\n");
			sb.append(shot45Home + " total out for 45/65. " + shot45PlayHome
					+ " out for 45/65 from play\n");
			sb.append(shotSavedHome + " total saved/short. "
					+ shotSavedPlayHome + " saved/short from play \n");
			sb.append(shotPostsHome + " total off posts. " + shotPostsPlayHome
					+ " off posts from play\n");
			sb.append(tShotsTotalHome.getText().toString() + "\n");
			sb.append(tShotsPlayHome.getText().toString() + "\n");
			sb.append("frees conceded: " + freeConcededHome + "\n");
			sb.append("Total puck/kick outs: " + totPHome + "\n");
			sb.append("own puck/kick outs won: " + puckWonCleanHome + "\n");
			sb.append("own puck/kick outs lost: " + puckLostCleanHome + "\n");
			sb.append(cardHome + subHome + "\n");

			sb.append("\nTeam 2: " + tOppTeam.getText() + "\n");
			sb.append(shotGoalsOpp + " Goals,  " + shotPointsOpp
					+ " Points.  Total:" + tOppTotal.getText() + "\n");
			sb.append(shotGoalsPlayOpp + " goals from play  "
					+ shotPointsPlayOpp + " points from play \n");
			sb.append(shotWidesOpp + " total wides. " + shotWidesPlayOpp
					+ " wides from play\n");
			sb.append(shot45Opp + " total out for 45/65. " + shot45PlayOpp
					+ " out for 45/65 from play\n");
			sb.append(shotSavedOpp + " total saved/short. " + shotSavedPlayOpp
					+ " saved/short from play \n");
			sb.append(shotPostsOpp + " total off posts. " + shotPostsPlayOpp
					+ " off posts from play\n");
			sb.append(tShotsTotalOpp.getText().toString() + "\n");
			sb.append(tShotsPlayOpp.getText().toString() + "\n");
			sb.append("frees conceded: " + freeConcededOpp + "\n");
			sb.append("Total puck/kick outs: " + totPOpp + "\n");
			sb.append("own puck/kick outs won: " + puckWonCleanOpp + "\n");
			sb.append("own puck/kick outs lost: " + puckLostCleanOpp + "\n");
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
					TeamContentProvider.SCORESMISS,
					TeamContentProvider.SCORESMISSFREE };

			// create array to map these fields to
			int[] to = new int[] { R.id.text1, R.id.text3, R.id.text4,
					R.id.text5, R.id.text6, R.id.text7, R.id.text8 };

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

			sb.append("player  **  Total Goals / Points  **  Goals/Points from placed balls  **"
					+ "  total wides  **  wides from placed ball\n\n");
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
							+ "  **  "
							+ c1.getString(c1
									.getColumnIndexOrThrow(TeamContentProvider.SCORESMISSFREE))
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
							+ "  **  "
							+ c2.getString(c2
									.getColumnIndexOrThrow(TeamContentProvider.SCORESMISSFREE))
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
			File imageFile = new File(mPath, "GAAScoresStats.jpg");
			Uri uri1 = Uri.fromFile(imageFile);

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

			Bitmap bitmapScorers = ((Startup) getActivity())
					.getFragmentScorers().createBitmap();
			fout = null;
			File imageFile2 = new File(mPath, "GAAScoresStatsScorers.jpg");
			Uri uri2 = Uri.fromFile(imageFile2);

			try {
				mPath.mkdirs();
				fout = new FileOutputStream(imageFile2);
				bitmapScorers.compress(Bitmap.CompressFormat.JPEG, 90, fout);
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
			uris.add(uri1);
			uris.add(uri2);
			File dir = new File(Environment.getExternalStorageDirectory(),
					"gaa_app_sysfiles");
			if (!dir.exists()) {
				dir.mkdirs();
			}
			File files[] = dir.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].getName().contains("GAAScoresStatsTeamSelection_")) {
					uris.add(Uri.fromFile(files[i]));
				}
			}
			for (int i = 0; i < files.length; i++) {
				if (files[i].getName().contains("GAAScoresStatsTeam_")) {
					uris.add(Uri.fromFile(files[i]));
				}
			}

			emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
			startActivity(Intent.createChooser(emailIntent, "Share Using:"));
		}
	};

	public Bitmap createBitmap() {
		// Create Bitmap to display team selection
		Bitmap bitmap = Bitmap.createBitmap(700, 490, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawColor(Color.rgb(204, 255, 204));
		Paint paint = new Paint();
		paint.setColor(Color.rgb(255, 255, 219));
		canvas.drawRect(350, 0, 700, 490, paint);
		paint.setColor(Color.BLACK);
		canvas.drawLine(350, 0, 350, 430, paint);
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
		canvas.drawText("No.", 225, 75, paint);
		paint.setTextSize(18);
		canvas.drawText("from play", 270, 75, paint);
		paint.setTextSize(22);
		paint.setColor(Color.BLACK);
		canvas.drawText("Goals", 5, 100, paint);
		canvas.drawText(homeGoals + " ", 235, 100, paint);
		canvas.drawText(shotGoalsPlayHome + " ", 305, 100, paint);

		canvas.drawText("Points", 5, 125, paint);
		canvas.drawText(homePoints + " ", 235, 125, paint);
		canvas.drawText(shotPointsPlayHome + " ", 305, 125, paint);

		canvas.drawText("Wides", 5, 150, paint);
		canvas.drawText(shotWidesHome + " ", 235, 150, paint);
		canvas.drawText(shotWidesPlayHome + " ", 305, 150, paint);

		canvas.drawText("Out for 45/65", 5, 175, paint);
		canvas.drawText(shot45Home + " ", 235, 175, paint);
		canvas.drawText(shot45PlayHome + " ", 305, 175, paint);

		canvas.drawText("Saved/Short", 5, 200, paint);
		canvas.drawText(shotSavedHome + " ", 235, 200, paint);
		canvas.drawText(shotSavedPlayHome + " ", 305, 200, paint);

		canvas.drawText("Off Posts", 5, 225, paint);
		canvas.drawText(shotPostsHome + " ", 235, 225, paint);
		canvas.drawText(shotPostsPlayHome + " ", 305, 225, paint);

		paint.setTextSize(20);
		canvas.drawText(tShotsTotalHome.getText().toString(), 5, 250, paint);
		canvas.drawText(tShotsPlayHome.getText().toString(), 5, 275, paint);

		paint.setTextSize(22);
		canvas.drawText("Frees Conceded", 5, 300, paint);
		canvas.drawText(freeConcededHome + " ", 235, 300, paint);
		paint.setTextSize(20);
		canvas.drawText("Total Puck/Kick Outs", 5, 325, paint);
		canvas.drawText(totPHome + " ", 235, 325, paint);

		canvas.drawText("Own Puck/Kick Out Won", 5, 350, paint);
		canvas.drawText(puckWonCleanHome + " ", 235, 350, paint);

		canvas.drawText("Own Puck/Kick Out Lost", 5, 375, paint);
		canvas.drawText(puckLostCleanHome + " ", 235, 375, paint);
		paint.setTextSize(22);
		canvas.drawText(cardHome + subHome, 5, 400, paint);

		paint.setTextAlign(Align.CENTER);
		canvas.drawText(tOppTeam.getText().toString(), 525, 25, paint);
		canvas.drawText(oppGoals + "-" + oppPoints + " (" + oppTotal + ")",
				525, 50, paint);
		paint.setTextAlign(Align.LEFT);
		paint.setColor(Color.RED);
		canvas.drawText("No.", 570, 75, paint);
		paint.setTextSize(18);
		canvas.drawText("from play", 620, 75, paint);
		paint.setTextSize(22);
		paint.setColor(Color.BLACK);
		canvas.drawText("Goals", 355, 100, paint);
		canvas.drawText(oppGoals + " ", 580, 100, paint);
		canvas.drawText(shotGoalsPlayOpp + " ", 655, 100, paint);

		canvas.drawText("Points", 355, 125, paint);
		canvas.drawText(oppPoints + " ", 580, 125, paint);
		canvas.drawText(shotPointsPlayOpp + " ", 655, 125, paint);

		canvas.drawText("Wides", 355, 150, paint);
		canvas.drawText(shotWidesOpp + " ", 580, 150, paint);
		canvas.drawText(shotWidesPlayOpp + " ", 655, 150, paint);

		canvas.drawText("O" + "ut for 45/65", 355, 175, paint);
		canvas.drawText(shot45Opp + " ", 580, 175, paint);
		canvas.drawText(shot45PlayOpp + " ", 655, 175, paint);

		canvas.drawText("Saved/Short", 355, 200, paint);
		canvas.drawText(shotSavedOpp + " ", 580, 200, paint);
		canvas.drawText(shotSavedPlayOpp + " ", 655, 200, paint);

		canvas.drawText("Off Posts", 355, 225, paint);
		canvas.drawText(shotPostsOpp + " ", 580, 225, paint);
		canvas.drawText(shotPostsPlayOpp + " ", 655, 225, paint);

		paint.setTextSize(20);
		canvas.drawText(tShotsTotalOpp.getText().toString(), 355, 250, paint);
		canvas.drawText(tShotsPlayOpp.getText().toString(), 355, 275, paint);
		paint.setTextSize(22);

		canvas.drawText("Frees Conceded", 355, 300, paint);
		canvas.drawText(freeConcededOpp + " ", 580, 300, paint);
		paint.setTextSize(20);
		canvas.drawText("Total Puck/Kick Outs", 355, 325, paint);
		canvas.drawText(totPOpp + " ", 580, 325, paint);

		canvas.drawText("Own Puck/Kick Out Won", 355, 350, paint);
		canvas.drawText(puckWonCleanOpp + " ", 580, 350, paint);

		canvas.drawText("Own Puck/Kick Out Lost", 355, 375, paint);
		canvas.drawText(puckLostCleanOpp + " ", 580, 375, paint);
		paint.setTextSize(22);
		canvas.drawText(cardOpp + subOpp, 355, 400, paint);

		paint.setColor(Color.GRAY);
		paint.setTextSize(16);
		paint.setTextAlign(Align.CENTER);
		canvas.drawText("GAA Scores Stats Plus - Android App.", 350, 455, paint);
		canvas.drawText("Available free from Google Play Store", 350, 475,
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
