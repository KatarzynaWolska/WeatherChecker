package com.example.weatherchecker;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class DailyWeatherData extends AsyncTask<String, Void, String> {

    private HashMap<String, String> names;
    private HashMap<String, String> units;
    private HashMap<String, Double> predictor;

    private String[] mainParameters;
    private String[] windParameters;
    private String[] cloudsParameters;

    private static final String MAIN = "main";
    private static final String WIND = "wind";
    private static final String CLOUDS = "clouds";
    private static final String COORD = "coord";

    private static final String TEMP = "temp";
    private static final String PRESSURE = "pressure";
    private static final String HUMIDITY = "humidity";
    private static final String TEMP_MIN = "temp_min";
    private static final String TEMP_MAX = "temp_max";
    private static final String WIND_SPEED = "speed";
    private static final String WIND_DEGREES = "deg";
    private static final String CLOUDINESS = "all";
    private static final String COORD_LON = "lon";
    private static final String COORD_LAT = "lat";

    private static final String TEMP_UNIT = (char) 0x00B0 + " C";
    private static final String WIND_DEGREES_UNIT = Character.toString((char) 0x00B0);
    private static final String PRESSURE_UNIT = "hPa";
    private static final String CLOUDINESS_UNIT = "%";
    private static final String WIND_SPEED_UNIT = "m/s";

    private static final String API_KEY = "0547eb1ce9af96469e2ba1e4a3c1cd8f";
    private static final String API_URL_CITY_ID = "http://api.openweathermap.org/data/2.5/weather?id=";

    public DailyWeatherData() {
        mainParameters = new String[] {TEMP, PRESSURE, HUMIDITY, TEMP_MIN, TEMP_MAX};
        windParameters = new String[] {WIND_SPEED, WIND_DEGREES};
        cloudsParameters = new String[] {CLOUDINESS};
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            URL url = new URL(API_URL_CITY_ID + strings[0] + "&APPID=" + API_KEY);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                return stringBuilder.toString();
            } finally {
                urlConnection.disconnect();
            }
        } catch (Exception e) {
            Log.e("ERROR", e.getMessage(), e);
            System.out.println("ERROR");
            return null;

        }
    }

    /*@Override
    protected void onPostExecute(String response) {

        if(response == null) {
            responseText.setText("THERE WAS AN ERROR");
            return;
        }

        HashMap<String, Double> map = getData(response);
        StringBuilder stringBuilder = new StringBuilder();

        for(String mainParameter: mainParameters) {
            stringBuilder.append(mainParameter + ": " + map.get(mainParameter) + ".\n");
        }

        for(String windParameter: windParameters) {
            stringBuilder.append(windParameter + ": " + map.get(windParameter) + ".\n");
        }

        for(String cloudParameter: cloudsParameters) {
            stringBuilder.append(cloudParameter + ": " + map.get(cloudParameter) + ".\n");
        }

        responseText.setText(stringBuilder.toString());
    }*/
}
