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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ListSubsCards extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.subs_cards_layout);
		Context context = this;

		// this class is called with an intent from the calling application
		// HELP_ID is used to pass in the calling screen so that
		// the relevant help screen is to be displayed
		Intent i = getIntent();
		String iSubs = i.getExtras().getString("SUBS", "No subs recorded");
		String iCards = i.getExtras().getString("CARDS", "No cards recorded");
		if (iSubs.equals("")) {
			iSubs = "No subs recorded";
		}
		if (iCards.equals("")) {
			iCards = "No cards recorded";
		}

		TextView tvSubs = (TextView) findViewById(R.id.tSubs);
		TextView tvCards = (TextView) findViewById(R.id.tCards);
		// the help screens are formatted using html
		// so they are loaded as html pages
		tvSubs.setText(iSubs);
		tvCards.setText(iCards);

		Button back = (Button) findViewById(R.id.bBack);
		back.setOnClickListener(goBack);

	}

	OnClickListener goBack = new OnClickListener() {
		@Override
		public void onClick(View v) {

			ListSubsCards.this.finish();

		}
	};
}
