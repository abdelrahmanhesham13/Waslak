package com.waslak.waslak.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.waslak.waslak.R;
import com.waslak.waslak.models.ShopModel;
import com.waslak.waslak.models.StoreModel;
import com.waslak.waslak.utils.Helper;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StoresAdapter extends RecyclerView.Adapter<StoresAdapter.StoreViewHolder> {


    private Context mContext;
    private ArrayList<ShopModel> mShopModels;
    private OnItemClicked onItemClicked;

    public StoresAdapter(Context mContext, ArrayList<ShopModel> mShopModels, OnItemClicked onItemClicked) {
        this.mContext = mContext;
        this.mShopModels = mShopModels;
        this.onItemClicked = onItemClicked;
    }

    @NonNull
    @Override
    public StoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.store_item, parent, false);
        return new StoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoreViewHolder holder, int position) {
        if (mShopModels.get(position).getDistance() != null) {
            holder.distance.setText(String.format(Locale.ENGLISH, "%.2f KM", Double.parseDouble(mShopModels.get(position).getDistance())));
        }
        if (URLUtil.isValidUrl(mShopModels.get(position).getImage())) {
            Uri uri = Uri.parse(mShopModels.get(position).getImage());
            String ref = uri.getQueryParameter("photoreference");
            Helper.writeToLog("https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + ref + "&key=AIzaSyChKwGm9z5bnNLPnzjCKkdbQl2owplxYvQ");
            if (ref != null)
                Picasso.get().load("https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + ref + "&key=AIzaSyA-39lOKwrCfBv1N31ofGpgeCeh6KK0va4").fit().centerCrop().error(R.drawable.shop1).into(holder.image, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                    }
                });
            else
                Picasso.get().load(mShopModels.get(position).getImage()).fit().centerCrop().error(R.drawable.shop1).into(holder.image, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                    }
                });
        } else {
            Picasso.get().load("http://www.cta3.com/waslk/prod_img/" + mShopModels.get(position).getImage()).fit().centerCrop().error(R.drawable.shop1).into(holder.image, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                }
            });
        }
        holder.title.setText(mShopModels.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return mShopModels.size();
    }


    public class StoreViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.distance)
        TextView distance;
        @BindView(R.id.order_image)
        ImageView image;
        @BindView(R.id.title)
        TextView title;

        StoreViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            onItemClicked.setOnItemClicked(getAdapterPosition());
        }

    }


    public void setShopModels(ArrayList<ShopModel> mShopModels) {
        this.mShopModels = mShopModels;
    }

    public interface OnItemClicked {
        void setOnItemClicked(int position);
    }
}
