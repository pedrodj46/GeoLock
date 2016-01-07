package com.michele.appdegree;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.michele.fragmentexample.R;

/**
 * Created by mattia on 29/11/15.
 */
public class loginFragment  extends Fragment {

    // inizio interfaccia di collegamento con la main activity
    ToolbarListener activityCallback;

    public interface ToolbarListener {
        public void onButtonLogin();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            activityCallback = (ToolbarListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ToolbarListener");
        }
    }
    // fine interfaccia di collegamento con la main activity

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login,
                container, false);

        final Button loginSend = (Button) view.findViewById(R.id.loginSend);
        loginSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                buttonClicked(v);
            }
        });

        return view;

    }

    public void buttonClicked (View view) {
        activityCallback.onButtonLogin();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu,  MenuInflater inflate) {
        menu.clear();
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.menu_vuoto, menu);
    }

}
