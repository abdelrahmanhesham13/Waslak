package com.waslak.waslak;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.squareup.picasso.Picasso;
import com.waslak.waslak.models.UserModel;
import com.waslak.waslak.networkUtils.Connector;
import com.waslak.waslak.networkUtils.Constants;
import com.waslak.waslak.utils.Helper;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 */
public class UserFragment extends Fragment {

    @BindView(R.id.edit_profile_button)
    Button mEditProfileButton;
    @BindView(R.id.add_coupon_button)
    Button mAddCouponButton;
    @BindView(R.id.setting_button)
    TextView mSettingButton;
    @BindView(R.id.menu_button)
    ImageView mMenuButton;
    @BindView(R.id.profile_image)
    ImageView mProfileImage;
    @BindView(R.id.name)
    TextView mName;
    @BindView(R.id.comments_number)
    TextView mCommentsNumber;
    @BindView(R.id.comments)
    View mCommentsParent;
    @BindView(R.id.orders_number)
    TextView mOrdersNumber;
    @BindView(R.id.orders)
    View mOrdersParent;
    @BindView(R.id.logout_button)
    Button mLogoutButton;
    @BindView(R.id.rating_bar)
    RatingBar mRatingBar;
    @BindView(R.id.authorized_acc)
    TextView mAuthorizedAcc;
    @BindView(R.id.balance)
    TextView mBalanceTextView;
    @BindView(R.id.attach_receipt)
    TextView mAttachReceipt;
    @BindView(R.id.featured_account)
    TextView mFeaturedAccount;

    OnMenuClicked mOnMenuClicked;

    UserModel mUserModel;
    private Connector mConnector;
    Context context;

    private final String TAG = UserFragment.class.getSimpleName();

    ProgressDialog mProgressDialog;

    public UserFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user, container, false);
        ButterKnife.bind(this, view);
        mEditProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AccountActivity.class).putExtra("Type", "Edit").putExtra("user", mUserModel));
            }
        });

        mAddCouponButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddCouponActivity.class));
            }
        });

        mSettingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), SettingActivity.class));
            }
        });

        mMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnMenuClicked.setOnMenuClicked();
            }
        });


        mCommentsParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), ReviewsActivity.class).putExtra("user", mUserModel));
            }
        });

        mLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.removeUserFromSharedPreferences(context);
                startActivity(new Intent(getActivity(), LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            }
        });

        mAttachReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(),WebViewActivity.class).putExtra("type","recharge"));
            }
        });

        mConnector = new Connector(context, new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                if (getActivity() != null)
                    if (mProgressDialog != null && mProgressDialog.isShowing() && !getActivity().isFinishing())
                        mProgressDialog.dismiss();
                if (Connector.checkStatus(response)) {
                    mUserModel = Connector.getUser(response);
                    Helper.SaveToSharedPreferences(context, mUserModel);
                    mName.setText(mUserModel.getName());
                    mCommentsNumber.setText(mUserModel.getComment());
                    mOrdersNumber.setText(mUserModel.getOrders());
                    mBalanceTextView.setText(mUserModel.getCredit());
                    if (!mUserModel.getRating().isEmpty())
                        mRatingBar.setRating(Float.parseFloat(mUserModel.getRating()));
                    if (mUserModel.getDelivery().equals("1")) {
                        mAuthorizedAcc.setText(context.getString(R.string.verified_account));
                    } else {
                        mAuthorizedAcc.setText(context.getString(R.string.not_verified_account));
                    }
                    if (mUserModel.getAdvanced().equals("1")) {
                        mFeaturedAccount.setVisibility(View.VISIBLE);
                    } else {
                        mFeaturedAccount.setVisibility(View.GONE);
                    }
                    if (context != null) {
                        if (URLUtil.isValidUrl(mUserModel.getImage()))
                            Picasso.get().load(mUserModel.getImage()).fit().centerCrop().into(mProfileImage);
                        else {
                            Picasso.get().load(Constants.WASLAK_BASE_URL + "/mobile/prod_img/" + mUserModel.getImage()).fit().centerCrop().into(mProfileImage);
                        }
                    }
                } else {
                    if (getActivity() != null)
                        Helper.showSnackBarMessage(context.getString(R.string.not_registered_mobile), (AppCompatActivity) getActivity());
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                if (mProgressDialog != null && mProgressDialog.isShowing())
                    mProgressDialog.dismiss();
                if (getActivity() != null)
                    Helper.showSnackBarMessage(context.getString(R.string.error), (AppCompatActivity) getActivity());
            }
        });


        return view;
    }

    public interface OnMenuClicked {
        void setOnMenuClicked();
    }


    public void setOnMenuClicked(OnMenuClicked mOnMenuClicked) {
        this.mOnMenuClicked = mOnMenuClicked;
    }


    public void showAlertDialog() {
        if (context != null) {
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
            } else {
                builder = new AlertDialog.Builder(context);
            }
            builder.setTitle("Your account isn't verified")
                    .setMessage("your account isn't verified if you want to be a courier in waslak you should verify your account?")
                    .setPositiveButton("Verify", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() != null) {
            if (getActivity().getIntent() != null) {
                mUserModel = (UserModel) getActivity().getIntent().getSerializableExtra("user");
                mProgressDialog = Helper.showProgressDialog(context, context.getString(R.string.loading), false);
                String url = Connector.createSignInUrl().build().toString() + "?username=" + Helper.getUserSharedPreferences(context).getUsername() + "&token=" + Helper.getTokenFromSharedPreferences(context);
                mConnector.getRequest(TAG, url);
            }
        }
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
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
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("lang", lang).apply();
        if (context != null)
            context.getResources().updateConfiguration(config,
                    context.getResources().getDisplayMetrics());
    }


    @Override
    public void onStop() {
        super.onStop();
        if (mConnector != null)
            mConnector.cancelAllRequests(TAG);
    }
}
