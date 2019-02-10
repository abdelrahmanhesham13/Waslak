package com.waslak.waslak;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.waslak.waslak.models.ShopDetailsModel;
import com.waslak.waslak.models.ShopModel;
import com.waslak.waslak.models.UserModel;
import com.waslak.waslak.networkUtils.Connector;
import com.waslak.waslak.utils.GPSTracker;
import com.waslak.waslak.utils.Helper;

import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.content.Context.LOCATION_SERVICE;


/**
 * A simple {@link Fragment} subclass.
 */
public class ShopInfoFragment extends Fragment {

    public final String TAG = "ShopInfoFragment";

    private GoogleMap mMap;
    int mMapPadding = 75;

    @BindView(R.id.fab)
    FloatingActionButton mFab;

    @BindView(R.id.store_name)
    TextView mStoreName;
    @BindView(R.id.store_distance)
    TextView mStoreDistance;

    @BindView(R.id.mapView)
    MapView mMapView;


    @BindView(R.id.btn_bottom_sheet)
    LinearLayout btnBottomSheet;
    @BindView(R.id.be_a_courier)
    CheckBox mBeACourierParent;
    @BindView(R.id.number_of_couriers)
    TextView mNumberOfCouriers;

    @BindView(R.id.bottom_sheet)
    RelativeLayout layoutBottomSheet;

    GPSTracker mTracker;

    boolean mLocated = false;

    ShopModel mShopModel = null;
    UserModel mUserModel = null;
    ShopDetailsModel shopDetailsModel = null;

    Connector mConnector;
    Context context;
    //Connector mEnrollConnector;
    //Connector mGetMyEnrolledConnector;

    ProgressDialog mProgressDialog;

    int day;

    public ShopInfoFragment() {
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

        View view = inflater.inflate(R.layout.fragment_shop_info, container, false);
        ButterKnife.bind(this, view);

        mUserModel = Helper.getUserSharedPreferences(getContext());

        if (!Helper.PreferencesContainsUser(getContext()))
            if (getActivity() != null)
                getActivity().finish();

        if (getActivity() != null) {
            Intent intent = getActivity().getIntent();
            if (intent != null) {
                if (intent.hasExtra("ShopModel")) {
                    mShopModel = (ShopModel) intent.getSerializableExtra("ShopModel");
                }
            }
        }

        if (getActivity() != null) {
            Intent intent = getActivity().getIntent();
            if (intent != null) {
                if (intent.hasExtra("user")) {
                    mUserModel = (UserModel) intent.getSerializableExtra("user");
                    mUserModel = Helper.getUserSharedPreferences(getContext());
                }
            }
        }

        Calendar calendar = Calendar.getInstance();
        day = calendar.get(Calendar.DAY_OF_WEEK);


        mConnector = new Connector(getContext(), new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                if (Connector.checkStatus(response)) {
                    shopDetailsModel = Connector.getShop(response, mShopModel);
                    mStoreName.setText(shopDetailsModel.getName());
                    mStoreDistance.setText(String.format(Locale.getDefault(), "%.2f " + getString(R.string.km), Double.valueOf(shopDetailsModel.getDistance())));


                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    String lang = preferences.getString("lang", "error");
                    if (lang.equals("error")) {
                        if (Locale.getDefault().getLanguage().equals("ar"))
                            mNumberOfCouriers.setText(shopDetailsModel.getDelivery_count() + " مندوبين موجودين");
                        else
                            mNumberOfCouriers.setText(shopDetailsModel.getDelivery_count() + " Couriers In");
                    } else if (lang.equals("en")) {
                        mNumberOfCouriers.setText(shopDetailsModel.getDelivery_count() + " Couriers In");
                    } else {
                        mNumberOfCouriers.setText(shopDetailsModel.getDelivery_count() + " مندوبين موجودين");
                    }
                } else {
                    if (getActivity() != null)
                        Helper.showSnackBarMessage(getString(R.string.error), (AppCompatActivity) getActivity());
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                if (getActivity() != null)
                    Helper.showSnackBarMessage(getString(R.string.error), (AppCompatActivity) getActivity());
            }
        });

        /*mGetMyEnrolledConnector = new Connector(getContext(), new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                if (Connector.checkStatus(response)) {
                    if (response.contains(mShopModel.getId())) {
                        mBeACourierParent.setOnCheckedChangeListener(null);
                        mBeACourierParent.setChecked(true);
                        mBeACourierParent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                if (isChecked) {
                                    showAlertDialog("register");
                                } else {
                                    showAlertDialog("unregister");
                                }
                            }
                        });
                    }
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {

            }
        });*/

        /*mEnrollConnector = new Connector(getContext(), new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                mProgressDialog.dismiss();
                if (Connector.checkStatus(response)) {
                    if (shopDetailsModel != null) {
                        if (Connector.checkType(response) == 1) {
                            Helper.showSnackBarMessage(Connector.getMessage(response), (AppCompatActivity) getActivity());
                            int delivery = Integer.parseInt(shopDetailsModel.getDelivery_count());
                            delivery++;
                            shopDetailsModel.setDelivery_count(String.valueOf(delivery));
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                            String lang = preferences.getString("lang", "error");
                            if (lang.equals("error")) {
                                if (Locale.getDefault().getLanguage().equals("ar"))
                                    mNumberOfCouriers.setText(delivery + " مندوبين موجودين");
                                else
                                    mNumberOfCouriers.setText(delivery + " Couriers In");
                            } else if (lang.equals("en")) {
                                mNumberOfCouriers.setText(delivery + " Couriers In");
                            } else {
                                mNumberOfCouriers.setText(delivery + " مندوبين موجودين");
                            }
                        } else {
                            Helper.showSnackBarMessage(Connector.getMessage(response), (AppCompatActivity) getActivity());
                            int delivery = Integer.parseInt(shopDetailsModel.getDelivery_count());
                            delivery--;
                            shopDetailsModel.setDelivery_count(String.valueOf(delivery));
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                            String lang = preferences.getString("lang", "error");
                            if (lang.equals("error")) {
                                if (Locale.getDefault().getLanguage().equals("ar"))
                                    mNumberOfCouriers.setText(delivery + " مندوبين موجودين");
                                else
                                    mNumberOfCouriers.setText(delivery + " Couriers In");
                            } else if (lang.equals("en")) {
                                mNumberOfCouriers.setText(delivery + " Couriers In");
                            } else {
                                mNumberOfCouriers.setText(delivery + " مندوبين موجودين");
                            }
                        }
                    }
                } else {
                    Helper.showSnackBarMessage(getString(R.string.error), (AppCompatActivity) getActivity());
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                mProgressDialog.dismiss();
                Helper.showSnackBarMessage(getString(R.string.error), (AppCompatActivity) getActivity());
            }
        });*/

        if (mShopModel != null)
            mConnector.getRequest(TAG, Connector.createGetShopUrl(mShopModel.getId()));

        mMapView.onCreate(savedInstanceState);

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                mMap.getUiSettings().setRotateGesturesEnabled(false);
                mTracker = new GPSTracker(getContext(), new GPSTracker.OnGetLocation() {
                    @Override
                    public void onGetLocation(double lat, double lon) {
                        if (!mLocated) {
                            mLocated = true;
                            LatLng myLocation = new LatLng(lat, lon);
                            int height = 100;
                            int width = 100;
                            BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.home);
                            Bitmap b = bitmapdraw.getBitmap();
                            Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
                            mMap.addMarker(new MarkerOptions().position(myLocation)).setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                            LatLng shopLocation = null;
                            if (mShopModel != null) {
                                shopLocation = new LatLng(Double.valueOf(mShopModel.getLat()), Double.valueOf(mShopModel.getLon()));
                                bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.shop1);
                                b = bitmapdraw.getBitmap();
                                smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
                                mMap.addMarker(new MarkerOptions().position(shopLocation)).setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                            }


                            LatLngBounds.Builder builder = new LatLngBounds.Builder();
                            builder.include(myLocation);
                            if (shopLocation != null)
                                builder.include(shopLocation);
                            LatLngBounds bounds = builder.build();


                            DisplayMetrics displayMetrics = new DisplayMetrics();
                            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                            int screenHeight = displayMetrics.heightPixels;
                            int screedWidth = displayMetrics.widthPixels;

                            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,screedWidth, 600,150);

                            mMap.moveCamera(cu);
                            mTracker.stopUsingGPS();
                        }
                    }
                });
                if (mTracker.canGetLocation() && !mLocated) {
                    Location location = mTracker.getLocation();
                    if (location != null) {
                        if (location.getLongitude() != 0 && location.getLongitude() != 0) {
                            mLocated = true;
                            LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            int height = 100;
                            int width = 100;
                            BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.home);
                            Bitmap b = bitmapdraw.getBitmap();
                            Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
                            mMap.addMarker(new MarkerOptions().position(myLocation)).setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                            LatLng shopLocation = null;
                            if (mShopModel != null) {
                                shopLocation = new LatLng(Double.valueOf(mShopModel.getLat()), Double.valueOf(mShopModel.getLon()));
                                bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.shop1);
                                b = bitmapdraw.getBitmap();
                                smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
                                mMap.addMarker(new MarkerOptions().position(shopLocation)).setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                            }


                            LatLngBounds.Builder builder = new LatLngBounds.Builder();
                            builder.include(myLocation);
                            if (shopLocation != null)
                                builder.include(shopLocation);
                            LatLngBounds bounds = builder.build();


                            DisplayMetrics displayMetrics = new DisplayMetrics();
                            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                            int screenHeight = displayMetrics.heightPixels;
                            int screedWidth = displayMetrics.widthPixels;

                            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,screedWidth, 600,150);

                            mMap.moveCamera(cu);
                            mTracker.stopUsingGPS();
                        }
                    }
                }
                if (getContext() != null) {
                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true);
                    }
                }
            }
        });


        /*sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);

        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED: {
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        String lang = preferences.getString("lang", "error");
                        if (lang.equals("error")) {
                            if (Locale.getDefault().getLanguage().equals("ar"))
                                mTodayTime.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_arrow_up, 0, 0, 0);
                                //mNumberOfCouriers.setText(shopDetailsModel.getDelivery_count() + " مندوبين موجودين");
                            else
                                mTodayTime.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_up, 0);
                            //mNumberOfCouriers.setText(shopDetailsModel.getDelivery_count() + " Couriers In");
                        } else if (lang.equals("en")) {
                            //mNumberOfCouriers.setText(shopDetailsModel.getDelivery_count() + " Couriers In");
                            mTodayTime.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_up, 0);
                        } else {
                            mTodayTime.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_arrow_up, 0, 0, 0);
                            //mNumberOfCouriers.setText(shopDetailsModel.getDelivery_count() + " مندوبين موجودين");
                        }
                    }
                    break;
                    case BottomSheetBehavior.STATE_COLLAPSED: {
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        String lang = preferences.getString("lang", "error");
                        if (lang.equals("error")) {
                            if (Locale.getDefault().getLanguage().equals("ar"))
                                mTodayTime.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_arrow_down, 0, 0, 0);
                                //mNumberOfCouriers.setText(shopDetailsModel.getDelivery_count() + " مندوبين موجودين");
                            else
                                mTodayTime.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down, 0);
                            //mNumberOfCouriers.setText(shopDetailsModel.getDelivery_count() + " Couriers In");
                        } else if (lang.equals("en")) {
                            //mNumberOfCouriers.setText(shopDetailsModel.getDelivery_count() + " Couriers In");
                            mTodayTime.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down, 0);
                        } else {
                            mTodayTime.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_arrow_down, 0, 0, 0);
                            //mNumberOfCouriers.setText(shopDetailsModel.getDelivery_count() + " مندوبين موجودين");
                        }
                    }
                    break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        //floatingActionButton.animate().scaleX(0).scaleY(0).setDuration(300).start();
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                //floatingActionButton.animate().scaleX(1 - slideOffset).scaleY(1 - slideOffset).setDuration(0).start();
            }
        });*/



        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), OrderNowActivity.class).putExtra("user", mUserModel).putExtra("ShopModel", mShopModel));
            }
        });

        mBeACourierParent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    showAlertDialog("register");
                } else {
                    showAlertDialog("unregister");
                }
            }
        });

        //mGetMyEnrolledConnector.getRequest(TAG, "http://www.cta3.com/waslk/api/get_my_enrolled_shop?user_id=" + mUserModel.getId());


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
        if (mConnector != null)
            mConnector.cancelAllRequests(TAG);
        /*if (mEnrollConnector != null)
            mEnrollConnector.cancelAllRequests(TAG);
        if (mGetMyEnrolledConnector != null)
            mGetMyEnrolledConnector.cancelAllRequests(TAG);*/
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    /*@OnClick(R.id.btn_bottom_sheet)
    public void toggleBottomSheet() {
        if (sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }*/


    public void showAlertDialog(String type) {
        if (getContext() != null) {
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
            } else {
                builder = new AlertDialog.Builder(getContext());
            }
            if (type.equals("register"))
                builder.setTitle(getString(R.string.be_courier))
                        .setMessage(getString(R.string.be_courier_description))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (mShopModel != null && mUserModel != null) {
                                    if (mUserModel.getDelivery().equals("1")) {
                                        //mEnrollConnector.getRequest(TAG, Connector.createEnrollInShopUrl() + "?user_id=" + mUserModel.getId() + "&shop_id=" + mShopModel.getId());
                                        mProgressDialog = Helper.showProgressDialog(getContext(), getString(R.string.loading), false);
                                    } else {
                                        mBeACourierParent.setOnCheckedChangeListener(null);
                                        mBeACourierParent.setChecked(false);
                                        mBeACourierParent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                            @Override
                                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                if (isChecked) {
                                                    showAlertDialog("register");
                                                } else {
                                                    showAlertDialog("unregister");
                                                }
                                            }
                                        });
                                        Toast.makeText(getContext(), getString(R.string.be_agent_first), Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mBeACourierParent.setOnCheckedChangeListener(null);
                                mBeACourierParent.setChecked(false);
                                mBeACourierParent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                    @Override
                                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                        if (isChecked) {
                                            showAlertDialog("register");
                                        } else {
                                            showAlertDialog("unregister");
                                        }
                                    }
                                });
                            }
                        })
                        .show();
            else
                builder.setTitle(getString(R.string.cancel_courier))
                        .setMessage(getString(R.string.cancel_be_courier_description))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (mShopModel != null && mUserModel != null) {
                                    //mEnrollConnector.getRequest(TAG, Connector.createEnrollInShopUrl() + "?user_id=" + mUserModel.getId() + "&shop_id=" + mShopModel.getId());
                                    mProgressDialog = Helper.showProgressDialog(getContext(), getString(R.string.loading), false);
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mBeACourierParent.setOnCheckedChangeListener(null);
                                mBeACourierParent.setChecked(true);
                                mBeACourierParent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                    @Override
                                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                        if (isChecked) {
                                            showAlertDialog("register");
                                        } else {
                                            showAlertDialog("unregister");
                                        }
                                    }
                                });
                            }
                        })
                        .show();
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
