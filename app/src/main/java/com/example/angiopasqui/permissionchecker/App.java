package com.example.angiopasqui.permissionchecker;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

/**
 * Created by Angiopasqui on 18/09/2017.
 */

public class App implements Serializable {
    private String name;
    private String packageName;
    private Drawable icon;

    public App(){

    }

    public App(String name, Drawable icon, String packageName){
        this.name = name;
        this.icon = icon;
        this.packageName = packageName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getName() {
        return name;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }
}
