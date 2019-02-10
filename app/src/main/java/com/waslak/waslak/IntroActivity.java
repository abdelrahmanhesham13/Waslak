package com.waslak.waslak;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.github.paolorotolo.appintro.model.SliderPage;
import com.waslak.waslak.R;
import com.waslak.waslak.utils.Helper;

import java.util.Locale;

public class IntroActivity extends AppIntro2 {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Helper.saveShowIntroSharedPreferences(this,false);


        IntroFragment frag1 = new IntroFragment();
        Bundle bun1 = new Bundle();
        bun1.putString("text",getString(R.string.text1));
        frag1.setArguments(bun1);

        IntroFragment frag2 = new IntroFragment();
        Bundle bun2 = new Bundle();
        bun2.putString("text",getString(R.string.text2));
        frag2.setArguments(bun2);


        IntroFragment frag3 = new IntroFragment();
        Bundle bun3 = new Bundle();
        bun3.putString("text",getString(R.string.text3));
        frag3.setArguments(bun3);

        IntroFragment frag4 = new IntroFragment();
        Bundle bun4 = new Bundle();
        bun4.putString("text",getString(R.string.text4));
        frag4.setArguments(bun4);

        IntroFragment frag5 = new IntroFragment();
        Bundle bun5 = new Bundle();
        bun5.putString("text",getString(R.string.text5));
        frag5.setArguments(bun5);

        addSlide(frag1);
        addSlide(frag2);
        addSlide(frag3);
        addSlide(frag4);
        addSlide(frag5);

        setBarColor(getResources().getColor(android.R.color.transparent));

    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        if (getIntent().hasExtra("type")){
            finish();
        } else {
            startActivity(new Intent(IntroActivity.this, LoginActivity.class));
        }
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        if (getIntent().hasExtra("type")){
            finish();
        } else {
            startActivity(new Intent(IntroActivity.this, LoginActivity.class));
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

}
