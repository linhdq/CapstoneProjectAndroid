package com.ldz.fpt.businesscardscannerandroid.blurdetection;

import com.googlecode.leptonica.android.Box;
import com.googlecode.leptonica.android.Pix;

public class BlurDetectionResult {

    public enum Blurriness{
        NOT_BLURRED, MEDIUM_BLUR, STRONG_BLUR;
    }

    private final Pix mPixBlur;
    private final double mBlurValue;
    private final Box mMostBlurredRegion;




    public BlurDetectionResult(long blurPixPointer, double mBlurValue, long blurRegionPointer) {
        this.mPixBlur = new Pix(blurPixPointer);
        this.mBlurValue = mBlurValue;
        this.mMostBlurredRegion = new Box(blurRegionPointer);
    }

    /**
     * Pix with overlay showing the extend of blurriness.
     */
    public Pix getPixBlur() {
        return mPixBlur;
    }

    /**
     * Value indicating overall pix blurriness. 0->very blurred, anything greater 0.35 is
     * sharp.
     */
    public double getBlurValue() {
        return mBlurValue;
    }

    public Blurriness getBlurriness() {
        if(mBlurValue<0.5){
            return Blurriness.NOT_BLURRED;
        } else if(mBlurValue<0.67) {
            return Blurriness.MEDIUM_BLUR;
        } else {
            return Blurriness.STRONG_BLUR;
        }
    }

    /**
     * Bounding box of the most blurry region.
     */
    public Box getMostBlurredRegion() {
        return mMostBlurredRegion;
    }
}
