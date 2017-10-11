package com.example.angiopasqui.permissionchecker;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;


/**
 * Created by Angelo on 19/09/2017.
 */

public class AppDetails extends Activity {
    PackageManager pm;
    PermissionAdapter arrayAdapterDescription;
    ListView listPermission;
    private String appName;
    String packageName;
    private Bitmap bitmap;
    private Drawable iconPermission;
    static int permissionControl;
    static int permControl;

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

        Log.d("DEBUG", "Pacchetto 2" + packageName);

        //GET PERMISSIONS
        pm = getPackageManager();
        PackageInfo pi;
        PermissionInfo pemInfo = null;

        Permesso permesso = new Permesso();

        boolean check = false;

        PermissionGroupInfo groupInfo;
        Drawable denied = getDrawable(R.drawable.denied);
        Drawable grant = getDrawable(R.drawable.check_granted);


        try {
            pi = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
            String[] requestedPermissions = pi.requestedPermissions;

            if (requestedPermissions != null) {
                for (int j = 0; j < requestedPermissions.length; j++) {
                    Log.d("test 888", requestedPermissions[j]);
                    try {
                        if (requestedPermissions[j].contains(("android.permission")) || requestedPermissions[j].contains("com.google") || requestedPermissions[j].contains("com.android.launcher")) {
                            permesso = new Permesso();

                            Log.d("test", requestedPermissions[j]);

                            pemInfo = pm.getPermissionInfo(requestedPermissions[j], 0);

                            check = true;

                            if (pemInfo.loadDescription(pm) == null) {
                                //Do nothing
                                System.out.println("CI ARRIVIII!!!");
                            } else {
                                permesso.setName(pemInfo.name);
                                permesso.setDescription((String) pemInfo.loadDescription(pm));
                                System.out.println("NOME PERMESSOOOOOOOO " + pemInfo.name);

                                System.out.println("NOME GRUPPOOO " + pemInfo.group);

                                groupInfo = pm.getPermissionGroupInfo(pemInfo.group, 0);
                                Drawable icona = pm.getResourcesForApplication("android").getDrawable(groupInfo.icon);
                                permesso.setIcon(icona);

                                arrayAdapterDescription.add(permesso);
                                check = false;
                                System.out.println("CONTINUA DOPO ECCEZIONE");
                            }
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                        System.out.println("ECCEZIONEEEEE");
                        if (check) {
                            switch (pemInfo.name) {
                                case "android.permission.INTERNET":
                                    iconPermission = getResources().getDrawable(R.drawable.internet_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.INTERNET",packageName);
                                    System.out.println("VALORE GGGGG: "+permControl);
                                    switch (permControl){
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            break;
                                    }
                                    break;
                                case "android.permission.BLUETOOTH":
                                    iconPermission = getResources().getDrawable(R.drawable.bluetooth_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.BLUETOOTH",packageName);
                                    System.out.println("VALORE GGGGG: "+permControl);
                                    switch (permControl){
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            break;
                                    }
                                    break;
                                case "com.google.android.c2dm.permission.RECEIVE":
                                    iconPermission = getResources().getDrawable(R.drawable.receive_wap_push_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("com.google.android.c2dm.permission.RECEIVE",packageName);
                                    System.out.println("VALORE GGGGG: "+permControl);
                                    switch (permControl){
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            break;
                                    }
                                    break;
                                case "com.google.android.providers.gsf.permission.READ_GSERVICES":
                                    iconPermission = getResources().getDrawable(R.drawable.gs_service);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("com.google.android.providers.gsf.permission.READ_GSERVICES",packageName);
                                    System.out.println("VALORE GGGGG: "+permControl);
                                    switch (permControl){
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            break;
                                    }
                                    break;
                                case "android.permission.MODIFY_AUDIO_SETTINGS":
                                    iconPermission = getResources().getDrawable(R.drawable.modify_audio_settings_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.MODIFY_AUDIO_SETTINGS",packageName);
                                    System.out.println("VALORE GGGGG: "+permControl);
                                    switch (permControl){
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            break;
                                    }
                                    break;
                                case "android.permission.ACCESS_NETWORK_STATE":
                                    iconPermission = getResources().getDrawable(R.drawable.access_network_state_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.ACCESS_NETWORK_STATE",packageName);
                                    System.out.println("VALORE GGGGG: "+permControl);
                                    switch (permControl){
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            break;
                                    }
                                    break;
                                case "android.permission.ACCESS_WIFI_STATE":
                                    iconPermission = getResources().getDrawable(R.drawable.access_wifi_state_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.ACCESS_WIFI_STATE",packageName);
                                    System.out.println("VALORE GGGGG: "+permControl);
                                    switch (permControl){
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            break;
                                    }
                                    break;
                                case "android.permission.WAKE_LOCK":
                                    iconPermission = getResources().getDrawable(R.drawable.wake_lock);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.WAKE_LOCK",packageName);
                                    System.out.println("VALORE GGGGG: "+permControl);
                                    switch (permControl){
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            break;
                                    }
                                    break;
                                case "android.permission.MANAGE_ACCOUNTS":
                                    iconPermission = getResources().getDrawable(R.drawable.manage_account);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.MANAGE_ACCOUNTS",packageName);
                                    System.out.println("VALORE GGGGG: "+permControl);
                                    switch (permControl){
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            break;
                                    }
                                    break;
                                case "android.permission.READ_PROFILE":
                                    iconPermission = getResources().getDrawable(R.drawable.read_profile_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.READ_PROFILE",packageName);
                                    System.out.println("VALORE GGGGG: "+permControl);
                                    switch (permControl){
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            break;
                                    }
                                    break;
                                case "android.permission.WRITE_SYNC_SETTINGS":
                                    iconPermission = getResources().getDrawable(R.drawable.sync);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.WRITE_SYNC_SETTINGS",packageName);
                                    System.out.println("VALORE GGGGG: "+permControl);
                                    switch (permControl){
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            break;
                                    }
                                    break;
                                case "android.permission.READ_SYNC_SETTINGS":
                                    iconPermission = getResources().getDrawable(R.drawable.read_sync_settings_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.READ_SYNC_SETTINGS",packageName);
                                    System.out.println("VALORE GGGGG: "+permControl);
                                    switch (permControl){
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            break;
                                    }
                                    break;
                                case "android.permission.AUTHENTICATE_ACCOUNTS":
                                    iconPermission = getResources().getDrawable(R.drawable.authenticate_accounts_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.AUTHENTICATE_ACCOUNTS",packageName);
                                    System.out.println("VALORE GGGGG: "+permControl);
                                    switch (permControl){
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            break;
                                    }
                                    break;
                                case "android.permission.VIBRATE":
                                    iconPermission = getResources().getDrawable(R.drawable.vibrate_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.VIBRATE",packageName);
                                    System.out.println("VALORE GGGGG: "+permControl);
                                    switch (permControl){
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            break;
                                    }
                                    break;
                                case "android.permission.SYSTEM_ALERT_WINDOW":
                                    iconPermission = getResources().getDrawable(R.drawable.alert);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.SYSTEM_ALERT_WINDOW",packageName);
                                    System.out.println("VALORE GGGGG: "+permControl);
                                    switch (permControl){
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            break;
                                    }
                                    break;
                                case "android.permission.RECEIVE_BOOT_COMPLETED":
                                    iconPermission = getResources().getDrawable(R.drawable.receive_boot_completed_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.RECEIVE_BOOT_COMPLETED",packageName);
                                    System.out.println("VALORE GGGGG: "+permControl);
                                    switch (permControl){
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            break;
                                    }
                                    break;
                                case "com.android.launcher.permission.INSTALL_SHORTCUT":
                                    iconPermission = getResources().getDrawable(R.drawable.install_shortcut_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("com.android.launcher.permission.INSTALL_SHORTCUT",packageName);
                                    System.out.println("VALORE GGGGG: "+permControl);
                                    switch (permControl){
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            break;
                                    }
                                    break;
                                case "com.android.launcher.permission.UNINSTALL_SHORTCUT":
                                    iconPermission = getResources().getDrawable(R.drawable.uninstall_shortcut_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("com.android.launcher.permission.UNINSTALL_SHORTCUT",packageName);
                                    System.out.println("VALORE GGGGG: "+permControl);
                                    switch (permControl){
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            break;
                                    }
                                    break;
                                case "android.permission.CHANGE_WIFI_STATE":
                                    iconPermission = getResources().getDrawable(R.drawable.access_wifi_state_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.CHANGE_WIFI_STATE",packageName);
                                    System.out.println("VALORE GGGGG: "+permControl);
                                    switch (permControl){
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            break;
                                    }
                                    break;
                                case "android.permission.NFC":
                                    iconPermission = getResources().getDrawable(R.drawable.nfc_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.NFC",packageName);
                                    System.out.println("VALORE GGGGG: "+permControl);
                                    switch (permControl){
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            break;
                                    }
                                    break;
                                case "android.permission.READ_SYNC_STATS":
                                    iconPermission = getResources().getDrawable(R.drawable.stats);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.READ_SYNC_STATS",packageName);
                                    System.out.println("VALORE GGGGG: "+permControl);
                                    switch (permControl){
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            break;
                                    }
                                    break;
                                case "android.permission.WRITE_SETTINGS":
                                    iconPermission = getResources().getDrawable(R.drawable.write_sett);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.WRITE_SETTINGS",packageName);
                                    System.out.println("VALORE GGGGG: "+permControl);
                                    switch (permControl){
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            break;
                                    }
                                    break;
                                case "android.permission.GET_TASKS":
                                    iconPermission = getResources().getDrawable(R.drawable.task);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.GET_TASKS",packageName);
                                    System.out.println("VALORE GGGGG: "+permControl);
                                    switch (permControl){
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            break;
                                    }
                                    break;
                                case "android.permission.GET_PACKAGE_SIZE":
                                    iconPermission = getResources().getDrawable(R.drawable.get_package_size_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.GET_PACKAGE_SIZE",packageName);
                                    System.out.println("VALORE GGGGG: "+permControl);
                                    switch (permControl){
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            break;
                                    }
                                    break;
                                case "android.permission.BROADCAST_STICKY":
                                    iconPermission = getResources().getDrawable(R.drawable.broadcast_sticky_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.BROADCAST_STICKY",packageName);
                                    System.out.println("VALORE GGGGG: "+permControl);
                                    switch (permControl){
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            break;
                                    }
                                    break;
                                case "android.permission.CHANGE_NETWORK_STATE":
                                    iconPermission = getResources().getDrawable(R.drawable.change_network_state_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.CHANGE_NETWORK_STATE",packageName);
                                    System.out.println("VALORE GGGGG: "+permControl);
                                    switch (permControl){
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            break;
                                    }
                                    break;
                                case "android.permission.EXPAND_STATUS_BAR":
                                    iconPermission = getResources().getDrawable(R.drawable.expand_status_bar_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.EXPAND_STATUS_BAR",packageName);
                                    System.out.println("VALORE GGGGG: "+permControl);
                                    switch (permControl){
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            break;
                                    }
                                    break;
                                case "android.permission.BLUETOOTH_ADMIN":
                                    iconPermission = getResources().getDrawable(R.drawable.bluetooth_admin_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.BLUETOOTH_ADMIN",packageName);
                                    System.out.println("VALORE GGGGG: "+permControl);
                                    switch (permControl){
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            break;
                                    }
                                    break;
                                case "android.permission.USE_CREDENTIALS":
                                    iconPermission = getResources().getDrawable(R.drawable.use_credentials_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.USE_CREDENTIALS",packageName);
                                    System.out.println("VALORE GGGGG: "+permControl);
                                    switch (permControl){
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            break;
                                    }
                                    break;
                                case "android.permission.REQUEST_INSTALL_PACKAGES":
                                    iconPermission = getResources().getDrawable(R.drawable.request_install_packages_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.REQUEST_INSTALL_PACKAGES",packageName);
                                    System.out.println("VALORE GGGGG: "+permControl);
                                    switch (permControl){
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            break;
                                    }
                                    break;
                                case "android.permission.WRITE_SMS":
                                    iconPermission = getResources().getDrawable(R.drawable.write_sms_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.WRITE_SMS",packageName);
                                    System.out.println("VALORE GGGGG: "+permControl);
                                    switch (permControl){
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            break;
                                    }
                                    break;
                                case "android.permission.CHANGE_WIFI_MULTICAST_STATE":
                                    iconPermission = getResources().getDrawable(R.drawable.change_wifi_multicast_state_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.CHANGE_WIFI_MULTICAST_STATE",packageName);
                                    System.out.println("VALORE GGGGG: "+permControl);
                                    switch (permControl){
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            break;
                                    }
                                    break;
                                case "android.permission.KILL_BACKGROUND_PROCESSES":
                                    iconPermission = getResources().getDrawable(R.drawable.kill_background_processes_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.KILL_BACKGROUND_PROCESSES",packageName);
                                    System.out.println("VALORE GGGGG: "+permControl);
                                    switch (permControl){
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            break;
                                    }
                                    break;
                                case "com.google.android.gm.permission.AUTO_SEND":
                                    iconPermission = getResources().getDrawable(R.drawable.send_sms_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("com.google.android.gm.permission.AUTO_SEND",packageName);
                                    System.out.println("VALORE GGGGG: "+permControl);
                                    switch (permControl){
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            break;
                                    }
                                    break;
                                case "com.google.android.gms.permission.ACTIVITY_RECOGNITION":
                                    iconPermission = getResources().getDrawable(R.drawable.fitness);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("com.google.android.gms.permission.ACTIVITY_RECOGNITION",packageName);
                                    System.out.println("VALORE GGGGG: "+permControl);
                                    switch (permControl){
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            break;
                                    }
                                    break;
                                case "com.android.launcher.permission.READ_SETTINGS":
                                    iconPermission = getResources().getDrawable(R.drawable.read_settings);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("com.android.launcher.permission.READ_SETTINGS",packageName);
                                    System.out.println("VALORE GGGGG: "+permControl);
                                    switch (permControl){
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            break;
                                    }
                                    break;
                                case "com.android.launcher.permission.WRITE_SETTINGS":
                                    iconPermission = getResources().getDrawable(R.drawable.write_settings);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("com.android.launcher.permission.WRITE_SETTINGS",packageName);
                                    System.out.println("VALORE GGGGG: "+permControl);
                                    switch (permControl){
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            break;
                                    }
                                    break;
                                case "android.permission.RESTART_PACKAGES":
                                    iconPermission = getResources().getDrawable(R.drawable.application_close);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.RESTART_PACKAGES",packageName);
                                    System.out.println("VALORE GGGGG: "+permControl);
                                    switch (permControl){
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            break;
                                    }
                                    break;
                                case "android.permission.DOWNLOAD_WITHOUT_NOTIFICATION":
                                    iconPermission = getResources().getDrawable(R.drawable.download_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.DOWNLOAD_WITHOUT_NOTIFICATION",packageName);
                                    System.out.println("VALORE GGGGG: "+permControl);
                                    switch (permControl){
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            break;
                                    }
                                    break;
                                case "android.permission.DISABLE_KEYGUARD":
                                    iconPermission = getResources().getDrawable(R.drawable.disable_keyguard_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.DISABLE_KEYGUARD",packageName);
                                    System.out.println("VALORE GGGGG: "+permControl);
                                    switch (permControl){
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            break;
                                    }
                                    break;
                                case "android.permission.REORDER_TASKS":
                                    iconPermission = getResources().getDrawable(R.drawable.reorder_tasks_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.REORDER_TASKS",packageName);
                                    System.out.println("VALORE GGGGG: "+permControl);
                                    switch (permControl){
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            break;
                                    }
                                    break;
                                case "android.permission.SET_WALLPAPER":
                                    iconPermission = getResources().getDrawable(R.drawable.set_wallpaper_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.SET_WALLPAPER",packageName);
                                    System.out.println("VALORE GGGGG: "+permControl);
                                    switch (permControl){
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            break;
                                    }
                                    break;
                            }
                            arrayAdapterDescription.add(permesso);
                        }

                        check = false;

                        continue;
                    }
                    permissionControl = pm.checkPermission(requestedPermissions[j],packageName);
                    System.out.println("VALORE GGGGG: "+permissionControl);
                    switch (permissionControl){
                        case PackageManager.PERMISSION_GRANTED:
                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                            permesso.setCheckPermission(grant);
                            break;
                        case PackageManager.PERMISSION_DENIED:
                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                            permesso.setCheckPermission(denied);
                            break;
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        listPermission.setAdapter(arrayAdapterDescription);

    }

    public void allowPermission(View v) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Toast.makeText(AppDetails.this, "CLICCATO!",
                Toast.LENGTH_LONG).show();
        Class<?> myClass = Class.forName(packageName);
        System.out.println("ASDDDDA : "+myClass.getName());
        Activity act = (Activity) myClass.newInstance();
        ArrayList<String> arrPerm = new ArrayList<>();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            arrPerm.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            arrPerm.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(!arrPerm.isEmpty()) {
            String[] permissions = new String[arrPerm.size()];
            permissions = arrPerm.toArray(permissions);
            ActivityCompat.requestPermissions(act, permissions, 1);
        }
    }
}
