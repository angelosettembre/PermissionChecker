package com.example.angiopasqui.permissionchecker;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class DownloadCert {
    protected Handler _taskHandler1 = new Handler();
    private Dialog dialog;
    private Activity mActivity;
    private String mDir;
    private boolean mFromSetupPermissions;
    private AppDetails mSetupPermissions;
    private SSL ssl;
    private boolean update;

    public DownloadCert(Activity context, String dir) {                                         //Ci passa l'activity SetupPermissions.class
        this.mActivity = context;
        this.mDir = dir;
        this.mFromSetupPermissions = false;
    }

    public DownloadCert(Activity context, String dir, AppDetails s) {
        this.mActivity = context;
        this.mDir = dir;
        this.mSetupPermissions = s;
        this.mFromSetupPermissions = true;
    }

    public void show() {                                                        //RICHIESTA PER SCARICARE IL FILE
        this.ssl = new SSL(this.mDir);                                          //mDir: path dove si trova la chiave
        if (!this.ssl.CertAvailable()) {
            new File(this.mDir).mkdirs();
            this.dialog = new Dialog(this.mActivity);
            this.dialog.setContentView(R.layout.custom_dialog);
            this.dialog.setTitle("Downloading");                                 //Mostra dialog per scaricare i file
            ((TextView) this.dialog.findViewById(R.id.text)).setText("       Please wait...       ");
            new Builder(this.mActivity).setTitle("Signing key and certificate not found").setMessage("The key and the certificate to sign the apps wasn't found. Should I download them? Total size is only 2.8 KByte\n\nInfo: You can search and download the file \"testkey.pk8\" and \"testkey.x509.pem\" by yourself and store it in " + this.mDir + "\n\nOr simply press the download button to start the download of the 2.8 KByte.").setPositiveButton(R.string.download, new OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    DownloadCert.this.dialog.show();
                    DownloadCert.this.StartDownload();                      //Fai partire il download
                }
            }).setNegativeButton("NO", new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (DownloadCert.this.mFromSetupPermissions) {
                        DownloadCert.this.mSetupPermissions.NoDownload();
                    }
                }
            }).setOnCancelListener(new OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    if (DownloadCert.this.mFromSetupPermissions) {
                        DownloadCert.this.mSetupPermissions.NoDownload();
                    }
                }
            }).create().show();
        }
    }

    private void StartDownload() {
        this.update = true;
        this._taskHandler1.post(new Runnable() {                                            //Avvio metodo Download con Thread
            public void run() {
                if (DownloadCert.this.update) {
                    DownloadCert.this.update = false;
                    DownloadCert.this._taskHandler1.post(this);
                    return;
                }
                DownloadCert.this.Download();
            }
        });
    }

    private void Download() {
        HTTP http = new HTTP();
        String[] urls2 = new String[]{"https://pdn-slatedroid.googlecode.com/svn-history/r30/trunk/eclair/build/target/product/security/testkey.x509.pem", "https://github.com/CyanogenMod/android_build/raw/gingerbread/target/product/security/testkey.x509.pem", "http://git.sourceforge.jp/view?p=gb-231r1-is01/GB_2.3_IS01.git;a=blob_plain;f=build/target/product/security/testkey.x509.pem;hb=d1887abb5bb3fffac1735fef558335c3d556dfc5"};
        if (!http.Get(new String[]{"https://pdn-slatedroid.googlecode.com/svn-history/r30/trunk/eclair/build/target/product/security/testkey.pk8", "https://github.com/CyanogenMod/android_build/raw/gingerbread/target/product/security/testkey.pk8", "http://git.sourceforge.jp/view?p=gb-231r1-is01/GB_2.3_IS01.git;a=blob_plain;f=build/target/product/security/testkey.pk8;hb=d1887abb5bb3fffac1735fef558335c3d556dfc5"}, this.ssl.privatekey)) {
            Toast.makeText(this.mActivity, "Download error: unable to get " + this.ssl.privatekeyName, Toast.LENGTH_LONG).show();
            DLError();
        } else if (http.Get(urls2, this.ssl.cert)) {                                                    //cert = chiave "testkey.x509.pem"
            Toast.makeText(this.mActivity, "The key and certificate download was successful", Toast.LENGTH_LONG).show();
            this.dialog.dismiss();
            if (this.mFromSetupPermissions) {
                this.mSetupPermissions.RestartUpdateAPK();
            }
        } else {
            Toast.makeText(this.mActivity, "Download error: unable to get " + this.ssl.certName, Toast.LENGTH_LONG).show();
            DLError();
        }
    }

    private void DLError() {
        this.dialog.dismiss();
        if (this.mFromSetupPermissions) {
            this.mSetupPermissions.NoDownload();
        }
    }
}
