package com.example.angiopasqui.permissionchecker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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

import java.util.List;

/**
 * Created by Angiopasqui on 18/09/2017.
 */

public class CustomAdapter extends ArrayAdapter<App> {
    private int resource;
    private LayoutInflater inflater;
    private PackageManager packageManager;
    private PackageInfo pi;
    static int numPermsissions;

    public CustomAdapter(Context context, int resourceId, List<App> objects, PackageManager pm) {
        super(context, resourceId, objects);
        packageManager = pm;
        resource = resourceId;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        if (v == null) {
            Log.d("DEBUG","Inflating view");
            v = inflater.inflate(R.layout.app_list_item, null);
        }

        App a = getItem(position);
        TextView nameApp;
        ImageView iconApp;
        TextView numtextPermission;
        ImageView go;

        nameApp = (TextView) v.findViewById(R.id.appName);
        iconApp = (ImageView) v.findViewById(R.id.appIcon);
        numtextPermission =(TextView) v.findViewById(R.id.numPermissions);

        try {
            pi = packageManager.getPackageInfo(a.getPackageName(),PackageManager.GET_PERMISSIONS);
            countPermissions();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        //go = (ImageView) v.findViewById(R.id.toAppInfo);

        nameApp.setText(a.getName());
        iconApp.setImageDrawable(a.getIcon());
        numtextPermission.setText(Integer.toString(numPermsissions));
        numPermsissions=0;
        //go.setBackgroundResource(R.drawable.go);

        return v;
    }

    public void countPermissions(){
        String[] requestedPermissions = pi.requestedPermissions;
        if(requestedPermissions!=null)
            numPermsissions = requestedPermissions.length;
    }

}
