package com.example.angiopasqui.permissionchecker;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.security.Permission;
import java.util.ArrayList;
import java.util.List;
//


/**
 * Created by Angelo on 19/09/2017.
 */

public class AppDetails extends Activity {
    ArrayAdapter<String> arrayAdapter;
    ListView listView;
    private String appName;
    String packageName;
    private Bitmap bitmap;

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

        Intent i = getIntent();
        appName = i.getStringExtra("Nome app");
        bitmap = (Bitmap) this.getIntent().getParcelableExtra("Icona app");
        Drawable icon = new BitmapDrawable(getResources(),bitmap);
        packageName = i.getStringExtra("PACKAGE");
        getActionBar().setLogo(icon);
        getActionBar().setTitle(appName);

        Log.d("DEBUG","Pacchetto2"+packageName);

        //GET PERMISSIONS
        /*PackageManager pm = getPackageManager();
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
        }*/ //FUNZIONANTE

        /*SOLUZIONE NON FUNZIONANTE
        PackageManager pm = getPackageManager();
        try {
            PackageInfo pi;
            pi = pm.getPackageInfo(packageName,PackageManager.GET_PERMISSIONS);
            Log.d("DEBUG","Nome PACK: "+packageName);
            String[] requestedPermissions = pi.requestedPermissions;
            Log.d("DEBUG","Valore d: "+requestedPermissions.length);
            for(int z=0;z<requestedPermissions.length;z++){
                if(requestedPermissions != null) {
                    String permName = requestedPermissions[z];
                    Log.d("DEBUG", "Nomeee: " + permName);
                    PermissionInfo pemInfo = pm.getPermissionInfo(permName, 0);
                    PermissionGroupInfo permGroupInfo = pm.getPermissionGroupInfo(pemInfo.group, 0);
                    Log.d("DEBUG", "Stampa 1: " + permName);
                    Log.d("DEBUG", "Stampa 2: " + permGroupInfo.loadLabel(pm).toString());
                    Log.d("DEBUG", "Stampa 3: " + pemInfo.loadLabel(pm).toString());
                    Log.d("DEBUG", "Stampa 3: " + pemInfo.loadDescription(pm).toString());
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }*/

        PackageManager pm = getPackageManager();
        List<PermissionInfo> permissions = new ArrayList<>();

        List<PermissionGroupInfo> groupList = pm.getAllPermissionGroups(0);
        groupList.add(null); // ungrouped permissions

        for (PermissionGroupInfo permissionGroup : groupList) {
            String name;
            if(permissionGroup == null){
                name = null;
            }
            else{
                name = permissionGroup.name;
            }
            try {
                permissions.addAll(pm.queryPermissionsByGroup(name, 0));
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }

        for(PermissionInfo asd:permissions){
            Log.d("DEBUG","Provolaaa: "+asd.name);
            Log.d("DEBUG","Provolaaa 2: "+asd.group);
        }
        //listView.setAdapter(arrayAdapter);
    }
}
