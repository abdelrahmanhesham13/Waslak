package com.waslak.waslak;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.waslak.waslak.networkUtils.Connector;
import com.waslak.waslak.networkUtils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PackageDeliveryActivity extends AppCompatActivity {

    private static final String TAG = "PackageDeliveryActivity";

    @BindView(R.id.customer_delivery)
    View mCustomerDelivery;
    @BindView(R.id.package_delivery)
    View mPackageDelivery;
    @BindView(R.id.parent_layout)
    View mParentLayout;
    @BindView(R.id.progressIndicator)
    ProgressBar mProgressIndicator;


    Connector mConnectorGetSettings;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_delivery);
        ButterKnife.bind(this);

        setTitle(getString(R.string.delivery_package));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mPackageDelivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PackageDeliveryActivity.this,PackageDeliveryDetailsActivity.class).putExtra("type","package"));
            }
        });


        mCustomerDelivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PackageDeliveryActivity.this,PackageDeliveryDetailsActivity.class).putExtra("type","customer"));
            }
        });


        mConnectorGetSettings = new Connector(this, new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                mProgressIndicator.setVisibility(View.GONE);
                mParentLayout.setVisibility(View.VISIBLE);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean taxi = jsonObject.getBoolean("taxi");
                    if (taxi){
                        mCustomerDelivery.setVisibility(View.VISIBLE);
                    } else {
                        mCustomerDelivery.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                finish();
            }
        });

        mConnectorGetSettings.getRequest(TAG, Constants.WASLAK_BASE_URL + "/mobile/api/get_settings?country=10");

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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return false;
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
