package com.example.angiopasqui.permissionchecker;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
//


/**
 * Created by Angelo on 19/09/2017.
 */

public class AppDetails extends Activity {
    ArrayAdapter<String> arrayAdapter;
    ListView listView;
    private TextView nomeApp;
    private String appName;
    String packageName;
    private Bitmap bitmap;
    private ImageView iconaApp;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_list_detail);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setDisplayUseLogoEnabled(true);


        arrayAdapter = new ArrayAdapter<String>(this, R.layout.app_list_detail_item, R.id.permissionName);

        listView = (ListView) findViewById(R.id.appPermission);

        nomeApp = (TextView) findViewById(R.id.appNameDetails);
        iconaApp = (ImageView) findViewById(R.id.appIconDetails);

        Intent i = getIntent();
        appName = i.getStringExtra("Nome app");
        bitmap = (Bitmap) this.getIntent().getParcelableExtra("Icona app");
        nomeApp.setText(appName);
        Drawable icon = new BitmapDrawable(getResources(),bitmap);
        iconaApp.setImageDrawable(icon);
        packageName = i.getStringExtra("PACKAGE");
        getActionBar().setLogo(icon);
        getActionBar().setTitle(appName);

        Log.d("DEBUG","Pacchetto2"+packageName);

        //GET PERMISSIONS
        PackageManager pm = getPackageManager();
        PackageInfo pi;
        try {
            pi = pm.getPackageInfo(packageName,PackageManager.GET_PERMISSIONS);
            String[] requestedPermissions = pi.requestedPermissions;
            if(requestedPermissions != null) {
                for (int j = 0; j < requestedPermissions.length; j++) {
                    Log.d("test", requestedPermissions[j]);
                    arrayAdapter.add(requestedPermissions[j]);
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        listView.setAdapter(arrayAdapter);
    }
}
