package com.waslak.waslak;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.android.volley.VolleyError;
import com.waslak.waslak.adapters.ReviewsAdapter;
import com.waslak.waslak.models.ReviewModel;
import com.waslak.waslak.models.UserModel;
import com.waslak.waslak.networkUtils.Connector;
import com.waslak.waslak.utils.Helper;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReviewsActivity extends AppCompatActivity {

    @BindView(R.id.reviews)
    RecyclerView mReviews;

    ArrayList<ReviewModel> mReviewsModels;
    ReviewsAdapter mAdapter;

    UserModel mUserModel;
    Connector mConnector;

    ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);
        ButterKnife.bind(this);
        setTitle(getString(R.string.customer_reviews));

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (getIntent() != null && getIntent().hasExtra("user"))
            mUserModel = (UserModel) getIntent().getSerializableExtra("user");

        mReviewsModels = new ArrayList<>();


        mAdapter = new ReviewsAdapter(this, mReviewsModels, new ReviewsAdapter.OnItemClicked() {
            @Override
            public void setOnItemClicked(int position) {

            }
        });

        mReviews.setHasFixedSize(true);
        mReviews.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mReviews.setAdapter(mAdapter);

        mConnector = new Connector(this, new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                mProgressDialog.dismiss();
                if (Connector.checkStatus(response)){
                    mReviewsModels.addAll(Connector.getReviews(response));
                    mAdapter.notifyDataSetChanged();
                } else {
                    finish();
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                mProgressDialog.dismiss();
                finish();
            }
        });

        mProgressDialog = Helper.showProgressDialog(this,getString(R.string.loading),false);
        if (mUserModel.getId().equals(Helper.getUserSharedPreferences(this).getId())) {
            mConnector.getRequest("ReviewsActivity", Connector.createGetCommentsUrl() + "?user_id=" + mUserModel.getId());
        } else {
            mConnector.getRequest("ReviewsActivity", Connector.createGetCommentsUrl() + "?delivery_id=" + mUserModel.getId());
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return false;
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (mConnector != null)
            mConnector.cancelAllRequests("ReviewsActivity");
    }


    public void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("lang", lang).apply();
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String lang = preferences.getString("lang", "error");
        if (lang.equals("error")) {
            if (Locale.getDefault().getLanguage().equals("ar"))
                setLocale("ar");
            else
                setLocale("en");
        } else if (lang.equals("en")) {
            setLocale("en");
        } else {
            setLocale("ar");
        }
    }


}
