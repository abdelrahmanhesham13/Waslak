package com.waslak.waslak.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.waslak.waslak.R;
import com.waslak.waslak.models.ReviewModel;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder> {

    private Context mContext;
    private ArrayList<ReviewModel> mReviewsModel;
    private OnItemClicked onItemClicked;

    public ReviewsAdapter(Context mContext, ArrayList<ReviewModel> mReviewsModel, OnItemClicked onItemClicked) {
        this.mContext = mContext;
        this.mReviewsModel = mReviewsModel;
        this.onItemClicked = onItemClicked;
    }


    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.review_item, parent,false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        holder.mComment.setText(mReviewsModel.get(position).getComment());
        holder.mCustomerName.setText(mReviewsModel.get(position).getName());
        holder.mRating.setRating(Float.parseFloat(mReviewsModel.get(position).getRating()));
    }

    @Override
    public int getItemCount() {
        return mReviewsModel.size();
    }


    public class ReviewViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.comment)
        TextView mComment;
        @BindView(R.id.rating)
        RatingBar mRating;
        @BindView(R.id.customer_name)
        TextView mCustomerName;

        ReviewViewHolder (View itemView){
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
