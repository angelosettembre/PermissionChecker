package com.isislab.settembre.privacychecker.privacyLeaks;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.isislab.settembre.privacychecker.R;

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
        TextView blocked;

        nameApp = (TextView) v.findViewById(R.id.nameApp);
        iconApp = (ImageView) v.findViewById(R.id.iconApp);
        hostname = (TextView) v.findViewById(R.id.hostname);
        blocked = (TextView) v.findViewById(R.id.blocked);

        nameApp.setText(app.getAppName());
        iconApp.setImageDrawable(app.getIcon());
        hostname.setText(app.getHostname());

        if(app.isBlocked()){
            blocked.setVisibility(View.VISIBLE);
        }
        else
            blocked.setVisibility(View.INVISIBLE);

        return v;
    }
}
