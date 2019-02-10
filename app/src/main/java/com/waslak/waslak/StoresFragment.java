package com.waslak.waslak;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.waslak.waslak.adapters.FragmentAdapter;
import com.waslak.waslak.models.UserModel;
import com.waslak.waslak.utils.Helper;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 */
public class StoresFragment extends Fragment {

    @BindView(R.id.view_pager)
    ViewPager mViewPager;
    @BindView(R.id.tab)
    TabLayout mTabLayout;
    @BindView(R.id.menu_button)
    ImageView mMenuButton;
    @BindView(R.id.setting_button)
    TextView mSettingButton;
    @BindView(R.id.search_parent)
    View mSearchParent;
    @BindView(R.id.search_text)
    EditText mSearchText;
    @BindView(R.id.search_button)
    Button mSearchButton;

    OnMenuClicked mOnMenuClicked;
    UserModel mUserModel;

    FragmentAdapter fragmentPagerAdapter;

    public StoresFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_stores, container, false);

        ButterKnife.bind(this, view);

        if (getActivity() != null)
            mUserModel = (UserModel) getActivity().getIntent().getSerializableExtra("user");

        fragmentPagerAdapter = new FragmentAdapter(getChildFragmentManager(), getContext());
        mViewPager.setAdapter(fragmentPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);

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

        mSearchParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(),SearchActivity.class).putExtra("user",mUserModel));
            }
        });

        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(),SearchActivity.class).putExtra("user",mUserModel));
            }
        });

        mSearchText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(),SearchActivity.class).putExtra("user",mUserModel));
            }
        });

        if (!isHighAccuracy()) {
            Helper.showAlertDialog(getContext(), getString(R.string.open_location), "", false,getString(R.string.ok) ,"",null,null);
        }


        return view;
    }

    public interface OnMenuClicked {
        void setOnMenuClicked();
    }


    public void setOnMenuClicked(OnMenuClicked mOnMenuClicked) {
        this.mOnMenuClicked = mOnMenuClicked;
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

    public boolean isHighAccuracy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int locationMode = 0;
            try {
                locationMode = Settings.Secure.getInt(getContext().getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }

            return (locationMode != Settings.Secure.LOCATION_MODE_OFF && locationMode == Settings.Secure.LOCATION_MODE_HIGH_ACCURACY); //check location mode

        } else {
            String locationProviders = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }
}
