package com.example.rossmaguire.beacontest;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;


import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "BeaconNotifications";
    private BeaconManager beaconManager;
    private Region region;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    private java.sql.Time time = new java.sql.Time(Calendar.getInstance().getTime().getTime());
    private DBConnection dbConnection = new DBConnection();
    private Connection con = dbConnection.CONN();
    private Button btnInsert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnInsert = (Button) (findViewById(R.id.btnInsert));

        btnInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String query = "INSERT INTO Timesheets (Check_In_Time) VALUES ('" + time + "');";
                    Statement statement = con.createStatement();
                    ResultSet rs = statement.executeQuery(query);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}

