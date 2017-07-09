package com.ldz.fpt.businesscardscannerandroid.activity.ocr_language_activity;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

/**
 * Created by linhdq on 6/6/17.
 */

public class DownloadBroadCastReceiver extends BroadcastReceiver {
    private static final String TAG = DownloadBroadCastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
            Log.d(TAG, "onReceive: receive mess");
            long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
            DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(downloadId);
            Cursor c = dm.query(query);
            if (c == null) {
                return;
            }
            if (c.moveToFirst()) {
                int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                int status = c.getInt(columnIndex);
                columnIndex = c.getColumnIndex(DownloadManager.COLUMN_TITLE);
                String title = c.getString(columnIndex);
                columnIndex = c.getColumnIndex(DownloadManager.COLUMN_URI);
                String name = c.getString(columnIndex);

                if (DownloadManager.STATUS_SUCCESSFUL == status) {
                    //start service to extract language file
                    Intent serviceIntent = new Intent(context, OCRLanguageInstallService.class);
                    serviceIntent.putExtra(DownloadManager.EXTRA_DOWNLOAD_ID, downloadId);
                    serviceIntent.putExtra(OCRLanguageInstallService.EXTRA_FILE_NAME, name);
                    context.startService(serviceIntent);
                } else if (DownloadManager.STATUS_FAILED == status) {
                    Intent resultIntent = new Intent(OCRLanguageInstallService.ACTION_INSTALL_FAILED);
                    resultIntent.putExtra(OCRLanguageInstallService.EXTRA_STATUS, status);
                    resultIntent.putExtra(OCRLanguageInstallService.EXTRA_OCR_LANGUAGE_DISPLAY, title);
                    context.sendBroadcast(resultIntent);
                }
            }
            c.close();
        }
    }
}
