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
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;

public class InputActivity extends Activity {
	private String player = "", stats1 = "", stats2 = "", teamName = "";
	private Button[] bb = new Button[16];
	private RadioButton[] rbtShot = new RadioButton[8];
	private RadioButton[] rbrshot = new RadioButton[9];
	private GRadioGroup grStats1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stats_layout_act);
		Bundle extras = getIntent().getExtras();
		String teamLineup[] = extras.getStringArray("teamLineup");
		teamName = extras.getString("teamName");

		Button back = (Button) findViewById(R.id.Bcancel);
		back.setOnClickListener(goBack);
		Button ok = (Button) findViewById(R.id.Bok);
		ok.setOnClickListener(goOK);

		bb = new Button[16];
		for (int i = 1; i <= 15; i++) {
			bb[i] = (Button) findViewById(getResources().getIdentifier(
					"ButtonP" + String.format("%02d", i), "id",
					"fm.gaa_scores.plus"));
			// For Home team assign player name to team lineup
			// For Opposition just use position numbers
			bb[i].setText(teamLineup[i]);
			bb[i].setOnClickListener(getPlayerClickListener);
		}

		// 9 choices for shots
		for (int i = 0; i < 9; i++) {
			rbrshot[i] = (RadioButton) findViewById(getResources()
					.getIdentifier("radio_shot_r" + String.format("%02d", i),
							"id", "fm.gaa_scores.plus"));
			rbrshot[i].setOnClickListener(getStats1ClickListener);
		}
		grStats1 = new GRadioGroup(rbrshot[0], rbrshot[1], rbrshot[2],
				rbrshot[3], rbrshot[4], rbrshot[5], rbrshot[6], rbrshot[7],
				rbrshot[8]);

		RadioButton[] rbtShot = new RadioButton[8];
		for (int i = 0; i < 8; i++) {
			rbtShot[i] = (RadioButton) findViewById(getResources()
					.getIdentifier("radio_shot_t" + String.format("%02d", i),
							"id", "fm.gaa_scores.plus"));
			rbtShot[i].setOnClickListener(getStats2ClickListener);
		}

	}

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

	OnClickListener getStats1ClickListener = new OnClickListener() {
		@Override
		public void onClick(View vvv) {
			RadioButton rB = (RadioButton) vvv;
			getReason();
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
			getReason();
			InputActivity.this.finish();
		}
	};

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
			stats1 = "saved/short";
			break;
		case R.id.radio_shot_r06:
			stats1 = "free/pen conceded";
			break;
		case R.id.radio_shot_r07:
			stats1 = "own puck/kick out won";
			break;
		case R.id.radio_shot_r08:
			stats1 = "own puck/kick out lost";
			break;
		}
	}

	@Override
	public void finish() {
		// Prepare data intent
		// Intent i = new Intent();
		getIntent().putExtra("stats1", stats1);
		getIntent().putExtra("stats2", stats2);
		getIntent().putExtra("player", player);
		getIntent().putExtra("teamName", teamName);
		setResult(RESULT_OK, getIntent());
		super.finish();
	}

}
