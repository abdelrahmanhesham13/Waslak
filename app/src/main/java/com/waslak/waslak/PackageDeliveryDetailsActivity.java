package com.waslak.waslak;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.google.android.gms.maps.model.LatLng;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.waslak.waslak.models.UserModel;
import com.waslak.waslak.networkUtils.Connector;
import com.waslak.waslak.utils.Helper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage;

public class PackageDeliveryDetailsActivity extends AppCompatActivity implements RoutingListener {

    private static final String TAG = "PackageDeliveryDetailsA";

    @BindView(R.id.order_details)
    EditText mOrderDetailsEditText;

    @BindView(R.id.add_photo)
    ImageView mAddPhoto;

    @BindView(R.id.start_delivery_location_parent)
    View mStartDeliveryLocation;

    @BindView(R.id.end_delivery_location_parent)
    View mEndDeliveryLocation;

    @BindView(R.id.start_choose_delivery_location)
    TextView startLocation;

    @BindView(R.id.end_choose_delivery_location)
    TextView endLocation;

    @BindView(R.id.choose_delivery_time)
    TextView mChooseDeliveryTime;

    @BindView(R.id.send)
    Button mSendButton;

    @BindView(R.id.estimated_price)
    TextView mPrice;



    File mSelectedFile;

    UserModel mUserModel;

    ProgressDialog mProgressDialog;

    double mLat = 0;
    double mLon = 0;
    String mAddress = "";
    String mAddressExtraDetails = "";
    String mDuration = "";
    String mCity = "";
    String mCountry = "";
    String mDescription = "";


    double mLatEnd = 0;
    double mLonEnd = 0;
    String mAddressEnd = "";
    String mAddressExtraDetailsEnd = "";
    String mDurationEnd = "";
    String mCityEnd = "";
    String mCountryEnd = "";
    String mDescriptionEnd = "";

    String mImage = "";

    Connector mAddRequestConnector;

    String mTotalDistance;

    String mMinPrice;
    String mPricePerKilo;
    String mMaxPrice;

    Connector mConnectorGetSettings;

    String mCountryLocale;

    String mCurrency;
    String mCurrencyArabic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_delivery_details);
        ButterKnife.bind(this);
        TelephonyManager tm = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
        mCountryLocale = tm.getNetworkCountryIso();

        if (mCountryLocale.contains("sa") || mCountryLocale.contains("SA")) {
            mCountryLocale = "Saudi arabia";
        } else if (mCountryLocale.equalsIgnoreCase("eg")){
            mCountryLocale = "Egypt";
        } else {
            mCountryLocale = "Jordan";
        }



        setTitle(getString(R.string.order_details_package));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUserModel = Helper.getUserSharedPreferences(this);



        if (getIntent().getStringExtra("type").equals("customer")) {
            mAddPhoto.setVisibility(View.GONE);
            mOrderDetailsEditText.setVisibility(View.GONE);
        } else {
            mAddPhoto.setVisibility(View.VISIBLE);
            mOrderDetailsEditText.setVisibility(View.VISIBLE);
        }



        mStartDeliveryLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(PackageDeliveryDetailsActivity.this,MapActivity.class).putExtra("title","start"),1);
            }
        });

        mEndDeliveryLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(PackageDeliveryDetailsActivity.this,MapActivity.class).putExtra("title","destination"),2);
            }
        });

        mAddPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });

        mAddRequestConnector = new Connector(this, new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                mProgressDialog.dismiss();
                if (Connector.checkStatus(response)){
                    Helper.showLongTimeToast(PackageDeliveryDetailsActivity.this,getString(R.string.added_successfully));
                    finish();
                } else {
                    Helper.showSnackBarMessage(getString(R.string.error),PackageDeliveryDetailsActivity.this);
                }

            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                mProgressDialog.dismiss();
                Helper.showSnackBarMessage(getString(R.string.error),PackageDeliveryDetailsActivity.this);
            }
        });


        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDescription = mOrderDetailsEditText.getText().toString();
                if (mLon == 0) {
                    Helper.showSnackBarMessage(getString(R.string.enter_your_location),PackageDeliveryDetailsActivity.this);
                } else if (mLonEnd == 0) {
                    Helper.showSnackBarMessage(getString(R.string.enter_your_location),PackageDeliveryDetailsActivity.this);
                } else {
                    if (getIntent().getStringExtra("type").equals("customer")) {
                        mDescription = "Delivery from " + mAddress + " to " + mAddressEnd;
                        mProgressDialog = Helper.showProgressDialog(PackageDeliveryDetailsActivity.this, getString(R.string.loading), false);
                        mAddRequestConnector.getRequest(TAG, "http://www.as.cta3.com/waslk/api/add_request?user_id=" + mUserModel.getId()
                                + "&longitude=" + mLon + "&latitude=" + mLat + "&address=" + Uri.encode(mAddress) + "&latitude_to=" + mLatEnd + "&longitude_to=" + mLonEnd
                                + "&address_to=" + Uri.encode(mAddressEnd) + "&shop_id=0" + "&city_id=" + Uri.encode(mCity) + "&country=" + Uri.encode(mCountry) + "&duration=" + Uri.encode(mDuration) + "&description=" + Uri.encode(mDescription) + "&detail=" + Uri.encode(mAddressExtraDetails) + "&image=" + Uri.encode(mImage) + "&type=2");
                    } else {
                        if (mDescription.isEmpty()){
                            mDescription = "Delivery from " + mAddress + " to " + mAddressEnd;
                        }
                        mProgressDialog = Helper.showProgressDialog(PackageDeliveryDetailsActivity.this, getString(R.string.loading), false);
                        mAddRequestConnector.getRequest(TAG, "http://www.as.cta3.com/waslk/api/add_request?user_id=" + mUserModel.getId()
                                + "&longitude=" + mLon + "&latitude=" + mLat + "&address=" + Uri.encode(mAddress) + "&latitude_to=" + mLatEnd + "&longitude_to=" + mLonEnd
                                + "&address_to=" + Uri.encode(mAddressEnd) + "&shop_id=0" + "&city_id=" + Uri.encode(mCity) + "&country=" + Uri.encode(mCountry) + "&duration=" + Uri.encode(mDuration) + "&description=" + Uri.encode(mDescription) + "&detail=" + Uri.encode(mAddressExtraDetails) + "&image=" + Uri.encode(mImage) + "&type=1");

                    }
                }
            }
        });


        mConnectorGetSettings = new Connector(this, new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    mMinPrice = jsonObject.getString("min_price");
                    mPricePerKilo = jsonObject.getString("price_per_kilo");
                    mCurrency = jsonObject.getString("currency");
                    mCurrencyArabic = jsonObject.getString("currency_ar");
                    if (getLocale().equals("ar"))
                        mPrice.setText(String.format(Locale.ENGLISH, "%.2f", Double.valueOf(mPricePerKilo) * Double.valueOf(mTotalDistance) + Double.valueOf(mMinPrice)) + " " + mCurrencyArabic + " " + getString(R.string.maximum_price_now));
                    else
                        mPrice.setText(String.format(Locale.ENGLISH, "%.2f", Double.valueOf(mPricePerKilo) * Double.valueOf(mTotalDistance) + Double.valueOf(mMinPrice)) + " " + mCurrency + " " + getString(R.string.maximum_price_now));
                    mMaxPrice = mPrice.getText().toString();
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



    private void pickImage() {
        ImagePicker.create(this)
                .folderMode(true) // folder mode (false by default)
                .toolbarFolderTitle("Image Folder") // folder selection title
                .toolbarImageTitle("Select Image") // image selection title
                .single() //  Max images can be selected
                .showCamera(true) // show camera or not (true by default)
                .start(); // start image picker activity with Request code
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return false;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                mLat=data.getDoubleExtra("lat",0);
                mLon=data.getDoubleExtra("lon",0);
                mAddress=data.getStringExtra("address");
                mAddressExtraDetails=data.getStringExtra("addressExtra");
                mCity=data.getStringExtra("city");
                mCountry=data.getStringExtra("country");
                startLocation.setText(mAddress);
                if (mLatEnd!= 0 && mLonEnd != 0) {
                    LatLng start = new LatLng(Double.valueOf(mLat), Double.valueOf(mLon));
                    LatLng end = new LatLng(mLatEnd, mLonEnd);
                    Location wayPointLocation = new Location("");
                    wayPointLocation.setLatitude(mLat);
                    wayPointLocation.setLongitude(mLon);

                    Location endLocation = new Location("");
                    endLocation.setLatitude(mLatEnd);
                    endLocation.setLongitude(mLonEnd);


                    mTotalDistance = String.valueOf((wayPointLocation.distanceTo(endLocation)) / 1000.0);
                    mConnectorGetSettings.getRequest(TAG, "http://as.cta3.com/waslk/api/get_prices?country=" + mCountryLocale);

                    Routing routing = new Routing.Builder()
                            .travelMode(Routing.TravelMode.DRIVING)
                            .withListener(PackageDeliveryDetailsActivity.this)
                            .waypoints(start, end)
                            .alternativeRoutes(false)
                            .key("AIzaSyAcazeBKVO9e7HvHB9ssU1jc9NhTj_AFsQ")
                            .build();
                    routing.execute();
                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Helper.showSnackBarMessage(getString(R.string.not_located),PackageDeliveryDetailsActivity.this);
            }
        } else if (requestCode == 2) {
            if(resultCode == Activity.RESULT_OK){
                mLatEnd=data.getDoubleExtra("lat",0);
                mLonEnd=data.getDoubleExtra("lon",0);
                mAddressEnd=data.getStringExtra("address");
                mAddressExtraDetailsEnd=data.getStringExtra("addressExtra");
                mCityEnd=data.getStringExtra("city");
                mCountryEnd=data.getStringExtra("country");
                endLocation.setText(mAddressEnd);
                if (mLat != 0 && mLon != 0) {
                    LatLng start = new LatLng(Double.valueOf(mLat), Double.valueOf(mLon));
                    LatLng end = new LatLng(mLatEnd, mLonEnd);
                    Location wayPointLocation = new Location("");
                    wayPointLocation.setLatitude(mLat);
                    wayPointLocation.setLongitude(mLon);

                    Location endLocation = new Location("");
                    endLocation.setLatitude(mLatEnd);
                    endLocation.setLongitude(mLonEnd);


                    mTotalDistance = String.valueOf((wayPointLocation.distanceTo(endLocation)) / 1000.0);
                    mConnectorGetSettings.getRequest(TAG, "http://as.cta3.com/waslk/api/get_prices?country=" + mCountryLocale);
                    Routing routing = new Routing.Builder()
                            .travelMode(Routing.TravelMode.DRIVING)
                            .withListener(PackageDeliveryDetailsActivity.this)
                            .waypoints(start, end)
                            .alternativeRoutes(false)
                            .key("AIzaSyAcazeBKVO9e7HvHB9ssU1jc9NhTj_AFsQ")
                            .build();
                    routing.execute();
                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Helper.showSnackBarMessage(getString(R.string.not_located),PackageDeliveryDetailsActivity.this);
            }
        }


        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            List<Image> images = ImagePicker.getImages(data);
            Image img = images.get(0);
            try {
                Bitmap bitmapImage = Helper.getBitmap(img.getPath(), 200);
                mAddPhoto.setImageBitmap(bitmapImage);
                mSelectedFile = bitmapToFile(img.getName(), checkImage(img.getPath(), getBitmap(img.getPath())));
                UploadImage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

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
    public void onRoutingFailure(RouteException e) {
        e.printStackTrace();
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> arrayList, int i) {
        mChooseDeliveryTime.setText(String.valueOf((arrayList.get(i).getDurationValue() + 2400) / 60) + " mins");
        mDuration = mChooseDeliveryTime.getText().toString();
    }

    @Override
    public void onRoutingCancelled() {

    }

    private Bitmap getBitmap(String path) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(path, bmOptions);
        return getResizedBitmap(bitmap, 2080);
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    private void UploadImage() {
        mProgressDialog = Helper.showProgressDialog(this,
                getString(R.string.uploading_photo),
                false);

        mProgressDialog.show();
        Ion.with(this)
                .load(Connector.createUploadImageUrl())
                .setMultipartFile("parameters[0]", "image", mSelectedFile)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        Helper.writeToLog(result);
                        if (Connector.checkImages(result)) {
                            mProgressDialog.dismiss();
                            ArrayList<String> imagePaths = Connector.getImages(result);
                            mImage = imagePaths.get(0);
                        } else {
                            Helper.showSnackBarMessage(getString(R.string.error), PackageDeliveryDetailsActivity.this);
                            mProgressDialog.dismiss();
                        }
                    }
                });
    }

    private Bitmap checkImage(String path, Bitmap bitmap) throws IOException {

        ExifInterface ei = new ExifInterface(path);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);
        Bitmap rotatedBitmap = null;

        switch (orientation) {

            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap = rotateImage(bitmap, 90);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = rotateImage(bitmap, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = rotateImage(bitmap, 270);
                break;

            case ExifInterface.ORIENTATION_NORMAL:
            default:
                rotatedBitmap = bitmap;
        }


        return rotatedBitmap;
    }


    private File bitmapToFile(String name, Bitmap bmap) throws IOException {
        if (PackageDeliveryDetailsActivity.this.getExternalCacheDir() != null) {
            File f = new File(PackageDeliveryDetailsActivity.this.getExternalCacheDir().getAbsolutePath() + "/" + name);
            boolean crated = f.createNewFile();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            byte[] bitmapData = bos.toByteArray();

            FileOutputStream fos = new FileOutputStream(f);
            fos.write(bitmapData);
            fos.flush();
            fos.close();
            return f;
        }
        return null;
    }

    private String getLocale(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String lang = preferences.getString("lang", "error");
        if (lang.equals("error")) {
            if (Locale.getDefault().getLanguage().equals("ar"))
                return "ar";
            else
                return "en";
        } else if (lang.equals("en")) {
            return "en";
        } else {
            return "ar";
        }
    }


}
