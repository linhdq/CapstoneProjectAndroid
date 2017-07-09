package com.ldz.fpt.businesscardscannerandroid.activity.ocr_activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.Scale;
import com.googlecode.leptonica.android.WriteFile;
import com.ldz.fpt.businesscardscannerandroid.R;
import com.ldz.fpt.businesscardscannerandroid.activity.MonitorActivity;
import com.ldz.fpt.businesscardscannerandroid.activity.result_activity.ResultActivity;
import com.ldz.fpt.businesscardscannerandroid.activity.result_activity.TestActivity;
import com.ldz.fpt.businesscardscannerandroid.activity.ocr_language_activity.OcrLanguage;
import com.ldz.fpt.businesscardscannerandroid.activity.ocr_language_activity.OcrLanguageDataStore;
import com.ldz.fpt.businesscardscannerandroid.utils.Constant;
import com.ldz.fpt.businesscardscannerandroid.utils.Screen;
import com.ldz.fpt.businesscardscannerandroid.utils.SharedPreferentUtils;
import com.ldz.fpt.businesscardscannerandroid.utils.Util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;

public class OCRActivity extends MonitorActivity implements OnClickListener {
    private static final String TAG = OCRActivity.class.getSimpleName();
    //view
    private ViewSwitcher viewSwitcher;
    private OCRImageView ocrImageView;
    //language dialog
    private Dialog languageDialog;
    private TextView txtImageQuality;
    private Spinner spinnerLanguage;
    private Button btnCancel;
    private Button btnStart;

    private int accuracy;
    private long nativePix;
    private String ocrLanguage; // is set by dialog in
    private boolean isFirst;
    private double blurValue;
    private String languageCode;
    private String language;
    private String imageUri;
    private String htmlString;
    private int[] listPoints;
    //
    private Bitmap bitmap;
    private Pix orgPix;
    private Pix finalPix;
    private OCR ocr;
    // receives messages from background task
    private Messenger messageReceiver = new Messenger(new ProgressActivityHandler());
    //
    private List<OcrLanguage> listOcrLanguage;
    private SharedPreferentUtils sharedPreferentUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        nativePix = bundle.getLong(Constant.EXTRA_NATIVE_PIX, -1);
        blurValue = bundle.getDouble(Constant.BLUR_VALUE, 0);
        listPoints = bundle.getIntArray(Constant.LIST_POINT_TEXT_BLOCK);
        if (nativePix == -1) {
            finish();
            return;
        }
        Screen.lockOrientation(this);
        setContentView(R.layout.activity_ocr);
        //
        init();
        addListener();
        //
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFirst) {
            new LoadDataTask().execute();
            //
            isFirst = false;
        }
    }

    private void init() {
        //view
        viewSwitcher = (ViewSwitcher) findViewById(R.id.view_switcher);
        ocrImageView = (OCRImageView) findViewById(R.id.ocr_imageview);
        //custom dialog language
        languageDialog = new Dialog(this);
        languageDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        languageDialog.setContentView(R.layout.custom_dialog_select_language);
        languageDialog.setCanceledOnTouchOutside(false);

        txtImageQuality = (TextView) languageDialog.findViewById(R.id.txt_image_quality);
        spinnerLanguage = (Spinner) languageDialog.findViewById(R.id.spinner_language);
        btnCancel = (Button) languageDialog.findViewById(R.id.btn_cancel);
        btnStart = (Button) languageDialog.findViewById(R.id.btn_start);
        //
        ocr = new OCR(this, messageReceiver);
        sharedPreferentUtils = SharedPreferentUtils.getInst(this);
        //
        isFirst = true;
    }

    private void addListener() {
        btnCancel.setOnClickListener(this);
        btnStart.setOnClickListener(this);
        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                languageCode = listOcrLanguage.get(position).getLanguageCode();
                language = listOcrLanguage.get(position).getDisplayText();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cancel:
                languageDialog.dismiss();
                finish();
                break;
            case R.id.btn_start:
                languageDialog.dismiss();
                if (viewSwitcher.getDisplayedChild() == 1) {
                    viewSwitcher.setDisplayedChild(0);
                }
                ocr.startOCRForSimpleLayout(OCRActivity.this, languageCode, orgPix, ocrImageView.getWidth(), ocrImageView.getHeight(), listPoints);
                break;
            default:
                break;
        }
    }

    private class LoadDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            orgPix = new Pix(nativePix);
            bitmap = WriteFile.writeBitmap(orgPix.copy());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            listOcrLanguage = OcrLanguageDataStore.getInstalledOCRLanguages(OCRActivity.this);
            List<String> listLanguages = new ArrayList<>();
            for (OcrLanguage lang : listOcrLanguage) {
                listLanguages.add(lang.getDisplayText());
            }
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(OCRActivity.this,
                    android.R.layout.simple_spinner_item, listLanguages);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerLanguage.setAdapter(dataAdapter);
            spinnerLanguage.setSelection(listLanguages.indexOf(sharedPreferentUtils.getLanguage()));
            //
            ocrImageView.setImageBitmapResetBase(bitmap, true, 0);
            if (viewSwitcher.getDisplayedChild() == 0) {
                viewSwitcher.setDisplayedChild(1);
            }
            if (blurValue < 0.5) {
                txtImageQuality.setText(getString(R.string.txt_good));
                txtImageQuality.setTextColor(getResources().getColor(R.color.green_a_700));
            } else if (blurValue < 0.67) {
                txtImageQuality.setText(getString(R.string.txt_not_good));
                txtImageQuality.setTextColor(getResources().getColor(R.color.blue_900));
            } else {
                txtImageQuality.setText(getString(R.string.txt_bad));
                txtImageQuality.setTextColor(getResources().getColor(R.color.red_primary_dark));
            }
            languageDialog.show();
        }
    }

    /**
     * receives progress status messages from the background ocr task and
     * displays them in the current activity
     */
    private class ProgressActivityHandler extends Handler {

        private String hocrString;

        private boolean mHasStartedOcr = false;

        public void handleMessage(Message msg) {
            switch (msg.what) {

                case OCR.MESSAGE_EXPLANATION_TEXT: {
                    Log.d(TAG, "handleMessage: " + msg.arg1);
                    break;
                }
                case OCR.MESSAGE_TESSERACT_PROGRESS: {
                    if (!mHasStartedOcr) {
                        mHasStartedOcr = true;
                    }
                    int percent = msg.arg1;
                    Bundle data = msg.getData();
                    ocrImageView.setProgress(percent,
                            (RectF) data.getParcelable(OCR.EXTRA_WORD_BOX),
                            (RectF) data.getParcelable(OCR.EXTRA_OCR_BOX));
                    break;
                }
                case OCR.MESSAGE_PREVIEW_IMAGE: {
                    if (viewSwitcher.getDisplayedChild() == 0) {
                        viewSwitcher.setDisplayedChild(1);
                    }
                    ocrImageView.setImageBitmapResetBase((Bitmap) msg.obj, true, 0);
                    break;
                }
                case OCR.MESSAGE_FINAL_IMAGE: {
                    long nativePix = (long) msg.obj;

                    if (nativePix != 0) {
                        if (finalPix != null) {
                            finalPix.recycle();
                        }
                        finalPix = new Pix(nativePix);
                    }
                    break;
                }
                case OCR.MESSAGE_LAYOUT_PIX: {
                    Bitmap layoutPix = (Bitmap) msg.obj;
                    ocrImageView.setImageBitmapResetBase(layoutPix, true, 0);
                    break;
                }
                case OCR.MESSAGE_HOCR_TEXT: {
                    this.hocrString = (String) msg.obj;
                    accuracy = msg.arg1;
                    break;
                }
                case OCR.MESSAGE_UTF8_TEXT: {
                    OCRActivity.this.htmlString = (String) msg.obj;
                    break;
                }
                case OCR.MESSAGE_END: {
                    new SavePixTask().execute();
                    viewSwitcher.setDisplayedChild(0);
                    break;
                }
                case OCR.MESSAGE_ERROR: {
                    Toast.makeText(getApplicationContext(), getText(msg.arg1), Toast.LENGTH_LONG).show();
                    break;
                }
            }
        }

    }

    private File saveImage(Pix p) throws IOException {
        CharSequence id = DateFormat.format("ssmmhhddMMyy", new Date(System.currentTimeMillis()));
        return Util.savePixToSD(p, id.toString());
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (ocrLanguage != null) {
            outState.putString(Constant.OCR_LANGUAGE, ocrLanguage);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (ocrLanguage == null) {
            ocrLanguage = savedInstanceState.getString(Constant.OCR_LANGUAGE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sharedPreferentUtils.saveLanguage(language);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public String getScreenName() {
        return TAG;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (finalPix != null) {
            finalPix.recycle();
            finalPix = null;
        }
        if (orgPix != null) {
            orgPix.recycle();
            orgPix = null;
        }
        if (languageDialog != null) {
            languageDialog.cancel();
        }
    }

    private class SavePixTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Pix newPix = orgPix.clone();
                float ratio = newPix.getWidth() * 1.0f / newPix.getHeight();
                int width = Screen.getScreenWidth(OCRActivity.this) / 2;
                int height = (int) (width / ratio);
                orgPix.recycle();
                orgPix = Scale.scaleToSize(newPix, width, height, Scale.ScaleType.FILL);
                newPix.recycle();
                File file = saveImage(orgPix);
                imageUri = Uri.fromFile(file).toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Intent intent = new Intent(OCRActivity.this, ResultActivity.class);
            intent.putExtra(Constant.NATIVE_PIX, nativePix);
            intent.putExtra(Constant.HTML_STRING, htmlString);
            intent.putExtra(Constant.IMAGE_URI, imageUri);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        }
    }
}
