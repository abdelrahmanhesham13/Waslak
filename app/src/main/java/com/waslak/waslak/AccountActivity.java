package com.waslak.waslak;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.squareup.picasso.Picasso;
import com.waslak.waslak.models.UserModel;
import com.waslak.waslak.networkUtils.Connector;
import com.waslak.waslak.utils.GPSTracker;
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
import se.arbitur.geocoding.Callback;
import se.arbitur.geocoding.Constants.AddressTypes;
import se.arbitur.geocoding.Constants.LocationTypes;
import se.arbitur.geocoding.Response;
import se.arbitur.geocoding.Result;
import se.arbitur.geocoding.ReverseGeocoding;

import static com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage;

public class AccountActivity extends AppCompatActivity {

    private static final String TAG = "AccountActivity";
    @BindView(R.id.male_text_view)
    TextView mMaleTextView;
    @BindView(R.id.female_text_view)
    TextView mFemaleTextView;
    @BindView(R.id.setting_button)
    TextView mSettingButton;
    @BindView(R.id.menu_button)
    ImageView mMenuButton;
    @BindView(R.id.save_register)
    Button mSaveRegisterButton;
    @BindView(R.id.mobile_number)
    EditText mMobileNumberEditText;
    @BindView(R.id.progressIndicator)
    ProgressBar mProgressBar;
    @BindView(R.id.parent_layout)
    View mParentLayout;
    @BindView(R.id.full_name_edit_text)
    EditText mFullNameEditText;
    @BindView(R.id.email)
    EditText mEmailEditText;
    @BindView(R.id.birth_date)
    EditText mBirthDateEditText;
    @BindView(R.id.profile_image)
    ImageView mProfileImage;

    Connector mConnector;

    UserModel mUserModel;

    GPSTracker mTracker;

    ProgressDialog mProgressDialog;

    double mLat = 0;
    double mLon = 0;

    boolean mLocated = false;

    int mGender = -1;

    String mFullName, mEmail, mBirthDate, mPhoneNumber;
    String mImage = "";
    File mSelectedFile;

    String mFullAddress;
    String mCountry;
    String mCity;

    List<Address> addresses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        ButterKnife.bind(this);

        mConnector = new Connector(this, new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                if (Connector.checkStatus(response)) {
                    if (getIntent().getStringExtra("Type").equals("SignUp")) {
                        mUserModel = Connector.getUser(response);
                        Helper.SaveToSharedPreferences(AccountActivity.this, mUserModel);
                        startActivity(new Intent(AccountActivity.this, HomeActivity.class).putExtra("user", mUserModel).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                        finish();
                    } else {
                        Helper.showSnackBarMessage(getString(R.string.updated_successfully), AccountActivity.this);
                        mUserModel.setName(mFullName);
                        mUserModel.setUsername(mEmail);
                        mUserModel.setBirthDate(mBirthDate);
                        mUserModel.setMobile(mPhoneNumber);
                        mUserModel.setImage(mImage);
                        mUserModel.setGender(String.valueOf(mGender));
                        Helper.SaveToSharedPreferences(AccountActivity.this, mUserModel);
                        mProgressBar.setVisibility(View.INVISIBLE);
                        mParentLayout.setVisibility(View.VISIBLE);
                    }
                } else {
                    Helper.showSnackBarMessage(getString(R.string.registered_before), AccountActivity.this);
                    mProgressBar.setVisibility(View.INVISIBLE);
                    mParentLayout.setVisibility(View.VISIBLE);
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                Helper.showSnackBarMessage(getString(R.string.error), AccountActivity.this);
                mProgressBar.setVisibility(View.INVISIBLE);
                mParentLayout.setVisibility(View.VISIBLE);
            }
        });

        mMenuButton.setVisibility(View.GONE);

        mMaleTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMaleTextView.setTextColor(getResources().getColor(R.color.colorPrimary));
                mFemaleTextView.setTextColor(getResources().getColor(R.color.whiteColor));
                mGender = 0;
            }
        });

        mFemaleTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMaleTextView.setTextColor(getResources().getColor(R.color.whiteColor));
                mFemaleTextView.setTextColor(getResources().getColor(R.color.colorPrimary));
                mGender = 1;
            }
        });

        mSettingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AccountActivity.this, SettingActivity.class));
            }
        });


        if (getIntent() != null) {
            if (getIntent().hasExtra("Type")) {
                String type = getIntent().getStringExtra("Type");
                if (type.equals("SignUp")) {
                    mSettingButton.setText(getString(R.string.register));
                    mSaveRegisterButton.setText(getString(R.string.register));
                    mSettingButton.setOnClickListener(null);
                    mSaveRegisterButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            signUp();
                        }
                    });
                    mMobileNumberEditText.setText(getIntent().getStringExtra("Phone"));
                    mMobileNumberEditText.setEnabled(false);
                    mSettingButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                } else if (type.equals("Edit")) {
                    mUserModel = (UserModel) getIntent().getSerializableExtra("user");
                    mUserModel = Helper.getUserSharedPreferences(AccountActivity.this);
                    mSaveRegisterButton.setText(getString(R.string.save));
                    mMobileNumberEditText.setText(mUserModel.getMobile());
                    mEmailEditText.setText(mUserModel.getUsername());
                    mBirthDateEditText.setText(mUserModel.getBirthDate());
                    mFullNameEditText.setText(mUserModel.getName());
                    mImage = mUserModel.getImage();
                    mSaveRegisterButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            editProfile();
                        }
                    });
                    if (URLUtil.isValidUrl(mUserModel.getImage()))
                        Picasso.get().load(mUserModel.getImage()).fit().centerCrop().into(mProfileImage);
                    else {
                        Picasso.get().load("http://www.as.cta3.com/waslk/prod_img/" + mUserModel.getImage()).fit().centerCrop().into(mProfileImage);
                    }
                    if (mUserModel.getGender().equals("0")) {
                        mMaleTextView.setTextColor(getResources().getColor(R.color.colorPrimary));
                        mFemaleTextView.setTextColor(getResources().getColor(R.color.whiteColor));
                        mGender = 0;
                    } else if (mUserModel.getGender().equals("1")) {
                        mMaleTextView.setTextColor(getResources().getColor(R.color.whiteColor));
                        mFemaleTextView.setTextColor(getResources().getColor(R.color.colorPrimary));
                        mGender = 1;
                    }
                }
            }
        }

        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });

    }

    public void signUp() {
        mFullName = mFullNameEditText.getText().toString();
        mPhoneNumber = mMobileNumberEditText.getText().toString();
        mBirthDate = mBirthDateEditText.getText().toString();
        mEmail = mEmailEditText.getText().toString();

        if (TextUtils.isEmpty(mFullName)) {
            Helper.showSnackBarMessage(getString(R.string.enter_full_name), AccountActivity.this);
        } else if (TextUtils.isEmpty(mPhoneNumber)) {
            Helper.showSnackBarMessage(getString(R.string.enter_phone_number), AccountActivity.this);
        } else if (TextUtils.isEmpty(mBirthDate)) {
            Helper.showSnackBarMessage(getString(R.string.enter_your_birth_date), AccountActivity.this);
        } else if (!Patterns.EMAIL_ADDRESS.matcher(mEmail).matches() || TextUtils.isEmpty(mEmail)) {
            Helper.showSnackBarMessage(getString(R.string.enter_your_email), AccountActivity.this);
        } else if (mGender == -1) {
            Helper.showSnackBarMessage(getString(R.string.enter_your_gender), AccountActivity.this);
        } else if (mImage.isEmpty()) {
            Helper.showSnackBarMessage(getString(R.string.insert_your_photo), AccountActivity.this);
        } else {
            mLocated = false;
            mParentLayout.setVisibility(View.INVISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);
            getLocationSignUp();
        }
    }


    public void editProfile() {
        mFullName = mFullNameEditText.getText().toString();
        mPhoneNumber = mMobileNumberEditText.getText().toString();
        mBirthDate = mBirthDateEditText.getText().toString();
        mEmail = mEmailEditText.getText().toString();

        if (TextUtils.isEmpty(mFullName)) {
            Helper.showSnackBarMessage(getString(R.string.enter_full_name), AccountActivity.this);
        } else if (TextUtils.isEmpty(mPhoneNumber)) {
            Helper.showSnackBarMessage(getString(R.string.enter_phone_number), AccountActivity.this);
        } else if (TextUtils.isEmpty(mBirthDate)) {
            Helper.showSnackBarMessage(getString(R.string.enter_your_birth_date), AccountActivity.this);
        } else if (!Patterns.EMAIL_ADDRESS.matcher(mEmail).matches() || TextUtils.isEmpty(mEmail)) {
            Helper.showSnackBarMessage(getString(R.string.enter_your_email), AccountActivity.this);
        } else if (mGender == -1) {
            Helper.showSnackBarMessage(getString(R.string.enter_your_gender), AccountActivity.this);
        } else if (mImage.isEmpty()) {
            Helper.showSnackBarMessage(getString(R.string.insert_your_photo), AccountActivity.this);
        } else {
            mLocated = false;
            mParentLayout.setVisibility(View.INVISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);
            getLocationEdit();
        }
    }


    public List<Address> getAddress(double lat, double lon, final String type) {
        if (Geocoder.isPresent()) {
            Geocoder geocoder;
            geocoder = new Geocoder(AccountActivity.this, Locale.ENGLISH);

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

                            if (type.equals("edit")) {
                                String url = Connector.createEditProfileUrl() + "?username=" + mEmail + "&name=" +
                                        mFullName.replaceAll(" ", "%20") + "&longitude=" + String.valueOf(mLon) + "&latitude=" + String.valueOf(mLat) +
                                        "&address=" + Uri.encode(mFullAddress) + "&city_id=" + Uri.encode(mCity) +
                                        "&country=" + Uri.encode(mCountry) + "&image=" + mImage + "&token=" + Helper.getTokenFromSharedPreferences(AccountActivity.this) + "&mobile=" + mPhoneNumber + "&gender=" + String.valueOf(mGender) + "&id=" + mUserModel.getId() + "&birth_date=" + mBirthDate;
                                mConnector.getRequest(TAG, url);
                            } else {
                                String url = Connector.createSignUpUrl().build().toString() + "?username=" + mEmail + "&name=" +
                                        mFullName.replaceAll(" ", "%20") + "&longitude=" + String.valueOf(mLon) + "&latitude=" + String.valueOf(mLat) +
                                        "&address=" + Uri.encode(mFullAddress) + "&city_id=" + Uri.encode(mCity) +
                                        "&country=" + Uri.encode(mCountry) + "&image=" + mImage + "&token=" + Helper.getTokenFromSharedPreferences(AccountActivity.this) + "&mobile=" + mPhoneNumber + "&gender=" + String.valueOf(mGender) + "&birth_date=" + mBirthDate;
                                mConnector.getRequest(TAG, url);
                            }

                        }

                        @Override
                        public void onFailed(Response response, IOException e) {

                        }
                    });
            return null;
        }
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
                            Helper.showSnackBarMessage(getString(R.string.error), AccountActivity.this);
                            mProgressDialog.dismiss();
                        }
                    }
                });
    }

    private Bitmap getBitmap(String path, int size) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        // Calculate inSampleSize
        bmOptions.inSampleSize = calculateInSampleSize(bmOptions, 500, 500);

        // Decode bitmap with inSampleSize set
        bmOptions.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(path, bmOptions);
        if (bitmap == null)
            return null;

        return getResizedBitmap(bitmap, size);
    }

    private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
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


    private void pickImage() {
        ImagePicker.create(this)
                .folderMode(true) // folder mode (false by default)
                .toolbarFolderTitle("Image Folder") // folder selection title
                .toolbarImageTitle("Select Image") // image selection title
                .single() //  Max images can be selected
                .showCamera(true) // show camera or not (true by default)
                .start(); // start image picker activity with Request code
    }


    private File bitmapToFile(String name, Bitmap bmap) throws IOException {
        if (AccountActivity.this.getExternalCacheDir() != null) {
            File f = new File(AccountActivity.this.getExternalCacheDir().getAbsolutePath() + "/" + name);
            boolean crated = f.createNewFile();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bmap.compress(Bitmap.CompressFormat.JPEG, 70, bos);
            byte[] bitmapData = bos.toByteArray();

            FileOutputStream fos = new FileOutputStream(f);
            fos.write(bitmapData);
            fos.flush();
            fos.close();
            return f;
        }
        return null;
    }


    public void getLocationEdit() {
        mTracker = new GPSTracker(this, new GPSTracker.OnGetLocation() {
            @Override
            public void onGetLocation(double lat, double lon) {
                if (lon != 0 && lat != 0 && !mLocated) {
                    mLocated = true;
                    mLat = lat;
                    mLon = lon;
                    addresses = getAddress(mLat, mLon, "edit");
                    if (addresses != null && addresses.size() > 0) {
//                        String url = Connector.createEditProfileUrl() + "?username=" + mEmail + "&name=" +
//                                mFullName.replaceAll(" ", "%20") + "&longitude=" + String.valueOf(mLon) + "&latitude=" + String.valueOf(mLat) +
//                                "&address=" + Uri.encode(addresses.get(0).getAddressLine(0)) + "&city_id=" + Uri.encode(addresses.get(0).getAdminArea()) +
//                                "&country=" + Uri.encode(addresses.get(0).getCountryName()) + "&image=" + mImage + "&token=" + Helper.getTokenFromSharedPreferences(AccountActivity.this) + "&mobile=" + mPhoneNumber + "&gender=" + String.valueOf(mGender) + "&id=" + mUserModel.getId() + "&birth_date=" + mBirthDate;
//                        mConnector.getRequest(TAG, url);
                        startActivityForResult(new Intent(AccountActivity.this, MobileVerificationActivity.class).putExtra("mobile",mPhoneNumber), 3);
                    }
                    mTracker.stopUsingGPS();
                }
            }
        });

        if (mTracker.canGetLocation() && !mLocated) {
            Location location = mTracker.getLocation();
            if (location != null) {
                if (location.getLongitude() != 0 && location.getLatitude() != 0) {
                    mLocated = true;
                    mLat = location.getLatitude();
                    mLon = location.getLongitude();
                    addresses = getAddress(mLat, mLon, "edit");
                    if (addresses != null && addresses.size() > 0) {
//                        String url = Connector.createEditProfileUrl() + "?username=" + mEmail + "&name=" +
//                                mFullName.replaceAll(" ", "%20") + "&longitude=" + String.valueOf(mLon) + "&latitude=" + String.valueOf(mLat) +
//                                "&address=" + Uri.encode(addresses.get(0).getAddressLine(0)) + "&city_id=" + Uri.encode(addresses.get(0).getAdminArea()) +
//                                "&country=" + Uri.encode(addresses.get(0).getCountryName()) + "&image=" + mImage + "&token=" + Helper.getTokenFromSharedPreferences(AccountActivity.this) + "&mobile=" + mPhoneNumber + "&gender=" + String.valueOf(mGender) + "&id=" + mUserModel.getId() + "&birth_date=" + mBirthDate;
//                        mConnector.getRequest(TAG, url);
                        }
                    mTracker.stopUsingGPS();
                }
            }
        }
    }


    public void getLocationSignUp() {
        mTracker = new GPSTracker(this, new GPSTracker.OnGetLocation() {
            @Override
            public void onGetLocation(double lat, double lon) {
                if (lon != 0 && lat != 0 && !mLocated) {
                    mLocated = true;
                    mLat = lat;
                    mLon = lon;
                    addresses = getAddress(mLat, mLon, "sign up");
                    if (addresses != null && addresses.size() > 0) {
                        String url = Connector.createSignUpUrl().build().toString() + "?username=" + mEmail + "&name=" +
                                mFullName.replaceAll(" ", "%20") + "&longitude=" + String.valueOf(mLon) + "&latitude=" + String.valueOf(mLat) +
                                "&address=" + Uri.encode(addresses.get(0).getAddressLine(0)) + "&city_id=" + Uri.encode(addresses.get(0).getAdminArea()) +
                                "&country=" + Uri.encode(addresses.get(0).getCountryName()) + "&image=" + mImage + "&token=" + Helper.getTokenFromSharedPreferences(AccountActivity.this) + "&mobile=" + mPhoneNumber + "&gender=" + String.valueOf(mGender) + "&birth_date=" + mBirthDate;
                        mConnector.getRequest(TAG, url);
                    }
                    mTracker.stopUsingGPS();
                }
            }
        });

        if (mTracker.canGetLocation() && !mLocated) {
            Location location = mTracker.getLocation();
            if (location != null) {
                if (location.getLongitude() != 0 && location.getLatitude() != 0) {
                    mLocated = true;
                    mLat = location.getLatitude();
                    mLon = location.getLongitude();
                    addresses = getAddress(mLat, mLon, "sign up");
                    if (addresses != null && addresses.size() > 0) {
                        String url = Connector.createSignUpUrl().build().toString() + "?username=" + mEmail + "&name=" +
                                mFullName.replaceAll(" ", "%20") + "&longitude=" + String.valueOf(mLon) + "&latitude=" + String.valueOf(mLat) +
                                "&address=" + Uri.encode(addresses.get(0).getAddressLine(0)) + "&city_id=" + Uri.encode(addresses.get(0).getAdminArea()) +
                                "&country=" + Uri.encode(addresses.get(0).getCountryName()) + "&image=" + mImage + "&token=" + Helper.getTokenFromSharedPreferences(AccountActivity.this) + "&mobile=" + mPhoneNumber + "&gender=" + String.valueOf(mGender) + "&birth_date=" + mBirthDate;
                        mConnector.getRequest(TAG, url);
                    }
                    mTracker.stopUsingGPS();
                }
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 3) {
            if (resultCode == Activity.RESULT_OK) {
                String result = data.getStringExtra("result");
                if (result.equals("verified")) {
                    mPhoneNumber = data.getStringExtra("phone");
                    if (getIntent().getStringExtra("Type").equals("SignUp")) {
                        String url = Connector.createSignUpUrl().build().toString() + "?username=" + mEmail + "&name=" +
                                mFullName.replaceAll(" ", "%20") + "&longitude=" + String.valueOf(mLon) + "&latitude=" + String.valueOf(mLat) +
                                "&address=" + Uri.encode(addresses.get(0).getAddressLine(0)) + "&city_id=" + Uri.encode(addresses.get(0).getAdminArea()) +
                                "&country=" + Uri.encode(addresses.get(0).getCountryName()) + "&image=" + mImage + "&token=" + Helper.getTokenFromSharedPreferences(AccountActivity.this) + "&mobile=" + mPhoneNumber + "&gender=" + String.valueOf(mGender) + "&birth_date=" + mBirthDate;
                        mConnector.getRequest(TAG, url);
                    } else {
                        String url = Connector.createEditProfileUrl() + "?username=" + mEmail + "&name=" +
                                mFullName.replaceAll(" ", "%20") + "&longitude=" + String.valueOf(mLon) + "&latitude=" + String.valueOf(mLat) +
                                "&address=" + Uri.encode(addresses.get(0).getAddressLine(0)) + "&city_id=" + Uri.encode(addresses.get(0).getAdminArea()) +
                                "&country=" + Uri.encode(addresses.get(0).getCountryName()) + "&image=" + mImage + "&token=" + Helper.getTokenFromSharedPreferences(AccountActivity.this) + "&mobile=" + mPhoneNumber + "&gender=" + String.valueOf(mGender) + "&id=" + mUserModel.getId() + "&birth_date=" + mBirthDate;
                        mConnector.getRequest(TAG, url);
                    }
                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Helper.showSnackBarMessage(getString(R.string.not_verified_mobile), AccountActivity.this);
            }
        }

        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            List<Image> images = ImagePicker.getImages(data);
            if (images != null) {
                Image img = images.get(0);
                try {
                    Bitmap bitmapImage = Helper.getBitmap(img.getPath(), 200);
                    mProfileImage.setImageBitmap(bitmapImage);
                    mSelectedFile = bitmapToFile(img.getName(), checkImage(img.getPath(), getBitmap(img.getPath(), 200)));
                    UploadImage();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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

}
