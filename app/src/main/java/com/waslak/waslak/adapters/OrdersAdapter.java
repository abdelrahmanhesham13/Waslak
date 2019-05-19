package com.waslak.waslak.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.waslak.waslak.R;
import com.waslak.waslak.models.OrderModel;
import com.waslak.waslak.models.RequestModel;
import com.waslak.waslak.utils.Helper;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {


    private Context mContext;
    private ArrayList<RequestModel> mOrderModels;
    private OnItemClicked onItemClicked;


    public OrdersAdapter(Context mContext, ArrayList<RequestModel> mOrderModels, OnItemClicked onItemClicked) {
        this.mContext = mContext;
        this.mOrderModels = mOrderModels;
        this.onItemClicked = onItemClicked;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.order_item, parent,false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        holder.itemView.setTag(position);
        if (!mOrderModels.get(position).getDelivery().getName().equals("null")) {
            holder.mDeliveryName.setVisibility(View.VISIBLE);
            holder.mDeliveryName.setText(Html.fromHtml(mContext.getString(R.string.delivered_by) + mOrderModels.get(position).getDelivery().getName()));
        } else {
            holder.mDeliveryName.setVisibility(View.GONE);
        }

        holder.mDeliveryState.setText(mOrderModels.get(position).getNote());
        holder.mExpireDate.setText(Html.fromHtml(mContext.getString(R.string.delivered_within) + mOrderModels.get(position).getDuration()));
        holder.mPrice.setText(mOrderModels.get(position).getPrice());
        holder.mState.setText(mContext.getString(R.string.approved));
        holder.mTitle.setText(String.format("%s %s", mContext.getString(R.string.shop_name), mOrderModels.get(position).getShop().getName()));
        if (URLUtil.isValidUrl(mOrderModels.get(position).getImage())) {
            Uri uri = Uri.parse(mOrderModels.get(position).getImage());
            String ref = uri.getQueryParameter("photoreference");
            Helper.writeToLog("https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + ref + "&key=AIzaSyChKwGm9z5bnNLPnzjCKkdbQl2owplxYvQ");
            if (ref != null)
                Picasso.get().load("https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + ref + "&key=AIzaSyChKwGm9z5bnNLPnzjCKkdbQl2owplxYvQ").fit().centerCrop().error(R.drawable.shop1).into(holder.mImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                    }
                });
            else
                Picasso.get().load(mOrderModels.get(position).getImage()).fit().centerCrop().error(R.drawable.shop1).into(holder.mImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                    }
                });
        } else {
            Picasso.get().load("http://www.as.cta3.com/waslk/prod_img/" + mOrderModels.get(position).getImage()).fit().centerCrop().error(R.drawable.shop1).into(holder.mImage, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mOrderModels.size();
    }

    public class OrderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.order_image)
        ImageView mImage;
        @BindView(R.id.title)
        TextView mTitle;
        @BindView(R.id.state)
        TextView mState;
        @BindView(R.id.expire_date)
        TextView mExpireDate;
        @BindView(R.id.delivery_name)
        TextView mDeliveryName;
        @BindView(R.id.delivery_state)
        TextView mDeliveryState;
        @BindView(R.id.price)
        TextView mPrice;

        OrderViewHolder (View itemView){
            super(itemView);
            ButterKnife.bind(this, itemView);
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
