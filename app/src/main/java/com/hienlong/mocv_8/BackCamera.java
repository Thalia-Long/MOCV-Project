package com.hienlong.mocv_8;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Spinner;
import android.widget.VideoView;

import java.util.Timer;


public class BackCamera extends ActionBarActivity {

        Spinner spinner1;
    //1.This is the extra code of IP camera
    public WebView webView;
    public Timer autoUpdate;
    //hardcode the url link to stream the video form IP camera


    VideoView videoView;
    //End 1.

    @Override
        protected void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.backcam);
            spinner1 = (Spinner)findViewById(R.id.spinner);
            ArrayAdapter adapter = ArrayAdapter.createFromResource(this,R.array.view,R.layout.spinner_item);
            spinner1.setAdapter(adapter);
            spinner1.setSelection(1);



//==========================================================================================
        //This is the code to display the video from IP camera

        MediaController controller=new MediaController(this);
        controller.show();
        // VideoView videoView= (VideoView) findViewById(R.id.videoView1);and this one
        videoView= (VideoView) findViewById(R.id.videoView1);
        videoView.setMediaController(null);

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
            @Override
            public void onCompletion(MediaPlayer mp){
                // invoke your activity here

            }
        });

        //End IP camera
//=============================================================================================

        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    spinner1.setSelection(1);
                    switch(position){
                        case 0:
                            Intent FrontCam = new Intent(view.getContext(), MainActivity.class);
                            startActivity(FrontCam);
                            break;
                        case 2:
                            Intent obd = new Intent(view.getContext(), OBDII.class);
                            startActivity(obd);
                            break;
                        case 3:
                            Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                                    Uri.parse("google.navigation:q=Wright+State+University+Dayton+OH"));
                            startActivity(intent);

//                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
//                                Uri.parse("http://maps.google.com/maps?saddr=20.344,34.34&daddr=20.5666,45.345"));
//                        startActivity(intent);
                            break;
                    }

                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    setContentView(R.layout.activity_main);
                }
            });

        }
    //===========================================================
    //This is connectivity method to connect to the internet, also for IP camera
    public void checkNetworkConnectivity() {
        // TODO Auto-generated method stub
        ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        final android.net.NetworkInfo wifi =
                connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        final android.net.NetworkInfo mobile =
                connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if( wifi.isConnected() || mobile.isConnected()){

        }
    }

    @Override
    public void onResume() {
        videoView.setVideoURI(Uri.parse(SettingsActivity.url));
        videoView.start();
        super.onResume();
    }
//===================================================================
   // public void saveButtonOnClick(View view) {

    //}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    //Responding to Setting button when it is clicked
    public void buttonOnClick(View view){
        Button button = (Button)view;
        Intent bcs = new Intent(view.getContext(), SettingsActivity.class);
        startActivity(bcs);
    }
}
