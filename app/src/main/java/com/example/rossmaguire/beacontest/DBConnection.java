package com.example.rossmaguire.beacontest;

import android.annotation.SuppressLint;
import android.os.StrictMode;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class DBConnection{

    String ip = "192.168.0.88";
    String dbClass = "net.sourceforge.jtds.jdbc.Driver";
    String db = "Beacon";
    String username = "AndroidTest";
    String password = "test";

    @SuppressLint("NewApi")
    public Connection CONN() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Connection conn = null;
        String connURL = null;
        try {

            Class.forName(dbClass);
            connURL = "jdbc:jtds:sqlserver://" + ip + ":1433/" + db + ";user=" + username + ";password=" + password + ";";
            conn = DriverManager.getConnection(connURL);
        } catch (SQLException se) {
            Log.e("ERROR", se.getMessage());
        } catch (ClassNotFoundException e) {
            Log.e("ERROR", e.getMessage());
        } catch (Exception e) {
            Log.e("ERROR", e.getMessage());
        }
        return conn;
    }

    public String getDateTime()
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
}
