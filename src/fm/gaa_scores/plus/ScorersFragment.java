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
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class ScorersFragment extends ListFragment {
	
	private ListView lv1 = null;
	private ListView lv2 = null;
	private TextView  tOwnTeam, tOppTeam;
	private String ownTeam, oppTeam;

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

		lv1 = (ListView) v.findViewById (android.R.id.list);
	    lv2 = (ListView) v.findViewById (R.id.list2);
	    	    
		// set up text buttons edittexts etc.
		tOwnTeam = (TextView) v.findViewById(R.id.score1);
		tOppTeam = (TextView) v.findViewById(R.id.score2);
		ownTeam=sharedPref.getString("OURTEAM", "OWN TEAM");
		oppTeam=sharedPref.getString("OPPTEAM", "OPPOSITION");
		tOwnTeam.setText("SCORERS " +ownTeam);
		tOppTeam.setText("SCORERS " + oppTeam);
	    
		fillData();

		return v;
	}

	public void fillData(){
		Uri allTitles = TeamContentProvider.CONTENT_URI_3;
		String[] from = new String[] { TeamContentProvider.SCORESNAME,
//				TeamContentProvider.SCORESTEAM,
				TeamContentProvider.SCORESGOALS,
				TeamContentProvider.SCORESPOINTS,
				TeamContentProvider.SCORESGOALSFREE,
				TeamContentProvider.SCORESPOINTSFREE,
				TeamContentProvider.SCORESMISS};

		// create array to map these fields to
		int[] to = new int[] { R.id.text1,  R.id.text3,
				R.id.text4, R.id.text5, R.id.text6,R.id.text7 };

		// load database info from PanelContentProvider into a cursor and use an
		// adapter to display on screen
		String[] args = { ownTeam };
		Cursor c1 = getActivity().getContentResolver().query(allTitles, null, "team=?", args,
				TeamContentProvider.SCORESNAME);
		SimpleCursorAdapter reminders = new SimpleCursorAdapter(getActivity(),
				R.layout.scorers_row, c1, from, to, 0);
		lv1.setAdapter(reminders);
		Log.e("get ownTeam "," "+ownTeam+"  c : "+c1.getCount());

		String[] args1 = { oppTeam };
		Cursor c2 = getActivity().getContentResolver().query(allTitles, null, "team=?", args1,
				TeamContentProvider.SCORESNAME);
		SimpleCursorAdapter reminders2 = new SimpleCursorAdapter(getActivity(),
				R.layout.scorers_row, c2, from, to, 0);
		lv2.setAdapter(reminders2);
		Log.e("get oppTeam "," "+oppTeam+"  c : "+c2.getCount());

	}
	
	// this method is called from the SETUP fragment to update the names of the
	// home and away teams and to receive team line and teams from setup screen
	public void setTeamNames(String homeTeam, String oppTeamm) {
		if (!homeTeam.equals(""))
				tOwnTeam.setText("SCORERS "+homeTeam);
				ownTeam=homeTeam;
		if (!oppTeam.equals(""))
				tOppTeam.setText("SCORERS "+oppTeamm);
				oppTeam=oppTeamm;
	}
	
	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.scorers_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
	// set up help menu in action bar
	//@Override
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)	{
		Intent ihelp;
		switch (item.getItemId()) {
		case 0:
			//menu pointer do nothing
		case R.id.helpTeam:
			ihelp = new Intent(getActivity(), HelpActivity.class);
			ihelp.putExtra("HELP_ID", R.string.scorersHelp);
			startActivity(ihelp);
			return true;		}	
	    return super.onOptionsItemSelected(item);	
	}
	
}
