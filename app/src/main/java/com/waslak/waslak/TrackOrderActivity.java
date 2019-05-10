package com.waslak.waslak;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
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
import com.google.android.gms.maps.model.PolylineOptions;
import com.waslak.waslak.models.RequestModel;
import com.waslak.waslak.models.ShopModel;
import com.waslak.waslak.models.UserModel;
import com.waslak.waslak.networkUtils.Connector;
import com.waslak.waslak.utils.GPSTracker;
import com.waslak.waslak.utils.Helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import se.arbitur.geocoding.Callback;
import se.arbitur.geocoding.Response;
import se.arbitur.geocoding.ReverseGeocoding;

public class TrackOrderActivity extends AppCompatActivity implements OnMapReadyCallback, RoutingListener {

    private static final String TAG = "TrackOrderActivity";
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
    ProgressDialog mProgressDialog;

    @BindView(R.id.delivery_location)
    TextView mDeliveryLocation;
    @BindView(R.id.store_location)
    TextView mStoreLocation;
    @BindView(R.id.to_customer_distance)
    TextView mToCustomerDistance;
    @BindView(R.id.to_shop_distance)
    TextView mToShopDistance;
    @BindView(R.id.received)
    CheckBox mReceived;

    String mFullAddress;
    String mCountry;
    String mCity;

    String mTotalDistance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_order);
        ButterKnife.bind(this);

        if (getIntent() != null && getIntent().hasExtra("user") && getIntent().hasExtra("shopModel") && getIntent().hasExtra("request")) {
            mUserModel = (UserModel) getIntent().getSerializableExtra("user");
            mShopModel = (ShopModel) getIntent().getSerializableExtra("shopModel");
            mRequestModel = (RequestModel) getIntent().getSerializableExtra("request");
            mStoreLocation.setText(mShopModel.getAddress());
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mMapView = mapFragment.getView();
        mapFragment.getMapAsync(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setTitle(getString(R.string.track_order));


        mConnector = new Connector(this, new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {

            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {

            }
        });

        if (mRequestModel.getDeliveryId().equals(Helper.getUserSharedPreferences(this).getId())) {
            mReceived.setEnabled(true);
            if (!mRequestModel.getStatus().equals("2") && !mRequestModel.getStatus().equals("1")) {
                mReceived.setChecked(true);
                mReceived.setEnabled(false);

            }

        } else {
            if (!mRequestModel.getStatus().equals("2") && !mRequestModel.getStatus().equals("1"))
                mReceived.setChecked(true);

            mReceived.setEnabled(false);
        }

        mReceived.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mConnector.getRequest(TAG, "http://www.as.cta3.com/waslk/api/update_request_status?id=" + mRequestModel.getId() + "&longitude=" + start.longitude + "&latitude=" + start.latitude + "&status=3" + "&user_id=" + mRequestModel.getUser_id());
                mReceived.setEnabled(false);
            }
        });


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mRequestModel.getDeliveryId().equals(Helper.getUserSharedPreferences(TrackOrderActivity.this).getId())) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            }
            googleMap.getUiSettings().setRotateGesturesEnabled(false);
            googleMap.getUiSettings().setCompassEnabled(false);
            mTracker = new GPSTracker(TrackOrderActivity.this, new GPSTracker.OnGetLocation() {
                @Override
                public void onGetLocation(double lat, double lon) {
                    if (!mLocated) {
                        mLocated = true;
                        start = new LatLng(lat, lon);
                        mConnector.getRequest(TAG, "http://www.as.cta3.com/waslk/api/update_request_status?id=" + mRequestModel.getId() + "&longitude=" + start.longitude + "&latitude=" + start.latitude + "&user_id=" + mRequestModel.getUser_id() + "&status=" + mRequestModel.getStatus());
                        mDeliveryLocation.setText(getAddress(start, "delivery"));
                        wayPoint = new LatLng(Double.parseDouble(mShopModel.getLat()), Double.parseDouble(mShopModel.getLon()));
                        end = new LatLng(Double.parseDouble(mRequestModel.getLatitude()), Double.parseDouble(mRequestModel.getLongitude()));
                        Location startLocation = new Location("");
                        startLocation.setLatitude(lat);
                        startLocation.setLongitude(lon);
                        Location wayPointLocation = new Location("");
                        wayPointLocation.setLatitude(Double.parseDouble(mShopModel.getLat()));
                        wayPointLocation.setLongitude(Double.parseDouble(mShopModel.getLon()));
                        Location endLocation = new Location("");
                        endLocation.setLatitude(Double.parseDouble(mRequestModel.getLatitude()));
                        endLocation.setLongitude(Double.parseDouble(mRequestModel.getLongitude()));
                        mToShopDistance.setText(String.format(Locale.getDefault(), "%.2f " + getString(R.string.km), startLocation.distanceTo(wayPointLocation) / 1000.0));
                        mToCustomerDistance.setText(String.format(Locale.getDefault(), "%.2f " + getString(R.string.km), startLocation.distanceTo(endLocation) / 1000.0));

                        Helper.writeToLog("Distance : " + ((endLocation.distanceTo(startLocation) / 1000.0) + (wayPointLocation.distanceTo(startLocation) / 1000.0)));

                        mTotalDistance = String.valueOf((endLocation.distanceTo(startLocation) + wayPointLocation.distanceTo(startLocation)) / 1000.0);

                        Routing routing = new Routing.Builder()
                                .travelMode(Routing.TravelMode.DRIVING)
                                .withListener(TrackOrderActivity.this)
                                .waypoints(start, wayPoint, end)
                                .alternativeRoutes(false)
                                .key("AIzaSyAcazeBKVO9e7HvHB9ssU1jc9NhTj_AFsQ")
                                .build();
                        routing.execute();
                        mTracker.stopUsingGPS();
                    }
                }
            });
            if (mTracker.canGetLocation() && !mLocated) {
                mLocated = true;
                Location location = mTracker.getLocation();
                start = new LatLng(location.getLatitude(), location.getLongitude());
                mConnector.getRequest(TAG, "http://www.as.cta3.com/waslk/api/update_request_status?id=" + mRequestModel.getId() + "&longitude=" + start.longitude + "&latitude=" + start.latitude + "&user_id=" + mRequestModel.getUser_id() + "&status=" + mRequestModel.getStatus());
                mDeliveryLocation.setText(getAddress(start, "delivery"));
                wayPoint = new LatLng(Double.parseDouble(mShopModel.getLat()), Double.parseDouble(mShopModel.getLon()));
                end = new LatLng(Double.parseDouble(mRequestModel.getLatitude()), Double.parseDouble(mRequestModel.getLongitude()));

                Location startLocation = new Location("");
                startLocation.setLatitude(location.getLatitude());
                startLocation.setLongitude(location.getLongitude());
                Location wayPointLocation = new Location("");
                wayPointLocation.setLatitude(Double.parseDouble(mShopModel.getLat()));
                wayPointLocation.setLongitude(Double.parseDouble(mShopModel.getLon()));
                Location endLocation = new Location("");
                endLocation.setLatitude(Double.parseDouble(mRequestModel.getLatitude()));
                endLocation.setLongitude(Double.parseDouble(mRequestModel.getLongitude()));
                mToShopDistance.setText(String.format(Locale.getDefault(), "%.2f " + getString(R.string.km), wayPointLocation.distanceTo(startLocation) / 1000.0));
                mToCustomerDistance.setText(String.format(Locale.getDefault(), "%.2f " + getString(R.string.km), endLocation.distanceTo(startLocation) / 1000.0));

                Helper.writeToLog("Distance : " + ((endLocation.distanceTo(startLocation) / 1000.0) + (wayPointLocation.distanceTo(startLocation) / 1000.0)));

                mTotalDistance = String.valueOf((endLocation.distanceTo(startLocation) + wayPointLocation.distanceTo(startLocation)) / 1000.0);


                Routing routing = new Routing.Builder()
                        .travelMode(Routing.TravelMode.DRIVING)
                        .withListener(TrackOrderActivity.this)
                        .waypoints(start, wayPoint, end)
                        .alternativeRoutes(false)
                        .key("AIzaSyAcazeBKVO9e7HvHB9ssU1jc9NhTj_AFsQ")
                        .build();
                routing.execute();
                mTracker.stopUsingGPS();
            }
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            }
            googleMap.getUiSettings().setRotateGesturesEnabled(false);
            googleMap.getUiSettings().setCompassEnabled(false);
            if (!mRequestModel.getLatitudeUpdate().equals("0"))
                start = new LatLng(Double.valueOf(mRequestModel.getLatitudeUpdate()), Double.valueOf(mRequestModel.getLongitudeUpdate()));
            else
                start = new LatLng(Double.valueOf(mRequestModel.getDelivery().getLatitude()), Double.valueOf(mRequestModel.getDelivery().getLongitude()));

            mDeliveryLocation.setText(getAddress(start, "delivery"));
            wayPoint = new LatLng(Double.parseDouble(mShopModel.getLat()), Double.parseDouble(mShopModel.getLon()));
            end = new LatLng(Double.parseDouble(mRequestModel.getLatitude()), Double.parseDouble(mRequestModel.getLongitude()));

            Location startLocation = new Location("");
            startLocation.setLatitude(start.latitude);
            startLocation.setLongitude(start.longitude);
            Location wayPointLocation = new Location("");
            wayPointLocation.setLatitude(Double.parseDouble(mShopModel.getLat()));
            wayPointLocation.setLongitude(Double.parseDouble(mShopModel.getLon()));
            Location endLocation = new Location("");
            endLocation.setLatitude(Double.parseDouble(mRequestModel.getLatitude()));
            endLocation.setLongitude(Double.parseDouble(mRequestModel.getLongitude()));
            mToShopDistance.setText(String.format(Locale.getDefault(), "%.2f " + getString(R.string.km), wayPointLocation.distanceTo(startLocation) / 1000.0));
            mToCustomerDistance.setText(String.format(Locale.getDefault(), "%.2f " + getString(R.string.km), endLocation.distanceTo(startLocation) / 1000.0));

            Helper.writeToLog("Distance : " + ((endLocation.distanceTo(startLocation) / 1000.0) + (wayPointLocation.distanceTo(startLocation) / 1000.0)));

            mTotalDistance = String.valueOf((endLocation.distanceTo(startLocation) + wayPointLocation.distanceTo(startLocation)) / 1000.0);


            Routing routing = new Routing.Builder()
                    .travelMode(Routing.TravelMode.DRIVING)
                    .withListener(TrackOrderActivity.this)
                    .waypoints(start, wayPoint, end)
                    .alternativeRoutes(false)
                    .key("AIzaSyAcazeBKVO9e7HvHB9ssU1jc9NhTj_AFsQ")
                    .build();
            routing.execute();

        }
    }


    public String getAddress(LatLng latLng, final String type) {
        if (Geocoder.isPresent()) {
            Geocoder geocoder;
            List<Address> addresses = null;
            geocoder = new Geocoder(TrackOrderActivity.this, Locale.ENGLISH);

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
                            }
                        }

                        @Override
                        public void onFailed(Response response, IOException e) {

                        }
                    });
            return null;
        }
    }


    @Override
    public void onRoutingFailure(RouteException e) {
        if (e != null) {
            e.printStackTrace();
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
        options.position(wayPoint);
        bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.shop1);
        b = bitmapdraw.getBitmap();
        smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

        mMap.addMarker(options).setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(start);
        builder.include(wayPoint);
        builder.include(end);
        LatLngBounds bounds = builder.build();


        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 150);

        mMap.moveCamera(cu);
    }

    @Override
    public void onRoutingCancelled() {

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
