package com.example.rossmaguire.beacontest;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Date;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import static com.example.rossmaguire.beacontest.LoginActivity.USERNAME;

public class MainActivity extends AppCompatActivity {

    private BeaconManager beaconManager;
    private TextView userName, checkInTime, checkOutTime;
    private InputStream is = null;
    private String result = null;
    private String line;
    private String inOrOut;
    private String user;
    private long millis;
    private DateFormat dfDate = new SimpleDateFormat("yyyy-MM-dd");
    private DateFormat dfTime = new SimpleDateFormat("HH:mm");
    private Time cTime;
    private Date cDate;
    private String reportDate;
    private String reportTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //dfTime.setTimeZone(TimeZone.getTimeZone("UTC"));

        userName = findViewById(R.id.userName);
        checkInTime = findViewById(R.id.checkIn);
        checkOutTime = findViewById(R.id.checkOut);
        Intent intent = getIntent();
        user = intent.getStringExtra(USERNAME);

        Intent service = new Intent(MainActivity.this, ForegroundService.class);
        service.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
        service.putExtra("username", user);
        startService(service);

        userName.setText("Welcome " + user);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.log_out) {
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
            mBuilder.setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent stopIntent = new Intent(MainActivity.this, ForegroundService.class);
                            stopIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
                            startService(stopIntent);
                            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } else if (id == R.id.user_analytics) {
            Intent intent = new Intent(this, AnalyticsActivity.class);
            intent.putExtra(USERNAME, user);
            this.startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void showNotification(String title, String message) {
        Intent notifyIntent = new Intent(this, MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0,
                new Intent[]{notifyIntent}, PendingIntent.FLAG_UPDATE_CURRENT);
        @SuppressWarnings("deprecation") Notification notification = new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();
        //noinspection deprecation
        notification.defaults |= Notification.DEFAULT_SOUND;
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }

}