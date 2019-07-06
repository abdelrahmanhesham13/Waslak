package com.waslak.waslak;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.waslak.waslak.models.RequestModel;
import com.waslak.waslak.models.ShopModel;
import com.waslak.waslak.models.UserModel;
import com.waslak.waslak.networkUtils.Connector;
import com.waslak.waslak.utils.GPSTracker;
import com.waslak.waslak.utils.Helper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
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

public class ConfirmDeliveryActivity extends AppCompatActivity implements OnMapReadyCallback, RoutingListener {


    private static final String TAG = "ConfirmActivity";
    private GoogleMap mMap;
    GPSTracker mTracker;

    View mMapView;

    UserModel mUserModel;
    ShopModel mShopModel;
    RequestModel mRequestModel;

    boolean mLocated = false;
    LatLng start;
    LatLng wayPoint;
    LatLng end;

    Connector mConnector;
    Connector mConnectorGetSettings;
    Connector mConnectorUpdate;

    ProgressDialog mProgressDialog;
    String mMinPrice;
    String mPricePerKilo;

    @BindView(R.id.delivery_location)
    TextView mDeliveryLocation;
    @BindView(R.id.store_location)
    TextView mStoreLocation;
    @BindView(R.id.to_customer_distance)
    TextView mToCustomerDistance;
    @BindView(R.id.to_shop_distance)
    TextView mToShopDistance;
    @BindView(R.id.duration)
    TextView mDuration;
    @BindView(R.id.price)
    EditText mPrice;
    String mTotalDistance;

    String mFullAddress;
    String mCountry;
    String mCity;
    String mMaxPrice;

    String mCountryLocale = "Jordan";

    String mCurrency;
    String mCurrencyArabic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_delivery);
        ButterKnife.bind(this);
        TelephonyManager tm = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
        mCountryLocale = tm.getNetworkCountryIso();

        if (mCountryLocale.contains("sa") || mCountryLocale.contains("SA")) {
            mCountryLocale = "Saudi%20arabia";
        } else if (mCountryLocale.equalsIgnoreCase("eg")){
            mCountryLocale = "Egypt";
        } else if (mCountryLocale.equalsIgnoreCase("jo")) {
            mCountryLocale = "Jordan";
        } else {
            mCountryLocale = "";
        }

        if (getIntent() != null && getIntent().hasExtra("user") && getIntent().hasExtra("shopModel") && getIntent().hasExtra("request")) {
            mUserModel = (UserModel) getIntent().getSerializableExtra("user");
            mShopModel = (ShopModel) getIntent().getSerializableExtra("shopModel");
            mRequestModel = (RequestModel) getIntent().getSerializableExtra("request");

            mDuration.setText(mRequestModel.getDuration());
            if (mShopModel.getName() == null) {
                setTitle(getString(R.string.delivery));
                mStoreLocation.setText(mRequestModel.getUserAddress());
            } else {
                setTitle(mShopModel.getName());
                mStoreLocation.setText(mShopModel.getAddress());
            }
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mMapView = mapFragment.getView();
        mapFragment.getMapAsync(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        //setTitle(getString(R.string.delivery_location));

        mConnector = new Connector(this, new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {

                if (Connector.checkStatus(response)) {
                    mProgressDialog.dismiss();
                    Helper.showAlertDialog(ConfirmDeliveryActivity.this, getString(R.string.offer_confirmed), "", false, getString(R.string.ok), "", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setResult(Activity.RESULT_OK);
                            finish();
                        }
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                } else {
                    mProgressDialog.dismiss();
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String message = jsonObject.getString("message");
                        Helper.showSnackBarMessage(message, ConfirmDeliveryActivity.this);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                mProgressDialog.dismiss();
                Helper.showSnackBarMessage(getString(R.string.error), ConfirmDeliveryActivity.this);
            }
        });


        mConnectorGetSettings = new Connector(this, new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    mMinPrice = jsonObject.getString("min_price");
                    mPricePerKilo = jsonObject.getString("price_per_kilo");
                    mPrice.setText(String.format(Locale.ENGLISH, "%.2f", Double.valueOf(mPricePerKilo) * Double.valueOf(mTotalDistance) + Double.valueOf(mMinPrice)));
                    if (mRequestModel.getPromo().equals("0")) {
                        mMaxPrice = mPrice.getText().toString();
                    } else {
                        mMaxPrice = String.valueOf((Double.valueOf(mPricePerKilo) * Double.valueOf(mTotalDistance)) * (Double.valueOf(mRequestModel.getPromo())/100.0));
                        mMaxPrice = String.valueOf(Double.valueOf(mMaxPrice) + 1);
                        mPrice.setText(mMaxPrice);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {

            }
        });

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
        googleMap.getUiSettings().setRotateGesturesEnabled(false);
        googleMap.getUiSettings().setCompassEnabled(false);
        mTracker = new GPSTracker(ConfirmDeliveryActivity.this, new GPSTracker.OnGetLocation() {
            @Override
            public void onGetLocation(double lat, double lon) {
                if (!mLocated) {
                    mLocated = true;
                    start = new LatLng(lat, lon);
                    mDeliveryLocation.setText(getAddress(start, "delivery"));
                    try {
                        wayPoint = new LatLng(Double.parseDouble(mShopModel.getLat()), Double.parseDouble(mShopModel.getLon()));
                    } catch (Exception e) {
                        wayPoint = new LatLng(Double.parseDouble(mRequestModel.getUserRequestLat()), Double.parseDouble(mRequestModel.getUserRequestLon()));
                        e.printStackTrace();
                    }
                    end = new LatLng(Double.parseDouble(mRequestModel.getLatitude()), Double.parseDouble(mRequestModel.getLongitude()));
                    Location startLocation = new Location("");
                    startLocation.setLatitude(lat);
                    startLocation.setLongitude(lon);
                    Location wayPointLocation = new Location("");
                    try {
                        wayPointLocation.setLatitude(Double.parseDouble(mShopModel.getLat()));
                        wayPointLocation.setLongitude(Double.parseDouble(mShopModel.getLon()));
                    } catch (Exception e) {
                        wayPointLocation.setLatitude(Double.parseDouble(mRequestModel.getUserRequestLat()));
                        wayPointLocation.setLongitude(Double.parseDouble(mRequestModel.getUserRequestLon()));
                        e.printStackTrace();
                    }
                    Location endLocation = new Location("");
                    endLocation.setLatitude(Double.parseDouble(mRequestModel.getLatitude()));
                    endLocation.setLongitude(Double.parseDouble(mRequestModel.getLongitude()));
                    mToCustomerDistance.setText(String.format(Locale.getDefault(), "%.2f " + getString(R.string.km), startLocation.distanceTo(endLocation) / 1000.0));

                    Helper.writeToLog("Distance : " + ((wayPointLocation.distanceTo(endLocation) / 1000.0)));

                    if (wayPoint != null) {
                        mTotalDistance = String.valueOf((wayPointLocation.distanceTo(endLocation)) / 1000.0);
                        mToShopDistance.setText(String.format(Locale.getDefault(), "%.2f " + getString(R.string.km), startLocation.distanceTo(wayPointLocation) / 1000.0));
                    } else {
                        mTotalDistance = String.valueOf((startLocation.distanceTo(endLocation)) / 1000.0);
                        mToShopDistance.setVisibility(View.GONE);
                    }
                    Helper.writeToLog(mTotalDistance);
                    mConnectorGetSettings.getRequest(TAG, "http://as.cta3.com/waslk/api/get_prices?country=" + mCountryLocale);


                    if (wayPoint != null) {
                        Routing routing = new Routing.Builder()
                                .travelMode(Routing.TravelMode.DRIVING)
                                .withListener(ConfirmDeliveryActivity.this)
                                .waypoints(start, wayPoint, end)
                                .alternativeRoutes(false)
                                .key("AIzaSyAcazeBKVO9e7HvHB9ssU1jc9NhTj_AFsQ")
                                .build();
                        routing.execute();
                    } else {
                        Routing routing = new Routing.Builder()
                                .travelMode(Routing.TravelMode.DRIVING)
                                .withListener(ConfirmDeliveryActivity.this)
                                .waypoints(start, end)
                                .alternativeRoutes(false)
                                .key("AIzaSyAcazeBKVO9e7HvHB9ssU1jc9NhTj_AFsQ")
                                .build();
                        routing.execute();
                    }
                    mTracker.stopUsingGPS();
                }
            }
        });
        if (mTracker.canGetLocation() && !mLocated) {
            mLocated = true;
            Location location = mTracker.getLocation();
            start = new LatLng(location.getLatitude(), location.getLongitude());
            mDeliveryLocation.setText(getAddress(start, "delivery"));
            try {
                wayPoint = new LatLng(Double.parseDouble(mShopModel.getLat()), Double.parseDouble(mShopModel.getLon()));
            } catch (Exception e) {
                wayPoint = new LatLng(Double.parseDouble(mRequestModel.getUserRequestLat()), Double.parseDouble(mRequestModel.getUserRequestLon()));
                e.printStackTrace();
            }
            end = new LatLng(Double.parseDouble(mRequestModel.getLatitude()), Double.parseDouble(mRequestModel.getLongitude()));

            Location startLocation = new Location("");
            startLocation.setLatitude(location.getLatitude());
            startLocation.setLongitude(location.getLongitude());
            Location wayPointLocation = new Location("");
            try {
                wayPointLocation.setLatitude(Double.parseDouble(mShopModel.getLat()));
                wayPointLocation.setLongitude(Double.parseDouble(mShopModel.getLon()));
            } catch (Exception e) {
                wayPointLocation.setLatitude(Double.parseDouble(mRequestModel.getUserRequestLat()));
                wayPointLocation.setLongitude(Double.parseDouble(mRequestModel.getUserRequestLon()));
                e.printStackTrace();
            }

            Location endLocation = new Location("");
            endLocation.setLatitude(Double.parseDouble(mRequestModel.getLatitude()));
            endLocation.setLongitude(Double.parseDouble(mRequestModel.getLongitude()));
            mToCustomerDistance.setText(String.format(Locale.getDefault(), "%.2f " + getString(R.string.km), endLocation.distanceTo(startLocation) / 1000.0));

            Helper.writeToLog("Distance : " + ((wayPointLocation.distanceTo(endLocation) / 1000.0)));

            if (wayPoint != null) {
                mTotalDistance = String.valueOf((wayPointLocation.distanceTo(endLocation)) / 1000.0);
                mToShopDistance.setText(String.format(Locale.getDefault(), "%.2f " + getString(R.string.km), startLocation.distanceTo(wayPointLocation) / 1000.0));
            } else {
                mTotalDistance = String.valueOf((startLocation.distanceTo(endLocation)) / 1000.0);
                mToShopDistance.setVisibility(View.GONE);
            }
            Helper.writeToLog(mTotalDistance);
            mConnectorGetSettings.getRequest(TAG, "http://as.cta3.com/waslk/api/get_prices?country=" + mCountryLocale);

            if (wayPoint != null) {
                Routing routing = new Routing.Builder()
                        .travelMode(Routing.TravelMode.DRIVING)
                        .withListener(ConfirmDeliveryActivity.this)
                        .waypoints(start, wayPoint, end)
                        .alternativeRoutes(false)
                        .key("AIzaSyAcazeBKVO9e7HvHB9ssU1jc9NhTj_AFsQ")
                        .build();
                routing.execute();
            } else {
                Routing routing = new Routing.Builder()
                        .travelMode(Routing.TravelMode.DRIVING)
                        .withListener(ConfirmDeliveryActivity.this)
                        .waypoints(start, end)
                        .alternativeRoutes(false)
                        .key("AIzaSyAcazeBKVO9e7HvHB9ssU1jc9NhTj_AFsQ")
                        .build();
                routing.execute();
            }
            mTracker.stopUsingGPS();
        }
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        if (e != null) {
            Helper.writeToLog("Error " + e.getMessage());
        } else {
            Toast.makeText(this, getString(R.string.error), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        CameraUpdate center = CameraUpdateFactory.newLatLng(start);

        mMap.moveCamera(center);


        PolylineOptions polyOptions = new PolylineOptions();
        polyOptions.width(5);
        polyOptions.color(getResources().getColor(R.color.blue));
        polyOptions.addAll(route.get(0).getPoints());
        mMap.addPolyline(polyOptions);
        // Start marker
        MarkerOptions options = new MarkerOptions();
        options.position(start);
        int height = 100;
        int width = 100;
        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.coruier);
        Bitmap b = bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

        mMap.addMarker(options).setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));

        // End marker
        options = new MarkerOptions();
        options.position(end);
        bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.home);
        b = bitmapdraw.getBitmap();
        smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

        mMap.addMarker(options).setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));

        // End marker
        options = new MarkerOptions();
        if (wayPoint != null) {
            options.position(wayPoint);
            bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.shop1);
            b = bitmapdraw.getBitmap();
            smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

            mMap.addMarker(options).setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));
        }


        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(start);
        if (wayPoint != null) {
            builder.include(wayPoint);
        }
        builder.include(end);
        LatLngBounds bounds = builder.build();


        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 150);

        mMap.moveCamera(cu);
    }

    @Override
    public void onRoutingCancelled() {

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cofirm_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            if (item.getItemId() == R.id.save) {
                if (TextUtils.isEmpty(mPrice.getText().toString())) {
                    Helper.showSnackBarMessage(getString(R.string.enter_your_price), ConfirmDeliveryActivity.this);
                } else if (Double.valueOf(arabicToDecimal(mPrice.getText().toString()).replace("٫", ".")) < Double.valueOf(mMinPrice) || Double.valueOf(arabicToDecimal(mPrice.getText().toString()).replace("٫", ".")) > Double.valueOf(mMaxPrice)) {
                    Helper.showSnackBarMessage(getString(R.string.min_price_is) + mMinPrice + " " + getString(R.string.max_price_is) + mMaxPrice, ConfirmDeliveryActivity.this);
                } else if (getAddress(start, "request") != null) {
                    mProgressDialog = Helper.showProgressDialog(ConfirmDeliveryActivity.this, "Loading", false);
                    mConnector.getRequest(TAG, Connector.createSendOfferUrl() + "?request_id=" + mRequestModel.getId()
                            + "&price=" + arabicToDecimal(mPrice.getText().toString()).replace("٫", ".") + "&delivery_id=" + mUserModel.getId() + "&user_id=" + mRequestModel.getUser_id()
                            + "&longitude=" + start.longitude + "&latitude=" + start.latitude + "&address=" + Uri.encode(getAddress(start, "request")) + "&description=i%20can%20deliver%20it");

                }
                return true;
            }
            return false;
        } catch (NullPointerException e) {
            return false;
        }

    }


    private static String arabicToDecimal(String number) {
        char[] chars = new char[number.length()];
        for (int i = 0; i < number.length(); i++) {
            char ch = number.charAt(i);
            if (ch >= 0x0660 && ch <= 0x0669)
                ch -= 0x0660 - '0';
            else if (ch >= 0x06f0 && ch <= 0x06F9)
                ch -= 0x06f0 - '0';
            chars[i] = ch;
        }
        return new String(chars);
    }

    public String getAddress(LatLng latLng, final String type) {
        if (Geocoder.isPresent()) {
            Geocoder geocoder;
            List<Address> addresses = null;
            geocoder = new Geocoder(ConfirmDeliveryActivity.this, Locale.ENGLISH);

            try {
                addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            } catch (IOException e) {
                e.printStackTrace();
            }
            String address = null;
            if (addresses != null && addresses.size() > 0) {
                address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            }
            return address;
        } else {
            new ReverseGeocoding(latLng.latitude, latLng.longitude, "AIzaSyATc3Nte8Pj1oWTFKAbLWUiJbzSIJEDzxc")
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

                            if (type.equals("delivery")) {
                                mDeliveryLocation.setText(mFullAddress);
                            } else {
                                mProgressDialog = Helper.showProgressDialog(ConfirmDeliveryActivity.this, "Loading", false);
                                mConnector.getRequest(TAG, Connector.createSendOfferUrl() + "?request_id=" + mRequestModel.getId()
                                        + "&price=" + mPrice.getText().toString() + "&delivery_id=" + mUserModel.getId() + "&user_id=" + mRequestModel.getUser_id()
                                        + "&longitude=" + start.longitude + "&latitude=" + start.latitude + "&address=" + Uri.encode(mFullAddress) + "&description=i%20can%20deliver%20it");
                            }

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
