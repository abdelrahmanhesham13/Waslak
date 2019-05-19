package com.waslak.waslak.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.waslak.waslak.models.UserModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import static com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage;

/**
 * Created by Abdelrahman Hesham on 3/9/2018.
 */

public class Helper {

    public static void showLongTimeToast(Context context, String message) {

        Toast.makeText(context, message, Toast.LENGTH_LONG).show();

    }

    public static void writeToLog(String message) {
        if (message != null)
            Log.i("Helper Log", message);

    }


    public static boolean validateEmail(String email) {

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return false;
        } else {
            return true;
        }

    }

    public static boolean validateFields(String name) {

        return !TextUtils.isEmpty(name);
    }

    public static boolean validateMobile(String string) {

        return !(TextUtils.isEmpty(string) || string.length() != 10);
    }

    public static void showSnackBarMessage(String message, AppCompatActivity activity) {

        if (activity.findViewById(android.R.id.content) != null) {

            Snackbar.make(activity.findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
        }
    }

    public static void hideKeyboard(AppCompatActivity activity, View v) {
        View currentFocus = activity.getCurrentFocus();
        if (currentFocus != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        }
    }


    public static boolean isJSONValid(String test) {
        if (test == null)
            return false;

        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            // edited, to include @Arthur's comment
            // e.g. in case JSONArray is valid as well...
            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }


    public static String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }


    public static void SaveToSharedPreferences(Context context, UserModel userModel) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("id", userModel.getId());
        editor.putString("email", userModel.getUsername());
        editor.putString("mobile", userModel.getMobile());
        editor.putString("name", userModel.getName());
        editor.putString("role", String.valueOf(userModel.getRole()));
        editor.putString("birth_date", userModel.getBirthDate());
        editor.putString("city", userModel.getCity());
        editor.putString("comment", userModel.getComment());
        editor.putString("country", userModel.getCountry());
        editor.putString("gender", userModel.getGender());
        editor.putString("image", userModel.getImage());
        editor.putString("latitude", userModel.getLatitude());
        editor.putString("longitude", userModel.getLongitude());
        editor.putString("orders", userModel.getOrders());
        editor.putString("password", userModel.getPassword());
        editor.putString("rating", userModel.getRating());
        editor.putString("activate", String.valueOf(userModel.getActivate()));
        editor.putString("delivery", userModel.getDelivery());
        editor.putString("balance", userModel.getBalance());
        editor.putString("credit", userModel.getCredit());
        editor.putString("block",userModel.getBlocked());
        editor.putString("advanced",userModel.getAdvanced());
        editor.apply();
    }


    public static void saveTokenToSharePreferences(Context context, String token) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("token", token);
        editor.apply();
    }

    public static String getTokenFromSharedPreferences(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("token", "");
    }


    public static void removeUserFromSharedPreferences(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("id");
        editor.remove("email");
        editor.remove("mobile");
        editor.remove("name");
        editor.remove("role");
        editor.remove("birth_date");
        editor.remove("city");
        editor.remove("comment");
        editor.remove("country");
        editor.remove("gender");
        editor.remove("image");
        editor.remove("latitude");
        editor.remove("longitude");
        editor.remove("orders");
        editor.remove("password");
        editor.remove("rating");
        editor.remove("activate");
        editor.remove("delivery");
        editor.remove("balance");
        editor.remove("notification_tone");
        editor.remove("credit");
        editor.remove("notification_count");
        editor.remove("rejectCount");
        editor.remove("notification");
        editor.remove("block");
        editor.remove("advanced");
        editor.apply();
    }

    public static boolean PreferencesContainsUser(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.contains("id");
    }

    public static UserModel getUserSharedPreferences(Context context) {
        if (context == null)
            return new UserModel();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String id = preferences.getString("id", null);
        String email = preferences.getString("email", null);
        String mobile = preferences.getString("mobile", null);
        String name = preferences.getString("name", null);
        String role = preferences.getString("role", null);
        String birthDate = preferences.getString("birth_date", null);
        String city = preferences.getString("city", null);
        String comment = preferences.getString("comment", null);
        String country = preferences.getString("country", null);
        String gender = preferences.getString("gender", null);
        String image = preferences.getString("image", null);
        String latitude = preferences.getString("latitude", null);
        String longitude = preferences.getString("longitude", null);
        String orders = preferences.getString("orders", null);
        String password = preferences.getString("password", null);
        String rating = preferences.getString("rating", null);
        String token = preferences.getString("token", null);
        String activate = preferences.getString("activate", null);
        String delivery = preferences.getString("delivery", null);
        String balance = preferences.getString("balance", null);
        String credit = preferences.getString("credit", null);
        String block = preferences.getString("block",null);
        String advanced = preferences.getString("advanced","0");
        if (role == null){
            role = "99";
        }

        if (activate == null)
            activate = "99";

        UserModel userModel = new UserModel(name, email, token, birthDate, password, mobile, longitude, latitude, city, country, image, Integer.valueOf(activate), Integer.valueOf(role), id, gender, rating, delivery,block);
        userModel.setOrders(orders);
        userModel.setBalance(balance);
        userModel.setComment(comment);
        userModel.setCredit(credit);
        userModel.setAdvanced(advanced);
        return userModel;
    }

    public static boolean getShowIntroSharedPreferences(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("show_intro", true);
    }

    public static void saveShowIntroSharedPreferences(Context context, boolean showIntro) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("show_intro", showIntro);
        editor.apply();
    }


    public static boolean getNotificationSharedPreferences(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("notification", true);
    }

    public static void saveNotificationSharedPreferences(Context context, boolean showIntro) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("notification", showIntro);
        editor.apply();
    }

    public static void setNotificationCount(Context context, int number) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("notification_count", number);
        editor.apply();
    }

    public static int getNotificationCount(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt("notification_count", 0);
    }

    public static void saveDeliverySharedPreferences(Context context, String delivery) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("delivery", delivery);
        editor.apply();
    }

    public static String getDeliverySharedPreferences(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("delivery", null);
    }


    public static void savePromoSharedPreferences(Context context, String delivery) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("promo", delivery);
        editor.apply();
    }

    public static String getPromoSharedPreferences(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("promo", null);
    }


    public static Bitmap getBitmap(String path, int size) throws IOException {
        Bitmap bitmap = getBitmapFromPath(path, size);

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


    private static Bitmap getBitmapFromPath(String path, int size) {
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


    private static Bitmap getResizedBitmap(@NonNull Bitmap image, int maxSize) {
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


    public static ProgressDialog showProgressDialog(Context context, String bodyText, boolean cancelable) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(cancelable);
        progressDialog.setMessage(bodyText);
        progressDialog.show();
        return progressDialog;
    }


    public static void setRejectCountSharedPreferences(Context context, int rejectCount) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("rejectCount", rejectCount);
        editor.apply();
    }

    public static int getRejectCountSharedPreferences(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt("rejectCount", 0);
    }


    public static void showAlertDialog(Context context, String body, String title, boolean cancelable, String positiveText, String negativeText, DialogInterface.OnClickListener yesListener, DialogInterface.OnClickListener NoListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setCancelable(cancelable).setMessage(body);
        if (!positiveText.isEmpty()) {
            builder.setPositiveButton(positiveText, yesListener);
        }
        if (!negativeText.isEmpty()) {
            builder.setNegativeButton(negativeText, NoListener);
        }

        AlertDialog dialog = builder.create();
        dialog.show();
    }


}
