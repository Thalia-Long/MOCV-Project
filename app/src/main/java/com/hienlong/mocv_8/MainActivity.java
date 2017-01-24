package com.hienlong.mocv_8;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

class MyDebug {
    static final boolean LOG = false;
}

public class MainActivity extends ActionBarActivity {
    private static final String TAG = "MainActivity";
    Spinner spinner;
    Button fcSettingBtn;
    private Preview preview = null;
    private boolean supports_auto_stabilise = false;
    private SensorManager mSensorManager = null;
    private Sensor mSensorAccelerometer = null;
    private OrientationEventListener orientationEventListener = null;
    private int current_orientation = 0;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i= new Intent(this.getApplicationContext(), BackgroundVideoRecorder.class);
        this.getApplicationContext().stopService(i);

        setContentView(R.layout.activity_main);
        spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.view, R.layout.spinner_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinner.setSelection(0);
                switch (position) {
                    case 1:
                        Intent BackCam = new Intent(view.getContext(), BackCamera.class);
                        startActivity(BackCam);
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

        preview = new Preview(this, savedInstanceState);
        ((FrameLayout) findViewById(R.id.preview)).addView(preview);

        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        if( activityManager.getLargeMemoryClass() >= 128 ) {
            supports_auto_stabilise = true;
        }
        orientationEventListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {
                //MainActivity.this.onOrientationChanged(orientation);
            }
        };

        // keep screen active - see http://stackoverflow.com/questions/2131948/force-screen-on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // set screen to max brightness - see http://stackoverflow.com/questions/11978042/android-screen-brightness-max-value
        {
            WindowManager.LayoutParams layout = getWindow().getAttributes();
            layout.screenBrightness = 1.0f;
            getWindow().setAttributes(layout);
        }

        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        if( mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null ) {
            if( MyDebug.LOG )
                Log.d(TAG, "found accelerometer");
            mSensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        else {
            if( MyDebug.LOG )
                Log.d(TAG, "no support for accelerometer");
        }
    }

    protected void onStop() {
        super.onStop();
        // use this to start and trigger a service
        Intent i= new Intent(this.getApplicationContext(), BackgroundVideoRecorder.class);
        // potentially add data to the intent
        //i.putExtra("KEY1", "Value to be used by the service");
        this.getApplicationContext().startService(i);
    }

    protected void onRestart() {
        super.onRestart();
        Intent i= new Intent(this.getApplicationContext(), BackgroundVideoRecorder.class);
        this.getApplicationContext().stopService(i);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        //super.onWindowFocusChanged(hasFocus);
        if(hasFocus){
            //Start the recording
            final int volume = mute();

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    preview.takePicturePressed();
                    unmute(volume);
                }
            }, 0);
        }
    }

    //Responding to Setting button when it is clicked
    public void buttonOnClick(View view) {
        Button button = (Button) view;
        Intent fcs = new Intent(view.getContext(), frontCamSetting.class);
        startActivity(fcs);
    }

    @Override
    protected void onResume() {
        if( MyDebug.LOG )
            Log.d(TAG, "onResume");
        Intent i= new Intent(this.getApplicationContext(), BackgroundVideoRecorder.class);
        this.getApplicationContext().stopService(i);
        super.onResume();

        mSensorManager.registerListener(preview, mSensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        orientationEventListener.enable();

        layoutUI();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                preview.onResume();
                //preview.takePicturePressed();
            }
        }, 0);

    }

    public void saveButtonOnClick(View view) {

    }

    public void exitButtonOnClick(View view) {
        finish();
        System.exit(0);
    }
    public int mute()
    {
        AudioManager audioMgr = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        int oldStreamVolume = audioMgr.getStreamVolume(AudioManager.STREAM_RING);
        audioMgr.setStreamVolume(AudioManager.STREAM_RING, 0, 0);
        return oldStreamVolume;
    }
    public void unmute(int volume)
    {
        AudioManager audioMgr = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        audioMgr.setStreamVolume(AudioManager.STREAM_RING, volume, AudioManager.FLAG_PLAY_SOUND);
        //audioMgr.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_RAISE, 0);
    }

    @SuppressWarnings("deprecation")
    public static long freeMemory() { // return free memory in MB
        try {
            StatFs statFs = new StatFs(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath());
            // cast to long to avoid overflow!
            long blocks = statFs.getAvailableBlocks();
            long size = statFs.getBlockSize();
            long free = (blocks * size) / 1048576;
			/*if( MyDebug.LOG ) {
				Log.d(TAG, "freeMemory blocks: " + blocks + " size: " + size + " free: " + free);
			}*/
            return free;
        } catch (IllegalArgumentException e) {
            // can fail on emulator, at least!
            return -1;
        }
    }

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private File getImageFolder() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String folder_name = sharedPreferences.getString("preference_save_location", "MOCV");
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), folder_name);
        if (MyDebug.LOG) {
            Log.d(TAG, "folder_name: " + folder_name);
            Log.d(TAG, "full path: " + file);
        }
        return file;
    }

    /**
     * Create a File for saving an image or video
     */
    @SuppressLint("SimpleDateFormat")
    public File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = getImageFolder();
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                if (MyDebug.LOG)
                    Log.e(TAG, "failed to create directory");
                return null;
            }
            this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.fromFile(mediaStorageDir)));
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd'T'HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "MOCV_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        if (MyDebug.LOG) {
            Log.d(TAG, "getOutputMediaFile returns: " + mediaFile);
        }
        return mediaFile;
    }
    public boolean supportsAutoStabilise() {
        return this.supports_auto_stabilise;
    }

    private void layoutUI() {
        if( MyDebug.LOG )
            Log.d(TAG, "layoutUI");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String ui_placement = sharedPreferences.getString("preference_ui_placement", "ui_right");
        boolean ui_placement_right = ui_placement.equals("ui_right");
        if( MyDebug.LOG )
            Log.d(TAG, "ui_placement: " + ui_placement);
		// new code for orientation fixed to landscape
        if( getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ) {
            // despite being fixed to orientation, the app is switched to portrait when the screen is blanked
            if( MyDebug.LOG ) {
                Log.d(TAG, "unexpected portrait mode");
            }
            return;
        }
        // the display orientation should be locked to landscape, but how many degrees is that?
        int rotation = this.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }
        // getRotation is anti-clockwise, but current_orientation is clockwise, so we add rather than subtract
        // relative_orientation is clockwise from landscape-left
        //int relative_orientation = (current_orientation + 360 - degrees) % 360;
        int relative_orientation = (current_orientation + degrees) % 360;
        if( MyDebug.LOG ) {
            Log.d(TAG, "    current_orientation = " + current_orientation);
            Log.d(TAG, "    degrees = " + degrees);
            Log.d(TAG, "    relative_orientation = " + relative_orientation);
        }
        int ui_rotation = (360 - relative_orientation) % 360;
        preview.setUIRotation(ui_rotation);
        int align_left = RelativeLayout.ALIGN_LEFT;
        int align_right = RelativeLayout.ALIGN_RIGHT;
        int left_of = RelativeLayout.LEFT_OF;
        int right_of = RelativeLayout.RIGHT_OF;
        int align_top = RelativeLayout.ALIGN_TOP;
        int align_bottom = RelativeLayout.ALIGN_BOTTOM;
        if( ( relative_orientation == 0 && ui_placement_right ) || ( relative_orientation == 180 && ui_placement_right ) || relative_orientation == 90 || relative_orientation == 270) {
            if (!ui_placement_right && (relative_orientation == 90 || relative_orientation == 270)) {
                align_top = RelativeLayout.ALIGN_BOTTOM;
                align_bottom = RelativeLayout.ALIGN_TOP;
            }
        }
    }

    @Override
    protected void onPause() {
        if( MyDebug.LOG )
            Log.d(TAG, "onPause");
        super.onPause();
        mSensorManager.unregisterListener(preview);
        orientationEventListener.disable();
        preview.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        if( MyDebug.LOG )
            Log.d(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(state);
        if( this.preview != null ) {
            int cameraId = preview.getCameraId();
            if( MyDebug.LOG )
                Log.d(TAG, "save cameraId: " + cameraId);
            state.putInt("cameraId", cameraId);
        }
    }
}