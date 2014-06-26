/*
 *  PanelContentProvider.java
 *
 *  Reference: BooksProvider class from Beginning Android 4 Application Development by Wei-Meng Lee
 *  http://www.wrox.com/WileyCDA/WroxTitle/Beginning-Android-4-Application-Development.productCd-1118199545,descCd-DOWNLOAD.html
 *  modified to suit
 *  
 *  Description: This class is the Content Provider for the panel table in the App database
 *  it facilitates CRUD operations on the database table and notifies ContentResolver whenever
 *  the database is changed
 *  

 *  Written on: Jan 2013
 *  
 * 
 */
package fm.gaa_scores.plus;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class TeamContentProvider extends DatabaseSetup {
	// set up uri for content provider
	public static final String PROVIDER_NAME = "fm.gaa_scores.plus.provider.team";
	public static final String BASE_PATH = "players";
	public static final String BASE_PATH_2 = "stats";
	public static final String BASE_PATH_3 = "scores";

	public static final Uri CONTENT_URI = Uri.parse("content://"
			+ PROVIDER_NAME + "/" + BASE_PATH);
	public static final Uri CONTENT_URI_2 = Uri.parse("content://"
			+ PROVIDER_NAME + "/" + BASE_PATH_2);
	public static final Uri CONTENT_URI_3 = Uri.parse("content://"
			+ PROVIDER_NAME + "/" + BASE_PATH_3);
//
	public static final String _ID = "_id";
	private static final int PLAYERS = 1;
	private static final int PLAYERS_ID = 2;
	private static final int STATS = 3;
	private static final int STATS_ID = 4; 
	private static final int SCORES = 5;
	private static final int SCORES_ID = 6; 

	// use urimatcher to parse input uri from contentresolver
	private static final UriMatcher uriMatcher;
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(PROVIDER_NAME, BASE_PATH, PLAYERS);
		uriMatcher.addURI(PROVIDER_NAME, BASE_PATH + "/#", PLAYERS_ID);
		uriMatcher.addURI(PROVIDER_NAME, BASE_PATH_2, STATS);
		uriMatcher.addURI(PROVIDER_NAME, BASE_PATH_2 + "/#", STATS_ID);
		uriMatcher.addURI(PROVIDER_NAME, BASE_PATH_3, SCORES);
		uriMatcher.addURI(PROVIDER_NAME, BASE_PATH_3 + "/#", SCORES_ID);
	}

	// ---for database use--- 
	private SQLiteDatabase matchAppDB;
	public static final String DATABASE_TABLE_PANEL = "panel";
	public static final String PANELID = "_id";
	public static final String TEAM = "team";
	public static final String NAME = "name";
	public static final String POSN = "posn";

	public static final String DATABASE_TABLE_STATS = "stats";
	public static final String STATSID = "_id";
	public static final String STATSLINE = "line";
	//version 5
	public static final String STATSTIME = "time";
	public static final String STATSSORT = "sort";
	public static final String STATSPERIOD = "period";
	public static final String STATSTEAM = "team";
	public static final String STATSPLAYER = "player";
	public static final String STATS1 = "stats1";
	public static final String STATS2 = "stats2";
	//type: s=start/stop u=sub t=stats
	public static final String STATSTYPE = "type";
	public static final String STATSSUBON = "subon";
	public static final String STATSSUBOFF = "suboff";
	public static final String STATSBLOOD = "blood";
	
	public static final String DATABASE_TABLE_SCORES = "scores";
	public static final String SCORESID = "_id";
	public static final String SCORESNAME = "name";
	public static final String SCORESTEAM = "team";
	public static final String SCORESGOALS = "goals";
	public static final String SCORESPOINTS = "points";
	public static final String SCORESTOTAL = "total";
	public static final String SCORESGOALSFREE = "goalsfree";
	public static final String SCORESPOINTSFREE = "pointsfree";
	public static final String SCORESMISS = "miss";
	public static final String SCORESMISSFREE = "missfree";

	@Override
	// open connection to team defined in BaseProvider Class
	public boolean onCreate() {
		
		Context context = getContext();
		DatabaseHelper dbHelper = new DatabaseHelper(context);
		matchAppDB = dbHelper.getWritableDatabase();
		return (matchAppDB == null) ? false : true;
	}

	@Override
	// delete players from database
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int count = 0;
		switch (uriMatcher.match(uri)) {
		// delete all players
		case PLAYERS:
			count = matchAppDB.delete(DATABASE_TABLE_PANEL, selection, selectionArgs);
			break;
		// delete single player
		case PLAYERS_ID:
			String id = uri.getPathSegments().get(1);
			count = matchAppDB.delete(DATABASE_TABLE_PANEL, _ID
					+ " = "
					+ id
					+ (!TextUtils.isEmpty(selection) ? " AND (" + selection
							+ ')' : ""), selectionArgs);
			break;
		case STATS:
			count = matchAppDB.delete(DATABASE_TABLE_STATS, selection, selectionArgs);
			break;
		// delete single player
		case STATS_ID:
			String id1 = uri.getPathSegments().get(1);
			count = matchAppDB.delete(DATABASE_TABLE_STATS, _ID
					+ " = "
					+ id1
					+ (!TextUtils.isEmpty(selection) ? " AND (" + selection
							+ ')' : ""), selectionArgs);
			break;
		case SCORES:
			count = matchAppDB.delete(DATABASE_TABLE_SCORES, selection, selectionArgs);
			break;
		// delete single player
		case SCORES_ID:
			String id2 = uri.getPathSegments().get(1);
			count = matchAppDB.delete(DATABASE_TABLE_SCORES, _ID
					+ " = "
					+ id2
					+ (!TextUtils.isEmpty(selection) ? " AND (" + selection
							+ ')' : ""), selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		// notify contentresolver of change
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		// ---get all players---
		case PLAYERS:
			return "fm.gaa_scores.plus.cursor.dir/fm.gaa_scores.plus.players ";
			// ---get a particular player---
		case PLAYERS_ID:
			return "fm.gaa_scores.plus.cursor.item/fm.gaa_scores.plus.players ";
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	//
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Uri _uri = null;

		switch (uriMatcher.match(uri)) {
		case PLAYERS:
			long rowID1 = matchAppDB.insert(DATABASE_TABLE_PANEL, "", values);
			if (rowID1 > 0) {
				_uri = ContentUris.withAppendedId(CONTENT_URI, rowID1);
				// notify contentresolver of change
				getContext().getContentResolver().notifyChange(_uri, null);
			}
			break;
		case STATS:
			long rowID2 = matchAppDB.insert(DATABASE_TABLE_STATS, "", values);
			if (rowID2 > 0) {
				_uri = ContentUris.withAppendedId(CONTENT_URI_2, rowID2);
				// notify contentresolver of change
				getContext().getContentResolver().notifyChange(_uri, null);
			}
			break;
		case SCORES:
			long rowID3 = matchAppDB.insert(DATABASE_TABLE_SCORES, "", values);
			if (rowID3 > 0) {
				_uri = ContentUris.withAppendedId(CONTENT_URI_2, rowID3);
				// notify contentresolver of change
				getContext().getContentResolver().notifyChange(_uri, null);
			}
			break;
		default:

			throw new SQLException("Failed to insert row into " + uri);
		}
		return _uri;
	}

	@Override
	//N
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
		switch (uriMatcher.match(uri)) {
		case PLAYERS:
			sqlBuilder.setTables(DATABASE_TABLE_PANEL);
			if (uriMatcher.match(uri) == PLAYERS_ID)
				// ---if getting a particular player---
				sqlBuilder.appendWhere(_ID + " = "
						+ uri.getPathSegments().get(1));
			if (sortOrder == null || sortOrder == "")
				sortOrder = PANELID;
			break;

		case STATS:
			sqlBuilder.setTables(DATABASE_TABLE_STATS);
			if (uriMatcher.match(uri) == STATS_ID)
				// ---if getting a particular player---
				sqlBuilder.appendWhere(_ID + " = "
						+ uri.getPathSegments().get(1));
			if (sortOrder == null || sortOrder == "")
				sortOrder = STATSID;
			break;

		case SCORES:
			sqlBuilder.setTables(DATABASE_TABLE_SCORES);
			if (uriMatcher.match(uri) == SCORES_ID)
				// ---if getting a particular player---
				sqlBuilder.appendWhere(_ID + " = "
						+ uri.getPathSegments().get(1));
			if (sortOrder == null || sortOrder == "")
				sortOrder = SCORESID;
			break;

		}
		Cursor c = sqlBuilder.query(matchAppDB, projection, selection,
				selectionArgs, null, null, sortOrder);
		// notify contentresolver of change
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int count = 0;
		switch (uriMatcher.match(uri)) {
		// update single player
		case PLAYERS:
			count = matchAppDB.update(DATABASE_TABLE_PANEL, values, selection,
					selectionArgs);
			break;
		// update all players
		case PLAYERS_ID:
			count = matchAppDB.update(
					DATABASE_TABLE_PANEL,
					values,
					_ID
							+ " = "
							+ uri.getPathSegments().get(1)
							+ (!TextUtils.isEmpty(selection) ? " AND ("
									+ selection + ')' : ""), selectionArgs);
			break;
		case STATS:
			count = matchAppDB.update(DATABASE_TABLE_STATS, values, selection,
					selectionArgs);
			break;
		// update all players
		case STATS_ID:
			count = matchAppDB.update(
					DATABASE_TABLE_STATS,
					values,
					_ID
							+ " = "
							+ uri.getPathSegments().get(1)
							+ (!TextUtils.isEmpty(selection) ? " AND ("
									+ selection + ')' : ""), selectionArgs);
			break;
		case SCORES:
			count = matchAppDB.update(DATABASE_TABLE_SCORES, values, selection,
					selectionArgs);
			break;
		// update all players
		case SCORES_ID:
			count = matchAppDB.update(
					DATABASE_TABLE_SCORES,
					values,
					_ID
							+ " = "
							+ uri.getPathSegments().get(1)
							+ (!TextUtils.isEmpty(selection) ? " AND ("
									+ selection + ')' : ""), selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		// notify contentresolver of change
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
}
