package com.waslak.waslak;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.waslak.waslak.adapters.MessagesAdapter;
import com.waslak.waslak.adapters.PendingOrdersAdapter;
import com.waslak.waslak.models.PendingOrderModel;
import com.waslak.waslak.models.RequestModel;
import com.waslak.waslak.models.ShopModel;
import com.waslak.waslak.models.UserModel;
import com.waslak.waslak.networkUtils.Connector;
import com.waslak.waslak.utils.Helper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 */
public class StorePendingOrdersFragment extends Fragment {

    private static final String TAG = "PendingOrdersFragment";
    @BindView(R.id.pending_orders)
    RecyclerView mPendingOrdersRecycler;
    @BindView(R.id.progressIndicator)
    ProgressBar mProgressBar;
    ArrayList<RequestModel> mRequestModels;
    PendingOrdersAdapter mPendingOrdersAdapter;

    ShopModel mShopModel;
    UserModel mUserModel;

    Connector mConnector;
    //Connector mGetMyEnrolledConnector;

    Context context;

    public StorePendingOrdersFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_store_pending_orders, container, false);
        ButterKnife.bind(this,view);
        mRequestModels = new ArrayList<>();
        if (getActivity() != null)
            mShopModel = (ShopModel) getActivity().getIntent().getSerializableExtra("ShopModel");

        if (getActivity() != null) {
            mUserModel = (UserModel) getActivity().getIntent().getSerializableExtra("user");
            mUserModel = Helper.getUserSharedPreferences(getContext());
        }


        /*mGetMyEnrolledConnector = new Connector(getContext(), new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                if (Connector.checkStatus(response)) {
                    if (response.contains(mShopModel.getId()) && mUserModel.getDelivery().equals("1")) {
                        } else {
                        mProgressBar.setVisibility(View.GONE);
                        mPendingOrdersRecycler.setVisibility(View.VISIBLE);
                    }
                } else {
                    mProgressBar.setVisibility(View.GONE);
                    mPendingOrdersRecycler.setVisibility(View.VISIBLE);
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {

            }
        });*/

        mConnector = new Connector(getContext(), new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                if (Connector.checkStatus(response)){
                    if (mUserModel.getDelivery().equals("1")) {
                        mRequestModels.clear();
                        mRequestModels.addAll(Connector.getRequests(response, mShopModel));
                        mPendingOrdersAdapter.notifyDataSetChanged();
                        mProgressBar.setVisibility(View.INVISIBLE);
                        mPendingOrdersRecycler.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(context,context.getString(R.string.be_agent_first),Toast.LENGTH_LONG).show();
                    }
                } else {
                    mProgressBar.setVisibility(View.INVISIBLE);
                    mPendingOrdersRecycler.setVisibility(View.VISIBLE);
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                if (getActivity() != null)
                    Helper.showSnackBarMessage(getString(R.string.error),(AppCompatActivity)getActivity());
                mProgressBar.setVisibility(View.INVISIBLE);
                mPendingOrdersRecycler.setVisibility(View.VISIBLE);
            }
        });



        mPendingOrdersAdapter = new PendingOrdersAdapter(getContext(), mRequestModels, new PendingOrdersAdapter.OnItemClicked() {
            @Override
            public void setOnItemClicked(int position) {
                if (!mRequestModels.get(position).getUser_id().equals(mUserModel.getId()))
                    startActivity(new Intent(getContext(), ChatActivity.class).putExtra("request",mRequestModels.get(position)).putExtra("shopModel",mShopModel).putExtra("user",mUserModel));
            }
        },mUserModel,0);


        mPendingOrdersRecycler.setHasFixedSize(true);
        mPendingOrdersRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mPendingOrdersRecycler.setAdapter(mPendingOrdersAdapter);


        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        //mGetMyEnrolledConnector.getRequest(TAG, "http://www.cta3.com/waslk/api/get_my_enrolled_shop?user_id=" + mUserModel.getId());
        if (mUserModel.getDelivery().equals("1")) {
            mRequestModels.clear();
            mProgressBar.setVisibility(View.VISIBLE);
            mPendingOrdersRecycler.setVisibility(View.GONE);
            mConnector.getRequest(TAG, Connector.createGetRequestsUrl() + "?shop_id=" + mShopModel.getId() + "&delivery_id=" + mUserModel.getId());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mConnector != null)
            mConnector.cancelAllRequests(TAG);
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
