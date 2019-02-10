package com.waslak.waslak;


import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class IntroFragment extends Fragment {


    public IntroFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_intro, container, false);

        TextView text = v.findViewById(R.id.text);


        if (getArguments() != null) {
            text.setText(getArguments().getString("text"));
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
