package com.example.angiopasqui.permissionchecker.privacyLeaks;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.angiopasqui.permissionchecker.App;
import com.example.angiopasqui.permissionchecker.R;

import java.util.List;

/**
 * Created by passet on 30/10/2017.
 */

public class AppLeaksAdapter extends ArrayAdapter<AppLeak> {
    private LayoutInflater inflater;
    private int resource;


    public AppLeaksAdapter(Context context, int resourceId) {
        super(context, resourceId);
        resource = resourceId;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        if (v == null) {
            Log.d("DEBUG", "Inflating view");
            v = inflater.inflate(R.layout.listappleaks_detail_item, null);
        }

        AppLeak app = getItem(position);
        TextView nameApp;
        ImageView iconApp;
        TextView hostname;

        nameApp = (TextView) v.findViewById(R.id.nameApp);
        iconApp = (ImageView) v.findViewById(R.id.iconApp);
        hostname = (TextView) v.findViewById(R.id.hostname);


        nameApp.setText(app.getAppName());
        iconApp.setImageDrawable(app.getIcon());
        hostname.setText(app.getHostname());

        return v;
    }
}
