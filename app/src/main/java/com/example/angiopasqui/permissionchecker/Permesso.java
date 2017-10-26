package com.example.angiopasqui.permissionchecker;

import android.graphics.drawable.Drawable;
import android.view.View;

import java.io.Serializable;

/**
 * Created by Angiopasqui on 05/10/2017.
 */

public class Permesso implements Serializable {

    private String name;
    private String description;
    private Drawable icon;
    private Drawable checkPermission;
    private int containerVisible;
    private int protectionLevel;

    public int len;
    String name_;
    public int ofs;
    public int stringID;
    public boolean removed = false;
    Boolean checked = Boolean.valueOf(false);

    public Permesso(){}

    public Permesso(int stringID, String name_, int offset, int len) {
        this.stringID = stringID;
        SetName(name_);
        this.ofs = offset;
        this.len = len;
    }

    public Permesso(String name, String description,Drawable icon, Drawable checkPermission) {
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.checkPermission = checkPermission;
    }

    public int getProtectionLevel() {
        return protectionLevel;
    }

    public void setProtectionLevel(int protectionLevel) {
        this.protectionLevel = protectionLevel;
    }

    public Boolean GetChecked() {
        return this.checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }

    public void SetName(String s) {
        this.name_ = s.substring(s.lastIndexOf(".") + 1);
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

    public String getName_() {
        return name_;
    }

    public void setName_(String name_) {
        this.name_ = name_;
    }

    @Override
    public String toString() {
        return "Permesso{" +
                "len=" + len +
                ", name_='" + name_ + '\'' +
                ", ofs=" + ofs +
                ", stringID=" + stringID +
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

    public int getContainerVisible() {
        return containerVisible;
    }

    public void setContainerVisible(int containerVisible) {
        this.containerVisible = containerVisible;
    }
}

