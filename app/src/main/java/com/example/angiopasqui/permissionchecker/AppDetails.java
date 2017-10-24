package com.example.angiopasqui.permissionchecker;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.os.StrictMode;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import kellinwood.security.zipsigner.ZipSigner;


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
    private String pathNew = (this.path + "new/");                                                  // /sdcard/at.plop.PermissionRemover/new CREARE apk modificato;
    private String pathTmp = "tmp/";                                                  // /sdcard/at.plop.PermissionRemover/tmp
    private String apkfile;
    private XMLFile xmlfile;
    private boolean update;
    protected Handler _taskHandler1 = new Handler();
    private List<Permesso> permls;
    private String newapkfile;
    private int activityReturn = 0;
    private static final Integer INSTALL = Integer.valueOf(1);
    private static final Integer UNINSTALL = Integer.valueOf(2);
    private ProgressDialog dialog;
    private String apkBackup;


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


        /**
         * @source https://stackoverflow.com/questions/38200282/android-os-fileuriexposedexception-file-storage-emulated-0-test-txt-exposed
         */
        //CONSENTE L'INSTALLAZIONE DI UN PACCHETTO DOPO LA RIMOZIONE DEL APK PRECENDENTE
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();                    //LA VM IGNORA L'Uri del file di installazione .apk
        StrictMode.setVmPolicy(builder.build());

        builder.detectFileUriExposure();                                                            //RILEVA QUANDO L'APPLICAZIONE CHIAMANTE 'espone', "file://uri" ad un altra app

        //CREAZIONE CARTELLA TMP
        new File(getExternalFilesDir(this.pathTmp), "");
        System.out.println("DIRECTORY Tmp CREATA:  ");


        //ESTRAZIONE DEL FILE AndroidManifest.xml DAL FILE APK
        pm = getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(0);
        for (ApplicationInfo packInfo : apps) {
            if ((packInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 1) {
                if (packInfo.packageName.equals(packageName)) {
                    System.out.println("NOMMMEEEE: " + packInfo.packageName);
                    apkfile = packInfo.sourceDir;
                    System.out.println("APK DIRECTORYYY " + packInfo.sourceDir);
                    boolean b = new Unzip().Unzip(packInfo.sourceDir, this.path + this.pathTmp, "AndroidManifest.xml").booleanValue();
                    System.out.println("TUTTO APPOSTOOOOOOO" + b);
                    this.xmlfile = new XMLFile(this.path + this.pathTmp + "AndroidManifest.xml");
                    this.permls = this.xmlfile.GetPermisionList();                                      //LISTA PERMESSI PRESI DAL FILE MANIFEST DELL'apk
                    System.out.println("LISTTAAAAA PERMMIEE:: " + permls);
                    break;
                }
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
                                    permControl = pm.checkPermission("android.permission.INTERNET", packageName);
                                    System.out.println("VALORE GGGGG: " + permControl);
                                    switch (permControl) {
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            permesso.setContainerVisible(View.VISIBLE);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            permesso.setContainerVisible(View.INVISIBLE);
                                            break;
                                    }
                                    break;
                                case "android.permission.BLUETOOTH":
                                    iconPermission = getResources().getDrawable(R.drawable.bluetooth_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.BLUETOOTH", packageName);
                                    System.out.println("VALORE GGGGG: " + permControl);
                                    switch (permControl) {
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            permesso.setContainerVisible(View.VISIBLE);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            permesso.setContainerVisible(View.INVISIBLE);
                                            break;
                                    }
                                    break;
                                case "com.google.android.c2dm.permission.RECEIVE":
                                    iconPermission = getResources().getDrawable(R.drawable.receive_wap_push_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("com.google.android.c2dm.permission.RECEIVE", packageName);
                                    System.out.println("VALORE GGGGG: " + permControl);
                                    switch (permControl) {
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            permesso.setContainerVisible(View.VISIBLE);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            permesso.setContainerVisible(View.INVISIBLE);
                                            break;
                                    }
                                    break;
                                case "com.google.android.providers.gsf.permission.READ_GSERVICES":
                                    iconPermission = getResources().getDrawable(R.drawable.gs_service);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("com.google.android.providers.gsf.permission.READ_GSERVICES", packageName);
                                    System.out.println("VALORE GGGGG: " + permControl);
                                    switch (permControl) {
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            permesso.setContainerVisible(View.VISIBLE);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            permesso.setContainerVisible(View.INVISIBLE);
                                            break;
                                    }
                                    break;
                                case "android.permission.MODIFY_AUDIO_SETTINGS":
                                    iconPermission = getResources().getDrawable(R.drawable.modify_audio_settings_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.MODIFY_AUDIO_SETTINGS", packageName);
                                    System.out.println("VALORE GGGGG: " + permControl);
                                    switch (permControl) {
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            permesso.setContainerVisible(View.VISIBLE);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            permesso.setContainerVisible(View.INVISIBLE);
                                            break;
                                    }
                                    break;
                                case "android.permission.ACCESS_NETWORK_STATE":
                                    iconPermission = getResources().getDrawable(R.drawable.access_network_state_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.ACCESS_NETWORK_STATE", packageName);
                                    System.out.println("VALORE GGGGG: " + permControl);
                                    switch (permControl) {
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            permesso.setContainerVisible(View.VISIBLE);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            permesso.setContainerVisible(View.INVISIBLE);
                                            break;
                                    }
                                    break;
                                case "android.permission.ACCESS_WIFI_STATE":
                                    iconPermission = getResources().getDrawable(R.drawable.access_wifi_state_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.ACCESS_WIFI_STATE", packageName);
                                    System.out.println("VALORE GGGGG: " + permControl);
                                    switch (permControl) {
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            permesso.setContainerVisible(View.VISIBLE);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            permesso.setContainerVisible(View.INVISIBLE);
                                            break;
                                    }
                                    break;
                                case "android.permission.WAKE_LOCK":
                                    iconPermission = getResources().getDrawable(R.drawable.wake_lock);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.WAKE_LOCK", packageName);
                                    System.out.println("VALORE GGGGG: " + permControl);
                                    switch (permControl) {
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            permesso.setContainerVisible(View.VISIBLE);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            permesso.setContainerVisible(View.INVISIBLE);
                                            break;
                                    }
                                    break;
                                case "android.permission.MANAGE_ACCOUNTS":
                                    iconPermission = getResources().getDrawable(R.drawable.manage_account);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.MANAGE_ACCOUNTS", packageName);
                                    System.out.println("VALORE GGGGG: " + permControl);
                                    switch (permControl) {
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            permesso.setContainerVisible(View.VISIBLE);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            permesso.setContainerVisible(View.INVISIBLE);
                                            break;
                                    }
                                    break;
                                case "android.permission.READ_PROFILE":
                                    iconPermission = getResources().getDrawable(R.drawable.read_profile_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.READ_PROFILE", packageName);
                                    System.out.println("VALORE GGGGG: " + permControl);
                                    switch (permControl) {
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            permesso.setContainerVisible(View.VISIBLE);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            permesso.setContainerVisible(View.INVISIBLE);
                                            break;
                                    }
                                    break;
                                case "android.permission.WRITE_SYNC_SETTINGS":
                                    iconPermission = getResources().getDrawable(R.drawable.sync);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.WRITE_SYNC_SETTINGS", packageName);
                                    System.out.println("VALORE GGGGG: " + permControl);
                                    switch (permControl) {
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            permesso.setContainerVisible(View.VISIBLE);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            permesso.setContainerVisible(View.INVISIBLE);
                                            break;
                                    }
                                    break;
                                case "android.permission.READ_SYNC_SETTINGS":
                                    iconPermission = getResources().getDrawable(R.drawable.read_sync_settings_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.READ_SYNC_SETTINGS", packageName);
                                    System.out.println("VALORE GGGGG: " + permControl);
                                    switch (permControl) {
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            permesso.setContainerVisible(View.VISIBLE);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            permesso.setContainerVisible(View.INVISIBLE);
                                            break;
                                    }
                                    break;
                                case "android.permission.AUTHENTICATE_ACCOUNTS":
                                    iconPermission = getResources().getDrawable(R.drawable.authenticate_accounts_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.AUTHENTICATE_ACCOUNTS", packageName);
                                    System.out.println("VALORE GGGGG: " + permControl);
                                    switch (permControl) {
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            permesso.setContainerVisible(View.VISIBLE);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            permesso.setContainerVisible(View.INVISIBLE);
                                            break;
                                    }
                                    break;
                                case "android.permission.VIBRATE":
                                    iconPermission = getResources().getDrawable(R.drawable.vibrate_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.VIBRATE", packageName);
                                    System.out.println("VALORE GGGGG: " + permControl);
                                    switch (permControl) {
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            permesso.setContainerVisible(View.VISIBLE);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            permesso.setContainerVisible(View.INVISIBLE);
                                            break;
                                    }
                                    break;
                                case "android.permission.SYSTEM_ALERT_WINDOW":
                                    iconPermission = getResources().getDrawable(R.drawable.alert);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.SYSTEM_ALERT_WINDOW", packageName);
                                    System.out.println("VALORE GGGGG: " + permControl);
                                    switch (permControl) {
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            permesso.setContainerVisible(View.VISIBLE);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            permesso.setContainerVisible(View.INVISIBLE);
                                            break;
                                    }
                                    break;
                                case "android.permission.RECEIVE_BOOT_COMPLETED":
                                    iconPermission = getResources().getDrawable(R.drawable.receive_boot_completed_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.RECEIVE_BOOT_COMPLETED", packageName);
                                    System.out.println("VALORE GGGGG: " + permControl);
                                    switch (permControl) {
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            permesso.setContainerVisible(View.VISIBLE);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            permesso.setContainerVisible(View.INVISIBLE);
                                            break;
                                    }
                                    break;
                                case "com.android.launcher.permission.INSTALL_SHORTCUT":
                                    iconPermission = getResources().getDrawable(R.drawable.install_shortcut_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("com.android.launcher.permission.INSTALL_SHORTCUT", packageName);
                                    System.out.println("VALORE GGGGG: " + permControl);
                                    switch (permControl) {
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            permesso.setContainerVisible(View.VISIBLE);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            permesso.setContainerVisible(View.INVISIBLE);
                                            break;
                                    }
                                    break;
                                case "com.android.launcher.permission.UNINSTALL_SHORTCUT":
                                    iconPermission = getResources().getDrawable(R.drawable.uninstall_shortcut_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("com.android.launcher.permission.UNINSTALL_SHORTCUT", packageName);
                                    System.out.println("VALORE GGGGG: " + permControl);
                                    switch (permControl) {
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            permesso.setContainerVisible(View.VISIBLE);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            permesso.setContainerVisible(View.INVISIBLE);
                                            break;
                                    }
                                    break;
                                case "android.permission.CHANGE_WIFI_STATE":
                                    iconPermission = getResources().getDrawable(R.drawable.access_wifi_state_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.CHANGE_WIFI_STATE", packageName);
                                    System.out.println("VALORE GGGGG: " + permControl);
                                    switch (permControl) {
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            permesso.setContainerVisible(View.VISIBLE);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            permesso.setContainerVisible(View.INVISIBLE);
                                            break;
                                    }
                                    break;
                                case "android.permission.NFC":
                                    iconPermission = getResources().getDrawable(R.drawable.nfc_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.NFC", packageName);
                                    System.out.println("VALORE GGGGG: " + permControl);
                                    switch (permControl) {
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            permesso.setContainerVisible(View.VISIBLE);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            permesso.setContainerVisible(View.INVISIBLE);
                                            break;
                                    }
                                    break;
                                case "android.permission.READ_SYNC_STATS":
                                    iconPermission = getResources().getDrawable(R.drawable.stats);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.READ_SYNC_STATS", packageName);
                                    System.out.println("VALORE GGGGG: " + permControl);
                                    switch (permControl) {
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            permesso.setContainerVisible(View.VISIBLE);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            permesso.setContainerVisible(View.INVISIBLE);
                                            break;
                                    }
                                    break;
                                case "android.permission.WRITE_SETTINGS":
                                    iconPermission = getResources().getDrawable(R.drawable.write_sett);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.WRITE_SETTINGS", packageName);
                                    System.out.println("VALORE GGGGG: " + permControl);
                                    switch (permControl) {
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            permesso.setContainerVisible(View.VISIBLE);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            permesso.setContainerVisible(View.INVISIBLE);
                                            break;
                                    }
                                    break;
                                case "android.permission.GET_TASKS":
                                    iconPermission = getResources().getDrawable(R.drawable.task);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.GET_TASKS", packageName);
                                    System.out.println("VALORE GGGGG: " + permControl);
                                    switch (permControl) {
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            permesso.setContainerVisible(View.VISIBLE);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            permesso.setContainerVisible(View.INVISIBLE);
                                            break;
                                    }
                                    break;
                                case "android.permission.GET_PACKAGE_SIZE":
                                    iconPermission = getResources().getDrawable(R.drawable.get_package_size_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.GET_PACKAGE_SIZE", packageName);
                                    System.out.println("VALORE GGGGG: " + permControl);
                                    switch (permControl) {
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            permesso.setContainerVisible(View.VISIBLE);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            permesso.setContainerVisible(View.INVISIBLE);
                                            break;
                                    }
                                    break;
                                case "android.permission.BROADCAST_STICKY":
                                    iconPermission = getResources().getDrawable(R.drawable.broadcast_sticky_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.BROADCAST_STICKY", packageName);
                                    System.out.println("VALORE GGGGG: " + permControl);
                                    switch (permControl) {
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            permesso.setContainerVisible(View.VISIBLE);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            permesso.setContainerVisible(View.INVISIBLE);
                                            break;
                                    }
                                    break;
                                case "android.permission.CHANGE_NETWORK_STATE":
                                    iconPermission = getResources().getDrawable(R.drawable.change_network_state_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.CHANGE_NETWORK_STATE", packageName);
                                    System.out.println("VALORE GGGGG: " + permControl);
                                    switch (permControl) {
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            permesso.setContainerVisible(View.VISIBLE);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            permesso.setContainerVisible(View.INVISIBLE);
                                            break;
                                    }
                                    break;
                                case "android.permission.EXPAND_STATUS_BAR":
                                    iconPermission = getResources().getDrawable(R.drawable.expand_status_bar_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.EXPAND_STATUS_BAR", packageName);
                                    System.out.println("VALORE GGGGG: " + permControl);
                                    switch (permControl) {
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            permesso.setContainerVisible(View.VISIBLE);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            permesso.setContainerVisible(View.INVISIBLE);
                                            break;
                                    }
                                    break;
                                case "android.permission.BLUETOOTH_ADMIN":
                                    iconPermission = getResources().getDrawable(R.drawable.bluetooth_admin_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.BLUETOOTH_ADMIN", packageName);
                                    System.out.println("VALORE GGGGG: " + permControl);
                                    switch (permControl) {
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            permesso.setContainerVisible(View.VISIBLE);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            permesso.setContainerVisible(View.INVISIBLE);
                                            break;
                                    }
                                    break;
                                case "android.permission.USE_CREDENTIALS":
                                    iconPermission = getResources().getDrawable(R.drawable.use_credentials_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.USE_CREDENTIALS", packageName);
                                    System.out.println("VALORE GGGGG: " + permControl);
                                    switch (permControl) {
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            permesso.setContainerVisible(View.VISIBLE);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            permesso.setContainerVisible(View.INVISIBLE);
                                            break;
                                    }
                                    break;
                                case "android.permission.REQUEST_INSTALL_PACKAGES":
                                    iconPermission = getResources().getDrawable(R.drawable.request_install_packages_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.REQUEST_INSTALL_PACKAGES", packageName);
                                    System.out.println("VALORE GGGGG: " + permControl);
                                    switch (permControl) {
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            permesso.setContainerVisible(View.VISIBLE);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            permesso.setContainerVisible(View.INVISIBLE);
                                            break;
                                    }
                                    break;
                                case "android.permission.WRITE_SMS":
                                    iconPermission = getResources().getDrawable(R.drawable.write_sms_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.WRITE_SMS", packageName);
                                    System.out.println("VALORE GGGGG: " + permControl);
                                    switch (permControl) {
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            permesso.setContainerVisible(View.VISIBLE);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            permesso.setContainerVisible(View.INVISIBLE);
                                            break;
                                    }
                                    break;
                                case "android.permission.CHANGE_WIFI_MULTICAST_STATE":
                                    iconPermission = getResources().getDrawable(R.drawable.change_wifi_multicast_state_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.CHANGE_WIFI_MULTICAST_STATE", packageName);
                                    System.out.println("VALORE GGGGG: " + permControl);
                                    switch (permControl) {
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            permesso.setContainerVisible(View.VISIBLE);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            permesso.setContainerVisible(View.INVISIBLE);
                                            break;
                                    }
                                    break;
                                case "android.permission.KILL_BACKGROUND_PROCESSES":
                                    iconPermission = getResources().getDrawable(R.drawable.kill_background_processes_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.KILL_BACKGROUND_PROCESSES", packageName);
                                    System.out.println("VALORE GGGGG: " + permControl);
                                    switch (permControl) {
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            permesso.setContainerVisible(View.VISIBLE);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            permesso.setContainerVisible(View.INVISIBLE);
                                            break;
                                    }
                                    break;
                                case "com.google.android.gm.permission.AUTO_SEND":
                                    iconPermission = getResources().getDrawable(R.drawable.send_sms_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("com.google.android.gm.permission.AUTO_SEND", packageName);
                                    System.out.println("VALORE GGGGG: " + permControl);
                                    switch (permControl) {
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            permesso.setContainerVisible(View.VISIBLE);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            permesso.setContainerVisible(View.INVISIBLE);
                                            break;
                                    }
                                    break;
                                case "com.google.android.gms.permission.ACTIVITY_RECOGNITION":
                                    iconPermission = getResources().getDrawable(R.drawable.fitness);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("com.google.android.gms.permission.ACTIVITY_RECOGNITION", packageName);
                                    System.out.println("VALORE GGGGG: " + permControl);
                                    switch (permControl) {
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            permesso.setContainerVisible(View.VISIBLE);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            permesso.setContainerVisible(View.INVISIBLE);
                                            break;
                                    }
                                    break;
                                case "com.android.launcher.permission.READ_SETTINGS":
                                    iconPermission = getResources().getDrawable(R.drawable.read_settings);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("com.android.launcher.permission.READ_SETTINGS", packageName);
                                    System.out.println("VALORE GGGGG: " + permControl);
                                    switch (permControl) {
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            permesso.setContainerVisible(View.VISIBLE);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            permesso.setContainerVisible(View.INVISIBLE);
                                            break;
                                    }
                                    break;
                                case "com.android.launcher.permission.WRITE_SETTINGS":
                                    iconPermission = getResources().getDrawable(R.drawable.write_settings);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("com.android.launcher.permission.WRITE_SETTINGS", packageName);
                                    System.out.println("VALORE GGGGG: " + permControl);
                                    switch (permControl) {
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            permesso.setContainerVisible(View.VISIBLE);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            permesso.setContainerVisible(View.INVISIBLE);
                                            break;
                                    }
                                    break;
                                case "android.permission.RESTART_PACKAGES":
                                    iconPermission = getResources().getDrawable(R.drawable.application_close);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.RESTART_PACKAGES", packageName);
                                    System.out.println("VALORE GGGGG: " + permControl);
                                    switch (permControl) {
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            permesso.setContainerVisible(View.VISIBLE);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            permesso.setContainerVisible(View.INVISIBLE);
                                            break;
                                    }
                                    break;
                                case "android.permission.DOWNLOAD_WITHOUT_NOTIFICATION":
                                    iconPermission = getResources().getDrawable(R.drawable.download_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.DOWNLOAD_WITHOUT_NOTIFICATION", packageName);
                                    System.out.println("VALORE GGGGG: " + permControl);
                                    switch (permControl) {
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            permesso.setContainerVisible(View.VISIBLE);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            permesso.setContainerVisible(View.INVISIBLE);
                                            break;
                                    }
                                    break;
                                case "android.permission.DISABLE_KEYGUARD":
                                    iconPermission = getResources().getDrawable(R.drawable.disable_keyguard_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.DISABLE_KEYGUARD", packageName);
                                    System.out.println("VALORE GGGGG: " + permControl);
                                    switch (permControl) {
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            permesso.setContainerVisible(View.VISIBLE);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            permesso.setContainerVisible(View.INVISIBLE);
                                            break;
                                    }
                                    break;
                                case "android.permission.REORDER_TASKS":
                                    iconPermission = getResources().getDrawable(R.drawable.reorder_tasks_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.REORDER_TASKS", packageName);
                                    System.out.println("VALORE GGGGG: " + permControl);
                                    switch (permControl) {
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            permesso.setContainerVisible(View.VISIBLE);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            permesso.setContainerVisible(View.INVISIBLE);
                                            break;
                                    }
                                    break;
                                case "android.permission.SET_WALLPAPER":
                                    iconPermission = getResources().getDrawable(R.drawable.set_wallpaper_icon);
                                    permesso.setIcon(iconPermission);
                                    permControl = pm.checkPermission("android.permission.SET_WALLPAPER", packageName);
                                    System.out.println("VALORE GGGGG: " + permControl);
                                    switch (permControl) {
                                        case PackageManager.PERMISSION_GRANTED:
                                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                                            permesso.setCheckPermission(grant);
                                            permesso.setContainerVisible(View.VISIBLE);
                                            break;
                                        case PackageManager.PERMISSION_DENIED:
                                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                                            permesso.setCheckPermission(denied);
                                            permesso.setContainerVisible(View.INVISIBLE);
                                            break;
                                    }
                                    break;
                            }
                            arrayAdapterDescription.add(permesso);
                        }

                        check = false;

                        continue;
                    }
                    permissionControl = pm.checkPermission(requestedPermissions[j], packageName);
                    System.out.println("VALORE GGGGG: " + permissionControl);
                    switch (permissionControl) {
                        case PackageManager.PERMISSION_GRANTED:
                            System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                            permesso.setCheckPermission(grant);
                            permesso.setContainerVisible(View.VISIBLE);
                            break;
                        case PackageManager.PERMISSION_DENIED:
                            System.out.println("1234 PERMESSO NON CONCESSO!!!");
                            permesso.setCheckPermission(denied);
                            permesso.setContainerVisible(View.INVISIBLE);
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
        dialog = ProgressDialog.show(AppDetails.this, "",
                "Caricamento. Attendere...", true);
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
                dialog.dismiss();
            }
        });
    }

    private void UpdateAPK() {                                                      //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!AVVIOOOO!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        RWFile rwfile = new RWFile();                                               //CLASSE PER LETTURA,SCRITTURA,GENERAZIONE,COPIA DI UN FILE
        System.out.println("Entriiii??");
        for (int i = 0; i < this.permls.size(); i++) {
            System.out.println("HAI FATTO????: " + this.permls.size());
            System.out.println("okkkkk????: " + this.permls.get(i));

            if (((Permesso) this.permls.get(i)).GetChecked().booleanValue()) {
                this.xmlfile.RemovePermission(i);                                           //Rimozione del permesso da XMLFILE
            }
        }
        //CREAZIONE BACKUP APK
        if (this.apkfile.length() > 7) {
            new File(this.pathBackup).mkdirs();                                                                //CREA LA DIRECTORY DI BACKUP
            System.out.println("CREAZIONEEE: " + this.apkfile);
            apkBackup = this.pathBackup + rwfile.GenFilename(this.apkfile, this.pathBackup, "");
            try {
                rwfile.CopyFile(this.apkfile, apkBackup);         //Copia del file "apk" originale nella directory del backup
                System.out.println("COPIA AVVENUTAAAAA");
            } catch (Exception e) {
            }
        }
        if (new Unzip().Unzip(this.apkfile, this.path + this.pathTmp).booleanValue()) {                             //Se il valore è true
            new File(this.path + this.pathTmp + "AndroidManifest.xml").delete();                                    //Cancella la directory temp con il Manifest
            System.out.println("CANCELLAZIONE AVVENUTA");
            if (this.xmlfile.WriteFile(this.path + this.pathTmp + "AndroidManifest.xml").booleanValue()) {          //Se RWFile ha effettuato la scrittura del file
                new UpdateSHAFiles(this.path + this.pathTmp, getPackageInfo().versionName, getString(R.string.app_name)).Update();          //versioName -> il nome della versione del pacchetto; getString() = "Nome App"
                System.out.println("SCRITTURA AVVENUTA");

                new File(this.pathNew).mkdirs();                                                //Crea directory /sdcard/at.plop.PermissionRemover/new
                String newfilename = this.pathNew + rwfile.GenFilename(this.apkfile, this.pathNew, " new");                     //NUOVO NOME DEL FILE

                /**
                 * Qui avviene la firma del apk, utilizzando una libreria esterna ZipSigner.jar
                 * @sources zipsigner-lib-1.17.jar;
                 * @sources kellinwood-logging-lib.1.1.jar
                 * @sources zipio-lib-1.8.jar
                 */
                try {
                    ZipSigner zipSigner = new ZipSigner();
                    zipSigner.setKeymode("testkey");
                    zipSigner.signZip(apkBackup, newfilename);
                } catch (Throwable t) {
                    Log.e("Signing apk", "Error while signing apk to external directory", t);
                    t.printStackTrace();
                }

                //new Compress(this.path +this.pathTmp, new DirectoryFiles(this.path +this.pathTmp).GetOnlyFiles(), newfilename).zip();
                RemoveTempDir();
                this.newapkfile = newfilename;
                String packageName = GetPackageName(this.apkfile);
                System.out.println("PACK NAME::  " + packageName);
                if (packageName.equals("")) {
                    InstallPackage(this.newapkfile);                                                                //INSTALLAZIONE PACCHETTO
                    finish();                                                                                       //SI CHIUDE L'ACTIVITY
                    return;
                } else if (true) {
                    Toast.makeText(getApplicationContext(), "Quest'app deve essere disinstallata prima di poterla installare.", Toast.LENGTH_LONG).show();
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
        new DirectoryRemove("").RemoveDir(this.path + this.pathTmp);
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


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean isPresent = isAppPresent(packageName, this);
        if (isPresent) {
            //L'utente ha cliccato su annulla disinstallazione
            finish();
        } else {
            if (this.activityReturn == UNINSTALL.intValue()) {
                Toast.makeText(getApplicationContext(), "IL FILE APK DI BACKUP SI TROVA IN /storage/emulated/0/Android/data/com.example.angiopasqui.permissionchecker/files/backup", Toast.LENGTH_LONG).show();
                InstallPackage(this.newapkfile);
                finish();
            }
        }
    }

    public static boolean isAppPresent(String packageName, Context context) {                       //CONTROLLA SE L'APP SIA ANCORA PRESENTE O MENO NEL SISTEMA
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
