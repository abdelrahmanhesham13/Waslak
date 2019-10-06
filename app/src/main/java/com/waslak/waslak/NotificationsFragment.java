package com.waslak.waslak;


import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.waslak.waslak.adapters.NotificationsAdapter;
import com.waslak.waslak.adapters.OrdersAdapter;
import com.waslak.waslak.models.ChatModel;
import com.waslak.waslak.models.NotificationModel;
import com.waslak.waslak.models.RequestModel;
import com.waslak.waslak.models.ShopModel;
import com.waslak.waslak.models.UserModel;
import com.waslak.waslak.networkUtils.Connector;
import com.waslak.waslak.networkUtils.Constants;
import com.waslak.waslak.utils.Helper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 */
public class NotificationsFragment extends Fragment {

    @BindView(R.id.notifications_recycler)
    RecyclerView mNotificationsRecycler;
    @BindView(R.id.setting_button)
    TextView mSettingButton;
    @BindView(R.id.menu_button)
    ImageView mMenuButton;

    ProgressDialog mProgressDialog;
    Connector mConnector;
    UserModel mUserModel;

    ArrayList<NotificationModel> mNotificationModels;
    NotificationsAdapter mNotificationsAdapter;

    OnMenuClicked mOnMenuClicked;

    Connector mConnectorSendMessage;
    Connector mConnectorGetRequest;
    Connector mConnectorCancelOrder;
    RequestModel mRequestModel;

    public final String TAG = "NotificationsFragment";

    ChatModel mChatModel;
    String mRequestId;

    AlertDialog alertDialog;
    Float mRatingNumber;

    int mPos;
    Connector mConnectorRate;
    NotificationModel sendOffer;

    public NotificationsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        ButterKnife.bind(this,view);
        if (getActivity() != null) {
            if (getActivity().getIntent() != null) {
                mUserModel = (UserModel) getActivity().getIntent().getSerializableExtra("user");
            }
        }

        mNotificationModels = new ArrayList<>();


        mNotificationsAdapter = new NotificationsAdapter(getContext(), mNotificationModels, new NotificationsAdapter.OnItemClicked() {
            @Override
            public void setOnItemClicked(final int position) {
                if (mNotificationModels.get(position).getStatus().equals("0") && !mNotificationModels.get(position).getType().equals("chat_admin"))
                    showDeliveryDialog(position);
                else if (mNotificationModels.get(position).getStatus().equals("1")) {
                    if (mNotificationModels.get(position).getDelivery_id().equals(mUserModel.getId())) {
                        mPos = position;
                        mRequestId = mNotificationModels.get(position).getRequest_id();
                        String url = Connector.createStartChatUrl() + "?message=&user_id=" + mUserModel.getId() + "&request_id=" + mNotificationModels.get(position).getRequest_id() + "&to_id=" + mNotificationModels.get(position).getUserId();
                        Helper.writeToLog(url);
                        mConnectorSendMessage.getRequest(TAG, url);
                    } else {
                        mPos = position;
                        mRequestId = mNotificationModels.get(position).getRequest_id();
                        String url = Connector.createStartChatUrl() + "?message=&user_id=" + mUserModel.getId() + "&request_id=" + mNotificationModels.get(position).getRequest_id() + "&to_id=" + mNotificationModels.get(position).getDelivery_id();
                        Helper.writeToLog(url);
                        mConnectorSendMessage.getRequest(TAG, url);
                    }
                } else if (mNotificationModels.get(position).getStatus().equals("4") && mNotificationModels.get(position).getUserId().equals(mUserModel.getId())){
                    //mProgressDialog.show();
                    //mConnectorCancelOrder.getRequest(TAG, "http://www.cta3.com/waslk/api/complete_offer?price=" + "" + "&id=" + mNotificationModels.get(position).getRequest_id() + "&delivery_id=" + mNotificationModels.get(position).getDelivery_id() + "&user_id=" + mNotificationModels.get(position).getUserId());
                    Helper.showAlertDialog(getContext(), getString(R.string.your_order_delivered), "", true, getString(R.string.yes), getString(R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mProgressDialog.show();
                            mConnectorCancelOrder.getRequest(TAG, Constants.WASLAK_BASE_URL + "/mobile/api/complete_offer?price=" + "" + "&id=" + mNotificationModels.get(position).getRequest_id() + "&delivery_id=" + mNotificationModels.get(position).getDelivery_id() + "&user_id=" + mNotificationModels.get(position).getUserId());
                        }
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mProgressDialog.show();
                            mConnectorCancelOrder.getRequest(TAG, Constants.WASLAK_BASE_URL + "/mobile/api/cancel_offer?price=" + "" + "&id=" + mNotificationModels.get(position).getRequest_id() + "&delivery_id=" + mNotificationModels.get(position).getDelivery_id() + "&user_id=" + mNotificationModels.get(position).getUserId() + "&status=7");
                        }
                    });
                } else if (mNotificationModels.get(position).getType().equals("chat_admin")){
                    startActivity(new Intent(getContext(),ChatActivity.class).putExtra("type","admin"));
                }

            }
        });

        mConnectorRate = new Connector(getContext(), new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                alertDialog.dismiss();
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                alertDialog.dismiss();
                Helper.showSnackBarMessage(getString(R.string.error), (AppCompatActivity)getActivity());
            }
        });

        mConnectorCancelOrder = new Connector(getContext(), new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                if (mProgressDialog.isShowing() && mProgressDialog != null)
                mProgressDialog.dismiss();
                if (Connector.checkStatus(response)) {
                    mNotificationModels.get(mPos).setStatus("5");
                    mNotificationModels.get(mPos).setText("Confirm Order Completed");
                    show();
                } else {
                    if (mProgressDialog.isShowing() && mProgressDialog != null)
                        mProgressDialog.dismiss();
                    Helper.showSnackBarMessage(getString(R.string.error), (AppCompatActivity)getActivity());
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                Helper.showSnackBarMessage(getString(R.string.error), (AppCompatActivity) getActivity());
            }
        });

        mConnectorSendMessage = new Connector(getContext(), new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                if (Connector.checkStatus(response)) {
                    if (mNotificationModels.get(mPos).getDelivery_id().equals(mUserModel.getId())) {
                        mChatModel = Connector.getChatModelJson(response, "", mNotificationModels.get(mPos).getUserId(), mUserModel.getId());
                    } else {
                        mChatModel = Connector.getChatModelJson(response, "", mNotificationModels.get(mPos).getDelivery_id(), mUserModel.getId());
                    }

                    mConnectorGetRequest.getRequest(TAG,Constants.WASLAK_BASE_URL + "/mobile/api/get_request?id=" + mRequestId);
                } else {
                    Helper.showSnackBarMessage(getString(R.string.error), (AppCompatActivity)getActivity());
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                Helper.showSnackBarMessage(getString(R.string.error), (AppCompatActivity)getActivity());
            }
        });

        mConnectorGetRequest = new Connector(getContext(), new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                if (Connector.checkStatus(response)){
                    mRequestModel = Connector.getRequest(response,new ShopModel());
                    startActivity(new Intent(getContext(), ChatActivity.class).putExtra("chat", mChatModel).putExtra("request",mRequestModel));
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {

            }
        });

        mNotificationsRecycler.setHasFixedSize(true);
        mNotificationsRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mNotificationsRecycler.setNestedScrollingEnabled(false);
        mNotificationsRecycler.setFocusable(false);
        mNotificationsRecycler.setAdapter(mNotificationsAdapter);


        mSettingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(),SettingActivity.class));
            }
        });


        mMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnMenuClicked.setOnMenuClicked();
            }
        });


        mConnector = new Connector(getContext(), new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                if (mProgressDialog.isShowing() && mProgressDialog != null && !getActivity().isFinishing())
                    mProgressDialog.dismiss();
                if (Connector.checkStatus(response)){
                    mNotificationModels.clear();
                    mNotificationModels.addAll(Connector.getNotifications(response));
                    int i = 0;
                    for (Iterator<NotificationModel> it = mNotificationModels.iterator(); it.hasNext(); i++) {
                        NotificationModel n = it.next(); // must be called before you can call i.remove()
                        if (n.getStatus().equals("0") && !mUserModel.getId().equals(n.getUserId()) && !n.getType().equals("chat_admin"))
                            it.remove();

                        if (getActivity() != null) {
                            if (getActivity().getIntent().hasExtra("offer_id")) {
                                if (getActivity().getIntent().hasExtra("offer_id") && !getActivity().getIntent().getStringExtra("offer_id").equals("0")) {
                                    Log.d(TAG, "onComplete: " + getActivity().getIntent().getStringExtra("offer_id"));
                                    if (n.getSecondary_id().equals(getActivity().getIntent().getStringExtra("offer_id"))) {
                                        sendOffer = n;
                                    }
                                }
                            }
                        }
                    }
                    mNotificationsAdapter.notifyDataSetChanged();
                    if (getActivity() != null) {
                        if (getActivity().getIntent().hasExtra("offer_id")) {
                            if (getActivity().getIntent().hasExtra("offer_id") && !getActivity().getIntent().getStringExtra("offer_id").equals("0")) {
                                showDeliveryDialog(mNotificationModels.indexOf(sendOffer));
                            }
                        }
                    }
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                if (mProgressDialog.isShowing() && mProgressDialog != null)
                    mProgressDialog.dismiss();
            }
        });

        return view;
    }

    public interface OnMenuClicked {
        void setOnMenuClicked();
    }


    public void setOnMenuClicked(OnMenuClicked mOnMenuClicked) {
        this.mOnMenuClicked = mOnMenuClicked;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mConnector != null)
            mConnector.cancelAllRequests("NotificationFragment");
    }

    public void showDeliveryDialog(int position) {
        if (getActivity() != null) {
            FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
            android.app.Fragment prev = getActivity().getFragmentManager().findFragmentByTag("dialog");
            if (prev != null) {
                ft.remove(prev).commit();
            }
            ft.addToBackStack(null);

            DeliveryDialogFragment deliveryDialogFragment = new DeliveryDialogFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("notification",mNotificationModels.get(position));
            deliveryDialogFragment.setArguments(bundle);


            deliveryDialogFragment.show(getActivity().getSupportFragmentManager(), "dialog");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mProgressDialog = Helper.showProgressDialog(getContext(),"Loading",false);
        mConnector.getRequest("NotificationsFragment",Connector.createGetNotificationsUrl() + "?user_id=" + mUserModel.getId());
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



    private void show() {
        final android.app.AlertDialog.Builder dialogBuilder = new android.app.AlertDialog.Builder(getContext());
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_rating, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        final RatingBar rating = dialogView.findViewById(R.id.rating_bar_2);
        final Button rate = dialogView.findViewById(R.id.btn_rate);
        final EditText comment = dialogView.findViewById(R.id.comment);
        rating.setIsIndicator(false);
        alertDialog = dialogBuilder.create();
        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
        alertDialog.show();
        rating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                mRatingNumber = rating;
            }
        });
        rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String commentText = comment.getText().toString();
                if (TextUtils.isEmpty(commentText)) {
                    Helper.showSnackBarMessage(getString(R.string.enter_comment), (AppCompatActivity)getActivity());
                } else {
                        mConnectorRate.getRequest(TAG, Constants.WASLAK_BASE_URL + "/mobile/api/add_comment?comment=" + Uri.encode(commentText) + "&rating=" + mRatingNumber + "&request_id=" + mNotificationModels.get(mPos).getRequest_id() + "&delivery_id=" + mNotificationModels.get(mPos).getDelivery_id());

                }
            }
        });


    }

}
