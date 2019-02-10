package com.waslak.waslak.networkUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonArray;
import com.waslak.waslak.models.ChatModel;
import com.waslak.waslak.models.MessageModel;
import com.waslak.waslak.models.NotificationModel;
import com.waslak.waslak.models.OfferModel;
import com.waslak.waslak.models.RequestModel;
import com.waslak.waslak.models.RequestOfferModel;
import com.waslak.waslak.models.ReviewModel;
import com.waslak.waslak.models.ShopDetailsModel;
import com.waslak.waslak.models.ShopModel;
import com.waslak.waslak.models.UserModel;
import com.waslak.waslak.utils.Helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Abdelrahman Hesham on 3/13/2018.
 */

public class Connector {

    private Context mContext;
    private LoadCallback mLoadCallback;
    private ErrorCallback mErrorCallback;
    private RequestQueue mQueue;
    private Map<String, String> mMap;
    String lang;


    public interface LoadCallback {

        void onComplete(String tag, String response);

    }

    public interface ErrorCallback {

        void onError(VolleyError error);

    }

    public Connector(Context mContext, LoadCallback mLoadCallback, ErrorCallback mErrorCallback) {
        this.mContext = mContext;
        this.mLoadCallback = mLoadCallback;
        this.mErrorCallback = mErrorCallback;
    }


    public void getRequest(final String tag, String url) {
        String response = "";
        if (isOnline(mContext)) {
            mQueue = Volley.newRequestQueue(mContext);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            String lang = preferences.getString("lang", "error");
            if (lang.equals("error")) {
                if (Locale.getDefault().getLanguage().equals("ar"))
                    //setLocale("ar");
                    lang = "ar";
                else
                    lang = "en";
            } else if (lang.equals("en")) {
                lang = "en";
            } else {
                lang = "ar";
            }
            url += "&lang=" + lang;
            Helper.writeToLog(url);
            StringRequest mStringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Helper.writeToLog(response);
                            mLoadCallback.onComplete(tag, response);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    mErrorCallback.onError(error);
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    return mMap;
                }
            };
            mStringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    50000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            mStringRequest.setTag(tag);
            mQueue.add(mStringRequest);
        } else {
            mErrorCallback.onError(new NoConnectionError());
        }


    }

    public void cancelAllRequests(final String tag) {
        if (mQueue != null) {
            mQueue.cancelAll(tag);
        }
    }


    public static String createGetShopsUrl() {
        Uri.Builder builder = Uri.parse(Constants.WASLAK_BASE_URL).buildUpon()
                .appendPath(Constants.GET_SHOPS_PATH);

        return builder.toString();

    }

    public static Uri.Builder createSignUpUrl() {

        return Uri.parse(Constants.WASLAK_BASE_URL).buildUpon()
                .appendPath(Constants.SIGN_UP_PATH);

    }

    public static Uri.Builder createSignInUrl() {

        return Uri.parse(Constants.WASLAK_BASE_URL).buildUpon()
                .appendPath(Constants.LOGIN_PATH);

    }

    public static String createEditProfileUrl() {

        return Uri.parse(Constants.WASLAK_BASE_URL).buildUpon()
                .appendPath(Constants.EDIT_PROFILE).build().toString();

    }

    public static String createUploadImageUrl() {
        Uri.Builder builder = Uri.parse(Constants.WASLAK_BASE_URL).buildUpon()
                .appendPath(Constants.UPLOAD_IMAGE);

        return builder.toString();
    }


    public static String createGetShopUrl(String id) {
        Uri.Builder builder = Uri.parse(Constants.WASLAK_BASE_URL).buildUpon()
                .appendPath(Constants.GET_SHOP_PATH)
                .appendQueryParameter("id", id);

        return builder.toString();

    }

    public static String createGetRequestsUrl() {
        Uri.Builder builder = Uri.parse(Constants.WASLAK_BASE_URL).buildUpon()
                .appendPath(Constants.GET_REQUESTS);

        return builder.toString();

    }

    public static String createAddRequestUrl() {
        Uri.Builder builder = Uri.parse(Constants.WASLAK_BASE_URL).buildUpon()
                .appendPath(Constants.ADD_REQUEST);

        return builder.toString();

    }


    public static String createEnrollInShopUrl() {
        Uri.Builder builder = Uri.parse(Constants.WASLAK_BASE_URL).buildUpon()
                .appendPath(Constants.ENROLL_TO_SHOP);

        return builder.toString();

    }


    public static String createGetRequestUrl() {
        Uri.Builder builder = Uri.parse(Constants.WASLAK_BASE_URL).buildUpon()
                .appendPath(Constants.GET_REQUEST);

        return builder.toString();

    }

    public static String createGetUserUrl() {
        Uri.Builder builder = Uri.parse(Constants.WASLAK_BASE_URL).buildUpon()
                .appendPath(Constants.GET_USER);

        return builder.toString();

    }

    public static String createSendOfferUrl() {
        Uri.Builder builder = Uri.parse(Constants.WASLAK_BASE_URL).buildUpon()
                .appendPath(Constants.SEND_OFFER);

        return builder.toString();

    }


    public static String createWebViewUrl() {
        Uri.Builder builder = Uri.parse(Constants.WASLAK_BASE_URL).buildUpon()
                .appendPath(Constants.WEB_VIEW);

        return builder.toString();

    }


    public static String createGetCommentsUrl() {
        Uri.Builder builder = Uri.parse(Constants.WASLAK_BASE_URL).buildUpon()
                .appendPath(Constants.GET_COMMENTS);

        return builder.toString();

    }

    public static String createGetNotificationsUrl() {
        Uri.Builder builder = Uri.parse(Constants.WASLAK_BASE_URL).buildUpon()
                .appendPath(Constants.GET_NOTIFICATIONS);

        return builder.toString();

    }

    public static String createGetOfferUrl() {
        Uri.Builder builder = Uri.parse(Constants.WASLAK_BASE_URL).buildUpon()
                .appendPath(Constants.GET_OFFERS);

        return builder.toString();

    }

    public static String createAcceptOfferUrl() {
        Uri.Builder builder = Uri.parse(Constants.WASLAK_BASE_URL).buildUpon()
                .appendPath(Constants.ACCEPT_OFFER);

        return builder.toString();

    }


    public static boolean checkStatus(String response) {
        boolean status = false;
        if (Helper.isJSONValid(response)) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                status = jsonObject.optBoolean("status");
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        return status;
    }


    public static int checkType(String response) {
        int status = 0;
        if (Helper.isJSONValid(response)) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                status = jsonObject.optInt("type");
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        return status;
    }

    public static String getMessage(String response) {
        String message = null;
        if (Helper.isJSONValid(response)) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                message = jsonObject.optString("message");
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        return message;
    }


    public static boolean checkImages(String response) {
        boolean status = false;
        JSONArray images;
        if (Helper.isJSONValid(response)) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                images = jsonObject.optJSONArray("images");
                status = true;
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        return status;
    }

    public static String createSendMessageUrl() {
        Uri.Builder builder = Uri.parse(Constants.WASLAK_BASE_URL).buildUpon()
                .appendPath(Constants.SEND_MESSAGE);

        return builder.toString();
    }

    public static String createGetChatMessagesUrl() {
        Uri.Builder builder = Uri.parse(Constants.WASLAK_BASE_URL).buildUpon()
                .appendPath(Constants.GET_CHAT_MESSAGES);

        return builder.toString();
    }

    public static String createStartChatUrl() {
        Uri.Builder builder = Uri.parse(Constants.WASLAK_BASE_URL).buildUpon()
                .appendPath(Constants.START_CHAT);

        return builder.toString();
    }

    public static ArrayList<String> getImages(String response) {
        ArrayList<String> imagesPaths = new ArrayList<>();
        if (Helper.isJSONValid(response)) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray images = jsonObject.optJSONArray("images");
                for (int i = 0; i < images.length(); i++) {
                    imagesPaths.add(images.getString(i));
                }
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        return imagesPaths;
    }

    public static ChatModel getChatModelJson(String response, String sellerName, String sellerId, String userId) {
        ChatModel chatModel = null;
        if (Helper.isJSONValid(response)) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                String chatId = jsonObject.getString("chat_id");
                chatModel = new ChatModel(chatId, null, null, sellerName, sellerId, null, userId, null);
                return chatModel;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static OfferModel getOffer(String response) {
        OfferModel offerModel = null;
        if (Helper.isJSONValid(response)) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray offers = jsonObject.getJSONArray("offers");
                JSONObject offer = offers.getJSONObject(0);
                String id = offer.optString("id");
                String deliveryId = offer.optString("delivery_id");
                String userId = offer.optString("user_id");
                String price = offer.optString("price");
                String description = offer.optString("description");
                String status = offer.optString("status");
                String longitude = offer.optString("longitude");
                String latitude = offer.optString("latitude");
                String address = offer.optString("address");
                String updated = offer.optString("updated");
                String created = offer.optString("created");
                String requestId = offer.optString("request_id");
                String name = offer.optString("name");
                String duration = offer.optString("duration");
                String rating = offer.optString("rating");
                String image = offer.optString("image");
                String deliveryLongitude = offer.optString("delivery_longitude");
                String deliveryLatitude = offer.optString("delivery_latitude");
                String deliveryAddress = offer.optString("delivery_address");
                String userLongitude = offer.optString("user_longitude");
                String userLatitude = offer.optString("user_latitude");
                String userAddress = offer.optString("user_address");

                JSONObject request = offer.optJSONObject("request");
                String idRequest = request.optString("id");
                String cityIdRequest = request.optString("city_id");
                String addressRequest = request.optString("address");
                String longitudeRequest = request.optString("longitude");
                String latitudeRequest = request.optString("latitude");
                String descriptionRequest = request.optString("description");
                String nameRequest = request.optString("name");
                String statusRequest = request.optString("status");
                String createdRequest = request.optString("created");
                String updatedRequest = request.optString("updated");
                String userIdRequest = request.optString("user_id");
                String countryRequest = request.optString("country");
                String priceRequest = request.optString("price");
                String imageRequest = request.optString("image");
                String viewsRequest = request.optString("views");
                String deliveryIdRequest = request.optString("delivery_id");
                String durationRequest = request.optString("duration");
                String shopIdRequest = request.optString("shop_id");
                String detailRequest = request.optString("detail");
                String longitudeUpdateRequest = request.optString("longitude_update");
                String LatitudeUpdateRequest = request.optString("latitude_update");
                RequestOfferModel requestOfferModel = new RequestOfferModel(idRequest, cityIdRequest, addressRequest, longitudeRequest
                        , latitudeRequest, descriptionRequest, nameRequest, statusRequest, createdRequest, updatedRequest, userIdRequest
                        , countryRequest, priceRequest, imageRequest, viewsRequest, deliveryIdRequest, durationRequest, shopIdRequest
                        , detailRequest, longitudeUpdateRequest, LatitudeUpdateRequest);

                JSONObject user = offer.getJSONObject("customer");
                String nameUser = user.optString("name");
                String username = user.optString("username");
                String token = user.optString("token");
                String birthDate = user.optString("birth_date");
                String password = user.optString("password");
                String mobile = user.optString("mobile");
                String longitudeUser = user.optString("longitude");
                String latitudeUser = user.optString("latitude");
                String city = user.optString("city_id");
                String country = user.optString("country");
                String imageUser = user.optString("image");
                int role = user.optInt("role");
                String idUser = user.optString("id");
                String gender = user.optString("gender");
                String ratingUser = user.optString("rating");
                String deliveryFlag = user.optString("delivery");
                UserModel customer = new UserModel(nameUser, username, token, birthDate, password, mobile, longitudeUser, latitudeUser, city, country, imageUser, 0, role, idUser, gender, ratingUser, deliveryFlag);

                user = offer.getJSONObject("delivery");
                nameUser = user.optString("name");
                username = user.optString("username");
                token = user.optString("token");
                birthDate = user.optString("birth_date");
                password = user.optString("password");
                mobile = user.optString("mobile");
                longitudeUser = user.optString("longitude");
                latitudeUser = user.optString("latitude");
                city = user.optString("city_id");
                country = user.optString("country");
                imageUser = user.optString("image");
                role = user.optInt("role");
                idUser = user.optString("id");
                gender = user.optString("gender");
                ratingUser = user.optString("rating");
                deliveryFlag = user.optString("delivery");
                UserModel delivery = new UserModel(nameUser, username, token, birthDate, password, mobile, longitudeUser, latitudeUser, city, country, imageUser, 0, role, idUser, gender, ratingUser, deliveryFlag);


                offerModel = new OfferModel(id, deliveryId, userId, price, description, status, longitude, latitude, address, updated
                        , created, requestId, name, duration, rating, image, deliveryLongitude, deliveryLatitude, deliveryAddress, userLongitude, userLatitude, userAddress, requestOfferModel, customer, delivery);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        return offerModel;
    }

    public static ArrayList<NotificationModel> getNotifications(String response) {
        ArrayList<NotificationModel> notificationModels = new ArrayList<>();
        if (Helper.isJSONValid(response)) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray notifications = jsonObject.getJSONArray("notifications");
                for (int i = 0; i < notifications.length(); i++) {
                    JSONObject notification = notifications.getJSONObject(i);
                    String id = notification.optString("id");
                    String title = notification.optString("title");
                    String type = notification.optString("type");
                    String userId = notification.optString("user_id");
                    String created = notification.optString("created");
                    String secondaryId = notification.optString("secondary_id");
                    String requestId = notification.optString("request_id");
                    String deliveryId = notification.optString("delivery_id");
                    String status = notification.optString("status");
                    String titleDelivery = notification.optString("title_delivery");
                    notificationModels.add(new NotificationModel(title, id, type, userId, created, secondaryId, requestId, deliveryId, status, titleDelivery));
                }
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        return notificationModels;
    }

    public static ArrayList<ReviewModel> getReviews(String response) {
        ArrayList<ReviewModel> reviews = new ArrayList<>();
        if (Helper.isJSONValid(response)) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray comments = jsonObject.getJSONArray("comments");
                for (int i = 0; i < comments.length(); i++) {
                    JSONObject comment = comments.getJSONObject(i);
                    String commentString = comment.optString("delivery_comment");
                    String deliveryRating = comment.optString("delivery_rating");
                    reviews.add(new ReviewModel("", deliveryRating, commentString, ""));
                }
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        return reviews;
    }


    public static ArrayList<ShopModel> getShops(String response) {
        ArrayList<ShopModel> shopModels = new ArrayList<>();
        ArrayList<String> time = new ArrayList<>();
        if (Helper.isJSONValid(response)) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray shops = jsonObject.getJSONArray("shops");
                for (int i = 0; i < shops.length(); i++) {
                    JSONObject shop = shops.getJSONObject(i);
                    String id = shop.optString("id");
                    String city = shop.optString("city_id");
                    String address = shop.optString("address");
                    String longitude = shop.optString("longitude");
                    String latitude = shop.optString("latitude");
                    String description = shop.optString("description");
                    String name = shop.optString("name");
                    String approved = shop.optString("approved");
                    String created = shop.optString("created");
                    String updated = shop.optString("updated");
                    String userId = shop.optString("user_id");
                    String country = shop.optString("country");
                    String region = shop.optString("region");
                    String image = shop.optString("image");
                    String views = shop.optString("views");
                    JSONArray timeArray = shop.optJSONArray("time");
                    for (int j = 0; j < timeArray.length(); j++) {
                        time.add(timeArray.getString(j));
                    }
                    shopModels.add(new ShopModel(id, city, address, longitude, latitude, description, name, approved, created, updated
                            , userId, country, region, image, views, time));
                }
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        return shopModels;
    }


    public static ArrayList<ShopModel> getShopsGoogle(String response) {
        ArrayList<ShopModel> shopModels = new ArrayList<>();
        if (Helper.isJSONValid(response)) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray shops = jsonObject.getJSONArray("results");
                for (int i = 0; i < shops.length(); i++) {
                    JSONObject shop = shops.getJSONObject(i);
                    String address = shop.optString("formatted_address");
                    JSONObject geometry = shop.optJSONObject("geometry");
                    JSONObject location = geometry.optJSONObject("location");
                    String longitude = String.valueOf(location.optDouble("lng"));
                    String latitude = String.valueOf(location.optDouble("lat"));
                    String name = shop.optString("name");
                    double rating = shop.optDouble("rating");
                    JSONArray photos = shop.optJSONArray("photos");
                    String imageFullPath = "";
                    if (photos != null) {
                        String image = photos.optJSONObject(0).optString("photo_reference");
                        imageFullPath = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + image + "&key=AIzaSyChKwGm9z5bnNLPnzjCKkdbQl2owplxYvQ";
                    } else {
                        imageFullPath = shop.optString("icon");
                    }
                    shopModels.add(new ShopModel("0", "0", address, longitude, latitude, "0", name, "0", "0", "0"
                            , "0", "0", "0", imageFullPath, "0", new ArrayList<String>(), rating));
                }
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        return shopModels;
    }


    public static ShopDetailsModel getShop(String response, ShopModel shopModel) {
        ShopDetailsModel shopDetailsModel = null;
        ArrayList<String> images = new ArrayList<>();
        ArrayList<String> time = new ArrayList<>();
        if (Helper.isJSONValid(response)) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONObject shop = jsonObject.getJSONObject("shop");
                String id = shop.optString("id");
                String city = shop.optString("city_id");
                String address = shop.optString("address");
                String longitude = shop.optString("longitude");
                String latitude = shop.optString("latitude");
                String description = shop.optString("description");
                String name = shop.optString("name");
                String approved = shop.optString("approved");
                String created = shop.optString("created");
                String updated = shop.optString("updated");
                String userId = shop.optString("user_id");
                String country = shop.optString("country");
                String region = shop.optString("region");
                String image = shop.optString("image");
                String views = shop.optString("views");
                String body = shop.optString("body");
                String deliveryCount = shop.optString("delivery_count");
                JSONArray timeArray = shop.optJSONArray("time");
                for (int j = 0; j < timeArray.length(); j++) {
                    time.add(timeArray.getString(j));
                }
                String baseUrl = jsonObject.getString("base_url");
                JSONArray imagesJson = shop.getJSONArray("images");
                for (int i = 0; i < imagesJson.length(); i++) {
                    images.add(baseUrl + imagesJson.getString(i));
                }
                shopDetailsModel = new ShopDetailsModel(id, city, address, longitude, latitude, description, name, approved, created, updated
                        , userId, country, region, image, views, body, images, time);
                shopDetailsModel.setDistance(shopModel.getDistance());
                shopDetailsModel.setDelivery_count(deliveryCount);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        return shopDetailsModel;
    }


    public static ShopModel getShopGoogle(String response, ShopModel shopModel) {
        ShopModel shopDetailsModel = null;
        ArrayList<String> images = new ArrayList<>();
        ArrayList<String> time = new ArrayList<>();
        if (Helper.isJSONValid(response)) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONObject shop = jsonObject.getJSONObject("shop");
                String id = shop.optString("id");
                String city = shop.optString("city_id");
                String address = shop.optString("address");
                String longitude = shop.optString("longitude");
                String latitude = shop.optString("latitude");
                String description = shop.optString("description");
                String name = shop.optString("name");
                String approved = shop.optString("approved");
                String created = shop.optString("created");
                String updated = shop.optString("updated");
                String userId = shop.optString("user_id");
                String country = shop.optString("country");
                String region = shop.optString("region");
                String image = shop.optString("image");
                String views = shop.optString("views");
                String body = shop.optString("body");
                String deliveryCount = shop.optString("delivery_count");
                JSONArray timeArray = shop.optJSONArray("time");
                for (int j = 0; j < timeArray.length(); j++) {
                    time.add(timeArray.getString(j));
                }
                shopDetailsModel = new ShopModel(id, city, address, longitude, latitude, description, name, approved, created, updated, userId, country, region, image, views, time);
                shopDetailsModel.setDistance(shopModel.getDistance());
                shopDetailsModel.setDelivery_count(deliveryCount);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        return shopDetailsModel;
    }


    public static ArrayList<RequestModel> getRequests(String response, ShopModel shopModel) {
        ArrayList<RequestModel> requestModels = new ArrayList<>();
        if (Helper.isJSONValid(response)) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray requests = jsonObject.optJSONArray("requests");
                for (int i = 0; i < requests.length(); i++) {
                    JSONObject request = requests.optJSONObject(i);
                    String id = request.optString("id");
                    String city = request.optString("city_id");
                    String address = request.optString("address");
                    String longitude = request.optString("longitude");
                    String latitude = request.optString("latitude");
                    String description = request.optString("description");
                    String name = request.optString("name");
                    String status = request.optString("status");
                    String created = request.optString("created");
                    String updated = request.optString("updated");
                    String userId = request.optString("user_id");
                    String country = request.optString("country");
                    String price = request.optString("price");
                    boolean deleteStatus = request.optBoolean("delete_status");
                    String image = request.optString("image");
                    String views = request.optString("views");
                    String delivery_id = request.optString("delivery_id");
                    String duration = request.optString("duration");
                    String shopId = request.optString("shop_id");
                    String detail = request.optString("detail");
                    String longitudeUpdate = request.optString("longitude_update");
                    String latitudeUpdate = request.getString("latitude_update");
                    String shopName = request.getString("shopname");
                    JSONObject delivery = request.optJSONObject("delivery");
                    String nameDelivery = delivery.optString("name");
                    String usernameDelivery = delivery.optString("username");
                    String tokenDelivery = delivery.optString("token");
                    String birthDateDelivery = delivery.optString("birth_date");
                    String passwordDelivery = delivery.optString("password");
                    String mobileDelivery = delivery.optString("mobile");
                    String longitudeDelivery = delivery.optString("longitude");
                    String latitudeDelivery = delivery.optString("latitude");
                    String cityDelivery = delivery.optString("city_id");
                    String countryDelivery = delivery.optString("country");
                    String imageDelivery = delivery.optString("image");
                    int roleDelivery = delivery.optInt("role");
                    String idDelivery = delivery.optString("id");
                    String genderDelivery = delivery.optString("gender");
                    String ratingDelivery = delivery.optString("rating");
                    String deliveryState = delivery.optString("delivery");
                    UserModel deliveryModel = new UserModel(nameDelivery, usernameDelivery, tokenDelivery, birthDateDelivery, passwordDelivery, mobileDelivery, longitudeDelivery, latitudeDelivery, cityDelivery, countryDelivery, imageDelivery, 0, roleDelivery, idDelivery, genderDelivery, ratingDelivery, deliveryState);
                    String note = request.optString("note");
                    JSONObject shop = request.getJSONObject("shop");
                    String idShop = shop.optString("id");
                    String cityShop = shop.optString("city_id");
                    String addressShop = shop.optString("address");
                    String longitudeShop = shop.optString("longitude");
                    String latitudeShop = shop.optString("latitude");
                    String descriptionShop = shop.optString("description");
                    String nameShop = shop.optString("name");
                    String approvedShop = shop.optString("approved");
                    String createdShop = shop.optString("created");
                    String updatedShop = shop.optString("updated");
                    String userIdShop = shop.optString("user_id");
                    String countryShop = shop.optString("country");
                    String regionShop = shop.optString("region");
                    String imageShop = shop.optString("image");
                    String viewsShop = shop.optString("views");
                    ShopModel shopModelCreated = new ShopModel(idShop, cityShop, addressShop, longitudeShop, latitudeShop, descriptionShop, nameShop, approvedShop, createdShop, updatedShop, userIdShop, countryShop, regionShop, imageShop, viewsShop, new ArrayList<String>());
                    shopModelCreated.setDistance(shopModel.getDistance());
                    JSONObject user = request.optJSONObject("user");
                    String nameUser = user.optString("name");
                    String usernameUser = user.optString("username");
                    String tokenUser = user.optString("token");
                    String birthDateUser = user.optString("birth_date");
                    String passwordUser = user.optString("password");
                    String mobileUser = user.optString("mobile");
                    String longitudeUser = user.optString("longitude");
                    String latitudeUser = user.optString("latitude");
                    String cityUser = user.optString("city_id");
                    String countryUser = user.optString("country");
                    String imageUser = user.optString("image");
                    int roleUser = user.optInt("role");
                    String idUser = user.optString("id");
                    String genderUser = user.optString("gender");
                    String rating = user.optString("rating");
                    String deliveryFlag = user.optString("delivery");
                    String promo = request.optString("promo");
                    UserModel userModel = new UserModel(nameUser, usernameUser, tokenUser, birthDateUser, passwordUser, mobileUser, longitudeUser, latitudeUser, cityUser, countryUser, imageUser, 0, roleUser, idUser, genderUser, rating, deliveryFlag);
                    requestModels.add(new RequestModel(id, city, address, longitude, latitude, description, name, status, created, updated, userId, country, price, image, views, delivery_id, duration, shopId, detail, longitudeUpdate, latitudeUpdate, shopName, deliveryModel, note, shopModelCreated, userModel, deleteStatus,promo));
                }
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        return requestModels;
    }


    public static RequestModel getRequest(String response, ShopModel shopModel) {
        RequestModel requestModel = null;
        if (Helper.isJSONValid(response)) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONObject request = jsonObject.optJSONObject("request");
                String id = request.optString("id");
                String city = request.optString("city_id");
                String address = request.optString("address");
                String longitude = request.optString("longitude");
                String latitude = request.optString("latitude");
                String description = request.optString("description");
                String name = request.optString("name");
                String status = request.optString("status");
                String created = request.optString("created");
                String updated = request.optString("updated");
                String userId = request.optString("user_id");
                boolean deleteStatus = request.optBoolean("delete_status");
                String country = request.optString("country");
                String price = request.optString("price");
                String image = request.optString("image");
                String views = request.optString("views");
                String delivery_id = request.optString("delivery_id");
                String duration = request.optString("duration");
                String shopId = request.optString("shop_id");
                String detail = request.optString("detail");
                String longitudeUpdate = request.optString("longitude_update");
                String latitudeUpdate = request.getString("latitude_update");
                JSONObject delivery = request.optJSONObject("delivery");
                String nameDelivery = delivery.optString("name");
                String usernameDelivery = delivery.optString("username");
                String tokenDelivery = delivery.optString("token");
                String birthDateDelivery = delivery.optString("birth_date");
                String passwordDelivery = delivery.optString("password");
                String mobileDelivery = delivery.optString("mobile");
                String longitudeDelivery = delivery.optString("longitude");
                String latitudeDelivery = delivery.optString("latitude");
                String cityDelivery = delivery.optString("city_id");
                String countryDelivery = delivery.optString("country");
                String imageDelivery = delivery.optString("image");
                int roleDelivery = delivery.optInt("role");
                String idDelivery = delivery.optString("id");
                String genderDelivery = delivery.optString("gender");
                String rating = delivery.optString("rating");
                String deliveryFlag = delivery.optString("delivery");
                UserModel deliveryModel = new UserModel(nameDelivery, usernameDelivery, tokenDelivery, birthDateDelivery, passwordDelivery, mobileDelivery, longitudeDelivery, latitudeDelivery, cityDelivery, countryDelivery, imageDelivery, 0, roleDelivery, idDelivery, genderDelivery, rating, deliveryFlag);
                String note = request.optString("note");
                JSONObject shop = request.getJSONObject("shop");
                String idShop = shop.optString("id");
                String cityShop = shop.optString("city_id");
                String addressShop = shop.optString("address");
                String longitudeShop = shop.optString("longitude");
                String latitudeShop = shop.optString("latitude");
                String descriptionShop = shop.optString("description");
                String nameShop = shop.optString("name");
                String approvedShop = shop.optString("approved");
                String createdShop = shop.optString("created");
                String updatedShop = shop.optString("updated");
                String userIdShop = shop.optString("user_id");
                String countryShop = shop.optString("country");
                String regionShop = shop.optString("region");
                String imageShop = shop.optString("image");
                String viewsShop = shop.optString("views");
                ShopModel shopModelCreated = new ShopModel(idShop, cityShop, addressShop, longitudeShop, latitudeShop, descriptionShop, nameShop, approvedShop, createdShop, updatedShop, userIdShop, countryShop, regionShop, imageShop, viewsShop, new ArrayList<String>());
                if (shopModel != null)
                    shopModelCreated.setDistance(shopModel.getDistance());
                JSONObject user = request.optJSONObject("user");
                String nameUser = user.optString("name");
                String usernameUser = user.optString("username");
                String tokenUser = user.optString("token");
                String birthDateUser = user.optString("birth_date");
                String passwordUser = user.optString("password");
                String mobileUser = user.optString("mobile");
                String longitudeUser = user.optString("longitude");
                String latitudeUser = user.optString("latitude");
                String cityUser = user.optString("city_id");
                String countryUser = user.optString("country");
                String imageUser = user.optString("image");
                int roleUser = user.optInt("role");
                String idUser = user.optString("id");
                String genderUser = user.optString("gender");
                String ratingUser = user.optString("rating");
                String deliveryUser = user.optString("delivery");
                String promo = request.optString("promo");
                UserModel userModel = new UserModel(nameUser, usernameUser, tokenUser, birthDateUser, passwordUser, mobileUser, longitudeUser, latitudeUser, cityUser, countryUser, imageUser, 0, roleUser, idUser, genderUser, ratingUser, deliveryUser);
                if (shopModel != null)
                    requestModel = new RequestModel(id, city, address, longitude, latitude, description, name, status, created, updated, userId, country, price, image, views, delivery_id, duration, shopId, detail, longitudeUpdate, latitudeUpdate, shopModel.getName(), deliveryModel, note, shopModelCreated, userModel, deleteStatus,promo);
                else
                    requestModel = new RequestModel(id, city, address, longitude, latitude, description, name, status, created, updated, userId, country, price, image, views, delivery_id, duration, shopId, detail, longitudeUpdate, latitudeUpdate, "", deliveryModel, note, shopModelCreated, userModel, deleteStatus,promo);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        return requestModel;
    }


    public static UserModel getUser(String response) {
        UserModel userModel = null;
        if (Helper.isJSONValid(response)) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONObject user = jsonObject.getJSONObject("user");
                String name = user.optString("name");
                String username = user.optString("username");
                String token = user.optString("token");
                String birthDate = user.optString("birth_date");
                String password = user.optString("password");
                String mobile = user.optString("mobile");
                String longitude = user.optString("longitude");
                String latitude = user.optString("latitude");
                String city = user.optString("city_id");
                String country = user.optString("country");
                String image = user.optString("image");
                int role = user.optInt("role");
                String id = user.optString("id");
                String gender = user.optString("gender");
                String rating = user.optString("rating");
                String delivery = user.optString("delivery");
                String balance = user.optString("balance");
                userModel = new UserModel(name, username, token, birthDate, password, mobile, longitude, latitude, city, country, image, 0, role, id, gender, rating, delivery);
                String orders = String.valueOf(jsonObject.getInt("orders"));
                String comments = String.valueOf(jsonObject.getInt("comments"));
                String credit = user.optString("credit");
                userModel.setComment(comments);
                userModel.setBalance(balance);
                userModel.setOrders(orders);
                userModel.setCredit(credit);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        return userModel;
    }


    private static boolean isOnline(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnectedOrConnecting();
        } else {
            return false;
        }
    }


    public static ArrayList<MessageModel> getChatMessagesJson(String response, UserModel userModel) {
        ArrayList<MessageModel> list = new ArrayList<>();
        if (Helper.isJSONValid(response)) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray messages = jsonObject.getJSONArray("chats");
                for (int i = 0; i < messages.length(); i++) {
                    JSONObject message = messages.getJSONObject(i);
                    String chatId = message.getString("chat_id");
                    String messageText = message.getString("message");
                    String toId = message.getString("to_id");
                    String fromId = message.getString("from_id");
                    String date = message.getString("date");
                    String type = message.getString("type");
                    if (userModel.getId().equals(fromId)) {
                        list.add(new MessageModel(chatId, toId, fromId, date, messageText, type, true));
                    } else {
                        list.add(new MessageModel(chatId, toId, fromId, date, messageText, type, false));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return list;
    }


    public void setMap(Map<String, String> mMap) {
        this.mMap = mMap;
    }
}
