/*
 MatchApplication.java
 *
 *  Written by: Fintan Mahon 12101524
 *  Description: Parent Activity for the three fragment screens
 *  SETUP, RECORD and REVIEW 
 *  This class sets up a view pager and menu tabs and loads up the
 *  three fragments so that they are swipeable
 *  
 *  Written on: Jan 2013
 *  
 *  references code from Android Reference
 *	http://developer.android.com/reference/android/support/v4/view/ViewPager.html
 *
 *  
 *  
 */

package fm.gaa_scores.plus;

import android.app.ActionBar;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

public class Startup extends FragmentActivity {

	private TabsAdapterfm mTabsAdapter;
	private ViewPager mViewPager;
	private String tabFragmentScore, tabFragmentReview, tabFragmentTeamOne;
	private String tabFragmentTeamTwo, tabFragmentScorers, tabFragmentEvent;
	private ScoresFragment fragmentScore;
	private ScorersFragment fragmentScorers;
	private ReviewFragment fragmentReview;
	private TeamOneFragment fragmentTeamOne;
	private TeamTwoFragment fragmentTeamTwo;
	private EventsListFragment fragmentEvent;
	private Bundle bundle;
	private ActionBar actionBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// set up 3 fragment screens
		// initialise view and view pager
		setContentView(R.layout.tabs_layout);
		mViewPager = (ViewPager) findViewById(R.id.viewpager);
		// set up tabs display in action bar
		actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayShowTitleEnabled(false);

		// start up the 3 fragments
		// uses code from Android Reference
		// http://developer.android.com/reference/android/support/v4/view/ViewPager.html
		mTabsAdapter = new TabsAdapterfm(super.getSupportFragmentManager(),
				this, mViewPager);
		mTabsAdapter.addTab(actionBar.newTab().setText("team2"),
				TeamTwoFragment.class, null);
		mTabsAdapter.addTab(actionBar.newTab().setText("team1"),
				TeamOneFragment.class, null);
		mTabsAdapter.addTab(actionBar.newTab().setText("match"),
				ScoresFragment.class, null);
		mTabsAdapter.addTab(actionBar.newTab().setText("stats"),
				ReviewFragment.class, null);
		mTabsAdapter.addTab(actionBar.newTab().setText("scorers"),
				ScorersFragment.class, null);
		mTabsAdapter.addTab(actionBar.newTab().setText("events"),
				EventsListFragment.class, null);
		// if restarting program, return to the last active tab/fragment
		if (savedInstanceState != null) {
			actionBar.setSelectedNavigationItem(savedInstanceState.getInt(
					"tab", 0));
		} else
			actionBar.setSelectedNavigationItem(2);
	}
	
	@Override
	public void onPause() {
		// Save/persist data to be used on reopen
		super.onPause(); // Always call the superclass method first
		SharedPreferences sharedPref = getSharedPreferences(
				"team_stats_record_data", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putInt("TAB", getActionBar().getSelectedNavigationIndex());
		editor.commit();
	}

	@Override
	public void onResume() {
		super.onResume(); // Always call the superclass method first
		//start up middle fragment for 100 so that all fragments instantiatded
//		actionBar.setSelectedNavigationItem(2);
//		Handler handler = new Handler();
//		handler.postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				SharedPreferences sharedPref = getSharedPreferences(
//						"team_stats_record_data", Context.MODE_PRIVATE);
//
//				actionBar.setSelectedNavigationItem(sharedPref.getInt("TAB",2));
//				}
//		}, 10);	
	}

	@Override
	// save which fragment is active as you close so that you can return
	// to it when restarting
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("tab", getActionBar().getSelectedNavigationIndex());
	}

	// this method is called by the RECORD fragment (MatchRecordFragment) which
	// passes its tag name into here once it starts up. This parent Activity
	// can use that tag name to create a reference to the fragment
	public void setTagFragmentScore(String t) {
		tabFragmentScore = t;
		fragmentScore = (ScoresFragment) this.getSupportFragmentManager()
				.findFragmentByTag(tabFragmentScore);
	}

	// method returns the tag name of the active RECORD fragment
	public String getTagFragmentScore() {
		return tabFragmentScore;
	}

	// method called by SETUP and REVIEW fragments to get a reference to the
	// RECORD
	// fragment so that they can make method calls/access variables
	public ScoresFragment getFragmentScore() {
		if (fragmentScore == null)
			fragmentScore = (ScoresFragment) this.getSupportFragmentManager()
					.findFragmentByTag(tabFragmentScore);
		return fragmentScore;
	}

	// this method is called by the SCORERS fragment (ScoresrFragment) which
	// passes its tag name into here once it starts up. This parent Activity
	// can use that tag name to create a reference to the fragment
	public void setTagFragmentScorers(String t) {
		tabFragmentScorers = t;
		fragmentScorers = (ScorersFragment) this.getSupportFragmentManager()
				.findFragmentByTag(tabFragmentScorers);
	}

	// method returns the tag name of the active RECORD fragment
	public String getTagFragmentScorers() {
		return tabFragmentScorers;
	}

	// method called by SETUP and REVIEW fragments to get a reference to the
	// RECORD
	// fragment so that they can make method calls/access variables
	public ScorersFragment getFragmentScorers() {
		if (fragmentScorers == null)
			fragmentScorers = (ScorersFragment) this
					.getSupportFragmentManager().findFragmentByTag(
							tabFragmentScorers);
		return fragmentScorers;
	}

	// this method is called by the REVIEW fragment (MatchReviewFragment) which
	// passes its tag name into here once it starts up. This parent Activity
	// can use that tag name to create a reference to the fragment
	public void setTagFragmentReview(String t) {
		tabFragmentReview = t;
		fragmentReview = (ReviewFragment) this.getSupportFragmentManager()
				.findFragmentByTag(tabFragmentReview);
	}

	// method returns the tag name of the active REVIEW fragment
	public String getTagFragmentReview() {
		return tabFragmentReview;
	}

	// method called by SETUP and RECORD fragments to get a reference to the
	// REVIEW
	// fragment so that they can make method calls/access variables
	public ReviewFragment getFragmentReview() {
		if (fragmentReview == null)
			fragmentReview = (ReviewFragment) this.getSupportFragmentManager()
					.findFragmentByTag(tabFragmentReview);
		return fragmentReview;
	}

	// this method is called by the REVIEW fragment (MatchReviewFragment) which
	// passes its tag name into here once it starts up. This parent Activity
	// can use that tag name to create a reference to the fragment
	public void setTagFragmentTeamOne(String t) {
		tabFragmentTeamOne = t;
		fragmentTeamOne = (TeamOneFragment) this.getSupportFragmentManager()
				.findFragmentByTag(tabFragmentTeamOne);
	}

	// method returns the tag name of the active REVIEW fragment
	public String getTagFragmentTeamOne() {
		return tabFragmentTeamOne;
	}

	// method called by SETUP and RECORD fragments to get a reference to the
	// REVIEW
	// fragment so that they can make method calls/access variables
	public TeamOneFragment getFragmentTeamOne() {
		if (fragmentTeamOne == null)
			fragmentTeamOne = (TeamOneFragment) this
					.getSupportFragmentManager().findFragmentByTag(
							tabFragmentTeamOne);
		return fragmentTeamOne;
	}

	// this method is called by the REVIEW fragment (MatchReviewFragment) which
	// passes its tag name into here once it starts up. This parent Activity
	// can use that tag name to create a reference to the fragment
	public void setTagFragmentTeamTwo(String t) {
		tabFragmentTeamTwo = t;
		fragmentTeamTwo = (TeamTwoFragment) this.getSupportFragmentManager()
				.findFragmentByTag(tabFragmentTeamTwo);
	}

	// method returns the tag name of the active REVIEW fragment
	public String getTagFragmentTeamTwo() {
		return tabFragmentTeamTwo;
	}

	// method called by SETUP and RECORD fragments to get a reference to the
	// REVIEW
	// fragment so that they can make method calls/access variables
	public TeamTwoFragment getFragmentTeamTwo() {
		if (fragmentTeamTwo == null)
			fragmentTeamTwo = (TeamTwoFragment) this
					.getSupportFragmentManager().findFragmentByTag(
							tabFragmentTeamTwo);
		return fragmentTeamTwo;
	}
	
	// this method is called by the REVIEW fragment (MatchReviewFragment) which
		// passes its tag name into here once it starts up. This parent Activity
		// can use that tag name to create a reference to the fragment
		public void setTagFragmentEvents(String t) {
			tabFragmentEvent = t;
			fragmentEvent = (EventsListFragment) this.getSupportFragmentManager()
					.findFragmentByTag(tabFragmentEvent);
		}

		// method returns the tag name of the active REVIEW fragment
		public String getTagFragmentEvent() {
			return tabFragmentEvent;
		}

		// method called by SETUP and RECORD fragments to get a reference to the
		// REVIEW
		// fragment so that they can make method calls/access variables
		public EventsListFragment getFragmentEvent() {
			if (fragmentEvent == null)
				fragmentEvent = (EventsListFragment) this
						.getSupportFragmentManager().findFragmentByTag(
								tabFragmentEvent);
			return fragmentEvent;
		}

}
