package com.waslak.waslak;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.InputFilter;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.lamudi.phonefield.PhoneInputLayout;
import com.waslak.waslak.networkUtils.Connector;
import com.waslak.waslak.networkUtils.Constants;
import com.waslak.waslak.utils.Helper;

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

public class BecomeFeaturedClient extends AppCompatActivity {


    @BindView(R.id.national_id)
    ImageView mNationalIdPhoto;
    @BindView(R.id.phone_input_layout)
    PhoneInputLayout mMobileNumberEditText;
    @BindView(R.id.send_data)
    Button mSendData;
    @BindView(R.id.car_image)
    ImageView mCarImage;

    File mSelectedFile;
    ProgressDialog mProgressDialog;
    String mImage = "";
    String mNonConvicts = "";
    String mCar = "";
    Connector mConnector;
    int mobileLength;

    int mType = -1;

    private final String TAG = BecomeFeaturedClient.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_become_featured_client);
        setTitle(getString(R.string.become_featured_client));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        ButterKnife.bind(this);

        mMobileNumberEditText.setHint(R.string.enter_phone);
        mMobileNumberEditText.setDefaultCountry("SA");
        mMobileNumberEditText.setTextColor(getResources().getColor(R.color.colorPrimary));


        mConnector = new Connector(this, new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                if (Connector.checkStatus(response)) {
                    if (mProgressDialog.isShowing() && mProgressDialog != null && !BecomeFeaturedClient.this.isFinishing())
                        mProgressDialog.dismiss();
                    Toast.makeText(BecomeFeaturedClient.this, getString(R.string.be_agent_response), Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    if (mProgressDialog.isShowing() && mProgressDialog != null && !BecomeFeaturedClient.this.isFinishing())
                        mProgressDialog.dismiss();
                    Helper.showSnackBarMessage(getString(R.string.error), BecomeFeaturedClient.this);
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                if (mProgressDialog.isShowing() && mProgressDialog != null && !BecomeFeaturedClient.this.isFinishing())
                    mProgressDialog.dismiss();
                Helper.showSnackBarMessage(getString(R.string.error), BecomeFeaturedClient.this);
            }
        });

        mNationalIdPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mType = 0;
                pickImage();
            }
        });

        mCarImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mType = 2;
                pickImage();
            }
        });

        mSendData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mImage.isEmpty()) {
                    Helper.showSnackBarMessage(getString(R.string.add_your_national_id), BecomeFeaturedClient.this);
                } else if (mCar.isEmpty()) {
                    Helper.showSnackBarMessage(getString(R.string.add_your_car_image), BecomeFeaturedClient.this);
                } else if (mMobileNumberEditText.getPhoneNumber().isEmpty()) {
                    Helper.showSnackBarMessage(getString(R.string.enter_phone), BecomeFeaturedClient.this);
                } else if (mMobileNumberEditText.getPhoneNumber().isEmpty()) {
                    Helper.showSnackBarMessage(getString(R.string.enter_phone_number), BecomeFeaturedClient.this);
                } else if (!mMobileNumberEditText.isValid()) {
                    Helper.showSnackBarMessage(getString(R.string.invalid_phone_number), BecomeFeaturedClient.this);
                }  else {
//                    mProgressDialog = Helper.showProgressDialog(BecomeFeaturedClient.this, getString(R.string.loading), false);
//                    mConnector.getRequest(TAG, "http://www.waslakbooking.com/waslk/api/become_client?user_id=" + Helper.getUserSharedPreferences(BecomeFeaturedClient.this).getId() + "&national_photo=" + mImage + "&mobile=" + Uri.encode(mMobileNumberEditText.getText().toString()) + "&address_photo=" + mCar);
                    startActivityForResult(new Intent(BecomeFeaturedClient.this, MobileVerificationActivity.class).putExtra("mobile",mMobileNumberEditText.getPhoneNumber().toString()), 3);
                }
            }
        });

        TelephonyManager tm = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
        String mCountryLocale = tm.getNetworkCountryIso();

        if (mCountryLocale.contains("sa") || mCountryLocale.contains("SA")) {
            mobileLength = 13;
        } else if (mCountryLocale.equalsIgnoreCase("eg")){
            mobileLength = 13;
        } else if (mCountryLocale.equalsIgnoreCase("jo")) {
            mobileLength = 14;
        } else {
            mCountryLocale = "";
        }

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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return false;
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            List<Image> images = ImagePicker.getImages(data);
            if (images != null) {
                Image img = images.get(0);
                try {
                    if (mType == 0) {
                        Bitmap bitmapImage = Helper.getBitmap(img.getPath(), 2080);
                        mNationalIdPhoto.setImageBitmap(bitmapImage);
                        mSelectedFile = bitmapToFile(img.getName(), checkImage(img.getPath(), getBitmap(img.getPath())));
                        UploadImage();
                    } else if (mType == 2) {
                        Bitmap bitmapImage = Helper.getBitmap(img.getPath(), 2080);
                        mCarImage.setImageBitmap(bitmapImage);
                        mSelectedFile = bitmapToFile(img.getName(), checkImage(img.getPath(), getBitmap(img.getPath())));
                        UploadImage();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        if (requestCode == 3) {
            if (resultCode == Activity.RESULT_OK) {
                String result = data.getStringExtra("result");
                if (result.equals("verified")) {
                    //mPhoneNumber = data.getStringExtra("phone");
                    mProgressDialog = Helper.showProgressDialog(BecomeFeaturedClient.this, getString(R.string.loading), false);
                    mConnector.getRequest(TAG, Constants.WASLAK_BASE_URL + "/mobile/api/become_client?user_id=" + Helper.getUserSharedPreferences(BecomeFeaturedClient.this).getId() + "&national_photo=" + mImage + "&mobile=" + Uri.encode(mMobileNumberEditText.getPhoneNumber()) + "&address_photo=" + mCar);

                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Helper.showSnackBarMessage(getString(R.string.not_verified_mobile), BecomeFeaturedClient.this);
            }
        }
    }


    private File bitmapToFile(String name, Bitmap bmap) throws IOException {
        if (BecomeFeaturedClient.this.getExternalCacheDir() != null) {
            File f = new File(BecomeFeaturedClient.this.getExternalCacheDir().getAbsolutePath() + "/" + name);
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


    private Bitmap getBitmap(String path) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(path, bmOptions);
        return getResizedBitmap(bitmap, 600);
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
                            if (mProgressDialog.isShowing() && mProgressDialog != null && !BecomeFeaturedClient.this.isFinishing())
                                mProgressDialog.dismiss();
                            ArrayList<String> imagePaths = Connector.getImages(result);
                            if (mType == 0)
                                mImage = imagePaths.get(0);
                            else if (mType == 1)
                                mNonConvicts = imagePaths.get(0);
                            else if (mType == 2)
                                mCar = imagePaths.get(0);
                        } else {
                            Helper.showSnackBarMessage(getString(R.string.error), BecomeFeaturedClient.this);
                            if (mProgressDialog.isShowing() && mProgressDialog != null && !BecomeFeaturedClient.this.isFinishing())
                                mProgressDialog.dismiss();
                        }
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mConnector != null)
            mConnector.cancelAllRequests(TAG);
    }

}
