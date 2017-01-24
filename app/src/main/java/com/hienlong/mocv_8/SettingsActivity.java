package com.hienlong.mocv_8;
/*
This is Hien newest version with IP camera working and IP cam setting where
we can put url manually
 */

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsActivity extends ActionBarActivity {

    public static String ip = "192.168.1.200:554";
    public static String user = "admin";
    public static String pass = "";
    public static String port = "";
    public static String url = "rtsp://"+ip+"/user="+user+"&password="+pass+"&channel=1&stream=1.sdp?";
    public static final String PREF_NAME = "AppPreferences";
    EditText aip,aport,auser,apass;
    Button save;
    SharedPreferences settings;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.settingsbackcammanuallyt);
        this.settings = getSharedPreferences(PREF_NAME,
                MODE_PRIVATE);
        aip = (EditText) findViewById(R.id.ip);
        aip.setText(ip);
        aport = (EditText) findViewById(R.id.port);
        aport.setText(port);
        auser = (EditText) findViewById(R.id.us);
        auser.setText(user);
        apass = (EditText) findViewById(R.id.ps);
        apass.setText(pass);
        save = (Button)findViewById(R.id.saveb);

        save.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub

                String lip=	aip.getText().toString();
                SettingsActivity.ip=lip;
                String lport=	aport.getText().toString();
                SettingsActivity.port=lport;
                String luser=	auser.getText().toString();
                SettingsActivity.user=luser;
                String lpass=	apass.getText().toString();
                SettingsActivity.pass=lpass;
                SharedPreferences settings = getSharedPreferences(
                        PREF_NAME, MODE_PRIVATE);
                Editor editor = settings.edit();
                editor.putString(SettingsActivity.ip, lip);
                editor.putString(SettingsActivity.port, lport);
                editor.putString(SettingsActivity.user, luser);
                editor.putString(SettingsActivity.pass, lpass);
                editor.commit();
                Toast.makeText(SettingsActivity.this, "Link Saved", Toast.LENGTH_LONG).show();
            }
        });
    }


}

// public void buttonOnClick(View view) {
//   save = (Button) view;
// String ls = linkstr.getText().toString();
//BackCamera.url = ls;

// }

