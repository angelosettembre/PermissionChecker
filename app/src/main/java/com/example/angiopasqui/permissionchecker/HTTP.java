package com.example.angiopasqui.permissionchecker;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class HTTP {
    public String Get(String urlstring) {
        String ret = "";
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) new URL(urlstring).openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            byte[] buffer = new byte[1024];
            while (true) {
                int len1 = in.read(buffer);
                if (len1 == -1) {
                    in.close();
                    urlConnection.disconnect();
                    return ret;
                }
                for (int i = 0; i < len1; i++) {
                    ret = new StringBuilder(String.valueOf(ret)).append((char) buffer[i]).toString();
                }
            }
        } catch (Exception e) {
            return "exception";
        } catch (Throwable th) {
            urlConnection.disconnect();
        }
        return ret;
    }

    public boolean Get(String[] urlstrings, String destfile) {                                              //destfile = chiave privata di SSL.java
        HttpURLConnection urlConnection = null;
        int i = 0;
        while (i < urlstrings.length) {
            try {
                urlConnection = (HttpURLConnection) new URL(urlstrings[i]).openConnection();                //Creazione un oggetto URL dalla stringa i-esima
                                                                                                            //openConnection() -> Restituisce URLConnection , un'istanza che rappresenta una connessione all'oggetto remoto di cui si riferisce URL
                System.out.println("CI ARRIVIIIIIIIIII::::::");

                System.out.println("CI ARRIVIIIIIIIIII:::::: 222 2 2 2"+urlConnection.getURL());
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());                   //getInputStream() -> Restituisce un flusso di input che legge da questa connessione aperta
                System.out.println("CI ARRIVIIIIIIIIII:::::: 33333"+urlConnection.getURL());
                RandomAccessFile fw = new RandomAccessFile(new File(destfile), "rw");

                byte[] buffer = new byte[1024];
                while (true) {
                    int len1 = in.read(buffer);
                    if (len1 == -1) {
                        in.close();
                        fw.close();
                        urlConnection.disconnect();
                        urlConnection.disconnect();
                        return true;
                    } else if (len1 > 0) {                                                          //Se la read ritorna il prossimo byte dei dati
                        fw.write(buffer, 0, len1);
                    }
                }
            } catch (Exception e) {
                urlConnection.disconnect();
            } catch (Throwable th) {
                urlConnection.disconnect();
            }
            i++;
        }
        return false;
    }
}
