package com.ldz.fpt.businesscardscannerandroid.activity.home_activity;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ldz.fpt.businesscardscannerandroid.R;
import com.ldz.fpt.businesscardscannerandroid.database.model.CardHistoryModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linhdq on 4/20/17.
 */

public class ListCardAdapter extends RecyclerView.Adapter<ListCardAdapter.ViewHolder> {
    //
    private List<CardHistoryModel> list;
    private Context context;
    private LayoutInflater inflater;
    private CardHistoryModel model;

    public ListCardAdapter(List<CardHistoryModel> list, Context context) {
        if (list != null) {
            this.list = list;
        } else {
            this.list = new ArrayList<>();
        }
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_on_list_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (list.size() > position) {
            model = list.get(position);
            if (model != null) {
                if (model.getCardUri().isEmpty()) {
                    Glide.with(context).load(Base64.decode(model.getCardImageBase64().getBytes(), Base64.DEFAULT)).into(holder.imageView);
                } else {
                    Glide.with(context).load(model.getCardUri()).into(holder.imageView);
                }
                holder.txtName.setText(model.getContactName());
                holder.txtPhoneNumber.setText(model.getPhoneNumber());
                holder.txtEmail.setText(model.getEmailAddress());
            }
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        //
        private ImageView imageView;
        private TextView txtName;
        private TextView txtPhoneNumber;
        private TextView txtEmail;

        public ViewHolder(View itemView) {
            super(itemView);
            //
            imageView = (ImageView) itemView.findViewById(R.id.imv_image);
            txtName = (TextView) itemView.findViewById(R.id.txt_name);
            txtPhoneNumber = (TextView) itemView.findViewById(R.id.txt_phone_number);
            txtEmail = (TextView) itemView.findViewById(R.id.txt_email);
        }
    }
}
