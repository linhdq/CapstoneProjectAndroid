package com.ldz.fpt.businesscardscannerandroid.utils.image_util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.ReadFile;
import com.googlecode.leptonica.android.Scale;

/**
 * Created by linhdq on 6/15/17.
 */

public class ImageLoaderAsyncTask extends AsyncTask<Void, Void, ImageLoaderResult> {
    private static final String TAG = ImageLoaderAsyncTask.class.getSimpleName();
    public static final String ACTION_IMAGE_LOADED = ImageLoaderAsyncTask.class.getName() + ".image.loaded";
    public static final String ACTION_IMAGE_LOADING_START = ImageLoaderAsyncTask.class.getName() + ".image.loading.start";

    public static final String EXTRA_PIX = "pix";
    public static final String EXTRA_STATUS = "status";
    public static final int MIN_PIXEL_COUNT = 3 * 1024 * 1024;

    private boolean skipCrop;
    private Context context;
    private Uri imageUri;

    public ImageLoaderAsyncTask(Context context, Uri imageUri, boolean skipCrop) {
        this.context = context;
        this.imageUri = imageUri;
        this.skipCrop = skipCrop;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Intent intent = new Intent(ACTION_IMAGE_LOADING_START);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Override
    protected ImageLoaderResult doInBackground(Void... voids) {
        if (isCancelled()) {
            return null;
        }
        Pix pix = ReadFile.loadWithPicasso(context, imageUri);
        if (pix == null) {
            return new ImageLoaderResult(PixLoaderStatus.IMAGE_FORMAT_UNSUPPORTED);
        }

        final long pixPixelCount = pix.getWidth() * pix.getHeight();
        if (pixPixelCount < MIN_PIXEL_COUNT) {
            double scale = Math.sqrt(((double) MIN_PIXEL_COUNT) / pixPixelCount);
            Pix scaledPix = Scale.scale(pix, (float) scale);
            if (scaledPix.getNativePix() != 0) {
                pix.recycle();
                pix = scaledPix;
            }
        }
        return new ImageLoaderResult(pix);
    }

    @Override
    protected void onPostExecute(ImageLoaderResult imageLoaderResult) {
        super.onPostExecute(imageLoaderResult);
        Intent intent = new Intent(ACTION_IMAGE_LOADED);
        if (imageLoaderResult.getStatus() == PixLoaderStatus.SUCCESS) {
            intent.putExtra(EXTRA_PIX, imageLoaderResult.getPix().getNativePix());
        }
        intent.putExtra(EXTRA_STATUS, imageLoaderResult.getStatus().ordinal());
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
