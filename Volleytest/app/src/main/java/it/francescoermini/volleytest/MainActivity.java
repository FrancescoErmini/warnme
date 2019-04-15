package it.francescoermini.volleytest;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements HomeFragment.OnDbOpListener {

    private final static String TAG = MainActivity.class.getSimpleName();


    private BluetoothLeService mBluetoothLeService;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    private TextView mDataField;
    private String mDeviceName;
    private String mDeviceAddress;


    private LocationManager locationManager;
    private LocationListener locationListener;


    Double warning_latitude;
    Double warning_longitude;
    Integer warning_degree;
    Integer warning_type;
    String warning_street;
    Integer degree_diff;
    Double current_latitude;
    Double current_longitude;
    Integer current_degree;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            HomeFragment homeFragment = new HomeFragment();
            //f1: add the homeFragment to the mainActivity fragment ( dove ho definito fragment_container )
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, homeFragment).commit();
        }

        //BLE
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);

        getPosition(); //call matchPosition

    }


    @Override
    public void dBOpPerformed(int method) {

        switch (method) {
            case 0:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DatasetFragment()).addToBackStack(null).commit();
                break;
            case 1:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SyncDbFragment()).addToBackStack(null).commit();
                break;


        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Dataset.DatasetEntry.delay, 0, locationListener);

            }
        }
    }


    public void getPosition() {

        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d("Location", location.toString());
                Log.d("Latidude", Double.toString(location.getLatitude()));
                Log.d("Longitude", Double.toString(location.getLongitude()));

                String currLatitudeStr = "CURR LAT: " + Double.toString(location.getLatitude());
                String currLongitudeStr = "CURR LONG: " + Double.toString(location.getLongitude());
                String currDegreeStr = "CURR DEG: " + Float.toString(location.getBearing());

                TextView currLatitude = (TextView) findViewById(R.id.current_latitude);
                TextView currLongitude = (TextView) findViewById(R.id.current_longitude);
                TextView currDegree = (TextView) findViewById(R.id.current_degree);

                if( currLatitude == null )
                    return;

                currLatitude.setText(currLatitudeStr);
                currLongitude.setText(currLongitudeStr);
                currDegree.setText(currDegreeStr);

                matchPosition(location);

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (Build.VERSION.SDK_INT < 23) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Dataset.DatasetEntry.delay, 0, locationListener);


        } else {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                return;

            } else {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Dataset.DatasetEntry.delay, 0, locationListener);

            }
        }
    }


    public void matchPosition(Location location)   // ?? DatasetDbHelper dbHelper, SQLiteDatabase database)
    {

        DatasetDbHelper dbHelper = new DatasetDbHelper(this);
        SQLiteDatabase database = dbHelper.getReadableDatabase();

        current_latitude = location.getLatitude();
        current_longitude = location.getLongitude();
        current_degree = Math.round(location.getBearing());





        //  warningBox1 = (TextView) findViewById(R.id.warning_box1);
        //  warningBox2 = (TextView) findViewById(R.id.warning_box2);


        // final GradientDrawable gradientDrawable1 = (GradientDrawable) warningBox1.getBackground().mutate();
        // final GradientDrawable gradientDrawable2 = (GradientDrawable) warningBox2.getBackground().mutate();

        // Double warning_latitude;
        // Double warning_longitude;
        // Integer warning_degree;

        Cursor cursor = dbHelper.readDataset(database);

        while (cursor.moveToNext()) {

            //FOR EACH 'WARNING' ROW IN THE DATASET
            warning_latitude = cursor.getDouble(cursor.getColumnIndex(Dataset.DatasetEntry.LAT));
            warning_longitude = cursor.getDouble(cursor.getColumnIndex(Dataset.DatasetEntry.LONG));

            if ((warning_latitude - Dataset.DatasetEntry.RADIUS) < current_latitude && current_latitude < (warning_latitude + Dataset.DatasetEntry.RADIUS) &&
                    (warning_longitude - Dataset.DatasetEntry.RADIUS) < current_longitude && current_longitude < (warning_longitude + Dataset.DatasetEntry.RADIUS)) {


                Log.d("WARNINGRESULTFOUND", "EVVAAAAAAIII GOOOOOT IIIIIITTTT HEYAAAAHH");

                warning_degree = cursor.getInt(cursor.getColumnIndex(Dataset.DatasetEntry.WARN));
                warning_type = cursor.getInt(cursor.getColumnIndex(Dataset.DatasetEntry.CRASH));
                warning_street = cursor.getString(cursor.getColumnIndex(Dataset.DatasetEntry.STREET));

                int side = calculateWarningSide(current_degree, warning_degree); //destra, sinistra, frontale
                int color = calculateColorFromType(warning_type);
                int level = calculateLevelFromDistance(current_latitude, warning_latitude, current_longitude, warning_longitude);

                String warnLatitudeStr = "WARN LAT: " + Double.toString(warning_latitude);
                String warnLongitudeStr = "WARN LONG: " + Double.toString(warning_longitude);
                String warnDegreeStr = "WARN ABS DEG: " + Integer.toString(warning_degree);
                String warnStreetStr = "WARN STREET: " + warning_street;
                String degreeDiffStr = "WARN REL DEGREE: " + String.valueOf(degree_diff);

                outputDisplay(warnLongitudeStr, warnLongitudeStr, warnDegreeStr, warnStreetStr, degreeDiffStr, side, color, level);

                // WAIT a time before reset the screen
                Log.d("WARN:", "CODE reset");
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // reset output field
                        outputDisplay("","","","","",Dataset.DatasetEntry.FRONT, Color.GRAY, Dataset.DatasetEntry.MAX_LEVEL);
                        Log.d("DELAY:", "5second");
                    }
                }, Dataset.DatasetEntry.WARN_LASTING);

            }



        }

        dbHelper.close();
    }




    private Integer calculateWarningSide(int current_degree, int warning_degree)
    {
        int degree_diff = 0;

        Log.d("WARN SIDE", Integer.toString(warning_degree));
        if (0 <= warning_degree && warning_degree < 180) {
            Log.d("WARN BLA bLA", "BLA");
            if (warning_degree <= current_degree && current_degree < warning_degree + 180) {
                //sinistra - 1
                Log.d("SIDE", "SINISTRA 1");
                degree_diff = warning_degree - current_degree;
                return Dataset.DatasetEntry.SX;
            }
           else {
                //destra - 2
                Log.d("SIDE", "DESTRA 1");
                degree_diff = current_degree - warning_degree;
                return Dataset.DatasetEntry.DX;
            }
        }
        if (180 <= warning_degree && warning_degree < 360) {
            if (warning_degree <= current_degree && current_degree < 360 || 0 <= current_degree && current_degree < (warning_degree - 180)) {
                //sinistra
                if (current_degree < 360) {
                    degree_diff = current_degree + warning_degree;
                } else {
                    degree_diff = -(360 - warning_degree + current_degree);
                }
                Log.d("SIDE", "SINISTRA 2");
                return  Dataset.DatasetEntry.SX;
            } else {
                //destra
                degree_diff = warning_degree - current_degree;
                Log.d("SIDE", "DESTRA 2");
                return  Dataset.DatasetEntry.DX;
            }
        } else {
            //frontale
            // set warning degree to any number > 360
            Log.d("SIDE", "FRONTALE");
            return Dataset.DatasetEntry.FRONT;
        }


    }

    private Integer calculateColorFromType(int type){
        int color = Color.GRAY;
        switch (type){
            case 1:
                color = Color.RED;
                break;
            case 2:
                color = Color.GREEN;
                break;
            case 3:
                color = Color.BLUE;
                break;

        }
        return  color;
    }

    private  Integer calculateLevelFromDistance(Double x1, Double x2, Double y1, Double y2) {
        Double distance = Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
        Log.d("DISTANCE",Double.toString(distance));
        Double interval = Dataset.DatasetEntry.RADIUS / Dataset.DatasetEntry.MAX_LEVEL;
        Double level = distance / interval;
        int l = (int) Math.round(level);
        Log.d("LEVEL", Integer.toString(l));

        return l;
    }

    public void outputDisplay(String warnLongitudeStr, String warnLatitudeStr, String warnDegreeStr, String warnStreetStr, String degreeDiffStr, int side, int color, int level)
    {

        TextView warningBox1 = (TextView) findViewById(R.id.warning_box1);
        TextView warningBox1_2 = (TextView) findViewById(R.id.warning_box1_2);
        TextView warningBox1_3 = (TextView) findViewById(R.id.warning_box1_3);
        TextView warningBox1_4 = (TextView) findViewById(R.id.warning_box1_4);

        TextView warningBox2 = (TextView) findViewById(R.id.warning_box2);
        TextView warningBox2_2 = (TextView) findViewById(R.id.warning_box2_2);
        TextView warningBox2_3 = (TextView) findViewById(R.id.warning_box2_3);
        TextView warningBox2_4 = (TextView) findViewById(R.id.warning_box2_4);


        TextView warnStreet = (TextView) findViewById(R.id.warning_street);
        TextView warnLatitude = (TextView) findViewById(R.id.warning_latitude);
        TextView warnLongitude = (TextView) findViewById(R.id.warning_longitude);
        TextView warnDegree = (TextView) findViewById(R.id.warning_degree);
        TextView degreeDiff = (TextView) findViewById(R.id.degree_diff);


        if (warningBox1 == null || warningBox2 == null) {
            return;
        }

        warnLatitude.setText(warnLatitudeStr);
        warnLongitude.setText(warnLongitudeStr);
        warnDegree.setText(warnDegreeStr);
        warnStreet.setText(warnStreetStr);
        degreeDiff.setText(degreeDiffStr);

        GradientDrawable gradientDrawable1 = (GradientDrawable) warningBox1.getBackground().mutate();
        GradientDrawable gradientDrawable1_2 = (GradientDrawable) warningBox1_2.getBackground().mutate();
        GradientDrawable gradientDrawable1_3 = (GradientDrawable) warningBox1_3.getBackground().mutate();
        GradientDrawable gradientDrawable1_4 = (GradientDrawable) warningBox1_4.getBackground().mutate();


        GradientDrawable gradientDrawable2 = (GradientDrawable) warningBox2.getBackground().mutate();
        GradientDrawable gradientDrawable2_2 = (GradientDrawable) warningBox2_2.getBackground().mutate();
        GradientDrawable gradientDrawable2_3 = (GradientDrawable) warningBox2_3.getBackground().mutate();
        GradientDrawable gradientDrawable2_4 = (GradientDrawable) warningBox2_4.getBackground().mutate();


        switch (side) {
            case Dataset.DatasetEntry.SX:
                //level = 1 più distante
                if (level == 1) {
                    gradientDrawable1_4.setColor(color);
                }
                if(level == 2){
                    gradientDrawable1_4.setColor(color);
                    gradientDrawable1_3.setColor(color);
                }
                if(level==3){
                    gradientDrawable1_4.setColor(color);
                    gradientDrawable1_3.setColor(color);
                    gradientDrawable1_2.setColor(color);
                }
                if(level==4){
                    gradientDrawable1_4.setColor(color);
                    gradientDrawable1_3.setColor(color);
                    gradientDrawable1_2.setColor(color);
                    gradientDrawable1.setColor(color);
                }
                break;

            case Dataset.DatasetEntry.DX:
                if (level == 1) {
                    gradientDrawable2_4.setColor(color);
                }
                if(level == 2){
                    gradientDrawable2_4.setColor(color);
                    gradientDrawable2_3.setColor(color);
                }
                if(level==3){
                    gradientDrawable2_4.setColor(color);
                    gradientDrawable2_3.setColor(color);
                    gradientDrawable2_2.setColor(color);
                }
                if(level==4){
                    gradientDrawable2_4.setColor(color);
                    gradientDrawable2_3.setColor(color);
                    gradientDrawable2_2.setColor(color);
                    gradientDrawable2.setColor(color);
                }
                break;
            case Dataset.DatasetEntry.FRONT:
                if (level == 1) {
                    gradientDrawable1_4.setColor(color);
                    gradientDrawable2_4.setColor(color);
                }
                if(level == 2){
                    gradientDrawable1_4.setColor(color);
                    gradientDrawable1_3.setColor(color);
                    gradientDrawable2_4.setColor(color);
                    gradientDrawable2_3.setColor(color);
                }
                if(level==3){
                    gradientDrawable1_4.setColor(color);
                    gradientDrawable1_3.setColor(color);
                    gradientDrawable1_2.setColor(color);
                    gradientDrawable2_4.setColor(color);
                    gradientDrawable2_3.setColor(color);
                    gradientDrawable2_2.setColor(color);
                }
                if(level==4){
                    gradientDrawable1_4.setColor(color);
                    gradientDrawable1_3.setColor(color);
                    gradientDrawable1_2.setColor(color);
                    gradientDrawable1.setColor(color);
                    gradientDrawable2_4.setColor(color);
                    gradientDrawable2_3.setColor(color);
                    gradientDrawable2_2.setColor(color);
                    gradientDrawable2.setColor(color);
                }
                break;
        }

    }

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };




}
