package it.francescoermini.volleytest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DatasetDbHelper extends SQLiteOpenHelper {

    public  static  final  String DATABASE_NAME = "point_of_interests_db";
    public  static  final  int DATABASE_VERSION = 1;

    public static final String CREATE_TABLE = "create table " + Dataset.DatasetEntry.TABLE_NAME +
            "( id integer primary key AUTOINCREMENT, "+
            Dataset.DatasetEntry.LAT + " real, " +
            Dataset.DatasetEntry.LONG + " real, " +
            Dataset.DatasetEntry.WARN + " number, " +
            Dataset.DatasetEntry.CRASH + " number, " +
            Dataset.DatasetEntry.STREET + " string );";

    private  static  final String CREATE_BLE_DEV = "create table " + Dataset.DatasetEntry.TABLE_BLE_DEV +
            "( id integer primary key AUTOINCREMENT, " +
            Dataset.DatasetEntry.BLE_NAME + " string, " +
            Dataset.DatasetEntry.BLE_ADDRESS + " string );";




    public static  final  String DROP_TABLE = "drop table if exists "+Dataset.DatasetEntry.TABLE_NAME+";";

    public  DatasetDbHelper(Context context){

        super(context,  DATABASE_NAME, null, DATABASE_VERSION);



    }

    @Override
    public void onCreate(SQLiteDatabase db) {
       db.execSQL(CREATE_TABLE );
       db.execSQL(CREATE_BLE_DEV);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE);
        onCreate(db);
    }
    /*
    public  void addWarning(int LAT, int LONG, int WARN,  SQLiteDatabase db){

      //  SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Dataset.DatasetEntry.LAT, LAT);
        contentValues.put(Dataset.DatasetEntry.LONG, LONG);
        contentValues.put(Dataset.DatasetEntry.WARN, WARN);
        db.insert(Dataset.DatasetEntry.TABLE_NAME, null, contentValues);
        db.close();
        Log.d("DB","Insert raw");


    }*/

    public void setDatasetFromJson(JSONObject jsonObject, SQLiteDatabase db){

        resetDataset(db);

        try {

            JSONArray jsonArray = jsonObject.getJSONArray("warnings");
            int length = jsonArray.length();

            for (int i = 0; i < length; i++) {
                JSONObject o = jsonArray.getJSONObject(i);
                double LAT = o.getDouble("LAT");
                double LONG = o.getDouble("LONG");
                int WARN = o.getInt("WARN");
                int CRASH = o.getInt("CRASH");
                String STREET = o.getString("STREET");

                ContentValues c = new ContentValues();
                c.put(Dataset.DatasetEntry.LAT, LAT);
                c.put(Dataset.DatasetEntry.LONG, LONG);
                c.put(Dataset.DatasetEntry.WARN, WARN);
                c.put(Dataset.DatasetEntry.CRASH, CRASH);
                c.put(Dataset.DatasetEntry.STREET, STREET);

                db.insert(Dataset.DatasetEntry.TABLE_NAME, null, c);
                Log.d("DB","Insert raw");

            }
            db.close();
            Log.d("DB","close db");

        } catch (JSONException e) {
            e.printStackTrace();
        }



    }

    public Cursor readDataset(SQLiteDatabase db) {
        String[] projections = {Dataset.DatasetEntry.LAT, Dataset.DatasetEntry.LONG, Dataset.DatasetEntry.WARN, Dataset.DatasetEntry.CRASH, Dataset.DatasetEntry.STREET};
        Cursor cursor = db.query(Dataset.DatasetEntry.TABLE_NAME, projections, null, null, null, null, null);
        return  cursor;
    }

    public  void resetDataset(SQLiteDatabase db){
        db.execSQL("delete from "+ Dataset.DatasetEntry.TABLE_NAME);
    }

    public  void storeBLEDevice(String name, String address, SQLiteDatabase db) {
        ContentValues c = new ContentValues();
        c.put(Dataset.DatasetEntry.BLE_NAME, name);
        c.put(Dataset.DatasetEntry.BLE_ADDRESS, address);

        db.insert(Dataset.DatasetEntry.TABLE_BLE_DEV, null, c);
        Log.d("DB","Insert raw");
        db.close();

    }


}
