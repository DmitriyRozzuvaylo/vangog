package com.vangog.barcodereader;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class Splash extends Activity {
    private static final int PERMISSION_ALL = 0;
    private Handler h;
    private Runnable r;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        h = new Handler();
        r = new Runnable() {
            @Override
            public void run() {
                Toast.makeText(Splash.this, "Runnable started", Toast.LENGTH_SHORT).show();

                startActivity(new Intent(Splash.this, BarcodeReader.class));
                finish();
            }
        };

        String[] PERMISSIONS = {
                Manifest.permission.CALL_PHONE,
                Manifest.permission.CAMERA,
                Manifest.permission.INTERNET
                // TODO: add extra permission
        };

        if(!UtilPermissions.hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
        else
            h.postDelayed(r, 1500);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        int index = 0;
        Map<String, Integer> PermissionsMap = new HashMap<String, Integer>();
        for (String permission : permissions){
            PermissionsMap.put(permission, grantResults[index]);
            index++;
        }

        if((PermissionsMap.get(Manifest.permission.CAMERA) != 0)
                || PermissionsMap.get(Manifest.permission.INTERNET) != 0){
            Toast.makeText(this, "Camera and Inernet permissions are a must", Toast.LENGTH_SHORT).show();
            finish();
        } else  {
            h.postDelayed(r, 1000);
        }
    }
}
