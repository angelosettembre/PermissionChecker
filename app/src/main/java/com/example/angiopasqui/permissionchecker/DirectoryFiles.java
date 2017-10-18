package com.example.angiopasqui.permissionchecker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DirectoryFiles {
    private String dir;

    public DirectoryFiles(String dir) {
        this.dir = dir;
    }

    private List<String> ReadDir(String dir) {
        List<String> files = new ArrayList();
        try {
            for (File ff : new File(dir).listFiles()) {
                    if (ff.isDirectory()) {                                  //Se il file i-esimo è una directory
                    files.addAll(ReadDir(ff.getAbsolutePath()));             //Aggiungi la lista dei path assouluti [es:"sdcard/android/...."]
                } else {
                    files.add(ff.getAbsolutePath());                        //Aggiungi il path assouluto ex."sdcard/android/...."
                }
            }
        } catch (Exception e) {
        }
        return files;
    }

    public String[] GetOnlyFiles() {
        List<String> files = ReadDir(this.dir);                 //Lista path
        String[] ret = new String[files.size()];
        files.toArray(ret);
        return ret;
    }
}
