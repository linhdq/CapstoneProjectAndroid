package com.ldz.fpt.businesscardscannerandroid.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.ldz.fpt.businesscardscannerandroid.BCSApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linhdq on 6/15/17.
 */

public abstract class MonitorActivity extends AppCompatActivity {
    private static final String TAG = MonitorActivity.class.getSimpleName();

    private List<LifeCycleListener> listeners = new ArrayList<>();

    public abstract String getScreenName();

    public synchronized void addLifeCycleListener(LifeCycleListener listener) {
        if (listeners.contains(listener))
            return;
        listeners.add(listener);
    }

    public synchronized void removeLifeCycleListener(LifeCycleListener listener) {
        listeners.remove(listener);
    }


    @Override
    protected synchronized void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        for (LifeCycleListener listener : listeners) {
            listener.onActivityCreated(this);
        }
        Log.d(TAG, "onCreate: ");
    }

    @Override
    protected synchronized void onStart() {
        super.onStart();
        for (LifeCycleListener listener : listeners) {
            listener.onActivityStarted(this);
        }
        Log.d(TAG, "onStart: ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        for (LifeCycleListener listener : listeners) {
            listener.onActivityResumed(this);
        }
        ensurePermission();
        Log.d(TAG, "onResume: ");
    }

    @Override
    protected void onPause() {
        super.onPause();
        for (LifeCycleListener listener : listeners) {
            listener.onActivityPaused(this);
        }
        Log.d(TAG, "onPause: ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        for (LifeCycleListener listener : listeners) {
            listener.onActivityStopped(this);
        }
        Log.d(TAG, "onStop: ");
    }

    @Override
    protected synchronized void onDestroy() {
        super.onDestroy();
        for (LifeCycleListener listener : listeners) {
            listener.onActivityDestroyed(this);
        }
        Log.d(TAG, "onDestroy: ");
    }

    public void ensurePermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    111);
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    112);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 111:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    finish();
                }
                break;
            case 112:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    finish();
                }
                break;
            default:
                break;
        }
    }
}
