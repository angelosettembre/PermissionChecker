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
import android.widget.CompoundButton;
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
    static ToggleButton startButton;
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
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        startButton = (ToggleButton)findViewById(R.id.toggleButton);
        listView = (ListView)findViewById(R.id.listAppLeaks);

        adapter = new AppLeaksAdapter(this,R.layout.listappleaks_detail_item);

        listView.setAdapter(adapter);

        if (preferences.getString("vpn abilitata","").equalsIgnoreCase("si")) {    //SE LA VPN E' AVVIATA
            Log.d("DEBUG","SHARED PREFERENCES");
            startButton.setChecked(true);                                          //VIENE MESSO IL TASTO SU ON
        }
        startButton.setTextOff(getResources().getString(R.string.offToggleState));
        startButton.setTextOn(getResources().getString(R.string.onToggleState));


        startButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    Log.d("DEBUG", "PREMUTOOOOOOOO ON");

                    if(!hasPermission()){
                        Log.d("DEBUG","PERMESSO NON TROVATO");
                        AlertDialog.Builder dialog;

                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                            dialog = new AlertDialog.Builder(activity,android.R.style.Theme_Material_Light_Dialog_Alert);
                        }
                        else{
                            dialog = new AlertDialog.Builder(activity);
                        }
                        dialog.setTitle("Avviso")
                        .setMessage("Per avviare il monitoraggio, devi concedere il permesso che permette di analizzare le app in esecuzione")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                            }
                        })
                        .show();
                    }


                    if ((!GlobalState.VPN_ENABLED )  ) {   //SE IL PULSANTE E' CLICCATO ED E' SU ON, E LA VPN NON E' ABILITATA
                        //boolean consent = PreferenceManager.getDefaultSharedPreferences(PrivacyLeaksMain.context).getBoolean("USER_CONSENT", Boolean.FALSE.booleanValue());

                        vpnController(true, PrivacyLeaksMain.activity);       //SE LA VPN E' ABILITATA
                            //PrivacyLeaksMain.startButton.setEnabled(false);

                    }


                }else if ((!isChecked && GlobalState.VPN_ENABLED) || startButton.getText().equals("ON")) {                                                     //SE IL PULSANTE VIENE CLICCATO ED E' SU OFF
                    //PrivacyLeaksMain.vpnController(false, PrivacyLeaksMain.activity);      //CHIAMA IL METODO PASSANDO IL PARAMETRO false
                    Log.d("DEBUG", "PREMUTOOOOOOOO OFF");
                    stopVPNService();                       //SI ARRESTA LA VPN

                    PrivacyLeaksMain.startButton.setEnabled(true);
                }else
                    PrivacyLeaksMain.startButton.setEnabled(true);


            }
        });



    }


    public static boolean hasPermission(){
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = 0;
        mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }



    public boolean vpnController(boolean start, Activity mActivity) {      //METODO PER IL CONTROLLER PER LA VPN (viene passato il valore booleano start, e l'activity principale)
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mActivity.getBaseContext());

        if (!start) {                               //SE START E' FALSE
            if(GlobalState.VPN_ENABLED) {
                stopVPNService();                       //SI ARRESTA LA VPN
                Log.d(TAG, "Stopping vpn. Change button to Off");
                Log.d(TAG, "Button disabled");

                GlobalState.launching_vpn = false;
                GlobalState.VPN_ENABLED = false;
            }
            /*if (notificationEnabled.booleanValue()) {
                //((NotificationManager) mActivity.getBaseContext().getSystemService("notification")).cancel(1);
            }*/
            // new UploadDataAsyncTask(mActivity.getBaseContext()).execute(new Void[0]);    //CREAZIONE OGGETTO UploadDataAsyncTask PER CARICARE I DATI

        }else if(start ){                    //SE LA VPN NON E' AVVIATA

            if (((ConnectivityManager) mActivity.getBaseContext().getSystemService(CONNECTIVITY_SERVICE)).getActiveNetworkInfo() == null) {   //SE NON C'E' CONNETTIVITA' SUL DISPOSITVO
                Log.i(TAG, "Error opening VPN service. Your device does not seem to have connectivity. ");
                Toast.makeText(mActivity.getBaseContext(), R.string.nonetworkaccess, Toast.LENGTH_LONG).show();
                return false;
            }

            startVPNService(mActivity);     //VIENE CHIAMATO IL METODO PER AVVIARE il SERVIZIO VPN
            GlobalState.launching_vpn = true;
            /*if (notificationEnabled.booleanValue()) {  //SE LA NOTIFICA E' ABILITATA
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mActivity.getBaseContext()).setSmallIcon(R.drawable.lumen_bulb_transparent).setContentTitle(mActivity.getBaseContext().getResources().getString(R.string.app_name)).setContentText(mActivity.getBaseContext().getResources().getString(R.string.monitoringStateON));
                mBuilder.setOngoing(false);
                Intent resultIntent = new Intent(mActivity.getBaseContext(), MainActivity.class);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(mActivity.getBaseContext());
                stackBuilder.addParentStack(MainActivity.class);
                stackBuilder.addNextIntent(resultIntent);
                mBuilder.setContentIntent(stackBuilder.getPendingIntent(0, 134217728));
                ((NotificationManager) mActivity.getBaseContext().getSystemService("notification")).notify(1, mBuilder.build());
            }
            new UploadDataAsyncTask(mActivity.getBaseContext()).execute(new Void[0]); //CREAZIONE OGGETTO UploadDataAsyncTask PER CARICARE I DATI*/
        }
        return true;
    }

    public static void startVPNService(Activity mActivity) {    //METODO CHE AVVIA IL SERVIZIO VPN
        GlobalState.start_error = false;
        GlobalState.STOP = false;

        //VIENE CREATO L'INTENT CON L'ACTIVITY PRINCIPALE
        Intent intent = VpnService.prepare(mActivity);      //IL METODO VPN PREPARE, PREPARA LA CONNESSIONE VPN,
                                                            //- QUESTO METODO RITORNA UN INTENT
                                                            //    - SE IL VALORE RITORNATO E' NULL SIGNIFICA CHE LA VPN E' GIA' AVVIATA
                                                            //- ALTRIMENTI RITORNA L'INTENT ALL'ACTIVITY

        if (intent != null) {                               //SE L'INTENT NON E' NULL, CIOE' LA VPN GIA' E' AVVIATA
            mActivity.startActivityForResult(intent, 0);    //VIENE LANCIATA L'ACTIVITY PER PREPARARLA ALLA CONNESSIONE
            Log.d(TAG, "PREPARAZIONE VPN");                  //L'ACTIVITY MOSTRERA' UNA FINESTRA DI DIALOGO PER RICHIEDERE L'AZIONE DELL'UTENTE,
                                                            // E IL RISULTATO SARA' RESTITUITO ATTRAVERSO IL METODO:
                                                            // onActivityResult(int,int, Intent)

        } else {                                            //ALTRIMENTI LANCIA LA VPN
            launchVpnService(mActivity);
        }

    }

    //VIENE CHIAMATO PER RESTITUIRE IL RISTULATO DALL'ACTIVITY
    protected void onActivityResult(int request, int result, Intent data) {
        if (result == -1) {                //RESULT=-1 SIGNIFICA --> RESULT_OK , CIOE' SIGNIFICA CHE L'APPLICAZIONE E' PRONTA PER STABILIRE UNA CONNESSIONE VPN
            Log.d(TAG,"IL RISULTATO DELL'INTENT E' -1");
            launchVpnService(activity);   //VIENE LANCIATA LA VPN
        }
        else
            startButton.setChecked(false);

    }

    //METODO CHE PERMETTE DI AVVIARE LA VPN
    protected static void launchVpnService(Activity mActivity) {
        if(hasPermission()) {
            Log.d("DEBUG", "LANCIO VPN!!!!!!!!!!!!!!!!!!!!!!");
            GlobalState.VPN_ENABLED = true;
            Toast.makeText(context, "Utilizza le tue app per effettuare il monitoraggio", Toast.LENGTH_LONG).show();
            mActivity.getBaseContext().startService(new Intent(mActivity.getBaseContext(), LocalVpnService2.class));    //VIENE AVVIATO IL SERVIZIO, CREANDO UN INTENT ATTRAVERSO LA CLASSE LocalVpnService

        }
        else{
            showPermissionNotGranted(context);
            startButton.setChecked(false);

        }
    }


    static void showPermissionNotGranted(Context context) {
        Toast.makeText(context,"Il permesso di analizzare le app non è abilitato, devi concedere il permesso", Toast.LENGTH_LONG).show();   //VIENE MOSTRATO IL TOAST
    }

    public void stopVPNService(){
        LocalVpnService2.getInstance().stopVPN();
        activity.stopService(new Intent(this,LocalVpnService2.class));
        Toast.makeText(context, "VPN DISCONNESSA", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        Log.d("DEBUG","onDestroy PrivacyActivity");
        if(GlobalState.VPN_ENABLED){
            activity.stopService(new Intent(this,LocalVpnService.class));
            GlobalState.VPN_ENABLED = false;
        }
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
