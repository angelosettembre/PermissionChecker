package com.example.angiopasqui.permissionchecker;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Angiopasqui on 18/09/2017.
 */

public class CustomAdapter extends ArrayAdapter<App> {
    private int resource;
    private LayoutInflater inflater;

    public CustomAdapter(Context context, int resourceId, List<App> objects) {
        super(context, resourceId, objects);
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
        ImageView go;

        nameApp = (TextView) v.findViewById(R.id.appName);
        iconApp = (ImageView) v.findViewById(R.id.appIcon);
        go = (ImageView) v.findViewById(R.id.toAppInfo);

        nameApp.setText(a.getName());
        iconApp.setImageDrawable(a.getIcon());
        int id = getContext().getResources().getIdentifier("play_icon.png", "drawable", getContext().getPackageName());
        go.setImageResource(id);

        return v;
    }
}
