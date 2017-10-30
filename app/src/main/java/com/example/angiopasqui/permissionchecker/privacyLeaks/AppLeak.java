package com.example.angiopasqui.permissionchecker.privacyLeaks;

import android.graphics.drawable.Drawable;

/**
 * Created by passet on 30/10/2017.
 */

public class AppLeak {
    private String appName;
    private Drawable icon;
    private String hostname;

    public AppLeak() {
    }

    public AppLeak(String appName, Drawable icon, String hostname) {
        this.appName = appName;
        this.icon = icon;
        this.hostname = hostname;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
}
