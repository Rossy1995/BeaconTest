package com.example.rossmaguire.beacontest;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.rossmaguire.beacontest.LoginActivity.USERNAME;

public class AnalyticsActivity extends AppCompatActivity implements View.OnClickListener {

    private InputStream is = null;

    private ListView dataList;

    private Button datePicker;
    private Calendar calendar;
    private String username, result;
    private int year, month, day;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        calendar = Calendar.getInstance();

        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        username = getIntent().getStringExtra(USERNAME);

        dataList = findViewById(R.id.dataList);
        datePicker = findViewById(R.id.datePicker);

        datePicker.setOnClickListener(this);

        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onClick(View v)
    {
        if (v == datePicker)
        {
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener()
            {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
                {
                    result = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                    datePicker.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                    new GetJSON().execute(username, result);
                }
            }, year, month, day);
            datePickerDialog.show();
        }
    }
    public class GetJSON extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... params) {
                String user = params[0];
                String date = params[1];

                List<NameValuePair> nameValuePairs = new ArrayList<>();
                nameValuePairs.add(new BasicNameValuePair("username", user));
                nameValuePairs.add(new BasicNameValuePair("date", date));

                try {
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost("http://gc_reporting.sagat.dnsalias.com/checkUserTime.php");
                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    HttpResponse response = httpClient.execute(httpPost);
                    HttpEntity entity = response.getEntity();

                    is = entity.getContent();

                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
                    StringBuilder sb = new StringBuilder();

                    String json;

                    while ((json = bufferedReader.readLine()) != null) {
                        sb.append(json + "\n");
                    }
                    //finally returning the read string
                    return sb.toString().trim();
                } catch (Exception e) {
                    return null;
                }

            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                try {
                    loadIntoListView(s);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }

    private void loadIntoListView(String json) throws JSONException {

        JSONArray jsonArray = new JSONArray(json);

        int [] view = new int [] {R.id.time, R.id.in_or_out};

        List<HashMap<String, String>> userTime = new ArrayList<>(jsonArray.length());

        for (int i = 0; i < jsonArray.length(); i++) {

            HashMap item = new HashMap<String, String>();
            JSONObject obj = jsonArray.getJSONObject(i);

            item.put("Time", obj.getString("cTime"));
            item.put("In or Out", obj.getString("InOROut"));

            userTime.add(item);

        }

        ListAdapter adapter = new SimpleAdapter(this, userTime, R.layout.item_layout, new String[] {"Time", "In or Out"}, new int[] {R.id.time, R.id.in_or_out});

        dataList.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onBackPressed()
    {}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home)
        {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        else if (id == R.id.log_out) {
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(AnalyticsActivity.this);
            mBuilder.setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(AnalyticsActivity.this, LoginActivity.class);
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

