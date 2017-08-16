package com.example.rossmaguire.beacontest;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
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

public class MainActivity extends AppCompatActivity {

    private BeaconManager beaconManager;
    private final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private TextView userName, checkInTime, checkOutTime;
    private InputStream is = null;
    private String result = null;
    private String line;
    private String inOrOut;
    private String user;
    private long millis = System.currentTimeMillis();
    private DateFormat dfDate = new SimpleDateFormat("yyyy-MM-dd");
    private DateFormat dfTime = new SimpleDateFormat("HH:mm:ss");
    private Time cTime;
    private Date cDate;
    private String reportDate;
    private String reportTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userName = findViewById(R.id.userName);
        checkInTime = findViewById(R.id.checkIn);
        checkOutTime = findViewById(R.id.checkOut);
        Intent intent = getIntent();
        user = intent.getStringExtra(LoginActivity.USERNAME);

        userName.setText("Welcome " + user);

        final Region gc = new Region("GC", UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), 55141, null);

        beaconManager = new BeaconManager(getApplicationContext());

        beaconManager.setBackgroundScanPeriod(25000, 30000);

        beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {
            @Override
            public void onEnteredRegion(Region region, List<Beacon> list) {
                showNotification("You have entered Greenwood Campbell.", "Welcome to GC!");
                inOrOut = "In";
                cTime = new Time(millis);
                cDate = new Date(millis);
                reportTime = dfTime.format(cTime);
                reportDate = dfDate.format(cDate);
                checkInTime.setText("Check in time: " + cTime);
                insertToDatabase(user, reportTime, reportDate, inOrOut);
            }
            /*else if (region.getIdentifier().equals("entrance"))
                {
                    showNotification("Entered entrance", "Welcome to GC!");
                    inOrOut = "In";
                    checkInOut = new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime());
                    checkInTime.setText("Check in time: " + checkInOut);
                    insertToDatabase(user, checkInOut, inOrOut);
                }*/


            @Override
            public void onExitedRegion(Region region) {
                showNotification("You have exited Greenwood Campbell.", "See you soon!");
                inOrOut = "Out";
                cTime = new Time(millis);
                cDate = new Date(millis);
                reportTime = dfTime.format(cTime);
                reportDate = dfDate.format(cDate);
                checkOutTime.setText("Check out time: " + cTime);
                insertToDatabase(user, reportTime, reportDate, inOrOut);
                mBluetoothAdapter.disable();
            }
            /*else if (region.getIdentifier().equals("entrance"))
                {
                    showNotification("Exited entrance", "Goodbye!");
                    inOrOut = "Out";
                    checkInOut = new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime());
                    checkOutTime.setText("Check in time: " + checkInOut);
                    insertToDatabase(user, checkInOut, inOrOut);
                }*/
        });

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startMonitoring(gc);
            }
        });
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

    private void insertToDatabase(final String user, final String reportTime, final String reportDate, final String inOrOut) {
        class SendPostReqAsyncTask extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... params) {
                String user = params[0];
                String reportTime = params[1];
                String reportDate = params[2];
                String inOrOut = params[3];

                List<NameValuePair> nameValuePairs = new ArrayList<>();
                nameValuePairs.add(new BasicNameValuePair("username", user));
                nameValuePairs.add(new BasicNameValuePair("time", reportTime));
                nameValuePairs.add(new BasicNameValuePair("date", reportDate));
                nameValuePairs.add(new BasicNameValuePair("in_or_out", inOrOut));

                try {
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost("http://ssmale.ddns.net/GC/add_check_in_time.php");
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
        SendPostReqAsyncTask sendPostReqAsyncTask = new SendPostReqAsyncTask();
        sendPostReqAsyncTask.execute(user, reportTime, reportDate, inOrOut);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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
        }
        return super.onOptionsItemSelected(item);
    }
}