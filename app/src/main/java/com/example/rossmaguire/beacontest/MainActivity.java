package com.example.rossmaguire.beacontest;

import android.content.DialogInterface;
import android.content.Intent;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import static com.example.rossmaguire.beacontest.LoginActivity.USERNAME;

public class MainActivity extends AppCompatActivity {

    private TextView userName;
    private String user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userName = findViewById(R.id.userName);

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
}