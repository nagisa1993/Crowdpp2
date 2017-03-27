package com.crowdpp.nagisa.crowdpp2.db;

import com.crowdpp.nagisa.crowdpp2.util.Constants;
import com.crowdpp.nagisa.crowdpp2.db.DataBaseTable.DiaryTable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * The DataBaseHelper class
 * @author Chenren Xu, Haiyue Ma
 */

public class DataBaseHelper extends SQLiteOpenHelper{
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = Constants.dbName;

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " 	+ DiaryTable.TABLE_NAME
                + " ("
                + DiaryTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DiaryTable.SYS_TIME + " INTEGER, "
                + DiaryTable.DATE + " TEXT, "
                + DiaryTable.START + " TEXT, "
                + DiaryTable.END + " TEXT, "
                + DiaryTable.ACTIVITY + "TEXT,"
                + DiaryTable.ACTIVITY_CONFIDENCE + "REAL"
                + ");");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS " + DiaryTable.TABLE_NAME;
        db.execSQL(sql);
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    public long insertDiary(SQLiteDatabase db, long sys_time, String date, String start, String end, String activity, int confidence) {
        ContentValues cv = new ContentValues();
        db.beginTransaction();
        try {
            cv.put(DiaryTable.SYS_TIME, sys_time);
            cv.put(DiaryTable.DATE, date);
            cv.put(DiaryTable.START, start);
            cv.put(DiaryTable.END, end);
            cv.put(DiaryTable.ACTIVITY, activity);
            cv.put(DiaryTable.ACTIVITY_CONFIDENCE, confidence);
            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }
        return db.insert(DiaryTable.TABLE_NAME, null, cv);
    }

    public String[] queryDatesInDiary(SQLiteDatabase db) {
        String query = "SELECT DISTINCT " + DiaryTable.DATE + " FROM " + DiaryTable.TABLE_NAME + " ORDER BY " + DiaryTable.ORDER + ";";
        Cursor cursor = db.rawQuery(query, null);
        int dates_count = cursor.getCount();
        Log.i("Dates count", Integer.toString(dates_count));
        String[] dates = new String[dates_count];
        if (cursor.moveToFirst()) {
            for(int i = 0; i < dates_count; i++) {
                dates[i] = cursor.getString(0);
                cursor.moveToNext();
                Log.i("Dates", dates[i]);
            }
        }
        return dates;
    }

    public Cursor queryDiaryByDates(SQLiteDatabase db, String[] date) {
        String tb = DiaryTable.TABLE_NAME;
        String[] cols = new String[] {DiaryTable.DATE,
                DiaryTable.START, DiaryTable.END,
                DiaryTable.ACTIVITY, DiaryTable.ACTIVITY_CONFIDENCE};
        String sel = DiaryTable.DATE.concat("=?");
        String order = DiaryTable.ORDER;
        Cursor cursor = db.query(tb, cols, sel, date, null, null, order);
        return cursor;
    }
}
