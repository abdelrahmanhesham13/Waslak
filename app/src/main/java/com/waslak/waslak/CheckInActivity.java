package com.waslak.waslak;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.waslak.waslak.adapters.StoresAdapter;
import com.waslak.waslak.models.ShopModel;
import com.waslak.waslak.models.StoreModel;
import com.waslak.waslak.models.UserModel;
import com.waslak.waslak.networkUtils.Connector;
import com.waslak.waslak.utils.GPSTracker;
import com.waslak.waslak.utils.Helper;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CheckInActivity extends AppCompatActivity {


    @BindView(R.id.check_list_recycler)
    RecyclerView mStoresRecycler;
    @BindView(R.id.menu_button)
    ImageView mMenuButton;
    @BindView(R.id.setting_button)
    TextView mSettingButton;

    ArrayList<ShopModel> mStoreModels;
    StoresAdapter mAdapter;

    Connector mConnector;

    GPSTracker mTracker;

    UserModel mUserModel;

    boolean mLocated = false;
    ProgressDialog mProgressDialog;

    public final String TAG = "CheckInActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in);
        finish();
        ButterKnife.bind(this);

        mMenuButton.setVisibility(View.GONE);

        mStoreModels = new ArrayList<>();

        mUserModel = (UserModel) getIntent().getSerializableExtra("user");

        mConnector = new Connector(this, new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                if (Connector.checkStatus(response)){
                    mStoreModels.addAll(Connector.getShops(response));
                    adjustDistances();
                } else {
                    Helper.showSnackBarMessage(getString(R.string.no_check_in_list),CheckInActivity.this);
                    mProgressDialog.dismiss();
                    mStoresRecycler.setVisibility(View.VISIBLE);
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                Helper.showSnackBarMessage(getString(R.string.error),CheckInActivity.this);
                mProgressDialog.dismiss();
                mStoresRecycler.setVisibility(View.VISIBLE);
            }
        });


        mAdapter = new StoresAdapter(this, mStoreModels, new StoresAdapter.OnItemClicked() {
            @Override
            public void setOnItemClicked(int position) {

            }
        });

        mSettingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CheckInActivity.this,SettingActivity.class));
            }
        });


        mProgressDialog = Helper.showProgressDialog(this,getString(R.string.loading),false);
        mStoresRecycler.setVisibility(View.GONE);
        mConnector.getRequest(TAG,Connector.createGetShopsUrl() + "?user_id=" + mUserModel.getId() + "&request=true");


        mStoresRecycler.setHasFixedSize(true);
        mStoresRecycler.setNestedScrollingEnabled(false);
        mStoresRecycler.setFocusable(false);
        mStoresRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mStoresRecycler.setAdapter(mAdapter);
    }



    public void adjustDistances(){
        mTracker = new GPSTracker(this, new GPSTracker.OnGetLocation() {
            @Override
            public void onGetLocation(double lat, double lon) {
                Location location = new Location("MyLocation");
                location.setLongitude(lon);
                location.setLatitude(lat);
                if (location.getLongitude() != 0 && location.getLatitude() != 0 && !mLocated){
                    mLocated = true;
                    for (int i = 0 ;i<mStoreModels.size();i++){
                        Location location1 = new Location("ShopLocation");
                        location1.setLatitude(Double.parseDouble(mStoreModels.get(i).getLat()));
                        location1.setLongitude(Double.parseDouble(mStoreModels.get(i).getLon()));

                        double distance = location.distanceTo(location1);
                        Helper.writeToLog("Distance" + distance);
                        mStoreModels.get(i).setDistance(String.valueOf(distance/1000.0));
                    }
                    mAdapter.notifyDataSetChanged();
                    mProgressDialog.dismiss();
                    mStoresRecycler.setVisibility(View.VISIBLE);
                    mTracker.stopUsingGPS();
                }
            }
        });
        if (mTracker.canGetLocation() && !mLocated){
            Location location = mTracker.getLocation();
            if (location != null){
                if (location.getLongitude() != 0 && location.getLatitude() != 0){
                    mLocated = true;
                    for (int i = 0 ;i<mStoreModels.size();i++){
                        Location location1 = new Location("ShopLocation");
                        location1.setLatitude(Double.parseDouble(mStoreModels.get(i).getLat()));
                        location1.setLongitude(Double.parseDouble(mStoreModels.get(i).getLon()));

                        double distance = location.distanceTo(location1);
                        mStoreModels.get(i).setDistance(String.valueOf(distance/1000.0));
                    }
                    mAdapter.notifyDataSetChanged();
                    mProgressDialog.dismiss();
                    mStoresRecycler.setVisibility(View.VISIBLE);
                    mTracker.stopUsingGPS();
                }
            }
        }
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
