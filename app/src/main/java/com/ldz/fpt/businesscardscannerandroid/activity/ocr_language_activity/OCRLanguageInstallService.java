package com.ldz.fpt.businesscardscannerandroid.activity.ocr_language_activity;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.annotation.Nullable;

import com.ldz.fpt.businesscardscannerandroid.database.db_context.DatabaseHandler;
import com.ldz.fpt.businesscardscannerandroid.utils.Util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by linhdq on 6/6/17.
 */

public class OCRLanguageInstallService extends IntentService {

    public static final String ACTION_INSTALL_COMPLETED = "com.ldz.fpt.businesscardscannerandroid.ACTION_OCR_LANGUAGE_INSTALLED";
    public static final String ACTION_INSTALL_FAILED = "com.ldz.fpt.businesscardscannerandroid.ACTION_INSTALL_FAILED";
    public static final String EXTRA_OCR_LANGUAGE = "ocr_language";
    public static final String EXTRA_OCR_LANGUAGE_DISPLAY = "ocr_language_display";

    public static final String EXTRA_STATUS = "status";
    public static final String EXTRA_FILE_NAME = "file_name";
    public static final String LOG_TAG = OCRLanguageInstallService.class.getSimpleName();

    //database
    private DatabaseHandler databaseHandler;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public OCRLanguageInstallService(String name) {
        super(name);
    }

    public OCRLanguageInstallService() {
        super(LOG_TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        //database
        databaseHandler = new DatabaseHandler(getApplicationContext());
        //
        if (intent == null || !intent.hasExtra(DownloadManager.EXTRA_DOWNLOAD_ID)) {
            return;
        }
        final long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
        if (downloadId != 0) {
            DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            ParcelFileDescriptor file;
            BufferedInputStream in = null;
            FileInputStream fin = null;
            String langName = null;
            try {
                String fileName = intent.getStringExtra(EXTRA_FILE_NAME);
                final Uri fileUri = Uri.parse(fileName);
                langName = extractLanguageNameFromUri(fileUri);
                File tessDir = Util.getTrainingDataDir(this);
                if (tessDir.mkdirs() || tessDir.isDirectory() && langName != null) {

                    file = dm.openDownloadedFile(downloadId);
                    fin = new FileInputStream(file.getFileDescriptor());
                    in = new BufferedInputStream(fin);

                    copyInputStream(in, fileUri.getLastPathSegment(), tessDir);

                    notifySuccess(langName);
                } else {
                    notifyError(langName);
                }
                OcrLanguage language = databaseHandler.getOCRLanguageByCode(langName);
                language.setDownloadId(0);
                language.setDownloading(false);
                databaseHandler.updateOCRLanguageFull(language);
            } catch (IOException e) {
                e.printStackTrace();
                notifyError(langName);
            } finally {
                dm.remove(downloadId);
                closeInputStream(in);
                closeInputStream(fin);
            }

        }
    }

    private void closeInputStream(InputStream inputStream) {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void copyInputStream(InputStream inputStream, String fileName, File tessDir) throws IOException {
        final byte[] buffer = new byte[4096 * 8];

        File trainedData = new File(tessDir, fileName);
        FileOutputStream out = new FileOutputStream(trainedData);
        int n;
        while (-1 != (n = inputStream.read(buffer))) {
            out.write(buffer, 0, n);
        }
        out.close();
    }

    private String extractLanguageNameFromUri(final Uri fileName) {
        final String lastPathSegment = fileName.getLastPathSegment();

        int index = lastPathSegment.indexOf(".traineddata");
        if (index != -1) {
            return lastPathSegment.substring(0, index);
        }

        index = lastPathSegment.indexOf(".cube");
        if (index != -1) {
            return lastPathSegment.substring(0, index);
        }
        index = lastPathSegment.indexOf(".tesseract_cube.nn");
        if (index != -1) {
            return lastPathSegment.substring(0, index);
        }

        return null;
    }

    private void notifyError(String lang) {
        Intent resultIntent = new Intent(ACTION_INSTALL_FAILED);
        resultIntent.putExtra(EXTRA_OCR_LANGUAGE, lang);
        resultIntent.putExtra(EXTRA_STATUS, DownloadManager.STATUS_FAILED);
        sendBroadcast(resultIntent);
    }

    private void notifySuccess(String lang) {
        Intent resultIntent = new Intent(ACTION_INSTALL_COMPLETED);
        resultIntent.putExtra(EXTRA_OCR_LANGUAGE, lang);
        resultIntent.putExtra(EXTRA_STATUS, DownloadManager.STATUS_SUCCESSFUL);
        sendBroadcast(resultIntent);
    }
}
