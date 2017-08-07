package com.example.rossmaguire.beacontest;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DBAdapter {
    private static final String USERS_TABLE_NAME = "Users";
    public static final String U_COL_1 = "User_ID";
    public static final String U_COL_2 = "Username";
    public static final String U_COL_3 = "Password";

    SQLiteDatabase mDB;
    Context mContext;
    DBConnection mDBConnection;

    public DBAdapter(Context context)
    {
        this.mContext = context;
    }

    public DBAdapter open() throws SQLException
    {
        mDBConnection = new DBConnection(mContext);
        mDB = mDBConnection.getWritableDatabase();
        return this;
    }

    public boolean Login(String username, String password) throws SQLException
    {
        Cursor mCursor = mDB.rawQuery("SELECT * FROM " + USERS_TABLE_NAME + " WHERE username=? AND password=?", new String[]{username, password});
        if (mCursor != null)
        {
            if(mCursor.getCount() > 0)
            {
                return true;
            }
        }
        return false;
    }
}
