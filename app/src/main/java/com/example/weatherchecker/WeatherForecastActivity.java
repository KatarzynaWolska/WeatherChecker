package com.example.weatherchecker;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class WeatherForecastActivity extends AppCompatActivity {

    private static final String API_KEY = "0547eb1ce9af96469e2ba1e4a3c1cd8f";
    private static final String API_URL = "http://api.openweathermap.org/data/2.5/forecast?";
    private static final String API_URL_UNITS_METRIC = "&units=metric&APPID=";
    private static final String API_URL_CITY_ID = "id=";
    private static final String API_URL_GEO_COORDS_LAT = "lat=";
    private static final String API_URL_GEO_COORDS_LON = "&lon=";

    private TableLayout forecastTable;

    private String currentCityId;
    private String currentCityLon;
    private String currentCityLat;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_forecast);

        forecastTable = findViewById(R.id.forecast_table_layout);

        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            currentCityId = bundle.getString("city_id");
            currentCityLon = bundle.getString("lon");
            currentCityLat = bundle.getString("lat");
        }

        if (currentCityId != null) {
            new DataRetriever().execute(currentCityId);
        } else if (currentCityLat != null && currentCityLon != null) {
            new DataRetriever().execute(currentCityLat, currentCityLon);
        }
    }

    public TableRow createRow() {
        TableRow row = new TableRow(this);
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT);
        row.setLayoutParams(layoutParams);
        row.setPadding(0, 30, 0, 30);

        return row;
    }

    public View getDayForecastLayout() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View layout = inflater.inflate(R.layout.day_forecast, null, false);

        return layout;
    }

    public TableRow getWeatherRowDate(String time) {
        TableRow row = createRow();
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(time);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            TextView textView = new TextView(this);
            textView.setText(new SimpleDateFormat("dd-MM-yyyy").format(calendar.getTime()));

            row.addView(textView);

            return row;

        } catch (ParseException e) {
            System.out.println(e.toString());
        }

        return null;
    }

    class DataRetriever extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url;

                if (strings.length == 1) {
                    url = new URL(API_URL + API_URL_CITY_ID + strings[0] + API_URL_UNITS_METRIC + API_KEY);
                }
                else if (strings.length == 2) {
                    url = new URL(API_URL + API_URL_GEO_COORDS_LAT + strings[0] + API_URL_GEO_COORDS_LON + strings[1] + API_URL_UNITS_METRIC + API_KEY);
                }
                else {
                    return null;
                }


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

        @Override
        protected void onPostExecute(String response) {
            if(response == null) {
                Toast.makeText(getApplicationContext(), "There was an error.", Toast.LENGTH_SHORT).show();
                return;
            }

            getData(response);
        }

        private boolean checkNewRowHour(String date) throws ParseException {
            Date fullDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(fullDate);

            return calendar.get(Calendar.HOUR_OF_DAY) == 0;
        }

        private void getData(String response) {
            try {
                JSONObject jsonObject = new JSONObject(response);

                int lineNumber = jsonObject.getInt(ForecastConstants.CNT);

                JSONArray jsonArray = jsonObject.getJSONArray(ForecastConstants.LIST);

                TableRow row = createRow();

                for (int i = 0; i < lineNumber; i++) {
                    JSONObject json = jsonArray.getJSONObject(i);
                    String date = json.getString(ForecastConstants.DT_TXT);

                    if (i == 0) {
                        forecastTable.addView(getWeatherRowDate(date));
                    }

                    if (checkNewRowHour(date) && (i != 0)) {
                        forecastTable.addView(row);
                        forecastTable.addView(getWeatherRowDate(date));
                        row = createRow();
                    }

                    String temp = json.getJSONObject(ForecastConstants.MAIN).getString(ForecastConstants.TEMP);
                    String icon = json.getJSONArray(ForecastConstants.WEATHER).getJSONObject(0).getString(ForecastConstants.ICON_ID);
                    String time = json.getString(ForecastConstants.DT_TXT);

                    row.addView(getLayoutWithData(time, icon, temp));
                }

                forecastTable.addView(row);

            } catch (JSONException e) {
                Log.e("ERROR", e.getMessage(), e);

            } catch (ParseException e) {
                Log.e("ERROR", e.getMessage(), e);
            }
        }

        private View getLayoutWithData(String time, String icon, String temp) {
            View layout = getDayForecastLayout();

            TextView timeTextView = layout.findViewById(R.id.frcst_time_text_view);
            ImageView frcstImageView = layout.findViewById(R.id.frcst_icon_image_view);
            TextView tempTextView = layout.findViewById(R.id.frcst_temp_text_view);

            try {
                Calendar calendar = Calendar.getInstance();
                Date firstDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(time);
                calendar.setTime(firstDate);

                timeTextView.setText(new SimpleDateFormat("HH:mm").format(calendar.getTime()));
            } catch (ParseException e) {
                System.out.println(e.toString());
            }

            frcstImageView.setImageDrawable(getIcon(icon));
            tempTextView.setText(String.format("%.0f", Double.parseDouble(temp)) + CurrentWeatherConstants.TEMP_UNIT);

            return layout;
        }

        private Drawable getIcon(String name) {
            String iconName = "icon_" + name;
            int iconId = getResources().getIdentifier(iconName, "drawable", getPackageName());
            return getResources().getDrawable(iconId);
        }
    }
}
