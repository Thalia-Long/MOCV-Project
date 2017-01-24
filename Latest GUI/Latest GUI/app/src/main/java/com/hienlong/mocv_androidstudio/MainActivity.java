package com.hienlong.mocv_8;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
        setContentView(R.layout.activity_main);
        spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.view, R.layout.spinner_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 1:
                        Intent BackCam = new Intent(view.getContext(), BackCamera.class);
                        startActivity(BackCam);
                        break;
                    case 2:
                        Intent obd = new Intent(view.getContext(), OBDII.class);
                        startActivity(obd);
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

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        //super.onWindowFocusChanged(hasFocus);
        if(hasFocus){
            //Start the recording
            preview.takePicturePressed();
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
        super.onResume();
        mSensorManager.registerListener(preview, mSensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        orientationEventListener.enable();

        layoutUI();

        preview.onResume();
        //preview.takePicturePressed();
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
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
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

//            View view = findViewById(R.id.settings);
//            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
//            layoutParams.addRule(align_left, 0);
//            layoutParams.addRule(align_right, R.id.preview);
//            layoutParams.addRule(align_top, R.id.preview);
//            layoutParams.addRule(align_bottom, 0);
//            layoutParams.addRule(left_of, 0);
//            layoutParams.addRule(right_of, 0);
//            view.setLayoutParams(layoutParams);
//            view.setRotation(ui_rotation);
//
//            view = findViewById(R.id.gallery);
//            layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
//            layoutParams.addRule(align_top, R.id.preview);
//            layoutParams.addRule(align_bottom, 0);
//            layoutParams.addRule(left_of, R.id.settings);
//            layoutParams.addRule(right_of, 0);
//            view.setLayoutParams(layoutParams);
//            view.setRotation(ui_rotation);
//
//            view = findViewById(R.id.focus_mode);
//            layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
//            layoutParams.addRule(align_top, R.id.preview);
//            layoutParams.addRule(align_bottom, 0);
//            layoutParams.addRule(left_of, R.id.gallery);
//            layoutParams.addRule(right_of, 0);
//            view.setLayoutParams(layoutParams);
//            view.setRotation(ui_rotation);
//
//            view = findViewById(R.id.flash);
//            layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
//            layoutParams.addRule(align_top, R.id.preview);
//            layoutParams.addRule(align_bottom, 0);
//            layoutParams.addRule(left_of, R.id.focus_mode);
//            layoutParams.addRule(right_of, 0);
//            view.setLayoutParams(layoutParams);
//            view.setRotation(ui_rotation);
//
//            view = findViewById(R.id.switch_video);
//            layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
//            layoutParams.addRule(align_left, 0);
//            layoutParams.addRule(align_right, 0);
//            layoutParams.addRule(align_top, R.id.preview);
//            layoutParams.addRule(align_bottom, 0);
//            layoutParams.addRule(left_of, R.id.flash);
//            layoutParams.addRule(right_of, 0);
//            view.setLayoutParams(layoutParams);
//            view.setRotation(ui_rotation);
//
//            view = findViewById(R.id.switch_camera);
//            layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
//            layoutParams.addRule(align_left, 0);
//            layoutParams.addRule(align_right, 0);
//            layoutParams.addRule(align_top, R.id.preview);
//            layoutParams.addRule(align_bottom, 0);
//            layoutParams.addRule(left_of, R.id.switch_video);
//            layoutParams.addRule(right_of, 0);
//            view.setLayoutParams(layoutParams);
//            view.setRotation(ui_rotation);
//
//            view = findViewById(R.id.trash);
//            layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
//            layoutParams.addRule(align_top, R.id.preview);
//            layoutParams.addRule(align_bottom, 0);
//            layoutParams.addRule(left_of, R.id.switch_camera);
//            layoutParams.addRule(right_of, 0);
//            view.setLayoutParams(layoutParams);
//            view.setRotation(ui_rotation);
//
//            view = findViewById(R.id.share);
//            layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
//            layoutParams.addRule(align_top, R.id.preview);
//            layoutParams.addRule(align_bottom, 0);
//            layoutParams.addRule(left_of, R.id.trash);
//            layoutParams.addRule(right_of, 0);
//            view.setLayoutParams(layoutParams);
//            view.setRotation(ui_rotation);
//
//            view = findViewById(R.id.take_photo);
//            layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
//            layoutParams.addRule(align_left, 0);
//            layoutParams.addRule(align_right, R.id.preview);
//            view.setLayoutParams(layoutParams);
//            view.setRotation(ui_rotation);
//
//            view = findViewById(R.id.zoom);
//            layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
//            layoutParams.addRule(align_left, 0);
//            layoutParams.addRule(align_right, R.id.preview);
//            layoutParams.addRule(align_top, 0);
//            layoutParams.addRule(align_bottom, R.id.preview);
//            view.setLayoutParams(layoutParams);
//            if( relative_orientation != 0 ) {
//                view.setRotation(180.0f);
//            }
//            else {
//                view.setRotation(0.0f);
//            }
//        }
//        else {
//            View view = findViewById(R.id.switch_camera);
//            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
//            layoutParams.addRule(align_left, R.id.preview);
//            layoutParams.addRule(align_right, 0);
//            layoutParams.addRule(align_top, R.id.preview);
//            layoutParams.addRule(align_bottom, 0);
//            layoutParams.addRule(left_of, 0);
//            layoutParams.addRule(right_of, 0);
//            view.setLayoutParams(layoutParams);
//            view.setRotation(ui_rotation);
//
//            view = findViewById(R.id.switch_video);
//            layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
//            layoutParams.addRule(align_top, R.id.preview);
//            layoutParams.addRule(align_bottom, 0);
//            layoutParams.addRule(left_of, 0);
//            layoutParams.addRule(right_of, R.id.switch_camera);
//            view.setLayoutParams(layoutParams);
//            view.setRotation(ui_rotation);
//
//            view = findViewById(R.id.flash);
//            layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
//            layoutParams.addRule(align_top, R.id.preview);
//            layoutParams.addRule(align_bottom, 0);
//            layoutParams.addRule(left_of, 0);
//            layoutParams.addRule(right_of, R.id.switch_video);
//            view.setLayoutParams(layoutParams);
//            view.setRotation(ui_rotation);
//
//            view = findViewById(R.id.focus_mode);
//            layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
//            layoutParams.addRule(align_top, R.id.preview);
//            layoutParams.addRule(align_bottom, 0);
//            layoutParams.addRule(left_of, 0);
//            layoutParams.addRule(right_of, R.id.flash);
//            view.setLayoutParams(layoutParams);
//            view.setRotation(ui_rotation);
//
//            view = findViewById(R.id.gallery);
//            layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
//            layoutParams.addRule(align_top, R.id.preview);
//            layoutParams.addRule(align_bottom, 0);
//            layoutParams.addRule(left_of, 0);
//            layoutParams.addRule(right_of, R.id.focus_mode);
//            view.setLayoutParams(layoutParams);
//            view.setRotation(ui_rotation);
//
//            view = findViewById(R.id.settings);
//            layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
//            layoutParams.addRule(align_top, R.id.preview);
//            layoutParams.addRule(align_bottom, 0);
//            layoutParams.addRule(align_left, 0);
//            layoutParams.addRule(align_right, 0);
//            layoutParams.addRule(left_of, 0);
//            layoutParams.addRule(right_of, R.id.gallery);
//            view.setLayoutParams(layoutParams);
//            view.setRotation(ui_rotation);
//
//            view = findViewById(R.id.share);
//            layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
//            layoutParams.addRule(align_top, R.id.preview);
//            layoutParams.addRule(align_bottom, 0);
//            layoutParams.addRule(left_of, 0);
//            layoutParams.addRule(right_of, R.id.settings);
//            view.setLayoutParams(layoutParams);
//            view.setRotation(ui_rotation);
//
//            view = findViewById(R.id.trash);
//            layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
//            layoutParams.addRule(align_top, R.id.preview);
//            layoutParams.addRule(align_bottom, 0);
//            layoutParams.addRule(left_of, 0);
//            layoutParams.addRule(right_of, R.id.share);
//            view.setLayoutParams(layoutParams);
//            view.setRotation(ui_rotation);
//
//            view = findViewById(R.id.take_photo);
//            layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
//            layoutParams.addRule(align_left, R.id.preview);
//            layoutParams.addRule(align_right, 0);
//            view.setLayoutParams(layoutParams);
//            view.setRotation(ui_rotation);
//
//            view = findViewById(R.id.zoom);
//            layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
//            layoutParams.addRule(align_left, R.id.preview);
//            layoutParams.addRule(align_right, 0);
//            layoutParams.addRule(align_top, 0);
//            layoutParams.addRule(align_bottom, R.id.preview);
//            view.setLayoutParams(layoutParams);
//            if( relative_orientation == 180 ) {
//                view.setRotation(180.0f);
//            }
//            else {
//                view.setRotation(0.0f);
//            }
//        }
//
//        {
//            // set icon for taking photos vs videos
//            ImageButton view = (ImageButton)findViewById(R.id.take_photo);
//            if( preview != null ) {
//                view.setImageResource(preview.isVideo() ? R.drawable.take_video : R.drawable.take_photo);
//            }
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