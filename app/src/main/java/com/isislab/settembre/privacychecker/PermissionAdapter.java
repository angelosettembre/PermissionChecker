package com.isislab.settembre.privacychecker;

import android.content.pm.PermissionInfo;
import android.widget.ArrayAdapter;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Angiopasqui on 05/10/2017.
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
            v = inflater.inflate(R.layout.app_list_detail_item, null);
        }

        Permesso perm = getItem(position);
        ImageView iconPermission;
        TextView namePermission;
        TextView permissionDescription;
        ImageView checkPermission;
        LinearLayout containerDeny;
        ImageView denyButton;
        TextView denyText;
        LinearLayout containerAllow;
        ImageView allowButton;
        TextView allowText;

        iconPermission = (ImageView) v.findViewById(R.id.iconPermssion);
        namePermission = (TextView) v.findViewById(R.id.namePermission);
        permissionDescription = (TextView) v.findViewById(R.id.descriptionPermission);
        checkPermission = (ImageView) v.findViewById(R.id.checkGRANT_DENIED);
        containerDeny = (LinearLayout) v.findViewById(R.id.containerDeny);
        denyButton = (ImageView) v.findViewById(R.id.denyButton);
        containerAllow = (LinearLayout) v.findViewById(R.id.containerAllow);
        allowButton = (ImageView) v.findViewById(R.id.allowButton);
        denyText = (TextView) v.findViewById(R.id.denyText);
        allowText = (TextView) v.findViewById(R.id.allowText);

        iconPermission.setImageDrawable(perm.getIcon());
        namePermission.setText(perm.getName());
        permissionDescription.setText(perm.getDescription());

        checkPermission.setImageDrawable(perm.getCheckPermission());

        if(perm.getContainerVisible() == View.VISIBLE){
            containerDeny.setVisibility(View.VISIBLE);
            if(perm.getProtectionLevel() == PermissionInfo.PROTECTION_NORMAL){
                denyText.setText("ELIMINA");
                denyButton.setImageResource(R.drawable.icon_error);
            }
            else{
                denyText.setText("NEGA IN \nIMPOSTAZIONI");
                denyButton.setImageResource(R.drawable.deny);
            }
            containerAllow.setVisibility(View.INVISIBLE);
        } else {
            containerDeny.setVisibility(View.INVISIBLE);
            if(perm.getProtectionLevel() == PermissionInfo.PROTECTION_NORMAL){
                containerAllow.setVisibility(View.INVISIBLE);
            }
            else {
                containerAllow.setVisibility(View.VISIBLE);
            }
        }

        denyButton.setTag(position);
        allowButton.setTag(position);
        denyText.setTag(position);
        allowText.setTag(position);

        return v;
    }
}
