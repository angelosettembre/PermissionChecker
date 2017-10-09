package com.example.angiopasqui.permissionchecker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
//


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
    int count = 0;
    View v;
    ImageView iconGrantedDenied;
    TextView textGrantedDenied;

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

        v = getLayoutInflater().inflate(R.layout.app_list_detail_item,null);

        /*iconGrantedDenied = (ImageView) v.findViewById(R.id.granted_denied);
        textGrantedDenied = (TextView) v.findViewById(R.id.text_granted_denied);*/

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

        try {
            pi = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
            String[] requestedPermissions = pi.requestedPermissions;

            if (requestedPermissions != null) {
                for (int j = 0; j < requestedPermissions.length; j++) {
                    Log.d("test 888", requestedPermissions[j]);
                    /*
                    int g = pm.checkPermission(requestedPermissions[j],packageName);
                    if(g == PackageManager.PERMISSION_GRANTED){
                        System.out.println("1234 PERMESSO GIA' CONCESSO!!!");
                        iconGrantedDenied.setImageResource(R.drawable.granted);
                        textGrantedDenied.setText("CONCESSO");
                        System.out.println("VALIIII TESTOOO CONCESSO: "+textGrantedDenied.getText());
                    }
                    if(g == PackageManager.PERMISSION_DENIED){
                        System.out.println("1234 PERMESSO NON CONCESSO!!!");
                        iconGrantedDenied.setImageResource(R.drawable.denied);
                        textGrantedDenied.setText("NEGATO");
                        System.out.println("VALIIII TESTOOO NEGATO: "+textGrantedDenied.getText());
                    }*/

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
                                    break;
                                case "android.permission.BLUETOOTH":
                                    iconPermission = getResources().getDrawable(R.drawable.bluetooth_icon);
                                    permesso.setIcon(iconPermission);
                                    break;
                                case "com.google.android.c2dm.permission.RECEIVE":
                                    iconPermission = getResources().getDrawable(R.drawable.receive_wap_push_icon);
                                    permesso.setIcon(iconPermission);
                                    break;
                                case "com.google.android.providers.gsf.permission.READ_GSERVICES":
                                    iconPermission = getResources().getDrawable(R.drawable.gs_service);
                                    permesso.setIcon(iconPermission);
                                    break;
                                case "android.permission.MODIFY_AUDIO_SETTINGS":
                                    iconPermission = getResources().getDrawable(R.drawable.modify_audio_settings_icon);
                                    permesso.setIcon(iconPermission);
                                    break;
                                case "android.permission.ACCESS_NETWORK_STATE":
                                    iconPermission = getResources().getDrawable(R.drawable.access_network_state_icon);
                                    permesso.setIcon(iconPermission);
                                    break;
                                case "android.permission.ACCESS_WIFI_STATE":
                                    iconPermission = getResources().getDrawable(R.drawable.access_wifi_state_icon);
                                    permesso.setIcon(iconPermission);
                                    break;
                                case "android.permission.WAKE_LOCK":
                                    iconPermission = getResources().getDrawable(R.drawable.wake_lock);
                                    permesso.setIcon(iconPermission);
                                    break;
                                case "android.permission.MANAGE_ACCOUNTS":
                                    iconPermission = getResources().getDrawable(R.drawable.manage_account);
                                    permesso.setIcon(iconPermission);
                                    break;
                                case "android.permission.READ_PROFILE":
                                    iconPermission = getResources().getDrawable(R.drawable.read_profile_icon);
                                    permesso.setIcon(iconPermission);
                                    break;
                                case "android.permission.WRITE_SYNC_SETTINGS":
                                    iconPermission = getResources().getDrawable(R.drawable.sync);
                                    permesso.setIcon(iconPermission);
                                    break;
                                case "android.permission.READ_SYNC_SETTINGS":
                                    iconPermission = getResources().getDrawable(R.drawable.read_sync_settings_icon);
                                    permesso.setIcon(iconPermission);
                                    break;
                                case "android.permission.AUTHENTICATE_ACCOUNTS":
                                    iconPermission = getResources().getDrawable(R.drawable.authenticate_accounts_icon);
                                    permesso.setIcon(iconPermission);
                                    break;
                                case "android.permission.VIBRATE":
                                    iconPermission = getResources().getDrawable(R.drawable.vibrate_icon);
                                    permesso.setIcon(iconPermission);
                                    break;
                                case "android.permission.SYSTEM_ALERT_WINDOW":
                                    iconPermission = getResources().getDrawable(R.drawable.alert);
                                    permesso.setIcon(iconPermission);
                                    break;
                                case "android.permission.RECEIVE_BOOT_COMPLETED":
                                    iconPermission = getResources().getDrawable(R.drawable.receive_boot_completed_icon);
                                    permesso.setIcon(iconPermission);
                                    break;
                                case "com.android.launcher.permission.INSTALL_SHORTCUT":
                                    iconPermission = getResources().getDrawable(R.drawable.install_shortcut_icon);
                                    permesso.setIcon(iconPermission);
                                    break;
                                case "com.android.launcher.permission.UNINSTALL_SHORTCUT":
                                    iconPermission = getResources().getDrawable(R.drawable.uninstall_shortcut_icon);
                                    permesso.setIcon(iconPermission);
                                    break;
                                case "android.permission.CHANGE_WIFI_STATE":
                                    iconPermission = getResources().getDrawable(R.drawable.access_wifi_state_icon);
                                    permesso.setIcon(iconPermission);
                                    break;
                                case "android.permission.NFC":
                                    iconPermission = getResources().getDrawable(R.drawable.nfc_icon);
                                    permesso.setIcon(iconPermission);
                                    break;
                                case "android.permission.READ_SYNC_STATS":
                                    iconPermission = getResources().getDrawable(R.drawable.stats);
                                    permesso.setIcon(iconPermission);
                                    break;
                                case "android.permission.WRITE_SETTINGS":
                                    iconPermission = getResources().getDrawable(R.drawable.write_sett);
                                    permesso.setIcon(iconPermission);
                                    break;
                                case "android.permission.GET_TASKS":
                                    iconPermission = getResources().getDrawable(R.drawable.task);
                                    permesso.setIcon(iconPermission);
                                    break;
                                case "android.permission.GET_PACKAGE_SIZE":
                                    iconPermission = getResources().getDrawable(R.drawable.get_package_size_icon);
                                    permesso.setIcon(iconPermission);
                                    break;
                                case "android.permission.BROADCAST_STICKY":
                                    iconPermission = getResources().getDrawable(R.drawable.broadcast_sticky_icon);
                                    permesso.setIcon(iconPermission);
                                    break;
                                case "android.permission.CHANGE_NETWORK_STATE":
                                    iconPermission = getResources().getDrawable(R.drawable.change_network_state_icon);
                                    permesso.setIcon(iconPermission);
                                    break;
                                case "android.permission.EXPAND_STATUS_BAR":
                                    iconPermission = getResources().getDrawable(R.drawable.expand_status_bar_icon);
                                    permesso.setIcon(iconPermission);
                                    break;
                                case "android.permission.BLUETOOTH_ADMIN":
                                    iconPermission = getResources().getDrawable(R.drawable.bluetooth_admin_icon);
                                    permesso.setIcon(iconPermission);
                                    break;
                                case "android.permission.USE_CREDENTIALS":
                                    iconPermission = getResources().getDrawable(R.drawable.use_credentials_icon);
                                    permesso.setIcon(iconPermission);
                                    break;
                                case "android.permission.REQUEST_INSTALL_PACKAGES":
                                    iconPermission = getResources().getDrawable(R.drawable.request_install_packages_icon);
                                    permesso.setIcon(iconPermission);
                                    break;
                                case "android.permission.WRITE_SMS":
                                    iconPermission = getResources().getDrawable(R.drawable.write_sms_icon);
                                    permesso.setIcon(iconPermission);
                                    break;
                                case "android.permission.CHANGE_WIFI_MULTICAST_STATE":
                                    iconPermission = getResources().getDrawable(R.drawable.change_wifi_multicast_state_icon);
                                    permesso.setIcon(iconPermission);
                                    break;
                                case "android.permission.KILL_BACKGROUND_PROCESSES":
                                    iconPermission = getResources().getDrawable(R.drawable.kill_background_processes_icon);
                                    permesso.setIcon(iconPermission);
                                    break;
                                case "com.google.android.gm.permission.AUTO_SEND":
                                    iconPermission = getResources().getDrawable(R.drawable.send_sms_icon);
                                    permesso.setIcon(iconPermission);
                                    break;
                                case "com.google.android.gms.permission.ACTIVITY_RECOGNITION":
                                    iconPermission = getResources().getDrawable(R.drawable.fitness);
                                    permesso.setIcon(iconPermission);
                                    break;
                                case "com.android.launcher.permission.READ_SETTINGS":
                                    iconPermission = getResources().getDrawable(R.drawable.read_settings);
                                    permesso.setIcon(iconPermission);
                                    break;
                                case "com.android.launcher.permission.WRITE_SETTINGS":
                                    iconPermission = getResources().getDrawable(R.drawable.write_settings);
                                    permesso.setIcon(iconPermission);
                                    break;
                                case "android.permission.RESTART_PACKAGES":
                                    iconPermission = getResources().getDrawable(R.drawable.application_close);
                                    permesso.setIcon(iconPermission);
                                    break;
                                case "android.permission.DOWNLOAD_WITHOUT_NOTIFICATION":
                                    iconPermission = getResources().getDrawable(R.drawable.download_icon);
                                    permesso.setIcon(iconPermission);
                                    break;
                                case "android.permission.DISABLE_KEYGUARD":
                                    iconPermission = getResources().getDrawable(R.drawable.disable_keyguard_icon);
                                    permesso.setIcon(iconPermission);
                                    break;
                                case "android.permission.REORDER_TASKS":
                                    iconPermission = getResources().getDrawable(R.drawable.reorder_tasks_icon);
                                    permesso.setIcon(iconPermission);
                                    break;
                                case "android.permission.SET_WALLPAPER":
                                    iconPermission = getResources().getDrawable(R.drawable.set_wallpaper_icon);
                                    permesso.setIcon(iconPermission);
                                    break;
                            }
                            arrayAdapterDescription.add(permesso);
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

    public void allowPermission(View v){
        System.out.println("CLICCATOOOOOO !!!");
    }

    public void openDialog(View v){
        System.out.println("CLICCATOOOOOO  22222!!!");
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_check,null));


        builder.setMessage("Stai per ripartire da capo. Sei sicuro?")
                .setPositiveButton("Ok", dialogClickListener)
                .setNegativeButton("Annulla", dialogClickListener).show();
        return;
    }
}
