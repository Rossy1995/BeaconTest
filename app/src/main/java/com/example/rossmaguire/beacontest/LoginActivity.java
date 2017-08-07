package com.example.rossmaguire.beacontest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    DBAdapter dbAdapter;
    EditText txtUsername;
    EditText txtPassword;
    Button btnLogin;
    DBConnection myDbConnection;
    String username;
    String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final SharedPreferences prefs = getSharedPreferences("MyApp", MODE_PRIVATE);

        txtUsername = (EditText) findViewById(R.id.username);
        txtPassword = (EditText) findViewById(R.id.password);
        btnLogin = (Button) findViewById(R.id.login);

        myDbConnection = new DBConnection(this);
        dbAdapter = new DBAdapter(this);
        dbAdapter.open();

        btnLogin.setOnClickListener(new android.view.View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(txtUsername.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(txtPassword.getWindowToken(), 0);
                username = txtUsername.getText().toString();
                password = txtPassword.getText().toString();
                if (username.length() > 0 && password.length() > 0) {
                    try {
                        if (dbAdapter.Login(username, password)) {
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        } else {
                            Toast.makeText(LoginActivity.this,
                                    "Invalid username or password",
                                    Toast.LENGTH_LONG).show();
                        }

                    } catch (Exception e) {
                        Toast.makeText(LoginActivity.this, "Some problem occurred",
                                Toast.LENGTH_LONG).show();

                    }
                } else {
                    Toast.makeText(LoginActivity.this,
                            "Username or Password is empty", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    public String getUsername()
    {
        return username;
    }
}
