package com.waslak.waslak;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.VolleyError;
import com.waslak.waslak.networkUtils.Connector;
import com.waslak.waslak.networkUtils.Constants;
import com.waslak.waslak.utils.Helper;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ComplaintsActivity extends AppCompatActivity {

    @BindView(R.id.full_name_edit_text)
    EditText mFullNameEditText;
    @BindView(R.id.email)
    EditText mEmailEditText;
    @BindView(R.id.title)
    EditText mTitleEditText;
    @BindView(R.id.body)
    EditText mBodyEditText;
    @BindView(R.id.send)
    Button mSendButton;

    Connector mConnector;
    ProgressDialog mProgressDialog;

    private final String TAG = "ComplaintsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaints);
        ButterKnife.bind(this);

        setTitle(getString(R.string.complaints));

        mFullNameEditText.setEnabled(false);
        mEmailEditText.setEnabled(false);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mConnector = new Connector(this, new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                mProgressDialog.dismiss();
                if (Connector.checkStatus(response))
                    finish();
                else
                    Helper.showSnackBarMessage(getString(R.string.error),ComplaintsActivity.this);
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                mProgressDialog.dismiss();
                Helper.showSnackBarMessage(getString(R.string.error),ComplaintsActivity.this);
            }
        });

        if (Helper.PreferencesContainsUser(this)) {
            mFullNameEditText.setText(Helper.getUserSharedPreferences(this).getName());
            mEmailEditText.setText(Helper.getUserSharedPreferences(this).getUsername());
        }


        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = mTitleEditText.getText().toString();
                String body = mBodyEditText.getText().toString();
                if (title.isEmpty()) {
                    Helper.showSnackBarMessage(getString(R.string.enter_title),ComplaintsActivity.this);
                } else if (body.isEmpty()) {
                    Helper.showSnackBarMessage(getString(R.string.enter_title),ComplaintsActivity.this);
                } else {
                    mProgressDialog = Helper.showProgressDialog(ComplaintsActivity.this,getString(R.string.loading),false);
                    mConnector.getRequest(TAG, Constants.WASLAK_BASE_URL + "/mobile/api/send_feedback?name=" + mFullNameEditText.getText().toString().replaceAll(" ","%20") +"&title="+mTitleEditText.getText().toString().replaceAll(" ","%20")+"&comment=" + mBodyEditText.getText().toString().replaceAll(" ","%20") +"&user_id=" + Helper.getUserSharedPreferences(ComplaintsActivity.this).getId() + "&email=" + mEmailEditText.getText().toString());
                }
            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mConnector != null)
            mConnector.cancelAllRequests(TAG);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return false;
    }

}
