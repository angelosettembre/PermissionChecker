package com.example.angiopasqui.permissionchecker;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

/**
 * Created by Angiopasqui on 18/09/2017.
 */

public class App implements Serializable {
    private String name;
    private String packageName;
    private int numPermissions;
    private Drawable icon;

    public App(){

    }

    public App(String name, String packageName, int numPermissions, Drawable icon) {
        this.name = name;
        this.packageName = packageName;
        this.numPermissions = numPermissions;
        this.icon = icon;
    }

    @Override
    public String toString() {
        return "App{" +
                "name='" + name + '\'' +
                ", packageName='" + packageName + '\'' +
                ", numPermissions=" + numPermissions +
                ", icon=" + icon +
                '}';
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setNumPermissions(int numPermissions) {
        this.numPermissions = numPermissions;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getName() {

        return name;
    }

    public String getPackageName() {
        return packageName;
    }

    public int getNumPermissions() {
        return numPermissions;
    }

    public Drawable getIcon() {
        return icon;
    }
}
