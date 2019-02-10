package com.waslak.waslak;

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.android.volley.VolleyError;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.waslak.waslak.models.UserModel;
import com.waslak.waslak.networkUtils.Connector;
import com.waslak.waslak.utils.GPSTracker;
import com.waslak.waslak.utils.Helper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
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

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";
    @BindView(R.id.facebook_sign_up)
    Button mFacebookSignUp;
    @BindView(R.id.google_sign_up)
    Button mGoogleSignUp;
    @BindView(R.id.progressIndicator)
    ProgressBar mProgressBar;
    @BindView(R.id.parent_layout)
    View mParentLayout;
    @BindView(R.id.sign_up_phone)
    Button mSignUpPhoneButton;


    CallbackManager callbackManager;
    GoogleSignInClient mGoogleSignInClient;
    String mPhoneNumber;

    Connector mConnector;

    UserModel mUserModel;

    GPSTracker mTracker;

    String mEmail = null;
    String mName = null;
    String mImage = null;

    String mFullAddress;
    String mCountry;
    String mCity;

    double mLat = 0;
    double mLon = 0;
    private boolean mLocated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.bind(this);

        mConnector = new Connector(this, new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                if (Connector.checkStatus(response)){
                    mUserModel = Connector.getUser(response);
                    Helper.SaveToSharedPreferences(SignUpActivity.this,mUserModel);
                    startActivity(new Intent(SignUpActivity.this,HomeActivity.class).putExtra("user",mUserModel).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    finish();
                } else {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String reason = jsonObject.getString("reason");
                        if  (reason.equals("block")) {
                            mProgressBar.setVisibility(View.INVISIBLE);
                            mParentLayout.setVisibility(View.VISIBLE);
                            Helper.showSnackBarMessage(reason, SignUpActivity.this);
                        } else {
                            String url = Connector.createSignInUrl().build().toString() + "?username=" + mEmail + "&token=" + Helper.getTokenFromSharedPreferences(SignUpActivity.this);
                            mConnector.getRequest(TAG, url);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                Helper.showSnackBarMessage(getString(R.string.error),SignUpActivity.this);
                mProgressBar.setVisibility(View.INVISIBLE);
                mParentLayout.setVisibility(View.VISIBLE);
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        mFacebookSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callbackManager = CallbackManager.Factory.create();
                LoginManager.getInstance().logInWithReadPermissions(SignUpActivity.this, Arrays.asList("public_profile", "email"));
                LoginManager.getInstance().registerCallback(callbackManager,
                        new FacebookCallback<LoginResult>() {
                            @Override
                            public void onSuccess(LoginResult loginResult) {
                                GraphRequest request = GraphRequest.newMeRequest(
                                        loginResult.getAccessToken(),
                                        new GraphRequest.GraphJSONObjectCallback() {
                                            @Override
                                            public void onCompleted(JSONObject object, GraphResponse response) {
                                                Log.v("LoginActivity", response.toString());

                                                try {
                                                    mLocated = false;
                                                    mEmail = object.getString("email");
                                                    mName = object.getString("name");
                                                    mImage = "https://graph.facebook.com/" + object.get("id") + "/picture?type=large";
                                                    mParentLayout.setVisibility(View.INVISIBLE);
                                                    mProgressBar.setVisibility(View.VISIBLE);
                                                    getLocation();
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
                                Helper.showSnackBarMessage(getString(R.string.error), SignUpActivity.this);
                            }

                            @Override
                            public void onError(FacebookException exception) {
                                exception.printStackTrace();
                                Helper.showSnackBarMessage(getString(R.string.error), SignUpActivity.this);
                            }
                        });
            }
        });

        mGoogleSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGoogleSignInClient.signOut().addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mGoogleSignInClient.revokeAccess().addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                signIn();
                            }
                        });
                    }
                });


            }
        });

        mSignUpPhoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(SignUpActivity.this,MobileVerificationActivity.class),3);
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            Helper.writeToLog("Google SignUp");
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignUpResult(task);
        } else if (requestCode == 3) {
            if(resultCode == Activity.RESULT_OK){
                String result=data.getStringExtra("result");
                if (result.equals("verified")){
                    mPhoneNumber = data.getStringExtra("phone");
                    startActivity(new Intent(SignUpActivity.this,AccountActivity.class).putExtra("Type","SignUp").putExtra("Phone",mPhoneNumber));
                }
            } if (resultCode == Activity.RESULT_CANCELED) {
                Helper.showSnackBarMessage(getString(R.string.not_verified_mobile),SignUpActivity.this);
            }
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }



    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 0);
    }


    private void handleSignUpResult(Task<GoogleSignInAccount> completedTask) {
        Log.i(TAG,"Here");
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
                    mLocated = false;
                    mParentLayout.setVisibility(View.INVISIBLE);
                    mProgressBar.setVisibility(View.VISIBLE);
                    getLocation();
                }
            } else {
                Helper.writeToLog("Account b null");
            }
        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }


    public List<Address> getAddress(double lat,double lon){
        if (Geocoder.isPresent()) {
            Geocoder geocoder;
            List<Address> addresses = null;
            geocoder = new Geocoder(SignUpActivity.this, Locale.ENGLISH);

            try {
                addresses = geocoder.getFromLocation(lat, lon, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return addresses;
        } else {
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

                            String url = Connector.createSignUpUrl().build().toString() + "?username=" + mEmail + "&name=" +
                                    mName.replaceAll(" ","%20") + "&longitude=" + String.valueOf(mLon) + "&latitude=" + String.valueOf(mLat) +
                                    "&address=" + Uri.encode(mFullAddress) + "&city_id=" + Uri.encode(mCity) +
                                    "&country=" + Uri.encode(mCountry) + "&image=" + mImage.replaceAll(" ","%20") + "&token=" + Helper.getTokenFromSharedPreferences(SignUpActivity.this);
                            mConnector.getRequest(TAG, url);

                        }

                        @Override
                        public void onFailed(Response response, IOException e) {

                        }
                    });
            return null;
        }
    }


    @Override
    public void onStop(){
        super.onStop();
        mConnector.cancelAllRequests(TAG);
    }


    public void getLocation(){
        Helper.writeToLog("Get Location");
        mTracker = new GPSTracker(this, new GPSTracker.OnGetLocation() {
            @Override
            public void onGetLocation(double lat, double lon) {
                if (lon != 0 && lat != 0 && !mLocated){
                    mLat = lat;
                    mLon = lon;
                    mLocated = true;
                    Helper.writeToLog(mLat + " " + mLon);
                    List<Address> addresses = getAddress(mLat,mLon);
                    if (addresses != null && addresses.size() >0) {
                        String url = Connector.createSignUpUrl().build().toString() + "?username=" + mEmail + "&name=" +
                                mName.replaceAll(" ","%20") + "&longitude=" + String.valueOf(mLon) + "&latitude=" + String.valueOf(mLat) +
                                "&address=" + Uri.encode(addresses.get(0).getAddressLine(0)) + "&city_id=" + Uri.encode(addresses.get(0).getAdminArea()) +
                                "&country=" + Uri.encode(addresses.get(0).getCountryName()) + "&image=" + mImage.replaceAll(" ","%20") + "&token=" + Helper.getTokenFromSharedPreferences(SignUpActivity.this);
                        mConnector.getRequest(TAG, url);
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
                    if (addresses != null && addresses.size()> 0) {
                        String url = Connector.createSignUpUrl().build().toString() + "?username=" + mEmail + "&name=" +
                                mName.replaceAll(" ","%20") + "&longitude=" + String.valueOf(mLon) + "&latitude=" + String.valueOf(mLat) +
                                "&address=" + Uri.encode(addresses.get(0).getAddressLine(0)) + "&city_id=" + Uri.encode(addresses.get(0).getAdminArea()) +
                                "&country=" + Uri.encode(addresses.get(0).getCountryName()) + "&image=" + mImage.replaceAll(" ","%20") + "&token=" + Helper.getTokenFromSharedPreferences(SignUpActivity.this);
                        mConnector.getRequest(TAG, url);
                    }
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