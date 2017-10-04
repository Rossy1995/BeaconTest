package com.example.rossmaguire.beacontest;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
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
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.example.rossmaguire.beacontest.LoginActivity.USERNAME;

public class ForegroundService extends Service{

    private static final String LOG_TAG = "ForegroundService";
    private String user;
    private BeaconManager beaconManager;
    private InputStream is = null;
    private String result = null;
    private String line;
    private String inOrOut;
    private long millis;
    private Time cTime;

    @Override
    public void onCreate() {
        super.onCreate();

        final Region gc = new Region("GC", UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), 55141, null);

        beaconManager = new BeaconManager(getApplicationContext());

        beaconManager.setBackgroundScanPeriod(25000, 30000);

        beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {
            @Override
            public void onEnteredRegion(Region region, List<Beacon> list) {
                showNotification("You have entered Greenwood Campbell.", "Welcome to GC!");
                inOrOut = "In";
                millis = System.currentTimeMillis();
                cTime = new Time(millis);
                Intent intent = new Intent("usertimes");
                intent.putExtra("checkIn", cTime);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                new SendPostReqAsyncTask().execute(user, inOrOut);
            }

            @Override
            public void onExitedRegion(Region region) {
                showNotification("You have exited Greenwood Campbell.", "See you soon!");
                inOrOut = "Out";
                millis = System.currentTimeMillis();
                cTime = new Time(millis);
                Intent intent = new Intent("usertimes");
                intent.putExtra("checkOut", cTime);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                new SendPostReqAsyncTask().execute(user, inOrOut);
            }
        });

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                try {
                    beaconManager.startMonitoring(gc);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public class SendPostReqAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String user = params[0];
            String inOrOut = params[1];

            List<NameValuePair> nameValuePairs = new ArrayList<>();
            nameValuePairs.add(new BasicNameValuePair("username", user));
            nameValuePairs.add(new BasicNameValuePair("in_or_out", inOrOut));

            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost("http://gc_reporting.sagat.dnsalias.com/add_check_in_time_test.php");
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = httpClient.execute(httpPost);
                HttpEntity entity = response.getEntity();

                is = entity.getContent();

                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                result = sb.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(), "Insert was successful!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        user = intent.getStringExtra("username");

        if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {
            Log.i(LOG_TAG, "Received Start Foreground Intent ");
            postNotification();
        } else if (intent.getAction().equals(Constants.ACTION.STOPFOREGROUND_ACTION)) {
            Log.i(LOG_TAG, "Received Stop Foreground Intent");
            stopForeground(true);
            stopSelf();
        }
        return START_STICKY;
    }

    private void postNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra(USERNAME, user);
        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.addCategory("android.intent.category.LAUNCHER");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.mipmap.beacon_icon);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("GC iBeacon Agent")
                .setTicker("GC iBeacon Agent")
                .setContentText("Making checking in easier!")
                .setSmallIcon(R.mipmap.beacon_icon)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                notification);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "Service destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Used only in case if services are bound (Bound Services).
        return null;
    }
}
