package com.example.rossmaguire.beacontest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

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

    public final SimpleDateFormat parser = new SimpleDateFormat("HH:mm:ss");

    public DBConnection(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String DATABASE_CREATE_USERS = "CREATE TABLE " + USERS_TABLE_NAME + "(User_ID INTEGER PRIMARY KEY AUTOINCREMENT,Username TEXT NOT NULL,Password TEXT NOT NULL);";
        db.execSQL(DATABASE_CREATE_USERS);
        db.execSQL("INSERT INTO Users (Username, Password) VALUES('user', 'pass')");
        String DATABASE_CREATE_TIMESHEETS = "CREATE TABLE Timesheets (Timesheet_id INTEGER PRIMARY KEY AUTOINCREMENT,Check_In_Time DATETIME,Check_Out_Time DATETIME);";
        db.execSQL(DATABASE_CREATE_TIMESHEETS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + USERS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TIMESHEETS_TABLE_NAME);
        onCreate(db);
    }

    public boolean insertCheckInTime (Timestamp checkInTime){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(T_COL_2, parser.format(checkInTime));
        Long result  = db.insert(TIMESHEETS_TABLE_NAME, null, contentValues); // inserts parsed values into TABLE_NAME
        if (result == -1){
            return false;
        }
        else {
            return  true; // if result is not minus -1 then the insert has worked
        }
    }
}
