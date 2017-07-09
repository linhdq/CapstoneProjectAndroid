package com.ldz.fpt.businesscardscannerandroid.utils.image_util;

import com.googlecode.leptonica.android.Pix;

/**
 * Created by linhdq on 6/15/17.
 */

public class ImageLoaderResult {
    private Pix pix;
    private PixLoaderStatus status;

    public ImageLoaderResult(Pix pix) {
        this.pix = pix;
        this.status = PixLoaderStatus.SUCCESS;
    }

    public ImageLoaderResult(PixLoaderStatus status) {
        this.status = status;
        this.pix = null;
    }

    public Pix getPix() {
        return pix;
    }

    public void setPix(Pix pix) {
        this.pix = pix;
    }

    public PixLoaderStatus getStatus() {
        return status;
    }

    public void setStatus(PixLoaderStatus status) {
        this.status = status;
    }
}
