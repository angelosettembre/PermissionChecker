package com.example.angiopasqui.permissionchecker;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.angiopasqui.permissionchecker.privacyLeaks.GlobalState;
import com.example.angiopasqui.permissionchecker.privacyLeaks.LocalVpnService;
import com.example.angiopasqui.permissionchecker.privacyLeaks.PrivacyLeaksMain;

public class MainActivity extends Activity {
    ImageView icon_lock_unlcok;
    TextView textMonitoring;
    ImageView icon_listApp;
    TextView text_listApp;
    TextView text_init_Monitoring;
    ImageView start;
    ImageView stop;
    Button goPrivacyLeaksActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setLogo(R.mipmap.ic_launcher);
        actionBar.setDisplayUseLogoEnabled(true);

        icon_lock_unlcok = (ImageView) findViewById(R.id.icon_lock_unlock);
        textMonitoring = (TextView) findViewById(R.id.monitoring);
        icon_listApp = (ImageView) findViewById(R.id.icon_listApp);
        text_listApp = (TextView) findViewById(R.id.text_listApp);
        text_init_Monitoring = (TextView) findViewById(R.id.text_initMonitoring);
        start = (ImageView) findViewById(R.id.start);
        stop = (ImageView) findViewById(R.id.stop);
        goPrivacyLeaksActivity = (Button) findViewById(R.id.goPrivacyLeaksActivity);

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

        goPrivacyLeaksActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),PrivacyLeaksMain.class);
                startActivity(i);
            }
        });

    }

    @Override
    protected void onDestroy() {
        Log.d("DEBUG","onDestroy MainActivity");

        super.onDestroy();
    }
}
