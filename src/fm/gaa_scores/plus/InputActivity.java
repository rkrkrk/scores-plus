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

import fm.gaa_scores.plus.GRadioGroup;
import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.RadioButton;

public class InputActivity extends Activity {
	private String player = "", stats1 = "", stats2 = "",
			homeTeam = "OWN TEAM", oppTeam = "OPPOSITION", teamBack = "",
			teamOriginal = "";
	private String[] teamLineUpHome, teamLineUpOpp,
			teamLineUp = new String[26];
	private Button[] bb = new Button[16];
	private RadioButton[] rbtShot = new RadioButton[8];
	private RadioButton[] rbrshot = new RadioButton[10];
	private RadioButton bHomeTeam, bOppTeam;
	private GRadioGroup grStats1, grTeam;
	boolean backPressed = false;
	private int call = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.stats_layout_edit);
 
		Bundle extras = getIntent().getExtras();
		teamLineUpHome = extras.getStringArray("teamLineUpHome");
		teamLineUpOpp = extras.getStringArray("teamLineUpOpp");
		call = extras.getInt("call");
		stats1 = extras.getString("stats1");
		stats2 = extras.getString("stats2");
		player = extras.getString("player");
		homeTeam = extras.getString("homeTeam");
		oppTeam = extras.getString("oppTeam");
		teamOriginal = extras.getString("teamOriginal");
		teamOriginal = (teamOriginal == null) ? "" : teamOriginal;
		teamBack = homeTeam;
		if (call == 1) {
			setContentView(R.layout.stats_layout_edit);
			if (teamOriginal.equals(homeTeam)) {
				teamLineUp = teamLineUpHome;
			} else if (teamOriginal.equals(oppTeam)) {
				teamLineUp = teamLineUpOpp;
			} else if (!teamOriginal.equals("")) {
				for (int j = 1; j <= 15; j++) {
					teamLineUp[j] = String.valueOf(j);
					teamLineUpHome[j] = String.valueOf(j);
					homeTeam = teamOriginal;
				}
			} else {
				teamLineUp = teamLineUpHome;
			}
			getActionBar().setTitle("insert/edit stats entry"); 
		} else {
			setContentView(R.layout.stats_layout);
			teamLineUp = teamLineUpHome;
			getActionBar().setTitle("enter stats for "+teamOriginal);  
		}
		// set up player arrays
		Button back = (Button) findViewById(R.id.Bcancel);
		back.setOnClickListener(goBack);
		Button ok = (Button) findViewById(R.id.Bok);
		ok.setOnClickListener(goOK);
		Button reset = (Button) findViewById(R.id.Breset);
		reset.setOnClickListener(goReset);

		bb = new Button[16];
		for (int i = 1; i <= 15; i++) {
			bb[i] = (Button) findViewById(getResources().getIdentifier(
					"ButtonP" + String.format("%02d", i), "id",
					"fm.gaa_scores.plus"));
			// For Home team assign player name to team lineup
			// For Opposition just use position numbers
			bb[i].setText(teamLineUp[i]);
			// bb[i].setOnClickListener(getPlayerClickListener);
			bb[i].setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_UP) {
						// if(event.getAction()==MotionEvent.ACTION_DOWN) return
						// true;
						// if(event.getAction()!=MotionEvent.ACTION_UP) return
						// false;
						player = ((Button) v).getText().toString();
						((Button) v).setPressed(true);
						resetButton();
						return true;
					}
					return false;
				}
			});
			if (player != null && player.equals(teamLineUp[i])) {
				bb[i].setPressed(true);
			}
		}

		// 9 choices for shots
		for (int i = 0; i < 10; i++) {
			rbrshot[i] = (RadioButton) findViewById(getResources()
					.getIdentifier("radio_shot_r" + String.format("%02d", i),
							"id", "fm.gaa_scores.plus"));
			if (stats1 != null
					&& stats1.equals(rbrshot[i].getText().toString())) {
				rbrshot[i].setChecked(true);
			}
		}
		grStats1 = new GRadioGroup(rbrshot[0], rbrshot[1], rbrshot[2],
				rbrshot[3], rbrshot[4], rbrshot[5], rbrshot[6], rbrshot[7],
				rbrshot[8],rbrshot[9]);

		for (int i = 0; i < 8; i++) {
			rbtShot[i] = (RadioButton) findViewById(getResources()
					.getIdentifier("radio_shot_t" + String.format("%02d", i),
							"id", "fm.gaa_scores.plus"));
			rbtShot[i].setOnClickListener(getStats2ClickListener);
			if (stats2 != null
					&& stats2.equals(rbtShot[i].getText().toString())) {
				rbtShot[i].setChecked(true);
			}
		}

		if (call == 1) {
			bHomeTeam = (RadioButton) findViewById(R.id.teamHome);
			bHomeTeam.setText(homeTeam);
			bHomeTeam.setOnClickListener(getTeamClickListener);
			bOppTeam = (RadioButton) findViewById(R.id.teamOpp);
			bOppTeam.setText(oppTeam);
			bOppTeam.setOnClickListener(getTeamClickListener);			
			bHomeTeam.setChecked(false);
			bOppTeam.setChecked(false);
			if (teamOriginal.equals(homeTeam) ||teamOriginal.equals("")) {
				bHomeTeam.setChecked(true);
				teamBack = homeTeam;
			} else if (teamOriginal.equals(oppTeam)) {
				bOppTeam.setChecked(true);
				teamBack = oppTeam;
			}
		}

	}

	// Listener to get player name
	OnClickListener getTeamClickListener = new OnClickListener() {
		@Override
		public void onClick(View vvv) {
			Button b = (Button) vvv;
			switch (b.getId()) {
			case R.id.teamHome:
				bHomeTeam.setChecked(true);
				bOppTeam.setChecked(false);
				teamBack = homeTeam;
				for (int i = 1; i <= 15; i++) {
					bb[i].setText(teamLineUpHome[i]);
				}
				break;
			case R.id.teamOpp:
				bHomeTeam.setChecked(false);
				bOppTeam.setChecked(true);
				teamBack = oppTeam;
				for (int i = 1; i <= 15; i++) {
					bb[i].setText(teamLineUpOpp[i]);
				}
				break;
			}
		}
	};

	// Listener to get player name
	OnClickListener getPlayerClickListener = new OnClickListener() {
		@Override
		public void onClick(View vvv) {
			Button b = (Button) vvv;
			player = (b.getText().toString());
		}
	};

	// listener to get shot type
	OnClickListener getStats2ClickListener = new OnClickListener() {
		@Override
		public void onClick(View vvv) {
			RadioButton rB = (RadioButton) vvv;
			stats2 = (rB.getText().toString());
		}
	};

	OnClickListener goBack = new OnClickListener() {
		@Override
		public void onClick(View v) {
			stats1 = "";
			stats2 = "";
			player = "";
			InputActivity.this.finish();
		}
	};

	OnClickListener goOK = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (grStats1.getID() > 0) {
				getReason();
			}
			InputActivity.this.finish();
		}
	};

	OnClickListener goReset = new OnClickListener() {
		@Override
		public void onClick(View v) {
			for (int i = 1; i <= 15; i++) {
				bb[i].setPressed(false);
			}
			for (int i = 0; i < 8; i++) {
				rbtShot[i].setChecked(false);
			}
			for (int i = 0; i < 9; i++) {
				grStats1.radios.get(i).setChecked(false);
			}
			stats1 = "";
			stats2 = "";
			player = "";
			grStats1.setID(-1);
			if (teamOriginal.equals(homeTeam) && teamBack.equals(oppTeam)) {
				bHomeTeam.setChecked(true);
				bOppTeam.setChecked(false);
				teamBack = homeTeam;
				for (int i = 1; i <= 15; i++) {
					bb[i].setText(teamLineUpHome[i]);
				}
			} else if (teamOriginal.equals(oppTeam)
					&& teamBack.equals(homeTeam)) {
				bHomeTeam.setChecked(false);
				bOppTeam.setChecked(true);
				teamBack = oppTeam;
				for (int i = 1; i <= 15; i++) {
					bb[i].setText(teamLineUpOpp[i]);
				}
			}
		}
	};

	private void resetButton() {
		for (int i = 1; i <= 15; i++) {
			bb[i].setPressed(false);
		}
	}

	private void getReason() {
		switch (grStats1.getID()) {
		case R.id.radio_shot_r00:
			stats1 = "goal";
			break;
		case R.id.radio_shot_r01:
			stats1 = "point";
			break;
		case R.id.radio_shot_r02:
			stats1 = "wide";
			break;
		case R.id.radio_shot_r03:
			stats1 = "out for 45/65";
			break;
		case R.id.radio_shot_r04:
			stats1 = "off posts";
			break;
		case R.id.radio_shot_r05:
			stats1 = "saved";
			break;
		case R.id.radio_shot_r06:
			stats1 = "short";
			break;
		case R.id.radio_shot_r07:
			stats1 = "free/pen conceded";
			break;
		case R.id.radio_shot_r08:
			stats1 = "own puck/kick out won";
			break;
		case R.id.radio_shot_r09:
			stats1 = "own puck/kick out lost";
			break;
		}
	}
	
	public void onBackPressed() {
        backPressed=true;   
        finish();
	}

	@Override
	public void finish() {
		// Prepare data intent
		// Intent i = new Intent();
		getIntent().putExtra("backPressed", backPressed);
		getIntent().putExtra("stats1", stats1);
		getIntent().putExtra("stats2", stats2);
		getIntent().putExtra("player", player);
		if (call == 1) {
			getIntent().putExtra("teamBack", teamBack);
		}
		setResult(RESULT_OK, getIntent());
		super.finish();
	}

}
