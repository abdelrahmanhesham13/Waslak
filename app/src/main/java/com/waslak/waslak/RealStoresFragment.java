package com.waslak.waslak;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.android.volley.VolleyError;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.waslak.waslak.adapters.StoresAdapter;
import com.waslak.waslak.models.ShopModel;
import com.waslak.waslak.models.StoreModel;
import com.waslak.waslak.models.UserModel;
import com.waslak.waslak.networkUtils.Connector;
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

import static android.content.Context.LOCATION_SERVICE;


/**
 * A simple {@link Fragment} subclass.
 */
public class RealStoresFragment extends Fragment {

    private static final String TAG = "RealStoresFragment";
    @BindView(R.id.stores)
    RecyclerView mShopsRecycler;
    @BindView(R.id.progressIndicator)
    ProgressBar mProgressBar;

    ArrayList<ShopModel> mShopModels;
    StoresAdapter mAdapter;

    Connector mConnector;

    GPSTracker mTracker;
    GPSTracker mTrackerGetLocation;

    UserModel mUserModel;

    ProgressDialog mProgressDialog;

    boolean mLocated = false;
    boolean mLocatedGetLocation = false;

    String mFullAddress;
    String mCountry;
    String mCity;
    String mType;


    public RealStoresFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_real_stores, container, false);
        ButterKnife.bind(this, view);

        mShopModels = new ArrayList<>();
        if (getArguments() != null) {
            mType = getArguments().getString("Type");
        }

        if (getActivity() != null)
            mUserModel = (UserModel) getActivity().getIntent().getSerializableExtra("user");

        mUserModel = Helper.getUserSharedPreferences(getContext());

        mConnector = new Connector(getContext(), new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                if (Connector.checkStatus(response)) {
                    mShopModels.addAll(Connector.getShops(response));
                    mAdapter.notifyDataSetChanged();
                    adjustDistances();
                } else {
                    if (getActivity() != null)
                        Helper.showSnackBarMessage(getString(R.string.error), (AppCompatActivity) getActivity());
                    mProgressBar.setVisibility(View.INVISIBLE);
                    mShopsRecycler.setVisibility(View.VISIBLE);
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                if (getActivity() != null)
                    Helper.showSnackBarMessage(getString(R.string.error), (AppCompatActivity) getActivity());
                mProgressBar.setVisibility(View.INVISIBLE);
                mShopsRecycler.setVisibility(View.VISIBLE);
            }
        });


        mAdapter = new StoresAdapter(getContext(), mShopModels, new StoresAdapter.OnItemClicked() {
            @Override
            public void setOnItemClicked(int position) {
                startActivity(new Intent(getContext(), StoreActivity.class).putExtra("ShopModel", mShopModels.get(position)).putExtra("user", mUserModel));
            }
        });

        mShopsRecycler.setHasFixedSize(true);
        mShopsRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mShopsRecycler.setAdapter(mAdapter);


        return view;
    }


    public void adjustDistances() {
        mTracker = new GPSTracker(getContext(), new GPSTracker.OnGetLocation() {
            @Override
            public void onGetLocation(double lat, double lon) {
                Location location = new Location("MyLocation");
                location.setLongitude(lon);
                location.setLatitude(lat);
                if (location.getLongitude() != 0 && location.getLatitude() != 0 && !mLocated) {
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
                    mLocated = true;
                    mAdapter.notifyDataSetChanged();
                    adjustArrayList(mShopModels);
                }
            }
        });
        if (mTracker.canGetLocation()) {
            Location location = mTracker.getLocation();
            if (location != null) {
                if (location.getLongitude() != 0 && location.getLatitude() != 0 && !mLocated) {
                    for (int i = 0; i < mShopModels.size(); i++) {
                        Location location1 = new Location("ShopLocation");
                        location1.setLatitude(Double.parseDouble(mShopModels.get(i).getLat()));
                        location1.setLongitude(Double.parseDouble(mShopModels.get(i).getLon()));

                        double distance = location.distanceTo(location1);
                        mShopModels.get(i).setDistance(String.valueOf(distance / 1000.0));
                    }
                    mAdapter.notifyDataSetChanged();
                    mProgressBar.setVisibility(View.INVISIBLE);
                    mShopsRecycler.setVisibility(View.VISIBLE);
                    mTracker.stopUsingGPS();
                    mLocated = true;
                    mAdapter.notifyDataSetChanged();
                    adjustArrayList(mShopModels);
                }
            }
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        mConnector.cancelAllRequests(TAG);
    }


    public void adjustArrayList(ArrayList<ShopModel> shopModels) {
        ArrayList<ShopModel> mShopModelsCopy = new ArrayList<>(shopModels);
        if (getArguments() != null) {
            String type = getArguments().getString("Type");
            if (type != null) {
                switch (type) {
                    case "All":
                        mAdapter.setShopModels(mShopModels);
                        mAdapter.notifyDataSetChanged();
                        break;
                    case "Active":
                        mShopModels.clear();
                        for (int i = 0; i < mShopModelsCopy.size(); i++) {
                            if (mShopModelsCopy.get(i).getApproved().equals("1"))
                                mShopModels.add(mShopModelsCopy.get(i));
                        }
                        mAdapter.setShopModels(mShopModels);
                        mAdapter.notifyDataSetChanged();
                        break;
                    case "Nearby":
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
                        break;
                }
            }
        }
    }


    public void getLocation() {
        mTrackerGetLocation = new GPSTracker(getContext(), new GPSTracker.OnGetLocation() {
            @Override
            public void onGetLocation(double lat, double lon) {
                if (lon != 0 && lat != 0 && !mLocatedGetLocation) {
                    Log.i(TAG, "onGetLocation");
                    mLocatedGetLocation = true;
                    List<Address> addresses = getAddress(lat, lon);
                    if (addresses != null && addresses.size() > 0) {
                        if (mUserModel != null) {
                            if (mType.equals("Nearby"))
                                mConnector.getRequest(TAG, Connector.createGetShopsUrl() + "?user_id=" + mUserModel.getId() + "&lat=" + lat + "&long=" + lon + "&city_id=" + Uri.encode(addresses.get(0).getAdminArea()) + "&country=" + Uri.encode(addresses.get(0).getCountryName()));
                            else
                                mConnector.getRequest(TAG, Connector.createGetShopsUrl() + "?user_id=" + mUserModel.getId());
                        } else {
                            Helper.showLongTimeToast(getContext(),getString(R.string.error));
                        }
                    }
                    mTrackerGetLocation.stopUsingGPS();
                }
            }
        });

        if (mTrackerGetLocation.canGetLocation()) {
            Location location = mTrackerGetLocation.getLocation();
            if (location != null) {
                if (location.getLongitude() != 0 && location.getLatitude() != 0 && !mLocatedGetLocation) {
                    Log.i(TAG, "canGetLocation");
                    mLocatedGetLocation = true;
                    List<Address> addresses = getAddress(location.getLatitude(), location.getLongitude());
                    if (addresses != null && addresses.size() > 0) {
                        if (mType.equals("Nearby"))
                            mConnector.getRequest(TAG, Connector.createGetShopsUrl() + "?user_id=" + mUserModel.getId() + "&lat=" + location.getLatitude() + "&long=" + location.getLongitude() + "&city_id=" + Uri.encode(addresses.get(0).getAdminArea()) + "&country=" + Uri.encode(addresses.get(0).getCountryName()));
                        else
                            mConnector.getRequest(TAG, Connector.createGetShopsUrl() + "?user_id=" + mUserModel.getId());
                    }
                    mTrackerGetLocation.stopUsingGPS();
                }
            }
        }
    }


    public List<Address> getAddress(final double lat, final double lon) {
        if (Geocoder.isPresent()) {
            Geocoder geocoder;
            List<Address> addresses = null;
            if (getContext() != null) {
                geocoder = new Geocoder(getContext(), Locale.ENGLISH);

                try {
                    addresses = geocoder.getFromLocation(lat, lon, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return addresses;
        } else {
            mProgressBar.setVisibility(View.VISIBLE);
            mShopsRecycler.setVisibility(View.GONE);
            new ReverseGeocoding(lat, lon, "AIzaSyATc3Nte8Pj1oWTFKAbLWUiJbzSIJEDzxc")
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
                            mConnector.getRequest(TAG, Connector.createGetShopsUrl() + "?user_id=" + mUserModel.getId() + "&lat=" + lat + "&long=" + lon + "&city_id=" + Uri.encode(mCity) + "&country=" + Uri.encode(mCountry));

                        }

                        @Override
                        public void onFailed(Response response, IOException e) {

                        }
                    });
            return null;
        }
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


    public boolean isHighAccuracy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int locationMode = 0;
            try {
                locationMode = Settings.Secure.getInt(getContext().getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }

            return (locationMode != Settings.Secure.LOCATION_MODE_OFF && locationMode == Settings.Secure.LOCATION_MODE_HIGH_ACCURACY); //check location mode

        } else {
            String locationProviders = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (isHighAccuracy()) {
            mLocated = false;
            mLocatedGetLocation = false;
            mShopModels.clear();
            mProgressBar.setVisibility(View.VISIBLE);
            mShopsRecycler.setVisibility(View.GONE);
            getLocation();
        }
    }
}
