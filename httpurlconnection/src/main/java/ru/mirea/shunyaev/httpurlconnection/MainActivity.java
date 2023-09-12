package ru.mirea.shunyaev.httpurlconnection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import ru.mirea.shunyaev.httpurlconnection.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private String longitude;
    private String latitude;
    private String taskIdentifier;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.buttonWeather.setActivated(false);
        binding.buttonIP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectivityManager connectivityManager =
                        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkinfo = null;
                if (connectivityManager != null) {
                    networkinfo = connectivityManager.getActiveNetworkInfo();
                }
                if (networkinfo != null && networkinfo.isConnected()) {
                    taskIdentifier = "IP";
                    new DownloadPageTask().execute("https://ipinfo.io/json");
                }
                else {
                    Toast.makeText(getApplicationContext(), "Нет интернета", Toast.LENGTH_SHORT).show();
                }
            }
        });
        binding.buttonWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectivityManager connectivityManager =
                        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkinfo = null;
                if (connectivityManager != null) {
                    networkinfo = connectivityManager.getActiveNetworkInfo();
                }
                if (networkinfo != null && networkinfo.isConnected()) {
                    taskIdentifier = "Weather";
                    new DownloadPageTask().execute(String.format("https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s&current_weather=true", latitude, longitude));
                }
                else {
                    Toast.makeText(getApplicationContext(), "Нет интернета", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private class DownloadPageTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                return downloadIpInfo(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
                return "error";
            }
        }@Override
        protected void onPostExecute(String result) {
            Log.d(MainActivity.class.getSimpleName(), result);
            try {
                JSONObject responseJson = new JSONObject(result);
                Log.d(MainActivity.class.getSimpleName(), "Response: " + responseJson);
                if(taskIdentifier.equals("IP")){
                    String ip = responseJson.getString("ip");
                    String city = responseJson.getString("city");
                    String region = responseJson.getString("region");
                    String country = responseJson.getString("country");
                    String[] coordinates = responseJson.getString("loc").split(",");
                    Arrays.stream(coordinates).forEach(s -> s = s.substring(0, 5));
                    latitude = coordinates[0];
                    longitude = coordinates[1];
                    binding.textViewIP.setText(ip);
                    binding.textViewCity.setText(city);
                    binding.textViewCountry.setText(country);
                    binding.textViewRegion.setText(region);
                    binding.buttonWeather.setActivated(true);
                    Log.d(MainActivity.class.getSimpleName(), "IP: " + ip);
                }
                if(taskIdentifier.equals("Weather")){
                    JSONObject weatherJsonData = responseJson.getJSONObject("current_weather");
                    String weather = String.format("Temperature:%s, wind:%s", weatherJsonData.getString("temperature"), weatherJsonData.getString("windspeed"));
                    binding.textViewWeather.setText(weather);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            super.onPostExecute(result);
        }

        private String downloadIpInfo(String address) throws IOException {
            InputStream inputStream = null;
            String data = "";
            try {
                URL url = new URL(address);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setReadTimeout(100000);
                connection.setConnectTimeout(100000);
                connection.setRequestMethod("GET");
                connection.setInstanceFollowRedirects(true);
                connection.setUseCaches(false);
                connection.setDoInput(true);
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) { // 200 OK

                    inputStream = connection.getInputStream();
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    int read = 0;
                    while ((read = inputStream.read()) != -1) {
                        bos.write(read);
                    }
                    bos.close();
                    data = bos.toString();
                } else {
                    data = connection.getResponseMessage() + ". Error Code: " + responseCode;
                }
                connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
            return data;
        }
    }
}