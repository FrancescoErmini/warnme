package it.francescoermini.volleytest;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment implements  View.OnClickListener {

    private Button BnView, BnUpdate;

    OnDbOpListener dbOpListener;

    public HomeFragment() {
        // Required empty public constructor
    }

    //interfaccia per far comunicare fragment home con activity
    public interface OnDbOpListener {
        public  void dBOpPerformed(int method);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        BnView = view.findViewById( R.id.bn_view_dataset);
        BnView.setOnClickListener(this);

        BnUpdate = view.findViewById(R.id.bn_update_dataset);
        BnUpdate.setOnClickListener(this);
        return  view;
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.bn_view_dataset:
                dbOpListener.dBOpPerformed(0);
                break;

            case R.id.bn_update_dataset:
                dbOpListener.dBOpPerformed(1);
                break;

        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity activity = (Activity) context;
        try {
            dbOpListener = (OnDbOpListener) activity;
        } catch(ClassCastException e){
            throw new ClassCastException(activity.toString() + "must implement the interface method..");
        };
    }
}
