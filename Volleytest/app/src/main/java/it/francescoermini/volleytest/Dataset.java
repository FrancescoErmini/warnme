package it.francescoermini.volleytest;

public final class Dataset {

    private Dataset(){}

    public static class DatasetEntry {

        public  static  final String TABLE_NAME = "dataset";



        //public  static  final  Double RADIUS_METER = 3.0;

        public static final  Double RADIUS = 0.0004; //adattare metri alla precisione delle coordinate

        public static final String LAT = "lat";
        public static final String LONG = "long";
        public static final String WARN = "warn";
        public static final String CRASH = "crash";
        public static final String STREET = "street";



        public static final long delay = 3000; //tempo di campionameto del GPS
        public static final long WARN_LASTING = 5000; //durata del messaggio di warning prima di svanire
        public static final int FRONT = 3;
        public static final int SX = 1;
        public static final int DX = 2;
        public  static  final int MAX_LEVEL = 4;


        public static final String TABLE_BLE_DEV = "ble_devices";
        public static final String BLE_ADDRESS = "ble_address";
        public static final String BLE_NAME = "ble_name";

    }
}


