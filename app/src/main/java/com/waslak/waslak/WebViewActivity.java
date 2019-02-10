package com.waslak.waslak;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;

import com.waslak.waslak.networkUtils.Connector;
import com.waslak.waslak.utils.Helper;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WebViewActivity extends AppCompatActivity {

    @BindView(R.id.web_view)
    WebView mWebView;

    String mType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        ButterKnife.bind(this);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent().hasExtra("type")){
            mType = getIntent().getStringExtra("type");
        }

        if (mType.equals("about")) {
            Helper.writeToLog("Test");
            mWebView.loadUrl(Connector.createWebViewUrl() + "?type=about");
            if (getSupportActionBar() != null)
                getSupportActionBar().setTitle(getString(R.string.about_us));
        }
        else if (mType.equals("privacy")) {
            mWebView.loadUrl(Connector.createWebViewUrl() + "?type=privacy");
            if (getSupportActionBar() != null)
                getSupportActionBar().setTitle(getString(R.string.privacy_policy));
        }
        else if (mType.equals("terms")) {
            mWebView.loadUrl(Connector.createWebViewUrl() + "?type=terms");
            if (getSupportActionBar() != null)
                getSupportActionBar().setTitle(getString(R.string.terms_and_conditions));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return false;
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



}
