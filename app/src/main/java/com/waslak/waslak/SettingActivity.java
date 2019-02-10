package com.waslak.waslak;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.waslak.waslak.networkUtils.Connector;
import com.waslak.waslak.utils.Helper;

import java.util.Locale;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingActivity extends AppCompatActivity {

    @BindView(R.id.notification_settings)
    TextView mNotificationSettings;
    @BindView(R.id.language_setting)
    TextView mLanguageSetting;
    @BindView(R.id.complaints)
    TextView mComplaints;
    @BindView(R.id.nav_my_check_in_list)
    TextView mMyCheckInList;
    @BindView(R.id.how_to_be)
    TextView mHowToBe;
    @BindView(R.id.app_tour)
    TextView mAppTour;
    @BindView(R.id.notification_setting_enable)
    Switch mNotificationSettingEnable;

    Connector mUpdateNotification;
    ProgressDialog mProgressDialog;

    private final String TAG = SettingActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);

        mUpdateNotification = new Connector(this, new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                if (mProgressDialog != null && mProgressDialog.isShowing() && !SettingActivity.this.isFinishing())
                    mProgressDialog.dismiss();

            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                if (mProgressDialog != null && mProgressDialog.isShowing() && !SettingActivity.this.isFinishing())
                    mProgressDialog.dismiss();
            }
        });

        mNotificationSettingEnable.setChecked(Helper.getNotificationSharedPreferences(this));


        mNotificationSettingEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Helper.saveNotificationSharedPreferences(SettingActivity.this,b);
                mProgressDialog = Helper.showProgressDialog(SettingActivity.this,getString(R.string.loading),false);
                if (b){
                    mUpdateNotification.getRequest(TAG,"https://www.cta3.com/waslk/api/update_notification?id=" + Helper.getUserSharedPreferences(SettingActivity.this).getId() + "&status=1");
                } else {
                    mUpdateNotification.getRequest(TAG,"https://www.cta3.com/waslk/api/update_notification?id=" + Helper.getUserSharedPreferences(SettingActivity.this).getId() + "&status=0");
                }
            }
        });

        mNotificationSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i,1);
                } catch (Exception e){
                    e.printStackTrace();
                    Helper.showLongTimeToast(SettingActivity.this,getString(R.string.error));
                }

            }
        });

        mLanguageSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });

        mComplaints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingActivity.this,ComplaintsActivity.class));
            }
        });

        mMyCheckInList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingActivity.this,CheckInActivity.class).putExtra("user",Helper.getUserSharedPreferences(SettingActivity.this)));
            }
        });

        mHowToBe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingActivity.this,BeAgentActivity.class));
            }
        });

        mAppTour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingActivity.this,IntroActivity.class).putExtra("type","inside"));
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1){
            if (resultCode == Activity.RESULT_OK){
                Uri uri = data.getData();
                if (uri != null) {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("notification_tone", uri.toString());
                    editor.apply();
                }
            }
        }
    }


    public void showDialog(){
        int selection = 0;
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        final CharSequence items[] = new CharSequence[] {"English","العربية"};
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String lang = preferences.getString("lang", "error");
        if (lang.equals("error")) {
            if (Locale.getDefault().getLanguage().equals("ar"))
                selection = 1;
            else
                selection = 0;
        } else if (lang.equals("en")) {
            selection = 0;
        } else {
            selection = 1;
        }
        adb.setSingleChoiceItems(items, selection, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0){
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SettingActivity.this);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("lang","en");
                    editor.apply();
                    setLocale("en");
                    startActivity(new Intent(SettingActivity.this, HomeActivity.class).putExtra("user",Helper.getUserSharedPreferences(SettingActivity.this)).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                } else {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SettingActivity.this);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("lang","ar");
                    editor.apply();
                    setLocale("ar");
                    startActivity(new Intent(SettingActivity.this, HomeActivity.class).putExtra("user", Helper.getUserSharedPreferences(SettingActivity.this)).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                }
                dialog.dismiss();
            }
        });

        adb.setNegativeButton(getString(R.string.cancel), null);
        adb.setTitle(getString(R.string.select_time));
        adb.show();
    }
}
