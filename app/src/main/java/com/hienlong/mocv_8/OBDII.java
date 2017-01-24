package com.hienlong.mocv_8;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;


public class OBDII extends ActionBarActivity implements SensorEventListener {
    Spinner spinner1;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.obd2);
        spinner1 = (Spinner)findViewById(R.id.spinner);
        ArrayAdapter adapter = ArrayAdapter.createFromResource(this,R.array.view,R.layout.spinner_item);
        spinner1.setAdapter(adapter);
        spinner1.setSelection(2);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);


        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinner1.setSelection(2);
                switch(position){
                    case 0:
                        Intent FrontCam = new Intent(view.getContext(), MainActivity.class);
                        startActivity(FrontCam);
                        break;
                    case 1:
                        Intent BackCam = new Intent(view.getContext(), BackCamera.class);
                        startActivity(BackCam);
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



    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // can be safely ignored for this demo
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_obd2, menu);
        return true;
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        TextView tvA = (TextView) findViewById(R.id.textViewA);
        TextView tvG = (TextView) findViewById(R.id.textViewG);

        TextView tvX = (TextView) findViewById(R.id.x_axis);
        TextView tvY = (TextView) findViewById(R.id.y_axis);
        TextView tvZ = (TextView) findViewById(R.id.z_axis);
        ProgressBar pbX = (ProgressBar) findViewById(R.id.progressBar);
        ProgressBar pbY = (ProgressBar) findViewById(R.id.progressBar2);
        ProgressBar pbZ = (ProgressBar) findViewById(R.id.progressBar3);
        TextView tvGX = (TextView) findViewById(R.id.x_axisG);
        TextView tvGY = (TextView) findViewById(R.id.y_axisG);
        TextView tvGZ = (TextView) findViewById(R.id.z_axisG);
        ProgressBar pbGX = (ProgressBar) findViewById(R.id.progressBar4);
        ProgressBar pbGY = (ProgressBar) findViewById(R.id.progressBar5);
        ProgressBar pbGZ = (ProgressBar) findViewById(R.id.progressBar6);
        if (mAccelerometer == null) {
            tvA.setText("Accelerometer (None detected)");
        }
        if (mGyroscope == null) {
            tvG.setText("Gyroscope (None detected)");
        }
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            tvX.setText(Float.toString(x));
            tvY.setText(Float.toString(y));
            tvZ.setText(Float.toString(z));
            pbX.setProgress((int) ((x + 5) * 10));
            pbY.setProgress((int) ((y + 5) * 10));
            pbZ.setProgress((int) ((z + 5) * 10));
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            tvGX.setText(Float.toString(x));
            tvGY.setText(Float.toString(y));
            tvGZ.setText(Float.toString(z));
            pbGX.setProgress((int) ((x + 5) * 10));
            pbGY.setProgress((int) ((y + 5) * 10));
            pbGZ.setProgress((int) ((z + 5) * 10));
        }
    }

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
}

