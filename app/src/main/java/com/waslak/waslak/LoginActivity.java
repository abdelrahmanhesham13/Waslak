package com.waslak.waslak;

import android.app.Activity;
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
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.waslak.waslak.models.UserModel;
import com.waslak.waslak.networkUtils.Connector;
import com.waslak.waslak.networkUtils.Constants;
import com.waslak.waslak.utils.GPSTracker;
import com.waslak.waslak.utils.Helper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
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

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    @BindView(R.id.facebook_login)
    Button mFacebookLoginButton;
    @BindView(R.id.sign_up)
    Button mSignUpButton;
    @BindView(R.id.progressIndicator)
    ProgressBar mProgressBar;
    @BindView(R.id.parent_layout)
    View mParentLayout;
    @BindView(R.id.google_login)
    Button mGoogleLogin;
    @BindView(R.id.login_phone)
    Button mLoginPhone;


    CallbackManager callbackManager;
    GoogleSignInClient mGoogleSignInClient;
    String mPhoneNumber;

    Connector mConnector;
    Connector mConnectorSignUp;
    Connector mConnectorMobile;

    UserModel mUserModel = null;

    GPSTracker mTracker;

    String mEmail = null;
    String mName = null;
    String mImage = null;

    double mLat = 0;
    double mLon = 0;
    private boolean mLocated = false;

    String mFullAddress;
    String mCountry;
    String mCity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        if (!isLocationEnabled() || !isHighAccuracy()) {
            displayLocationSettingsRequest(LoginActivity.this);
        }

        mConnectorSignUp = new Connector(this, new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                if (Connector.checkStatus(response)) {
                    mUserModel = Connector.getUser(response);
                    Helper.SaveToSharedPreferences(LoginActivity.this, mUserModel);
                    startActivity(new Intent(LoginActivity.this, HomeActivity.class).putExtra("user", mUserModel).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    finish();
                } else {
                    Helper.showSnackBarMessage(getString(R.string.registered_before), LoginActivity.this);
                    mProgressBar.setVisibility(View.INVISIBLE);
                    mParentLayout.setVisibility(View.VISIBLE);
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                Helper.showSnackBarMessage(getString(R.string.error), LoginActivity.this);
                mProgressBar.setVisibility(View.INVISIBLE);
                mParentLayout.setVisibility(View.VISIBLE);
            }
        });

        mConnector = new Connector(this, new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                if (Connector.checkStatus(response)) {
                    mUserModel = Connector.getUser(response);
                    Helper.SaveToSharedPreferences(LoginActivity.this, mUserModel);
                    if (isLocationEnabled() && isHighAccuracy()) {
                        startActivity(new Intent(LoginActivity.this, HomeActivity.class).putExtra("user", mUserModel).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                        finish();
                    } else {
                        displayLocationSettingsRequest(LoginActivity.this);
                    }
                } else {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String reason = jsonObject.getString("reason");
                        if  (reason.equals("block")) {
                            mProgressBar.setVisibility(View.INVISIBLE);
                            mParentLayout.setVisibility(View.VISIBLE);
                            Helper.showSnackBarMessage(reason, LoginActivity.this);
                        }
                        else
                            getLocation();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                Helper.showSnackBarMessage(getString(R.string.error), LoginActivity.this);
                mProgressBar.setVisibility(View.INVISIBLE);
                mParentLayout.setVisibility(View.VISIBLE);
            }
        });

        mConnectorMobile = new Connector(this, new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                if (Connector.checkStatus(response)) {
                    mUserModel = Connector.getUser(response);
                    Helper.SaveToSharedPreferences(LoginActivity.this, mUserModel);
                    if (isLocationEnabled() && isHighAccuracy()) {
                        startActivity(new Intent(LoginActivity.this, HomeActivity.class).putExtra("user", mUserModel).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                        finish();
                    } else {
                        mProgressBar.setVisibility(View.INVISIBLE);
                        mParentLayout.setVisibility(View.VISIBLE);
                        displayLocationSettingsRequest(LoginActivity.this);
                    }
                } else {
                    mProgressBar.setVisibility(View.INVISIBLE);
                    mParentLayout.setVisibility(View.VISIBLE);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String reason = jsonObject.getString("reason");
                        if  (reason.equals("block")) {
                            mProgressBar.setVisibility(View.INVISIBLE);
                            mParentLayout.setVisibility(View.VISIBLE);
                            Helper.showSnackBarMessage(reason, LoginActivity.this);
                        } else {
                            startActivity(new Intent(LoginActivity.this,AccountActivity.class).putExtra("Type","SignUp").putExtra("Phone",mPhoneNumber));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                Helper.showSnackBarMessage(getString(R.string.error), LoginActivity.this);
                mProgressBar.setVisibility(View.INVISIBLE);
                mParentLayout.setVisibility(View.VISIBLE);
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mLoginPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(LoginActivity.this, MobileVerificationActivity.class), 3);
            }
        });


        mFacebookLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callbackManager = CallbackManager.Factory.create();
                LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("public_profile", "email"));
                LoginManager.getInstance().registerCallback(callbackManager,
                        new FacebookCallback<LoginResult>() {
                            @Override
                            public void onSuccess(LoginResult loginResult) {
                                GraphRequest request = GraphRequest.newMeRequest(
                                        loginResult.getAccessToken(),
                                        new GraphRequest.GraphJSONObjectCallback() {
                                            @Override
                                            public void onCompleted(JSONObject object, GraphResponse response) {
                                                try {
                                                    mEmail = object.getString("email");
                                                    mLocated = false;
                                                    mEmail = object.getString("email");
                                                    mName = object.getString("name");
                                                    mImage = "https://graph.facebook.com/" + object.get("id") + "/picture?type=large";
                                                    String url = Connector.createSignInUrl().build().toString() + "?username=" + mEmail + "&token=" + Helper.getTokenFromSharedPreferences(LoginActivity.this);
                                                    mParentLayout.setVisibility(View.INVISIBLE);
                                                    mProgressBar.setVisibility(View.VISIBLE);
                                                    mConnector.getRequest(TAG, url);
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                Bundle parameters = new Bundle();
                                parameters.putString("fields", "id,name,email,gender,birthday");
                                request.setParameters(parameters);
                                request.executeAsync();

                            }

                            @Override
                            public void onCancel() {
                                Helper.showSnackBarMessage(getString(R.string.error), LoginActivity.this);
                            }

                            @Override
                            public void onError(FacebookException exception) {
                                exception.printStackTrace();
                                Helper.showSnackBarMessage(getString(R.string.error), LoginActivity.this);
                            }
                        });
            }
        });

        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLocationEnabled() && isHighAccuracy()) {
                    startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
                } else {
                    displayLocationSettingsRequestSignUp(LoginActivity.this);
                }
            }
        });

        mGoogleLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGoogleSignInClient.signOut().addOnCompleteListener(LoginActivity.this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mGoogleSignInClient.revokeAccess().addOnCompleteListener(LoginActivity.this, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                signIn();
                            }
                        });
                    }
                });


            }
        });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 2);
    }


    public boolean isLocationEnabled() {
        LocationManager lm = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        if (lm != null) {

            try {
                gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            try {
                network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return gps_enabled || network_enabled;
    }


    private void displayLocationSettingsRequest(Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        if (isHighAccuracy()) {
                            startActivity(new Intent(LoginActivity.this, HomeActivity.class).putExtra("user", mUserModel).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                        }
                        Log.i(TAG, "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            status.startResolutionForResult(LoginActivity.this, 0);
                            //startIntentSenderForResult(status.getResolution().getIntentSender(), 0, null, 0, 0, 0, null);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }


    private void displayLocationSettingsRequestSignUp(Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        if (isHighAccuracy()) {
                            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
                        }
                        Log.i(TAG, "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            status.startResolutionForResult(LoginActivity.this, 16);
                            //startIntentSenderForResult(status.getResolution().getIntentSender(), 0, null, 0, 0, 0, null);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (resultCode == Activity.RESULT_CANCELED) {
                displayLocationSettingsRequest(this);
            } else if (resultCode == Activity.RESULT_OK) {
                if (isHighAccuracy() && mUserModel != null) {
                    startActivity(new Intent(LoginActivity.this, HomeActivity.class).putExtra("user", mUserModel).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    finish();
                }
            }
        } else if (requestCode == 2) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        } else if (requestCode == 3) {
            if (resultCode == Activity.RESULT_OK) {
                String result = data.getStringExtra("result");
                if (result.equals("verified")) {
                    mPhoneNumber = data.getStringExtra("phone");
                    String url = Connector.createSignInUrl().build().toString() + "?username=" + mPhoneNumber + "&token=" + Helper.getTokenFromSharedPreferences(this);
                    mParentLayout.setVisibility(View.INVISIBLE);
                    mProgressBar.setVisibility(View.VISIBLE);
                    mConnectorMobile.getRequest(TAG, url);
                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Helper.showSnackBarMessage(getString(R.string.not_verified_mobile), LoginActivity.this);
            }
        } else if (requestCode == 16) {
            if (resultCode == Activity.RESULT_CANCELED) {
                displayLocationSettingsRequestSignUp(this);
            } else if (resultCode == Activity.RESULT_OK) {
                if (isHighAccuracy()) {
                    startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
                }
            }
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }


    public boolean isHighAccuracy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int locationMode = 0;
            try {
                locationMode = Settings.Secure.getInt(this.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }

            return (locationMode != Settings.Secure.LOCATION_MODE_OFF && locationMode == Settings.Secure.LOCATION_MODE_HIGH_ACCURACY); //check location mode

        } else {
            String locationProviders = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        mConnector.cancelAllRequests(TAG);
    }


    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        Log.i(TAG, "Here");
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                mEmail = account.getEmail();
                mName = account.getDisplayName();
                if (account.getPhotoUrl() != null)
                    mImage = account.getPhotoUrl().toString();
                else
                    mImage = "";
                if (mEmail != null) {
                    String url = Connector.createSignInUrl().build().toString() + "?username=" + mEmail + "&token=" + Helper.getTokenFromSharedPreferences(this);
                    mParentLayout.setVisibility(View.INVISIBLE);
                    mProgressBar.setVisibility(View.VISIBLE);
                    mConnector.getRequest(TAG, url);
                }
            }

        } catch (ApiException e) {
            e.printStackTrace();
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }


    public void getLocation(){
        mTracker = new GPSTracker(this, new GPSTracker.OnGetLocation() {
            @Override
            public void onGetLocation(double lat, double lon) {
                if (lon != 0 && lat != 0 && !mLocated){
                    mLat = lat;
                    mLon = lon;
                    mLocated = true;
                    Helper.writeToLog(mLat + " " + mLon);
                    List<Address> addresses = getAddress(mLat,mLon);
                    if (addresses != null && addresses.size() > 0) {
                        if (mName == null)
                            mName = "";
                        String url = Connector.createSignUpUrl().build().toString() + "?username=" + mEmail + "&name=" +
                                Uri.encode(mName) + "&longitude=" + String.valueOf(mLon) + "&latitude=" + String.valueOf(mLat) +
                                "&address=" + Uri.encode(addresses.get(0).getAddressLine(0)) + "&city_id=" + Uri.encode(addresses.get(0).getAdminArea()) +
                                "&country=" + Uri.encode(addresses.get(0).getCountryName()) + "&image=" + mImage.replaceAll(" ","%20") + "&token=" + Helper.getTokenFromSharedPreferences(LoginActivity.this);

                        mConnectorSignUp.getRequest(TAG, url);
                    }
                    mTracker.stopUsingGPS();
                }
            }
        });

        if (mTracker.canGetLocation()){
            Location location = mTracker.getLocation();
            if (location != null){
                if (location.getLongitude() != 0 && location.getLatitude() != 0 && !mLocated){
                    mLat = location.getLatitude();
                    mLon = location.getLongitude();
                    mLocated = true;
                    Helper.writeToLog(mLat + " " + mLon);
                    List<Address> addresses = getAddress(mLat,mLon);
                    if (addresses != null && addresses.size() > 0) {
                        if (mName == null)
                            mName = "";
                        String url = Connector.createSignUpUrl().build().toString() + "?username=" + mEmail + "&name=" +
                                Uri.encode(mName) + "&longitude=" + String.valueOf(mLon) + "&latitude=" + String.valueOf(mLat) +
                                "&address=" + Uri.encode(addresses.get(0).getAddressLine(0)) + "&city_id=" + Uri.encode(addresses.get(0).getAdminArea()) +
                                "&country=" + Uri.encode(addresses.get(0).getCountryName()) + "&image=" + mImage.replaceAll(" ","%20") + "&token=" + Helper.getTokenFromSharedPreferences(this);
                        mConnectorSignUp.getRequest(TAG, url);
                    }
                    mTracker.stopUsingGPS();
                }
            }
        }
    }


    public List<Address> getAddress(double lat,double lon){
        if (Geocoder.isPresent()) {
            Geocoder geocoder;
            List<Address> addresses = null;
            geocoder = new Geocoder(LoginActivity.this, Locale.ENGLISH);

            try {
                addresses = geocoder.getFromLocation(lat, lon, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return addresses;
        } else {
            new ReverseGeocoding(lat, lon, Constants.API_KEY)
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
                            String url = Connector.createSignUpUrl().build().toString() + "?username=" + mEmail + "&name=" +
                                    mName.replaceAll(" ","%20") + "&longitude=" + String.valueOf(mLon) + "&latitude=" + String.valueOf(mLat) +
                                    "&address=" + Uri.encode(mFullAddress) + "&city_id=" + Uri.encode(mCity) +
                                    "&country=" + Uri.encode(mCountry) + "&image=" + mImage.replaceAll(" ","%20") + "&token=" + Helper.getTokenFromSharedPreferences(LoginActivity.this);

                            mConnectorSignUp.getRequest(TAG, url);
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
