package it.francescoermini.warnmebackgroundapp;


import android.app.IntentService;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.util.List;
import java.util.UUID;

import static android.content.ContentValues.TAG;

/**
 *
 * start an IntentService when called startService(new Intent(this, BluetoothLeGatt.class).putExtra("device", dev)) in MainActivity
 * The intent service call onHandleIntent at creation time.
 *
* */

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BluetoothLeGatt extends IntentService {

    public static final String UUID_SERV = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";


    private BluetoothGatt mGatt;

    private List<BluetoothGattService> services;

    private BluetoothGattCharacteristic characteristicWrite_1;


    private boolean conn = false;


    //Timeout di connessione
    //se non riesco a connettermi al device entro tot secondi dalla chiamata disconnetto il GattClient
    Handler handlerConnessione = new Handler();
    long connectionTimeout = 50000;
    private Runnable runnableCodeConnessione = new Runnable() {
        @Override
        public void run() {

            if (!conn) {
                Log.e(TAG,"Timeout connessione dispositivo");
                //@TODO aggiungi notifica al MainActivity
                mGatt.disconnect();
            }
        }
    };


    /**
     * A constructor is required, and must call the super <code><a href="/reference/android/app/IntentService.html#IntentService(java.lang.String)">IntentService(String)</a></code>
     * constructor with a name for the worker thread.
     */
    public BluetoothLeGatt() {
            super("ReadingService");
    }

    /**
     *  2. onHandleIntent chiamato in fase di creazione del ServiceIntent.
     *  riceve intent creato nel MainActivity.
     *  Dal quel intent prende il BluetoothDevice (trovato dalla scansione) e passato tramite:
     *  service_intent = new Intent(this, BluetoothLeGatt.class).putExtra("device", dev);
     *  Quando questo metodo ritorna, il service finisce il suo ciclo di vita.
     */

    @Override
    protected void onHandleIntent(Intent intent) {
        BluetoothDevice device = (BluetoothDevice) intent.getExtras().get("device");
        getServices(device);
    }

    /**
     * 3. getServices chiamato da onHandelIntent
     * riceve il BluetoothDevice su cui provare la connessione tramite device.connectGatt()
     */

    public void getServices(BluetoothDevice device) {

        mGatt = device.connectGatt(this, true, gattCallback, BluetoothDevice.TRANSPORT_LE);
        //controlla l'avvenuta connessione al dispositivo entro 5 secondi
        handlerConnessione.postDelayed(runnableCodeConnessione, connectionTimeout);
    }


    /**
     *
     * 4. gattCallback chiamata da device.connectGatt(..., gattCallBack,..)
     * La callback seleziona gli stati della connessione:
     * Quando onConnectionStateChange porta nello status connesso, viene chiamato discoverService.
     *
     */

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    conn = true;
                    //broadcastUpdate(ACTION_GATT_CONNECTED);
                    gatt.discoverServices();
                    Log.i("gattCallback", "STATE_CONNECTED");
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e("gattCallback", "STATE_DISCONNECTED");
                    //@TODO Gestire la riconnessione automatica accidentale entro tot tempo, poi sconnetti gatt
                    //if ( status == BluetoothProfile.STATE_CONNECTED )
                    //   gatt.discoverServices();
                    break;
                default:
                    Log.e("gattCallback", "STATE_OTHER");
            }

        }

        /**
         *
         * 5. onServicesDiscovered chiamata da gatt.discoverServices() incaso di stato connesso, BluetoothProfile.STATE_CONNECTED
         *  La callback acquisce tutti i BluetoothGattService, seleziona il service di interesse.
         *  Su un dato service acquisice tutte le BluetoothCharacteristic.
         *
         *  Quando trova una characteristic di tipo Notify, effettua il subscribe a quella caratteristica.
         *  I messaggi di tipo Notify ricevuti su quella caratteristica verranno letti da onCharacteristicChange().
         *  osserva: Il subscribe avviene settando BluetoothGattDescriptor con uuid  2902, noto da specifiche BLE.
         *  osserva: che per ricevere in READ devi settare il descritore a ENABLE_INDICATION_VALUE oltre che a ENABLE_NOTIFICATION_VALUE
         *
         *  Quando trova una characteristic di tipo Write la salva in modo da poterla recuperare per l'invio di dati al dispositivo BLE.
         */

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            services = gatt.getServices();

            Log.d("onServicesDiscovered", services.toString());

            //configure Notification
            for ( BluetoothGattService service : services ) {
                if (service.getUuid().equals(UUID.fromString(UUID_SERV))) {
                    for (BluetoothGattCharacteristic characteristic1 : service.getCharacteristics()) {
                        Log.i("onServicesDiscovered", " characteristic=" + characteristic1.getUuid());

                        if ((characteristic1.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            Log.i("onServicesDiscovered", " notify characteristic=" + characteristic1.toString() );
                            for (BluetoothGattDescriptor descriptor : characteristic1.getDescriptors()) {
                                //nota: l'id sotto è specificato nelle spec del BLE
                                if ( descriptor == characteristic1.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))){
                                    Log.i("getDescriptor", "found 2902 descriptor");
                                }
                                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                gatt.writeDescriptor(descriptor);
                            }

                            if (gatt.setCharacteristicNotification(characteristic1, true) == false) {
                                Log.w(TAG, "Failed to set notify characteristic");
                            }
                        }


                        if ((characteristic1.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                            Log.i("onServicesDiscovered", " write charateritic=" + characteristic1.toString());
                            characteristicWrite_1 = characteristic1;
                            /*
                            characteristic1.setValue("Welcome message goes here");
                            gatt.writeCharacteristic(characteristic1);
                            */
                        }
                    }
                }
            }

            //broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
        }


        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
           // Log.i("onCharacteristicRead", characteristic.toString());
            final byte[] data = characteristic.getValue();

            if(status == BluetoothGatt.GATT_SUCCESS && data != null && data.length > 0) {
                String recv =  new String(data);
                System.out.println("Received broadcast: " + recv);

                //broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }

        }

        /**
         * 6. onCharacteriticChange chiamata quando viene ricevuto un messaggio Notify.
         * Il messaggio viene letto, e re-inviato al bluetooth device.
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

           // Log.i("onCharacteristicChanged", characteristic.toString());
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                String recv =  new String(data);
                System.out.println("Received: " + recv);
                //broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);

                // rispondi con lo stesso char!
                send(recv);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic,
                                          int status) {
           // Log.i("onCharacteristicWrite", characteristic.toString());
        }
    };


    private void send(String msg){
        //charateristic1_write è salvata nel onServiceDiscover in modo automatico
        // se ho bisognio di caratteristiche multiple in write devo fare una lista e poi selezionarle per UUID.

        System.out.println("Send " + msg);
        characteristicWrite_1.setValue(msg);
        mGatt.writeCharacteristic(characteristicWrite_1);
    }


    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if ( mGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mGatt == null) {
            return;
        }
        mGatt.close();
        mGatt = null;
    }

    /*
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // For all other profiles, writes the data formatted in HEX.
        final byte[] data = characteristic.getValue();
        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for (byte byteChar : data)
                stringBuilder.append(String.format("%02X", byteChar));
            intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
            // Log.d("BLE", String(data) + stringBuilder.toString());
        }
        Log.d("BLE", "broadcastUpdate");

        sendBroadcast(intent);
    }*/
}