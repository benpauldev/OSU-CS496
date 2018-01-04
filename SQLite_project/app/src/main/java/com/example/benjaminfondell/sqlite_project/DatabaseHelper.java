package com.example.benjaminfondell.sqlite_project;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.benjaminfondell.sqlite_project.DatabaseContract.*;
/**
 * Created by benjaminfondell on 11/4/17.
 */

public class DatabaseHelper extends SQLiteOpenHelper {


    private static final String DATABASE_NAME = "location.db";
    private static final int DATABASE_VERSION = 1;


    public DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    // When a database is created, this is called to set up the location table.
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String BUILD_LOCATION_TABLE = "CREATE TABLE " + DatabaseContract.LocationDBEntry.TABLE_NAME + " (" +
                DatabaseContract.LocationDBEntry._ID              + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DatabaseContract.LocationDBEntry.COLUMN_NAME              + " TEXT, " +
                DatabaseContract.LocationDBEntry.COLUMN_LATITUDE  + " FLOAT NOT NULL, " +
                DatabaseContract.LocationDBEntry.COLUMN_LONGITUDE + " FLOAT NOT NULL, " +
                DatabaseContract.LocationDBEntry.COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP" + ");";

        sqLiteDatabase.execSQL(BUILD_LOCATION_TABLE);
    }

    //
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
