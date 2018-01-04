package com.example.benjaminfondell.sqlite_project;

import android.provider.BaseColumns;

/**
 * Created by benjaminfondell on 11/4/17.
 */


public class DatabaseContract {
    public static final class LocationDBEntry implements BaseColumns {
        public static final String TABLE_NAME = "Locations";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";
        public static final String COLUMN_TIMESTAMP = "timestamp";
    }
}