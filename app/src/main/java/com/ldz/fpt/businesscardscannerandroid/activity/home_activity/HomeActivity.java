package com.ldz.fpt.businesscardscannerandroid.activity.home_activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.ldz.fpt.businesscardscannerandroid.R;
import com.ldz.fpt.businesscardscannerandroid.activity.about_activity.AboutActivity;
import com.ldz.fpt.businesscardscannerandroid.activity.crop_image_activity.CropImageActivity;
import com.ldz.fpt.businesscardscannerandroid.activity.ocr_language_activity.DownloadLanguageActivity;
import com.ldz.fpt.businesscardscannerandroid.activity.feedback_activity.FeedbackActivity;
import com.ldz.fpt.businesscardscannerandroid.activity.MonitorActivity;
import com.ldz.fpt.businesscardscannerandroid.activity.setting_activity.SettingActivity;
import com.ldz.fpt.businesscardscannerandroid.database.db_context.DatabaseHandler;
import com.ldz.fpt.businesscardscannerandroid.database.model.CardHistoryModel;
import com.ldz.fpt.businesscardscannerandroid.utils.CheckDataTrainingProcess;
import com.ldz.fpt.businesscardscannerandroid.utils.Constant;
import com.ldz.fpt.businesscardscannerandroid.utils.image_util.ImageLoaderAsyncTask;
import com.ldz.fpt.businesscardscannerandroid.utils.image_util.PixLoaderStatus;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HomeActivity extends MonitorActivity implements View.OnClickListener {
    private static final String TAG = HomeActivity.class.getSimpleName();

    public static final int CAMERA_REQUEST_CODE = 404;
    public static final int GALLERY_REQUEST_CODE = 405;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    public static final String EXTRA_NATIVE_PIX = "native_pix";

    //view
    private DrawerLayout drawerLayout;
    private ListView listActionDrawer;
    private RecyclerView listCard;
    private ImageView btnCamera;
    private ImageView btnQRCode;
    private ImageView btnGallery;
    private Toast toast;
    //dialog
    private ProgressDialog progressDialog;
    //
    private Uri fileUri;
    private ImageLoaderAsyncTask imageLoaderAsyncTask;
    //
    private boolean receiverRegister;
    private boolean isFromCamera;
    //adapter
    private ListDrawerApdapter listDrawerApdapter;
    private ListCardAdapter listCardAdapter;
    //collections
    private List<DrawerItemModel> drawerItemModelList;
    private List<CardHistoryModel> cardHistoryModelList;
    //database
    private DatabaseHandler databaseHandler;

    @Override
    public String getScreenName() {
        return null;
    }

    @Override
    protected synchronized void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //
        init();
        addListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //get all card from database
        cardHistoryModelList.clear();
        cardHistoryModelList.addAll(databaseHandler.getAllCardHistory());
        //
        if (listCardAdapter == null) {
            bindDataToListCard();
        } else {
            listCardAdapter.notifyDataSetChanged();
        }
        //
        new CheckDataTrainingProcess().execute(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void init() {
        //
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        //view
        listActionDrawer = (ListView) findViewById(R.id.list_menu_drawer);
        listCard = (RecyclerView) findViewById(R.id.list_card);
        btnCamera = (ImageView) findViewById(R.id.imv_camera_button);
        btnQRCode = (ImageView) findViewById(R.id.imv_qr_code_button);
        btnGallery = (ImageView) findViewById(R.id.imv_gallery_button);
        //dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Loading ...");
        //
        receiverRegister = false;
        //collections
        drawerItemModelList = new ArrayList<>();
        drawerItemModelList.add(new DrawerItemModel(R.mipmap.ic_language_, getString(R.string.add_language)));
        drawerItemModelList.add(new DrawerItemModel(R.mipmap.ic_feedback, getString(R.string.feedback)));
        drawerItemModelList.add(new DrawerItemModel(R.mipmap.ic_setting, getString(R.string.action_settings)));
        drawerItemModelList.add(new DrawerItemModel(R.mipmap.ic_about, getString(R.string.about)));
        //
        listDrawerApdapter = new ListDrawerApdapter(this, drawerItemModelList);
        listActionDrawer.setAdapter(listDrawerApdapter);
        //
        cardHistoryModelList = new ArrayList<>();
        //database
        databaseHandler = new DatabaseHandler(this);
    }

    private void addListener() {
        //
        btnGallery.setOnClickListener(this);
        btnQRCode.setOnClickListener(this);
        btnCamera.setOnClickListener(this);
        //
        listActionDrawer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        openDownloadLanguageActivity();
                        break;
                    case 1:
                        openFeedbackActivity();
                        break;
                    case 2:
                        openSettingActivity();
                        break;
                    case 3:
                        openAboutUSActivity();
                        break;
                    default:
                        break;
                }
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }

    private void openDownloadLanguageActivity() {
        Intent intent = new Intent(HomeActivity.this, DownloadLanguageActivity.class);
        startActivity(intent);
    }

    private void openFeedbackActivity() {
        Intent intent = new Intent(HomeActivity.this, FeedbackActivity.class);
        startActivity(intent);
    }

    private void openSettingActivity() {
        Intent intent = new Intent(HomeActivity.this, SettingActivity.class);
        startActivity(intent);
    }

    private void openAboutUSActivity() {
        Intent intent = new Intent(HomeActivity.this, AboutActivity.class);
        startActivity(intent);
    }

    private void bindDataToListCard() {
        //config recyclerview
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        listCardAdapter = new ListCardAdapter(cardHistoryModelList, this);
        listCard.setLayoutManager(layoutManager);
        listCard.setAdapter(listCardAdapter);
    }

    private void showToast(String mess) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(this, mess, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void openIntentQRCode() {
        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        intentIntegrator.setPrompt("Scan");
        intentIntegrator.setCameraId(0);
        intentIntegrator.setBeepEnabled(false);
        intentIntegrator.setBarcodeImageEnabled(false);
        intentIntegrator.initiateScan();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imv_camera_button:
                openCamera();
                break;
            case R.id.imv_gallery_button:
                openGallery();
                break;
            case R.id.imv_qr_code_button:
                openIntentQRCode();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != GALLERY_REQUEST_CODE && requestCode != CAMERA_REQUEST_CODE) {
            IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (data != null) {
                if (intentResult.getContents() == null) {
                    showToast("Cancelled!");
                } else {
                    showToast("Scanned: " + intentResult.getContents());
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
            if (resultCode == RESULT_OK) {
                switch (requestCode) {
                    case GALLERY_REQUEST_CODE:
                        if (data != null && data.getData() != null) {
                            fileUri = data.getData();
                        }
                        isFromCamera = false;
                        break;
                    case CAMERA_REQUEST_CODE:
                        isFromCamera = true;
                        break;
                    default:
                        break;
                }
                if (fileUri != null) {
                    loadBitmapFromContentUri(fileUri);
//                Intent cropImageIntent = new Intent(HomeActivity.this, CropImageActivity.class);
//                cropImageIntent.putExtra(Constant.INTENT_IMAGE_URI, fileUri.toString());
//                cropImageIntent.putExtra(Constant.IS_RESULT_FROM_CAMERA, isFromCamera);
//                startActivity(cropImageIntent);
//                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                } else {
                    showToast("Take photo error!");
                }
            }
        }
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE, this); // create a file to save the image
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_REQUEST_CODE);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    /**
     * Create a file Uri for saving an image or video
     */
    private static Uri getOutputMediaFileUri(int type, Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return FileProvider.getUriForFile(context, "com.ldz.fpt.businesscardscannerandroid.provider", getOutputMediaFile(type));
        } else {
            return Uri.fromFile(getOutputMediaFile(type));
        }
    }

    /**
     * Create a File for saving an image or video
     */
    private static File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "BCS");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("BCS", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".png");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    protected void loadBitmapFromContentUri(final Uri cameraPicUri) {
        if (imageLoaderAsyncTask != null) {
            imageLoaderAsyncTask.cancel(true);
        }
        registerImageLoaderReceiver();
        imageLoaderAsyncTask = new ImageLoaderAsyncTask(this, cameraPicUri, false);
        imageLoaderAsyncTask.execute();
    }

    private synchronized void unRegisterImageLoaderReceiver() {
        if (receiverRegister) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
            receiverRegister = false;
        }
    }

    private synchronized void registerImageLoaderReceiver() {
        if (!receiverRegister) {
            final IntentFilter intentFilter = new IntentFilter(ImageLoaderAsyncTask.ACTION_IMAGE_LOADED);
            intentFilter.addAction(ImageLoaderAsyncTask.ACTION_IMAGE_LOADING_START);
            LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);
            receiverRegister = true;
        }
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase(ImageLoaderAsyncTask.ACTION_IMAGE_LOADED)) {
                progressDialog.dismiss();
                unRegisterImageLoaderReceiver();
                final long nativePix = intent.getLongExtra(ImageLoaderAsyncTask.EXTRA_PIX, 0);
                final int statusNumber = intent.getIntExtra(ImageLoaderAsyncTask.EXTRA_STATUS, PixLoaderStatus.SUCCESS.ordinal());
                if (PixLoaderStatus.values()[statusNumber] == PixLoaderStatus.SUCCESS) {

                    Intent intent1 = new Intent(HomeActivity.this, CropImageActivity.class);
                    intent1.putExtra(Constant.EXTRA_NATIVE_PIX, nativePix);
                    startActivity(intent1);

                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            } else if (intent.getAction().equalsIgnoreCase(ImageLoaderAsyncTask.ACTION_IMAGE_LOADING_START)) {
                progressDialog.show();
            }
        }
    };
}
