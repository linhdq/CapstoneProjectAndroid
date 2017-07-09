package com.ldz.fpt.businesscardscannerandroid.activity.ocr_language_activity;

import android.app.Dialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.ldz.fpt.businesscardscannerandroid.R;
import com.ldz.fpt.businesscardscannerandroid.activity.MonitorActivity;
import com.ldz.fpt.businesscardscannerandroid.database.db_context.DatabaseHandler;

import java.util.Iterator;
import java.util.List;

/**
 * Created by linhdq on 6/6/17.
 */

public class DownloadLanguageActivity extends MonitorActivity implements View.OnClickListener {
    private static final String TAG = DownloadLanguageActivity.class.getSimpleName();
    //view
    private ListView listView;
    private ViewSwitcher viewSwitcher;
    private Dialog confirmDialog;
    private TextView txtConfirmTitle;
    private TextView txtConfirmContent;
    private Button btnCancel;
    private Button btnDelete;
    private Dialog cancelDialog;
    private Button btnNo;
    private Button btnYes;
    private TextView txtContentCancel;
    //
    private BroadcastReceiver downloadReceiver;
    private OCRLanguageAdapter adapter;
    private BroadcastReceiver failedReceiver;
    //
    private boolean isReceiverRegistered;
    //
    private final DownloadManagerResolver downloadManagerResolver = new DownloadManagerResolver();
    private DownloadManager downloadManager;
    private OcrLanguage language;
    //database
    private DatabaseHandler databaseHandler;

    @Override
    public String getScreenName() {
        return TAG;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr_language);
        //
        getSupportActionBar().setTitle(getString(R.string.language_management));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //
        init();
        addListener();
        new LoadListLanguageTask().execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isReceiverRegistered) {
            unregisterReceiver(downloadReceiver);
            unregisterReceiver(failedReceiver);
            isReceiverRegistered = false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }

    private void init() {
        //view
        listView = (ListView) findViewById(R.id.list_ocr_languages);
        viewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcher_language_list);
        //dialog
        confirmDialog = new Dialog(this);
        confirmDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        confirmDialog.setContentView(R.layout.custom_dialog_delete_confirmation);
        confirmDialog.setCanceledOnTouchOutside(false);
        //view inside confirm dialog
        txtConfirmTitle = (TextView) confirmDialog.findViewById(R.id.txt_title);
        txtConfirmContent = (TextView) confirmDialog.findViewById(R.id.txt_content);
        btnCancel = (Button) confirmDialog.findViewById(R.id.btn_cancel);
        btnDelete = (Button) confirmDialog.findViewById(R.id.btn_delete);
        //
        cancelDialog = new Dialog(this);
        cancelDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        cancelDialog.setContentView(R.layout.custom_dialog_cancel_confirmation);
        cancelDialog.setCanceledOnTouchOutside(false);
        //
        btnNo = (Button) cancelDialog.findViewById(R.id.btn_no);
        btnYes = (Button) cancelDialog.findViewById(R.id.btn_yes);
        txtContentCancel = (TextView) cancelDialog.findViewById(R.id.txt_content_cancel);
        //
        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        //database
        databaseHandler = new DatabaseHandler(this);
    }

    private void addListener() {
        btnDelete.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        btnYes.setOnClickListener(this);
        btnNo.setOnClickListener(this);
        //
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                language = (OcrLanguage) adapter.getItem(position);
                if (language.getLanguageCode().equals("eng")) {
                    return;
                }
                if (!language.isInstalled()) {
                    Log.d(TAG, "onItemClick: language is not install");
                    if (downloadManagerResolver.resolve(DownloadLanguageActivity.this) && !language.isDownloading()) {
                        Log.d(TAG, "onItemClick: start download language");
                        startDownload(language);
                    } else if (language.isDownloading()) {
                        Log.d(TAG, "onItemClick: stop download language");
                        txtContentCancel.setText(String.format(getString(R.string.cancel_download_language), language.getDisplayText()));
                        cancelDialog.show();
                    }
                } else {
                    Log.d(TAG, "onItemClick: language is installed");
                    txtConfirmTitle.setText(String.format(getString(R.string.delete_language_title), language.getDisplayText()));
                    txtConfirmContent.setText(String.format(getString(R.string.delete_language_message), language.getSize() * 1.0f / (1024 * 1024)));
                    confirmDialog.show();
                }
                Log.d(TAG, "onItemClick: " + language.getDisplayText() + ": " + language.isDownloading());
            }
        });
    }

    private void hideArabicDownload(List<OcrLanguage> languages) {
        Iterator<OcrLanguage> it = languages.iterator();
        while (it.hasNext()) {
            final OcrLanguage lang = it.next();
            if (lang.getLanguageCode().equalsIgnoreCase("ara")) {
                it.remove();
                return;
            }
        }
    }

    private void updateLanguageListWithDownloadManagerStatus(OCRLanguageAdapter adapter) {
        if (adapter != null) {
            // find languages that are currently being downloaded
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterByStatus(DownloadManager.STATUS_RUNNING | DownloadManager.STATUS_PENDING | DownloadManager.STATUS_PAUSED);
            final DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            Cursor c = dm.query(query);
            if (c == null) {
                return;
            }
            int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_TITLE);
            while (c.moveToNext()) {
                final String title = c.getString(columnIndex);
                adapter.setDownloading(title, true);
            }
            adapter.notifyDataSetChanged();
            c.close();
        }
    }

    private void registerDownloadReceiver() {
        downloadReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String lang = intent.getStringExtra(OCRLanguageInstallService.EXTRA_OCR_LANGUAGE);
                int status = intent.getIntExtra(OCRLanguageInstallService.EXTRA_STATUS, -1);
                updateLanguageList(lang, status);
            }

        };
        failedReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String lang = intent.getStringExtra(OCRLanguageInstallService.EXTRA_OCR_LANGUAGE_DISPLAY);
                int status = intent.getIntExtra(OCRLanguageInstallService.EXTRA_STATUS, -1);
                updateLanguageListByDisplayValue(lang, status);
            }
        };
        registerReceiver(failedReceiver, new IntentFilter(OCRLanguageInstallService.ACTION_INSTALL_FAILED));
        registerReceiver(downloadReceiver, new IntentFilter(OCRLanguageInstallService.ACTION_INSTALL_COMPLETED));
        isReceiverRegistered = true;
    }

    protected void updateLanguageListByDisplayValue(String displayValue, int status) {
        for (int i = 0; i < adapter.getCount(); i++) {
            final OcrLanguage language = (OcrLanguage) adapter.getItem(i);
            if (language.getDisplayText().equalsIgnoreCase(displayValue)) {
                updateLanguage(language, status);
                return;
            }
        }
    }

    protected void updateLanguageList(String lang, int status) {
        for (int i = 0; i < adapter.getCount(); i++) {
            final OcrLanguage language = (OcrLanguage) adapter.getItem(i);
            if (language.getLanguageCode().equalsIgnoreCase(lang)) {
                updateLanguage(language, status);
                return;
            }
        }
    }

    private void updateLanguage(final OcrLanguage language, int status) {
        if (status == DownloadManager.STATUS_SUCCESSFUL) {
            final InstallStatus installStatus = OcrLanguageDataStore.isLanguageInstalled(language.getLanguageCode(), DownloadLanguageActivity.this);
            language.setInstallStatus(installStatus);
            if (installStatus.isInstalled()) {
                language.setDownloading(false);
                adapter.notifyDataSetChanged();
            }
        } else {
            language.setDownloading(false);
            adapter.notifyDataSetChanged();
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    String msg = "Failed!";
                    msg = String.format(msg, language.getDisplayText());
                    Toast.makeText(DownloadLanguageActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void startDownload(OcrLanguage language) {
        Uri uri = language.getDownloadUri();
        if (uri != null) {
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setTitle("Downloading language: " + language.getDisplayText());
            long id = downloadManager.enqueue(request);
            language.setDownloadId(id);
            language.setDownloading(true);
            adapter.notifyDataSetChanged();
            databaseHandler.updateOCRLanguageFull(language);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cancel:
                confirmDialog.dismiss();
                break;
            case R.id.btn_delete:
                confirmDialog.dismiss();
                OcrLanguageDataStore.deleteLanguage(language, DownloadLanguageActivity.this);
                adapter.notifyDataSetChanged();
                break;
            case R.id.btn_no:
                cancelDialog.dismiss();
                break;
            case R.id.btn_yes:
                cancelDialog.dismiss();
                downloadManager.remove(language.getDownloadId());
                language.setDownloading(false);
                language.setDownloadId(0);
                adapter.notifyDataSetChanged();
                databaseHandler.updateOCRLanguageFull(language);
                break;
            default:
                break;
        }
    }

    private class LoadListLanguageTask extends AsyncTask<Void, Void, OCRLanguageAdapter> {

        @Override
        protected OCRLanguageAdapter doInBackground(Void... params) {
            OCRLanguageAdapter adapter = new OCRLanguageAdapter(getApplicationContext(), false);
            List<OcrLanguage> languages = OcrLanguageDataStore.getAvailableOcrLanguages(DownloadLanguageActivity.this);
            for (OcrLanguage lang : languages) {
                if (databaseHandler.checkLanguageCodeIsExists(lang.getLanguageCode())) {
                    databaseHandler.updateOCRLanguage(lang);
                } else {
                    databaseHandler.addOCRLanguage(lang);
                }
            }
            hideArabicDownload(languages);
            adapter.refreshData();
            updateLanguageListWithDownloadManagerStatus(adapter);
            return adapter;
        }

        @Override
        protected void onPostExecute(OCRLanguageAdapter result) {
            super.onPostExecute(result);
            registerDownloadReceiver();
            adapter = result;
            listView.setAdapter(result);
            viewSwitcher.setDisplayedChild(1);
        }
    }
}
