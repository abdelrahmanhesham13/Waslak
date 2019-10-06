package com.waslak.waslak;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ViewUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.waslak.waslak.models.ChatModel;
import com.waslak.waslak.models.RequestModel;
import com.waslak.waslak.models.ShopModel;
import com.waslak.waslak.networkUtils.Connector;
import com.waslak.waslak.networkUtils.Constants;
import com.waslak.waslak.utils.GPSTracker;
import com.waslak.waslak.utils.Helper;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OrderDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = OrderDetailsActivity.class.getSimpleName();
    @BindView(R.id.setting_button)
    TextView mSettingButton;
    @BindView(R.id.menu_button)
    ImageView mMenuButton;
    @BindView(R.id.title)
    TextView mTitleTextView;
    @BindView(R.id.state)
    TextView mStateTextView;
    @BindView(R.id.price)
    TextView mPriceTextView;
    @BindView(R.id.expire_date)
    TextView mExpireDate;
    @BindView(R.id.delivery_name)
    TextView mDeliveryName;
    @BindView(R.id.delivery_state)
    TextView mDeliveryState;
    @BindView(R.id.save_register)
    Button mOpenChat;


    RequestModel mRequestModel;
    GoogleMap mMap = null;
    ChatModel mChatModel;
    Connector mConnectorGetRequest;
    private Connector mConnectorSendMessage;

    LatLngBounds bounds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);
        ButterKnife.bind(this);

        mMenuButton.setVisibility(View.GONE);

        mSettingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(OrderDetailsActivity.this, SettingActivity.class));
            }
        });

        Intent intent = getIntent();
        if (intent.hasExtra("request"))
            mRequestModel = (RequestModel) intent.getSerializableExtra("request");


        mTitleTextView.setText(mRequestModel.getShop().getName());
        mStateTextView.setText(mRequestModel.getDescription());
        mPriceTextView.setText(mRequestModel.getPrice());
        mExpireDate.setText("Delivered Within : " + mRequestModel.getDuration());
        if (!mRequestModel.getDelivery().getName().equals("null")) {
            mDeliveryName.setVisibility(View.VISIBLE);
            mDeliveryName.setText("Delivered By : " + mRequestModel.getDelivery().getName());
        } else {
            mDeliveryName.setVisibility(View.GONE);
        }

        mDeliveryState.setText(mRequestModel.getNote());

        mOpenChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRequestModel.getDelivery().getName().equals("null")) {
                    Helper.showSnackBarMessage(getString(R.string.no_offer_accepted), OrderDetailsActivity.this);
                } else if (Integer.valueOf(mRequestModel.getStatus()) == 1) {
                    String url = Connector.createStartChatUrl() + "?message=&user_id=" + mRequestModel.getUser_id() + "&request_id=" + mRequestModel.getId() + "&to_id=" + mRequestModel.getDelivery().getId();
                    Helper.writeToLog(url);
                    mConnectorSendMessage.getRequest(TAG, url);
                }
            }
        });


        mConnectorSendMessage = new Connector(this, new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                if (Connector.checkStatus(response)) {
                    mChatModel = Connector.getChatModelJson(response, "", mRequestModel.getDeliveryId(), mRequestModel.getUser_id());

                    mConnectorGetRequest.getRequest(TAG, Constants.WASLAK_BASE_URL + "/mobile/api/get_request?id=" + mRequestModel.getId());
                } else {
                    Helper.showSnackBarMessage(getString(R.string.error), OrderDetailsActivity.this);
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                Helper.showSnackBarMessage(getString(R.string.error), OrderDetailsActivity.this);
            }
        });

        mConnectorGetRequest = new Connector(this, new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                if (Connector.checkStatus(response)) {
                    mRequestModel = Connector.getRequest(response, new ShopModel());
                    startActivity(new Intent(OrderDetailsActivity.this, ChatActivity.class).putExtra("chat", mChatModel).putExtra("request", mRequestModel));
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {

            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (mMap != null) {

        }

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
        googleMap.getUiSettings().setRotateGesturesEnabled(false);

        LatLng start = new LatLng(Double.parseDouble(mRequestModel.getLatitude()), Double.parseDouble(mRequestModel.getLongitude()));
        int height = 100;
        int width = 100;
        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.home);
        Bitmap b = bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
        mMap.addMarker(new MarkerOptions().position(start).icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));

        LatLng wayPoint = null;

        try {
            wayPoint = new LatLng(Double.parseDouble(mRequestModel.getShop().getLat()), Double.parseDouble(mRequestModel.getShop().getLon()));
            height = 100;
            width = 100;
            bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.shop1);
            b = bitmapdraw.getBitmap();
            smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
            mMap.addMarker(new MarkerOptions().position(wayPoint).icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        LatLng end = null;
        if (!mRequestModel.getDelivery().getName().equals("null")) {
            end = new LatLng(Double.parseDouble(mRequestModel.getDelivery().getLatitude()), Double.parseDouble(mRequestModel.getDelivery().getLongitude()));
            height = 100;
            width = 100;
            bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.coruier);
            b = bitmapdraw.getBitmap();
            smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
            mMap.addMarker(new MarkerOptions().position(end).icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(start);
        if (wayPoint != null)
            builder.include(wayPoint);
        if (end != null) {
            builder.include(end);
        }

        bounds = builder.build();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenHeight = displayMetrics.heightPixels;
        int screedWidth = displayMetrics.widthPixels;

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, screedWidth, 600, 150);

        mMap.animateCamera(cu);


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
