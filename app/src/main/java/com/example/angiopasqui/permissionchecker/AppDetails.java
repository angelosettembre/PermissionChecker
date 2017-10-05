package com.example.angiopasqui.permissionchecker;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
//


/**
 * Created by Angelo on 19/09/2017.
 */

public class AppDetails extends Activity {
    ArrayAdapter<String> arrayAdapterGroup;
    PermissionAdapter arrayAdapterDescription;

    ListView listGroups;
    ListView listDescription;
    private String appName;
    String packageName;
    private Bitmap bitmap;

    boolean cliccato=false;
    ListView container;
    ImageView freccia;

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

        listGroups = (ListView) findViewById(R.id.listGroup);
        listDescription = (ListView) findViewById(R.id.appPermission);

        arrayAdapterGroup = new ArrayAdapter<String>(this, R.layout.app_list_group_detail_item, R.id.groupName);
        arrayAdapterDescription = new PermissionAdapter(this, R.layout.app_list_descrption_detail_item, new ArrayList<Permesso>());

        Intent i = getIntent();
        appName = i.getStringExtra("Nome app");
        bitmap = (Bitmap) this.getIntent().getParcelableExtra("Icona app");
        Drawable icon = new BitmapDrawable(getResources(), bitmap);
        packageName = i.getStringExtra("PACKAGE");
        getActionBar().setLogo(icon);
        getActionBar().setTitle(appName);

        Log.d("DEBUG", "Pacchetto2" + packageName);

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

        PackageManager pm = getPackageManager();
        PackageInfo pi;
        PermissionInfo pgi;
        try {
            pi = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
            String[] perm = pi.requestedPermissions;

            if (perm != null) {
                for (int j = 0; j < perm.length; j++) {
                    try {
                        System.out.println("PROVAAA 555: " + perm[j]);
                        pgi = pm.getPermissionInfo(perm[j], 0);
                        Permesso permesso = new Permesso();
                        permesso.setName(pgi.name);
                        System.out.println("PROVAAA 999: " + permesso.getName());
                        System.out.println("PROVAAA 1010: " + pgi.group);
                        permesso.setDescription(pgi.loadLabel(pm).toString());
                        //System.out.println("PROVAAA 888: " + pgi.group);
                        if (pgi.group != null) {
                            arrayAdapterGroup.add(pgi.group);



                            arrayAdapterDescription.add(permesso);
                        //} else {
                            //arrayAdapterGroup.add(pgi.name);
                        }
                        //arrayAdapterDescription.add(permesso);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                        continue;
                    }
                }
            }
             /*
                    //Log.d("test 11: ", perm[j]);
                    pgi = pm.getPermissionInfo(p1, 0);
                    System.out.println("PROVAAA: " + pgi.group);
                    //Log.d("test 22: ",pgi.loadLabel(pm).toString());
                    //Log.d("test 55: ",pgi.group.toString());
                    //permGroupInfo = pm.getPermissionGroupInfo(pgi.group,0);
                }*/
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        listGroups.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                freccia = (ImageView) view.findViewById(R.id.row);
                container = (ListView) findViewById(R.id.appPermission);
                System.out.println("VAIIIII: "+container.isShown());
                if(!cliccato) {
                    freccia.setImageResource(R.drawable.freccia_up);
                    container.setVisibility(View.VISIBLE);
                    System.out.println("VAIIIII: 2222"+container.isShown());
                    cliccato=true;
                }
                else {
                    freccia.setImageResource(R.drawable.freccia_down);
                    container.setVisibility(View.INVISIBLE);
                    System.out.println("VAIIIII: 3333"+container.isShown());
                    cliccato=false;
                }

            }
        });

        listGroups.setAdapter(arrayAdapterGroup);
        listDescription.setAdapter(arrayAdapterDescription);
    }
}
