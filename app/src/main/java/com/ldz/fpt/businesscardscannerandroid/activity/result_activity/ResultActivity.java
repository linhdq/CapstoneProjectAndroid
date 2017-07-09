package com.ldz.fpt.businesscardscannerandroid.activity.result_activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ldz.fpt.businesscardscannerandroid.R;
import com.ldz.fpt.businesscardscannerandroid.database.db_context.DatabaseHandler;
import com.ldz.fpt.businesscardscannerandroid.utils.Constant;
import com.ldz.fpt.businesscardscannerandroid.utils.StringUtil;
import com.ldz.fpt.businesscardscannerandroid.utils.xml_parser.LineModel;
import com.ldz.fpt.businesscardscannerandroid.utils.xml_parser.XMLParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ResultActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = ResultActivity.class.getSimpleName();
    //view
    @BindView(R.id.imv_card)
    protected ImageView imvCard;
    @BindView(R.id.txt_raw_output)
    protected TextView txtRawOutput;
    @BindView(R.id.recycler_view_name)
    protected RecyclerView recyclerViewName;
    @BindView(R.id.recycler_view_job)
    protected RecyclerView recyclerViewJob;
    @BindView(R.id.recycler_view_phone)
    protected RecyclerView recyclerViewPhone;
    @BindView(R.id.recycler_view_email)
    protected RecyclerView recyclerViewEmail;
    @BindView(R.id.item_add_job)
    protected LinearLayout itemAddJob;
    @BindView(R.id.item_add_phone)
    protected LinearLayout itemAddPhone;
    @BindView(R.id.item_add_email)
    protected LinearLayout itemAddEmail;
    //
    private boolean isFirst;
    private String htmlResult;
    private String utf8Result;
    private String base64;
    private String imageUri;
    //database
    private DatabaseHandler databaseHandler;
    //
    private StringUtil stringUtil;
    private XMLParser xmlParser;
    //adapter
    private ListItemInfoAdapter adapterName;
    private ListItemInfoAdapter adapterJob;
    private ListItemInfoAdapter adapterPhone;
    private ListItemInfoAdapter adapterEmail;
    //
    private List<String> listName;
    private List<String> listJob;
    private List<String> listPhone;
    private List<String> listEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        //
        ButterKnife.bind(this);
        //
        init();
        addListener();
        getDataFromBundle();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFirst) {
            configRecyclerView();
            bindData();
            isFirst = false;
        }
        refreshData();
    }

    private void init() {
        //
        isFirst = true;
        //database
        databaseHandler = new DatabaseHandler(this);
        //
        stringUtil = StringUtil.getInst(this);
        xmlParser = XMLParser.getInst();
        //
        listName = new ArrayList<>();
        listJob = new ArrayList<>();
        listPhone = new ArrayList<>();
        listEmail = new ArrayList<>();
    }

    private void addListener() {
        itemAddJob.setOnClickListener(this);
        itemAddPhone.setOnClickListener(this);
        itemAddEmail.setOnClickListener(this);
    }

    private void configRecyclerView() {
        StaggeredGridLayoutManager layoutManagerName = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        StaggeredGridLayoutManager layoutManagerJob = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        StaggeredGridLayoutManager layoutManagerPhone = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        StaggeredGridLayoutManager layoutManagerEmail = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        //
        adapterName = new ListItemInfoAdapter(this, listName);
        adapterJob = new ListItemInfoAdapter(this, listJob);
        adapterPhone = new ListItemInfoAdapter(this, listPhone);
        adapterEmail = new ListItemInfoAdapter(this, listEmail);
        //
        recyclerViewName.setLayoutManager(layoutManagerName);
        recyclerViewJob.setLayoutManager(layoutManagerJob);
        recyclerViewPhone.setLayoutManager(layoutManagerPhone);
        recyclerViewEmail.setLayoutManager(layoutManagerEmail);
        //
        recyclerViewName.setAdapter(adapterName);
        recyclerViewJob.setAdapter(adapterJob);
        recyclerViewPhone.setAdapter(adapterPhone);
        recyclerViewEmail.setAdapter(adapterEmail);
    }

    private void refreshData() {
        adapterName.notifyDataSetChanged();
        adapterJob.notifyDataSetChanged();
        adapterPhone.notifyDataSetChanged();
        adapterEmail.notifyDataSetChanged();
    }

    private void getDataFromBundle() {
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            onBackPressed();
        }
        imageUri = extras.getString(Constant.IMAGE_URI, "");
        htmlResult = extras.getString(Constant.HTML_STRING, "");
        utf8Result = extras.getString(Constant.UTF8_STRING, "");
    }

    private void bindData() {
        Glide.with(this).load(imageUri).into(imvCard);
        Log.d(TAG, "bindData: " + htmlResult);
        List<LineModel> list = xmlParser.getListLineFromXml(htmlResult);
        Collections.sort(list, new Comparator<LineModel>() {
            @Override
            public int compare(LineModel o1, LineModel o2) {
                return o2.getFontSize() - o1.getFontSize();
            }
        });
        utf8Result = "";
        for (LineModel lm : list) {
            utf8Result += lm.toString() + "\n";
        }
        listPhone.clear();
        listEmail.clear();
        listName.clear();
        listPhone.addAll(stringUtil.extractPhone(utf8Result));
        listEmail.addAll(stringUtil.extractEmails(utf8Result));
        listName.add(stringUtil.getNameFromRawOutput(list));
        if (listName.size() == 0) {
            listName.add("");
        }
        txtRawOutput.setText(utf8Result);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.item_add_job:
                listJob.add("");
                adapterJob.notifyDataSetChanged();
                break;
            case R.id.item_add_phone:
                listPhone.add("");
                adapterPhone.notifyDataSetChanged();
                break;
            case R.id.item_add_email:
                listEmail.add("");
                adapterEmail.notifyDataSetChanged();
                break;
            default:
                break;
        }
    }
}
