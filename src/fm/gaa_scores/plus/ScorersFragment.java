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

import fm.gaa_scores.plus.R;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class ScorersFragment extends ListFragment {

	private ListView lv1 = null;
	private ListView lv2 = null;
	private TextView tOwnTeam, tOppTeam;
	private String ownTeam, oppTeam;
	private Button bSendAll, bTweetAll;

	@Override
	// start main method to display screen
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.scorers, container, false);

		String myTag = getTag();
		((Startup) getActivity()).setTagFragmentScorers(myTag);
		this.setHasOptionsMenu(true);

		SharedPreferences sharedPref = getActivity().getSharedPreferences(
				"team_stats_review_data", Context.MODE_PRIVATE);

		lv1 = (ListView) v.findViewById(android.R.id.list);
		lv2 = (ListView) v.findViewById(R.id.list2);

		// set up text buttons edittexts etc.
		tOwnTeam = (TextView) v.findViewById(R.id.score1);
		tOppTeam = (TextView) v.findViewById(R.id.score2);
		ownTeam = sharedPref.getString("OURTEAM", "OWN TEAM");
		oppTeam = sharedPref.getString("OPPTEAM", "OPPOSITION");
		tOwnTeam.setText("SCORERS " + ownTeam);
		tOppTeam.setText("SCORERS " + oppTeam);
		bSendAll = (Button) v.findViewById(R.id.bSendAllScores);
		bSendAll.setOnClickListener(sendAllListener);
		bTweetAll = (Button) v.findViewById(R.id.bTweetAllScores);
		bTweetAll.setOnClickListener(tweetAllListener);

		fillData();

		return v;
	}

	public void fillData() {
		Uri allTitles = TeamContentProvider.CONTENT_URI_3;
		String[] from = new String[] {
				TeamContentProvider.SCORESNAME,
				// TeamContentProvider.SCORESTEAM,
				TeamContentProvider.SCORESGOALS,
				TeamContentProvider.SCORESPOINTS,
				TeamContentProvider.SCORESGOALSFREE,
				TeamContentProvider.SCORESPOINTSFREE,
				TeamContentProvider.SCORESMISS };

		// create array to map these fields to
		int[] to = new int[] { R.id.text1, R.id.text3, R.id.text4, R.id.text5,
				R.id.text6, R.id.text7 };

		// load database info from PanelContentProvider into a cursor and use an
		// adapter to display on screen
		String[] args = { ownTeam };
		Cursor c1 = getActivity().getContentResolver().query(allTitles, null,
				"team=?", args, TeamContentProvider.SCORESTOTAL + " DESC");
		SimpleCursorAdapter reminders = new SimpleCursorAdapter(getActivity(),
				R.layout.scorers_row, c1, from, to, 0);
		lv1.setAdapter(reminders);

		String[] args1 = { oppTeam };
		Cursor c2 = getActivity().getContentResolver().query(allTitles, null,
				"team=?", args1, TeamContentProvider.SCORESTOTAL + " DESC");
		SimpleCursorAdapter reminders2 = new SimpleCursorAdapter(getActivity(),
				R.layout.scorers_row, c2, from, to, 0);
		lv2.setAdapter(reminders2);
		
	}

	// for reset buttons diplay message to long click, won't work with ordinary
	// click
	OnClickListener sendAllListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			StringBuilder sb = new StringBuilder("");
			Uri allTitles = TeamContentProvider.CONTENT_URI_3;
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
			String[] args = { ownTeam };
			Log.e("getting cursor","cc1");
			Cursor c1 = getActivity().getContentResolver().query(allTitles,
					null, "team=?", args,
					TeamContentProvider.SCORESTOTAL + " DESC");

			String[] args1 = { oppTeam };
			Cursor c2 = getActivity().getContentResolver().query(allTitles,
					null, "team=?", args1,
					TeamContentProvider.SCORESTOTAL + " DESC");
			Log.e("got cursor","c21");


			sb.append("Team 1: " + ownTeam + "SCORERS \n\n");
			sb.append("player  *  Total Goals / Points  *  Goals/Points from frees/65s/45s/penalties/sidelines  *  wides/short/saved\n\n");

			if (c1.getCount() > 0) {
				c1.moveToFirst();
				do {
					// read in player nicknames
					sb.append(
							c1.getString(c1
									.getColumnIndexOrThrow(TeamContentProvider.SCORESNAME))
							+ "  *   "
							+ c1.getString(c1
									.getColumnIndexOrThrow(TeamContentProvider.SCORESGOALS))
							+ "-"
							+ c1.getString(c1
									.getColumnIndexOrThrow(TeamContentProvider.SCORESPOINTS))
							+ "  *  "
							+ c1.getString(c1
									.getColumnIndexOrThrow(TeamContentProvider.SCORESGOALSFREE))
							+ "-"
							+ c1.getString(c1
									.getColumnIndexOrThrow(TeamContentProvider.SCORESPOINTSFREE))
							+ "  *  "
							+ c1.getString(c1
									.getColumnIndexOrThrow(TeamContentProvider.SCORESMISS))
							+ "\n\n");
					// insert players into positions

				} while (c1.moveToNext());

			}

			sb.append("\nTeam 2: " +oppTeam + "\n\n");
			if (c2.getCount() > 0) {
				c2.moveToFirst();
				do {
					// read in player nicknames
					sb.append(c2.getString(c2
									.getColumnIndexOrThrow(TeamContentProvider.SCORESNAME))
							+ "  *   "
							+ c2.getString(c2
									.getColumnIndexOrThrow(TeamContentProvider.SCORESGOALS))
							+ "-"
							+ c2.getString(c2
									.getColumnIndexOrThrow(TeamContentProvider.SCORESPOINTS))
							+ "  *  "
							+ c2.getString(c2
									.getColumnIndexOrThrow(TeamContentProvider.SCORESGOALSFREE))
							+ "-"
							+ c2.getString(c2
									.getColumnIndexOrThrow(TeamContentProvider.SCORESPOINTSFREE))
							+ "  *  "
							+ c2.getString(c2
									.getColumnIndexOrThrow(TeamContentProvider.SCORESMISS))
							+ "\n\n");
					// insert players into positions

				} while (c2.moveToNext());

			}
				Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
			emailIntent
					.putExtra(Intent.EXTRA_SUBJECT, "match report "
							+ ((Startup) getActivity()).getFragmentScore()
									.getLocText());
			emailIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
			emailIntent.setType("text/plain");
			startActivity(Intent.createChooser(emailIntent, "Share Using:"));
			c1.close();
			c2.close();
		}
	};

	OnClickListener tweetAllListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			//
		}
	};

	// this method is called from the SETUP fragment to update the names of the
	// home and away teams and to receive team line and teams from setup screen
	public void setTeamNames(String homeTeam, String oppTeamm) {
		if (!homeTeam.equals(""))
			tOwnTeam.setText("SCORERS " + homeTeam);
		ownTeam = homeTeam;
		if (!oppTeam.equals(""))
			tOppTeam.setText("SCORERS " + oppTeamm);
		oppTeam = oppTeamm;
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		inflater.inflate(R.menu.scorers_menu, menu);
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
			ihelp.putExtra("HELP_ID", R.string.scorersHelp);
			startActivity(ihelp);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
