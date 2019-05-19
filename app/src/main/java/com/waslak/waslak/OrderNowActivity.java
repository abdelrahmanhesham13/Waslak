package com.waslak.waslak;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.maps.model.LatLng;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.squareup.picasso.Picasso;
import com.waslak.waslak.models.ShopModel;
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

public class OrderNowActivity extends AppCompatActivity implements RoutingListener {

    private static final String TAG = "OrderNowActivity";
    @BindView(R.id.menu)
    FloatingActionButton mMenu;
    @BindView(R.id.delivery_time_parent)
    View mDeliveryTimeParent;
    @BindView(R.id.choose_delivery_time)
    TextView mChooseDeliveryTime;
    @BindView(R.id.delivery_location_parent)
    View mDeliveryLocationParent;
    @BindView(R.id.finish_button)
    ImageView mFinishBtn;
    @BindView(R.id.description_edit_text)
    EditText mDescriptionEditText;
    @BindView(R.id.progressIndicator)
    ProgressBar mProgressIndicator;
    @BindView(R.id.choose_delivery_location)
    TextView mDeliveryLocation;
    @BindView(R.id.store_image)
    ImageView mStoreImage;
    @BindView(R.id.store_name)
    TextView mStoreName;
    @BindView(R.id.store_address)
    TextView mStoreAddress;
    @BindView(R.id.back_button)
    ImageView mBackButton;
    @BindView(R.id.coordinatorLayout)
    CoordinatorLayout cLayout;
    @BindView(R.id.estimated_price)
    TextView mPrice;

    ProgressDialog mProgressDialog;

    double mLat = 0;
    double mLon = 0;
    String mAddress = "";
    String mAddressExtraDetails = "";
    String mDuration = "";
    String mCity = "";
    String mCountry = "";
    String mDescription = "";
    String mImage;

    Connector mConnector;

    UserModel mUserModel;
    ShopModel mShopModel;

    Connector mConnectorGetSettings;

    File mSelectedFile;

    String mTotalDistance;

    String mMinPrice;
    String mPricePerKilo;
    String mMaxPrice;

    String mCountryLocale;

    String mCurrency;
    String mCurrencyArabic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_now);
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


        if (getIntent() != null) {
            mUserModel = (UserModel) getIntent().getSerializableExtra("user");
            mShopModel = (ShopModel) getIntent().getSerializableExtra("ShopModel");
            mStoreName.setText(mShopModel.getName());
            mStoreAddress.setText(mShopModel.getAddress());
            if (!mShopModel.getImage().isEmpty())
                Picasso.get().load(mShopModel.getImage()).into(mStoreImage);
        }

        mMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });

        mDeliveryTimeParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showDialog();
            }
        });

        mDeliveryLocationParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(OrderNowActivity.this, MapActivity.class), 1);
            }
        });

        mFinishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDescription = mDescriptionEditText.getText().toString();
                mDuration = mChooseDeliveryTime.getText().toString();
                if (mAddress.isEmpty()) {
                    Helper.showSnackBarMessage(getString(R.string.enter_your_location), OrderNowActivity.this);
                } else if (mDescription.isEmpty()) {
                    Helper.showSnackBarMessage(getString(R.string.enter_your_description), OrderNowActivity.this);
                } else if (mDuration.isEmpty()) {
                    Helper.showSnackBarMessage(getString(R.string.enter_your_duration), OrderNowActivity.this);
                } else {
                    mProgressDialog = Helper.showProgressDialog(OrderNowActivity.this, getString(R.string.loading), false);
                    mConnector.getRequest(TAG, "http://www.as.cta3.com/waslk/api/add_request?user_id=" + mUserModel.getId() + "&shop_id=" + mShopModel.getId() + "&longitude=" + mLon + "&latitude=" + mLat + "&city_id=" + Uri.encode(mCity) + "&country=" + Uri.encode(mCountry) + "&address=" + Uri.encode(mAddress) + "&duration=" + Uri.encode(mDuration) + "&description=" + Uri.encode(mDescription) + "&detail=" + Uri.encode(mAddressExtraDetails) + "&image=" + Uri.encode(mImage));
                }
            }
        });

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        mConnector = new Connector(this, new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                mProgressDialog.dismiss();
                if (Connector.checkStatus(response)) {
                    finish();
                } else {

                    Helper.showSnackBarMessage(getString(R.string.error), OrderNowActivity.this);
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                mProgressDialog.dismiss();
                Helper.showSnackBarMessage(getString(R.string.error), OrderNowActivity.this);
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


    public void showDialog() {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        final CharSequence items[] = new CharSequence[]{getString(R.string.one_hour), getString(R.string.two_hour), getString(R.string.three_hour), getString(R.string.one_day), getString(R.string.two_day), getString(R.string.three_day)};
        adb.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mDuration = items[which].toString();
                mChooseDeliveryTime.setText(items[which]);
                dialog.dismiss();
            }
        });
        adb.setNegativeButton(getString(R.string.cancel), null);
        adb.setTitle(getString(R.string.select_time));
        adb.show();
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                mLat = data.getDoubleExtra("lat", 0);
                mLon = data.getDoubleExtra("lon", 0);
                mAddress = data.getStringExtra("address");
                mAddressExtraDetails = data.getStringExtra("addressExtra");
                mCity = data.getStringExtra("city");
                mCountry = data.getStringExtra("country");
                mDeliveryLocation.setText(mAddress);

                Location wayPointLocation = new Location("");
                wayPointLocation.setLatitude(Double.parseDouble(mShopModel.getLat()));
                wayPointLocation.setLongitude(Double.parseDouble(mShopModel.getLon()));

                Location endLocation = new Location("");
                endLocation.setLatitude(mLat);
                endLocation.setLongitude(mLon);


                mTotalDistance = String.valueOf((wayPointLocation.distanceTo(endLocation)) / 1000.0);
                mConnectorGetSettings.getRequest(TAG, "http://as.cta3.com/waslk/api/get_prices?country=" + mCountryLocale);
                LatLng start = new LatLng(Double.valueOf(mShopModel.getLat()), Double.valueOf(mShopModel.getLon()));
                LatLng end = new LatLng(mLat, mLon);
                Routing routing = new Routing.Builder()
                        .travelMode(Routing.TravelMode.DRIVING)
                        .withListener(OrderNowActivity.this)
                        .waypoints(start, end)
                        .alternativeRoutes(false)
                        .key("AIzaSyAcazeBKVO9e7HvHB9ssU1jc9NhTj_AFsQ")
                        .build();
                routing.execute();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Helper.showSnackBarMessage(getString(R.string.not_located), OrderNowActivity.this);
            }
        }


        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            List<Image> images = ImagePicker.getImages(data);
            Image img = images.get(0);
            try {
                Bitmap bitmapImage = Helper.getBitmap(img.getPath(), 200);
                mSelectedFile = bitmapToFile(img.getName(), checkImage(img.getPath(), getBitmap(img.getPath())));
                UploadImage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

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
                            Helper.showSnackBarMessage(getString(R.string.error), OrderNowActivity.this);
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
        if (OrderNowActivity.this.getExternalCacheDir() != null) {
            File f = new File(OrderNowActivity.this.getExternalCacheDir().getAbsolutePath() + "/" + name);
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

    @Override
    protected void onStop() {
        super.onStop();
        if (mConnector != null) {
            mConnector.cancelAllRequests(TAG);
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

    @Override
    public void onRoutingFailure(RouteException e) {

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> arrayList, int i) {
        mChooseDeliveryTime.setText(String.valueOf((arrayList.get(i).getDurationValue() + 2400) / 60) + " mins");
    }

    @Override
    public void onRoutingCancelled() {

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
