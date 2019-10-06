package com.waslak.waslak;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.android.volley.VolleyError;
import com.waslak.waslak.adapters.StoresAdapter;
import com.waslak.waslak.models.ShopDetailsModel;
import com.waslak.waslak.models.ShopModel;
import com.waslak.waslak.models.UserModel;
import com.waslak.waslak.networkUtils.Connector;
import com.waslak.waslak.networkUtils.Constants;
import com.waslak.waslak.utils.GPSTracker;
import com.waslak.waslak.utils.Helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import se.arbitur.geocoding.Callback;
import se.arbitur.geocoding.Constants.AddressTypes;
import se.arbitur.geocoding.Constants.LocationTypes;
import se.arbitur.geocoding.Response;
import se.arbitur.geocoding.Result;
import se.arbitur.geocoding.ReverseGeocoding;

public class SearchActivity extends AppCompatActivity {

    private static final String TAG = "SearchActivity";
    @BindView(R.id.back_button)
    ImageView mBackButton;
    @BindView(R.id.search_text)
    EditText mSearchText;
    @BindView(R.id.progressIndicator)
    ProgressBar mProgressBar;
    @BindView(R.id.stores)
    RecyclerView mShopsRecycler;

    ArrayList<ShopModel> mShopModels;
    ArrayList<ShopModel> mShopModelsCopy;
    StoresAdapter mAdapter;

    Connector mConnector;
    Connector mConnectorGoogleShops;
    Connector mConnectorAddShop;

    GPSTracker mTracker;
    GPSTracker mTrackerGetLocation;

    UserModel mUserModel;

    String mFullAddress;
    String mCountry;
    String mCity;

    ShopModel mShopModel;
    ShopModel mShopDetailModel;

    ProgressDialog mProgressDialog;

    String mLat;
    String mLon;


    boolean mLocated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        if (getIntent().hasExtra("user"))
            mUserModel = (UserModel) getIntent().getSerializableExtra("user");

        mShopModels = new ArrayList<>();
        mShopModelsCopy = new ArrayList<>();

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        mUserModel = (UserModel) getIntent().getSerializableExtra("user");

        mConnector = new Connector(this, new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                if (Connector.checkStatus(response)) {
                    mLocated = false;
                    mShopModels.clear();
                    mShopModels.addAll(Connector.getShops(response));
                    adjustDistances();
                } else {
                    /*Helper.showSnackBarMessage(getString(R.string.no_matching_stores), SearchActivity.this);
                    mShopModels.clear();
                    mShopModelsCopy.clear();
                    mAdapter.notifyDataSetChanged();
                    mProgressBar.setVisibility(View.INVISIBLE);
                    mShopsRecycler.setVisibility(View.VISIBLE);*/
                    mConnectorGoogleShops.getRequest(TAG, "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + mLat + "," + mLon + "&radius=50000&keyword=" + Uri.encode(mSearchText.getText().toString()) + "&key=" + Constants.API_KEY);

                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                Helper.showSnackBarMessage(getString(R.string.error), SearchActivity.this);
                mProgressBar.setVisibility(View.INVISIBLE);
                mShopsRecycler.setVisibility(View.VISIBLE);
            }
        });

        mConnectorGoogleShops = new Connector(this, new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                mLocated = false;
                mShopModels.clear();
                mShopModels.addAll(Connector.getShopsGoogle(response));
                adjustDistances();
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                Helper.showSnackBarMessage(getString(R.string.error), SearchActivity.this);
                mProgressBar.setVisibility(View.INVISIBLE);
                mShopsRecycler.setVisibility(View.VISIBLE);
            }
        });

        mConnectorAddShop = new Connector(this, new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                mShopDetailModel = Connector.getShopGoogle(response, mShopModel);
                mProgressDialog.dismiss();
                startActivity(new Intent(SearchActivity.this, StoreActivity.class).putExtra("ShopModel", mShopDetailModel).putExtra("user", mUserModel));

            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                mProgressDialog.dismiss();

            }
        });


        mAdapter = new StoresAdapter(this, mShopModels, new StoresAdapter.OnItemClicked() {
            @Override
            public void setOnItemClicked(int position) {
                if (mShopModels.get(position).getId().equals("0")) {
                    mShopModel = mShopModels.get(position);
                    mProgressDialog = Helper.showProgressDialog(SearchActivity.this,getString(R.string.loading),false);
                    mConnectorAddShop.getRequest(TAG, Constants.WASLAK_BASE_URL + "/mobile/api/add_shop?name=" + Uri.encode(mShopModel.getName()) + "&description=description&longitude=" + mShopModel.getLon() + "&latitude=" + mShopModel.getLat() + "&address=" + Uri.encode(mShopModel.getAddress()) + "&city=city&country=country&image=" + Uri.encode(mShopModel.getImage()));
                } else {
                    startActivity(new Intent(SearchActivity.this, StoreActivity.class).putExtra("ShopModel", mShopModels.get(position)).putExtra("user", mUserModel));
                }
            }
        });

        mShopsRecycler.setHasFixedSize(true);
        mShopsRecycler.setLayoutManager(new LinearLayoutManager(SearchActivity.this, LinearLayoutManager.VERTICAL, false));
        mShopsRecycler.setAdapter(mAdapter);

        mSearchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Helper.writeToLog(s.toString());
                if (!s.toString().isEmpty()) {
                    mLocated = false;
                    mProgressBar.setVisibility(View.VISIBLE);
                    mShopsRecycler.setVisibility(View.GONE);
                    mConnector.cancelAllRequests(TAG);
                    mConnectorGoogleShops.cancelAllRequests(TAG);
                    getLocation(s.toString());
                } else {
                    mConnector.cancelAllRequests(TAG);
                    mConnectorGoogleShops.cancelAllRequests(TAG);
                    mProgressBar.setVisibility(View.INVISIBLE);
                    mShopsRecycler.setVisibility(View.VISIBLE);
                    mShopModels.clear();
                    mShopModelsCopy.clear();
                    mAdapter.notifyDataSetChanged();
                }
            }
        });

    }


    public void adjustDistances() {
        mTracker = new GPSTracker(this, new GPSTracker.OnGetLocation() {
            @Override
            public void onGetLocation(double lat, double lon) {
                Location location = new Location("MyLocation");
                location.setLongitude(lon);
                location.setLatitude(lat);
                if (location.getLongitude() != 0 && location.getLatitude() != 0 && !mLocated) {
                    mLocated = true;
                    for (int i = 0; i < mShopModels.size(); i++) {
                        Location location1 = new Location("ShopLocation");
                        location1.setLatitude(Double.parseDouble(mShopModels.get(i).getLat()));
                        location1.setLongitude(Double.parseDouble(mShopModels.get(i).getLon()));

                        double distance = location.distanceTo(location1);
                        Helper.writeToLog("Distance" + distance);
                        mShopModels.get(i).setDistance(String.valueOf(distance / 1000.0));
                    }
                    mAdapter.notifyDataSetChanged();
                    mProgressBar.setVisibility(View.INVISIBLE);
                    mShopsRecycler.setVisibility(View.VISIBLE);
                    mTracker.stopUsingGPS();
                    adjustArrayList(mShopModels);
                }
            }
        });
        if (mTracker.canGetLocation() && !mLocated) {
            Location location = mTracker.getLocation();
            if (location != null) {
                if (location.getLongitude() != 0 && location.getLatitude() != 0) {
                    mLocated = true;
                    for (int i = 0; i < mShopModels.size(); i++) {
                        Location location1 = new Location("ShopLocation");
                        location1.setLatitude(Double.parseDouble(mShopModels.get(i).getLat()));
                        location1.setLongitude(Double.parseDouble(mShopModels.get(i).getLon()));


                        double distance = location.distanceTo(location1);
                        Helper.writeToLog(String.valueOf(distance));
                        mShopModels.get(i).setDistance(String.valueOf(distance / 1000.0));
                    }
                    mAdapter.notifyDataSetChanged();
                    mProgressBar.setVisibility(View.INVISIBLE);
                    mShopsRecycler.setVisibility(View.VISIBLE);
                    mTracker.stopUsingGPS();
                    adjustArrayList(mShopModels);
                }
            }
        }
    }


    public void adjustArrayList(ArrayList<ShopModel> shopModels) {
        ArrayList<ShopModel> mShopModelsCopy = new ArrayList<>(shopModels);
        mShopModels.clear();
        Collections.sort(mShopModelsCopy, new Comparator<ShopModel>() {
            @Override
            public int compare(ShopModel o1, ShopModel o2) {
                return Double.valueOf(o1.getDistance()).compareTo(Double.valueOf(o2.getDistance()));
            }
        });
        mShopModels.addAll(mShopModelsCopy);
        mAdapter.setShopModels(mShopModels);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        super.onStop();
        mConnector.cancelAllRequests(TAG);
    }


    public void getLocation(final String s) {
        mTrackerGetLocation = new GPSTracker(this, new GPSTracker.OnGetLocation() {
            @Override
            public void onGetLocation(double lat, double lon) {
                if (lon != 0 && lat != 0 && !mLocated) {
                    mLocated = true;
                    mLat = String.valueOf(lat);
                    mLon = String.valueOf(lon);
                    List<Address> addresses = getAddress(lat, lon, s);
                    if (addresses != null && addresses.size() > 0) {
                        mProgressBar.setVisibility(View.VISIBLE);
                        mShopsRecycler.setVisibility(View.GONE);
                        mConnector.getRequest(TAG, Connector.createGetShopsUrl() + "?user_id=" + mUserModel.getId() + "&search=" + s);
                    }
                    mTrackerGetLocation.stopUsingGPS();
                }
            }
        });

        if (mTrackerGetLocation.canGetLocation()) {
            Location location = mTrackerGetLocation.getLocation();
            if (location != null) {
                if (location.getLongitude() != 0 && location.getLatitude() != 0 && !mLocated) {
                    mLocated = true;
                    mLat = String.valueOf(location.getLatitude());
                    mLon = String.valueOf(location.getLongitude());
                    List<Address> addresses = getAddress(location.getLatitude(), location.getLongitude(), s);
                    if (addresses != null && addresses.size() > 0) {
                        mProgressBar.setVisibility(View.VISIBLE);
                        mShopsRecycler.setVisibility(View.GONE);
                        mConnector.getRequest(TAG, Connector.createGetShopsUrl() + "?user_id=" + mUserModel.getId() + "&search=" + s);
                    }
                    mTrackerGetLocation.stopUsingGPS();
                }
            }
        }
    }


    public List<Address> getAddress(final double lat, final double lon, final String s) {
        if (Geocoder.isPresent()) {
            Geocoder geocoder;
            List<Address> addresses = null;
            geocoder = new Geocoder(this, Locale.ENGLISH);

            try {
                addresses = geocoder.getFromLocation(lat, lon, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return addresses;
        } else {
            mProgressBar.setVisibility(View.VISIBLE);
            mShopsRecycler.setVisibility(View.GONE);
            new ReverseGeocoding(lat, lon, "AIzaSyCbltU9nU7ZytFzEwJwPdVji-7Y71DV6B8")
                    .setLanguage("en")
                    .fetch(new Callback() {
                        @Override
                        public void onResponse(Response response) {

                            mFullAddress = response.getResults()[0].getFormattedAddress();
                            for (int i = 0; i < response.getResults()[0].getAddressComponents().length; i++) {
                                for (int j = 0; j < response.getResults()[0].getAddressComponents()[i].getAddressTypes().length; j++) {
                                    switch (response.getResults()[0].getAddressComponents()[i].getAddressTypes()[j]) {
                                        case "administrative_area_level_1":
                                            mCity = response.getResults()[0].getAddressComponents()[i].getLongName();
                                            break;
                                        case "country":
                                            mCountry = response.getResults()[0].getAddressComponents()[i].getLongName();
                                            break;
                                    }
                                }
                            }
                            mConnector.getRequest(TAG, Connector.createGetShopsUrl() + "?user_id=" + mUserModel.getId() + "&search=" + s);

                        }

                        @Override
                        public void onFailed(Response response, IOException e) {

                        }
                    });
            return null;
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
