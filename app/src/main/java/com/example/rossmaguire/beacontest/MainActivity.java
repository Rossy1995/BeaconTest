package com.example.rossmaguire.beacontest;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private BeaconManager beaconManager;
    private TextView userName = (TextView) findViewById(R.id.userName);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        String username = intent.getStringExtra(LoginActivity.USER_NAME);

        userName.setText("Welcome " + username);

        final Region dev = new Region("dev", UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), 3640, 4061);
        final Region entrance = new Region("entrance", UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), 55141, 43349);

        beaconManager = new BeaconManager(getApplicationContext());

        beaconManager.setBackgroundScanPeriod(25000, 30000);

        beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {
            @Override
            public void onEnteredRegion(Region region, List<Beacon> list) {
                if (region.getIdentifier().equals("dev"))
                {
                    showNotification(
                            "Entered dev floor", "Welcome to GC!");
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
                    showNotification(
                            "Exited dev floor", "Goodbye!");
                }
                else if (region.getIdentifier().equals("entrance"))
                {
                    showNotification(
                            "Exited entrance", "Goodbye!");
                }
            }
        });

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startMonitoring(dev);
                beaconManager.startMonitoring(entrance);
            }
        });
    }

    public void showNotification(String title, String message) {
        Intent notifyIntent = new Intent(this, MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0,
                new Intent[] { notifyIntent }, PendingIntent.FLAG_UPDATE_CURRENT);
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

