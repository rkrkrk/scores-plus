/*
 *  MatchSetupFragment.java
 *
 *  Written by: Fintan Mahon 12101524
 *  
 *  Description: GUI to get input re match details and team lineup
 *  store details to database
 *  pass relevant details into MatchRecordFragment
 *  
 *  Written on: Jan 2013
 *  
 * 
 */
package fm.gaa_scores.plus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import fm.gaa_scores.plus.R;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
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
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class TeamOneFragment extends Fragment {
	// ArrayList to store panel from database
	private ArrayList<String> panelList = new ArrayList<String>();
	private ArrayList<String> subsList = new ArrayList<String>();
	private ArrayList<Integer> posnList = new ArrayList<Integer>();

	// HashMap to Store Player Name and ID for lookup on saving.
	private HashMap<String, Integer> playerIDLookUp = new HashMap<String, Integer>();

	private String panel[], strTemp[], strTemp2[];
	private String[] teamLineUpCurrent = new String[16];// stores selected team
	private Button[] bTeam = new Button[16];// array of buttons for team
											// selection
	private Button b;
	// private MatchRecordFragment fragmentRecord;//referenence
	private TextView tTeamHome;
	long matchID;
	private String panelName, player, team, playerOff, playerOn, oppTeamName;
	private Date currentDate;
	private SimpleDateFormat sdfdate;
	private EditText input;
	private int index, indexOff, indexOn;
	private TextView tCards, tSubs;
	private boolean bloodSub = false;

	// setup uri to read panel from database using content provider
	Uri allTitles = TeamContentProvider.CONTENT_URI;

	@Override
	// start main method to display screen
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.team_layout, container, false);
		// get the tag name of this Fragment and pass it up to the parent
		// activity MatchApplication so that this Fragment may be accessed
		// by other fragments through using a reference created from tag name

		String myTag = getTag();
		((Startup) getActivity()).setTagFragmentTeamOne(myTag);
		this.setHasOptionsMenu(true);
		v.setBackgroundColor(Color.rgb(204, 255, 204));

		// hide softkeyboard after entry
		// getActivity().getWindow().setSoftInputMode(
		// WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		// set up text view and buttons
		tTeamHome = (TextView) v.findViewById(R.id.homeTeamName);
		Button bButtonReset = (Button) v.findViewById(R.id.button_setup_reset);
		bButtonReset.setOnClickListener(resetTeamListener);
		Button bSub = (Button) v.findViewById(R.id.bSub);
		bSub.setOnClickListener(recordSub);
		Button bBlood = (Button) v.findViewById(R.id.bBlood);
		bBlood.setOnClickListener(recordSub);
		Button bButtonChange = (Button) v.findViewById(R.id.homeTeam);
		bButtonChange.setOnClickListener(changeNameListener);
		// read persisted stored data to set up screen on restart
		SharedPreferences sharedPref = getActivity().getSharedPreferences(
				"home_team_data", Context.MODE_PRIVATE);

		// setup input edittext boxes
		panelName = sharedPref.getString("PANELNAME", "OUR TEAM");
		sharedPref = getActivity().getSharedPreferences("opp_team_data",
				Context.MODE_PRIVATE);
		oppTeamName = sharedPref.getString("PANELNAME", "OPPOSITION");
		tTeamHome.setText(panelName);
		setButtons(v);
		getTeam(panelName);

		tCards = (TextView) v.findViewById(R.id.tCards);
		tSubs = (TextView) v.findViewById(R.id.tSubs);
		updateCards();
		updateSubsList();

		// Listener for reset team button
		// resets team lineup and edittext fields
		bButtonReset.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				resetTeam();
				v.playSoundEffect(SoundEffectConstants.CLICK);
				getTeam(panelName);
				return true;
			}
		});

		Button bSelTweet = (Button) v.findViewById(R.id.sel_tweet);
		bSelTweet.setOnClickListener(selTweetListener);
		Button bSelText = (Button) v.findViewById(R.id.sel_text);
		bSelText.setOnClickListener(selTextListener);
		Button bSelShare = (Button) v.findViewById(R.id.sel_share);
		bSelShare.setOnClickListener(selShareListener);

		return v;
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void onPause() {
		// Save out the details so that they are available on restart
		super.onPause(); // Always call the superclass method first
		SharedPreferences sharedPref = getActivity().getSharedPreferences(
				"home_team_data", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString("PANELNAME", panelName);
		editor.commit();
	}

	// share team selection
	OnClickListener selShareListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// Create Bitmap to display team selection
			StringBuilder sb = new StringBuilder("");
			sb.append(panelName + " v. " + oppTeamName + ". ");

			sb.append(((Startup) getActivity()).getFragmentScore().getLocText()
					+ ". ");
			sb.append(panelName + " team selection:\n ");
			for (int i = 1; i <= 15; i++) {
				sb.append(teamLineUpCurrent[i].length() > 2 ? String.valueOf(i)
						+ ". " + String.valueOf(teamLineUpCurrent[i]) + "\n "
						: String.valueOf(i) + ".\n ");
			}
			Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
			emailIntent.putExtra(Intent.EXTRA_SUBJECT, panelName + " v. "
					+ oppTeamName + ". " + "Team Selection");
			emailIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
			emailIntent.setType("text/plain");
			startActivity(Intent.createChooser(emailIntent, "Share Using:"));
		}
	};

	// text team selection
	OnClickListener selTextListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// Create Bitmap to display team selection
			StringBuilder sb = new StringBuilder("");
			sb.append(panelName + " v. " + oppTeamName + ". ");

			sb.append(((Startup) getActivity()).getFragmentScore().getLocText()
					+ ". ");
			sb.append(panelName + " team selection:\n ");
			for (int i = 1; i <= 15; i++) {
				sb.append(teamLineUpCurrent[i].length() > 2 ? String.valueOf(i)
						+ ". " + String.valueOf(teamLineUpCurrent[i]) + "\n "
						: String.valueOf(i) + ".\n ");
			}
			try {
				Intent intentText = new Intent(Intent.ACTION_VIEW);
				intentText.setType("vnd.android-dir/mms-sms");
				intentText.putExtra("sms_body", sb.toString());
				intentText.setData(Uri.parse("sms: "
						+ ((Startup) getActivity()).getFragmentScore()
								.getPhone()));
				startActivity(intentText);
			} catch (Exception ex) {
				Log.e("Error in Text", ex.toString());
				Toast.makeText(getActivity(), "Unable to send text message",
						Toast.LENGTH_LONG).show();
			}
		}
	};

	// tweet team selection
	// write selection to bitmal and tweet bitmap
	OnClickListener selTweetListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// Create Bitmap to display team selection
			Bitmap bitmap = Bitmap.createBitmap(600, 550,
					Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(bitmap);
			canvas.drawColor(Color.LTGRAY);
			Paint paint = new Paint();
			paint.setColor(Color.BLACK);
			paint.setAntiAlias(true);
			paint.setTextAlign(Align.CENTER);
			paint.setTextSize(22);
			// Write teams
			canvas.drawText(panelName + " v. " + oppTeamName, 300, 25, paint);
			paint.setTextSize(20);
			// write comment - height can vary
			TextPaint mTextPaint = new TextPaint();
			mTextPaint.setTextSize(20);
			StaticLayout mTextLayout = new StaticLayout(
					((Startup) getActivity()).getFragmentScore().getLocText(),
					mTextPaint, canvas.getWidth(), Alignment.ALIGN_NORMAL,
					1.0f, 0.0f, false);
			int commentLines = mTextLayout.getLineCount();
			canvas.save();
			canvas.translate(10, 35);
			mTextLayout.draw(canvas);
			canvas.restore();

			paint.setTextAlign(Align.CENTER);
			paint.setTextSize(22);
			canvas.drawText(panelName + " team selection ", 300,
					80 + (commentLines * 20), paint);

			String str;
			int xxx = 5;
			// Full Forwards
			paint.setTextSize(15);
			paint.setColor(Color.RED);
			canvas.drawText("left corner forward", 100, 130
					+ (commentLines * 20) + xxx, paint);
			canvas.drawText("full forward", 300, 130 + (commentLines * 20)
					+ xxx, paint);
			canvas.drawText("right corner forward", 500, 130
					+ (commentLines * 20) + xxx, paint);
			paint.setTextSize(18);
			paint.setColor(Color.BLACK);
			str = teamLineUpCurrent[15].length() > 2 ? String
					.valueOf(teamLineUpCurrent[15]) : String.valueOf(15) + ".";
			canvas.drawText(str, 100, 150 + (commentLines * 20) + xxx, paint);
			str = teamLineUpCurrent[14].length() > 2 ? String
					.valueOf(teamLineUpCurrent[14]) : String.valueOf(14) + ".";
			canvas.drawText(str, 300, 150 + (commentLines * 20) + xxx, paint);
			str = teamLineUpCurrent[13].length() > 2 ? String
					.valueOf(teamLineUpCurrent[13]) : String.valueOf(13) + ".";
			canvas.drawText(str, 500, 150 + (commentLines * 20) + xxx, paint);

			// HALF forwards
			paint.setTextSize(15);
			paint.setColor(Color.RED);
			canvas.drawText("left half forward", 100, 185 + (commentLines * 20)
					+ xxx, paint);
			canvas.drawText("center forward", 300, 185 + (commentLines * 20)
					+ xxx, paint);
			canvas.drawText("right half forward", 500, 185
					+ (commentLines * 20) + xxx, paint);
			paint.setTextSize(18);
			paint.setColor(Color.BLACK);
			str = teamLineUpCurrent[12].length() > 2 ? String
					.valueOf(teamLineUpCurrent[12]) : String.valueOf(12) + ".";
			canvas.drawText(str, 100, 205 + (commentLines * 20) + xxx, paint);
			str = teamLineUpCurrent[11].length() > 2 ? String
					.valueOf(teamLineUpCurrent[11]) : String.valueOf(11) + ".";
			canvas.drawText(str, 300, 205 + (commentLines * 20) + xxx, paint);
			str = teamLineUpCurrent[10].length() > 2 ? String
					.valueOf(teamLineUpCurrent[10]) : String.valueOf(10) + ".";
			canvas.drawText(str, 500, 205 + (commentLines * 20) + xxx, paint);

			// MidField
			paint.setTextSize(15);
			paint.setColor(Color.RED);
			canvas.drawText("mid field", 150, 240 + (commentLines * 20) + xxx,
					paint);
			canvas.drawText("mid field", 450, 240 + (commentLines * 20) + xxx,
					paint);
			paint.setTextSize(18);
			paint.setColor(Color.BLACK);
			str = teamLineUpCurrent[8].length() > 2 ? String
					.valueOf(teamLineUpCurrent[9]) : String.valueOf(9) + ".";
			canvas.drawText(str, 150, 260 + (commentLines * 20) + xxx, paint);
			str = teamLineUpCurrent[8].length() > 2 ? String
					.valueOf(teamLineUpCurrent[8]) : String.valueOf(8) + ".";
			canvas.drawText(str, 450, 260 + (commentLines * 20) + xxx, paint);

			// HALF backs
			paint.setTextSize(15);
			paint.setColor(Color.RED);
			canvas.drawText("left half back", 100, 295 + (commentLines * 20)
					+ xxx, paint);
			canvas.drawText("center back", 300,
					295 + (commentLines * 20) + xxx, paint);
			canvas.drawText("right half back", 500, 295 + (commentLines * 20)
					+ xxx, paint);
			paint.setTextSize(18);
			paint.setColor(Color.BLACK);
			str = teamLineUpCurrent[7].length() > 2 ? String
					.valueOf(teamLineUpCurrent[7]) : String.valueOf(7) + ".";
			canvas.drawText(str, 100, 315 + (commentLines * 20) + xxx, paint);
			str = teamLineUpCurrent[6].length() > 2 ? String
					.valueOf(teamLineUpCurrent[6]) : String.valueOf(6) + ".";
			canvas.drawText(str, 300, 315 + (commentLines * 20) + xxx, paint);
			str = teamLineUpCurrent[5].length() > 2 ? String
					.valueOf(teamLineUpCurrent[5]) : String.valueOf(5) + ".";
			canvas.drawText(str, 500, 315 + (commentLines * 20) + xxx, paint);

			// FULL backs
			paint.setTextSize(15);
			paint.setColor(Color.RED);
			canvas.drawText("left corner back", 100, 350 + (commentLines * 20)
					+ xxx, paint);
			canvas.drawText("full back", 300, 350 + (commentLines * 20) + xxx,
					paint);
			canvas.drawText("right corner back", 500, 350 + (commentLines * 20)
					+ xxx, paint);
			paint.setTextSize(18);
			paint.setColor(Color.BLACK);
			str = teamLineUpCurrent[4].length() > 2 ? String
					.valueOf(teamLineUpCurrent[4]) : String.valueOf(4) + ".";
			canvas.drawText(str, 100, 370 + (commentLines * 20) + xxx, paint);
			str = teamLineUpCurrent[3].length() > 2 ? String
					.valueOf(teamLineUpCurrent[3]) : String.valueOf(3) + ".";
			canvas.drawText(str, 300, 370 + (commentLines * 20) + xxx, paint);
			str = teamLineUpCurrent[2].length() > 2 ? String
					.valueOf(teamLineUpCurrent[2]) : String.valueOf(2) + ".";
			canvas.drawText(str, 500, 370 + (commentLines * 20) + xxx, paint);

			// Goal
			paint.setTextSize(15);
			paint.setColor(Color.RED);
			canvas.drawText("goal", 300, 405 + (commentLines * 20) + xxx, paint);
			paint.setTextSize(18);
			paint.setColor(Color.BLACK);
			str = teamLineUpCurrent[1].length() > 2 ? String
					.valueOf(teamLineUpCurrent[1]) : String.valueOf(1) + ".";
			canvas.drawText(str, 300, 425 + (commentLines * 20) + xxx, paint);

			paint.setTextSize(15);
			paint.setColor(Color.RED);
			canvas.drawText("GAA Scores Stats Plus - Android App", 300, 470
					+ (commentLines * 20) + xxx, paint);

			File mPath = Environment
					.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
			OutputStream fout = null;
			File imageFile = new File(mPath, "selTweet.jpg");
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
				shareIntent.putExtra(Intent.EXTRA_TEXT, panelName
						+ " Team Selection");
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

	private void getTeam(String teamName) {
		// load panel from database and assign to arraylist
		String[] projection = { TeamContentProvider.PANELID,
				TeamContentProvider.NAME, TeamContentProvider.POSN };
		CursorLoader cL;
		int posn;
		// reset line up and read from database
		for (int j = 1; j <= 15; j++) {
			teamLineUpCurrent[j] = null;
		}
		cL = new CursorLoader(getActivity(), allTitles, projection,
				TeamContentProvider.TEAM + " = '" + teamName + "'", null,
				TeamContentProvider.NAME);
		Cursor c1 = cL.loadInBackground();
		panelList.clear();
		playerIDLookUp.clear();
		if (c1.getCount() > 0) {
			c1.moveToFirst();
			do {
				// read in player nicknames
				panelList.add(c1.getString(c1
						.getColumnIndexOrThrow(TeamContentProvider.NAME)));
				// insert players into positions
				posn = c1.getInt(c1
						.getColumnIndexOrThrow(TeamContentProvider.POSN));
				if (posn > 0) {
					teamLineUpCurrent[posn] = c1.getString(c1
							.getColumnIndexOrThrow(TeamContentProvider.NAME));
				}

				posnList.add(c1.getInt(c1
						.getColumnIndexOrThrow(TeamContentProvider.POSN)));

				playerIDLookUp
						.put(c1.getString(c1
								.getColumnIndexOrThrow(TeamContentProvider.NAME)),
								c1.getInt(c1
										.getColumnIndexOrThrow(TeamContentProvider.PANELID)));
			} while (c1.moveToNext());

		}

		// remove from panellist names of players that are already selected and
		// assigned to a button onscreen
		for (int j = 1; j <= 15; j++) {
			if (panelList.indexOf(teamLineUpCurrent[j]) != -1) {
				panelList.remove(teamLineUpCurrent[j]);
			}
		}

		// assign default number to rest
		for (int j = 1; j <= 15; j++) {
			if (teamLineUpCurrent[j] == null) {
				teamLineUpCurrent[j] = String.valueOf(j);
			}
			bTeam[j].setText(teamLineUpCurrent[j]);
		}
		// insert SWAP into panelist in 1st position to facilitate position
		// changes and substitutions
		panelList.remove("...");
		panelList.add(0, "RESET POSITION TO NUMBER");
		panelList.add(0, "ENTER NEW PLAYER NAME");
		c1.close();
	}

	private void setButtons(View w) {
		// Set buttonlisteners and use position numbers as default team lineup
		for (int i = 1; i <= 15; i++) {
			// set listener on team buttons
			bTeam[i] = (Button) w.findViewById(getResources().getIdentifier(
					"ButtonP" + String.format("%02d", i), "id",
					"fm.gaa_scores.plus"));
			bTeam[i].setOnClickListener(teamSetupClickListener);
		}
	}

	OnClickListener recordSub = new OnClickListener() {
		@Override
		public void onClick(View v) {

			int txtButton = ((Button) v).getId();
			if (txtButton == R.id.bBlood) {
				bloodSub = true;
			}

			// set up panelist
			strTemp2 = new String[panelList.size() - 2];
			for (int i = 0; i < panelList.size() - 2; i++) {
				strTemp2[i] = panelList.get(i + 2);
			}
			strTemp = new String[15];
			for (int i = 1; i <= 15; i++) {
				strTemp[i - 1] = i + ": " + teamLineUpCurrent[i];
			}
			teamLineUpCurrent[0] = "0";
			// Get whois coming off swap with going on and write change to
			// databse
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle("select who is coming off");
			builder.setSingleChoiceItems(strTemp, 0,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							indexOff = which + 1;
							playerOff = teamLineUpCurrent[indexOff];
							// dialog to see who is goping on

							// Get whois coming off swap with going on and write
							// change to databse
							AlertDialog.Builder builder1 = new AlertDialog.Builder(
									getActivity());
							builder1.setTitle("select who is going on");
							builder1.setSingleChoiceItems(strTemp2, 0,
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											indexOn = which + 2;// text in first
																// 2
											playerOn = panelList.get(which + 2);
											makeSub(bloodSub);

											dialog.dismiss();
										}
									});
							AlertDialog alert1 = builder1.create();
							alert1.show();
							dialog.dismiss();
						}
					});
			AlertDialog alert = builder.create();
			alert.show();
		}
	};

	private void makeSub(boolean bloodSub) {
		// update database
		// Where a Player is not already selected
		// for team the button text will be just the
		// position number and so length < 3.
		// assign player to button/teamlineup and

		if (playerOff.length() < 3) {
			bTeam[indexOff].setText(playerOn);
			teamLineUpCurrent[indexOff] = playerOn;
			panelList.remove(playerOn);
			// write position to database
			ContentValues values = new ContentValues();
			values.put("posn", indexOff);
			Uri uri = Uri.parse(TeamContentProvider.CONTENT_URI + "/"
					+ playerIDLookUp.get(playerOn));
			getActivity().getContentResolver().update(uri, values, null, null);
		}
		// where Player already selected in position
		// need to swap. Assign new player to
		// button/teamlineup. Add swapped out player
		// back into panelList and Sort
		//
		else {
			bTeam[indexOff].setText(playerOn);
			teamLineUpCurrent[indexOff] = playerOn;
			panelList.remove(playerOn);
			panelList.add(playerOff);
			panelList.remove("RESET POSITION TO NUMBER");
			panelList.remove("ENTER NEW PLAYER NAME");
			Collections.sort(panelList);
			panelList.add(0, "RESET POSITION TO NUMBER");
			panelList.add(0, "ENTER NEW PLAYER NAME");
			// update position of selected player in database
			ContentValues values = new ContentValues();
			values.put("posn", indexOff);
			Uri uri = Uri.parse(TeamContentProvider.CONTENT_URI + "/"
					+ playerIDLookUp.get(playerOn));

			getActivity().getContentResolver().update(uri, values, null, null);
			// update position of removed player in database
			values = new ContentValues();
			values.put("posn", -1);
			uri = Uri.parse(TeamContentProvider.CONTENT_URI + "/"
					+ playerIDLookUp.get(playerOff));
			getActivity().getContentResolver().update(uri, values, null, null);
		}
		getTeam(panelName);
		// write to stats
		String temp = (((Startup) getActivity()).getFragmentScore().getTime() == "") ? ""
				: ((Startup) getActivity()).getFragmentScore().getTime()
						+ " mins "
						+ ((Startup) getActivity()).getFragmentScore().bPeriod
								.getText();
		ContentValues values = new ContentValues();
		String temp2 = (bloodSub) ? " blood sub " : " substitution ";
		values.put("line", temp + temp2 + panelName + "--> off: " + playerOff
				+ "  on: " + playerOn);
		getActivity().getContentResolver().insert(
				TeamContentProvider.CONTENT_URI_2, values);
		updateSubsList();
		((Startup) getActivity()).getFragmentScore().updateStatsList();
		((Startup) getActivity()).getFragmentReview().updateListView();
	}

	// reset team positions to numbers
	OnClickListener resetTeamListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// get reference to REVIEW fragment from parent activity
			// MatchApplication and use reference to execute resetStats
			// method in REVIEW fragment which will reset stats there to 0
			Toast.makeText(getActivity(), "Long Press to Reset",
					Toast.LENGTH_SHORT).show();
		}
	};

	private void resetTeam() {
		// Reset team lineup to default position numbers
		// and assign numbers ot buttons on screen
		for (int i = 1; i <= 15; i++) {
			teamLineUpCurrent[i] = String.valueOf(i);
			bTeam[i].setText(String.valueOf(i));
		}
		// Reset positions to -1 in database
		ContentValues values = new ContentValues();
		int count;
		values.put("posn", -1);
		// add to panel database
		count = getActivity().getContentResolver().update(
				TeamContentProvider.CONTENT_URI, values,
				TeamContentProvider.TEAM + " = '" + panelName + "'", null);

		// which will set team names and team lineup
	}

	// change name of current team
	OnClickListener changeNameListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			changeName();
		}
	};

	private void changeName() {
		AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
		input = new EditText(getActivity());
		input.setId(997);
		alert.setTitle("Enter New Team Name");
		alert.setMessage("Name:");
		alert.setView(input);
		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface indialog, int which) {
				String inName = input.getText().toString();
				if (inName.length() > 2) {
					// Update name in database
					ContentValues values = new ContentValues();
					int count;
					values.put("team", inName);
					// add to panel database
					count = getActivity().getContentResolver()
							.update(TeamContentProvider.CONTENT_URI,
									values,
									TeamContentProvider.TEAM + " = '"
											+ panelName + "'", null);
					// if team doesnt exist, create it
					if (count == 0) {
						values = new ContentValues();
						values.put("name", "...");
						values.put("posn", 0);
						values.put("team", inName);
						getActivity().getContentResolver().insert(
								TeamContentProvider.CONTENT_URI, values);
					}

					Toast.makeText(getActivity(), "panel renamed",
							Toast.LENGTH_LONG).show();
					// update title and panelname
					panelName = inName;
					tTeamHome.setText(panelName);
					SharedPreferences sharedPref = getActivity()
							.getSharedPreferences("team_stats_review_data",
									Context.MODE_PRIVATE);
					SharedPreferences.Editor editor = sharedPref.edit();
					editor.putString("OURTEAM", panelName);
					editor.commit();
					((Startup) getActivity()).getFragmentScore().setTeamLineUp(
							panelName, "");
					((Startup) getActivity()).getFragmentReview().setTeamNames(
							panelName, "");
					((Startup) getActivity()).getFragmentScorers()
							.setTeamNames(panelName, "");
					((Startup) getActivity()).getFragmentReview()
							.updateListView();
					((Startup) getActivity()).getFragmentScore()
							.updateStatsList();
					((Startup) getActivity()).getFragmentTeamTwo().setTeam(
							panelName);

				} else {
					Toast.makeText(
							getActivity(),
							"Invalid Name, Try Again\n"
									+ "Must be at least 3 characters long",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		alert.create();
		alert.show();
	}

	// create new team
	OnClickListener createNewTeamListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			createNewTeam();
		}
	};

	private void createNewTeam() {
		AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
		input = new EditText(getActivity());
		input.setId(996);
		alert.setTitle("Enter New Team Name");
		alert.setMessage("Name:");
		alert.setView(input);
		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface indialog, int which) {
				String inName = input.getText().toString();
				if (inName.length() > 2) {
					// Update name in database
					// Reset team lineup to default position numbers
					// and assign numbers ot buttons on screen
					for (int i = 1; i <= 15; i++) {
						teamLineUpCurrent[i] = String.valueOf(i);
						bTeam[i].setText(String.valueOf(i));
					}
					panelName = inName;
					tTeamHome.setText(panelName);
					panelList.clear();
					panelList.add(0, "RESET POSITION TO NUMBER");
					panelList.add(0, "ENTER NEW PLAYER NAME");
					playerIDLookUp.clear();
					// add to database
					ContentValues values = new ContentValues();
					values.put("name", "...");
					values.put("posn", 0);
					values.put("team", panelName);
					getActivity().getContentResolver().insert(
							TeamContentProvider.CONTENT_URI, values);

					// update other fragments
					SharedPreferences sharedPref = getActivity()
							.getSharedPreferences("team_stats_review_data",
									Context.MODE_PRIVATE);
					SharedPreferences.Editor editor = sharedPref.edit();
					editor.putString("OURTEAM", panelName);
					editor.commit();
					((Startup) getActivity()).getFragmentScore().setTeamLineUp(
							panelName, "");
					((Startup) getActivity()).getFragmentReview().setTeamNames(
							panelName, "");
					((Startup) getActivity()).getFragmentScorers()
							.setTeamNames(panelName, "");
					((Startup) getActivity()).getFragmentTeamTwo().setTeam(
							panelName);

				} else {
					Toast.makeText(
							getActivity(),
							"Invalid Name, Try Again\n"
									+ "Must be at least 3 characters long",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		alert.create();
		alert.show();
	}

	// Load existing team
	OnClickListener loadTeamListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// get list of team names
			loadTeam();
		}
	};

	private void loadTeam() {
		ArrayList<String> panelTeam = new ArrayList<String>();
		String str;
		String[] projection = { TeamContentProvider.TEAM };
		CursorLoader cL = new CursorLoader(getActivity(), allTitles,
				projection, null, null, TeamContentProvider.TEAM);
		Cursor c1 = cL.loadInBackground();
		if (c1.getCount() > 0) {
			c1.moveToFirst();
			do {
				str = c1.getString(c1
						.getColumnIndexOrThrow(TeamContentProvider.TEAM));
				if (!panelTeam.contains(str))
					panelTeam.add(str);
			} while (c1.moveToNext());
		}
		// take out team in other page
		panelTeam.remove(oppTeamName);
		if (panelTeam.size() > 0) {
			panel = new String[panelTeam.size()];
			for (int i = 0; i < panelTeam.size(); i++) {
				panel[i] = panelTeam.get(i);
			}
			c1.close();
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle("select team to load");
			builder.setSingleChoiceItems(panel, 0,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							panelName = panel[which];
							tTeamHome.setText(panelName);
							for (int j = 1; j <= 15; j++) {
								teamLineUpCurrent[j] = String.valueOf(j);
								bTeam[j].setText(teamLineUpCurrent[j]);
							}

							getTeam(panelName);
							SharedPreferences sharedPref = getActivity()
									.getSharedPreferences(
											"team_stats_review_data",
											Context.MODE_PRIVATE);
							SharedPreferences.Editor editor = sharedPref.edit();
							editor.putString("OURTEAM", panelName);
							editor.commit();

							((Startup) getActivity()).getFragmentScore()
									.setTeamLineUp(panelName, "");
							((Startup) getActivity()).getFragmentReview()
									.setTeamNames(panelName, "");
							((Startup) getActivity()).getFragmentScorers()
									.setTeamNames(panelName, "");
							((Startup) getActivity()).getFragmentTeamTwo()
									.setTeam(panelName);
							dialog.dismiss();
						}
					});
			AlertDialog alert = builder.create();
			alert.show();
		} else {
			// error no teams available
			Toast.makeText(getActivity(), "There are no saved teams to load",
					Toast.LENGTH_SHORT).show();
		}
	}

	// delete player
	OnClickListener deletePlayerListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			deletePlayer();
		}
	};

	private void deletePlayer() {
		// get list of player names
		ArrayList<String> panelList = new ArrayList<String>();
		String[] projection = { TeamContentProvider.NAME };
		CursorLoader cL = new CursorLoader(getActivity(), allTitles,
				projection,
				TeamContentProvider.TEAM + " = '" + panelName + "'", null,
				TeamContentProvider.NAME);
		Cursor c1 = cL.loadInBackground();
		if (c1.getCount() > 0) {
			c1.moveToFirst();
			do {
				panelList.add(c1.getString(c1
						.getColumnIndexOrThrow(TeamContentProvider.NAME)));
			} while (c1.moveToNext());
			panelList.remove("...");
			panel = new String[panelList.size()];
			for (int i = 0; i < panelList.size(); i++) {
				panel[i] = panelList.get(i);
			}
			c1.close();
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle("select player to delete");
			builder.setSingleChoiceItems(panel, 0,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							player = panel[which];
							getActivity()
									.getContentResolver()
									.delete(Uri
											.parse(TeamContentProvider.CONTENT_URI
													+ "/"
													+ playerIDLookUp
															.get(player)),
											null, null);

							getTeam(panelName);
							dialog.dismiss();
						}
					});
			AlertDialog alert = builder.create();
			alert.show();
		} else {
			// error no teams available
			Toast.makeText(getActivity(), "There are no players to delete",
					Toast.LENGTH_SHORT).show();
		}
	}

	// delete team
	OnClickListener deleteTeamListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// get list of player names
			deleteTeam();
		}
	};

	private void deleteTeam() {
		ArrayList<String> panelList = new ArrayList<String>();
		String str;
		String[] projection = { TeamContentProvider.TEAM };
		CursorLoader cL = new CursorLoader(getActivity(), allTitles,
				projection, null, null, TeamContentProvider.TEAM);
		Cursor c1 = cL.loadInBackground();
		if (c1.getCount() > 0) {
			c1.moveToFirst();
			do {
				str = c1.getString(c1
						.getColumnIndexOrThrow(TeamContentProvider.TEAM));
				if (!panelList.contains(str))
					panelList.add(str);
			} while (c1.moveToNext());
		}
		// don't delete current so remove from list
		panelList.remove(panelName);
		panelList.remove(oppTeamName);
		if (panelList.size() > 0) {
			panel = new String[panelList.size()];
			for (int i = 0; i < panelList.size(); i++) {
				panel[i] = panelList.get(i);
			}
			c1.close();
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle("select team to delete");
			builder.setSingleChoiceItems(panel, 0,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							int count;
							team = panel[which];
							count = getActivity().getContentResolver().delete(
									TeamContentProvider.CONTENT_URI,
									TeamContentProvider.TEAM + " = '" + team
											+ "'", null);
							Toast.makeText(
									getActivity(),
									team + " and " + count + " players deleted",
									Toast.LENGTH_LONG).show();

							dialog.dismiss();
						}
					});
			AlertDialog alert = builder.create();
			alert.show();
		} else {
			// error no teams available
			Toast.makeText(
					getActivity(),
					"There are no teams to delete \n\n"
							+ "note: you can't delete teams\n which are currently loaded",
					Toast.LENGTH_LONG).show();
		}
	}

	// Listener to select team lineup
	OnClickListener teamSetupClickListener = new OnClickListener() {
		@Override
		public void onClick(View w) {
			b = (Button) w;
			ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(
					getActivity(), R.layout.single_row_layout, panelList);
			new AlertDialog.Builder(getActivity())
					.setTitle("select player")
					.setAdapter(adapter1,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// find which position number button has
									// been clicked
									ContentValues values;
									Uri uri;
									String posnNo = getResources()
											.getResourceName(b.getId());
									index = Integer.parseInt(posnNo.substring(
											posnNo.length() - 2,
											posnNo.length()));
									//
									// Deal with Enter New Player
									if (which == 0) {
										// enter new player dialog

										// set up dialog to get filename use
										// edittext in an alertdialog to
										// Prompt for filename
										AlertDialog.Builder alert = new AlertDialog.Builder(
												getActivity());
										input = new EditText(getActivity());
										input.setId(999);
										alert.setTitle("enter name of new player");
										alert.setMessage("Enter Name:");
										alert.setView(input);
										alert.setPositiveButton(
												"OK",
												new DialogInterface.OnClickListener() {
													@Override
													public void onClick(
															DialogInterface indialog,
															int which) {
														String inName = input
																.getText()
																.toString();
														if (inName.length() > 2) {
															// write to database
															ContentValues values = new ContentValues();
															values.put("name",
																	inName);
															values.put("posn",
																	index);
															values.put("team",
																	panelName);
															getActivity()
																	.getContentResolver()
																	.insert(TeamContentProvider.CONTENT_URI,
																			values);
															// write to teamlist
															teamLineUpCurrent[index] = inName;
															// write to button
															if (b.getText()
																	.length() < 3) {
																b.setText(inName);
															} else {
																String s = (String) b
																		.getText();
																b.setText(inName);
																panelList
																		.add(s);
																panelList
																		.remove("RESET POSITION TO NUMBER");
																panelList
																		.remove("ENTER NEW PLAYER NAME");
																Collections
																		.sort(panelList);
																panelList
																		.add(0,
																				"RESET POSITION TO NUMBER");
																panelList
																		.add(0,
																				"ENTER NEW PLAYER NAME");
																// update
																// position of
																// removed
																// player in
																// database
																values = new ContentValues();
																values.put(
																		"posn",
																		-1);
																getActivity()
																		.getContentResolver()
																		.update(Uri
																				.parse(TeamContentProvider.CONTENT_URI
																						+ "/"
																						+ playerIDLookUp
																								.get(s)),
																				values,
																				null,
																				null);
															}
															getTeam(panelName);
														} else {
															Toast.makeText(
																	getActivity(),
																	"Invalid Name, Try Again\n"
																			+ "Must be at least 3 characters long",
																	Toast.LENGTH_SHORT)
																	.show();
														}
													}
												});
										alert.create();
										alert.show();
									}

									// Deal with reset
									else if (which == 1) {
										// if its just the number do nothing
										if (b.getText().length() > 2) {
											String s = (String) b.getText();
											b.setText(String.valueOf(index));
											teamLineUpCurrent[index] = String
													.valueOf(index);
											panelList.add(s);
											panelList
													.remove("RESET POSITION TO NUMBER");
											panelList
													.remove("ENTER NEW PLAYER NAME");
											Collections.sort(panelList);
											panelList.add(0,
													"RESET POSITION TO NUMBER");
											panelList.add(0,
													"ENTER NEW PLAYER NAME");
											values = new ContentValues();
											values.put("posn", -1);
											getActivity()
													.getContentResolver()
													.update(Uri.parse(TeamContentProvider.CONTENT_URI
															+ "/"
															+ playerIDLookUp
																	.get(s)),
															values, null, null);

										}
									}

									// Where a Player is not already selected
									// for team the button text will be just the
									// position number and so length < 3.
									// assign player to button/teamlineup and
									// remove from panelList
									else if (b.getText().length() < 3) {
										b.setText(panelList.get(which));
										teamLineUpCurrent[index] = panelList
												.get(which);
										panelList.remove(which);
										// write position to database
										values = new ContentValues();
										values.put("posn", index);
										uri = Uri.parse(TeamContentProvider.CONTENT_URI
												+ "/"
												+ playerIDLookUp
														.get(teamLineUpCurrent[index]));
										getActivity()
												.getContentResolver()
												.update(uri, values, null, null);

									}
									// where Player already selected in position
									// need to swap. Assign new player to
									// button/teamlineup. Add swapped out player
									// back into panelList and Sort
									//
									else {
										String s = (String) b.getText();
										b.setText(panelList.get(which));
										teamLineUpCurrent[index] = panelList
												.get(which);
										panelList.remove(which);
										panelList.add(s);
										panelList
												.remove("RESET POSITION TO NUMBER");
										panelList
												.remove("ENTER NEW PLAYER NAME");
										Collections.sort(panelList);
										panelList.add(0,
												"RESET POSITION TO NUMBER");
										panelList.add(0,
												"ENTER NEW PLAYER NAME");
										// update position of selected player in
										// database
										values = new ContentValues();
										values.put("posn", index);
										uri = Uri.parse(TeamContentProvider.CONTENT_URI
												+ "/"
												+ playerIDLookUp
														.get(teamLineUpCurrent[index]));
										getActivity()
												.getContentResolver()
												.update(uri, values, null, null);
										// update position of removed player in
										// database
										values = new ContentValues();
										values.put("posn", -1);
										uri = Uri
												.parse(TeamContentProvider.CONTENT_URI
														+ "/"
														+ playerIDLookUp.get(s));
										getActivity()
												.getContentResolver()
												.update(uri, values, null, null);
									}
									dialog.dismiss();
								}
							}).create().show();
		}
	};

	public void updateCards() {
		Uri allTitles = TeamContentProvider.CONTENT_URI_2;
		String[] projection = { TeamContentProvider.STATSID,
				TeamContentProvider.STATSLINE };
		CursorLoader cL;
		StringBuilder strBuilder = new StringBuilder();
		cL = new CursorLoader(getActivity(), allTitles, projection, null, null,
				TeamContentProvider.STATSID);
		Cursor c1 = cL.loadInBackground();
		if (c1.getCount() > 0) {
			String str[] = new String[c1.getCount()];
			int i = 0;
			c1.moveToFirst();
			do {
				// insert players into positions
				str[i] = c1.getString(c1
						.getColumnIndexOrThrow(TeamContentProvider.STATSLINE));
				if (((str[i].indexOf("red card") >= 0)
						|| (str[i].indexOf("black card") >= 0) || (str[i]
						.indexOf("yellow card") >= 0))
						&& (str[i].indexOf(tTeamHome.getText().toString()) >= 0)) {
					strBuilder.append("\n" + str[i]);
				}
				i++;
			} while (c1.moveToNext());
			// ermove leading line feed
			strBuilder.delete(0, 1);
			tCards.setText(strBuilder.toString());
		}
	}

	public void updateSubsList() {
		Uri allTitles = TeamContentProvider.CONTENT_URI_2;
		String[] projection = { TeamContentProvider.STATSID,
				TeamContentProvider.STATSLINE };
		CursorLoader cL;
		StringBuilder strBuilder = new StringBuilder();
		cL = new CursorLoader(getActivity(), allTitles, projection, null, null,
				TeamContentProvider.STATSID);
		Cursor c1 = cL.loadInBackground();
		if (c1.getCount() > 0) {
			String str[] = new String[c1.getCount()];
			int i = 0;
			c1.moveToFirst();
			do {
				// insert players into positions
				str[i] = c1.getString(c1
						.getColumnIndexOrThrow(TeamContentProvider.STATSLINE));
				if ((str[i].indexOf("--> off:") >= 0)
						&& (str[i].indexOf(panelName) >= 0)) {
					strBuilder.append("\n" + str[i]);
				}
				i++;
			} while (c1.moveToNext());
			// ermove leading line feed
			strBuilder.delete(0, 1);
			tSubs.setText(strBuilder.toString());
		}
	}

	public void setTeam(String team) {
		oppTeamName = team;
	}

	public void importTeam() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("file/plain");
		startActivityForResult(intent, 1);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != 0) {
			Log.e("impotteam", "+" + data.getData().getPath());
			readTeam(data.getData().getPath());
		}
	}

	public void readTeam(String fname) {
		StringBuffer buf = new StringBuffer();

		try {
			FileInputStream fileStream = new FileInputStream(fname);
			InputStreamReader inStreamReader = new InputStreamReader(fileStream);
			String str = "";
			BufferedReader reader = new BufferedReader(inStreamReader);
			if (inStreamReader != null) {
				while ((str = reader.readLine()) != null) {
					buf.append(str);
				}
			}
			// create team name

			// check if format is correct
			if ((buf.toString().toLowerCase().startsWith("teamstart,"))
					&& (buf.toString().toLowerCase().endsWith(",teamend"))) {
				// good to go
				// chop off start and end
				String strTemp[] = buf.toString()
						.substring(10, buf.toString().length() - 8)
						.split(",", -1);
				int inputNum = strTemp.length;
				// check for name
				if ((strTemp[strTemp.length - 1].toLowerCase()
						.startsWith("teamname:"))
						&& (strTemp[strTemp.length - 1].split(":").length == 2)) {
					panelName = strTemp[strTemp.length - 1].split(":")[1];
					inputNum = inputNum - 1;
				} else {
					SimpleDateFormat sdf = new SimpleDateFormat("HHmmddMMyyyy");
					Date date = new Date(System.currentTimeMillis());
					panelName = "team." + sdf.format(date);
				}

				tTeamHome.setText(panelName);
				panelList.clear();
				panelList.add(0, "RESET POSITION TO NUMBER");
				panelList.add(0, "ENTER NEW PLAYER NAME");
				playerIDLookUp.clear();
				// add to database
				ContentValues values = new ContentValues();
				values.put("name", "...");
				values.put("posn", 0);
				values.put("team", panelName);
				getActivity().getContentResolver().insert(
						TeamContentProvider.CONTENT_URI, values);
				SharedPreferences sharedPref = getActivity()
						.getSharedPreferences("team_stats_review_data",
								Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = sharedPref.edit();
				editor.putString("OURTEAM", panelName);
				editor.commit();

				((Startup) getActivity()).getFragmentScore().setTeamLineUp(
						panelName, "");
				((Startup) getActivity()).getFragmentReview().setTeamNames(
						panelName, "");
				((Startup) getActivity()).getFragmentScorers().setTeamNames(
						panelName, "");
				((Startup) getActivity()).getFragmentTeamTwo().setTeam(
						panelName);

				String s[] = new String[inputNum];
				for (int i = 0; i < inputNum; i++) {
					s[i] = strTemp[i];
				}

				// if more than 15 read in
				if (s.length > 15) {
					for (int i = 0; i < 15; i++) {
						if (s[i].length() > 2) {
							values = new ContentValues();
							values.put("name", s[i]);
							values.put("posn", String.valueOf(i + 1));
							values.put("team", panelName);
							getActivity().getContentResolver().insert(
									TeamContentProvider.CONTENT_URI, values);
						}
					}
					for (int i = 15; i < s.length; i++) {
						if (s[i].length() > 2) {
							values = new ContentValues();
							values.put("name", s[i]);
							values.put("posn", -1);
							values.put("team", panelName);
							getActivity().getContentResolver().insert(
									TeamContentProvider.CONTENT_URI, values);
						}
					}
				} else {
					// less than or equal to 15
					for (int i = 0; i < s.length; i++) {
						if (s[i].length() > 2) {
							values = new ContentValues();
							values.put("name", s[i]);
							values.put("posn", String.valueOf(i + 1));
							values.put("team", panelName);
							getActivity().getContentResolver().insert(
									TeamContentProvider.CONTENT_URI, values);
						}
					}
				}
				getTeam(panelName);
			} else {
				Log.e("file format", "wrong file format");
				Toast.makeText(getActivity(), "file format is wrong",
						Toast.LENGTH_LONG).show();

			}

		} catch (IOException e) {
			Log.e("file read failed", e.getMessage(), e);
			Toast.makeText(getActivity(), "unable to read file",
					Toast.LENGTH_LONG).show();
		}
	}

	public void downloadTeam() {
		try {
			List<File> fileList = new ArrayList<File>();
			File root = new File(Environment.getExternalStoragePublicDirectory(
					Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
			// list files in directory
			// filter only ones starting with appGAASCORESSTATS
			// then get most recent one
			File[] files = root.listFiles();
			for (File f : files) {
				if (f.getName().length() >= 18) {
					if (f.getName().substring(0, 17)
							.equals("appGAASCORESSTATS")) {
						fileList.add(f);
					}
				}
			}
			if (fileList.size() > 0) {

				String fName = fileList.get(0).getPath();
				long datemod = fileList.get(0).lastModified();
				for (int i = 1; i < fileList.size(); i++) {
					if (fileList.get(i).lastModified() > datemod) {
						fName = fileList.get(i).getPath();
					}
				}
				// ///////////read in team
				readTeam(fName);
				// // wait a second to read file then delete files
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						Log.v("start file delete", "OK");
						deleteDownloads();
					}
				}, 5000);

			} else {
				Toast.makeText(
						getActivity(),
						"Can't find downloaded file.\nTry downloading file in Twitter @gaaapps again\nor try ? HELP screen for suggestions",
						Toast.LENGTH_LONG).show();
			}
		}

		catch (Exception ex) {
			Toast.makeText(
					getActivity(),
					"Can't find downloaded file.\nHave a look in ? HELP screen"
							+ "for suggestions ", Toast.LENGTH_LONG).show();
		}
	}

	public void deleteDownloads() {
		try {
			List<File> fileList = new ArrayList<File>();
			File root = new File(Environment.getExternalStoragePublicDirectory(
					Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
			// list files in directory
			// filter only ones starting with appGAASCORESSTATS
			// then get most recent one
			File[] files = root.listFiles();
			for (File f : files) {
				if (f.getName().length() >= 18) {
					if (f.getName().substring(0, 17)
							.equals("appGAASCORESSTATS")) {
						Log.v("deleting", f.getName());
						f.delete();
						Log.v("file deleted", "OK");
					}
				}
			}
		}

		catch (Exception ex) {
			Log.e("error with deleting downloads", ex.toString());
		}
	}

	public void resetCardsSubs() {
		tCards.setText("");
		tSubs.setText("");
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		inflater.inflate(R.menu.team1_menu, menu);
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
			ihelp.putExtra("HELP_ID", R.string.teamHelp);
			startActivity(ihelp);
			return true;
		case R.id.deletePlayer:
			deletePlayer();
			return true;
		case R.id.createNewTeam:
			createNewTeam();
			return true;
		case R.id.loadSavedTeam:
			loadTeam();
			return true;
		case R.id.deleteTeam:
			deleteTeam();
			return true;
		case R.id.resetTeam:
			resetTeam();
			return true;
		case R.id.downloadTeam:
			downloadTeam();
			return true;
		case R.id.importTeam:
			importTeam();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
