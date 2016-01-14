package com.michele.appdegree;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.michele.fragmentexample.R;

/**
 * Created by mattia on 29/11/15.
 */
public class loginFragment  extends Fragment {

    public static final String MY_PREFS_NAME = "ricordamiLogin";

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

        getActivity().getActionBar().hide();

        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login,
                container, false);

        EditText loginUsername = (EditText) view.findViewById(R.id.loginUsername);
        SharedPreferences prefs = getActivity().getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        if(prefs.getString("username", "0") != "0"){
            String username = new String(Base64.decode(prefs.getString("username", "0"), Base64.DEFAULT));
            loginUsername.setText(username);
        }

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
