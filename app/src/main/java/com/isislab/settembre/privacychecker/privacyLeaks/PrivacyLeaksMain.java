package com.isislab.settembre.privacychecker.privacyLeaks;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import com.isislab.settembre.privacychecker.R;

import java.util.List;

/**
 * Created by Angiopasqui on 17/10/2017.
 */

public class PrivacyLeaksMain extends Activity {
    private static Context context = null;
    private static Activity activity = null;
    private static String TAG = "DEBUG";
    public ListView listView;
    public AppLeaksAdapter adapter;
    public int numPacket;
    private TextView countPacket;
    private TextView countBlocked;

    protected void onCreate(Bundle savedInstanceState) {
        Log.d("DEBUG","onCreate Activity");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.privacy_main);
        countPacket = (TextView) findViewById(R.id.countPack);
        context = getBaseContext();
        activity = this;

        listView = (ListView)findViewById(R.id.listAppLeaks);
        countBlocked = (TextView) findViewById(R.id.countBlock);
        adapter = new AppLeaksAdapter(this,R.layout.listappleaks_detail_item);

        listView.setAdapter(adapter);


    }

    @Override
    protected void onDestroy() {
        Log.d("DEBUG","onDestroy PrivacyActivity");
        super.onDestroy();
    }

    @Override
    protected void onRestart() {
        Log.d("DEBUG","onRestart Activity");

        super.onRestart();
    }

    @Override
    protected void onResume() {
        Log.d("DEBUG","onResume Activity");
        adapter = new AppLeaksAdapter(this,R.layout.listappleaks_detail_item);
        addAppInApapter();
        listView.setAdapter(adapter);
        super.onResume();
    }

    public void addAppInApapter(){
        List<AppLeak> list = LocalVpnService.getListaAppLeaks();
        if(list!=null) {
            for (AppLeak appLeak : list) {
                adapter.add(appLeak);
            }
        }
        countPacket.setText(String.valueOf(LocalVpnService.getCountPacket()));
        countBlocked.setText(String.valueOf(LocalVpnService.getCountBlocked()));
        System.out.println("Testoosoasos: "+LocalVpnService.getCountPacket());
    }
}
