package com.example.weatherchecker;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DataAdapter {

    private final Context mContext;
    private SQLiteDatabase database;
    private DatabaseHelper mDbHelper;

    private final String TABLE_NAME = "CITIES_LIST";

    private static final String ID_COL = "_id";
    private static final String NAME_COL = "_name";
    private static final String COUNTRY_COL = "_country";
    private static final String COORD_LON_COL = "_coord_lon";
    private static final String COORD_LAT_COL = "_coord_lat";

    public DataAdapter(Context context)
    {
        this.mContext = context;
        mDbHelper = new DatabaseHelper(mContext);
    }

    public String[] getColumns() {
        String[] columns = new String[] {
                NAME_COL, COORD_LAT_COL, COORD_LON_COL
        };

        return columns;
    }

    public DataAdapter createDatabase() throws SQLException
    {
        try
        {
            mDbHelper.createDatabase();
        }
        catch (IOException mIOException)
        {
            System.out.println("UnableToCreateDatabase");
            throw new Error("UnableToCreateDatabase");
        }
        return this;
    }

    public DataAdapter open() throws SQLException
    {
        try
        {
            mDbHelper.openDataBase();
            mDbHelper.close();
            database = mDbHelper.getReadableDatabase();

        }
        catch (SQLException mSQLException)
        {
            //Log.e(TAG, "open >>"+ mSQLException.toString());
            throw mSQLException;
        }
        return this;
    }

    public void close()
    {
        mDbHelper.close();
    }

    public Cursor getAllCities()
    {
        open();
        try
        {
            String sql = "SELECT * FROM " + TABLE_NAME;

            Cursor mCur = database.rawQuery(sql, null);
            if (mCur!=null)
            {
                mCur.moveToNext();
            }
            close();
            return mCur;
        }
        catch (SQLException mSQLException)
        {
            //Log.e(TAG, "getAllCities >>"+ mSQLException.toString());
            close();
            throw mSQLException;
        }
    }

    public Cursor filterByName(String name) {
        open();
        try
        {
            /*String sql = "SELECT " + NAME_COL + ", " + COORD_LON_COL + ", " + COORD_LAT_COL + " FROM " + TABLE_NAME + " WHERE " + NAME_COL + " LIKE " + "''";

            Cursor mCur = database.rawQuery(sql, null);*/

            Cursor mCur = database.query(TABLE_NAME, new String[] {ID_COL, NAME_COL, COORD_LAT_COL, COORD_LON_COL}, NAME_COL + " LIKE ?", new String[] {name + "%"}, null, null, null);

            if (mCur!=null)
            {
                mCur.moveToNext();
            }
            close();
            return mCur;
        }
        catch (SQLException mSQLException)
        {
            //Log.e(TAG, "getAllCities >>"+ mSQLException.toString());
            close();
            throw mSQLException;
        }
    }

    public String getCityId(Cursor cursor) {

        open();
        boolean isCursorEmpty = true;
        String sql = "SELECT " + ID_COL + " FROM " + TABLE_NAME + " WHERE " + NAME_COL + " = ?";

        String cityName = cursor.getString(cursor.getColumnIndex(NAME_COL));

        Cursor crs = database.rawQuery(sql, new String[] {cityName});

        if (crs != null)
        {
            isCursorEmpty = crs.moveToNext();
        }
        else
        {
            return null;
        }

        if(!isCursorEmpty) {
            return null;
        }

        String response = cursor.getString(cursor.getColumnIndex(ID_COL));
        System.out.println(response);
        crs.close();

        close();

        return response;
    }

    public Map<String, String> getCityCoords(Cursor cursor) {

        open();
        boolean isCursorEmpty = true;
        String sql = "SELECT " + COORD_LON_COL + " ," + COORD_LAT_COL + " FROM " + TABLE_NAME + " WHERE " + NAME_COL + " = ?";

        String cityName = cursor.getString(cursor.getColumnIndex(NAME_COL));

        Cursor crs = database.rawQuery(sql, new String[] {cityName});

        if (crs != null)
        {
            isCursorEmpty = crs.moveToNext();
        }
        else
        {
            return null;
        }

        if(!isCursorEmpty) {
            return null;
        }

        String lon = cursor.getString(cursor.getColumnIndex(COORD_LON_COL));
        String lat = cursor.getString(cursor.getColumnIndex(COORD_LAT_COL));

        crs.close();
        close();

        Map<String, String> coords = new HashMap<>();
        coords.put("LON", lon);
        coords.put("LAT", lat);

        return coords;
    }

}
