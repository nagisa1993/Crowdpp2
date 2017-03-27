package com.crowdpp.nagisa.crowdpp2.db;

import android.provider.BaseColumns;

/**
 * @author Chenren Xu, Haiyue Ma
 */

public class DataBaseTable {
    private DataBaseTable() {}

    /** The database table for social diary */
    public static final class DiaryTable implements BaseColumns {
        // CREATE TABLE Diary (id INTEGER PRIMARY KEY AUTOINCREMENT, time INTEGER, date TEXT, start TEXT, end TEXT, count INTEGER, percentage REAL, latitude REAL, longitude REAL, activity TEXT, confidence INTEGER)
        private DiaryTable() {}

        public static final String TABLE_NAME = "Diary";

        public static final String SYS_TIME             = "time";
        public static final String DATE 	            = "date";
        public static final String START 	            = "start";
        public static final String END 		            = "end";
        //public static final String COUNT 	            = "count";
        //public static final String PCT 	            = "percentage";
        //public static final String LAT 	            = "latitude";
        //public static final String LONG 	            = "longitude";
        public static final String ACTIVITY             = "activity";
        public static final String ACTIVITY_CONFIDENCE  = "confidence";

        public static final String ORDER 	            = "time ASC";
    }
}
