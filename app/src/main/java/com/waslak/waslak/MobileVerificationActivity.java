package com.waslak.waslak;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.lamudi.phonefield.PhoneInputLayout;
import com.waslak.waslak.utils.Helper;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MobileVerificationActivity extends AppCompatActivity {

    @BindView(R.id.phone_input_layout)
    PhoneInputLayout mMobileEditText;
    @BindView(R.id.verify_phone)
    Button mVerifyPhone;
    @BindView(R.id.verification_code)
    EditText mVerificationCodeEditText;
    @BindView(R.id.mobile_verification_parent)
    View mMobileVerificationParent;
    @BindView(R.id.progressIndicator)
    ProgressBar mProgressIndicator;

    String mPhoneNumber;

    PhoneAuthProvider.ForceResendingToken mResendToken;
    String mVerificationId = "";
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private final String TAG = "MobileVerification";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_verification);
        ButterKnife.bind(this);
        setTitle(getString(R.string.mobile_verification));
        FirebaseAuth.getInstance().useAppLanguage();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }




        mMobileEditText.setHint(R.string.enter_phone);
        mMobileEditText.setDefaultCountry("SA");
        mMobileEditText.setTextColor(getResources().getColor(R.color.colorPrimary));

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                Log.d(TAG, "onVerificationCompleted:" + credential);

                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.w(TAG, "onVerificationFailed", e);
                if (e instanceof FirebaseTooManyRequestsException)
                    Helper.showSnackBarMessage(getString(R.string.blocked_device), MobileVerificationActivity.this);
                else
                    Helper.showSnackBarMessage(getString(R.string.error_in_phone), MobileVerificationActivity.this);
                mProgressIndicator.setVisibility(View.GONE);
                mMobileVerificationParent.setVisibility(View.VISIBLE);
                mVerifyPhone.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                Log.d(TAG, "onCodeSent:" + verificationId);
                mVerificationId = verificationId;
                mResendToken = token;
                mProgressIndicator.setVisibility(View.GONE);
                mMobileVerificationParent.setVisibility(View.INVISIBLE);
                mVerificationCodeEditText.setVisibility(View.VISIBLE);
                mVerifyPhone.setVisibility(View.VISIBLE);
                mVerifyPhone.setText(getString(R.string.verify));
            }
        };

        mVerifyPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVerifyPhone.getText().equals(getString(R.string.send_code))) {
                    boolean valid = true;
                    if (mMobileEditText.isValid()) {
                        mMobileEditText.setError(null);
                    } else {
                        mMobileEditText.setError(getString(R.string.invalid_phone_number));
                        valid = false;
                    }

                    if (valid) {
                        mPhoneNumber = mMobileEditText.getPhoneNumber();
                        Helper.writeToLog(mPhoneNumber);
                        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                                mPhoneNumber,
                                30,
                                TimeUnit.SECONDS,
                                MobileVerificationActivity.this,
                                mCallbacks);
                        Helper.hideKeyboard(MobileVerificationActivity.this, v);
                        mMobileVerificationParent.setVisibility(View.INVISIBLE);
                        mProgressIndicator.setVisibility(View.VISIBLE);
                        mVerifyPhone.setVisibility(View.INVISIBLE);
                    } else {
                        Helper.showSnackBarMessage(getString(R.string.invalid_phone_number), MobileVerificationActivity.this);
                    }
                } else {
                    if (TextUtils.isEmpty(mVerificationCodeEditText.getText().toString())) {
                        Helper.showSnackBarMessage(getString(R.string.enter_code), MobileVerificationActivity.this);

                    } else {
                        mVerificationCodeEditText.setVisibility(View.GONE);
                        mVerifyPhone.setVisibility(View.GONE);
                        mProgressIndicator.setVisibility(View.VISIBLE);
                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, mVerificationCodeEditText.getText().toString());
                        signInWithPhoneAuthCredential(credential);
                    }
                }
            }
        });


        if (getIntent().hasExtra("mobile")){
            mMobileEditText.setPhoneNumber(getIntent().getStringExtra("mobile"));
            mVerifyPhone.performClick();
        }

    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra("result", "verified");
                            returnIntent.putExtra("phone", mPhoneNumber);
                            setResult(Activity.RESULT_OK, returnIntent);
                            finish();
                        } else {
                            mVerificationCodeEditText.setVisibility(View.VISIBLE);
                            mVerifyPhone.setVisibility(View.VISIBLE);
                            mProgressIndicator.setVisibility(View.GONE);
                            Helper.showSnackBarMessage(getString(R.string.error), MobileVerificationActivity.this);
                        }
                    }
                });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
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
