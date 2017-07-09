package com.ldz.fpt.businesscardscannerandroid.activity.crop_image_activity;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.WriteFile;
import com.ldz.fpt.businesscardscannerandroid.blurdetection.Blur;
import com.ldz.fpt.businesscardscannerandroid.blurdetection.BlurDetectionResult;

public class LoadPixForCropTask extends AsyncTask<Void, Void, CropData> {
    private static final String TAG = LoadPixForCropTask.class.getName();
    //
    private final Pix pix;
    private final int width;
    private final int height;

    public LoadPixForCropTask(Pix pix, int width, int height) {
        this.pix = pix;
        this.width = width;
        this.height = height;
    }

    @Override
    protected void onCancelled(CropData cropData) {
        super.onCancelled(cropData);
        if (cropData != null) {
            cropData.recylce();
        }
    }

    @Override
    protected void onPostExecute(CropData cropData) {
        super.onPostExecute(cropData);
        de.greenrobot.event.EventBus.getDefault().post(cropData);
    }

    @Override
    protected CropData doInBackground(Void... params) {
        BlurDetectionResult blurDetectionResult = Blur.blurDetect(pix);
        CropImageScaler scaler = new CropImageScaler();
        CropImageScaler.ScaleResult scaleResult;
        // scale it so that it fits the screen
        if (isCancelled()) {
            return null;
        }
        scaleResult = scaler.scale(pix.clone(), width, height);
        if (isCancelled()) {
            return null;
        }
        Bitmap bitmap = WriteFile.writeBitmap(scaleResult.getPix());
        if (isCancelled()) {
            return null;
        }
        pix.recycle();
        return new CropData(bitmap, scaleResult, blurDetectionResult);
    }
}