package com.waslak.waslak;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.model.LatLng;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.nguyenhoanglam.imagepicker.model.Config;
import com.nguyenhoanglam.imagepicker.model.Image;
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePicker;
import com.waslak.waslak.networkUtils.Connector;
import com.waslak.waslak.utils.Helper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage;

public class PackageDeliveryDetailsActivity extends AppCompatActivity implements RoutingListener {

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

    File mSelectedFile;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_delivery_details);
        ButterKnife.bind(this);



        setTitle(getString(R.string.order_details_package));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



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
                startActivityForResult(new Intent(PackageDeliveryDetailsActivity.this,MapActivity.class),1);
            }
        });

        mEndDeliveryLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(PackageDeliveryDetailsActivity.this,MapActivity.class),2);
            }
        });

        mAddPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });

    }



    private void pickImage() {
        ImagePicker.with(this)
                .setFolderMode(true) // folder mode (false by default)
                .setFolderTitle("Image Folder") // folder selection title
                .setImageTitle("Select Image") // image selection title
                .setMaxSize(1) //  Max images can be selected
                .setMultipleMode(false) //single mode
                .setShowCamera(true) // show camera or not (true by default)
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
                endLocation.setText(mAddress);
                if (mLat != 0 && mLon != 0) {
                    LatLng start = new LatLng(Double.valueOf(mLat), Double.valueOf(mLon));
                    LatLng end = new LatLng(mLatEnd, mLonEnd);
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


        if (requestCode == Config.RC_PICK_IMAGES && resultCode == RESULT_OK && data != null) {
            ArrayList<Image> images = data.getParcelableArrayListExtra(Config.EXTRA_IMAGES);
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


}
