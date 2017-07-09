package com.ldz.fpt.businesscardscannerandroid.utils;

import android.os.AsyncTask;

import com.googlecode.leptonica.android.Pix;
import com.ldz.fpt.businesscardscannerandroid.activity.ocr_activity.OCR;
import com.ldz.fpt.businesscardscannerandroid.utils.Util;

import java.io.File;
import java.io.IOException;

public class SavePixTask extends AsyncTask<Void, Void, File> {
    private final Pix mPix;
    private final File mDir;

    public SavePixTask(Pix pix, File dir) {
        mPix = pix;
        mDir = dir;
    }

    @Override
    protected File doInBackground(Void... params) {
        try {
            return Util.savePixToDir(mPix, OCR.ORIGINAL_PIX_NAME, mDir);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mPix.recycle();
        }

        return null;
    }

}
