package com.example.angiopasqui.permissionchecker;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Unzip {
    public Boolean Unzip(String src, String dest) {
        return Unzip(src, dest, "");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Boolean Unzip(String src, String dest, String singlefile) {
        FileNotFoundException e;
        try {
            ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(new FileInputStream(src)));              //ZipInputStream implementa un filtro di flusso di ingresso per leggere i file nel formato di file ZIP

            /*
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry())!= null){
                String ase = entry.getName();
                System.out.println("ECCOLLIIIIII::::::____ "+ase);
            }
             */

            BufferedOutputStream bufferedOutputStream = null;                                                                   //implementa un flusso di output bufferizzato
            while (true) {
                BufferedOutputStream bufferedOutputStream2;
                try {
                    ZipEntry zipEntry = zipInputStream.getNextEntry();                                                          //Questa classe viene usata per rappresentare una voce di file ZIP; getNextEntry() -> Legge la prossima registrazione di file ZIP e posiziona il flusso all'inizio dei dati di inserimento
                    if (zipEntry == null) {
                        break;
                    }
                    String zipEntryName = zipEntry.getName();                                                                   //getName() -> Restituisce il nome della voce
                    //System.out.println("Fillllleeeeeee: 222222 "+zipEntryName);
                    File file = new File(new StringBuilder(String.valueOf(dest)).append(zipEntryName).toString());              //CREAZIONE FILE
                    if (singlefile.equals("") || singlefile.equals(zipEntryName)) {
                        //System.out.println("TROVATOOOOOOOOO 2 22 2 2 22 ");
                        if (zipEntry.isDirectory()) {                                                                           //Se è una directory
                            file.mkdirs();                                                                                      //CREAZIONE DIRECTORY
                            bufferedOutputStream2 = bufferedOutputStream;
                        } else {
                            file.mkdirs();
                            file.delete();
                            byte[] buffer = new byte[4096];
                            bufferedOutputStream2 = new BufferedOutputStream(new FileOutputStream(file), 4096);
                            //System.out.println("TROVATOOOOOOOOO 6666777887 "+file.getName());
                            while (true) {
                                int count = zipInputStream.read(buffer, 0, 4096);                       //read(byte[] b, int off, int len) -> Legge l'attuale entrata ZIP in una matrice di byte. Se len non è zero, il metodo blocca fino a che non sia disponibile alcun input; altrimenti, nessun byte viene letto e 0 restituito
                                //Ritorna il numero effettivo di byte; altrimenti -1 se viene raggiunta la fine della voce
                                if (count == -1) {                                                      //Se viene raggiunta la fine della voce
                                    break;
                                }
                                bufferedOutputStream2.write(buffer, 0, count);                          //Scrive i count byte dall'allineamento di byte specificato partendo dall'offset  0 a questo flusso di output bufferato
                            }
                            bufferedOutputStream2.flush();
                            bufferedOutputStream2.close();
                        }
                        if (singlefile.equals("")) {
                            bufferedOutputStream = bufferedOutputStream2;
                        }
                        //break;
                    }
                } catch (FileNotFoundException e2) {
                    e = e2;
                    bufferedOutputStream2 = bufferedOutputStream;
                } catch (IOException e3) {
                    bufferedOutputStream2 = bufferedOutputStream;
                }
            }
            zipInputStream.close();
            return Boolean.valueOf(true);
        } catch (FileNotFoundException e4) {
            e = e4;
            return Boolean.valueOf(false);
        } catch (IOException e5) {
            return Boolean.valueOf(false);
        }
    }
}