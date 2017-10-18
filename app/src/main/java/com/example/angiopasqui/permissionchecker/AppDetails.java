package com.example.angiopasqui.permissionchecker;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


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

    private String path = "/storage/emulated/0/Android/data/com.example.angiopasqui.permissionchecker/files/";                                     //PERCORSO
    private String pathBackup = (this.path + "backup/");                                            // /sdcard/at.plop.PermissionRemover/backup backup del vecchio apk;
    private String pathKey = (this.path + "key/");                                                  // /sdcard/at.plop.PermissionRemover/key dove si trovano i file per la firma dell'apk
    private String pathNew = (this.path + "new/");                                                  // /sdcard/at.plop.PermissionRemover/new CREARE apk modificato;
    private String pathTmp = (this.path + "tmp/");                                                  // /sdcard/at.plop.PermissionRemover/tmp
    private String apkfile;
    private XMLFile xmlfile;
    private boolean update;
    protected Handler _taskHandler1 = new Handler();
    private List<Permesso> permls;
    private String newapkfile;
    private int activityReturn = 0;
    private static final Integer INSTALL = Integer.valueOf(1);
    private static final Integer UNINSTALL = Integer.valueOf(2);
    private SSL ssl;



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

        //PARTE NUOVA
        pm = getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(0);
        for(ApplicationInfo packInfo: apps){
            if((packInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 1) {
                boolean s = new File(packInfo.sourceDir).canRead();
                System.out.println("COSSSSSSS: "+s);
                System.out.println("ABSAAA W W A : "+packInfo.dataDir);
                System.out.println("NOMMMEEEE: "+packInfo.packageName);
                System.out.println("APK DIRECTORYYY "+packInfo.sourceDir);
            }
        }



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

        AppDetails.this.StartUpdateAPK();
    }

    private void StartUpdateAPK() {                                                                 //Avvio UpdateApk con Thread
        this.update = true;
        this._taskHandler1.post(new Runnable() {                                                            //CREAZIONE PROCESSO
            public void run() {
                if (AppDetails.this.update) {
                    AppDetails.this.update = false;
                    AppDetails.this._taskHandler1.post(this);
                    return;
                }
                AppDetails.this.UpdateAPK();                                                  //!!!!!AVVIOOOO!!!!!!!!!!
            }
        });
    }

    private void UpdateAPK() {                                                      //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!AVVIOOOO!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        RWFile rwfile = new RWFile();                                               //CLASSE PER LETTURA,SCRITTURA,GENERAZIONE,COPIA DI UN FILE
        SSL ssl = new SSL(this.pathKey);                                            //Percorso delle chiavi
        System.out.println("VAlllllll: "+ssl.CertAvailable());
        if (ssl.CertAvailable()) {                                                  //Se il certificato è disponibile
            System.out.println("Entriiii??");
            new File(this.pathTmp).mkdirs();
            System.out.println("DIRECTORY Tmp CREATA:  ");
            new Unzip().Unzip("/data/app/ApiDemos/ApiDemos.apk", this.pathTmp, "AndroidManifest.xml").booleanValue();
            System.out.println("TUTTO APPOSTOOOOOOO");

            this.xmlfile = new XMLFile(this.pathTmp + "AndroidManifest.xml");
            System.out.println("XMLLLLL: "+xmlfile.toString());
            this.permls = this.xmlfile.GetPermisionList();                                              //Ritorna la lista dei permessi

            for (int i = 0; i < this.permls.size(); i++) {
                System.out.println("HAI FATTO????: ");
                if (((Permesso) this.permls.get(i)).GetChecked().booleanValue()) {
                    this.xmlfile.RemovePermission(i);                                           //Rimozione del permesso da XMLFILE
                    System.out.println("okkkkk????: ");
                }
            }
            //System.out.println("QUANTOOOOOO: 222 "+this.appName.substring(0,31));
            if (this.appName.length() > 7 ) {
                new File(this.pathBackup).mkdirs();                                                                //CREA LA DIRECTORY DI BACKUP
                System.out.println("CREAZIONEEE: "+this.appName);
                try {
                    rwfile.CopyFile(this.apkfile, this.pathBackup + rwfile.GenFilename(this.apkfile, this.pathBackup, ""));         //Copia del file "apk" originale nella directory del backup
                } catch (Exception e) {
                }
            }
            if (new Unzip().Unzip(this.apkfile, this.pathTmp).booleanValue()) {                             //Se il valore è true
                new File(this.pathTmp + "AndroidManifest.xml").delete();                                    //Cancella la directory temp con il Manifest
                if (this.xmlfile.WriteFile(this.pathTmp + "AndroidManifest.xml").booleanValue()) {          //Se RWFile ha effettuato la scrittura del file
                    new UpdateSHAFiles(this.pathTmp, getPackageInfo().versionName, getString(R.string.app_name)).Update();          //versioName -> il nome della versione del pacchetto; getString() = "Nome App"
                    ssl.SignIt();                                                                   //Chiamata a metodo di SSL.java
                    new File(this.pathNew).mkdirs();                                                //Crea directory /sdcard/at.plop.PermissionRemover/new
                    String newfilename = this.pathNew + rwfile.GenFilename(this.apkfile, this.pathNew, " new");                     //NUOVO NOME DEL FILE
                    new Compress(this.pathTmp, new DirectoryFiles(this.pathTmp).GetOnlyFiles(), newfilename).zip();
                    RemoveTempDir();
                    this.newapkfile = newfilename;
                    String packageName = GetPackageName(this.apkfile);
                    if (packageName.equals("") || ssl.TestCert(getPackageManager(), packageName)) {
                        InstallPackage(this.newapkfile);                                                                //INSTALLAZIONE PACCHETTO
                        finish();                                                                                       //SI CHIUDE L'ACTIVITY
                        return;
                    } else if (true) {
                        Toast.makeText(getApplicationContext(), "This app must be uninstalled before it can be installed.", Toast.LENGTH_LONG).show();
                        UninstallPackage(this.apkfile);                                                                 //DISINSTALLAZIONE PACCHETTO
                        return;
                    } else {
                        InstallPackage(this.newapkfile);
                        finish();                                                                                       //SI CHIUDE L'ACTIVITY
                        return;
                    }
                }
                Toast.makeText(getApplicationContext(), "Error: unable to write file", Toast.LENGTH_LONG).show();
                RemoveTempDir();
                finish();                                                                                               //SI CHIUDE L'ACTIVITY
                return;
            }
            Toast.makeText(getApplicationContext(), "Error: unable to write to /sdcard", Toast.LENGTH_LONG).show();
            finish();                                                                                                   //SI CHIUDE L'ACTIVITY
            return;
        }
        //new DownloadCert(this, this.pathKey, this).show();                                                              //SCARICA CERTIFICATO
        else {
            this.ssl = new SSL(this.pathKey);                                          //mDir: path dove si trova la chiave

            try {
                InputStream inputStream = getAssets().open("testkey.pk8");
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                String text = "";

                File file = new File(getExternalFilesDir("key"), "testkey.pk8");
                file.createNewFile();
                BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));

                while ((line = br.readLine()) != null) {
                    text += line.toString();
                    text += '\n';
                }
                bw.write(text);
                bw.close();
                System.out.println("SADSDD" + text);
            } catch (IOException e) {
                e.printStackTrace();
            }


            File f = new File(getExternalFilesDir("key"), "testkey.x509.pem");

            try {
                f.createNewFile();
                BufferedWriter writer = new BufferedWriter(new FileWriter(f, true));
                writer.write("-----BEGIN CERTIFICATE-----\n" +
                        "MIIEqDCCA5CgAwIBAgIJAJNurL4H8gHfMA0GCSqGSIb3DQEBBQUAMIGUMQswCQYD\n" +
                        "VQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMNTW91bnRhaW4g\n" +
                        "VmlldzEQMA4GA1UEChMHQW5kcm9pZDEQMA4GA1UECxMHQW5kcm9pZDEQMA4GA1UE\n" +
                        "AxMHQW5kcm9pZDEiMCAGCSqGSIb3DQEJARYTYW5kcm9pZEBhbmRyb2lkLmNvbTAe\n" +
                        "Fw0wODAyMjkwMTMzNDZaFw0zNTA3MTcwMTMzNDZaMIGUMQswCQYDVQQGEwJVUzET\n" +
                        "MBEGA1UECBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMNTW91bnRhaW4gVmlldzEQMA4G\n" +
                        "A1UEChMHQW5kcm9pZDEQMA4GA1UECxMHQW5kcm9pZDEQMA4GA1UEAxMHQW5kcm9p\n" +
                        "ZDEiMCAGCSqGSIb3DQEJARYTYW5kcm9pZEBhbmRyb2lkLmNvbTCCASAwDQYJKoZI\n" +
                        "hvcNAQEBBQADggENADCCAQgCggEBANaTGQTexgskse3HYuDZ2CU+Ps1s6x3i/waM\n" +
                        "qOi8qM1r03hupwqnbOYOuw+ZNVn/2T53qUPn6D1LZLjk/qLT5lbx4meoG7+yMLV4\n" +
                        "wgRDvkxyGLhG9SEVhvA4oU6Jwr44f46+z4/Kw9oe4zDJ6pPQp8PcSvNQIg1QCAcy\n" +
                        "4ICXF+5qBTNZ5qaU7Cyz8oSgpGbIepTYOzEJOmc3Li9kEsBubULxWBjf/gOBzAzU\n" +
                        "RNps3cO4JFgZSAGzJWQTT7/emMkod0jb9WdqVA2BVMi7yge54kdVMxHEa5r3b97s\n" +
                        "zI5p58ii0I54JiCUP5lyfTwE/nKZHZnfm644oLIXf6MdW2r+6R8CAQOjgfwwgfkw\n" +
                        "HQYDVR0OBBYEFEhZAFY9JyxGrhGGBaR0GawJyowRMIHJBgNVHSMEgcEwgb6AFEhZ\n" +
                        "AFY9JyxGrhGGBaR0GawJyowRoYGapIGXMIGUMQswCQYDVQQGEwJVUzETMBEGA1UE\n" +
                        "CBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMNTW91bnRhaW4gVmlldzEQMA4GA1UEChMH\n" +
                        "QW5kcm9pZDEQMA4GA1UECxMHQW5kcm9pZDEQMA4GA1UEAxMHQW5kcm9pZDEiMCAG\n" +
                        "CSqGSIb3DQEJARYTYW5kcm9pZEBhbmRyb2lkLmNvbYIJAJNurL4H8gHfMAwGA1Ud\n" +
                        "EwQFMAMBAf8wDQYJKoZIhvcNAQEFBQADggEBAHqvlozrUMRBBVEY0NqrrwFbinZa\n" +
                        "J6cVosK0TyIUFf/azgMJWr+kLfcHCHJsIGnlw27drgQAvilFLAhLwn62oX6snb4Y\n" +
                        "LCBOsVMR9FXYJLZW2+TcIkCRLXWG/oiVHQGo/rWuWkJgU134NDEFJCJGjDbiLCpe\n" +
                        "+ZTWHdcwauTJ9pUbo8EvHRkU3cYfGmLaLfgn9gP+pWA7LFQNvXwBnDa6sppCccEX\n" +
                        "31I828XzgXpJ4O+mDL1/dBd+ek8ZPUP0IgdyZm5MTYPhvVqGCHzzTy3sIeJFymwr\n" +
                        "sBbmg2OAUNLEMO6nwmocSdN2ClirfxqCzJOLSDE4QyS9BAH6EhY6UFcOaE0=\n" +
                        "-----END CERTIFICATE-----");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("COSSAAAAAAA: " + getExternalFilesDir(null));
            RestartUpdateAPK();
        }
    }

    private PackageInfo getPackageInfo() {
        PackageInfo pi = null;
        try {
            pi = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return pi;
    }

    private void RemoveTempDir() {                      //Rimuovi la directory temp
        new DirectoryRemove("").RemoveDir(this.pathTmp);
    }

    private String GetPackageName(String file) {                        //RITORNA IL NOME DEL PACCHETTO (com.""."")
        PackageManager pm = getPackageManager();
        String packageName = pm.getPackageArchiveInfo(file, 0).packageName;
        List<ApplicationInfo> appinfos = pm.getInstalledApplications(0);
        for (int i = 0; i < appinfos.size(); i++) {
            if (((ApplicationInfo) appinfos.get(i)).packageName.equals(packageName)) {
                return packageName;
            }
        }
        return "";
    }

    private void InstallPackage(String packagename) {
        setResult(2);
        this.activityReturn = INSTALL.intValue();
        Intent intent = new Intent("android.intent.action.VIEW");                                   //LANCIA VIEW PER INSTALLARE IL PACCHETTO
        intent.setDataAndType(Uri.fromFile(new File(packagename)), "application/vnd.android.package-archive");
        startActivityForResult(intent, 0);
    }

    private void UninstallPackage(String packagename) {
        setResult(2);
        this.activityReturn = UNINSTALL.intValue();
        startActivityForResult(new Intent("android.intent.action.DELETE", Uri.fromParts("package", getPackageManager().getPackageArchiveInfo(packagename, 0).packageName, null)), 0);
    }       //LANCIA DELETE PER DISINSTALLARE IL PACCHETTO

    public void NoDownload() {
        Toast.makeText(getApplicationContext(), "Unable to create and sign the new APK file without the key and cert file.", Toast.LENGTH_LONG).show();
        RemoveTempDir();
        finish();
    }

    public void RestartUpdateAPK() {
        StartUpdateAPK();
    }
}
