package com.example.rossmaguire.beacontest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBConnection extends SQLiteOpenHelper{

    public static final String DATABASE_NAME = "users.db";
    public static final String USERS_TABLE_NAME = "Users";
    public static final String TIMESHEETS_TABLE_NAME = "Timesheets";

    /*public static final String U_COL_1 = "User_ID";
    public static final String U_COL_2 = "Username";
    public static final String U_COL_3 = "Password";*/

    public static final String T_COL_1 = "_id";
    public static final String T_COL_2 = "CHECK_IN_TIME";
    public static final String T_COL_3 = "CHECK_OUT_TIME";

    public DBConnection(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String DATABASE_CREATE_USERS = "CREATE TABLE " + USERS_TABLE_NAME + "(User_ID INTEGER PRIMARY KEY AUTOINCREMENT,Username TEXT NOT NULL,Password TEXT NOT NULL);";
        db.execSQL(DATABASE_CREATE_USERS);
        db.execSQL("INSERT INTO Users (Username, Password) VALUES('user', 'pass')");
        String DATABASE_CREATE_TIMESHEETS = "CREATE TABLE Timesheets (_id INTEGER PRIMARY KEY AUTOINCREMENT,CHECK_IN_TIME TIME,CHECK_OUT_TIME TIME);";
        db.execSQL(DATABASE_CREATE_TIMESHEETS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + USERS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TIMESHEETS_TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData (String checkInTime, String checkOutTime){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(T_COL_2, checkInTime);
        contentValues.put(T_COL_3, checkOutTime);
        Long result  = db.insert(TIMESHEETS_TABLE_NAME, null, contentValues); // inserts parsed values into TABLE_NAME
        if (result == -1){
            return false;
        }
        else {
            return  true; // if result is not minus -1 then the insert has worked
        }
    }
}
