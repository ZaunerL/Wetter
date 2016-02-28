package com.example.lukas.wetterapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends AppCompatActivity {

    private String url;
    private TextView city;
    private TextView sunrise;
    private TextView sunset;
    private EditText plz;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        city = (TextView) findViewById(R.id.city);
        sunrise = (TextView) findViewById(R.id.sunrise);
        sunset = (TextView) findViewById(R.id.sunset);
        plz = (EditText) findViewById(R.id.plz);
    }

    public void klickLoadButton(View v) {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {

            if(plz.getText().length() < 4) {
                Toast.makeText(MainActivity.this, "PLZ muss aus 4 Zahlen bestehen!", Toast.LENGTH_SHORT).show();
            }else {
                url = "http://api.openweathermap.org/data/2.5/forecast?zip=" + plz.getText() + ",at&mode=xml&appid=44db6a862fba0b067b1930da0d769e98";
                DownloadWebpageTask de = new DownloadWebpageTask();
                de.execute(url);
            }
        } else {
            Toast.makeText(MainActivity.this, "Keine Internet-Verbindung vorhanden", Toast.LENGTH_SHORT).show();
        }

    }
    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {

            String[] arr = null;
            arr = result.split(";");
            city.setText(arr[0]);
            sunrise.setText(arr[1]);
            sunset.setText(arr[2]);

        }
    }
    private String downloadUrl(String myurl) throws IOException {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 500;

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            is = conn.getInputStream();

            // Convert the InputStream into a string
            String contentAsString = readIt(is, len);
            return contentAsString;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }
    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);

        String result = null;

        String sBuffer = String.valueOf(buffer);
        if(sBuffer.contains("name")) {
            if (sBuffer.contains("sun")) {
                int iName = sBuffer.indexOf("<name>");
                int iSet = sBuffer.indexOf("set=\"");
                int iRise = sBuffer.indexOf("rise=\"");
                String sName = sBuffer.substring(sBuffer.indexOf("<name>")+6,sBuffer.indexOf("</name>"));
                String sRise = sBuffer.substring(sBuffer.indexOf('"', iRise) + 1, sBuffer.indexOf('"', iRise + 6)).replaceAll("T", "\nTime: ");
                String sSet = sBuffer.substring(sBuffer.indexOf('"', iSet) + 1, sBuffer.indexOf('"', iSet + 6)).replaceAll("T", "\nTime: ");
                result = sName + ";" + sRise + ";" + sSet;
            }
        }else {
            result = "Not Found;Error;Error";
        }
        return result;
    }

}
