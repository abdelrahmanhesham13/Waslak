package com.waslak.waslak;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.waslak.waslak.networkUtils.Connector;
import com.waslak.waslak.networkUtils.Constants;
import com.waslak.waslak.utils.Helper;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddCouponActivity extends AppCompatActivity {

    private static final String TAG = AddCouponActivity.class.getSimpleName();
    @BindView(R.id.setting_button)
    TextView mSettingButton;
    @BindView(R.id.menu_button)
    ImageView mMenuButton;
    @BindView(R.id.add_coupon)
    Button mAddCouponButton;
    @BindView(R.id.coupon)
    EditText mCouponEditText;

    Connector mConnector;
    ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_coupon);

        Helper.writeToLog(Helper.getPromoSharedPreferences(this));

        mConnector = new Connector(this, new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                mProgressDialog.dismiss();
                if (Connector.checkStatus(response)) {
                    Helper.savePromoSharedPreferences(AddCouponActivity.this,mCouponEditText.getText().toString());
                    Toast.makeText(AddCouponActivity.this,Connector.getMessage(response),Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(AddCouponActivity.this,Connector.getMessage(response),Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                mProgressDialog.dismiss();
                Helper.showSnackBarMessage(getString(R.string.error),AddCouponActivity.this);
            }
        });


        ButterKnife.bind(this);

        mMenuButton.setVisibility(View.GONE);

        mSettingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AddCouponActivity.this,SettingActivity.class));
            }
        });

        mAddCouponButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCouponEditText.getText().toString().isEmpty()){
                    Helper.showSnackBarMessage(getString(R.string.enter_code),AddCouponActivity.this);
                } else {
                    mProgressDialog = Helper.showProgressDialog(AddCouponActivity.this,getString(R.string.loading),false);
                    mConnector.getRequest(TAG, Constants.WASLAK_BASE_URL + "/mobile/api/add_promocode?promo=" + Uri.encode(mCouponEditText.getText().toString()) + "&user_id=" + Helper.getUserSharedPreferences(AddCouponActivity.this).getId());
                }
            }
        });

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
