package com.ldz.fpt.businesscardscannerandroid.activity.ocr_language_activity;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.bumptech.glide.Glide;
import com.ldz.fpt.businesscardscannerandroid.R;
import com.ldz.fpt.businesscardscannerandroid.database.db_context.DatabaseHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by linhdq on 6/6/17.
 */

public class OCRLanguageAdapter extends BaseAdapter implements ListAdapter {

    private final List<OcrLanguage> languages = new ArrayList<>();
    private final LayoutInflater inflater;
    private boolean showOnlyLanguageNames;
    private Context context;
    //database
    private DatabaseHandler databaseHandler;

    public OCRLanguageAdapter(final Context context, boolean showOnlyLanguageNames) {
        this.showOnlyLanguageNames = showOnlyLanguageNames;
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.databaseHandler = new DatabaseHandler(context);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public int getCount() {
        return languages.size();
    }

    @Override
    public Object getItem(int position) {
        return languages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_list_ocr_language, null);
            holder = new ViewHolder();
            holder.viewFlipper = (ViewFlipper) convertView.findViewById(R.id.viewFlipper);
            holder.txtLanguage = (TextView) convertView.findViewById(R.id.textView_language);
            holder.imvFlag = (ImageView) convertView.findViewById(R.id.imv_flag);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        OcrLanguage language = languages.get(position);
        Glide.with(context).load(Uri.parse("file:///android_asset/country_flag/" + language.getLanguageCode() + ".png")).into(holder.imvFlag);
        if (showOnlyLanguageNames) {
            holder.viewFlipper.setVisibility(View.INVISIBLE);
        } else {
            if (language.isInstalled()) {
                if (language.getLanguageCode().equals("eng")) {
                    holder.viewFlipper.setDisplayedChild(3);
                } else {
                    holder.viewFlipper.setDisplayedChild(2);
                }
            } else if (language.isDownloading()) {
                holder.viewFlipper.setDisplayedChild(1);
            } else {
                holder.viewFlipper.setDisplayedChild(0);
            }

        }

        holder.txtLanguage.setText(language.getDisplayText());
        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return languages.isEmpty();
    }

    public void setDownloading(String languageDisplayValue, boolean downloading) {
        for (OcrLanguage lang : this.languages) {
            if (lang.getDisplayText().equalsIgnoreCase(languageDisplayValue)) {
                lang.setDownloading(downloading);
                break;
            }
        }
    }

    public void addAll(List<OcrLanguage> languages) {
        this.languages.clear();
        this.languages.addAll(languages);
        Collections.sort(this.languages, languageComparator);
    }

    public void refreshData() {
        this.languages.clear();
        this.languages.addAll(databaseHandler.getAllOCRLanguage());
        Collections.sort(this.languages, languageComparator);
        this.notifyDataSetChanged();
    }

    public void add(OcrLanguage language) {
        languages.add(language);
        Collections.sort(languages, languageComparator);
        notifyDataSetChanged();
    }

    private static Comparator<OcrLanguage> languageComparator = new Comparator<OcrLanguage>() {

        @Override
        public int compare(OcrLanguage lhs, OcrLanguage rhs) {
            return lhs.getDisplayText().compareTo(rhs.getDisplayText());
        }
    };

    private static class ViewHolder {
        private ViewFlipper viewFlipper;
        private TextView txtLanguage;
        private ImageView imvFlag;
    }
}
