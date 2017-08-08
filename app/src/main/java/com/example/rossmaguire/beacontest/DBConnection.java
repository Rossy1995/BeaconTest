package com.example.rossmaguire.beacontest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class DBConnection extends SQLiteOpenHelper{

    public static final String DATABASE_NAME = "users.db";
    public static final String USERS_TABLE_NAME = "Users";
    public static final String TIMESHEETS_TABLE_NAME = "Timesheets";

    /*public static final String U_COL_1 = "User_ID";
    public static final String U_COL_2 = "Username";
    public static final String U_COL_3 = "Password";*/

    public static final String T_COL_1 = "Timesheet_id";
    public static final String T_COL_2 = "Check_In_Time";
    public static final String T_COL_3 = "Check_Out_Time";

    public DBConnection(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String DATABASE_CREATE_USERS = "CREATE TABLE " + USERS_TABLE_NAME + "(User_ID INTEGER PRIMARY KEY AUTOINCREMENT,Username TEXT NOT NULL,Password TEXT NOT NULL);";
        db.execSQL(DATABASE_CREATE_USERS);
        db.execSQL("INSERT INTO Users (Username, Password) VALUES('user', 'pass')");
        String DATABASE_CREATE_TIMESHEETS = "CREATE TABLE " + TIMESHEETS_TABLE_NAME + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, Check_In_Time TIMESTAMP, Check_Out_Time TIMESTAMP);";
        db.execSQL(DATABASE_CREATE_TIMESHEETS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + USERS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TIMESHEETS_TABLE_NAME);
        onCreate(db);
    }

    public boolean insertCheckInTime (){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(T_COL_2, getDateTime());
        Long result  = db.insert(TIMESHEETS_TABLE_NAME, null, contentValues); // inserts parsed values into TABLE_NAME
        if (result == -1){
            return false;
        }
        else {
            return true; // if result is not minus -1 then the insert has worked
        }
    }

    /*public Cursor getAllData (){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor result = db.rawQuery("SELECT rowid AS _id, Check_In_Time, Check_Out_Time from " + TIMESHEETS_TABLE_NAME, null); // selects all from db sorting by score ASC
        return result;
    }*/

    public String getAllData()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] columns = {T_COL_1, T_COL_2, T_COL_3};
        Cursor cursor = db.query(TIMESHEETS_TABLE_NAME, columns, null, null, null, null, null);
        StringBuffer buffer = new StringBuffer();
        while (cursor.moveToNext()) {
            int index = cursor.getColumnIndex(T_COL_1);
            int cid = cursor.getInt(index);
            int index1 = cursor.getColumnIndex(T_COL_2);
            String cid1 = cursor.getString(index1);
            int index2 = cursor.getColumnIndex(T_COL_3);
            String cid2 = cursor.getString(index2);

            buffer.append(cid + " " + cid1 + " " + cid2 + "\n");

        }
        return buffer.toString();
    }

    public String getDateTime()
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
}
