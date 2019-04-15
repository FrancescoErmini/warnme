package it.francescoermini.volleytest;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class DatasetFragment extends Fragment {


    private TextView Txt_Display;


    public DatasetFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dataset, container, false);
        Txt_Display = view.findViewById(R.id.txt_display);
        readDataset();
        return  view;
    }

    private void readDataset() {
        DatasetDbHelper dbHelper = new DatasetDbHelper(getActivity());
        SQLiteDatabase database = dbHelper.getReadableDatabase();

        Cursor cursor = dbHelper.readDataset(database);
        String info="";
        while (cursor.moveToNext()){
           String latitude = Double.toString(cursor.getDouble(cursor.getColumnIndex(Dataset.DatasetEntry.LAT)));
           String longitude = Double.toString(cursor.getDouble(cursor.getColumnIndex(Dataset.DatasetEntry.LONG)));
           String warning = Integer.toString(cursor.getInt(cursor.getColumnIndex(Dataset.DatasetEntry.WARN)));
           String crash = Integer.toString(cursor.getInt(cursor.getColumnIndex(Dataset.DatasetEntry.CRASH)));
           String street = cursor.getString(cursor.getColumnIndex(Dataset.DatasetEntry.STREET));

            info = info + "\n\n" + "\nlat: " + latitude + "\nlong: " + longitude
                    + "\nwarn: " + warning + "\ncrash: " + crash + "\nstreet: " + street ;

        }
        Txt_Display.setText(info);
        dbHelper.close();
    }
}
