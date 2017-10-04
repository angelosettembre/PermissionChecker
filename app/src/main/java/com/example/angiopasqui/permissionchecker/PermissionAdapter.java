package com.example.angiopasqui.permissionchecker;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Angiopasqui on 04/10/2017.
 */

public class PermissionAdapter extends ArrayAdapter<Permesso> {
    private LayoutInflater inflater;
    private int resource;

    public PermissionAdapter(Context context, int resourceId, List<Permesso> objects) {
        super(context, resourceId, objects);
        resource = resourceId;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        if (v == null) {
            Log.d("DEBUG","Inflating view");
            v = inflater.inflate(R.layout.app_list_descrption_detail_item, null);
        }

        Permesso perm = getItem(position);
        TextView namePermission;
        TextView permissionDescription;

        namePermission = (TextView) v.findViewById(R.id.permissionName);
        permissionDescription = (TextView) v.findViewById(R.id.permissionDescription);

        namePermission.setText(perm.getName());
        permissionDescription.setText(perm.getDescription());

        return v;
    }
}
