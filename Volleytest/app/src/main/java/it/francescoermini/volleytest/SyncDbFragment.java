package it.francescoermini.volleytest;


import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class SyncDbFragment extends Fragment {


    private TextView jsonText;
    public SyncDbFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sync_db, container, false);
        jsonText = view.findViewById(R.id.json_text);

        getJson();

        return view;
    }


    public  void  getJson() {


            RequestQueue rq; //json
            String url = "http:/francescoermini.it/dataset/data.php"; //json
            //String url = "http://192.168.1.155:8888/data.php"; //json
            rq = Volley.newRequestQueue(getActivity().getApplicationContext());


            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                    String jsonStr = response.toString();
                    Log.d("JSON: ", jsonStr);
                    jsonText.setText(jsonStr);

                    initDB(response);

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });

            rq.add(jsonObjectRequest);

    }

    public  void initDB(JSONObject jsonObject) {

        Log.d("DB","insert data from JSON to DB");

        DatasetDbHelper dbHelper = new  DatasetDbHelper(getActivity());
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        dbHelper.setDatasetFromJson(jsonObject, database);
        dbHelper.close();
        Log.d("DB","close DB");


    }


    private void getFakeJson() {

        Log.d("JSON: ", "CALL TO GET FAKE JSON");

        JSONObject student1 = new JSONObject();
        try {
            student1.put("LAT", "43.720001");
            student1.put("LONG", "11.280001");
            student1.put("WARN", "90");
            student1.put("CRASH", "1");
            student1.put("STREET", "Via Bikila");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        JSONObject student2 = new JSONObject();
        try {
            student2.put("LAT", "43.722222");
            student2.put("LONG", "11.282222");
            student2.put("WARN", "22");
            student2.put("WARN", "180");
            student2.put("CRASH", "2");
            student2.put("STREET", "Via Tizzano");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        JSONArray jsonArray = new JSONArray();

        jsonArray.put(student1);
        jsonArray.put(student2);

        JSONObject studentsObj = new JSONObject();
        try {
            studentsObj.put("warnings", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        String jsonString = studentsObj.toString();

        Log.d("JSON: ", jsonString);
        //jsonStr.setText(jsonString);

        //initDB(studentsObj);
    }
}
