package com.waslak.waslak.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.waslak.waslak.R;
import com.waslak.waslak.models.NotificationModel;
import com.waslak.waslak.models.UserModel;
import com.waslak.waslak.utils.Helper;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder> {



    private Context mContext;
    private ArrayList<NotificationModel> mNotificationModels;
    private OnItemClicked onItemClicked;
    UserModel mUserModel;

    public NotificationsAdapter(Context mContext, ArrayList<NotificationModel> mNotificationModels, OnItemClicked onItemClicked) {
        this.mContext = mContext;
        this.mNotificationModels = mNotificationModels;
        this.onItemClicked = onItemClicked;
        mUserModel = Helper.getUserSharedPreferences(mContext);
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_item, parent,false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        if (mUserModel.getId().equals(mNotificationModels.get(position).getUserId()))
            holder.mNotificationText.setText(mNotificationModels.get(position).getText());
        else
            holder.mNotificationText.setText(mNotificationModels.get(position).getTitleDelivery());
    }

    @Override
    public int getItemCount() {
        return mNotificationModels.size();
    }


    public class NotificationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.notification_text)
        TextView mNotificationText;

        NotificationViewHolder (View itemView){
            super(itemView);
            ButterKnife.bind(this,itemView);
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            onItemClicked.setOnItemClicked(getAdapterPosition());
        }
    }


    public interface OnItemClicked{
        void setOnItemClicked(int position);
    }

}


