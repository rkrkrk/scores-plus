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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
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
	private int index, indexOff, indexOn, sub = 0, subLines = 0, cardLines = 0;
	private boolean bloodSub = false;
	private StringBuilder strBuilderSub = new StringBuilder();
	private StringBuilder strBuilderCards = new StringBuilder();

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

		//
		Button bButtonReset = (Button) v.findViewById(R.id.button_setup_reset);
		bButtonReset.setOnClickListener(resetTeamListener);
		Button bSub = (Button) v.findViewById(R.id.bSub);
		bSub.setOnClickListener(recordSub);
		Button bBlood = (Button) v.findViewById(R.id.bBlood);
		bBlood.setOnClickListener(recordSub);
		Button bViewSubs = (Button) v.findViewById(R.id.bViewSubs);
		bViewSubs.setOnClickListener(viewSubs);
		Button bButtonChange = (Button) v.findViewById(R.id.homeTeam);
		bButtonChange.setOnClickListener(changeNameListener);
		// read persisted stored data to set up screen on restart
		SharedPreferences sharedPref = getActivity().getSharedPreferences(
				"home_team_data", Context.MODE_PRIVATE);

		// setup input edittext boxes
		panelName = sharedPref.getString("PANELNAME", "OWN TEAM");
		sharedPref = getActivity().getSharedPreferences("opp_team_data",
				Context.MODE_PRIVATE);
		oppTeamName = sharedPref.getString("PANELNAME", "OPPOSITION");
		tTeamHome.setText(panelName);
		setButtons(v);
		getTeam(panelName);

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
		Button bSelCard = (Button) v.findViewById(R.id.sel_cards);
		bSelCard.setOnClickListener(selTweetListener);
		Button bSelText = (Button) v.findViewById(R.id.sel_text);
		bSelText.setOnClickListener(selTextListener);
		Button bSelShare = (Button) v.findViewById(R.id.sel_share);
		bSelShare.setOnClickListener(selShareListener);
		Button bSaveSel = (Button) v.findViewById(R.id.bSaveSelection);
		bSaveSel.setOnClickListener(selShareListener);

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
		File root, outfile;

		@Override
		public void onClick(View v) {
			int txtButton = ((Button) v).getId();

			// Create Bitmap to display team selection
			StringBuilder sb = new StringBuilder("");
			sb.append(panelName + " v. " + oppTeamName + ". \n");
			sb.append(((Startup) getActivity()).getFragmentScore().getLocText()
					+ "\n ");
			String str1 = ((Startup) getActivity()).getFragmentScore()
					.getScore(true);
			sb.append(str1 + panelName + " team selection:\n\n ");

			sb.append("GK: "
					+ (teamLineUpCurrent[1].length() > 2 ? String
							.valueOf(teamLineUpCurrent[1]) + "\n " : " 01\n "));
			sb.append("RFB: "
					+ (teamLineUpCurrent[2].length() > 2 ? String
							.valueOf(teamLineUpCurrent[2]) + "\n " : " 02\n "));
			sb.append("FB: "
					+ (teamLineUpCurrent[3].length() > 2 ? String
							.valueOf(teamLineUpCurrent[3]) + "\n " : " 03\n "));
			sb.append("LFB: "
					+ (teamLineUpCurrent[4].length() > 2 ? String
							.valueOf(teamLineUpCurrent[4]) + "\n " : " 04\n "));
			sb.append("RHB: "
					+ (teamLineUpCurrent[5].length() > 2 ? String
							.valueOf(teamLineUpCurrent[5]) + "\n " : " 05\n "));
			sb.append("CB: "
					+ (teamLineUpCurrent[6].length() > 2 ? String
							.valueOf(teamLineUpCurrent[6]) + "\n " : " 06\n "));
			sb.append("LHB: "
					+ (teamLineUpCurrent[7].length() > 2 ? String
							.valueOf(teamLineUpCurrent[7]) + "\n " : " 07\n "));
			sb.append("MF: "
					+ (teamLineUpCurrent[8].length() > 2 ? String
							.valueOf(teamLineUpCurrent[8]) + "\n " : " 08\n "));
			sb.append("MF: "
					+ (teamLineUpCurrent[9].length() > 2 ? String
							.valueOf(teamLineUpCurrent[9]) + "\n " : " 09\n "));
			sb.append("RHF: "
					+ (teamLineUpCurrent[10].length() > 2 ? String
							.valueOf(teamLineUpCurrent[10]) + "\n " : " 10\n "));
			sb.append("CF: "
					+ (teamLineUpCurrent[11].length() > 2 ? String
							.valueOf(teamLineUpCurrent[11]) + "\n " : " 11\n "));
			sb.append("LHF: "
					+ (teamLineUpCurrent[12].length() > 2 ? String
							.valueOf(teamLineUpCurrent[12]) + "\n " : " 12\n "));
			sb.append("RFF: "
					+ (teamLineUpCurrent[13].length() > 2 ? String
							.valueOf(teamLineUpCurrent[13]) + "\n " : " 13\n "));
			sb.append("FF: "
					+ (teamLineUpCurrent[14].length() > 2 ? String
							.valueOf(teamLineUpCurrent[14]) + "\n " : " 14\n "));
			sb.append("LFF: "
					+ (teamLineUpCurrent[15].length() > 2 ? String
							.valueOf(teamLineUpCurrent[15]) + "\n " : " 15\n "));

			if (strBuilderSub.length() > 1) {
				sb.append("\nSUBS USED\n");

				String[] subArray = strBuilderSub.toString().split("\n");
				for (int i = 0; i < subArray.length; i++) {
					sb.append(subArray[i] + "\n");
				}
			}
			if (strBuilderCards.length() > 1) {
				sb.append("\nCARDS\n");

				String[] subArray = strBuilderCards.toString().split("\n");
				for (int i = 0; i < subArray.length; i++) {
					sb.append(subArray[i] + "\n");
				}
			}

			if (txtButton == R.id.sel_share) {
				try {
					root = new File(Environment.getExternalStorageDirectory(),
							"gaa_app_sysfiles");
					if (!root.exists()) {
						root.mkdirs();
					}
					outfile = new File(root, "GAAScoresStatsTeam1.txt");
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

				Bitmap bitmap = createBitmap(subLines, cardLines,
						R.id.sel_cards);

				OutputStream fout = null;
				File imageFile = new File(root,
						"GAAScoresStatsTeamSelection.jpg");
				Uri uri = Uri.fromFile(imageFile);
				try {
					root.mkdirs();
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
				emailIntent.putExtra(Intent.EXTRA_SUBJECT, "match report "
						+ ((Startup) getActivity()).getFragmentScore()
								.getLocText());
				emailIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
				emailIntent.setType("text/plain");
				String[] emailAttachments = new String[] { Environment
						.getExternalStorageDirectory()
						+ "/gaa_app_sysfiles/"
						+ "GAAScoresStatsTeam1.txt" };
				// put email attachments into an ArrayList
				ArrayList<Uri> uris = new ArrayList<Uri>();
				for (String file : emailAttachments) {
					File uriFiles = new File(file);
					Uri u = Uri.fromFile(uriFiles);
					uris.add(u);
				}
				uris.add(uri);
				emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,
						uris);
				startActivity(Intent.createChooser(emailIntent, "Share Using:"));
			}

			else if (txtButton == R.id.bSaveSelection) {
				File dir = new File(Environment.getExternalStorageDirectory(),
						"gaa_app_sysfiles");
				if (!dir.exists()) {
					dir.mkdirs();
				}
				File files[] = dir.listFiles();
				int fileNum = 0;
				for (int i = 0; i < files.length; i++) {
					if (files[i].getName().contains("GAAScoresStatsTeam_")
							|| files[i].getName().contains(
									"GAAScoresStatsTeamSelection_")) {
						String str = files[i].getName().substring(
								files[i].getName().length() - 6,
								files[i].getName().length() - 4);
						if (Integer.parseInt(str) > fileNum) {
							fileNum = Integer.parseInt(str);
						}
					}
				}
				try {
					outfile = new File(dir, "GAAScoresStatsTeam_"
							+ String.format("%02d", fileNum + 1) + ".txt");
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

				Bitmap bitmap = createBitmap(subLines, cardLines,
						R.id.sel_cards);
				OutputStream fout = null;
				File imageFile = new File(dir, "GAAScoresStatsTeamSelection_"
						+ String.format("%02d", fileNum + 1) + ".jpg");
				Uri uri = Uri.fromFile(imageFile);
				try {
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
				Toast.makeText(getActivity(), "team selection saved",
						Toast.LENGTH_SHORT).show();
			}
		}
	};

	// text team selection
	//
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

	public Bitmap createBitmap(int subLinesThis, int cardLinesThis,
			int txtButton) {
		// Create Bitmap to display team selection
		Bitmap bitmap = Bitmap.createBitmap(600, 560 + (subLinesThis * 20)
				+ (cardLinesThis * 20), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawColor(Color.rgb(204, 255, 204));
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
		StaticLayout mTextLayout = new StaticLayout(((Startup) getActivity())
				.getFragmentScore().getLocText(), mTextPaint,
				canvas.getWidth(), Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
		int commentLines = mTextLayout.getLineCount();
		canvas.save();
		canvas.translate(10, 35);
		mTextLayout.draw(canvas);
		canvas.restore();

		paint.setTextAlign(Align.CENTER);
		paint.setTextSize(22);
		String str;
		String str1 = ((Startup) getActivity()).getFragmentScore().getScore(
				true);
		canvas.drawText(str1 + panelName + " team selection", 300,
				80 + (commentLines * 20), paint);

		int xxx = 5;
		// Full Forwards
		paint.setTextSize(15);
		paint.setColor(Color.RED);
		canvas.drawText("left corner forward", 100, 130 + (commentLines * 20)
				+ xxx, paint);
		canvas.drawText("full forward", 300, 130 + (commentLines * 20) + xxx,
				paint);
		canvas.drawText("right corner forward", 500, 130 + (commentLines * 20)
				+ xxx, paint);
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
		canvas.drawText("center forward", 300, 185 + (commentLines * 20) + xxx,
				paint);
		canvas.drawText("right half forward", 500, 185 + (commentLines * 20)
				+ xxx, paint);
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
		canvas.drawText("left half back", 100, 295 + (commentLines * 20) + xxx,
				paint);
		canvas.drawText("center back", 300, 295 + (commentLines * 20) + xxx,
				paint);
		canvas.drawText("right half back", 500,
				295 + (commentLines * 20) + xxx, paint);
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

		int subsCount = 0;
		if (txtButton == R.id.sel_cards) {
			subsCount = 1;
			paint.setTextAlign(Align.LEFT);
			paint.setTextSize(16);
			if (strBuilderSub.length() > 1) {
				canvas.drawText("SUBS USED", 5,
						450 + (commentLines * 20) + xxx, paint);
				paint.setTextSize(14);
				String[] subArray = strBuilderSub.toString().split("\n");
				for (int i = 0; i < subArray.length; i++) {
					canvas.drawText(subArray[i], 5, 470 + (commentLines * 20)
							+ xxx + (i * 20), paint);
					subsCount++;
				}
			}
			if (strBuilderCards.length() > 1) {
				canvas.drawText("CARDS", 5, 470 + (commentLines * 20) + xxx
						+ (subsCount * 20), paint);
				paint.setTextSize(14);
				String[] subArray = strBuilderCards.toString().split("\n");
				for (int i = 0; i < subArray.length; i++) {
					canvas.drawText(subArray[i], 5, 490 + (commentLines * 20)
							+ xxx + (i * 20) + (subsCount * 20), paint);
				}
			}
		}

		paint.setTextSize(15);
		paint.setTextAlign(Align.CENTER);
		paint.setColor(Color.GRAY);
		canvas.drawText("GAA Scores Stats Plus - Android App", 300, 490
				+ (subLinesThis * 20) + (cardLinesThis * 20)
				+ (commentLines * 20) + xxx, paint);
		canvas.drawText("Available free from Google Play Store", 300, 490
				+ (subLinesThis * 20) + (cardLinesThis * 20)
				+ (commentLines * 20) + xxx + 20, paint);
		return bitmap;
	}

	// tweet team selection
	// write selection to bitmal and tweet bitmap
	OnClickListener selTweetListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			int txtButton = ((Button) v).getId(), subLinesThis = 0, cardLinesThis = 0;
			if (txtButton == R.id.sel_cards) {
				subLinesThis = subLines;
				cardLinesThis = cardLines;
			}
			Bitmap bitmap = createBitmap(subLinesThis, cardLinesThis, txtButton);
			File mPath = Environment
					.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
			OutputStream fout = null;
			File imageFile = new File(mPath, "GAAScoresStatsSelectionTweet.jpg");
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
						+ " Team Selection \n"
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
		cL = new CursorLoader(getActivity(), allTitles, projection, "team=?",
				new String[] { teamName }, TeamContentProvider.NAME);
		// cL = new CursorLoader(getActivity(), allTitles, projection,
		// TeamContentProvider.TEAM + " = '" + teamName + "'", null,
		// TeamContentProvider.NAME);
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

	OnClickListener viewSubs = new OnClickListener() {
		@Override
		public void onClick(View v) {
			updateCards();
			updateSubsList();

			// Intent iSubs;

			Intent iSubs = new Intent(getActivity(), ListSubsCards.class);
			iSubs.putExtra("CARDS", strBuilderCards.toString());
			iSubs.putExtra("SUBS", strBuilderSub.toString());
			startActivity(iSubs);

		}
	};

	OnClickListener recordSub = new OnClickListener() {
		@Override
		public void onClick(View v) {

			int txtButton = ((Button) v).getId();
			bloodSub = false;
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
			builder.setTitle("select who is going on");
			builder.setSingleChoiceItems(strTemp2, 0,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							indexOn = which + 2;// text in first 2
							playerOn = panelList.get(which + 2);
							// dialog to see who is going on

							// Get whois coming off swap with going on and write
							// change to databse
							AlertDialog.Builder builder1 = new AlertDialog.Builder(
									getActivity());
							builder1.setTitle("select who is coming off");
							builder1.setSingleChoiceItems(strTemp, 0,
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											indexOff = which + 1;
											playerOff = teamLineUpCurrent[indexOff];
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

	public void undoSub(String toComeOff, String toGoOn) {
		try {
			int posnIndex = 0, panelListIndex = 0;
			for (int i = 1; i < teamLineUpCurrent.length; i++) {
				if (teamLineUpCurrent[i].equals(toComeOff)) {
					posnIndex = i;
					break;
				}
			}
			if (toGoOn.length() >= 3) {
				panelListIndex = panelList.indexOf(toGoOn);
			}
			if (posnIndex > 0 && panelListIndex >= 0) {
				panelList.add(toComeOff);
				ContentValues values = new ContentValues();
				values = new ContentValues();
				values.put("posn", -1);
				Uri uri = Uri.parse(TeamContentProvider.CONTENT_URI + "/"
						+ playerIDLookUp.get(toComeOff));
				getActivity().getContentResolver().update(uri, values, null,
						null);
				if (toGoOn.length() < 3) {
					teamLineUpCurrent[posnIndex] = String.valueOf(posnIndex);
					bTeam[posnIndex].setText(teamLineUpCurrent[posnIndex]);
				} else {
					teamLineUpCurrent[posnIndex] = toGoOn;
					bTeam[posnIndex].setText(toGoOn);
					values.put("posn", posnIndex);
					uri = Uri.parse(TeamContentProvider.CONTENT_URI + "/"
							+ playerIDLookUp.get(toGoOn));
					getActivity().getContentResolver().update(uri, values,
							null, null);
					panelList.remove(toGoOn);
				}
				panelList.remove("RESET POSITION TO NUMBER");
				panelList.remove("ENTER NEW PLAYER NAME");
				Collections.sort(panelList);
				panelList.add(0, "RESET POSITION TO NUMBER");
				panelList.add(0, "ENTER NEW PLAYER NAME");
				// getTeam(panelName);
			} else {
				Toast.makeText(getActivity(),
						"error, unable to undo substition", Toast.LENGTH_SHORT)
						.show();
			}
		} catch (Exception e) {
			Log.e("undoSubs Error", e.toString());
		}
	}

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
		String temp1 = "", temp2 = "", temp3 = "", temp4;
		if (((Startup) getActivity()).getFragmentScore().getTime() != "") {
			temp1 = ((Startup) getActivity()).getFragmentScore().getTime();
			temp2 = ((Startup) getActivity()).getFragmentScore().bPeriod
					.getText().toString();
		}
		temp3 = (bloodSub) ? " blood sub " : " substitution ";
		temp4 = (bloodSub) ? "true" : "false";
		ContentValues values = new ContentValues();
		if (temp1.equals("")) {
			values.put("line", temp3 + panelName + "--> off: " + playerOff
					+ "  on: " + playerOn);
		} else {
			values.put("line", temp1 + "mins " + temp2 + temp3 + panelName
					+ "--> off: " + playerOff + "  on: " + playerOn);
		}
		values.put("sort", System.currentTimeMillis());
		values.put("type", "u");
		values.put("time", temp1);
		values.put("team", panelName);
		values.put("period", temp2);
		values.put("blood", temp4);
		values.put("subon", playerOn);
		values.put("suboff", playerOff);
		getActivity().getContentResolver().insert(
				TeamContentProvider.CONTENT_URI_2, values);

		// increment subsused counter
		if (!bloodSub) {
			((Startup) getActivity()).getFragmentReview().updateCardsSubs();
		}

		updateSubsList();
		((Startup) getActivity()).getFragmentScore().updateStatsList(false);
		((Startup) getActivity()).getFragmentEvent().fillData();
		;
		// ((Startup) getActivity()).getFragmentReview().updateListView();
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
				TeamContentProvider.CONTENT_URI, values, "team=?",
				new String[] { panelName });
		// count = getActivity().getContentResolver().update(
		// TeamContentProvider.CONTENT_URI, values,
		// TeamContentProvider.TEAM + " = '" + panelName + "'", null);
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
		input.setText(panelName);
		alert.setTitle("Enter New Team Name");
		alert.setMessage("Name:");
		alert.setView(input);
		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface indialog, int which) {
				String inName = input.getText().toString();
				if (inName.length() > 2) {
					// check if name exists and quit if it does
					String[] args = { inName };
					Cursor c1 = getActivity().getContentResolver().query(
							TeamContentProvider.CONTENT_URI, null, "team=?",
							args, null);
					if (c1.getCount() > 0) {
						Toast.makeText(
								getActivity(),
								"Team name already exists\n"
										+ "please enter a different name",
								Toast.LENGTH_LONG).show();
						c1.close();
						return;
					}
					// Update name in database
					ContentValues values = new ContentValues();
					int count;
					values.put("team", inName);
					// add to panel database
					count = getActivity().getContentResolver().update(
							TeamContentProvider.CONTENT_URI, values, "team=?",
							new String[] { panelName });
					// count = getActivity().getContentResolver()
					// .update(TeamContentProvider.CONTENT_URI,
					// values,
					// TeamContentProvider.TEAM + " = '"
					// + panelName + "'", null);
					// if team doesnt exist, create it
					if (count == 0) {
						values = new ContentValues();
						values.put("name", "...");
						values.put("posn", 0);
						values.put("team", inName);
						getActivity().getContentResolver().insert(
								TeamContentProvider.CONTENT_URI, values);
					} else {
						// update other dbs
						getActivity().getContentResolver().update(
								TeamContentProvider.CONTENT_URI_2, values,
								"team=?", new String[] { panelName });
						getActivity().getContentResolver().update(
								TeamContentProvider.CONTENT_URI_3, values,
								"team=?", new String[] { panelName });
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
					editor.putString("OWNTEAM", panelName);
					editor.commit();
					((Startup) getActivity()).getFragmentScore().setTeamLineUp(
							panelName, "");
					((Startup) getActivity()).getFragmentReview().setTeamNames(
							panelName, "");
					((Startup) getActivity()).getFragmentScorers()
							.setTeamNames(panelName, "");
					// ((Startup) getActivity()).getFragmentReview()
					// .updateListView();
					((Startup) getActivity()).getFragmentScore()
							.updateStatsList(false);
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
					// check if name exists and quit if it does
					String[] args = { inName };
					Cursor c1 = getActivity().getContentResolver().query(
							TeamContentProvider.CONTENT_URI, null, "team=?",
							args, null);
					if (c1.getCount() > 0) {
						Toast.makeText(
								getActivity(),
								"Team name already exists\n"
										+ "please enter a different name",
								Toast.LENGTH_LONG).show();
						c1.close();
						return;
					}

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
					editor.putString("OWNTEAM", panelName);
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
							editor.putString("OWNTEAM", panelName);
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

	private void deletePlayer() {
		// get list of player names
		ArrayList<String> panelList = new ArrayList<String>();
		String[] projection = { TeamContentProvider.NAME };
		CursorLoader cL = new CursorLoader(getActivity(), allTitles,
				projection, "team=?", new String[] { panelName },
				TeamContentProvider.NAME);
		// CursorLoader cL = new CursorLoader(getActivity(), allTitles,
		// projection,
		// TeamContentProvider.TEAM + " = '" + panelName + "'", null,
		// TeamContentProvider.NAME);
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

	private void renamePlayer() {
		// get list of player names
		ArrayList<String> panelList = new ArrayList<String>();
		String[] projection = { TeamContentProvider.NAME };
		CursorLoader cL = new CursorLoader(getActivity(), allTitles,
				projection, "team=?", new String[] { panelName },
				TeamContentProvider.NAME);
		// CursorLoader cL = new CursorLoader(getActivity(), allTitles,
		// projection,
		// TeamContentProvider.TEAM + " = '" + panelName + "'", null,
		// TeamContentProvider.NAME);
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
			builder.setTitle("select player to rename");
			builder.setSingleChoiceItems(panel, 0,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							player = panel[which];
							dialog.dismiss();
							AlertDialog.Builder alertB = new AlertDialog.Builder(
									getActivity());
							input = new EditText(getActivity());
							input.setId(997);
							input.setText(player);
							alertB.setTitle("Rename Player");
							alertB.setMessage("Name:");
							alertB.setView(input);
							alertB.setPositiveButton("OK",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface indialog,
												int which) {
											String inName = input.getText()
													.toString();
											if (inName.length() > 2) {
												// Update name in database
												ContentValues values = new ContentValues();
												values.put("name", inName);
												// add to panel database
												getActivity()
														.getContentResolver()
														.update(TeamContentProvider.CONTENT_URI,
																values,
																"name=?",
																new String[] { player });
												getActivity()
														.getContentResolver()
														.update(TeamContentProvider.CONTENT_URI_3,
																values,
																"name=?",
																new String[] { player });
												values.clear();
												values.put("player", inName);
												// update eventts database
												getActivity()
														.getContentResolver()
														.update(TeamContentProvider.CONTENT_URI_2,
																values,
																"player=?",
																new String[] { player });
												values.clear();
												values.put("subon", inName);
												// update eventts database
												getActivity()
														.getContentResolver()
														.update(TeamContentProvider.CONTENT_URI_2,
																values,
																"subon=?",
																new String[] { player });
												values.clear();
												values.put("suboff", inName);
												// update eventts database
												getActivity()
														.getContentResolver()
														.update(TeamContentProvider.CONTENT_URI_2,
																values,
																"suboff=?",
																new String[] { player });
												getTeam(panelName);
												((Startup) getActivity())
														.getFragmentScorers()
														.fillData();
												updateCards();
												updateSubsList();
												((Startup) getActivity())
														.getFragmentScore()
														.updateStatsList(false);
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
							alertB.create();
							alertB.show();
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
									TeamContentProvider.CONTENT_URI, "team=?",
									new String[] { team });
							// count =
							// getActivity().getContentResolver().delete(
							// TeamContentProvider.CONTENT_URI,
							// TeamContentProvider.TEAM + " = '" + team
							// + "'", null);
							Toast.makeText(
									getActivity(),
									team + " and " + (count - 1)
											+ " players deleted",
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
																String s = b
																		.getText()
																		.toString();
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
											String s = b.getText().toString();
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
										String s = b.getText().toString();
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
		int cardY = 0, cardB = 0, cardR = 0;
		cardLines = 1;
		Uri allTitles = TeamContentProvider.CONTENT_URI_2;
		strBuilderCards.setLength(0);
		CursorLoader cL;
		String[] projection = { TeamContentProvider.STATSLINE };
		String[] args = { tTeamHome.getText().toString(), "t", "%card%" };
		cL = new CursorLoader(getActivity(), allTitles, projection,
				"team=? AND type=? AND stats2 LIKE ? ", args,
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
				if (str[i].indexOf("red card") >= 0) {
					cardR++;
					strBuilderCards.append("\n" + str[i]);
					cardLines++;
				} else if (str[i].indexOf("yellow card") >= 0) {
					cardY++;
					strBuilderCards.append("\n" + str[i]);
					cardLines++;
				} else if (str[i].indexOf("black card") >= 0) {
					cardB++;
					strBuilderCards.append("\n" + str[i]);
					cardLines++;
				}
				i++;
			} while (c1.moveToNext());
			c1.close();
			// remove leading line feed
			if (strBuilderCards.length() > 0) {
				strBuilderCards.insert(0, "Summary:  " + cardY + " yellow  "
						+ cardB + " black  " + cardR + " red");
			}
		}
	}

	public void updateSubsList() {
		strBuilderSub.setLength(0);
		subLines = 1;
		int numSubs = 0;
		Uri allTitles = TeamContentProvider.CONTENT_URI_2;
		CursorLoader cL;

		String[] projection = { TeamContentProvider.STATSLINE };
		String[] args = { tTeamHome.getText().toString(), "u" };
		cL = new CursorLoader(getActivity(), allTitles, projection,
				"team=? AND type=? ", args, TeamContentProvider.STATSID);
		Cursor c1 = cL.loadInBackground();
		StringBuilder strBuilder = new StringBuilder();
		if (c1.getCount() > 0) {
			String str[] = new String[c1.getCount()];
			int i = 0;
			c1.moveToFirst();
			do {
				// insert players into positions
				str[i] = c1.getString(c1
						.getColumnIndexOrThrow(TeamContentProvider.STATSLINE));
				strBuilder.append("\n" + str[i]);
				strBuilderSub.append("\n" + str[i].replace("substitution", ""));
				subLines++;
				if (str[i].indexOf("blood sub") < 0) {
					numSubs++;
				}

				i++;
			} while (c1.moveToNext());
			c1.close();
			// rermove leading line feed
			if (strBuilder.length() > 0) {
				strBuilder.insert(0, "Summary:  " + numSubs
						+ " substitutions made");
				strBuilderSub.insert(0, "Summary:  " + numSubs
						+ " substitutions made");
			}
		}
	}

	public void setTeam(String team) {
		oppTeamName = team;
	}

	public void importTeam() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath()
				+ "/GAA_APP_Export/");
		intent.setDataAndType(uri, "file/plain");
		startActivityForResult(intent, 1);
	}

	public void exportTeam() {
		try {
			File root = new File(Environment.getExternalStorageDirectory(),
					"GAA_APP_Export");
			if (!root.exists()) {
				root.mkdirs();
			}
			File outfile = new File(root, panelName + ".txt");
			FileWriter writer = new FileWriter(outfile);
			String nl = System.getProperty("line.separator");
			writer.append("teamstart," + nl);
			for (int i = 1; i <= 15; i++) {
				writer.append(teamLineUpCurrent[i] + "," + nl);
			}
			for (int i = 2; i < panelList.size(); i++) {
				writer.append(panelList.get(i) + "," + nl);
			}

			writer.append("teamname:" + panelName + "," + nl);
			writer.append("teamend");
			writer.flush();
			writer.close();
			Toast.makeText(
					getActivity(),
					"team exported to storage in GAA_APP_export directory with filename "
							+ panelName + ".txt", Toast.LENGTH_LONG).show();

		} catch (IOException e) {
			Log.e("file write failed", e.getMessage(), e);
			Toast.makeText(
					getActivity(),
					"Error: unable to write to file\n"
							+ "make sure team name has only letters and numbers "
							+ "other characters like / will not work",
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != 0) {
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

				for (int i = 1; i < 10; i++) {
					// check if name exists and append __i if it does
					String[] args = { panelName };
					Cursor c1 = getActivity().getContentResolver().query(
							TeamContentProvider.CONTENT_URI, null, "team=?",
							args, null);
					if (c1.getCount() > 0) {
						// team exists
						if (panelName.substring(panelName.length() - 3,
								panelName.length() - 1).equals("__")) {
							panelName = panelName.substring(0,
									panelName.length() - 1)
									+ i;
						} else {
							panelName = panelName + "__" + i;
						}
					} else {
						c1.close();
						break;
					}
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
				editor.putString("OWNTEAM", panelName);
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
			Log.e("catch", " " + ex);
			Toast.makeText(
					getActivity(),
					"Can't find downloaded file.\nHave a look in ? HELP screen"
							+ "for suggestions ", Toast.LENGTH_LONG).show();
		}
	}

	public void deleteDownloads() {
		try {
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
						f.delete();
					}
				}
			}
		}

		catch (Exception ex) {
			Log.e("error with deleting downloads", ex.toString());
		}
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
		case R.id.renamePlayer:
			renamePlayer();
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
		case R.id.importTeam:
			importTeam();
			return true;
		case R.id.exportTeam:
			exportTeam();
			return true;
		case R.id.downloadTeam:
			downloadTeam();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
