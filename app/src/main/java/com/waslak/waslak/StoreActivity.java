package com.waslak.waslak;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Location;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.waslak.waslak.adapters.FragmentAdapter;
import com.waslak.waslak.adapters.StoresFragmentAdapter;
import com.waslak.waslak.models.ShopModel;
import com.waslak.waslak.networkUtils.Connector;
import com.waslak.waslak.utils.GPSTracker;
import com.waslak.waslak.utils.Helper;

import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StoreActivity extends AppCompatActivity {

    private static final String TAG = StoreActivity.class.getSimpleName();
    @BindView(R.id.view_pager)
    ViewPager mViewPager;
    @BindView(R.id.tab)
    TabLayout mTabLayout;
    @BindView(R.id.back_button)
    ImageView mBackButton;
    @BindView(R.id.store_name)
    TextView mStoreName;
    @BindView(R.id.store_distance)
    TextView mStoreDistance;
    @BindView(R.id.menu_button)
    ImageView mShareButton;

    ShopModel shopModel;
    Connector mConnector;
    GPSTracker mTracker;
    StoresFragmentAdapter fragmentPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);
        ButterKnife.bind(this);

        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        // Get deep link from result (may be null if no link is found)
                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
                            Helper.writeToLog(deepLink.toString());
                            String url = deepLink.getQueryParameter("link");
                            if (url != null) {
                                Uri uri = Uri.parse(url);
                                Helper.writeToLog(String.valueOf(deepLink));
                                mConnector.getRequest(TAG, Connector.createGetShopUrl(uri.getQueryParameter("id")));
                            } else {
                                mConnector.getRequest(TAG, Connector.createGetShopUrl(deepLink.getQueryParameter("id")));
                            }

                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "getDynamicLink:onFailure", e);
                    }
                });

        fragmentPagerAdapter = new StoresFragmentAdapter(getSupportFragmentManager(), this);


        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (getIntent() != null) {
            if (getIntent().hasExtra("ShopModel")) {
                shopModel = (ShopModel) getIntent().getSerializableExtra("ShopModel");
                mStoreName.setText(shopModel.getName());
                mStoreDistance.setText(String.format(Locale.getDefault(), "%.2f " + getString(R.string.km), Double.valueOf(shopModel.getDistance())));
                mViewPager.setAdapter(fragmentPagerAdapter);
                mTabLayout.setupWithViewPager(mViewPager);
            } else {
                shopModel = new ShopModel();
            }
        } else {
            shopModel = new ShopModel();
        }

        mShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shortLink(createDynamicUri(createShareUri()));
            }
        });


        mConnector = new Connector(this, new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                if (Connector.checkStatus(response)) {
                    shopModel = Connector.getShop(response, shopModel);
                    mStoreName.setText(shopModel.getName());
                    getLocation();
                } else {
                    Helper.showSnackBarMessage(getString(R.string.error), StoreActivity.this);
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                Helper.showSnackBarMessage(getString(R.string.error), StoreActivity.this);
            }
        });


    }

    public void getLocation() {
        mTracker = new GPSTracker(this, new GPSTracker.OnGetLocation() {
            @Override
            public void onGetLocation(double longtiude, double lantitude) {
                if (longtiude != 0 && lantitude != 0) {
                    mTracker.stopUsingGPS();
                    Location location = new Location("");
                    location.setLongitude(longtiude);
                    location.setLatitude(lantitude);

                    Location store = new Location("");
                    store.setLatitude(Double.parseDouble(shopModel.getLat()));
                    store.setLongitude(Double.parseDouble(shopModel.getLon()));

                    double distance = store.distanceTo(location) / 1000.0;
                    shopModel.setDistance(String.valueOf(distance));
                    getIntent().putExtra("ShopModel", shopModel);
                    mStoreDistance.setText(String.format(Locale.getDefault(), "%.2f " + getString(R.string.km), Double.valueOf(shopModel.getDistance())));
                    mViewPager.setAdapter(fragmentPagerAdapter);
                    mTabLayout.setupWithViewPager(mViewPager);
                }
            }
        });

        if (mTracker.canGetLocation()) {
            Location location = mTracker.getLocation();
            if (location != null)
                if (location.getLatitude() != 0 && location.getLongitude() != 0) {
                    mTracker.stopUsingGPS();

                    Location store = new Location("");
                    store.setLatitude(Double.parseDouble(shopModel.getLat()));
                    store.setLongitude(Double.parseDouble(shopModel.getLon()));

                    double distance = store.distanceTo(location) / 1000.0;
                    shopModel.setDistance(String.valueOf(distance));
                    mStoreDistance.setText(String.format(Locale.getDefault(), "%.2f " + getString(R.string.km), Double.valueOf(shopModel.getDistance())));
                    getIntent().putExtra("ShopModel", shopModel);
                    mViewPager.setAdapter(fragmentPagerAdapter);
                    mTabLayout.setupWithViewPager(mViewPager);
                }
        }
    }

    private Uri createShareUri() {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http") // "http"
                .authority("waslk.com") // "365salads.xyz"
                .appendPath("shops") // "salads"
                .appendQueryParameter("id", shopModel.getId());
        Helper.writeToLog(builder.toString());
        return builder.build();
    }

    private Uri createDynamicUri(Uri myUri) {
        DynamicLink dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(myUri)
                .setDomainUriPrefix("https://waslk.page.link")
                // Open links with this app on Android
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                .setSocialMetaTagParameters(
                        new DynamicLink.SocialMetaTagParameters.Builder()
                                .setTitle("Waslak | وصلك")
                                .setDescription("وصلك هو تطبيق لتوصيل الطلبات من مختلف المتاجر")
                                .build())

                // Open links with com.example.ios on iOS
                .buildDynamicLink();

        Uri dynamicLinkUri = dynamicLink.getUri();
        Helper.writeToLog(dynamicLink.toString());
        return dynamicLinkUri;
    }

    public void shortLink(Uri uri) {
        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(uri)
                .setDomainUriPrefix("https://waslk.page.link")
                .buildShortDynamicLink()
                .addOnCompleteListener(this, new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if (task.isSuccessful()) {
                            // Short link created
                            Uri shortLink = task.getResult().getShortLink();
                            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                            sharingIntent.setType("text/plain");
                            String shareBody = "Hey, Check the " + shopModel.getName() + " Restaurant on Waslk " + shortLink;
                            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, shopModel.getName());
                            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                            startActivity(Intent.createChooser(sharingIntent, "Share via"));
                        } else {
                            // Error
                            // ...
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


    @Override
    public void onStop() {
        super.onStop();
        if (mConnector != null)
            mConnector.cancelAllRequests(TAG);
    }

}
