package com.ldz.fpt.businesscardscannerandroid.activity.result_activity;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.ldz.fpt.businesscardscannerandroid.R;

import java.util.List;

/**
 * Created by linhdq on 7/10/17.
 */

public class ListItemInfoAdapter extends RecyclerView.Adapter<ListItemInfoAdapter.ViewHolder> {
    private Context context;
    private LayoutInflater inflater;
    private List<String> list;

    public ListItemInfoAdapter(Context context, List<String> list) {
        this.context = context;
        this.list = list;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_on_list_info, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (position < list.size()) {
            holder.txtContent.setText(list.get(position));
            holder.position = position;
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    protected class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        //view
        private TextView txtContent;
        private ImageView btnEdit;
        private ImageView btnDelete;
        private ImageView btnDone;
        private EditText edtContent;
        private ViewSwitcher viewSwitcher;
        //
        int position;

        public ViewHolder(View itemView) {
            super(itemView);
            //view
            txtContent = (TextView) itemView.findViewById(R.id.txt_content);
            btnEdit = (ImageView) itemView.findViewById(R.id.btn_edit);
            btnDelete = (ImageView) itemView.findViewById(R.id.btn_delete);
            btnDone = (ImageView) itemView.findViewById(R.id.btn_done);
            edtContent = (EditText) itemView.findViewById(R.id.edt_content);
            viewSwitcher = (ViewSwitcher) itemView.findViewById(R.id.view_switcher);
            //
            btnEdit.setOnClickListener(this);
            btnDone.setOnClickListener(this);
            btnDelete.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_edit:
                    edtContent.setText(txtContent.getText().toString());
                    viewSwitcher.setDisplayedChild(1);
                    break;
                case R.id.btn_delete:
                    list.remove(position);
                    ListItemInfoAdapter.this.notifyDataSetChanged();
                    break;
                case R.id.btn_done:
                    list.set(position, edtContent.getText().toString());
                    txtContent.setText(list.get(position));
                    viewSwitcher.setDisplayedChild(0);
                    break;
                default:
                    break;
            }
        }
    }
}
