package com.example.rossmaguire.beacontest;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;

import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private BeaconManager beaconManager;
    private final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private String checkIn;
    private InputStream is = null;
    private String result = null;
    private String line;
    private String inOrOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        final String user = intent.getStringExtra(LoginActivity.USER_NAME);

        TextView userName = findViewById(R.id.userName);

        userName.setText("Welcome \n" + user);

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
                    inOrOut = "In";
                    checkIn = new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime());
                    insertToDatabase(user, checkIn, inOrOut);
                }
                else if (region.getIdentifier().equals("entrance"))
                {
                    showNotification(
                            "Entered entrance", "Welcome to GC!");
                    inOrOut = "In";
                    checkIn = new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime());
                    insertToDatabase(user, checkIn, inOrOut);
                }
            }
            @Override
            public void onExitedRegion(Region region) {
                if (region.getIdentifier().equals("dev"))
                {
                    showNotification(
                            "Exited dev floor", "Goodbye!");
                    inOrOut = "Out";
                    checkIn = new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime());
                    insertToDatabase(user, checkIn, inOrOut);
                }
                else if (region.getIdentifier().equals("entrance"))
                {
                    showNotification(
                            "Exited entrance", "Goodbye!");
                    inOrOut = "Out";
                    checkIn = new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime());
                    insertToDatabase(user, checkIn, inOrOut);
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

    private void showNotification(String title, String message) {
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

    private void insertToDatabase(final String user, final String checkIn, final String inOrOut)
    {
        class SendPostReqAsyncTask extends AsyncTask<String, Void, String>{
            @Override
            protected String doInBackground(String... params){
                String user = params[0];
                String checkIn = params[1];
                String inOrOut = params[2];

                List<NameValuePair> nameValuePairs = new ArrayList<>();
                nameValuePairs.add(new BasicNameValuePair("username", user));
                nameValuePairs.add(new BasicNameValuePair("check_in_time", checkIn));
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

                    while ((line = reader.readLine()) != null)
                    {
                        sb.append(line + "\n");
                    }
                    result = sb.toString();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

                return result;
            }

            @Override
            protected void onPostExecute(String result){
                Toast.makeText(getApplicationContext(), "Insert was successful!", Toast.LENGTH_LONG).show();
                mBluetoothAdapter.disable();

            }
        }
        SendPostReqAsyncTask sendPostReqAsyncTask = new SendPostReqAsyncTask();
        sendPostReqAsyncTask.execute(user, checkIn, inOrOut);
    }
}