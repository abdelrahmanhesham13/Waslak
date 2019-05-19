package com.waslak.waslak;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.waslak.waslak.networkUtils.Connector;
import com.waslak.waslak.utils.GPSTracker;
import com.waslak.waslak.utils.Helper;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.fabric.sdk.android.Fabric;
import se.arbitur.geocoding.Callback;
import se.arbitur.geocoding.Constants.AddressTypes;
import se.arbitur.geocoding.Constants.LocationTypes;
import se.arbitur.geocoding.Response;
import se.arbitur.geocoding.Result;
import se.arbitur.geocoding.ReverseGeocoding;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    GPSTracker mTracker;
    @BindView(R.id.progressIndicator)
    ProgressBar progressBar;
    @BindView(R.id.address)
    TextView mAddressTextView;
    @BindView(R.id.send_btn)
    FloatingActionButton mSendButton;
    @BindView(R.id.address_details)
    EditText mAddressDetails;

    double mLat = 0;
    double mLon = 0;
    String mAddress = "";
    String mAddressExtraDetails = "";
    String mCity = "";
    String mCountry = "";
    View mMapView;

    boolean mLocated = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        ButterKnife.bind(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mMapView = mapFragment.getView();
        mapFragment.getMapAsync(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setTitle(getString(R.string.delivery_location));

        if (getIntent().hasExtra("title")) {
            if (getIntent().getStringExtra("title").equals("start")) {
                setTitle(getString(R.string.start_location_map));
            } else if (getIntent().getStringExtra("title").equals("destination")) {
                setTitle(getString(R.string.delivery_location));
            }
        }

        Places.initialize(getApplicationContext(), "AIzaSyAcazeBKVO9e7HvHB9ssU1jc9NhTj_AFsQ");

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAddressExtraDetails = mAddressDetails.getText().toString();
                if (mLat != 0 && mLon != 0 && !mAddress.isEmpty() && !mCity.isEmpty() && !mCountry.isEmpty()) {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("lat", mLat);
                    returnIntent.putExtra("lon", mLon);
                    returnIntent.putExtra("address", mAddress);
                    returnIntent.putExtra("addressExtra", mAddressExtraDetails);
                    returnIntent.putExtra("city", mCity);
                    returnIntent.putExtra("country", mCountry);
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                }
            }
        });

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME,Place.Field.LAT_LNG));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                mLat = place.getLatLng().latitude;
                mLon = place.getLatLng().longitude;
                mAddress = getAddress(place.getLatLng());
                mAddressTextView.setText(mAddress);
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 16.0f);
                mMap.animateCamera(cameraUpdate);
            }

            @Override
            public void onError(@NonNull Status status) {
                Helper.writeToLog(status.toString());
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            View locationButton = ((View) mMapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            // position on right bottom
            rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            rlp.setMargins(0, 180, 180, 180);
        }
        googleMap.getUiSettings().setRotateGesturesEnabled(false);
        mTracker = new GPSTracker(MapActivity.this, new GPSTracker.OnGetLocation() {
            @Override
            public void onGetLocation(double lat, double lon) {
                if (lat != 0 && lon != 0 && !mLocated) {
                    mLocated = true;
                    mLat = lat;
                    mLon = lon;
                    LatLng latLng = new LatLng(lat, lon);
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16.0f);
                    mMap.moveCamera(cameraUpdate);
                    if (progressBar.getVisibility() == View.VISIBLE && mAddressTextView.getVisibility() == View.INVISIBLE) {
                        progressBar.setVisibility(View.VISIBLE);
                        mAddressTextView.setVisibility(View.INVISIBLE);
                    }
                    if (progressBar.getVisibility() == View.GONE && mAddressTextView.getVisibility() == View.VISIBLE) {
                        progressBar.setVisibility(View.GONE);
                        mAddressTextView.setVisibility(View.VISIBLE);
                    }
                    mAddress = getAddress(latLng);
                    mAddressTextView.setText(mAddress);
                    mTracker.stopUsingGPS();
                }
            }
        });
        if (mTracker.canGetLocation()) {
            Location location = mTracker.getLocation();
            if (location != null) {
                if (location.getLatitude() != 0 && location.getLongitude() != 0 && !mLocated) {
                    mLat = location.getLatitude();
                    mLon = location.getLongitude();
                    mLocated = true;
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16.0f);
                    mMap.moveCamera(cameraUpdate);
                    if (progressBar.getVisibility() == View.VISIBLE && mAddressTextView.getVisibility() == View.INVISIBLE) {
                        progressBar.setVisibility(View.VISIBLE);
                        mAddressTextView.setVisibility(View.INVISIBLE);
                    }
                    if (progressBar.getVisibility() == View.GONE && mAddressTextView.getVisibility() == View.VISIBLE) {
                        progressBar.setVisibility(View.GONE);
                        mAddressTextView.setVisibility(View.VISIBLE);
                    }
                    mAddress = getAddress(latLng);
                    mAddressTextView.setText(mAddress);
                    mTracker.stopUsingGPS();
                }
            }
        }
        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                LatLng latLng = mMap.getCameraPosition().target;
                mLat = latLng.latitude;
                mLon = latLng.longitude;
                if (progressBar.getVisibility() == View.VISIBLE && mAddressTextView.getVisibility() == View.INVISIBLE) {
                    progressBar.setVisibility(View.VISIBLE);
                    mAddressTextView.setVisibility(View.INVISIBLE);
                }
                if (progressBar.getVisibility() == View.GONE && mAddressTextView.getVisibility() == View.VISIBLE) {
                    progressBar.setVisibility(View.GONE);
                    mAddressTextView.setVisibility(View.VISIBLE);
                }
                mAddressTextView.setText(getAddress(latLng));
            }
        });
    }


    public String getAddress(LatLng latLng) {
        if (Geocoder.isPresent()) {
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(MapActivity.this, Locale.ENGLISH);

            try {
                addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                if (addresses != null && addresses.size() > 0) {
                    mAddress = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                    if (mAddress == null){
                        mAddress = "";
                    }
                    mCity = addresses.get(0).getAdminArea();
                    if (mCity == null) {
                        mCity = "";
                    }
                    mCountry = addresses.get(0).getCountryName();
                    if (mCountry == null) {
                        mCountry = "";
                    }
                    Helper.writeToLog("Address : " + mAddress + " City : " + mCity + " Country :" + mCountry);
                     }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return mAddress;
        } else {
        new ReverseGeocoding(latLng.latitude, latLng.longitude, "AIzaSyATc3Nte8Pj1oWTFKAbLWUiJbzSIJEDzxc")
                .setLanguage("en")
                .fetch(new Callback() {
                    @Override
                    public void onResponse(Response response) {
                        mAddress = response.getResults()[0].getFormattedAddress();
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
                        if (mAddress == null)
                            mAddress = "";
                        if (mCity == null)
                            mCity = "";
                        if (mCountry == null)
                            mCountry= "";
                        Helper.writeToLog("Address : " + mAddress + " City : " + mCity + " Country :" + mCountry);
                        Helper.writeToLog(mAddress);
                        Helper.writeToLog("Address : " + mAddress + " City : " + mCity + " Country :" + mCountry);
                       mAddressTextView.setText(mAddress);

                    }

                    @Override
                    public void onFailed(Response response, IOException e) {

                    }
                });
        return mAddress;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_map: {
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                return true;
            }
            case R.id.nav_satellite: {
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                return true;
            }
            case R.id.nav_hybrid: {
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                return true;
            }
            case android.R.id.home: {
                finish();
                return true;
            }
            default:
                return false;
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
