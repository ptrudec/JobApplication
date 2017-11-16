package com.example.android.jobapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {
    private static final String REQUEST = "https://spika.chat/api/v3/signin?";
    public static final String MY_PREFS_NAME = "MyPrefsFile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button loginButton = (Button) findViewById(R.id.button_login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView usernameText = (TextView) findViewById(R.id.username_text);
                String username = usernameText.getText().toString();

                TextView passwordText = (TextView) findViewById(R.id.password_text);
                String password = passwordText.getText().toString();

                if (username.isEmpty() && password.isEmpty()){
                    Toast.makeText(LoginActivity.this, "Enter username and password please.", Toast.LENGTH_LONG).show();
                }
                else if(username.isEmpty()){
                    Toast.makeText(LoginActivity.this, "Enter username please.", Toast.LENGTH_LONG).show();
                }else if (password.isEmpty()){
                    Toast.makeText(LoginActivity.this, "Enter password please.", Toast.LENGTH_LONG).show();
                }else {
                    new AsyncLogin().execute(username, password);
                }


            }
        });

    }

    private class AsyncLogin extends AsyncTask<String, String, String> {
        ProgressDialog pdLoading = new ProgressDialog(LoginActivity.this);
        HttpURLConnection conn;
        URL url = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            pdLoading.setMessage("\tLoading...");
            pdLoading.setCancelable(false);
            pdLoading.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                url = new URL(REQUEST);

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return "exception";
            }
            try {
                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("apikey", "GMUwQIHielm7b1ZQNNJYMAfCC508Giof");
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");

                // setDoInput and setDoOutput method depict handling of both send and receive
                conn.setDoInput(true);
                conn.setDoOutput(true);

                JSONObject jsonParam = new JSONObject();
                try {

                    jsonParam.put("organization", "clover");
                    jsonParam.put("username", params[0]);
                    jsonParam.put("password", params[1]);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                // Open connection for sending data
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(jsonParam.toString());
                writer.flush();
                writer.close();
                os.close();
                conn.connect();

            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                return "exception";
            }

            try {

                int response_code = conn.getResponseCode();

                // Check if successful connection made
                if (response_code == HttpURLConnection.HTTP_OK) {

                    // Read data sent from server
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    String json = result.toString();
                    String token = null;
                     try {
                        JSONObject jsonObject = new JSONObject(json);
                        token = jsonObject.getString("access-token");
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    SharedPreferences sharedpreferences = getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString("token", token);
                    editor.apply();

                    return ("successful");

                } else if (response_code == 403) {
                    return ("wrong");

                } else {
                    return ("unsuccessful");
                }

            } catch (IOException e) {
                e.printStackTrace();
                return "exception";
            } finally {
                conn.disconnect();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            //this method will be running on UI thread

            pdLoading.dismiss();

            if (result.equalsIgnoreCase("successful")) {
                /* Here launching another activity when login successful. If you persist login state
                use sharedPreferences of Android. and logout button to clear sharedPreferences.
                 */
                Intent intent = new Intent(LoginActivity.this, MessageActivity.class);
                startActivity(intent);
                LoginActivity.this.finish();

            } else if (result.equalsIgnoreCase("wrong")) {
                Toast.makeText(LoginActivity.this, "Wrong username or password!", Toast.LENGTH_LONG).show();
            } else if (result.equalsIgnoreCase("exception") || result.equalsIgnoreCase("unsuccessful")) {
                Log.e("Result", "result: " + result);
                Toast.makeText(LoginActivity.this, "OOPs! Something went wrong. Connection Problem.", Toast.LENGTH_LONG).show();
            }
        }

    }

}
