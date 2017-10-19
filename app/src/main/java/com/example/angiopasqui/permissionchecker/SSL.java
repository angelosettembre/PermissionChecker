package com.example.angiopasqui.permissionchecker;

import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class SSL {
    public String cert;
    public String certName = "testkey.x509.pem";                                        //PRIMA CHIAVE PER LA FIRMA DELL'APK
    public String path;
    public String privatekey;
    public String privatekeyName = "testkey.pk8";                                       //SECONDA CHIAVE PER LA FIRMA DELL'APK

    private static native void Sign();                                                  //METODO NATIVO

    static {
        System.out.println("CIAOOONEEEEE::");
        System.loadLibrary("plopapksign");                                              //CARICAMENTO LIBRERIA DI CODICE NATIVO PER LA FIRMA DEL APK /src/main/jniLibs/armeabi/libplopaksing.so ("senza .so")
    }

    public SSL(String path) {
        this.path = path;
        this.privatekey = new StringBuilder(String.valueOf(path)).append("testkey.pk8").toString();
        this.cert = new StringBuilder(String.valueOf(path)).append("testkey.x509.pem").toString();
    }

    public boolean CertAvailable() {                                                    //Disponibilità certificato
        RWFile rwfile = new RWFile();                                                   //CLASSE PER LETTURA,SCRITTURA,GENERAZIONE,COPIA DI UN FILE
        if (rwfile.Exists(this.privatekey) || rwfile.Exists(this.cert)) {               //SE ALMENO UNA DELLE DUE CHIAVI ESISTE
            return true;
        }
        return false;
    }

    public void SignIt() {                                                              //Metodo per la firma
        RWFile f = new RWFile();
        Sign();                                                                         //Chiamata a metodo nativo
        System.out.println("FIRMA EFFETTUATAAAAAA");
        f.Delete("/sdcard/at.plop.PermissionRemover/tmp/testkey");                      //Cancellazione del file "testkey" dalla cartella /tmp
    }

    public boolean TestCert(PackageManager pm, String packageName) {                    //Verifica Certificato
        try {
            InputStream input = new ByteArrayInputStream(pm.getPackageInfo(packageName, 64).signatures[0].toByteArray());               //getPackageInfo(64) -> prende la firma del packageName; .signatures è la lista di tutte le firme del packageName;
            CertificateFactory cf = null;                           //Classe che definisce le funzionalità di un certificato
            try {
                cf = CertificateFactory.getInstance("X509");        //Restituisce un oggetto certificato che implementa il tipo di certificato specificato
            } catch (CertificateException e) {
                e.printStackTrace();
            }
            X509Certificate c = null;                               //Classe che permette l'accesso a tutti gli attributi del certificato X.509
            try {
                c = (X509Certificate) cf.generateCertificate(input);       //Genera un oggetto di certificato e l'inizializza con i dati letti dal flusso di input "input"
            } catch (CertificateException e2) {
                e2.printStackTrace();
            }
            if (c.getSubjectDN().toString().indexOf("CN=Android, OU=Android") != -1) {              //getSubjectDN() ritorna l'oggetto "subject" com oggetto di implementazione dal certificato, dove subject è il nome del soggetto distinto;
                                                                                                    //indexOf ritorna l'indice all'interno di questa stringa, della prima occorrenza della sottostringa specificata; l'indice restituito è il più piccolo valore k;
                                                                                                    //se nessun valore k esiste, allora -1 viene restituito
                return true;
            }
            return false;
        } catch (NameNotFoundException e3) {
            return false;
        }
    }
}
