package com.ldz.fpt.businesscardscannerandroid.opencv;

/**
 * Created by linhdq on 5/23/17.
 */

public class OpenCVNative {
    public native static void convertGray(long addrRgba, long addrGray);

    public native static void increaseContrast(long addrRgba, long addrGray);

    public native static void cropMat(long matS, long matD, int x, int y, int w, int h);

    public native static int[] detectTextBlock(long addrRgba);

    public native static float[] detectCardVisit(long addrRgba);

}
