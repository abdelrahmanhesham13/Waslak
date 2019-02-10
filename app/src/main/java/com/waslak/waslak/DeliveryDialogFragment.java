package com.waslak.waslak;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Location;
import android.media.Image;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.squareup.picasso.Picasso;
import com.waslak.waslak.R;
import com.waslak.waslak.models.ChatModel;
import com.waslak.waslak.models.NotificationModel;
import com.waslak.waslak.models.OfferModel;
import com.waslak.waslak.models.RequestModel;
import com.waslak.waslak.models.ShopModel;
import com.waslak.waslak.models.UserModel;
import com.waslak.waslak.networkUtils.Connector;
import com.waslak.waslak.utils.GPSTracker;
import com.waslak.waslak.utils.Helper;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DeliveryDialogFragment extends AppCompatDialogFragment {

    Connector mGetOfferConnector;
    Connector mAcceptOfferConnector;
    Connector mBlockUserConnector;
    NotificationModel mNotificationModel;
    OfferModel mOfferModel;
    RequestModel mRequestModel;
    @BindView(R.id.delegate_name)
    TextView mDeliveryName;
    @BindView(R.id.delegate_image)
    ImageView mDelegateImage;
    @BindView(R.id.rating)
    RatingBar mDeliveryRating;
    @BindView(R.id.price)
    TextView mPrice;
    @BindView(R.id.accept)
    TextView mAcceptButton;
    @BindView(R.id.duration)
    TextView mDuration;
    @BindView(R.id.distance)
    TextView mDistance;
    @BindView(R.id.reject)
    TextView mRejectButton;
    @BindView(R.id.description)
    TextView mDescription;
    @BindView(R.id.verify_state)
    TextView mVerifyState;
    ProgressDialog mProgressDialog;

    GPSTracker mTracker;
    boolean mLocated = false;

    UserModel mUserModel;

    private final String TAG = DeliveryDialogFragment.class.getSimpleName();
    private Connector mConnectorSendMessage;
    ChatModel mChatModel;
    private Connector mConnectorGetRequest;
    Context mContext;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_delivery_dialog, container, false);
        ButterKnife.bind(this, v);

        if (getDialog().getWindow() != null)
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        if (getArguments() != null)
            mNotificationModel = (NotificationModel) getArguments().getSerializable("notification");

        mUserModel = Helper.getUserSharedPreferences(getContext());
        if (mUserModel.getDelivery().equals("1")) {
            mVerifyState.setText(getString(R.string.verified_account));
        } else {
            mVerifyState.setText(getString(R.string.not_verified_account));
        }

        mGetOfferConnector = new Connector(getContext(), new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                if (Connector.checkStatus(response)) {
                    mOfferModel = Connector.getOffer(response);
                    if (mOfferModel != null) {
                        getLocation();
                        mDuration.setText(mOfferModel.getDuration());
                        mPrice.setText(mOfferModel.getPrice());
                        mDescription.setText(mOfferModel.getDescription());
                        if (mOfferModel.getDelivery() != null) {
                            mDeliveryRating.setRating(Float.parseFloat(mOfferModel.getDelivery().getRating()));
                            mDeliveryName.setText(mOfferModel.getDelivery().getName());
                            if (mOfferModel.getDelivery().getDelivery().equals("1")) {
                                mVerifyState.setText(getString(R.string.verified_account));
                            } else {
                                mVerifyState.setText(getString(R.string.not_verified_account));
                            }
                        } else {
                            dismiss();
                        }
                        if (URLUtil.isValidUrl(mOfferModel.getImage()))
                            Picasso.get().load(mOfferModel.getImage()).fit().centerCrop().into(mDelegateImage);
                        else {
                            Picasso.get().load("http://www.cta3.com/waslk/prod_img/" + mOfferModel.getImage()).fit().centerCrop().into(mDelegateImage);
                        }
                    } else {
                        dismiss();
                    }
                } else {
                    dismiss();
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                dismiss();
            }
        });

        mGetOfferConnector.getRequest("DeliveryFragment", Connector.createGetOfferUrl() + "?id=" + mNotificationModel.getSecondary_id());

        mAcceptOfferConnector = new Connector(getContext(), new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                if (Connector.checkStatus(response)) {
                    mProgressDialog.dismiss();
                    String url = Connector.createStartChatUrl() + "?message=&user_id=" + Helper.getUserSharedPreferences(getContext()).getId() + "&request_id=" + mOfferModel.getRequestId() + "&to_id=" + mOfferModel.getDeliveryId();
                    Helper.writeToLog(url);
                    mConnectorSendMessage.getRequest(TAG, url);
                    if (getActivity() != null)
                        Helper.showSnackBarMessage(getString(R.string.order_accepted), (AppCompatActivity) getActivity());
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                dismiss();
            }
        });


        mConnectorSendMessage = new Connector(getContext(), new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                if (Connector.checkStatus(response)) {

                    mChatModel = Connector.getChatModelJson(response, "", mOfferModel.getDeliveryId(), mUserModel.getId());
                    mConnectorGetRequest.getRequest(TAG, "http://www.cta3.com/waslk/api/get_request?id=" + mOfferModel.getRequestId());

                } else {
                    Helper.showSnackBarMessage(getString(R.string.error), (AppCompatActivity) getActivity());
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                Helper.showSnackBarMessage(getString(R.string.error), (AppCompatActivity) getActivity());
            }
        });

        mConnectorGetRequest = new Connector(getContext(), new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                if (Connector.checkStatus(response)) {
                    mRequestModel = Connector.getRequest(response, new ShopModel());
                    mContext.startActivity(new Intent(mContext, ChatActivity.class).putExtra("chat", mChatModel).putExtra("request", mRequestModel));
                    dismiss();
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {

            }
        });

        mBlockUserConnector = new Connector(getContext(), new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                mProgressDialog.dismiss();
                if (Connector.checkStatus(response))
                    Helper.removeUserFromSharedPreferences(getContext());
                startActivity(new Intent(getContext(), LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                dismiss();
                if (getActivity() != null)
                    Helper.showSnackBarMessage(getString(R.string.blocked), (AppCompatActivity) getActivity());
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                dismiss();
            }
        });

        mAcceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressDialog = Helper.showProgressDialog(getContext(), getString(R.string.loading), false);
                if (mOfferModel != null)
                    mAcceptOfferConnector.getRequest("DeliveryFragment", Connector.createAcceptOfferUrl() + "?price=" + mOfferModel.getPrice()
                            + "&id=" + mOfferModel.getRequestId() + "&delivery_id=" + mOfferModel.getDelivery().getId() + "&user_id=" + mOfferModel.getUserId());
            }
        });

        mRejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Helper.getRejectCountSharedPreferences(getContext()) >= 3) {
                    Helper.writeToLog(String.valueOf(Helper.getRejectCountSharedPreferences(getContext())));
                    mProgressDialog = Helper.showProgressDialog(getContext(), getString(R.string.loading), false);
                    mProgressDialog.show();
                    mBlockUserConnector.getRequest("DeliveryFragment", "http://www.cta3.com/waslk/api/block_user?id=" + mOfferModel.getUserId());
                    Helper.setRejectCountSharedPreferences(getContext(), 0);
                } else {
                    int rejectCount = Helper.getRejectCountSharedPreferences(getContext());
                    rejectCount++;
                    Helper.setRejectCountSharedPreferences(getContext(), rejectCount);
                    Helper.writeToLog(String.valueOf(Helper.getRejectCountSharedPreferences(getContext())));
                    dismiss();
                }
            }
        });

        return v;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAcceptOfferConnector != null)
            mAcceptOfferConnector.cancelAllRequests("DeliveryFragment");
        if (mGetOfferConnector != null)
            mGetOfferConnector.cancelAllRequests("DeliveryFragment");
        if (mBlockUserConnector != null)
            mBlockUserConnector.cancelAllRequests("DeliveryFragment");
    }

    public void getLocation() {
        mTracker = new GPSTracker(getContext(), new GPSTracker.OnGetLocation() {
            @Override
            public void onGetLocation(double longtiude, double lantitude) {
                if (longtiude != 0 && lantitude != 0 && !mLocated) {
                    mLocated = true;
                    Location location = new Location("");
                    location.setLongitude(longtiude);
                    location.setLatitude(lantitude);
                    Location location1 = new Location("");
                    location1.setLatitude(Double.parseDouble(mOfferModel.getDeliveryLatitude()));
                    location1.setLongitude(Double.parseDouble(mOfferModel.getDeliveryLongitude()));
                    mDistance.setText(String.format(Locale.getDefault(), "%.2f " + getString(R.string.km), location.distanceTo(location1) / 1000.0));
                    mTracker.stopUsingGPS();
                }
            }
        });

        if (mTracker.canGetLocation() && !mLocated) {
            Location location = mTracker.getLocation();
            if (location != null) {
                if (location.getLongitude() != 0 && location.getLatitude() != 0) {
                    mLocated = true;
                    Location location3 = new Location("");
                    location3.setLongitude(location.getLongitude());
                    location3.setLatitude(location.getLatitude());
                    Location location1 = new Location("");
                    location1.setLatitude(Double.parseDouble(mOfferModel.getDeliveryLatitude()));
                    location1.setLongitude(Double.parseDouble(mOfferModel.getDeliveryLongitude()));
                    mDistance.setText(String.format(Locale.getDefault(), "%.2f " + getString(R.string.km), location3.distanceTo(location1) / 1000.0));
                    mTracker.stopUsingGPS();
                }
            }
        }
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
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
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("lang", lang).apply();
        if (getActivity() != null)
            getActivity().getResources().updateConfiguration(config,
                    getActivity().getResources().getDisplayMetrics());
    }

}
