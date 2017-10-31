package com.example.angiopasqui.permissionchecker.privacyLeaks;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.security.KeyChain;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.example.angiopasqui.permissionchecker.R;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.List;

import javax.security.cert.X509Certificate;

/**
 * Created by Angiopasqui on 17/10/2017.
 */

public class PrivacyLeaksMain extends Activity {
    private static Context context = null;
    private static Activity activity = null;
    private static String TAG = "DEBUG";
    private SharedPreferences preferences;
    private AssetManager am;
    public ListView listView;
    public AppLeaksAdapter adapter;

    protected void onCreate(Bundle savedInstanceState) {
        Log.d("DEBUG","onCreate Activity");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.privacy_main);
        context = getBaseContext();
        activity = this;
        am = context.getAssets();

        listView = (ListView)findViewById(R.id.listAppLeaks);

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
        addAppInApapter();

        super.onResume();
    }

    public void addAppInApapter(){
        List<AppLeak> list = LocalVpnService2.getListaAppLeaks();
        if(list!=null) {
            for (AppLeak appLeak : list) {
                adapter.add(appLeak);
            }
        }
    }
}
