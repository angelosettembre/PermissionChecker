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
import java.util.Arrays;
import java.util.List;
//


/**
 * Created by Angelo on 19/09/2017.
 */

public class AppDetails extends Activity {
    PermissionAdapter arrayAdapterDescription;
    ListView listPermission;
    private String appName;
    String packageName;
    private Bitmap bitmap;
    private Drawable iconPermission;
    int count=0;

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

        arrayAdapterDescription = new PermissionAdapter(this, R.layout.app_list_detail_item, new ArrayList<Permesso>());

        listPermission = (ListView) findViewById(R.id.appPermission);

        Intent i = getIntent();
        appName = i.getStringExtra("Nome app");
        bitmap = (Bitmap) this.getIntent().getParcelableExtra("Icona app");
        Drawable icon = new BitmapDrawable(getResources(), bitmap);
        packageName = i.getStringExtra("PACKAGE");
        getActionBar().setLogo(icon);
        getActionBar().setTitle(appName);

        Log.d("DEBUG", "Pacchetto2" + packageName);

        //GET PERMISSIONS
        PackageManager pm = getPackageManager();
        PackageInfo pi;
        PermissionInfo pemInfo=null;

        Permesso permesso = new Permesso();

        boolean check=false;

        PermissionGroupInfo groupInfo;

        try {
            pi = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
            String[] requestedPermissions = pi.requestedPermissions;

            if (requestedPermissions != null) {
                for (int j = 0; j < requestedPermissions.length; j++) {
                    try {
                        if (requestedPermissions[j].contains(("android.permission")) || requestedPermissions[j].contains("com.google") || requestedPermissions[j].contains("com.android.launcher")) {
                            permesso = new Permesso();

                            Log.d("test", requestedPermissions[j]);

                            pemInfo = pm.getPermissionInfo(requestedPermissions[j], 0);

                            check = true;

                            permesso.setName(pemInfo.name);
                            permesso.setDescription((String) pemInfo.loadDescription(pm));


                            System.out.println("NOME PERMESSOOOOOOOO " + pemInfo.name);

                            System.out.println("NOME GRUPPOOO " + pemInfo.group);

                            groupInfo = pm.getPermissionGroupInfo(pemInfo.group, 0);
                            Drawable icona = pm.getResourcesForApplication("android").getDrawable(groupInfo.icon);
                            permesso.setIcon(icona);

                           /* switch (pemInfo.name) {
                                case "android.permission.INTERNET":
                                    iconPermission = getResources().getDrawable(R.drawable.access_network_state_icon);
                                    //permesso.setIcon(iconPermission);
                            }*/
                            arrayAdapterDescription.add(permesso);
                            scanArray(arrayAdapterDescription);
                            check=false;
                            System.out.println("CONTINUA DOPO ECCEZIONE");
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                        System.out.println("ECCEZIONEEEEE");
                        if(check){
                            arrayAdapterDescription.add(permesso);
                            scanArray(arrayAdapterDescription);
                        }
                        check = false;

                        continue;
                    }


                }

            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        listPermission.setAdapter(arrayAdapterDescription);
    }


    public void scanArray(PermissionAdapter adapter){
        count++;
        for(int i=0; i<count; i++){
            System.out.println("ARRAY "+arrayAdapterDescription.getItem(i));
        }
    }
}
