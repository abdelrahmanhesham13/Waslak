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
import com.waslak.waslak.models.PendingOrderModel;
import com.waslak.waslak.models.RequestModel;
import com.waslak.waslak.models.UserModel;
import com.waslak.waslak.networkUtils.Constants;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PendingOrdersAdapter extends RecyclerView.Adapter<PendingOrdersAdapter.PendingOrderViewHolder> {

    private Context mContext;
    private ArrayList<RequestModel> mRequestModels;
    private OnItemClicked onItemClicked;
    private UserModel mUserModel;
    private int flag;

    public PendingOrdersAdapter(Context mContext, ArrayList<RequestModel> mRequestModels, OnItemClicked onItemClicked, UserModel mUserModel,int flag) {
        this.mContext = mContext;
        this.mRequestModels = mRequestModels;
        this.onItemClicked = onItemClicked;
        this.mUserModel = mUserModel;
        this.flag = flag;
    }

    @NonNull
    @Override
    public PendingOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.pending_order_item, parent,false);
        return new PendingOrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PendingOrderViewHolder holder, int position) {
        if (flag == 1) {
            holder.deliveryDate.setVisibility(View.GONE);
            holder.mDeliveryDateText.setVisibility(View.GONE);
        } else {
            holder.deliveryDate.setText(mRequestModels.get(position).getDuration());
            holder.deliveryDate.setVisibility(View.VISIBLE);
            holder.mDeliveryDateText.setVisibility(View.VISIBLE);
        }

        holder.description.setText(mRequestModels.get(position).getDescription());
        if (mRequestModels.get(position).getUser_id().equals(mUserModel.getId()))
            holder.type.setText(mContext.getString(R.string.my_orders));
        else
            holder.type.setText(mContext.getString(R.string.customer_orders));
        holder.name.setText(mRequestModels.get(position).getShopName());
        try {
            holder.mParentLayout.setVisibility(View.VISIBLE);
            holder.distance.setText(String.format(Locale.getDefault(), "%.2f KM", Double.parseDouble(mRequestModels.get(position).getShop().getDistance())));
        } catch (Exception e) {
            holder.mParentLayout.setVisibility(View.GONE);
            e.printStackTrace();
        }
        if (URLUtil.isValidUrl(mRequestModels.get(position).getUser().getImage()))
            Picasso.get().load(mRequestModels.get(position).getUser().getImage()).fit().centerCrop().into(holder.mImage);
        else {
            Picasso.get().load(Constants.WASLAK_BASE_URL + "/mobile/prod_img/" + mRequestModels.get(position).getUser().getImage()).fit().centerCrop().into(holder.mImage);
        }

        if (mRequestModels.get(position).getShopName().equals("null")){
            holder.name.setText(mContext.getString(R.string.delivery));
            holder.mParentLayout.setVisibility(View.GONE);
        } else {
            //holder.mParentLayout.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public int getItemCount() {
        return mRequestModels.size();
    }

    public class PendingOrderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.my_order)
        TextView type;
        @BindView(R.id.description)
        TextView description;
        @BindView(R.id.distance)
        TextView distance;
        @BindView(R.id.delivery_date)
        TextView deliveryDate;
        @BindView(R.id.image)
        ImageView mImage;
        @BindView(R.id.parent_layout)
                View mParentLayout;
        @BindView(R.id.delivery_date_text)
                TextView mDeliveryDateText;

        PendingOrderViewHolder (View itemView){
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
