package com.example.angiopasqui.permissionchecker;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

/**
 * Created by Angiopasqui on 05/10/2017.
 */

public class Permesso implements Serializable {

    private String name;
    private String description;
    private Drawable icon;
    private Drawable checkPermission;

    public Permesso(){}

    public Permesso(String name, String description,Drawable icon, Drawable checkPermission) {
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.checkPermission = checkPermission;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Permesso{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", icon=" + icon +
                '}';
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public Drawable getCheckPermission() {
        return checkPermission;
    }

    public void setCheckPermission(Drawable checkPermission) {
        this.checkPermission = checkPermission;
    }
}

