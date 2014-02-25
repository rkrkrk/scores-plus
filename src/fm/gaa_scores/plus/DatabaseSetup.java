/*
 *  BaseProvider.java
 *
 *  Written by: Fintan Mahon 12101524
 *  
 *  Description: Create all tables in SQLite databasen team
 *  
 *  Written on: Jan 2013
 *  
 * 
 */
package fm.gaa_scores.plus;

import android.content.ContentProvider;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public abstract class DatabaseSetup extends ContentProvider {

	DatabaseHelper dbHelper;

	public static final String DATABASE_NAME = "teams";
	public static final int DATABASE_VERSION = 3;

	// setup table to store panel player details 
	private static final String CREATE_TABLE_PANEL = "create table "
			+ TeamContentProvider.DATABASE_TABLE_PANEL
			+ " (_id integer primary key autoincrement, "
			+ TeamContentProvider.TEAM + " text, " 
			+ TeamContentProvider.NAME + " text, " 
			+ TeamContentProvider.POSN + " integer);";
	
	private static final String CREATE_TABLE_STATS = "create table "
			+ TeamContentProvider.DATABASE_TABLE_STATS
			+ " (_id integer primary key autoincrement, " 
			+ TeamContentProvider.STATSLINE + " text);";
	
	private static final String CREATE_TABLE_SCORES = "create table "
			+ TeamContentProvider.DATABASE_TABLE_SCORES
			+ " (_id integer primary key autoincrement, " 
			+ TeamContentProvider.SCORESNAME + " text, " 
			+ TeamContentProvider.SCORESTEAM + " text, " 
			+ TeamContentProvider.SCORESGOALS + " integer, " 
			+ TeamContentProvider.SCORESPOINTS + " integer, " 
			+ TeamContentProvider.SCORESTOTAL + " integer, " 
			+ TeamContentProvider.SCORESGOALSFREE + " integer, " 
			+ TeamContentProvider.SCORESPOINTSFREE + " integer, " 
			+ TeamContentProvider.SCORESMISS + " integer, " 
			+ TeamContentProvider.SCORESMISSFREE + " integer);";

	// inner class to create database
	static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		// method to create database tables defined above
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_TABLE_PANEL);
			db.execSQL(CREATE_TABLE_STATS); 
			db.execSQL(CREATE_TABLE_SCORES);
	}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// code to upgrade your db here (all of your tables, indexes,
			// triggers...)
			// upgrade to add panelname to DB
			if ( (oldVersion==1) && (newVersion==2)) {
				db.execSQL(CREATE_TABLE_SCORES);
			}
			if (oldVersion <= 2 && newVersion == 3) {
				db.execSQL("ALTER TABLE " + TeamContentProvider.DATABASE_TABLE_SCORES
						+ " ADD COLUMN " + TeamContentProvider.SCORESMISSFREE);
			}
		}
	}

}
