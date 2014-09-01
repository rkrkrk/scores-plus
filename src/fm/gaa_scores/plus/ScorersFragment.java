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

import fm.gaa_scores.plus.R;
import android.content.Context;
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
import android.widget.Toast;

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
		ownTeam = sharedPref.getString("OWNTEAM", "OWN TEAM");
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
				TeamContentProvider.SCORESMISS,
				TeamContentProvider.SCORESMISSFREE };

		// create array to map these fields to
		int[] to = new int[] { R.id.text1, R.id.text3, R.id.text4, R.id.text5,
				R.id.text6, R.id.text7, R.id.text8 };

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
		File root, outfile;

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
					TeamContentProvider.SCORESMISS,
					TeamContentProvider.SCORESMISSFREE };

			// create array to map these fields to
			int[] to = new int[] { R.id.text1, R.id.text3, R.id.text4,
					R.id.text5, R.id.text6, R.id.text7, R.id.text8 };

			// load database info from PanelContentProvider into a cursor and
			// use an
			// adapter to display on screen
			String[] args = { ownTeam };
			Cursor c1 = getActivity().getContentResolver().query(allTitles,
					null, "team=?", args,
					TeamContentProvider.SCORESTOTAL + " DESC");

			String[] args1 = { oppTeam };
			Cursor c2 = getActivity().getContentResolver().query(allTitles,
					null, "team=?", args1,
					TeamContentProvider.SCORESTOTAL + " DESC");
			sb.append(((Startup) getActivity()).getFragmentScore().getLocText());

			sb.append("\n\nplayer  **  Total Goals / Points  **  Goals/Points from placed balls"
					+ "  **  Total wides  **  wides from place balls \n\n");
			sb.append(ownTeam + " SCORERS \n\n");

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

			sb.append("\n" + oppTeam + " SCORERS\n\n");
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
			try {
				root = new File(Environment.getExternalStorageDirectory(),
						"GAA_APP_Export");
				if (!root.exists()) {
					root.mkdirs();
				}
				outfile = new File(root, "GAAScoresStatsScorers.txt");
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
			File imageFile = new File(mPath, "GAAScoresStatScoreTweet.jpg");
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
					+ "GAAScoresStatsScorers.txt" };
			// put email attachments into an ArrayList
			ArrayList<Uri> uris = new ArrayList<Uri>();
			for (String file : emailAttachments) {
				File uriFiles = new File(file);
				Uri u = Uri.fromFile(uriFiles);
				uris.add(u);
			}
			uris.add(uri);
			emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
			startActivity(Intent.createChooser(emailIntent, "Share Using:"));
			c1.close();
			c2.close();
		}
	};

	public Bitmap createBitmap() {
		StringBuilder sb = new StringBuilder("");
		Uri allTitles = TeamContentProvider.CONTENT_URI_3;
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
		int[] to = new int[] { R.id.text1, R.id.text3, R.id.text4, R.id.text5,
				R.id.text6, R.id.text7, R.id.text8 };

		// load database info from PanelContentProvider into a cursor and
		// use an
		// adapter to display on screen
		String[] args = { ownTeam };
		Cursor c1 = getActivity().getContentResolver().query(allTitles, null,
				"team=?", args, TeamContentProvider.SCORESTOTAL + " DESC");

		String[] args1 = { oppTeam };
		Cursor c2 = getActivity().getContentResolver().query(allTitles, null,
				"team=?", args1, TeamContentProvider.SCORESTOTAL + " DESC");

		// Create Bitmap to display team selection
		int length = ((c1.getCount() + c2.getCount()) * 25);
		Bitmap bitmap = Bitmap.createBitmap(640, length + 285,
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawColor(Color.rgb(255, 255, 219));
		Paint paint = new Paint();
		paint.setColor(Color.rgb(204, 255, 204));
		canvas.drawRect(0, 0, 640, (c1.getCount() * 25) + 100, paint);
		paint.setColor(Color.BLACK);
		paint.setAntiAlias(true);
		paint.setTextAlign(Align.CENTER);
		paint.setTextSize(22);
		// Write teams
		// sb.append("player  **  Total Goals / Points  **  Goals/Points from frees/65s/45s/penalties/sidelines  **  wides/short/saved\n\n");
		canvas.drawText(ownTeam + " SCORERS", 320, 25, paint);
		paint.setColor(Color.RED);
		paint.setTextSize(20);
		canvas.drawText("from placed", 425, 50, paint);
		canvas.drawText("misses", 525, 50, paint);
		canvas.drawText("misses", 600, 50, paint);
		canvas.drawText("player", 140, 75, paint);
		canvas.drawText("totals", 310, 75, paint);
		canvas.drawText("balls", 423, 75, paint);
		canvas.drawText("total", 525, 75, paint);
		canvas.drawText("placed", 600, 75, paint);
		paint.setTextSize(22);
		paint.setColor(Color.BLACK);
		int i = 0;
		if (c1.getCount() > 0) {
			c1.moveToFirst();
			do {
				paint.setTextAlign(Align.RIGHT);
				canvas.drawText(
						c1.getString(c1
								.getColumnIndexOrThrow(TeamContentProvider.SCORESNAME)),
						240, 100 + (i * 25), paint);
				paint.setTextAlign(Align.RIGHT);
				canvas.drawText(
						c1.getString(c1
								.getColumnIndexOrThrow(TeamContentProvider.SCORESGOALS))
								+ "-", 320, 100 + (i * 25), paint);
				paint.setTextAlign(Align.LEFT);
				canvas.drawText(
						c1.getString(c1
								.getColumnIndexOrThrow(TeamContentProvider.SCORESPOINTS)),
						320, 100 + (i * 25), paint);
				paint.setTextAlign(Align.RIGHT);
				canvas.drawText(
						c1.getString(c1
								.getColumnIndexOrThrow(TeamContentProvider.SCORESGOALSFREE))
								+ "-", 430, 100 + (i * 25), paint);
				paint.setTextAlign(Align.LEFT);
				canvas.drawText(
						c1.getString(c1
								.getColumnIndexOrThrow(TeamContentProvider.SCORESPOINTSFREE)),
						430, 100 + (i * 25), paint);
				canvas.drawText(
						c1.getString(c1
								.getColumnIndexOrThrow(TeamContentProvider.SCORESMISS)),
						520, 100 + (i * 25), paint);
				canvas.drawText(
						c1.getString(c1
								.getColumnIndexOrThrow(TeamContentProvider.SCORESMISSFREE)),
						600, 100 + (i * 25), paint);
				i++;

			} while (c1.moveToNext());
		}
		i = 0;
		int spacer = 125 + (c1.getCount() * 25);
		paint.setTextAlign(Align.CENTER);
		canvas.drawText(oppTeam + " SCORERS", 320, 25 + spacer, paint);
		paint.setColor(Color.RED);
		paint.setTextSize(20);
		canvas.drawText("from placed", 425, 50 + spacer, paint);
		canvas.drawText("misses", 525, 50 + spacer, paint);
		canvas.drawText("misses", 600, 50 + spacer, paint);
		canvas.drawText("player", 140, 75 + spacer, paint);
		canvas.drawText("totals", 310, 75 + spacer, paint);
		canvas.drawText("balls", 423, 75 + spacer, paint);
		canvas.drawText("total", 525, 75 + spacer, paint);
		canvas.drawText("placed", 600, 75 + spacer, paint);
		paint.setTextSize(22);
		paint.setColor(Color.BLACK);
		if (c2.getCount() > 0) {
			c2.moveToFirst();
			do {
				paint.setTextAlign(Align.RIGHT);
				canvas.drawText(
						c2.getString(c2
								.getColumnIndexOrThrow(TeamContentProvider.SCORESNAME)),
						240, 100 + spacer + (i * 25), paint);
				paint.setTextAlign(Align.RIGHT);
				canvas.drawText(
						c2.getString(c2
								.getColumnIndexOrThrow(TeamContentProvider.SCORESGOALS))
								+ "-", 320, 100 + spacer + (i * 25), paint);
				paint.setTextAlign(Align.LEFT);
				canvas.drawText(
						c2.getString(c2
								.getColumnIndexOrThrow(TeamContentProvider.SCORESPOINTS)),
						320, 100 + spacer + (i * 25), paint);
				paint.setTextAlign(Align.RIGHT);
				canvas.drawText(
						c2.getString(c2
								.getColumnIndexOrThrow(TeamContentProvider.SCORESGOALSFREE))
								+ "-", 430, 100 + spacer + (i * 25), paint);
				paint.setTextAlign(Align.LEFT);
				canvas.drawText(
						c2.getString(c2
								.getColumnIndexOrThrow(TeamContentProvider.SCORESPOINTSFREE)),
						430, 100 + spacer + (i * 25), paint);
				canvas.drawText(
						c2.getString(c2
								.getColumnIndexOrThrow(TeamContentProvider.SCORESMISS)),
						520, 100 + spacer + (i * 25), paint);
				canvas.drawText(
						c2.getString(c2
								.getColumnIndexOrThrow(TeamContentProvider.SCORESMISSFREE)),
						600, 100 + spacer + (i * 25), paint);
				i++;

			} while (c2.moveToNext());
		}
		
		paint.setColor(Color.BLACK);
		paint.setTextSize(16);
		paint.setTextAlign(Align.CENTER);
		canvas.drawText("Misses includes wides, out for 45/65, off posts, saved and short", 320,
				length + 240, paint);


		paint.setColor(Color.GRAY);
		paint.setTextSize(16);
		paint.setTextAlign(Align.CENTER);
		canvas.drawText("GAA Scores Stats Plus - Android App.", 320,
				length + 260, paint);
		canvas.drawText("Available free from Google Play Store", 320,
				length + 275, paint);
		
		
		
		
		
		c1.close();
		c2.close();

		return bitmap;

	}

	OnClickListener tweetAllListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Bitmap bitmap = createBitmap();

			File mPath = Environment
					.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
			OutputStream fout = null;
			File imageFile = new File(mPath, "GAAScoresStatScoreTweet.jpg");
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
				shareIntent.putExtra(Intent.EXTRA_TEXT, ownTeam
						+ " v. "
						+ oppTeam
						+ " scorers\n"
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
