package com.example.angiopasqui.permissionchecker;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileSHA1 {
    public String SHA1(String filename) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA1");            //Questa classe MessageDigest fornisce alle applicazioni la funzionalità di un algoritmo di digestazione dei messaggi, ad esempio SHA-1 o SHA-256
                                                                         //getInstance("") -> Restituisce un oggetto MessageDigest che implementa l'algoritmo di digest specificato.
        try {
            byte[] data = new RWFile().ReadFile(filename);               //Ritorna array di byte con lunghezza "length"
            md.update(data, 0, data.length);                             //Aggiorna il digest utilizzando l'array specificato di byte, a partire dall'offset specificato
            return Base64.encodeBytes(md.digest());                      //md.digest() -> Completa il calcolo hash eseguendo operazioni definitive come il padding. Il digest viene ripristinato dopo la chiamata
        } catch (Exception e) {
            return "";
        }
    }
}
