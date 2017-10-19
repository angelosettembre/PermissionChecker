package com.example.angiopasqui.permissionchecker;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

public class RWFile {
    public byte[] ReadFile(String file) throws IOException {
        return ReadFile(new File(file));
    }

    public byte[] ReadFile(File file) throws IOException {
        RandomAccessFile f = new RandomAccessFile(file, "r");                                       //APERTURA FILE CON LETTURA
        System.out.println("QWEERRRR: 4444 "+file.toString());
        try {
            long longlength = f.length();                                                           //Lunghezza del file
            int length = (int) longlength;
            System.out.println("QWEERRRR: 878 "+length);
            if (((long) length) != longlength) {
                throw new IOException("File size >= 2 GB");
            }
            byte[] data = new byte[length];
            f.readFully(data);                              //Lettura della lunghezza di questo file nell'array di byte "data"
            System.out.println("QWEERRRR: 5555 "+data.length);
            return data;                                    //Ritorna array di byte con lunghezza "length"
        } finally {
            f.close();
        }
    }

    public Boolean WriteFile(String name, byte[] data) {                                    //Name = tmp/AndroidManifest.xml
        try {
            RandomAccessFile f = new RandomAccessFile(new File(name), "rw");                //APERTURA FILE CON SCRITTURA E LETTURA
            f.write(data);                                                                  //Scrive un byte "data"
            f.close();
            return Boolean.valueOf(true);
        } catch (Exception e) {
            return Boolean.valueOf(false);
        }
    }

    public void CopyFile(String src, String dest) throws IOException {                      //Copia file (sorgente),(destinazione)
        byte[] data = new byte[2048];
        File srcfile = new File(src);
        File destfile = new File(dest);
        RandomAccessFile fr = new RandomAccessFile(srcfile, "r");                           //APERTURA FILE CON LETTURA
        RandomAccessFile fw = new RandomAccessFile(destfile, "rw");                         //APERTURA FILE CON SCRITTURA E LETTURA
        int bytes = 0;
        while (bytes >= 0) {
            try {
                bytes = fr.read(data);                                  //LETTURA STREAM DI BYTE DAL srcfile
                if (bytes > 0) {
                    fw.write(data, 0, bytes);                           //Scrive un numeno di byte "data" uguale a bytes a partire da 0
                }
            } catch (Throwable th) {
                fr.close();
                fw.close();
            }
        }
        fr.close();
        fw.close();
    }

    public String GenFilename(String name, String dir, String add) {                                                //GENERAZIONE NOME DEL FILE
        String fname = new StringBuilder(String.valueOf(dir)).append(new File(name).getName()).toString();          //fname = a "dir" concatena il nome del file
        int num = 1;
        boolean quit = false;
        String base = fname.substring(0, fname.lastIndexOf("."));                                                   //base = sottostringa da 0 a "." escluso //lastIndexOf() -> restituisce  l'indice dell'ultima occorrenza del carattere
        String ext = fname.substring(fname.lastIndexOf("."));                                                       //ext  = sottostringa da "." in poi, con "." incluso
        fname = new StringBuilder(String.valueOf(base)).append(add).append(ext).toString();                         //fname = a "base" concatena "add", "ext"
        while (!quit) {
            if (new File(fname).exists()) {                                                                         //Se fname ESISTE
                fname = new StringBuilder(String.valueOf(base)).append(add).append(" (").append(Integer.toString(num)).append(")").append(ext).toString();      //fname = a "base" concatena "add" e " (" e "num = 1" e ") " e "ext"
                num++;
            } else {
                quit = true;
            }
        }
        return new File(fname).getName();
    }

    public String Filename(String name, String add) {
        String fname = new File(name).getName();
        String base = fname.substring(0, fname.lastIndexOf("."));                                                 //base = sottostringa da 0 a "." escluso //lastIndexOf() -> restituisce  l'indice dell'ultima occorrenza del carattere
        return new StringBuilder(String.valueOf(base)).append(add).append(fname.substring(fname.lastIndexOf("."))).toString();
    }

    public void WriteFile(String filename, String text) {
        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(filename, false));                       //FileWriter = Costruisce un oggetto FileWriter dato un nome di file con un booleano che indica se aggiungere o meno i dati scritti
            buf.append(text);
            buf.close();
        } catch (Exception e) {
            Log.e("a", "excep2");
            e.printStackTrace();
        }
    }

    public boolean Exists(String name) {
        return new File(name).exists();
    }

    public void Delete(String name) {
        new File(name).delete();
    }
}
