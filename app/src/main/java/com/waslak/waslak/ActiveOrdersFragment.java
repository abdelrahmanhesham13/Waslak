package com.waslak.waslak;


import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.crashlytics.android.Crashlytics;
import com.waslak.waslak.adapters.OrdersAdapter;
import com.waslak.waslak.adapters.PendingOrdersAdapter;
import com.waslak.waslak.models.OrderModel;
import com.waslak.waslak.models.RequestModel;
import com.waslak.waslak.models.ShopModel;
import com.waslak.waslak.models.UserModel;
import com.waslak.waslak.networkUtils.Connector;
import com.waslak.waslak.networkUtils.Constants;
import com.waslak.waslak.utils.Helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 */
public class ActiveOrdersFragment extends Fragment {

    private static final String TAG = "ActiveOrdersFragment";
    @BindView(R.id.spinner)
    Spinner mSpinner;
    @BindView(R.id.progressIndicator)
    ProgressBar mProgressBar;
    @BindView(R.id.orders_recycler)
    RecyclerView mOrdersRecycler;
    @BindView(R.id.empty_order_parent)
    View mEmptyView;
    @BindView(R.id.setting_button)
    TextView mSettingButton;
    @BindView(R.id.menu_button)
    ImageView mMenuButton;
    @BindView(R.id.customer_orders)
    RadioButton mCustomerOrders;
    @BindView(R.id.my_orders)
    RadioButton mMyOrders;
    @BindView(R.id.spinner_parent)
    View mSpinnerParent;


    PendingOrdersAdapter mOrdersAdapter;
    PendingOrdersAdapter mPendingOrdersAdapter;

    OnMenuClicked onMenuClicked;

    ProgressDialog mProgressDialog;

    ArrayList<RequestModel> mRequestModels;
    UserModel mUserModel;

    Connector mConnector;
    Connector mConnectorDeleteRequest;
    Connector mConnectorAllRequests;

    int mPos;

    int spinnerPosition = 0;

    ItemTouchHelper itemTouchHelper;

    public ActiveOrdersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_active_orders, container, false);
        ButterKnife.bind(this, view);
        setupSpinner();

        mRequestModels = new ArrayList<>();

        if (getActivity() != null)
            mUserModel = (UserModel) getActivity().getIntent().getSerializableExtra("user");


        /*if (mOrderModels.isEmpty()){
            mOrdersRecycler.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mOrdersRecycler.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
        }*/

        mPendingOrdersAdapter = new PendingOrdersAdapter(getContext(), mRequestModels, new PendingOrdersAdapter.OnItemClicked() {
            @Override
            public void setOnItemClicked(int position) {
                if (!mRequestModels.get(position).getUser_id().equals(mUserModel.getId()))
                    startActivity(new Intent(getContext(), ChatActivity.class).putExtra("request",mRequestModels.get(position)).putExtra("shopModel",mRequestModels.get(position).getShop()).putExtra("user",mUserModel));
            }
        },mUserModel,1);

        mConnector = new Connector(getContext(), new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                if (Connector.checkStatus(response)) {
                    mRequestModels.clear();
                    mRequestModels.addAll(Connector.getRequests(response, new ShopModel()));
                    itemTouchHelper.attachToRecyclerView(mOrdersRecycler);
                    mOrdersRecycler.setAdapter(mOrdersAdapter);
                    mOrdersAdapter.notifyDataSetChanged();
                    mProgressBar.setVisibility(View.INVISIBLE);
                    mOrdersRecycler.setVisibility(View.VISIBLE);
                } else {
                    mRequestModels.clear();
                    itemTouchHelper.attachToRecyclerView(mOrdersRecycler);
                    mOrdersRecycler.setAdapter(mOrdersAdapter);
                    mOrdersAdapter.notifyDataSetChanged();
                    mProgressBar.setVisibility(View.INVISIBLE);
                    mOrdersRecycler.setVisibility(View.VISIBLE);
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                if (getActivity() != null)
                    Helper.showSnackBarMessage(getString(R.string.error), (AppCompatActivity) getActivity());
                mProgressBar.setVisibility(View.INVISIBLE);
                mOrdersRecycler.setVisibility(View.VISIBLE);
            }
        });


        mConnectorAllRequests = new Connector(getContext(), new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                if (Connector.checkStatus(response)){
                    if (mUserModel.getDelivery().equals("1")) {
                        mRequestModels.clear();
                        mRequestModels.addAll(Connector.getAllRequests(response));
                        mOrdersRecycler.setAdapter(mPendingOrdersAdapter);
                        itemTouchHelper.attachToRecyclerView(null);
                        mPendingOrdersAdapter.notifyDataSetChanged();
                        mProgressBar.setVisibility(View.INVISIBLE);
                        mOrdersRecycler.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(getContext(),getContext().getString(R.string.be_agent_first),Toast.LENGTH_LONG).show();
                    }
                } else {
                    mRequestModels.clear();
                    mOrdersRecycler.setAdapter(mPendingOrdersAdapter);
                    itemTouchHelper.attachToRecyclerView(null);
                    mPendingOrdersAdapter.notifyDataSetChanged();
                    mProgressBar.setVisibility(View.INVISIBLE);
                    mOrdersRecycler.setVisibility(View.INVISIBLE);
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                if (getActivity() != null)
                    Helper.showSnackBarMessage(getString(R.string.error), (AppCompatActivity) getActivity());
                mProgressBar.setVisibility(View.INVISIBLE);
                mOrdersRecycler.setVisibility(View.INVISIBLE);
            }
        });

        mConnectorDeleteRequest = new Connector(getContext(), new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                //mProgressDialog.dismiss();
                if (Connector.checkStatus(response)) {
                    mRequestModels.remove(mPos);
                    mOrdersAdapter.notifyItemRemoved(mPos);
                } else {
                    Helper.showSnackBarMessage(Connector.getMessage(response), (AppCompatActivity) getActivity());
                    mOrdersAdapter.notifyItemChanged(mPos);
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                mProgressDialog.dismiss();
                Helper.showSnackBarMessage(getString(R.string.error), (AppCompatActivity) getActivity());
                mOrdersAdapter.notifyItemChanged(mPos);
            }
        });

        mOrdersAdapter = new PendingOrdersAdapter(getContext(), mRequestModels, new PendingOrdersAdapter.OnItemClicked() {
            @Override
            public void setOnItemClicked(int position) {
                startActivity(new Intent(getContext(), OrderDetailsActivity.class).putExtra("request",mRequestModels.get(position)));
            }
        },mUserModel,1);

        mSettingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), SettingActivity.class));
            }
        });

        mOrdersRecycler.setHasFixedSize(true);
        mOrdersRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mOrdersRecycler.setAdapter(mOrdersAdapter);
        itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                final int pos = (int) viewHolder.itemView.getTag();
                Helper.writeToLog("Position " + pos);
                mPos = pos;
                Helper.writeToLog(String.valueOf(mRequestModels.get(pos).isDeleteStatus()));
                mConnectorDeleteRequest.getRequest(TAG, Constants.WASLAK_BASE_URL + "/mobile/api/delete_request?user_id=" + mRequestModels.get(pos).getUser_id() + "&id=" + mRequestModels.get(pos).getId());
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                Paint p = new Paint();
                Bitmap icon;
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

                    View itemView = viewHolder.itemView;
                    float height = (float) itemView.getBottom() - (float) itemView.getTop();
                    float width = height / 3;

                    if (dX < 0) {
                        p.setColor(Color.parseColor("#D32F2F"));
                        RectF background = new RectF((float) itemView.getRight() + dX / 4, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom());
                        c.drawRect(background, p);
                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_delete_swipe);
                        RectF icon_dest = new RectF((float) itemView.getRight() - 2 * width, (float) itemView.getTop() + width, (float) itemView.getRight() - width, (float) itemView.getBottom() - width);
                        p.setColor(Color.parseColor("#FFFFFF"));
                        c.drawBitmap(icon, null, icon_dest, p);
                    } else {
                        p.setColor(Color.parseColor("#D32F2F"));
                        RectF background = new RectF((float) itemView.getLeft() + dX / 4, (float) itemView.getTop(), (float) itemView.getLeft(), (float) itemView.getBottom());
                        c.drawRect(background, p);
                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_delete_swipe);
                        RectF icon_dest = new RectF((float) itemView.getLeft() + 2 * width, (float) itemView.getTop() + width, (float) itemView.getLeft() + width, (float) itemView.getBottom() - width);
                        p.setColor(Color.parseColor("#FFFFFF"));
                        c.drawBitmap(icon, null, icon_dest, p);
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX / 4, dY, actionState, isCurrentlyActive);
            }
        });
        itemTouchHelper.attachToRecyclerView(mOrdersRecycler);

        mMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMenuClicked.setOnMenuClicked();
            }
        });

        mMyOrders.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mSpinnerParent.setVisibility(View.VISIBLE);
                    mConnector.getRequest(TAG, Connector.createGetRequestsUrl() + "?user_id=" + mUserModel.getId() + "&status=" + spinnerPosition);
                }
            }
        });

        mCustomerOrders.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    mSpinnerParent.setVisibility(View.GONE);
                    mConnectorAllRequests.getRequest(TAG,Connector.createGetRequestsUrl() + "?filter_id=" + mUserModel.getId() + "&all=true");
                }
            }
        });

        if (getActivity().getIntent().hasExtra("orders"))
            if (getActivity().getIntent().getStringExtra("orders").equals("1"))
                mCustomerOrders.performClick();

        return view;
    }

    public void setupSpinner() {
        if (getActivity() != null) {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                    R.array.spinner_words, R.layout.spinner_item);
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
            mSpinner.setAdapter(adapter);
            mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position == 0) {
                        spinnerPosition = 0;
                        mProgressBar.setVisibility(View.VISIBLE);
                        mOrdersRecycler.setVisibility(View.GONE);
                        mConnector.getRequest(TAG, Connector.createGetRequestsUrl() + "?user_id=" + mUserModel.getId() + "&status=0");
                    } else {
                        spinnerPosition = 1;
                        mProgressBar.setVisibility(View.VISIBLE);
                        mOrdersRecycler.setVisibility(View.GONE);
                        mConnector.getRequest(TAG, Connector.createGetRequestsUrl() + "?user_id=" + mUserModel.getId() + "&status=1");
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
    }


    public interface OnMenuClicked {
        void setOnMenuClicked();
    }


    public void setOnMenuClicked(OnMenuClicked mOnMenuClicked) {
        this.onMenuClicked = mOnMenuClicked;
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
