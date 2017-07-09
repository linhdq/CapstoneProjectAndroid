package com.ldz.fpt.businesscardscannerandroid.activity.result_activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.bumptech.glide.Glide;
import com.ldz.fpt.businesscardscannerandroid.R;
import com.ldz.fpt.businesscardscannerandroid.database.db_context.DatabaseHandler;
import com.ldz.fpt.businesscardscannerandroid.database.model.CardHistoryModel;
import com.ldz.fpt.businesscardscannerandroid.utils.Constant;
import com.ldz.fpt.businesscardscannerandroid.utils.StringUtil;

public class TestActivity extends AppCompatActivity {
    private static final String TAG = TestActivity.class.getSimpleName();
    //view
    private ImageView imageView;
    private TextView txtResult;
    private ViewSwitcher viewSwitcher;
    //
    private boolean isFirst;
    private String htmlResult;
    private String base64;
    private String imageUri;
    //database
    private DatabaseHandler databaseHandler;
    //
    private StringUtil stringUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        //
        imageView = (ImageView) findViewById(R.id.imv_image);
        txtResult = (TextView) findViewById(R.id.txt_result);
        viewSwitcher = (ViewSwitcher) findViewById(R.id.view_switcher);
        //
        isFirst = true;
        //database
        databaseHandler = new DatabaseHandler(this);
        //
        stringUtil = StringUtil.getInst(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFirst) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                onBackPressed();
            }
            imageUri = extras.getString(Constant.IMAGE_URI, "");
            htmlResult = extras.getString(Constant.HTML_STRING, "");
//            long nativePix = extras.getLong(Constant.NATIVE_PIX, 0);
//            if (nativePix != 0) {
//                Pix pix = new Pix(nativePix);
//                Bitmap bitmap = WriteFile.writeBitmap(pix);
//                base64 = ImageUtils.encodeBase64(bitmap);
//                pix.recycle();
//            }
            //
            txtResult.setText(Html.fromHtml(htmlResult));
            Glide.with(TestActivity.this).load(imageUri).into(imageView);
            CardHistoryModel model = new CardHistoryModel("Nguyen Van A", "0976678404", "abc@gmail.com", base64, imageUri, htmlResult);
            if (databaseHandler.checkCardHistoryIsExists(model.getId())) {
                databaseHandler.updateCardHistory(model);
            } else {
                databaseHandler.addCardHistory(model);
            }
            viewSwitcher.setDisplayedChild(1);
            //
            isFirst = false;
            Log.d(TAG, "onResume: text: " + txtResult.getText().toString());
            Log.d(TAG, "onResume: phone_number: " + stringUtil.extractPhone(txtResult.getText().toString()));
            Log.d(TAG, "onResume: email: " + stringUtil.extractEmails(txtResult.getText().toString()));
            Log.d(TAG, "onResume: trim: "+stringUtil.trimString(txtResult.getText().toString()));
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
