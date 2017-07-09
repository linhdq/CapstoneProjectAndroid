package com.ldz.fpt.businesscardscannerandroid.activity.home_activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ldz.fpt.businesscardscannerandroid.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linhdq on 4/18/17.
 */

public class ListDrawerApdapter extends BaseAdapter {
    //
    private Context context;
    private LayoutInflater inflater;
    private List<DrawerItemModel> list;
    private DrawerItemModel model;

    public ListDrawerApdapter(Context context, List<DrawerItemModel> list) {
        this.context = context;
        if (list != null) {
            this.list = list;
        } else {
            this.list = new ArrayList<>();
        }
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = inflater.inflate(R.layout.item_on_list_drawer, parent, false);
        if (view != null) {
            ImageView imvIcon = (ImageView) view.findViewById(R.id.imv_icon);
            TextView txtTitle = (TextView) view.findViewById(R.id.txt_title);
            //
            model = list.get(position);
            imvIcon.setImageResource(model.getIcon());
            txtTitle.setText(model.getTitle());
        }
        return view;
    }
}
