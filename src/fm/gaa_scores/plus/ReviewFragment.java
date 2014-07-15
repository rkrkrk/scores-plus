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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ReviewFragment extends Fragment {
	private int homeGoals, homePoints, homeTotal, oppGoals, oppPoints,
			oppTotal;
	private TextView tHomeGoals, tHomePoints, tHomeTotal, tOppGoals,
			tOppPoints;
	private TextView tShotGoalsHome;
	private TextView tShotWidesHome;
	private TextView tShotPointsHome;
	private TextView tShotSavedHome, tShotShortHome;
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
	private TextView tPuckWonCleanHome, tPuckWonCleanHomePerCent;
	private TextView tPuckLostCleanHome, tPuckLostCleanHomePerCent;
	private TextView tPuckWonCleanOpp, tPuckWonCleanOppPerCent;
	private TextView tPuckLostCleanOpp, tPuckLostCleanOppPerCent;
	private TextView tShotsFreeHome, tShotsFreeOpp;
	private TextView tOwnTeam, tOppTeam;
	private TextView tCardHome, tCardOpp;
	private TextView tShotPointsPlayWidesHome, tShotPointsPlayWidesOpp;
	private TextView tShotPointsPlay45Home, tShotPointsPlay45Opp;
	private TextView tShotPointsPlaySavedHome, tShotPointsPlaySavedOpp,
			tShotPointsPlayShortHome, tShotPointsPlayShortOpp;
	private TextView tShotPointsPlayPostsHome, tShotPointsPlayPostsOpp;
	private TextView[] tVFreesHome = new TextView[7];
	private TextView[] tVFreesOpp = new TextView[7];
	private Button bSendAll, bTweetAll, bEvents;
	private int red = 0, yellow = 0, sub = 0;

	private int shotGoalsHome = 0, shotPointsHome = 0;
	private int shotGoalsPlayHome = 0, shotPointsPlayHome = 0;
	private int shotGoalsPlayOpp = 0, shotPointsPlayOpp = 0;
	private int shotWidesPlayHome = 0, shotWidesPlayOpp = 0;
	private int shot45PlayHome = 0, shot45PlayOpp = 0;
	private int shotSavedPlayHome = 0, shotSavedPlayOpp = 0;
	private int shotShortPlayHome = 0, shotShortPlayOpp = 0;
	private int shotPostsPlayHome = 0, shotPostsPlayOpp = 0;
	private int shotWidesHome = 0, shotSavedHome = 0, shotShortHome = 0,
			shotPostsHome = 0;
	private int freeConcededHome = 0;
	private int freeConcededOpp = 0;
	private int shot45Home = 0, shot45Opp = 0;
	private int totPHome = 0, totPOpp = 0;
	private int puckWonCleanHome = 0, puckWonHomePerCent = 0;
	private int puckLostCleanHome = 0, puckLostHomePerCent = 0;
	private int puckWonCleanOpp = 0, puckWonOppPerCent = 0;
	private int puckLostCleanOpp = 0, puckLostOppPerCent = 0;
	private int shotsFreeHome = 0, shotsFreeOpp = 0, shotsScoredFreeHome,
			shotsScoredFreeOpp;
	private int[] freeHome = new int[7];
	private int[] freeOpp = new int[7];
	private TextView tOppTotal;
	private TextView tShotGoalsOpp, tShotGoalsPlayOpp;
	private TextView tShotWidesOpp;
	private TextView tShotPointsOpp;
	private TextView tShotSavedOpp, tShotShortOpp;
	private TextView tShotPostsOpp;
	private TextView tShot45Opp;
	private TextView tTotPuckOpp;
	private int shotGoalsOpp = 0, shotPointsOpp = 0;
	private int shotWidesOpp = 0, shotSavedOpp = 0, shotShortOpp = 0,
			shotPostsOpp = 0;
	private ListView listViewStats;
	private String cardHome = "", subHome = "";
	private String cardOpp = "", subOpp = "";

	@Override
	// start main method to display screen
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.review_layout_new, container, false);
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
		tShotShortHome = (TextView) v.findViewById(R.id.tVwShotsShortNo);
		tShotPostsHome = (TextView) v.findViewById(R.id.tVwShotsPostsNo);

		tShotGoalsPlayHome = (TextView) v.findViewById(R.id.tVGoalsHomePlay);
		tShotPointsPlayHome = (TextView) v.findViewById(R.id.tVPointsHomePlay);
		tShotPointsPlayWidesHome = (TextView) v
				.findViewById(R.id.tVWidesHomePlay);
		tShotPointsPlay45Home = (TextView) v.findViewById(R.id.tV45HomePlay);
		tShotPointsPlaySavedHome = (TextView) v
				.findViewById(R.id.tVSavedHomePlay);
		tShotPointsPlayShortHome = (TextView) v
				.findViewById(R.id.tVShortHomePlay);
		tShotPointsPlayPostsHome = (TextView) v
				.findViewById(R.id.tVPostsHomePlay);

		tShotGoalsPlayOpp = (TextView) v.findViewById(R.id.tVGoalsOppPlay);
		tShotPointsPlayOpp = (TextView) v.findViewById(R.id.tVPointsOppPlay);
		tShotPointsPlayWidesOpp = (TextView) v
				.findViewById(R.id.tVWidesOppPlay);
		tShotPointsPlay45Opp = (TextView) v.findViewById(R.id.tV45OppPlay);
		tShotPointsPlaySavedOpp = (TextView) v
				.findViewById(R.id.tVSavedOppPlay);
		tShotPointsPlayShortOpp = (TextView) v
				.findViewById(R.id.tVShortOppPlay);
		tShotPointsPlayPostsOpp = (TextView) v
				.findViewById(R.id.tVPostsOppPlay);

		tShotGoalsOpp = (TextView) v.findViewById(R.id.tVwShotsGoalsOppNo);
		tShotPointsOpp = (TextView) v.findViewById(R.id.tVwShotsPointsOppNo);
		tShotWidesOpp = (TextView) v.findViewById(R.id.tVwShotsWidesOppNo);
		tShot45Opp = (TextView) v.findViewById(R.id.tVwOpp45);
		tShotSavedOpp = (TextView) v.findViewById(R.id.tVwShotsSavedOppNo);
		tShotShortOpp = (TextView) v.findViewById(R.id.tVwShotsShortOppNo);
		tShotPostsOpp = (TextView) v.findViewById(R.id.tVwShotsPostsOppNo);

		tTotPuckHome = (TextView) v.findViewById(R.id.tVwHomeTotPuck);
		tTotPuckOpp = (TextView) v.findViewById(R.id.tVwOppTotPuck);

		tShotsTotalHome = (TextView) v.findViewById(R.id.tTotalShotsHome);
		tShotsPlayHome = (TextView) v.findViewById(R.id.tShotsPlayHome);
		tShotsTotalOpp = (TextView) v.findViewById(R.id.tTotalShotsOpp);
		tShotsPlayOpp = (TextView) v.findViewById(R.id.tShotsPlayOpp);
		tShotsFreeHome = (TextView) v.findViewById(R.id.tShotsFreeHome);
		tShotsFreeOpp = (TextView) v.findViewById(R.id.tShotsFreeOpp);

		tCardHome = (TextView) v.findViewById(R.id.cardsHome);
		tCardOpp = (TextView) v.findViewById(R.id.cardsOpp);

		// Set up output for frees
		tFreeConcededHome = (TextView) v.findViewById(R.id.tVwFreeWonHome);
		tFreeConcededOpp = (TextView) v.findViewById(R.id.tVwFreeWonOpp);

		tVFreesHome[0] = (TextView) v.findViewById(R.id.tVHomeGoalsFree);
		tVFreesHome[1] = (TextView) v.findViewById(R.id.tVHomePointsFree);
		tVFreesHome[2] = (TextView) v.findViewById(R.id.tVHomeWidesFree);
		tVFreesHome[3] = (TextView) v.findViewById(R.id.tVHomeOutFree);
		tVFreesHome[4] = (TextView) v.findViewById(R.id.tVHomePostsFree);
		tVFreesHome[5] = (TextView) v.findViewById(R.id.tVHomeSavedFree);
		tVFreesHome[6] = (TextView) v.findViewById(R.id.tVHomeShortFree);
		tVFreesOpp[0] = (TextView) v.findViewById(R.id.tVOppGoalsFree);
		tVFreesOpp[1] = (TextView) v.findViewById(R.id.tVOppPointsFree);
		tVFreesOpp[2] = (TextView) v.findViewById(R.id.tVOppWidesFree);
		tVFreesOpp[3] = (TextView) v.findViewById(R.id.tVOppOutFree);
		tVFreesOpp[4] = (TextView) v.findViewById(R.id.tVOppPostsFree);
		tVFreesOpp[5] = (TextView) v.findViewById(R.id.tVOppSavedFree);
		tVFreesOpp[6] = (TextView) v.findViewById(R.id.tVOppShortFree);

		// Set up output for puckouts
		tPuckWonCleanHome = (TextView) v.findViewById(R.id.tVwPuckWonCleanHome);
		tPuckLostCleanHome = (TextView) v
				.findViewById(R.id.tVPuckLostCleanHome);
		tPuckWonCleanOpp = (TextView) v.findViewById(R.id.tVwPuckWonCleanOpp);
		tPuckLostCleanOpp = (TextView) v.findViewById(R.id.tVPuckLostCleanOpp);
		tPuckWonCleanHomePerCent = (TextView) v
				.findViewById(R.id.tVHomePuckOutWonPerCent);
		tPuckLostCleanHomePerCent = (TextView) v
				.findViewById(R.id.tVHomePuckOutLostPerCent);
		tPuckWonCleanOppPerCent = (TextView) v
				.findViewById(R.id.tVOppPuckOutWonPerCent);
		tPuckLostCleanOppPerCent = (TextView) v
				.findViewById(R.id.tVOppPuckOutLostPerCent);

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

		updateCardsSubs();
		updateShotsPerCent();

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
		shotShortHome = 0;
		shotSavedPlayHome = 0;
		shotShortPlayHome = 0;
		shotPostsHome = 0;
		shotPostsPlayHome = 0;
		freeConcededHome = 0;
		puckWonCleanHome = 0;
		puckLostCleanHome = 0;
		puckWonHomePerCent = 0;
		puckLostHomePerCent = 0;
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
		shotShortOpp = 0;
		shotSavedPlayOpp = 0;
		shotShortPlayOpp = 0;
		shotPostsOpp = 0;
		shotPostsPlayOpp = 0;
		freeConcededOpp = 0;
		puckWonCleanOpp = 0;
		puckLostCleanOpp = 0;
		puckWonOppPerCent = 0;
		puckLostOppPerCent = 0;
		for (int i = 0; i < freeHome.length; i++) {
			freeHome[i] = 0;
			freeOpp[i] = 0;
		}

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
					if ((stats2.equals("from free"))
							|| (stats2.equals("from 45/65"))
							|| (stats2.equals("from penalty"))) {
						freeHome[0] = freeHome[0] + 1;
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
					if ((stats2.equals("from free"))
							|| (stats2.equals("from 45/65"))
							|| (stats2.equals("from penalty"))) {
						freeHome[1] = freeHome[1] + 1;
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
					if ((stats2.equals("from free"))
							|| (stats2.equals("from 45/65"))
							|| (stats2.equals("from penalty"))) {
						freeHome[2] = freeHome[2] + 1;
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
					if ((stats2.equals("from free"))
							|| (stats2.equals("from 45/65"))
							|| (stats2.equals("from penalty"))) {
						freeHome[3] = freeHome[3] + 1;
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
					if ((stats2.equals("from free"))
							|| (stats2.equals("from 45/65"))
							|| (stats2.equals("from penalty"))) {
						freeHome[4] = freeHome[4] + 1;
					}
				} else if (stats1.equals("saved")) {
					// increment counter in review page
					shotSavedHome++;
					if ((!stats2.equals("from free"))
							&& (!stats2.equals("from 45/65"))
							&& (!stats2.equals("from penalty"))
							&& (!stats2.equals("from sideline"))) {
						shotSavedPlayHome++;
					}
					if ((stats2.equals("from free"))
							|| (stats2.equals("from 45/65"))
							|| (stats2.equals("from penalty"))) {
						freeHome[5] = freeHome[5] + 1;
					}
				} else if (stats1.equals("short")) {
					// increment counter in review page
					shotShortHome++;
					if ((!stats2.equals("from free"))
							&& (!stats2.equals("from 45/65"))
							&& (!stats2.equals("from penalty"))
							&& (!stats2.equals("from sideline"))) {
						shotShortPlayHome++;
					}
					if ((stats2.equals("from free"))
							|| (stats2.equals("from 45/65"))
							|| (stats2.equals("from penalty"))) {
						freeHome[6] = freeHome[6] + 1;
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
					if ((stats2.equals("from free"))
							|| (stats2.equals("from 45/65"))
							|| (stats2.equals("from penalty"))) {
						freeOpp[0] = freeOpp[0] + 1;
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
					if ((stats2.equals("from free"))
							|| (stats2.equals("from 45/65"))
							|| (stats2.equals("from penalty"))) {
						freeOpp[1] = freeOpp[1] + 1;
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
					if ((stats2.equals("from free"))
							|| (stats2.equals("from 45/65"))
							|| (stats2.equals("from penalty"))) {
						freeOpp[2] = freeOpp[2] + 1;
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
					if ((stats2.equals("from free"))
							|| (stats2.equals("from 45/65"))
							|| (stats2.equals("from penalty"))) {
						freeOpp[3] = freeOpp[3] + 1;
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
					if ((stats2.equals("from free"))
							|| (stats2.equals("from 45/65"))
							|| (stats2.equals("from penalty"))) {
						freeOpp[4] = freeOpp[4] + 1;
					}
				} else if (stats1.equals("saved")) {
					// increment counter in review page
					shotSavedOpp++;
					if ((!stats2.equals("from free"))
							&& (!stats2.equals("from 45/65"))
							&& (!stats2.equals("from penalty"))
							&& (!stats2.equals("from sideline"))) {
						shotSavedPlayOpp++;
					}
					if ((stats2.equals("from free"))
							|| (stats2.equals("from 45/65"))
							|| (stats2.equals("from penalty"))) {
						freeOpp[5] = freeOpp[5] + 1;
					}
				} else if (stats1.equals("short")) {
					// increment counter in review page
					shotShortOpp++;
					if ((!stats2.equals("from free"))
							&& (!stats2.equals("from 45/65"))
							&& (!stats2.equals("from penalty"))
							&& (!stats2.equals("from sideline"))) {
						shotShortPlayOpp++;
					}
					if ((stats2.equals("from free"))
							|| (stats2.equals("from 45/65"))
							|| (stats2.equals("from penalty"))) {
						freeOpp[6] = freeOpp[6] + 1;
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
		tShotShortHome.setText(String.valueOf(shotShortHome));
		tShotPostsHome.setText(String.valueOf(shotPostsHome));
		tShot45Home.setText(String.valueOf(shot45Home));
		tShotGoalsPlayHome.setText(String.valueOf(shotGoalsPlayHome));
		tShotPointsPlayHome.setText(String.valueOf(shotPointsPlayHome));
		tShotPointsPlayWidesHome.setText(String.valueOf(shotWidesPlayHome));
		tShotPointsPlay45Home.setText(String.valueOf(shot45PlayHome));
		tShotPointsPlaySavedHome.setText(String.valueOf(shotSavedPlayHome));
		tShotPointsPlayShortHome.setText(String.valueOf(shotShortPlayHome));
		tShotPointsPlayPostsHome.setText(String.valueOf(shotPostsPlayHome));
		tShotGoalsOpp.setText(String.valueOf(shotGoalsOpp));
		tShotPointsOpp.setText(String.valueOf(shotPointsOpp));
		tShotWidesOpp.setText(String.valueOf(shotWidesOpp));
		tShotSavedOpp.setText(String.valueOf(shotSavedOpp));
		tShotShortOpp.setText(String.valueOf(shotShortOpp));
		tShotPostsOpp.setText(String.valueOf(shotPostsOpp));
		tShot45Opp.setText(String.valueOf(shot45Opp));
		tShotGoalsPlayOpp.setText(String.valueOf(shotGoalsPlayOpp));
		tShotPointsPlayOpp.setText(String.valueOf(shotPointsPlayOpp));
		tShotPointsPlayWidesOpp.setText(String.valueOf(shotWidesPlayOpp));
		tShotPointsPlay45Opp.setText(String.valueOf(shot45PlayOpp));
		tShotPointsPlaySavedOpp.setText(String.valueOf(shotSavedPlayOpp));
		tShotPointsPlayShortOpp.setText(String.valueOf(shotShortPlayOpp));
		tShotPointsPlayPostsOpp.setText(String.valueOf(shotPostsPlayOpp));

		tFreeConcededHome.setText(String.valueOf(freeConcededHome));
		tPuckWonCleanHome.setText(String.valueOf(puckWonCleanHome));
		tPuckLostCleanHome.setText(String.valueOf(puckLostCleanHome));
		tTotPuckOpp.setText(String.valueOf(totPOpp));
		tFreeConcededOpp.setText(String.valueOf(freeConcededOpp));
		tPuckWonCleanOpp.setText(String.valueOf(puckWonCleanOpp));
		tPuckLostCleanOpp.setText(String.valueOf(puckLostCleanOpp));
		tTotPuckHome.setText(String.valueOf(totPHome));
		for (int i = 0; i < 7; i++) {
			tVFreesHome[i].setText(String.valueOf(freeHome[i]));
			tVFreesOpp[i].setText(String.valueOf(freeOpp[i]));
		}

		updateShotsPerCent();
	}

	public void updateShotsPerCent() {
		tShotsTotalHome.setText("TOTAL SHOTS:");
		tShotsPlayHome.setText("Shots from play:");
		tShotsTotalOpp.setText("TOTAL SHOTS:");
		tShotsPlayOpp.setText("Shots from play:");
		tShotsFreeHome.setText("Shots from free/pen/45/65:");
		tShotsFreeOpp.setText("Shots from free/pen/45/65:");

		int totalShotsHome, totalShotsOpp, shotsPlayHome, shotsPlayOpp;
		shotsFreeHome = 0;
		shotsFreeOpp = 0;
		int shotsScoredHome, shotsScoredOpp, shotsScoredPlayHome, shotsScoredPlayOpp;
		String perCent;
		totalShotsHome = shotGoalsHome + shotPointsHome + shotWidesHome
				+ shotSavedHome + shotShortHome + shotPostsHome + shot45Home;
		totalShotsOpp = shotGoalsOpp + shotPointsOpp + shotWidesOpp
				+ shotSavedOpp + shotShortOpp + shotPostsOpp + shot45Opp;
		shotsPlayHome = shotGoalsPlayHome + shotPointsPlayHome
				+ shotWidesPlayHome + shot45PlayHome + shotSavedPlayHome
				+ shotShortPlayHome + shotPostsPlayHome;
		shotsPlayOpp = shotGoalsPlayOpp + shotPointsPlayOpp + shotWidesPlayOpp
				+ shot45PlayOpp + shotSavedPlayOpp + shotShortPlayOpp
				+ shotPostsPlayOpp;
		for (int i = 0; i < 7; i++) {
			shotsFreeHome = shotsFreeHome + freeHome[i];
			shotsFreeOpp = shotsFreeOpp + freeOpp[i];
		}
		shotsScoredHome = shotGoalsHome + shotPointsHome;
		shotsScoredOpp = shotGoalsOpp + shotPointsOpp;
		shotsScoredPlayHome = shotGoalsPlayHome + shotPointsPlayHome;
		shotsScoredPlayOpp = shotGoalsPlayOpp + shotPointsPlayOpp;
		shotsScoredFreeHome = freeHome[0] + freeHome[1];
		shotsScoredFreeOpp = freeOpp[0] + freeOpp[1];

		if ((puckWonCleanHome + puckLostCleanHome) > 0) {
			puckWonHomePerCent = puckWonCleanHome * 100
					/ (puckWonCleanHome + puckLostCleanHome);
			puckLostHomePerCent = puckLostCleanHome * 100
					/ (puckWonCleanHome + puckLostCleanHome);
			tPuckWonCleanHomePerCent.setText(String.valueOf(puckWonHomePerCent)
					+ "%");
			tPuckLostCleanHomePerCent.setText(String
					.valueOf(puckLostHomePerCent) + "%");
		}
		if ((puckWonCleanOpp + puckLostCleanOpp) > 0) {
			puckWonOppPerCent = puckWonCleanOpp * 100
					/ (puckWonCleanOpp + puckLostCleanOpp);
			puckLostOppPerCent = puckLostCleanOpp * 100
					/ (puckWonCleanOpp + puckLostCleanOpp);
			tPuckWonCleanOppPerCent.setText(String.valueOf(puckWonOppPerCent)
					+ "%");
			tPuckLostCleanOppPerCent.setText(String.valueOf(puckLostOppPerCent)
					+ "%");
		}

		if (totalShotsHome > 0) {
			perCent = Integer
					.toString((shotsScoredHome * 100) / totalShotsHome);
			tShotsTotalHome.setText("TOTAL SHOTS:" + totalShotsHome
					+ "  Scored:" + shotsScoredHome + "\n(" + perCent + "%)");
		}
		if (totalShotsOpp > 0) {
			perCent = Integer.toString((shotsScoredOpp * 100) / totalShotsOpp);
			tShotsTotalOpp.setText("TOTAL SHOTS:" + totalShotsOpp + "  Scored:"
					+ shotsScoredOpp + "\n(" + perCent + "%)");
		}
		if (shotsPlayHome > 0) {
			perCent = Integer.toString((shotsScoredPlayHome * 100)
					/ shotsPlayHome);
			tShotsPlayHome
					.setText("Shots from play:" + shotsPlayHome + " Scored:"
							+ shotsScoredPlayHome + "\n(" + perCent + "%)");
		}
		if (shotsPlayOpp > 0) {
			perCent = Integer.toString((shotsScoredPlayOpp * 100)
					/ shotsPlayOpp);
			tShotsPlayOpp.setText("Shots from play:" + shotsPlayOpp
					+ " Scored:" + shotsScoredPlayOpp + "\n(" + perCent + "%)");
		}
		if (shotsFreeHome > 0) {
			perCent = Integer.toString((shotsScoredFreeHome * 100)
					/ shotsFreeHome);
			tShotsFreeHome.setText("Shots from free/pen/45/65:" + shotsFreeHome
					+ "  Scored:" + shotsScoredFreeHome + "  (" + perCent
					+ "%)");
		}
		if (shotsFreeOpp > 0) {
			perCent = Integer.toString((shotsScoredFreeOpp * 100)
					/ shotsFreeOpp);
			tShotsFreeOpp
					.setText("Shots from free/pen/45/65:" + shotsFreeOpp
							+ "  Scored:" + shotsScoredFreeOpp + "  ("
							+ perCent + "%)");
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
		shotShortHome = 0;
		shotSavedPlayHome = 0;
		shotShortPlayHome = 0;
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
		shotShortOpp = 0;
		shotSavedPlayOpp = 0;
		shotShortPlayOpp = 0;
		shotPostsOpp = 0;
		shotPostsPlayOpp = 0;
		freeConcededOpp = 0;
		puckWonCleanOpp = 0;
		puckLostCleanOpp = 0;
		puckWonOppPerCent = 0;
		puckLostOppPerCent = 0;
		puckWonHomePerCent = 0;
		puckLostHomePerCent = 0;
		for (int i = 0; i < freeHome.length; i++) {
			freeHome[i] = 0;
			freeOpp[i] = 0;
		}
		tShotGoalsHome.setText("0");
		tShotPointsHome.setText("0");
		tShotWidesHome.setText("0");
		tShotSavedHome.setText("0");
		tShotShortHome.setText("0");
		tShotPostsHome.setText("0");
		tShot45Home.setText("0");
		tShotGoalsPlayHome.setText("0");
		tShotPointsPlayHome.setText("0");
		tShotPointsPlayWidesHome.setText("0");
		tShotPointsPlay45Home.setText("0");
		tShotPointsPlaySavedHome.setText("0");
		tShotPointsPlayShortHome.setText("0");
		tShotPointsPlayPostsHome.setText("0");
		for (int i = 0; i < 7; i++) {
			tVFreesHome[i].setText("0");
			tVFreesOpp[i].setText("0");
		}

		tShotGoalsOpp.setText("0");
		tShotPointsOpp.setText("0");
		tShotWidesOpp.setText("0");
		tShotSavedOpp.setText("0");
		tShotShortOpp.setText("0");
		tShotPostsOpp.setText("0");
		tShot45Opp.setText("0");
		tShotGoalsPlayOpp.setText("0");
		tShotPointsPlayOpp.setText("0");
		tShotPointsPlayWidesOpp.setText("0");
		tShotPointsPlay45Opp.setText("0");
		tShotPointsPlaySavedOpp.setText("0");
		tShotPointsPlayShortOpp.setText("0");
		tShotPointsPlayPostsOpp.setText("0");

		tFreeConcededHome.setText("0");
		tFreeConcededOpp.setText("0");

		tPuckWonCleanHome.setText("0");
		tPuckLostCleanHome.setText("0");
		tPuckWonCleanOpp.setText("0");
		tPuckLostCleanOpp.setText("0");
		tPuckWonCleanHomePerCent.setText("--");
		tPuckLostCleanHomePerCent.setText("--");
		tPuckWonCleanOppPerCent.setText("--");
		tPuckLostCleanOppPerCent.setText("--");
		tTotPuckHome.setText("0");
		tTotPuckOpp.setText("0");
		tCardHome.setText("");
		tCardOpp.setText("");

		tShotsTotalHome.setText("TOTAL SHOTS:");
		tShotsPlayHome.setText("Shot from play:");
		tShotsTotalOpp.setText("TOTAL SHOTS:");
		tShotsPlayOpp.setText("Shot from play:");
		tShotsFreeHome.setText("Shots from free/pen/45/65:");
		tShotsFreeOpp.setText("Shots from free/pen/45/65:");
		updateCardsSubs();
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

		String[] args2 = { "u", "false" };
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
					+ redHome + "R  ";
		} else {
			cardHome = "";
		}
		if (redOpp > 0 || yellowOpp > 0 || blackOpp > 0) {
			cardOpp = "Cards: " + blackOpp + "B  " + yellowOpp + "Y  " + redOpp
					+ "R  ";
		} else {
			cardOpp = "";
		}
		if (subH > 0) {
			subHome = "\nSubs used: " + subH;
		} else {
			subHome = "";
		}
		if (subO > 0) {
			subOpp = "\nSubs used: " + subO;
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
			sb.append(shotGoalsPlayHome + " goals from play.  " + freeHome[0]
					+ " goals from frees/pen/45/65 \n");
			sb.append(shotPointsPlayHome + " points from play.  " + freeHome[1]
					+ " points from frees/pen/45/65 \n");
			sb.append(shotWidesHome + " total wides. " + shotWidesPlayHome
					+ " wides from play. " + freeHome[2]
					+ " wides from frees/pen/45/65 \n");
			sb.append(shot45Home + " total out for 45/65. " + shot45PlayHome
					+ " out for 45/65 from play. " + freeHome[3]
					+ " out for 45/65 from frees/pen/45/65 \n");
			sb.append(shotPostsHome + " total off posts. " + shotPostsPlayHome
					+ " off posts from play. " + freeHome[4]
					+ " off posts frees/pen/45/65 \n");
			sb.append(shotSavedHome + " total saved. " + shotSavedPlayHome
					+ " saved from play. " + freeHome[5]
					+ " saved from frees/pen/45/65 \n");
			sb.append(shotShortHome + " total short. " + shotShortPlayHome
					+ " short from play. " + freeHome[6]
					+ " short from frees/pen/45/65 \n");

			sb.append(tShotsTotalHome.getText().toString() + "\n");
			sb.append(tShotsPlayHome.getText().toString() + "\n");
			sb.append(tShotsFreeHome.getText().toString() + "\n");
			sb.append("Frees conceded: " + freeConcededHome + "\n");
			sb.append("Total puck/kick outs: " + totPHome + "\n");
			sb.append("own puck/kick outs won: " + puckWonCleanHome + "("
					+ puckWonHomePerCent + "%) \n");
			sb.append("own puck/kick outs lost: " + puckLostCleanHome + "("
					+ puckLostHomePerCent + "%) \n");
			sb.append(cardHome + subHome + "\n");

			sb.append("\nTeam 2: " + tOppTeam.getText() + "\n");
			sb.append(shotGoalsOpp + " Goals,  " + shotPointsOpp
					+ " Points.  Total:" + tOppTotal.getText() + "\n");
			sb.append(shotGoalsPlayOpp + " goals from play.  " + freeOpp[0]
					+ " goals from frees/pen/45/65 \n");
			sb.append(shotPointsPlayOpp + " points from play.  " + freeOpp[1]
					+ " points from frees/pen/45/65 \n");
			sb.append(shotWidesOpp + " total wides. " + shotWidesPlayOpp
					+ " wides from play. " + freeOpp[2]
					+ " wides from frees/pen/45/65 \n");
			sb.append(shot45Opp + " total out for 45/65. " + shot45PlayOpp
					+ " out for 45/65 from play. " + freeOpp[3]
					+ " out for 45/65 from frees/pen/45/65 \n");
			sb.append(shotPostsOpp + " total off posts. " + shotPostsPlayOpp
					+ " off posts from play. " + freeOpp[4]
					+ " off posts frees/pen/45/65 \n");
			sb.append(shotSavedOpp + " total saved. " + shotSavedPlayOpp
					+ " saved from play. " + freeOpp[5]
					+ " saved from frees/pen/45/65 \n");
			sb.append(shotShortOpp + " total short. " + shotShortPlayOpp
					+ " short from play. " + freeOpp[6]
					+ " short from frees/pen/45/65 \n");
			sb.append(tShotsTotalOpp.getText().toString() + "\n");
			sb.append(tShotsPlayOpp.getText().toString() + "\n");
			sb.append(tShotsFreeOpp.getText().toString() + "\n");
			sb.append("frees conceded: " + freeConcededOpp + "\n");
			sb.append("Total puck/kick outs: " + totPOpp + "\n");
			sb.append("own puck/kick outs won: " + puckWonCleanOpp + "("
					+ puckWonOppPerCent + "%) \n");
			sb.append("own puck/kick outs lost: " + puckLostCleanOpp + "("
					+ puckLostOppPerCent + "%) \n");
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
		Bitmap bitmap = Bitmap.createBitmap(700, 640, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawColor(Color.rgb(204, 255, 204));
		Paint paint = new Paint();
		paint.setColor(Color.rgb(255, 255, 219));
		canvas.drawRect(350, 0, 700, 640, paint);
		paint.setColor(Color.BLACK);
		canvas.drawLine(350, 0, 350, 580, paint);
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
		paint.setTextSize(18);
		canvas.drawText("from", 230, 80, paint);
		canvas.drawText("free/pen", 283, 80, paint);
		paint.setTextSize(22);
		canvas.drawText("Total", 160, 100, paint);
		paint.setTextSize(18);
		canvas.drawText("play", 235, 100, paint);
		canvas.drawText("45/65", 295, 100, paint);

		paint.setTextSize(22);
		paint.setColor(Color.BLACK);
		canvas.drawText("Goals", 5, 125, paint);
		canvas.drawText(homeGoals + " ", 180, 125, paint);
		canvas.drawText(shotGoalsPlayHome + " ", 245, 125, paint);
		canvas.drawText(freeHome[0] + " ", 310, 125, paint);

		canvas.drawText("Points", 5, 150, paint);
		canvas.drawText(homePoints + " ", 180, 150, paint);
		canvas.drawText(shotPointsPlayHome + " ", 245, 150, paint);
		canvas.drawText(freeHome[1] + " ", 310, 150, paint);

		canvas.drawText("Wides", 5, 175, paint);
		canvas.drawText(shotWidesHome + " ", 180, 175, paint);
		canvas.drawText(shotWidesPlayHome + " ", 245, 175, paint);
		canvas.drawText(freeHome[2] + " ", 310, 175, paint);

		canvas.drawText("Out for 45/65", 5, 200, paint);
		canvas.drawText(shot45Home + " ", 180, 200, paint);
		canvas.drawText(shot45PlayHome + " ", 245, 200, paint);
		canvas.drawText(freeHome[3] + " ", 310, 200, paint);

		canvas.drawText("Off Posts", 5, 225, paint);
		canvas.drawText(shotPostsHome + " ", 180, 225, paint);
		canvas.drawText(shotPostsPlayHome + " ", 245, 225, paint);
		canvas.drawText(freeHome[4] + " ", 310, 225, paint);

		canvas.drawText("Saved", 5, 250, paint);
		canvas.drawText(shotSavedHome + " ", 180, 250, paint);
		canvas.drawText(shotSavedPlayHome + " ", 245, 250, paint);
		canvas.drawText(freeHome[5] + " ", 310, 250, paint);

		canvas.drawText("Short", 5, 275, paint);
		canvas.drawText(shotShortHome + " ", 180, 275, paint);
		canvas.drawText(shotShortPlayHome + " ", 245, 275, paint);
		canvas.drawText(freeHome[6] + " ", 310, 275, paint);

		paint.setTextSize(20);
		canvas.drawText(tShotsTotalHome.getText().toString(), 5, 315, paint);
		canvas.drawText(tShotsPlayHome.getText().toString(), 5, 350, paint);
		canvas.drawText("Shots from frees/pen/45/65: " + shotsFreeHome, 5, 385,
				paint);
		if (shotsFreeHome > 0) {
			canvas.drawText(
					"Scored: "
							+ shotsScoredFreeHome
							+ " ("
							+ Integer.toString((shotsScoredFreeHome * 100)
									/ shotsFreeHome) + "%)", 5, 410, paint);
		} else {
			canvas.drawText("Scored: " + shotsScoredFreeHome, 5, 410, paint);
		}

		paint.setTextSize(22);
		canvas.drawText("Frees Conceded", 5, 450, paint);
		canvas.drawText(freeConcededHome + " ", 250, 450, paint);
		paint.setTextSize(20);
		canvas.drawText("Total Puck/Kick Outs", 5, 485, paint);
		canvas.drawText(totPHome + " ", 250, 485, paint);

		canvas.drawText("Own Puck/Kick Out Won", 5, 510, paint);
		canvas.drawText(puckWonCleanHome + " ", 250, 510, paint);
		canvas.drawText("(" + puckWonHomePerCent + "%) ", 280, 510, paint);

		canvas.drawText("Own Puck/Kick Out Lost", 5, 535, paint);
		canvas.drawText(puckLostCleanHome + " ", 250, 535, paint);
		canvas.drawText("(" + puckLostHomePerCent + "%) ", 280, 535, paint);

		paint.setTextSize(22);
		canvas.drawText(cardHome + subHome, 5, 570, paint);

		paint.setTextAlign(Align.CENTER);
		canvas.drawText(tOppTeam.getText().toString(), 550, 25, paint);
		canvas.drawText(oppGoals + "-" + oppPoints + " (" + oppTotal + ")",
				525, 50, paint);
		paint.setTextAlign(Align.LEFT);

		paint.setTextAlign(Align.LEFT);
		paint.setColor(Color.RED);
		paint.setTextSize(18);
		canvas.drawText("from", 580, 80, paint);
		canvas.drawText("free/pen", 632, 80, paint);
		paint.setTextSize(22);
		canvas.drawText("Total", 515, 100, paint);
		paint.setTextSize(18);
		canvas.drawText("play", 585, 100, paint);
		canvas.drawText("45/65", 640, 100, paint);

		paint.setTextSize(22);
		paint.setColor(Color.BLACK);
		canvas.drawText("Goals", 355, 125, paint);
		canvas.drawText(oppGoals + " ", 535, 125, paint);
		canvas.drawText(shotGoalsPlayOpp + " ", 595, 125, paint);
		canvas.drawText(freeOpp[0] + " ", 655, 125, paint);

		canvas.drawText("Points", 355, 150, paint);
		canvas.drawText(oppPoints + " ", 535, 150, paint);
		canvas.drawText(shotPointsPlayOpp + " ", 595, 150, paint);
		canvas.drawText(freeOpp[1] + " ", 655, 150, paint);

		canvas.drawText("Wides", 355, 175, paint);
		canvas.drawText(shotWidesOpp + " ", 535, 175, paint);
		canvas.drawText(shotWidesPlayOpp + " ", 595, 175, paint);
		canvas.drawText(freeOpp[2] + " ", 655, 175, paint);

		canvas.drawText("O" + "ut for 45/65", 355, 200, paint);
		canvas.drawText(shot45Opp + " ", 535, 200, paint);
		canvas.drawText(shot45PlayOpp + " ", 595, 200, paint);
		canvas.drawText(freeOpp[3] + " ", 655, 200, paint);

		canvas.drawText("Off Posts", 355, 225, paint);
		canvas.drawText(shotPostsOpp + " ", 535, 225, paint);
		canvas.drawText(shotPostsPlayOpp + " ", 595, 225, paint);
		canvas.drawText(freeOpp[4] + " ", 655, 225, paint);

		canvas.drawText("Saved", 355, 250, paint);
		canvas.drawText(shotSavedOpp + " ", 535, 250, paint);
		canvas.drawText(shotSavedPlayOpp + " ", 595, 250, paint);
		canvas.drawText(freeOpp[5] + " ", 655, 250, paint);

		canvas.drawText("Short", 355, 275, paint);
		canvas.drawText(shotShortOpp + " ", 535, 275, paint);
		canvas.drawText(shotShortPlayOpp + " ", 595, 275, paint);
		canvas.drawText(freeOpp[6] + " ", 655, 275, paint);

		paint.setTextSize(20);
		canvas.drawText(tShotsTotalOpp.getText().toString(), 355, 315, paint);
		canvas.drawText(tShotsPlayOpp.getText().toString(), 355, 350, paint);
		canvas.drawText("Shots from frees/pen/45/65: " + shotsFreeOpp, 355,
				385, paint);
		if (shotsFreeOpp > 0) {
			canvas.drawText(
					"Scored: "
							+ shotsScoredFreeOpp
							+ " ("
							+ Integer.toString((shotsScoredFreeOpp * 100)
									/ shotsFreeOpp) + "%)", 355, 410, paint);
		} else {
			canvas.drawText("Scored: " + shotsScoredFreeOpp, 355, 410, paint);
		}
		paint.setTextSize(22);

		canvas.drawText("Frees Conceded", 355, 450, paint);
		canvas.drawText(freeConcededOpp + " ", 600, 450, paint);
		paint.setTextSize(20);
		canvas.drawText("Total Puck/Kick Outs", 355, 485, paint);
		canvas.drawText(totPOpp + " ", 600, 485, paint);

		canvas.drawText("Own Puck/Kick Out Won", 355, 510, paint);
		canvas.drawText(puckWonCleanOpp + " ", 600, 510, paint);
		canvas.drawText("(" + puckWonOppPerCent + "%) ", 625, 510, paint);

		canvas.drawText("Own Puck/Kick Out Lost", 355, 535, paint);
		canvas.drawText(puckLostCleanOpp + " ", 600, 535, paint);
		canvas.drawText("(" + puckLostOppPerCent + "%) ", 625, 535, paint);

		paint.setTextSize(22);
		canvas.drawText(cardOpp + subOpp, 355, 570, paint);

		paint.setColor(Color.GRAY);
		paint.setTextSize(16);
		paint.setTextAlign(Align.CENTER);
		canvas.drawText("GAA Scores Stats Plus - Android App.", 350, 610, paint);
		canvas.drawText("Available free from Google Play Store", 350, 630,
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
