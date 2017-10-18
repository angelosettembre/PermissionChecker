package com.example.angiopasqui.permissionchecker;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;

public class UpdateSHAFiles {
    private String appname;
    private String basedir;
    private String version;

    public UpdateSHAFiles(String basedir, String version, String appname) {
        this.basedir = basedir;
        this.version = version;
        this.appname = appname;
    }

    public void Update() {
        try {
            for (File ff : new File(this.basedir + "META-INF/").listFiles()) {          //listFiles() - > Restituisce una serie di percorsi astratti che indicano i file nella directory indicata da questo percorso astratto
                if (ff.getName().endsWith(".RSA")) {                                    //Se il file i-esimo termina con .RSA
                    ff.renameTo(new File(this.basedir + "META-INF/CERT.RSA"));          //Rinominalo
                }
                if (ff.getName().endsWith(".SF")) {                                     //Se il file i-esimo termina con .SF
                    ff.renameTo(new File(this.basedir + "META-INF/CERT.SF"));           //Rinominalo
                }
            }
        } catch (Exception e) {
        }
        UpdateFile(this.basedir + "META-INF/MANIFEST.MF", this.basedir + "AndroidManifest.xml", "Name: AndroidManifest.xml", "SHA1-Digest: ", true);
        UpdateFile(this.basedir + "META-INF/CERT.SF", this.basedir + "META-INF/MANIFEST.MF", "Created", "SHA1-Digest-Manifest: ", false);
    }

    private void UpdateFile(String filename, String sha1file, String tag, String sha1digestprefix, boolean replace) {
        String[] lines = new String[1];
        FileSHA1 filesha1 = new FileSHA1();
        int numLines = 0;
        String sha1digest = "";
        lines[0] = "";
        int i2;
        try {
            sha1digest = filesha1.SHA1(sha1file);               //CODIFICA DEL FILE CON SHA1
        } catch (Exception e) {
            Log.e("sha1", "error");
        }
        try {
            InputStream instream = new FileInputStream(filename);                                  //rappresentano un flusso di byte di input
            while (new BufferedReader(new InputStreamReader(instream)).readLine() != null) {        //InputStreamReader -> è un ponte dai flussi di byte ai flussi di caratteri: legge i byte e li decodifica in caratteri utilizzando una specifica charset
                                                                                                    //readLine() -> legge una riga di testo
                numLines++;
            }
            instream.close();
            lines = new String[numLines];
            instream = new FileInputStream(filename);
            BufferedReader buffreader = new BufferedReader(new InputStreamReader(instream));        //Legge il testo da un flusso di input di caratteri memorizzando i caratteri in modo da fornire una lettura efficiente di caratteri, array e righe.
            int i = 0;
            while (true) {
                String line = buffreader.readLine();                                                //readLine() -> legge una riga di testo
                if (line == null) {
                    break;
                }
                i2 = i + 1;
                lines[i] = line;
                i = i2;
            }
            instream.close();
        } catch (Exception e2) {
            Log.e("a", "excep0");
        }
        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(filename, false));               //Scrive un testo a un flusso di caratteri-output, che bufferizza i caratteri in modo da assicurare la scrittura efficace di singoli caratteri, array e stringhe.
            boolean skip = false;
            boolean allowwrite = false;
            i2 = 0;
            while (i2 < lines.length) {
                if (skip) {
                    buf.append(new StringBuilder(String.valueOf(sha1digestprefix)).append(sha1digest).toString());      //Concatena = "SHA1-Digest: "+ "sha1digest = filesha1.SHA1(sha1file)  "
                    skip = false;
                } else {
                    if (replace) {
                        if (lines[i2].length() >= tag.length() && lines[i2].substring(0, tag.length()).equals(tag)) {
                            skip = true;
                        }
                    }
                if (!allowwrite && lines[i2].trim().equals("")) {                                   //.trim() -> toglie i spazi
                        String header;
                        allowwrite = true;
                        if (replace) {
                            header = "Manifest-Version: 1.0\r\nCreated-by: " + this.version + " (Android " + this.appname + ")\r\n";
                        } else {
                            header = "Signature-Version: 1.0\r\nCreated-by: " + this.version + " (Android " + this.appname + ")\r\nSHA1-Digest-Manifest: " + sha1digest + "\r\n";
                        }
                        buf.append(header);
                    } else if (allowwrite) {
                        buf.append(lines[i2]);
                    }
                }
                if (allowwrite) {
                    buf.write(13);                                      //Scrive un solo carattere
                    buf.newLine();                                      //Scrive '\n'
                }
                i2++;
            }
            buf.close();
        } catch (Exception e3) {
            Log.e("a", "excep2");
            e3.printStackTrace();
        }
    }

    private String[] Strings(byte[] data) {
        int i;
        int lines = 0;
        for (byte b : data) {
            if (b == (byte) 10) {
                lines++;
            }
        }
        String[] ret = new String[lines];
        int str = 0;
        for (i = 0; i < data.length; i++) {
            if (data[i] == (byte) 10) {
                str++;
            } else if (data[i] != (byte) 13) {
                ret[str] = ret[str] + ((char) data[i]);
            }
        }
        return ret;
    }
}
