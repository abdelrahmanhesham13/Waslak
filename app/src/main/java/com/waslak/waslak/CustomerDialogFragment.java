package com.waslak.waslak;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.squareup.picasso.Picasso;
import com.waslak.waslak.models.UserModel;
import com.waslak.waslak.networkUtils.Connector;
import com.waslak.waslak.networkUtils.Constants;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CustomerDialogFragment extends AppCompatDialogFragment {

    public final String TAG = "CustomerDialog";

    @BindView(R.id.customer_comments_parent)
    View mCustomerComments;
    @BindView(R.id.delegate_name)
    TextView mDelegateName;
    @BindView(R.id.imageView1)
    ImageView mDelegateImage;
    @BindView(R.id.rating)
    RatingBar mRatingBar;
    @BindView(R.id.num_of_orders)
    TextView mNumOfOrders;
    @BindView(R.id.comments)
    TextView mComments;
    @BindView(R.id.verify_state)
    TextView mVerifyState;
    @BindView(R.id.featured_account)
            View mFeaturedAccount;

    UserModel mUserModel;

    Connector mConnector;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_customer_dialog, container, false);
        ButterKnife.bind(this,v);

        if (getDialog().getWindow() != null)
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        mCustomerComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(),ReviewsActivity.class).putExtra("user",mUserModel));
            }
        });

        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey("user")) {
                mUserModel = (UserModel) bundle.getSerializable("user");
            }
        }

        mConnector = new Connector(getContext(), new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                if (Connector.checkStatus(response)){
                    mUserModel = Connector.getUser(response);
                    mDelegateName.setText(mUserModel.getName());
                    mRatingBar.setRating(Float.parseFloat(mUserModel.getRating()));
                    mNumOfOrders.setText(mUserModel.getOrders());
                    if (mUserModel.getDelivery().equals("1")) {
                        mVerifyState.setText(getString(R.string.verified_account));
                    } else {
                        mVerifyState.setText(getString(R.string.not_verified_account));
                    }
                    if (mUserModel.getAdvanced().equals("1")) {
                        mFeaturedAccount.setVisibility(View.VISIBLE);
                    } else {
                        mFeaturedAccount.setVisibility(View.GONE);
                    }
                    if (getContext() != null)
                        mComments.setText(String.format("%s %s", mUserModel.getComment(),getContext().getString(R.string.comments)));
                    if (URLUtil.isValidUrl(mUserModel.getImage()))
                        Picasso.get().load(mUserModel.getImage()).fit().centerCrop().into(mDelegateImage);
                    else {
                        Picasso.get().load(Constants.WASLAK_BASE_URL + "/mobile/prod_img/" + mUserModel.getImage()).fit().centerCrop().into(mDelegateImage);
                    }
                } else {
                    dismiss();
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                dismiss();
            }
        });

        mConnector.getRequest(TAG,Connector.createGetUserUrl() + "?id=" + mUserModel.getId());



        return v;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mConnector != null)
            mConnector.cancelAllRequests(TAG);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
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

    public void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("lang", lang).apply();
        if (getActivity() != null)
            getActivity().getResources().updateConfiguration(config,
                    getActivity().getResources().getDisplayMetrics());
    }

}
