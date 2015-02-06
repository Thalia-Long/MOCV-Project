package com.hienlong.mocv_androidstudio;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;


public class BackCamera extends ActionBarActivity {
        Spinner spinner1;
    @Override
        protected void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            spinner1 = (Spinner)findViewById(R.id.spinner);
            ArrayAdapter adapter = ArrayAdapter.createFromResource(this,R.array.view,android.R.layout.simple_spinner_item);
            spinner1.setAdapter(adapter);
             spinner1.setSelection(1);
            spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    switch(position){
                        case 0:
                            Intent FrontCam = new Intent(view.getContext(), MainActivity.class);
                            startActivity(FrontCam);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_backcam, menu);
        return true;
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
    //Responding to Setting button when it is clicked
    public void buttonOnClick(View view){
        Button button = (Button)view;
        Intent bcs = new Intent(view.getContext(), backCamSetting.class);
        startActivity(bcs);
    }
}
