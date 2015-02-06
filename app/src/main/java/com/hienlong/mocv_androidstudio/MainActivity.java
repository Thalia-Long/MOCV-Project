package com.hienlong.mocv_androidstudio;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity{

    Spinner spinner;
    Button fcSettingBtn;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        spinner = (Spinner)findViewById(R.id.spinner);
        ArrayAdapter adapter = ArrayAdapter.createFromResource(this,R.array.view,android.R.layout.simple_spinner_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            switch(position){
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

    }
    //Responding to Setting button when it is clicked
    public void buttonOnClick(View view){
        Button button = (Button)view;
        Intent fcs = new Intent(view.getContext(), frontCamSetting.class);
        startActivity(fcs);
    }


}
