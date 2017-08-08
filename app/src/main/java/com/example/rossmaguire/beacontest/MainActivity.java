package com.example.rossmaguire.beacontest;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private BeaconManager beaconManager;
    private Region region;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    private Timestamp timestamp;
    private LoginActivity login = new LoginActivity();
    private String user = login.getUsername();
    private DBConnection dbConnection = new DBConnection(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Region dev = new Region("dev", UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), 3640, 4061);
        final Region entrance = new Region("entrance", UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), 55141, 43349);

        beaconManager = new BeaconManager(this);
        region = new Region("GC", UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startMonitoring(dev);
                beaconManager.startMonitoring(entrance);


            }
        });

        beaconManager = new BeaconManager(getApplicationContext());
        beaconManager.setBackgroundScanPeriod(25000, 30000);
        beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {
            @Override
            public void onEnteredRegion(Region region, List<Beacon> list) {
                if (region.getIdentifier().equals("dev"))
                {
                    timestamp = new Timestamp(System.currentTimeMillis());
                    dbConnection.insertCheckInTime(timestamp);
                    dbConnection.close();
                    showNotification(
                            "Entered dev floor at " + sdf.format(timestamp), "Welcome to GC " + user);
                }
                else if (region.getIdentifier().equals("entrance"))
                {
                    showNotification(
                            "Entered entrance", "Welcome to GC!");
                }
            }
            @Override
            public void onExitedRegion(Region region) {
                if (region.getIdentifier().equals("dev"))
                {
                    timestamp = new Timestamp(System.currentTimeMillis());
                    showNotification(
                            "Exited dev floor at " + sdf.format(timestamp), "Goodbye!");
                }
                else if (region.getIdentifier().equals("entrance"))
                {
                    showNotification(
                            "Exited entrance", "Goodbye!");
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SystemRequirementsChecker.checkWithDefaultDialogs(this);
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(region);
            }
        });
    }

    @Override
    protected void onPause() {
        beaconManager.stopRanging(region);
        super.onPause();
    }

    public void showNotification(String title, String message) {
        Intent notifyIntent = new Intent(this, MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0,
                new Intent[]{notifyIntent}, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();
        notification.defaults |= Notification.DEFAULT_SOUND;
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }
}

