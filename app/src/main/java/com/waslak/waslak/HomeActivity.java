package com.waslak.waslak;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.github.javiersantos.appupdater.AppUpdater;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.koushikdutta.ion.Ion;
import com.squareup.picasso.Picasso;
import com.waslak.waslak.models.UserModel;
import com.waslak.waslak.networkUtils.Connector;
import com.waslak.waslak.networkUtils.Constants;
import com.waslak.waslak.utils.GPSTracker;
import com.waslak.waslak.utils.Helper;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Locale;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.bottom_navigation_view)
    BottomNavigationViewEx mBottomNavigationView;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawer;
    @BindView(R.id.nav_view)
    NavigationView mNavigationView;
    UserModel mUserModel;

    Connector mUpdateAddress;

    GPSTracker mTracker = null;

    FragmentManager mFragmentManager;

    UserFragment.OnMenuClicked onMenuClicked = new UserFragment.OnMenuClicked() {
        @Override
        public void setOnMenuClicked() {
            mDrawer.openDrawer(Gravity.START);
        }
    };

    NotificationsFragment.OnMenuClicked onMenuClickedNotification = new NotificationsFragment.OnMenuClicked() {
        @Override
        public void setOnMenuClicked() {
            mDrawer.openDrawer(Gravity.START);
        }
    };

    ActiveOrdersFragment.OnMenuClicked onMenuClickedOrders = new ActiveOrdersFragment.OnMenuClicked() {
        @Override
        public void setOnMenuClicked() {
            mDrawer.openDrawer(Gravity.START);
        }
    };

    StoresFragment.OnMenuClicked onMenuClickedStores = new StoresFragment.OnMenuClicked() {
        @Override
        public void setOnMenuClicked() {
            mDrawer.openDrawer(Gravity.START);
        }
    };
    private final String TAG = HomeActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        Ion.getDefault(this).getHttpClient().getSSLSocketMiddleware().setTrustManagers(new TrustManager[] {new X509TrustManager() {
            @Override
            public void checkClientTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {}

            @Override
            public void checkServerTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {}

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        }});

        ButterKnife.bind(this);
        setupBottomNavigationView();
        StoresFragment storesFragment = new StoresFragment();
        storesFragment.setOnMenuClicked(onMenuClickedStores);
        goToFragment(storesFragment, "StoresFragment");
        mNavigationView.setNavigationItemSelectedListener(this);

        AppUpdater appUpdater = new AppUpdater(this);
        appUpdater.start();

        mUserModel = Helper.getUserSharedPreferences(this);

        if (URLUtil.isValidUrl(mUserModel.getImage()))
            Picasso.get().load(mUserModel.getImage()).fit().centerCrop().into(((ImageView) (mNavigationView.getHeaderView(0).findViewById(R.id.profile_image))));
        else {
            Picasso.get().load(Constants.WASLAK_BASE_URL + "/mobile/prod_img/" + mUserModel.getImage()).fit().centerCrop().into(((ImageView) (mNavigationView.getHeaderView(0).findViewById(R.id.profile_image))));
        }
        ((TextView) mNavigationView.getHeaderView(0).findViewById(R.id.name)).setText(String.format("%s %s", getString(R.string.hello), mUserModel.getName()));

        if (savedInstanceState != null && savedInstanceState.containsKey("Fragment")) {
            String value = savedInstanceState.getString("Fragment");
            if (value != null) {
                switch (value) {
                    case "ActiveOrdersFragment":
                        ActiveOrdersFragment activeOrdersFragment = new ActiveOrdersFragment();
                        activeOrdersFragment.setOnMenuClicked(onMenuClickedOrders);
                        goToFragment(activeOrdersFragment, "ActiveOrdersFragment");
                        break;
                    case "NotificationsFragment":
                        NotificationsFragment notificationsFragment = new NotificationsFragment();
                        notificationsFragment.setOnMenuClicked(onMenuClickedNotification);
                        goToFragment(notificationsFragment, "NotificationsFragment");
                        break;
                    case "UserFragment":
                        UserFragment userFragment = new UserFragment();
                        userFragment.setOnMenuClicked(onMenuClicked);
                        goToFragment(userFragment, "UserFragment");
                        break;
                }
            }
        }

        if (mUserModel.getDelivery() == null)
            mUserModel.setDelivery("0");

        if (mUserModel.getDelivery().equals("1")) {
            mNavigationView.getMenu().getItem(7).setVisible(false);
        } else {
            mNavigationView.getMenu().getItem(7).setVisible(true);
        }

        mUpdateAddress = new Connector(this, new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {

            }
        });


        if (mUserModel.getDelivery().equals("1")) {
//            mTracker = new GPSTracker(this, new GPSTracker.OnGetLocation() {
//                @Override
//                public void onGetLocation(double longtiude, double lantitude) {
//                    if (longtiude != 0 && lantitude != 0) {
//                        Helper.writeToLog("Update : " + lantitude + " " + longtiude);
//                        mUpdateAddress.getRequest(TAG, Constants.WASLAK_BASE_URL + "/mobile/api/update_address?longitude=" + longtiude + "&latitude=" + lantitude + "&id=" + mUserModel.getId());
//                    }
//                }
//            });
//            if (mTracker.canGetLocation() && mTracker.getLatitude() != 0) {
//                Helper.writeToLog("Update : " + mTracker.getLatitude() + " " + mTracker.getLongitude());
//                mUpdateAddress.getRequest(TAG, Constants.WASLAK_BASE_URL + "/mobile/api/update_address?longitude=" + mTracker.getLatitude() + "&latitude=" + mTracker.getLongitude() + "&id=" + mUserModel.getId());
//            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(new Intent(HomeActivity.this,LocationTracker.class));
            } else {
                startService(new Intent(HomeActivity.this,LocationTracker.class));
            }
        }




        if (getIntent().hasExtra("notification"))
            if (getIntent().getStringExtra("notification").equals("1")) {
                NotificationsFragment notificationsFragment = new NotificationsFragment();
                notificationsFragment.setOnMenuClicked(onMenuClickedNotification);
                goToFragment(notificationsFragment, "NotificationsFragment");
                mBottomNavigationView.setCurrentItem(2);
            }

        if (getIntent().hasExtra("orders"))
            if (getIntent().getStringExtra("orders").equals("1")) {
                ActiveOrdersFragment notificationsFragment = new ActiveOrdersFragment();
                notificationsFragment.setOnMenuClicked(onMenuClickedOrders);
                goToFragment(notificationsFragment, "ActiveOrdersFragment");
                mBottomNavigationView.setCurrentItem(1);
            }

    }


    public void setupBottomNavigationView() {
        mBottomNavigationView.enableAnimation(false);
        mBottomNavigationView.enableShiftingMode(false);
        mBottomNavigationView.enableItemShiftingMode(false);
        mBottomNavigationView.setCurrentItem(0);
        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.nav_me:
                        UserFragment userFragment = new UserFragment();
                        userFragment.setOnMenuClicked(onMenuClicked);
                        goToFragment(userFragment, "UserFragment");
                        return true;
                    case R.id.nav_order:
                        ActiveOrdersFragment activeOrdersFragment = new ActiveOrdersFragment();
                        activeOrdersFragment.setOnMenuClicked(onMenuClickedOrders);
                        goToFragment(activeOrdersFragment, "ActiveOrdersFragment");
                        return true;
                    case R.id.nav_notifications:
                        NotificationsFragment notificationsFragment = new NotificationsFragment();
                        notificationsFragment.setOnMenuClicked(onMenuClickedNotification);
                        Helper.setNotificationCount(HomeActivity.this, 0);
                        goToFragment(notificationsFragment, "NotificationsFragment");
                        return true;
                    case R.id.nav_stores:
                        StoresFragment storesFragment = new StoresFragment();
                        storesFragment.setOnMenuClicked(onMenuClickedStores);
                        goToFragment(storesFragment, "StoresFragment");
                        return true;
                    default:
                        return true;
                }
            }
        });
    }

    public void goToFragment(Fragment fragment, String tag) {
        if (mFragmentManager != null) {
            mFragmentManager.beginTransaction().replace(R.id.content_main, fragment, tag).commit();
        } else {
            mFragmentManager = getSupportFragmentManager();
            mFragmentManager.beginTransaction().replace(R.id.content_main, fragment, tag).commit();
        }
    }


    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else if (mFragmentManager != null) {
            Fragment fragment = mFragmentManager.findFragmentById(R.id.content_main);
            if (fragment instanceof UserFragment || fragment instanceof ActiveOrdersFragment || fragment instanceof NotificationsFragment || fragment instanceof StoresFragment) {
                Helper.showAlertDialog(HomeActivity.this, getString(R.string.exit_app), "", true, getString(R.string.yes), getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        HomeActivity.super.onBackPressed();
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
            }
        } else {
            mFragmentManager = getSupportFragmentManager();
            Fragment fragment = mFragmentManager.findFragmentById(R.id.content_main);
            if (fragment instanceof UserFragment || fragment instanceof ActiveOrdersFragment || fragment instanceof NotificationsFragment || fragment instanceof StoresFragment) {
                Helper.showAlertDialog(HomeActivity.this, getString(R.string.exit_app), "", true, getString(R.string.yes), getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        HomeActivity.super.onBackPressed();
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
            }
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mFragmentManager != null) {
            Fragment fragment = mFragmentManager.findFragmentById(R.id.content_main);
            if (fragment instanceof ActiveOrdersFragment) {
                outState.putString("Fragment", "ActiveOrdersFragment");
            } else if (fragment instanceof NotificationsFragment) {
                outState.putString("Fragment", "NotificationsFragment");
            } else if (fragment instanceof UserFragment) {
                outState.putString("Fragment", "UserFragment");
            }
        } else {
            mFragmentManager = getSupportFragmentManager();
            Fragment fragment = mFragmentManager.findFragmentById(R.id.content_main);
            if (fragment instanceof ActiveOrdersFragment) {
                outState.putString("Fragment", "ActiveOrdersFragment");
            } else if (fragment instanceof NotificationsFragment) {
                outState.putString("Fragment", "Notifications Fragment");
            } else if (fragment instanceof UserFragment) {
                outState.putString("Fragment", "UserFragment");
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_privacy_policy:
                startActivity(new Intent(HomeActivity.this, WebViewActivity.class).putExtra("type", "privacy"));
                return true;
            case R.id.nav_edit_profile:
                startActivity(new Intent(HomeActivity.this, AccountActivity.class).putExtra("Type", "Edit").putExtra("user", mUserModel));
                return true;
            case R.id.nav_my_check_in_list:
                startActivity(new Intent(HomeActivity.this, CheckInActivity.class).putExtra("user", mUserModel));
                return true;
            case R.id.nav_about_us:
                startActivity(new Intent(HomeActivity.this, WebViewActivity.class).putExtra("type", "about"));
                return true;
            case R.id.nav_terms_conditions:
                startActivity(new Intent(HomeActivity.this, WebViewActivity.class).putExtra("type", "terms"));
                return true;
            case R.id.nav_log_out:
                Helper.removeUserFromSharedPreferences(HomeActivity.this);
                startActivity(new Intent(HomeActivity.this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                return true;
            case R.id.nav_become_an_agent:
                startActivity(new Intent(HomeActivity.this, BeAgentActivity.class));
                return true;
            case R.id.nav_app_tour:
                startActivity(new Intent(HomeActivity.this, IntroActivity.class).putExtra("type", "inside"));
                return true;
            case R.id.nav_featured_customer:
                startActivity(new Intent(HomeActivity.this, BecomeFeaturedClient.class));
                return true;
            default:
                return true;
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
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((mMessageReceiver),
                new IntentFilter("MyData")
        );
        if (Helper.getDeliverySharedPreferences(this).equals("1")) {
            mNavigationView.getMenu().getItem(7).setVisible(false);
        } else {
            mNavigationView.getMenu().getItem(7).setVisible(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras() != null) {

            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTracker != null)
            mTracker.stopUsingGPS();
    }


    /*private Badge addBadgeAt(int position, int number) {
        return new QBadgeView(this)
                .setBadgeNumber(number)
                .setGravityOffset(30, 2, true)
                .bindTarget(mBottomNavigationView.getBottomNavigationItemView(position));
    }*/

}
