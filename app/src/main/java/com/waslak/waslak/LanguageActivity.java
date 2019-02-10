package com.waslak.waslak;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.waslak.waslak.models.UserModel;
import com.waslak.waslak.networkUtils.Connector;
import com.waslak.waslak.utils.Helper;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LanguageActivity extends AppCompatActivity {

    private static final String TAG = "LanguageActivity";
    @BindView(R.id.arabic_language)
    Button mArabicLanguageButton;
    @BindView(R.id.english_language)
    Button mEnglishLanguageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);
        ButterKnife.bind(this);

        mArabicLanguageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(LanguageActivity.this);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("lang","ar");
                editor.apply();
                setLocale("ar");
                startActivity(new Intent(LanguageActivity.this,IntroActivity.class));
            }
        });

        mEnglishLanguageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(LanguageActivity.this);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("lang","en");
                editor.apply();
                setLocale("en");
                startActivity(new Intent(LanguageActivity.this,IntroActivity.class));

            }
        });

    }


    @Override
    public void onBackPressed() {
        Helper.showAlertDialog(LanguageActivity.this, getString(R.string.exit_app), "", true, getString(R.string.yes), getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LanguageActivity.super.onBackPressed();
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
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
