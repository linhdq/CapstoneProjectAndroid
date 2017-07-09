package com.ldz.fpt.businesscardscannerandroid.activity.crop_image_activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.common.base.Optional;
import com.googlecode.leptonica.android.Box;
import com.googlecode.leptonica.android.Clip;
import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.Projective;
import com.googlecode.leptonica.android.Rotate;
import com.googlecode.leptonica.android.Scale;
import com.googlecode.leptonica.android.WriteFile;
import com.ldz.fpt.businesscardscannerandroid.R;
import com.ldz.fpt.businesscardscannerandroid.activity.MonitorActivity;
import com.ldz.fpt.businesscardscannerandroid.activity.ocr_activity.OCRActivity;
import com.ldz.fpt.businesscardscannerandroid.opencv.OpenCVNative;
import com.ldz.fpt.businesscardscannerandroid.utils.Constant;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import de.greenrobot.event.EventBus;

public class CropImageActivity extends MonitorActivity implements View.OnClickListener {
    private static final String TAG = CropImageActivity.class.getSimpleName();
    //view
    private CropImageView cropImageView;
    private ViewSwitcher viewSwitcher;
    private LinearLayout itemRotate;
    private LinearLayout itemCrop;
    private LinearLayout layoutBottom;
    private Toast toast;
    //dialog
    private Dialog blurryWaringDialog;
    private Button btnContinue;
    private Button btnNewImage;
    //
    private int rotation = 0;
    private boolean cropping;
    private Pix pix;
    private float[] listPointCardDetection;
    private int[] listPointsTextBlock;
    private int mWidth, mHeight;
    private float ratioX, ratioY;
    private boolean isFirst;
    //
    private CropHighlightView cropHighlightView;
    private Optional<CropData> cropData = Optional.absent();
    private Optional<LoadPixForCropTask> loadPixForCropTaskOptional = Optional.absent();

    static {
        System.loadLibrary("opencv_processing");
    }

    BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case BaseLoaderCallback.SUCCESS:
                    startCropping();
                    break;
                default:
                    CropImageActivity.this.finish();
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    @Override
    public String getScreenName() {
        return TAG;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        EventBus.getDefault().register(this);
        setContentView(R.layout.activity_cropimage);
        //
        init();
        addListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFirst) {
            if (OpenCVLoader.initDebug()) {
                baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            } else {
                OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, baseLoaderCallback);
            }
            isFirst = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        unbindDrawables(findViewById(android.R.id.content));
        cropImageView.clear();
        if (loadPixForCropTaskOptional.isPresent()) {
            loadPixForCropTaskOptional.get().cancel(true);
            loadPixForCropTaskOptional = Optional.absent();
        }
        if (cropData.isPresent()) {
            cropData.get().recylce();
            cropData = Optional.absent();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
        if (pix != null) {
            pix.recycle();
        }
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.item_crop:
                onCropPressed();
                break;
            case R.id.item_rotate:
                onRotatePressed(1);
                break;
            case R.id.btn_continue:
                blurryWaringDialog.dismiss();
                showDefaultCroppingRectangle(CropImageActivity.this.cropData.get().getBitmap());
                break;
            case R.id.btn_new_image:
                blurryWaringDialog.dismiss();
                onBackPressed();
                break;
            default:
                break;
        }
    }

    private void init() {
        //view
        cropImageView = (CropImageView) findViewById(R.id.crop_image_view);
        viewSwitcher = (ViewSwitcher) findViewById(R.id.crop_layout);
        itemCrop = (LinearLayout) findViewById(R.id.item_crop);
        itemRotate = (LinearLayout) findViewById(R.id.item_rotate);
        layoutBottom = (LinearLayout) findViewById(R.id.layout_bottom);
        //custom blurry waring dialog
        blurryWaringDialog = new Dialog(this);
        blurryWaringDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        blurryWaringDialog.setContentView(R.layout.custom_dialog_blurry);
        blurryWaringDialog.setCanceledOnTouchOutside(false);
        blurryWaringDialog.getWindow().setGravity(Gravity.TOP);
        TypedValue tv = new TypedValue();
        WindowManager.LayoutParams params = blurryWaringDialog.getWindow().getAttributes();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            params.y = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }
        blurryWaringDialog.getWindow().setAttributes(params);

        btnContinue = (Button) blurryWaringDialog.findViewById(R.id.btn_continue);
        btnNewImage = (Button) blurryWaringDialog.findViewById(R.id.btn_new_image);
        //
        isFirst = true;
    }

    private void addListener() {
        itemRotate.setOnClickListener(this);
        itemCrop.setOnClickListener(this);
        btnContinue.setOnClickListener(this);
        btnNewImage.setOnClickListener(this);
    }

    private void showToast(String mess) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(this, mess, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void onRotatePressed(int delta) {
        if (cropData.isPresent()) {
            if (delta < 0) {
                delta = -delta * 3;
            }
            rotation += delta;
            rotation = rotation % 4;
            cropImageView.setImageBitmapResetBase(cropData.get().getBitmap(), false, rotation * 90);
            showDefaultCroppingRectangle(cropData.get().getBitmap());
        }
    }

    private void startCropping() {
        viewSwitcher.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                Bundle bundle = getIntent().getExtras();
                if (bundle != null) {
                    Log.d(TAG, "onGlobalLayout: start cropping");
                    final long nativePix = bundle.getLong(Constant.EXTRA_NATIVE_PIX);
                    pix = new Pix(nativePix);
                    rotation = 0;
                    final float margin = getResources().getDimension(R.dimen.crop_margin);
                    final int width = (int) (viewSwitcher.getWidth() - 2 * margin);
                    final int height = (int) (viewSwitcher.getHeight() - 2 * margin);
                    loadPixForCropTaskOptional = Optional.of(new LoadPixForCropTask(pix.clone(), width, height));
                    loadPixForCropTaskOptional.get().execute();
                } else {
                    showToast("Could not load image!");
                    finish();
                }
                viewSwitcher.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(final CropData cropData) {
        if (cropData.getBitmap() == null) {
            //should not happen. Scaling of the original document failed some how. Maybe out of memory?
            showToast("Could not load image!");
            return;
        }
        this.cropData = Optional.of(cropData);
        this.mWidth = viewSwitcher.getWidth();
        //
        adjustBottomMenu();
        //detect card visit in image
        new DetectCardProcess().execute(cropData.getBitmap());
    }

    private void checkBlurResult(CropData cropData) {
        switch (cropData.getBlurDetectionResult().getBlurriness()) {
            case NOT_BLURRED:
                showDefaultCroppingRectangle(cropData.getBitmap());
                break;
            case MEDIUM_BLUR:
            case STRONG_BLUR:
                blurryWaringDialog.show();
                break;
        }
    }

    private void adjustBottomMenu() {
        if (cropData.isPresent()) {
            layoutBottom.setVisibility(View.VISIBLE);
        } else {
            layoutBottom.setVisibility(View.GONE);
        }
    }

    private void onCropPressed() {
        if (!cropData.isPresent() || cropping || (cropHighlightView == null)) {
            return;
        }
        cropping = true;
        new CropImageProcess().execute();
    }

    private void unbindDrawables(View view) {
        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup && !(view instanceof AdapterView)) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            ((ViewGroup) view).removeAllViews();
        }
    }

    private void showDefaultCroppingRectangle(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Rect imageRect = new Rect(0, 0, width, height);

        // make the default size about 4/5 of the width or height
        int cropWidth = Math.min(width, height) * 4 / 5;


        int x = (width - cropWidth) / 2;
        int y = (height - cropWidth) / 2;

        RectF cropRect = new RectF(x, y, x + cropWidth, y + cropWidth);

        CropHighlightView hv = new CropHighlightView(cropImageView, imageRect, cropRect, listPointCardDetection);

        cropImageView.resetMaxZoom();
        cropImageView.add(hv);
        cropHighlightView = hv;
        cropHighlightView.setFocus(true);
        cropImageView.invalidate();
    }

    public boolean isCropping() {
        return cropping;
    }

    public class CropImageProcess extends AsyncTask<Void, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Void... params) {
            Bitmap bitmap = null;
            try {
                float scale = 1f / cropData.get().getScaleResult().getScaleFactor();
                Matrix scaleMatrix = new Matrix();
                scaleMatrix.setScale(scale, scale);

                final float[] trapezoid = cropHighlightView.getTrapezoid();
                final RectF perspectiveCorrectedBoundingRect = new RectF(cropHighlightView.getPerspectiveCorrectedBoundingRect());
                scaleMatrix.mapRect(perspectiveCorrectedBoundingRect);
                Box bb = new Box((int) perspectiveCorrectedBoundingRect.left, (int) perspectiveCorrectedBoundingRect.top, (int) perspectiveCorrectedBoundingRect.width(), (int) perspectiveCorrectedBoundingRect.height());
                Pix croppedPix = Clip.clipRectangle2(pix, bb);
                if (croppedPix == null) {
                    throw new IllegalStateException();
                }
                pix.recycle();

                scaleMatrix.postTranslate(-bb.getX(), -bb.getY());
                scaleMatrix.mapPoints(trapezoid);

                final float[] dest = new float[]{0, 0, bb.getWidth(), 0, bb.getWidth(), bb.getHeight(), 0, bb.getHeight()};
                Pix bilinear = Projective.projectiveTransformColor(croppedPix, dest, trapezoid);
                if (bilinear == null) {
                    bilinear = croppedPix.clone();
                }

                croppedPix.recycle();

                if (rotation != 0 && rotation != 4) {
                    Pix rotatedPix = Rotate.rotateOrth(bilinear, rotation);
                    bilinear.recycle();
                    bilinear = rotatedPix.clone();
                    rotatedPix.recycle();
                }
                if (bilinear == null) {
                    throw new IllegalStateException();
                }

                pix = bilinear.clone();
                bilinear.recycle();

                mHeight = mWidth * pix.getHeight() / pix.getWidth();
                bilinear = Scale.scaleToSize(pix.clone(), mWidth, mHeight, Scale.ScaleType.FIT);
                bitmap = WriteFile.writeBitmap(bilinear);
                bilinear.recycle();
                //
                ratioX = pix.getWidth() * 1.0f / mWidth;
                ratioY = pix.getHeight() * 1.0f / mHeight;
            } catch (IllegalStateException e) {
                setResult(RESULT_CANCELED);
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap != null) {
                new ImageProcessing().execute(bitmap);
            } else {
                showToast("Sorry! We have some mistakes...");
                finish();
            }
        }
    }


    public class ImageProcessing extends AsyncTask<Bitmap, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(Bitmap... params) {
            Bitmap bitmap = params[0];
            try {
                Mat matS = new Mat();
                Utils.bitmapToMat(bitmap, matS);
                listPointsTextBlock = OpenCVNative.detectTextBlock(matS.getNativeObjAddr());
                matS.release();
//                Pix pix = ReadFile.readBitmap(bitmap);
//                long nativeTextPix = nativeOCRBook(pix.getNativePix());
//                pix.recycle();
//                pix = new Pix(nativeTextPix);
//                bitmap = WriteFile.writeBitmap(pix);

//                listBitmap = new Bitmap[listPointCardDetection.length / 4];
//                if (listPointCardDetection.length / 4 == 1) {
//                    listBitmap[0] = (bitmap);
//                } else {
//                    int index = 0;
//                    for (int i = 0; i < listPointCardDetection.length; i += 4) {
//                        listBitmap[index++] = getSubBitmap(listPointsTextBlock[i], listPointsTextBlock[i + 1], listPointsTextBlock[i + 2], listPointsTextBlock[i + 3], matD);
//                    }
//                }
//                for (int i = 0; i < listBitmap.length; i++) {
//                    Mat matS1 = new Mat();
//                    Mat matD1 = new Mat();
//                    Utils.bitmapToMat(listBitmap[i], matS1);
//                    OpenCVNative.increaseContrast(matS1.getNativeObjAddr(), matD1.getNativeObjAddr());
////                    OpenCVNative.convertGray(matS1.getNativeObjAddr(), matD1.getNativeObjAddr());
//                    Utils.matToBitmap(matD1, listBitmap[i]);
//                }
//                OpenCVNative.increaseContrast(matS.getNativeObjAddr(), matD.getNativeObjAddr());
//                OpenCVNative.convertGray(matS.getNativeObjAddr(), matD.getNativeObjAddr());
                for (int i = 0; i < listPointsTextBlock.length; i++) {
                    if (i % 2 == 0) {
                        listPointsTextBlock[i] = (int) (listPointsTextBlock[i] * ratioX);
                    } else {
                        listPointsTextBlock[i] = (int) (listPointsTextBlock[i] * ratioY);
                    }
                }
//                bitmap = WriteFile.writeBitmap(pix.copy());
//                Utils.bitmapToMat(bitmap, matS);
//                for (int i = 0; i < listPointsTextBlock.length; i += 4) {
//                    Imgproc.rectangle(matS, new Point(listPointsTextBlock[i], listPointsTextBlock[i + 1]),
//                            new Point(listPointsTextBlock[i] + listPointsTextBlock[i + 2], listPointsTextBlock[i + 1] + listPointsTextBlock[i + 3]), new Scalar(0, 255, 0), 10);
//                }
//                Utils.matToBitmap(matS, bitmap);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            Intent intent = new Intent(CropImageActivity.this, OCRActivity.class);
            intent.putExtra(Constant.EXTRA_NATIVE_PIX, pix.getNativePix());
            intent.putExtra(Constant.BLUR_VALUE, cropData.get().getBlurDetectionResult().getBlurValue());
            intent.putExtra(Constant.LIST_POINT_TEXT_BLOCK, listPointsTextBlock);
            startActivity(intent);
            finish();
        }
    }

    public class DetectCardProcess extends AsyncTask<Bitmap, Void, Void> {

        @Override
        protected Void doInBackground(Bitmap... params) {
            try {
                Mat mat = new Mat();
                Utils.bitmapToMat(params[0], mat);
                listPointCardDetection = OpenCVNative.detectCardVisit(mat.getNativeObjAddr());
                mat.release();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            viewSwitcher.setDisplayedChild(1);
            cropImageView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    cropImageView.setImageBitmapResetBase(CropImageActivity.this.cropData.get().getBitmap(), true, rotation * 90);
                    checkBlurResult(CropImageActivity.this.cropData.get());
                    cropImageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }

            });
        }
    }
}

