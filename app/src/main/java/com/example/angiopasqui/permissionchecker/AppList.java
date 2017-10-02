package com.example.angiopasqui.permissionchecker;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Angiopasqui on 29/09/2017.
 */

public class AppList extends Activity {
    public ListView listView;
    CustomAdapter customAdapter;
    App app;
    String packageName;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_list);
        PackageManager pm = getPackageManager();
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);


        listView = (ListView) findViewById(R.id.listApp);

        customAdapter = new CustomAdapter(this, R.layout.app_list_item, new ArrayList<App>(),pm);

        listView.setAdapter(customAdapter);

        List<ApplicationInfo> apps = pm.getInstalledApplications(0);

        //GET APPS
        for (ApplicationInfo applicationInfo : apps)
        {
            if((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 1) {
                app = new App();
                app.setName((String) pm.getApplicationLabel(applicationInfo));
                Log.d("DEBUG", "NOME APP" + app.getName());
                app.setIcon(pm.getApplicationIcon(applicationInfo));
                app.setPackageName(applicationInfo.packageName);
                customAdapter.add(app);
            }
        }

        /*List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo info : packages) {
            Log.i("Info", "Installed package:" + info.packageName);
        }*/

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                App a = customAdapter.getItem(position);
                Intent i = new Intent(getApplicationContext(), AppDetails.class);
                i.putExtra("Nome app", a.getName());
                Log.d("DEBUG","COSSSSAA"+a.getName());
                Drawable s = a.getIcon();
                Bitmap bitmap = ((BitmapDrawable)s).getBitmap();
                i.putExtra("Icona app", bitmap);
                Log.d("DEBUG","SDASDS"+bitmap);
                i.putExtra("PACKAGE",a.getPackageName());
                Log.d("DEBUG","Pacchetto"+a.getPackageName());
                startActivity(i);
            }
        });
    }

}
