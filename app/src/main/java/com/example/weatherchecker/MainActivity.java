package com.example.weatherchecker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.widget.NestedScrollView;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static com.example.weatherchecker.CurrentWeatherConstants.CITY_NAME;
import static com.example.weatherchecker.CurrentWeatherConstants.DESCRIPTION;
import static com.example.weatherchecker.CurrentWeatherConstants.TEMP;
import static com.example.weatherchecker.CurrentWeatherConstants.WEATHER;
import static com.example.weatherchecker.CurrentWeatherConstants.ICON_ID;
import static com.example.weatherchecker.CurrentWeatherConstants.TEMP_MIN;
import static com.example.weatherchecker.CurrentWeatherConstants.TEMP_MAX;
import static com.example.weatherchecker.CurrentWeatherConstants.WIND_DEGREES_UNIT;
import static com.example.weatherchecker.CurrentWeatherConstants.WIND_DEGREES;
import static com.example.weatherchecker.CurrentWeatherConstants.WIND_SPEED;
import static com.example.weatherchecker.CurrentWeatherConstants.WIND_SPEED_UNIT;
import static com.example.weatherchecker.CurrentWeatherConstants.WIND;
import static com.example.weatherchecker.CurrentWeatherConstants.CLOUDINESS_UNIT;
import static com.example.weatherchecker.CurrentWeatherConstants.CLOUDINESS;
import static com.example.weatherchecker.CurrentWeatherConstants.PRESSURE_UNIT;
import static com.example.weatherchecker.CurrentWeatherConstants.PRESSURE;
import static com.example.weatherchecker.CurrentWeatherConstants.TEMP_UNIT;
import static com.example.weatherchecker.CurrentWeatherConstants.CLOUDS;
import static com.example.weatherchecker.CurrentWeatherConstants.COORD;
import static com.example.weatherchecker.CurrentWeatherConstants.COORD_LAT;
import static com.example.weatherchecker.CurrentWeatherConstants.COORD_LON;
import static com.example.weatherchecker.CurrentWeatherConstants.HUMIDITY;
import static com.example.weatherchecker.CurrentWeatherConstants.HUMIDITY_UNIT;
import static com.example.weatherchecker.CurrentWeatherConstants.MAIN;


public class MainActivity extends AppCompatActivity {

    private final int PERMISSION_ALL = 1;
    private final int INTERNET_PERMISSION_KEY = 5;
    private final int WRITE_EXTERNAL_STORAGE_PERMISSION_KEY = 10;
    private final int READ_EXTERNAL_PERMISSION_KEY = 15;
    private final int ACCESS_FINE_LOCATION_PERMISSION_KEY = 20;
    private final int ACCESS_COARSE_LOCATION_PERMISSION_KEY = 25;

    String[] PERMISSIONS = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };

    private static final String PREFS_NAME = "my_prefs";
    private static final String LAST_JSON = "last_json";
    private static final String LAST_CITY_ID = "last_city_id";
    private static final String LAST_LOCATION_LON = "last_lon";
    private static final String LAST_LOCATION_LAT = "last_lat";

    private RetrieveData retrieveData;
    private DataAdapter dataAdapter;

    private TableLayout parametersTable;
    private TableLayout weatherTable;
    private NestedScrollView nestView;

    private Button forecastButton;

    private TextView cityNameText;
    private ImageView weatherIcon;
    private TextView temperatureText;
    private TextView weatherDescText;

    private TextView humidityText;
    private TextView pressureText;
    private TextView tempMinText;
    private TextView tempMaxText;
    private TextView cloudinessText;
    private TextView coordsText;
    private TextView windSpeedText;
    private TextView windDegreeText;

    private Toolbar toolbar;
    private ListView listView;

    private SimpleCursorAdapter cursorAdapter;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private static final String API_KEY = "0547eb1ce9af96469e2ba1e4a3c1cd8f";
    //private static final String API_URL_CITY_ID = "http://api.openweathermap.org/data/2.5/weather?id=";
    private static final String API_URL = "http://api.openweathermap.org/data/2.5/weather?";
    private static final String API_URL_CITY_ID = "id=";
    private static final String API_URL_UNITS_METRIC = "&units=metric&APPID=";
    //private static final String API_URL_GEO_COORDS = "api.openweathermap.org/data/2.5/weather?lat={lat}&lon={lon}";
    private static final String API_URL_GEO_COORDS_LAT = "lat=";
    private static final String API_URL_GEO_COORDS_LON = "&lon=";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dataAdapter = new DataAdapter(getApplicationContext());
        retrieveData = new RetrieveData();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Weather Checker");
        }

        nestView = findViewById(R.id.nest_view);
        parametersTable = findViewById(R.id.parameters_table);
        weatherTable = findViewById(R.id.weather_table_layout);
        /*searchText = findViewById(R.id.search_text);
        searchButton = findViewById(R.id.searching_button);*/
        tempMaxText = findViewById(R.id.temp_max_text_view);
        tempMinText = findViewById(R.id.temp_min_text_view);
        pressureText = findViewById(R.id.pressure_text_view);
        humidityText = findViewById(R.id.humidity_text_view);
        cloudinessText = findViewById(R.id.cloudiness_text_view);
        windSpeedText = findViewById(R.id.wind_speed_text_view);
        windDegreeText = findViewById(R.id.wind_degree_text_view);
        coordsText = findViewById(R.id.geo_coords_text_view);
        cityNameText = findViewById(R.id.city_name_text_view);
        weatherIcon = findViewById(R.id.weather_icon_image);
        temperatureText = findViewById(R.id.temperature_text_view);
        weatherDescText = findViewById(R.id.weather_desc_text_view);
        listView = findViewById(R.id.list_view);
        forecastButton = findViewById(R.id.forecast_button);

        parametersTable.setVisibility(View.INVISIBLE);
        weatherTable.setVisibility(View.INVISIBLE);
        forecastButton.setVisibility(View.INVISIBLE);

        dataAdapter.createDatabase();

        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                retrieveData = new RetrieveData();
                retrieveData.execute(Double.toString(location.getLatitude()), Double.toString(location.getLongitude()));

                SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                editor.putString(LAST_LOCATION_LAT, Double.toString(location.getLatitude()));
                editor.putString(LAST_LOCATION_LON, Double.toString(location.getLongitude()));
                editor.apply();
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.app_bar_search_location) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 10, locationListener);
                        }
                        else {
                            ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, PERMISSION_ALL);
                        }
                    }
                }
                return false;
            }
        });

        int[] to = new int[] {
                R.id.city_name_list,
                R.id.coords_list,
        };

        cursorAdapter = new SimpleCursorAdapter(this, R.layout.city_list_item, dataAdapter.getAllCities(), dataAdapter.getColumns(), to, 0);
        listView.setAdapter(cursorAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Cursor cursor = (Cursor) listView.getItemAtPosition(i);
                Map<String, String> cityCoords = dataAdapter.getCityCoords(cursor);
                String cityID = dataAdapter.getCityId(cursor);
                String cityLat = cityCoords.get("LAT");
                String cityLon = cityCoords.get("LON");

                SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                editor.putString(LAST_CITY_ID, cityID);
                editor.putString(LAST_LOCATION_LAT, cityLat);
                editor.putString(LAST_LOCATION_LON, cityLon);
                editor.apply();

                if(cityID == null) {
                    Toast.makeText(getApplicationContext(), "City not found.", Toast.LENGTH_SHORT).show();
                }
                else {
                    setNestViewVisible();
                    toolbar.clearFocus();

                    retrieveData = new RetrieveData();
                    retrieveData.execute(cityID);
                    dataAdapter.close();
                }
            }
        });

        forecastButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(getBaseContext(), WeatherForecastActivity.class);

                SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

                if (!preferences.getString(LAST_LOCATION_LAT, "").isEmpty() && !preferences.getString(LAST_LOCATION_LON, "").isEmpty()) {
                    myIntent.putExtra("lat", preferences.getString(LAST_LOCATION_LAT, ""));
                    myIntent.putExtra("lon", preferences.getString(LAST_LOCATION_LON, ""));
                    startActivity(myIntent);
                }
                else if (!preferences.getString(LAST_CITY_ID, "").isEmpty()) {
                    myIntent.putExtra("city_id", preferences.getString(LAST_CITY_ID, ""));
                    startActivity(myIntent);
                }

            }
        });


        listView.setVisibility(View.GONE);

        if (!getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString(LAST_JSON, "").isEmpty()) {
            retrieveData.execute();
        }

    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case ACCESS_FINE_LOCATION_PERMISSION_KEY:
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(getApplicationContext(), "We need the permission to allow app to check your geo coordinates.", Toast.LENGTH_SHORT).show();
                }
                break;

            case ACCESS_COARSE_LOCATION_PERMISSION_KEY:
                if (!(grantResults.length > 0 && grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(getApplicationContext(), "We need the permission to allow app to check your geo coordinates.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /*private void setNestViewVisible() {
        setNestViewVisible();
    }*/

    private void setNestViewVisible() {
        listView.setVisibility(View.GONE);
        nestView.setVisibility(View.VISIBLE);
        forecastButton.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        MenuItem searchItem = menu.findItem(R.id.app_bar_search_city);

        SearchView search = (SearchView) searchItem.getActionView();
        search.setIconified(false);
        search.setSearchableInfo(manager.getSearchableInfo(getComponentName()));

        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                menuItem.getActionView().requestFocus();
                setListViewVisible();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                setNestViewVisible();
                return true;
            }
        });

        search.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setListViewVisible();
            }
        });


        search.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                setNestViewVisible();
                return false;
            }
        });


        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String s) {
                setNestViewVisible();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                setListViewVisible();

                cursorAdapter.getFilter().filter(s);
                return false;
            }
        });

        cursorAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence charSequence) {
                return dataAdapter.filterByName(charSequence.toString());
            }
        });

        return true;
    }

    private void setListViewVisible() {
        nestView.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);
        forecastButton.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setNestViewVisible();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    class RetrieveData extends AsyncTask<String, Void, String> {

        private String[] mainParameters;
        private String[] windParameters;
        private String[] cloudsParameters;
        private String[] geoCoords;
        private String[] weatherParameters;

        public RetrieveData() {
            mainParameters = new String[] {TEMP, PRESSURE, HUMIDITY, TEMP_MIN, TEMP_MAX};
            windParameters = new String[] {WIND_SPEED, WIND_DEGREES};
            cloudsParameters = new String[] {CLOUDINESS};
            geoCoords = new String[] {COORD_LON, COORD_LAT};
            weatherParameters = new String[] {ICON_ID, DESCRIPTION};
        }

        private HashMap<String, String> getData(String response) {
            HashMap<String, String> values = new HashMap<>();

            if(response == null) {
                response = "THERE WAS AN ERROR";
            }

            try {
                JSONObject jsonObject = new JSONObject(response);

                SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                editor.putString(LAST_JSON, jsonObject.toString());
                editor.apply();

                String value;

                for (String mainParameter : mainParameters) {
                    if(jsonObject.getJSONObject(MAIN).has(mainParameter)) {
                        value = jsonObject.getJSONObject(MAIN).getString(mainParameter);
                        values.put(mainParameter, value);
                    }
                }

                for (String windParameter : windParameters) {
                    if(jsonObject.getJSONObject(WIND).has(windParameter)) {
                        value = jsonObject.getJSONObject(WIND).getString(windParameter);
                        values.put(windParameter, value);
                    }
                }

                for (String cloudsParameter : cloudsParameters) {
                    if(jsonObject.getJSONObject(CLOUDS).has(cloudsParameter)) {
                        value = jsonObject.getJSONObject(CLOUDS).getString(cloudsParameter);
                        values.put(cloudsParameter, value);
                    }
                }

                for (String geoCoord : geoCoords) {
                    if(jsonObject.getJSONObject(COORD).has(geoCoord)) {
                        value = jsonObject.getJSONObject(COORD).getString(geoCoord);
                        values.put(geoCoord, value);
                    }
                }

                for (String weatherParameter : weatherParameters) {
                    if(jsonObject.getJSONArray(WEATHER).getJSONObject(0).has(weatherParameter)) {
                        value = jsonObject.getJSONArray(WEATHER).getJSONObject(0).getString(weatherParameter);
                        values.put(weatherParameter, value);
                    }
                }

                if(jsonObject.has(CITY_NAME)) {
                    value = jsonObject.getString(CITY_NAME);
                    values.put(CITY_NAME, value);
                }

            } catch (JSONException e) {
                Log.e("ERROR", e.getMessage(), e);
            }

            return values;

        }


        @Override
        protected String doInBackground(String... strings) {
            try {
                if (strings.length == 0) {
                    return getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString(LAST_JSON, "");
                }

                URL url = new URL(API_URL + API_URL_CITY_ID + strings[0] + API_URL_UNITS_METRIC + API_KEY);

                if (strings.length == 2) {
                    url = new URL(API_URL + API_URL_GEO_COORDS_LAT + strings[0] + API_URL_GEO_COORDS_LON + strings[1] + API_URL_UNITS_METRIC + API_KEY);
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

            HashMap<String, String> map = getData(response);

            parametersTable.setVisibility(View.VISIBLE);
            weatherTable.setVisibility(View.VISIBLE);
            forecastButton.setVisibility(View.VISIBLE);

            humidityText.setText((map.get(HUMIDITY)) != null ? map.get(HUMIDITY) + HUMIDITY_UNIT : "no info");
            pressureText.setText((map.get(PRESSURE)) != null ? map.get(PRESSURE) + PRESSURE_UNIT : "no info");
            tempMaxText.setText((map.get(TEMP_MAX)) != null ? String.format("%.1f", Double.parseDouble(map.get(TEMP_MAX))) + TEMP_UNIT : "no info");
            tempMinText.setText((map.get(TEMP_MIN)) != null ? String.format("%.1f", Double.parseDouble(map.get(TEMP_MIN))) + TEMP_UNIT : "no info");
            cloudinessText.setText((map.get(CLOUDINESS)) != null ? map.get(CLOUDINESS) + CLOUDINESS_UNIT : "no info");
            windSpeedText.setText((map.get(WIND_SPEED)) != null ? map.get(WIND_SPEED) + WIND_SPEED_UNIT : "no info");
            windDegreeText.setText((map.get(WIND_DEGREES)) != null ? map.get(WIND_DEGREES) + WIND_DEGREES_UNIT : "no info");
            coordsText.setText((map.get(COORD_LAT) != null || map.get(COORD_LON) != null) ? "[" + map.get(COORD_LAT) + ", " + map.get(COORD_LON) + "]" : "no info");
            cityNameText.setText((map.get(CITY_NAME)) != null ? map.get(CITY_NAME) : "no info");
            weatherIcon.setImageDrawable(getIcon(map.get(ICON_ID)));
            temperatureText.setText((map.get(TEMP)) != null ? String.format("%.0f", Double.parseDouble(map.get(TEMP))) + TEMP_UNIT : "no info");
            weatherDescText.setText((map.get(DESCRIPTION)) != null ? map.get(DESCRIPTION) : "no info");
        }

        private Drawable getIcon(String name) {
            String iconName = "icon_" + name;
            int iconId = getResources().getIdentifier(iconName, "drawable", getPackageName());
            return getResources().getDrawable(iconId);
        }
    }
}
