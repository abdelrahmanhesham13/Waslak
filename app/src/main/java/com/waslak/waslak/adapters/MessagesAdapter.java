package com.waslak.waslak.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.waslak.waslak.R;
import com.waslak.waslak.models.ChatModel;
import com.waslak.waslak.models.MessageModel;
import com.waslak.waslak.models.UserModel;
import com.waslak.waslak.utils.Helper;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private ArrayList<MessageModel> mMessagesModel;
    private OnItemClicked onItemClicked;
    private UserModel fromUser;


    public MessagesAdapter(Context mContext, ArrayList<MessageModel> mMessagesModel, OnItemClicked onItemClicked,UserModel fromUser) {
        this.mContext = mContext;
        this.mMessagesModel = mMessagesModel;
        this.onItemClicked = onItemClicked;
        this.fromUser = fromUser;
    }

    @Override
    public int getItemViewType(int position) {
        if (mMessagesModel.get(position).isMine()) {
            return 1;
        } else {
            return 2;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_item_date, parent, false);
            return new DateViewHolder(view);
        } else if (viewType == 1) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_item_outgoing, parent, false);
            return new MessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_item_incoming, parent, false);
            return new MessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        if (viewType == 0){
            ((DateViewHolder)holder).mDate.setText(mMessagesModel.get(position).getMessage());
        } else if (viewType == 1) {
            if (URLUtil.isValidUrl(Helper.getUserSharedPreferences(mContext).getImage()))
                Picasso.get().load(Helper.getUserSharedPreferences(mContext).getImage()).fit().centerCrop().error(R.drawable.shop1).into(((MessageViewHolder)holder).mProfileImage);
            else {
                Picasso.get().load("http://www.as.cta3.com/waslk/prod_img/" + Helper.getUserSharedPreferences(mContext).getImage()).fit().centerCrop().error(R.drawable.shop1).into(((MessageViewHolder)holder).mProfileImage);
            }

            if (mMessagesModel.get(position).getType().equals("text") || mMessagesModel.get(position).getType().equals("0")) {
                ((MessageViewHolder)holder).mImage.setVisibility(View.GONE);
                ((MessageViewHolder)holder).mMessageText.setVisibility(View.VISIBLE);
                ((MessageViewHolder) holder).mMessageText.setText(mMessagesModel.get(position).getMessage());
            } else {
                ((MessageViewHolder)holder).mImage.setVisibility(View.VISIBLE);
                ((MessageViewHolder)holder).mMessageText.setVisibility(View.GONE);
                Picasso.get().load("http://www.as.cta3.com/waslk/prod_img/" + mMessagesModel.get(position).getMessage()).into(((MessageViewHolder)holder).mImage);
            }
        } else {
            if (URLUtil.isValidUrl(fromUser.getImage())) {
                Helper.writeToLog(fromUser.getImage());
                Picasso.get().load(fromUser.getImage()).fit().centerCrop().error(R.drawable.shop1).into(((MessageViewHolder) holder).mProfileImage);
            } else {
                Helper.writeToLog(fromUser.getImage());
                Picasso.get().load("http://www.as.cta3.com/waslk/prod_img/" + fromUser.getImage()).fit().centerCrop().error(R.drawable.shop1).into(((MessageViewHolder)holder).mProfileImage);
            }

            if (mMessagesModel.get(position).getType().equals("text") || mMessagesModel.get(position).getType().equals("0")) {
                ((MessageViewHolder)holder).mImage.setVisibility(View.GONE);
                ((MessageViewHolder)holder).mMessageText.setVisibility(View.VISIBLE);
                ((MessageViewHolder) holder).mMessageText.setText(mMessagesModel.get(position).getMessage());
            } else {
                ((MessageViewHolder)holder).mImage.setVisibility(View.VISIBLE);
                ((MessageViewHolder)holder).mMessageText.setVisibility(View.GONE);
                Picasso.get().load("http://www.as.cta3.com/waslk/prod_img/" + mMessagesModel.get(position).getMessage()).into(((MessageViewHolder)holder).mImage);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mMessagesModel.size();
    }



    public class MessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.message_text)
        TextView mMessageText;
        @BindView(R.id.profile_image)
        ImageView mProfileImage;
        @BindView(R.id.image)
        ImageView mImage;

        MessageViewHolder (View itemView){
            super(itemView);
            ButterKnife.bind(this,itemView);
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            onItemClicked.setOnItemClicked(getAdapterPosition());
        }
    }


    public class DateViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.message_date)
        TextView mDate;

        DateViewHolder (View itemView){
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

    public void setFromUser(UserModel fromUser) {
        this.fromUser = fromUser;
    }
}
