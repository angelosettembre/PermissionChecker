package com.example.angiopasqui.permissionchecker;

import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity {
    ImageView icon_lock_unlcok;
    TextView textMonitoring;
    ImageView icon_listApp;
    TextView text_listApp;
    TextView text_init_Monitoring;
    ImageView start;
    ImageView stop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("DEBUG","DASDSADADSADADA");

        icon_lock_unlcok = (ImageView) findViewById(R.id.icon_lock_unlock);
        textMonitoring = (TextView) findViewById(R.id.monitoring);
        icon_listApp = (ImageView) findViewById(R.id.icon_listApp);
        text_listApp = (TextView) findViewById(R.id.text_listApp);
        text_init_Monitoring = (TextView) findViewById(R.id.text_initMonitoring);
        start = (ImageView) findViewById(R.id.start);
        stop = (ImageView) findViewById(R.id.stop);

        icon_listApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),AppList.class);
                startActivity(i);
            }
        });

        text_listApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),AppList.class);
                startActivity(i);
            }
        });

        /*if(Helper.isAppRunning(this,"com.example.angiopasqui.permissionchecker")) {
            Log.d("RUNNING", "App in ESECUZIONE");
        } else {
            Log.d("STOP", "App NON in esecuzione");
        }*/
    }
}
