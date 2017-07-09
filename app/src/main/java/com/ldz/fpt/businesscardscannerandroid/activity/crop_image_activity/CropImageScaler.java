package com.ldz.fpt.businesscardscannerandroid.activity.crop_image_activity;

import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.Scale;
import com.ldz.fpt.businesscardscannerandroid.utils.Util;

public class CropImageScaler {

    public class ScaleResult {
        private final Pix mPix;
        private final float mScaleFactor;

        public ScaleResult(Pix pix, float scaleFactor) {
            mPix = pix;
            mScaleFactor = scaleFactor;
        }

        public Pix getPix() {
            return mPix;
        }

        public float getScaleFactor() {
            return mScaleFactor;
        }
    }

    private float getScaleFactorToFitScreen(Pix mPix, int vwidth, int vheight) {
        float scale;
        int dWidth = mPix.getWidth();
        int dHeight = mPix.getHeight();
        if (dWidth <= vwidth && dHeight <= vheight) {
            scale = 1.0f;
        } else {
            scale = Math.min((float) vwidth / (float) dWidth, (float) vheight / (float) dHeight);
        }
        return scale;
    }

    public ScaleResult scale(Pix pix, final int w, final int h) {

        float bestScale = 1 / getScaleFactorToFitScreen(pix, w, h);
        float scaleFactor = Util.determineScaleFactor(pix.getWidth(), pix.getHeight(), w, h);
        if (scaleFactor == 0) {
            scaleFactor = 1;
        } else {
            if (bestScale < 1 && bestScale > 0.5f) {
                scaleFactor = (float) (1 / Math.pow(2, 0.5f));
            } else if (bestScale <= 0.5f) {
                scaleFactor = (float) (1 / Math.pow(2, 0.25f));
            } else {
                scaleFactor = 1 / scaleFactor;
            }
        }
        final Pix scaled = Scale.scaleWithoutFiltering(pix, scaleFactor);
        return new ScaleResult(scaled,scaleFactor);
    }
}
