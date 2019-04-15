package it.francescoermini.warnmebackgroundapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import static android.content.ContentValues.TAG;

import android.widget.TextView;

import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static  final int scanDuration = 20000;
    public static final String deviceName = "ESP32";


    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mLeBluetoothScanner;
    private BluetoothDevice BlueFruit;
    private Intent service_intent;

    private boolean mConnected = false;

    private static final int REQUEST_ENABLE_BT = 1;
    private boolean mRequestingLocationUpdates;
    private static final int MY_PERMISSION_FINE_LOCATION = 123;

    // UI Widgets.
    private Button connectButton;
   // private Button disconnectButton;
   // private TextView mDataField;

    private boolean scan = false;
    private Handler handler = new Handler();
    // Define the code block to be executed
    private Runnable runnableCode = new Runnable() {
        @Override
        public void run() {
            // Do something here on the main thread
            if (!scan) {
                startScanning();
            } else {
                stopScanning();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //UI
       // mDataField = (TextView) findViewById(R.id.data_value);
        connectButton = findViewById(R.id.connect_button);
        connectButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("onClickListener", "start scanning..");
                startScanning();
            }
        });
        /*disconnectButton = findViewById(R.id.disconnect_button);
        disconnectButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("BLE", "onClick: attempting to disconnect");
                disconnectToBLE();
            }
        });*/


        //check if necessary permissions are enabled
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSION_FINE_LOCATION);
        } else {
            mRequestingLocationUpdates = true;
        }

        //Initialize Bluetooth adapter
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Ensures Bluetooth is available on the android device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        mLeBluetoothScanner = mBluetoothAdapter.getBluetoothLeScanner();

    }

    //chiamato pigiando su bottone connect
    public void startScanning() {
        System.out.println("scanning started");
        scan = true;
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                mLeBluetoothScanner.startScan(leScanCallback);
            }
        });

        handler.postDelayed(runnableCode, scanDuration);
    }

    public void stopScanning() {

        scan = false;
        System.out.println("scanning ended");
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                mLeBluetoothScanner.stopScan(leScanCallback);
            }
        });


        if (BlueFruit != null) {
            Log.d(TAG, " trovato dispositivo con nome " + deviceName);
            connectToBLE(BlueFruit);
        }
        else {
            Log.d(TAG, " impossibile trovare dispositivo BLE con nome " + deviceName);
        }

    }
    // lo scan assegna a BlueFruit (di tipo BluetoothDevice) il device con nome ESP32 oppure device rimane null.
    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {

            if (result.getDevice().getName() != null && (result.getDevice().getName().equals(deviceName) )) {

                BlueFruit = result.getDevice();
            }

        }
    };


    public void connectToBLE(BluetoothDevice dev) {

        Log.d(TAG,"BLE connected to " + dev.getName() + " with address " +  dev.getAddress() );

        service_intent = new Intent(this, BluetoothLeGatt.class).putExtra("device", dev);
        startService(service_intent);
    }

    public void disconnectToBLE(){
        if (  mConnected )
            stopService(service_intent);
    }


    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    /*
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeGatt.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                //updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeGatt.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
               // updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
               // clearUI();
            } else if (BluetoothLeGatt.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                // displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeGatt.ACTION_DATA_AVAILABLE.equals(action)) {
                //displayData(intent.getStringExtra(BluetoothLeGatt.EXTRA_DATA));
            }
        }
    };*/

    /*
    private void displayData(String data) {
        if (data != null) {
            mDataField.setText(data);
        }
    }
    */

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_FINE_LOCATION: {
                if(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission granted
                    Log.d("PERMISSSION", "onRequestPermissionsResult: Permission granted by user");
                    mRequestingLocationUpdates = true;
                } else {
                    mRequestingLocationUpdates = false;
                }
            }
        }
    }

    /*
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeGatt.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeGatt.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeGatt.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeGatt.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
    */



}
