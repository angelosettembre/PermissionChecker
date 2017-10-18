package com.example.angiopasqui.permissionchecker;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Compress {
    private static final int BUFFER = 2048;
    private String _baseDir;
    private String[] _files;
    private String _zipFile;

    public Compress(String baseDir, String[] files, String zipFile) {
        this._files = files;
        this._zipFile = zipFile;
        this._baseDir = baseDir;
    }

    public void zip() {
        Exception e;
        try {
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(this._zipFile)));           //Questa classe implementa un filtro di flusso di output per la scrittura di file nel formato di file ZIP
                                                                                                                                //"fileName" da SetupPermissions.java com zip
            byte[] data = new byte[BUFFER];
            int i = 0;
            BufferedInputStream origin = null;
            BufferedInputStream origin2;
            while (i < this._files.length) {
                try {
                    origin2 = new BufferedInputStream(new FileInputStream(this._files[i]), BUFFER);                     //Lista dei path in input
                    out.putNextEntry(new ZipEntry(this._files[i].substring(this._baseDir.length())));                   //putNextEntry() -> Inizia la scrittura di una nuova voce di file ZIP e posiziona il flusso all'inizio dei dati di inserimento.
                    while (true) {
                        int count = origin2.read(data, 0, BUFFER);                                                      //read() -> Legge i byte di questo flusso di byte in ingresso nell'array di byte specificato, a partire dall'offset dato
                        if (count == -1) {
                            break;
                        }
                        out.write(data, 0, count);                                                                      //Scrive una array di byte ai dati dell'entrata ZIP corrente
                    }
                    origin2.close();
                    i++;
                    origin = origin2;
                } catch (Exception e2) {
                    e = e2;
                    origin2 = origin;
                }
            }
            out.close();
            return;
        } catch (Exception e3) {
            e = e3;
        }
        e.printStackTrace();
    }
}
