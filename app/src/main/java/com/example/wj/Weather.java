package com.example.wj;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;

public class Weather extends AppCompatActivity {

    private Class NoteEditorActivity;


    @Override //note: changing location in emulator will simulate a change in location and change temp data.
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GpsTracker gpstracker = new GpsTracker(Weather.this);
        double longitude = gpstracker.getLongitude();
        double latitude = gpstracker.getLatitude();
        setContentView(R.layout.activity_weather);
        Button button2 = findViewById(R.id.button_first);
        button2.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        });

        String lat = latitude+"";
        String log = longitude+"";
        new GetData().execute(lat, log);




    }

    public static double convertTemps(String temp)
    {
        double tkelvin = Double.parseDouble(temp);
        double tfheight = (tkelvin-273.15) * (9/5) + 32;
        tfheight = Math.round(tfheight * 100.0) / 100.0;
        return tfheight;
    }

    class GetData extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            String result = "";
            HttpURLConnection conn = null;
            try { //this uses my own unique app key to gather weather data. It's pretty sweet
                URL url = new URL("http://api.openweathermap.org/data/2.5/weather?lat="+ URLEncoder.encode(params[0], "UTF-8")+"&lon="+URLEncoder.encode(params[1], "UTF-8")+"&appid=49feb0f2d7867189f14ac358e34b66a5");
                conn = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(conn.getInputStream());
                if (in != null) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                    String line = "";
                    int count = 0;
                    while ((line = bufferedReader.readLine()) != null) {
                        result += line;
                    }


                }
                in.close();
                return result;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            finally {
                if(conn!=null)
                    conn.disconnect();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            String[] resultarr = result.split("\\{");
            String[] tempsplit = resultarr[4].split(","); //this is terribly, terribly written. However, ignorance of JSON parsing made me desperate.
            for(int i = 0; i<tempsplit.length;  i++)
            {
                String[] sp = tempsplit[i].split("\"");
                String str = sp[2];
                str = str.substring(1);
                tempsplit[i] = str;
            }

            TextView textview = findViewById(R.id.textview_first);
            textview.setText("Temperature: "+convertTemps(tempsplit[0])+" F");


        }

    }
}
