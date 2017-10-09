package com.example.angiopasqui.permissionchecker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.graphics.drawable.Drawable;
import android.os.Debug;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Angiopasqui on 18/09/2017.
 */

public class CustomAdapter extends ArrayAdapter<App> {
    private int resource;
    private LayoutInflater inflater;
    private PackageManager packageManager;
    private PackageInfo pi;
    private PermissionInfo pemInfo;
    int numPermsissions;

    public CustomAdapter(Context context, int resourceId, List<App> objects, PackageManager pm) {
        super(context, resourceId, objects);
        packageManager = pm;
        resource = resourceId;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        if (v == null) {
            Log.d("DEBUG", "Inflating view");
            v = inflater.inflate(R.layout.app_list_item, null);
        }

        App app = getItem(position);
        TextView nameApp;
        ImageView iconApp;
        TextView numtextPermission;

        nameApp = (TextView) v.findViewById(R.id.appName);
        iconApp = (ImageView) v.findViewById(R.id.appIcon);
        numtextPermission = (TextView) v.findViewById(R.id.numPermissions);

        try {
            numPermsissions = countPermissions(app.getPackageName());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        nameApp.setText(app.getName());
        iconApp.setImageDrawable(app.getIcon());
        numtextPermission.setText(Integer.toString(numPermsissions));
        numPermsissions = 0;

        return v;
    }

    public int countPermissions(String namePack) throws PackageManager.NameNotFoundException {
        int nPerm = 0;
        pi = packageManager.getPackageInfo(namePack, PackageManager.GET_PERMISSIONS);
        String[] requestedPermissions = pi.requestedPermissions;

        if (requestedPermissions != null) {
            for (int j = 0; j < requestedPermissions.length; j++) {
                try {
                    Log.d("test", requestedPermissions[j]);
                    pemInfo = packageManager.getPermissionInfo(requestedPermissions[j], 0);
                    if (pemInfo.loadDescription(packageManager) == null) {
                        //Do nothing
                        System.out.println("CI ARRIVIII!!! 22");
                    } else {
                        if (pemInfo.name.contains("android.permission") || pemInfo.name.contains("com.google") || pemInfo.name.contains("com.android.launcher"))
                            nPerm++;
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    continue;
                }
            }
        }
        return nPerm;
    }

}
