package com.isislab.settembre.privacychecker;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.isislab.settembre.privacychecker.privacyLeaks.Configuration;
import com.isislab.settembre.privacychecker.privacyLeaks.FileHelper;
import com.isislab.settembre.privacychecker.privacyLeaks.GlobalState;
import com.isislab.settembre.privacychecker.privacyLeaks.LocalVpnService;
import com.isislab.settembre.privacychecker.privacyLeaks.PrivacyLeaksMain;
import com.isislab.settembre.privacychecker.privacyLeaks.db.RuleDatabaseUpdateTask;

public class MainActivity extends Activity {
    public static ImageView icon_lock_unlcok;
    public static TextView textMonitoring;
    ImageView icon_listApp;
    TextView text_listApp;
    public static TextView text_init_Monitoring;
    public static ImageView start;
    public static ImageView stop;
    Button goPrivacyLeaksActivity;
    public static Context context;
    private SharedPreferences preferences;
    public static String TAG="DEBUG";
    public static Configuration config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setLogo(R.mipmap.ic_launcher);
        actionBar.setDisplayUseLogoEnabled(true);

        context = this;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        config = FileHelper.loadCurrentSettings(this);


        icon_lock_unlcok = (ImageView) findViewById(R.id.icon_lock_unlock);
        textMonitoring = (TextView) findViewById(R.id.monitoring);
        icon_listApp = (ImageView) findViewById(R.id.icon_listApp);
        text_listApp = (TextView) findViewById(R.id.text_listApp);
        text_init_Monitoring = (TextView) findViewById(R.id.text_initMonitoring);
        start = (ImageView) findViewById(R.id.start);
        stop = (ImageView) findViewById(R.id.stop);
        goPrivacyLeaksActivity = (Button) findViewById(R.id.goPrivacyLeaksActivity);

        refresh();

        if (preferences.getString("vpn abilitata","").equalsIgnoreCase("si")) {    //SE LA VPN E' AVVIATA
            Log.d("DEBUG","SHARED PREFERENCES");

            text_init_Monitoring.setText(R.string.stop_monitoring);
            textMonitoring.setText(R.string.monitoring_active);
            icon_lock_unlcok.setImageResource(R.drawable.lock_open);
            start.setVisibility(View.INVISIBLE);
            stop.setVisibility(View.VISIBLE);
        }

        icon_listApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),AppList.class);
                startActivity(i);
            }
        });

        text_listApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),AppList.class);
                startActivity(i);
            }
        });

        goPrivacyLeaksActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),PrivacyLeaksMain.class);
                startActivity(i);
            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("DEBUG","cliccato START");
                if(!hasPermission()){
                    Log.d("DEBUG","PERMESSO NON TROVATO");
                    AlertDialog.Builder dialog;

                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                        dialog = new AlertDialog.Builder(context,android.R.style.Theme_Material_Light_Dialog_Alert);
                    }
                    else{
                        dialog = new AlertDialog.Builder(context);
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

                if ((!GlobalState.VPN_ENABLED )){         //SE IL PULSANTE E' CLICCATO , E LA VPN NON E' ABILITATA
                    if(vpnController(true, MainActivity.this)){
                        start.setVisibility(View.INVISIBLE);
                        stop.setVisibility(View.VISIBLE);
                    }
                }


            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("DEBUG","cliccato STOP");
                if(GlobalState.VPN_ENABLED || !(start.VISIBLE==0)){
                    stopVPNService();                       //SI ARRESTA LA VPN
                    stop.setVisibility(View.INVISIBLE);
                    start.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public static boolean hasPermission(){
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = 0;
        mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private void refresh() {
        Log.d("DEBUG","metodo refresh");

        final RuleDatabaseUpdateTask task = new RuleDatabaseUpdateTask(getApplicationContext(), config, true);

        task.execute();
    }

    public static boolean vpnController(boolean start, Activity mActivity) {      //METODO PER IL CONTROLLER PER LA VPN (viene passato il valore booleano start, e l'activity principale)
        if(start ){                    //SE LA VPN NON E' AVVIATA

            if (((ConnectivityManager) mActivity.getBaseContext().getSystemService(CONNECTIVITY_SERVICE)).getActiveNetworkInfo() == null) {   //SE NON C'E' CONNETTIVITA' SUL DISPOSITVO
                Log.i(TAG, "Error opening VPN service. Your device does not seem to have connectivity. ");
                Toast.makeText(mActivity.getBaseContext(), R.string.nonetworkaccess, Toast.LENGTH_LONG).show();
                return false;
            }

            startVPNService(mActivity);     //VIENE CHIAMATO IL METODO PER AVVIARE il SERVIZIO VPN
            GlobalState.launching_vpn = true;
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
            launchVpnService(this);   //VIENE LANCIATA LA VPN
        }
        else {
            stop.setVisibility(View.INVISIBLE);
            start.setVisibility(View.VISIBLE);
        }
    }

    //METODO CHE PERMETTE DI AVVIARE LA VPN
    protected static void launchVpnService(Activity mActivity) {
        if(hasPermission()) {
            Log.d("DEBUG", "LANCIO VPN!!!!!!!!!!!!!!!!!!!!!!");


            text_init_Monitoring.setText(R.string.stop_monitoring);
            textMonitoring.setText(R.string.monitoring_active);
            icon_lock_unlcok.setImageResource(R.drawable.lock_open);

            GlobalState.VPN_ENABLED = true;
            Toast.makeText(context, "Utilizza le tue app per effettuare il monitoraggio", Toast.LENGTH_LONG).show();
            mActivity.getBaseContext().startService(new Intent(mActivity.getBaseContext(), LocalVpnService.class));    //VIENE AVVIATO IL SERVIZIO, CREANDO UN INTENT ATTRAVERSO LA CLASSE LocalVpnService
        }
        else{
            showPermissionNotGranted(context);
            stop.setVisibility(View.INVISIBLE);
            start.setVisibility(View.VISIBLE);

        }
    }

    static void showPermissionNotGranted(Context context) {
        Toast.makeText(context,"Il permesso di analizzare le app non è abilitato, devi concedere il permesso", Toast.LENGTH_LONG).show();   //VIENE MOSTRATO IL TOAST
    }

    public static void stopVPNService(){
        LocalVpnService.getInstance().stopVPN();
        context.stopService(new Intent(context,LocalVpnService.class));

        text_init_Monitoring.setText(R.string.init_monitoring);
        textMonitoring.setText(R.string.monitoring_not_active);
        icon_lock_unlcok.setImageResource(R.drawable.lock);

        Toast.makeText(context, "VPN DISCONNESSA", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        Log.d("DEBUG","onDestroy MainActivity");

        super.onDestroy();
    }
}
