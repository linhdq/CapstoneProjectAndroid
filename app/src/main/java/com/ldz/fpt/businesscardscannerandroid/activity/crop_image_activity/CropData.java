package com.ldz.fpt.businesscardscannerandroid.activity.crop_image_activity;

import android.graphics.Bitmap;

import com.ldz.fpt.businesscardscannerandroid.blurdetection.BlurDetectionResult;

/**
 * Created by linhdq on 6/17/17.
 */

public class CropData {
    private final Bitmap bitmap;
    private final CropImageScaler.ScaleResult scaleResult;
    private final BlurDetectionResult blurDetectionResult;

    CropData(Bitmap bitmap, CropImageScaler.ScaleResult scaleFactor, BlurDetectionResult blurDetectionResult) {
        this.bitmap = bitmap;
        this.scaleResult = scaleFactor;
        this.blurDetectionResult = blurDetectionResult;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public CropImageScaler.ScaleResult getScaleResult() {
        return scaleResult;
    }

    public BlurDetectionResult getBlurDetectionResult() {
        return blurDetectionResult;
    }

    public void recylce() {
        if (scaleResult != null) {
            scaleResult.getPix().recycle();
        }
        if (blurDetectionResult != null) {
            blurDetectionResult.getPixBlur().recycle();
        }
    }
}
