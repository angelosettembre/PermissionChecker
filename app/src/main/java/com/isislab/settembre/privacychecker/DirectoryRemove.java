package com.isislab.settembre.privacychecker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DirectoryRemove {
    private String dir;

    public DirectoryRemove(String dir) {
        this.dir = dir;
    }

    public void RemoveDir(String dir) {
        List<String> files = new ArrayList();
        File d = new File(dir);
        try {
            for (File ff : d.listFiles()) {
                if (!ff.getName().equals("..")) {
                    if (ff.isDirectory()) {
                        RemoveDir(ff.getAbsolutePath());                                    //Rimuovi il path assoluto
                    }
                    ff.delete();
                }
            }
        } catch (Exception e) {
        }
        d.delete();
    }
}
