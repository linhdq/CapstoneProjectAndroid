package com.ldz.fpt.businesscardscannerandroid.blurdetection;

import com.googlecode.leptonica.android.Pix;

public class Blur {
    static {
        System.loadLibrary("image_processing_jni");
    }


    public static BlurDetectionResult blurDetect(Pix pixs) {
        if (pixs == null) {
            throw new IllegalArgumentException("Source pix must be non-null");
        }
        return nativeBlurDetect(pixs.getNativePix());
    }


    // ***************
    // * NATIVE CODE *
    // ***************
    private static native BlurDetectionResult nativeBlurDetect(long pix);
}
