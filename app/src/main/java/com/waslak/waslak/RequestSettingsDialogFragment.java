package com.waslak.waslak;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;

import java.util.Locale;

public class RequestSettingsDialogFragment extends AppCompatDialogFragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_request_settings_dialog, container, false);
        if (getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        return v;
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
