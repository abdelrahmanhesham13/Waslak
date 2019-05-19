package com.waslak.waslak;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.crashlytics.android.Crashlytics;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.squareup.picasso.Picasso;
import com.waslak.waslak.adapters.MessagesAdapter;
import com.waslak.waslak.models.ChatModel;
import com.waslak.waslak.models.MessageModel;
import com.waslak.waslak.models.RequestModel;
import com.waslak.waslak.models.ShopModel;
import com.waslak.waslak.models.UserModel;
import com.waslak.waslak.networkUtils.Connector;
import com.waslak.waslak.utils.Helper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.leolin.shortcutbadger.ShortcutBadger;

import static com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage;

public class ChatActivity extends AppCompatActivity {

    public final String TAG = "ChatActivity";

    @BindView(R.id.scroll)
    NestedScrollView scroll;
    @BindView(R.id.messages)
    RecyclerView mMessagesRecycler;
    @BindView(R.id.store_name)
    TextView mStoreName;
    @BindView(R.id.order_id)
    TextView mOrderId;
    @BindView(R.id.back_button)
    ImageView mBackButton;
    @BindView(R.id.store_image)
    ImageView mStoreImage;
    @BindView(R.id.ready)
    Button mReady;
    @BindView(R.id.username)
    TextView mUsername;
    @BindView(R.id.refresh_button)
    ImageView refresh;
    @BindView(R.id.send_parent)
    View mSendParent;
    MessagesAdapter mAdapter;
    @BindView(R.id.message_text)
    EditText mMessageText;
    @BindView(R.id.send_btn)
    ImageView mSendMessageButton;
    @BindView(R.id.progressIndicator)
    ProgressBar mProgressBar;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.send_photo)
    ImageView mSendPhoto;
    @BindView(R.id.track_order)
    TextView mTrack;

    RequestModel mRequestModel;
    RequestModel mRequestModelDetails;
    UserModel mUserModel;
    ShopModel mShopModel;
    ChatModel mChatModel;

    Connector mConnector;
    Connector mConnectorMessages;
    Connector mConnectorSendMessage;
    Connector mConnectorCancelOrder;
    Connector mConnectorRate;

    ProgressDialog mProgressDialog;
    ProgressDialog mProgressDialogCancelOrder;

    String message;
    String mType;
    float mRatingNumber;
    String mImage;

    File mSelectedFile;

    ArrayList<MessageModel> mMessageModels;
    Menu mMenu;

    android.app.AlertDialog alertDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);
        ShortcutBadger.removeCount(this);
        Helper.setNotificationCount(this, 0);


        setSupportActionBar(mToolbar);
        if (getIntent() != null && getIntent().hasExtra("chat")) {
            mChatModel = (ChatModel) getIntent().getSerializableExtra("chat");
            mReady.setVisibility(View.GONE);
            mSendParent.setVisibility(View.VISIBLE);
            mTrack.setVisibility(View.VISIBLE);
        }



        if (getIntent().hasExtra("type")) {

            if (getIntent().getStringExtra("type").equals("admin")) {
                mReady.setVisibility(View.GONE);
                mSendParent.setVisibility(View.VISIBLE);
                mStoreName.setText("Admin Chat");
                mOrderId.setVisibility(View.GONE);
                mUsername.setVisibility(View.GONE);
            }
        } else {
            getIntent().putExtra("type","5raaaa");
        }

        if (getIntent() != null && getIntent().hasExtra("goToChat")) {
            mRequestModel = new RequestModel();
            mRequestModel.setId(getIntent().getStringExtra("request_id"));
            mChatModel = new ChatModel();
            mChatModel.setChatId(getIntent().getStringExtra("chat_id"));
            mReady.setVisibility(View.GONE);
            mSendParent.setVisibility(View.VISIBLE);
            mTrack.setVisibility(View.VISIBLE);
        }

        if (getIntent() != null && getIntent().hasExtra("goToRequest")) {
            mRequestModel = new RequestModel();
            mRequestModel.setId(getIntent().getStringExtra("request_id"));
        }

        mUserModel = Helper.getUserSharedPreferences(this);


        if (getIntent() != null && getIntent().hasExtra("request")) {
            mRequestModel = (RequestModel) getIntent().getSerializableExtra("request");
            mOrderId.setText(String.format("%s : %s", getString(R.string.order_id), mRequestModel.getId()));
            if (mRequestModel.getUser().getId().equals(mUserModel.getId())) {
                mUsername.setText(mRequestModel.getDelivery().getName());
            } else {
                mUsername.setText(mRequestModel.getUser().getName());
            }
        }

        if (getIntent() != null && getIntent().hasExtra("shopModel")) {
            mShopModel = (ShopModel) getIntent().getSerializableExtra("shopModel");
            mStoreName.setText(mShopModel.getName());
            if (!mShopModel.getImage().isEmpty())
                Picasso.get().load(mShopModel.getImage()).fit().centerCrop().into(mStoreImage);
        } else {
            if (mRequestModel != null)
                mShopModel = mRequestModel.getShop();
            if (mShopModel != null) {
                mStoreName.setText(mRequestModel.getShop().getName());
                if (URLUtil.isValidUrl(mShopModel.getImage()))
                    Picasso.get().load(mShopModel.getImage()).fit().centerCrop().into(mStoreImage);
                else {
                    Picasso.get().load("http://www.as.cta3.com/waslk/prod_img/" + mShopModel.getImage()).fit().centerCrop().into(mStoreImage);
                }
            }
        }
        mMessageModels = new ArrayList<>();


        mMessagesRecycler.setHasFixedSize(true);
        mMessagesRecycler.setNestedScrollingEnabled(false);
        mMessagesRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        mConnectorRate = new Connector(this, new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                alertDialog.dismiss();
                finish();
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                alertDialog.dismiss();
                Helper.showSnackBarMessage(getString(R.string.error), ChatActivity.this);
            }
        });

        mTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ChatActivity.this, TrackOrderActivity.class).putExtra("shopModel", mShopModel).putExtra("request", mRequestModelDetails).putExtra("user", mUserModel));

            }
        });

        mConnectorCancelOrder = new Connector(this, new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                if (mProgressDialogCancelOrder.isShowing() && mProgressDialogCancelOrder != null)
                    mProgressDialogCancelOrder.dismiss();
                if (Connector.checkStatus(response)) {
                    mMessagesRecycler.setVisibility(View.INVISIBLE);
                    mSendParent.setVisibility(View.INVISIBLE);
                    mProgressBar.setVisibility(View.VISIBLE);
                    String url = Connector.createSendMessageUrl() + "?chat_id=" + mChatModel.getChatId() +
                            "&user_id=" + mUserModel.getId() + "&to_id=" + mChatModel.getToId() + "&type=text" + "&request_id=" + mRequestModel.getId();
                    Uri builder = Uri.parse(url)
                            .buildUpon()
                            .appendQueryParameter("message", Connector.getMessage(response)).build();


                    mConnectorSendMessage.getRequest(TAG, builder.toString());
                    if (mType.equals("done")) {
                        show();
                    } else {
                        finish();
                    }
                } else {
                    Helper.showSnackBarMessage(getString(R.string.error), ChatActivity.this);
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                if (mProgressDialogCancelOrder.isShowing() && mProgressDialogCancelOrder != null)
                    mProgressDialogCancelOrder.dismiss();
                Helper.showSnackBarMessage(getString(R.string.error), ChatActivity.this);
            }
        });

        mConnector = new Connector(this, new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                if (mProgressDialog.isShowing() && mProgressDialog != null && !ChatActivity.this.isFinishing())
                    mProgressDialog.dismiss();
                if (Connector.checkStatus(response)) {
                    mRequestModelDetails = Connector.getRequest(response, mShopModel);
                    mRequestModel = mRequestModelDetails;
                    if (mUserModel.getId() == null)
                        finish();

                    try {
                        if (mUserModel.getId().equals(mRequestModel.getDeliveryId())) {
                            mAdapter = new MessagesAdapter(ChatActivity.this, mMessageModels, new MessagesAdapter.OnItemClicked() {
                                @Override
                                public void setOnItemClicked(int position) {

                                }
                            }, mRequestModel.getUser());
                        } else if (mUserModel.getId().equals(mRequestModel.getUser_id())) {
                            mAdapter = new MessagesAdapter(ChatActivity.this, mMessageModels, new MessagesAdapter.OnItemClicked() {
                                @Override
                                public void setOnItemClicked(int position) {

                                }
                            }, mRequestModel.getDelivery());
                        } else {
                            mAdapter = new MessagesAdapter(ChatActivity.this, mMessageModels, new MessagesAdapter.OnItemClicked() {
                                @Override
                                public void setOnItemClicked(int position) {

                                }
                            }, mRequestModel.getUser());
                        }
                    } catch (Exception e) {
                        finish();
                    }
                    mMessagesRecycler.setAdapter(mAdapter);
                    mOrderId.setText(String.format("%s : %s", getString(R.string.order_id), mRequestModelDetails.getId()));
                    if (mRequestModel.getUser().getId().equals(mUserModel.getId())) {
                        mUsername.setText(mRequestModel.getDelivery().getName());
                    } else {
                        mUsername.setText(mRequestModel.getUser().getName());
                    }
                    mShopModel = mRequestModelDetails.getShop();
                    if (mShopModel != null) {
                        mStoreName.setText(mRequestModelDetails.getShop().getName());
                        if (URLUtil.isValidUrl(mShopModel.getImage()))
                            Picasso.get().load(mShopModel.getImage()).fit().centerCrop().into(mStoreImage);
                        else {
                            Picasso.get().load("http://www.as.cta3.com/waslk/prod_img/" + mShopModel.getImage()).fit().centerCrop().into(mStoreImage);
                        }
                    }
                    Crashlytics.setString("user", mUserModel.getId());
                    Crashlytics.setString("user_name", mUserModel.getUsername());
                    Crashlytics.setString("name", mUserModel.getName());
                    if (mUserModel.getId().equals(mRequestModelDetails.getUser_id())) {
                        mMessageModels.add(new MessageModel("", mRequestModelDetails.getDeliveryId(), mRequestModelDetails.getUser_id(), mRequestModelDetails.getCreated(), mRequestModelDetails.getDescription(), "text", true));
                        if (mChatModel != null)
                            mChatModel.setToId(mRequestModelDetails.getDeliveryId());
                    } else {
                        mMessageModels.add(new MessageModel("", mRequestModelDetails.getDeliveryId(), mRequestModelDetails.getUser_id(), mRequestModelDetails.getCreated(), mRequestModelDetails.getDescription(), "text", false));
                        if (mChatModel != null)
                            mChatModel.setToId(mRequestModelDetails.getUser_id());
                    }
                    mAdapter.notifyDataSetChanged();
                    if (!mRequestModelDetails.getDeliveryId().equals("0")) {
                        if (mRequestModelDetails.getDeliveryId().equals(mUserModel.getId())) {
                            if (mMenu != null) {
                                mMenu.getItem(0).setVisible(true);
                                mMenu.getItem(1).setVisible(true);
                                mMenu.getItem(2).setVisible(true);
                            }
                        } else {
                            if (mMenu != null) {
                                mMenu.getItem(0).setVisible(true);
                                mMenu.getItem(1).setVisible(true);
                                mMenu.getItem(2).setVisible(true);
                            }
                        }
                    } else {
                        if (mMenu != null) {
                            mMenu.getItem(0).setVisible(false);
                            mMenu.getItem(1).setVisible(false);
                            mMenu.getItem(2).setVisible(false);
                        }
                    }
                    if (mChatModel != null) {
                        mMessagesRecycler.setVisibility(View.INVISIBLE);
                        mSendParent.setVisibility(View.INVISIBLE);
                        mProgressBar.setVisibility(View.VISIBLE);
                        String url = "";
                        if (getIntent().getStringExtra("type").equals("admin")) {
                            url = Connector.createGetChatMessagesUrl() + "?user_id" + mUserModel.getId() + "&to_id=1&chat_id=0";
                        } else {
                            url = Connector.createGetChatMessagesUrl() + "?chat_id=" + mChatModel.getChatId() + "&request_id=" + mRequestModel.getId();
                        }
                        if (mAdapter != null)
                            mConnectorMessages.getRequest(TAG, url);
                    }
                } else {
                    finish();
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                finish();
            }
        });

        mConnectorMessages = new Connector(this, new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                mProgressDialog.dismiss();
                if (Connector.checkStatus(response)) {
                    mMessageModels.clear();
                    if (!getIntent().getStringExtra("type").equals("admin")) {
                        if (mUserModel.getId().equals(mRequestModel.getDeliveryId())) {
                            mAdapter.setFromUser(mRequestModel.getUser());
                        } else {
                            mAdapter.setFromUser(mRequestModel.getDelivery());
                        }
                        if (mUserModel.getId().equals(mRequestModel.getUser_id()))
                            mMessageModels.add(new MessageModel("", mRequestModel.getDeliveryId(), mRequestModel.getUser_id(), mRequestModel.getCreated(), mRequestModel.getDescription(), "text", true));
                        else
                            mMessageModels.add(new MessageModel("", mRequestModel.getDeliveryId(), mRequestModel.getUser_id(), mRequestModel.getCreated(), mRequestModel.getDescription(), "text", false));
                    } else {
                        mAdapter = new MessagesAdapter(ChatActivity.this, mMessageModels, new MessagesAdapter.OnItemClicked() {
                            @Override
                            public void setOnItemClicked(int position) {

                            }
                        }, new UserModel());
                        mMessagesRecycler.setAdapter(mAdapter);
                    }
                    mMessageModels.addAll(Connector.getChatMessagesJson(response, mUserModel));
                    mAdapter.notifyDataSetChanged();
                    mMessagesRecycler.scrollToPosition(mMessageModels.size() - 1);
                    mProgressBar.setVisibility(View.INVISIBLE);
                    mMessagesRecycler.setVisibility(View.VISIBLE);
                    mSendParent.setVisibility(View.VISIBLE);
                    scroll.post(new Runnable() {
                        public void run() {
                            scroll.fullScroll(View.FOCUS_DOWN);
                        }
                    });
                } else {
                    mMessagesRecycler.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.INVISIBLE);
                    mSendParent.setVisibility(View.VISIBLE);
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                mMessagesRecycler.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.INVISIBLE);
                mSendParent.setVisibility(View.VISIBLE);
                Helper.showSnackBarMessage(getString(R.string.error), ChatActivity.this);
            }
        });


        mSendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message = mMessageText.getText().toString();
                if (!Helper.validateFields(message)) {
                    Helper.showSnackBarMessage(getString(R.string.enter_message), ChatActivity.this);
                } else {
                    Helper.hideKeyboard(ChatActivity.this, v);
                    mMessagesRecycler.setVisibility(View.INVISIBLE);
                    mSendParent.setVisibility(View.INVISIBLE);
                    mProgressBar.setVisibility(View.VISIBLE);
                    String url = "";
                    Uri builder = null;
                    if (getIntent().getStringExtra("type").equals("admin")) {
                        url = Connector.createSendMessageUrl() + "?chat_id=0" +
                                "&user_id=" + mUserModel.getId() + "&to_id=1" + "&type=text&request_id=0";
                        builder = Uri.parse(url)
                                .buildUpon()
                                .appendQueryParameter("message", message).build();
                    } else {
                        url = Connector.createSendMessageUrl() + "?chat_id=" + mChatModel.getChatId() +
                                "&user_id=" + mUserModel.getId() + "&to_id=" + mChatModel.getToId() + "&type=text" + "&request_id=" + mRequestModel.getId();
                        builder = Uri.parse(url)
                                .buildUpon()
                                .appendQueryParameter("message", message).build();
                    }


                    mConnectorSendMessage.getRequest(TAG, builder.toString());
                }
            }
        });


        mConnectorSendMessage = new Connector(this, new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                mMessageText.setText("");
                String url = "";
                if (getIntent().getStringExtra("type").equals("admin")) {
                    url = Connector.createGetChatMessagesUrl() + "?user_id=" + mUserModel.getId() + "&to_id=1&chat_id=0&request_id=0";
                } else {
                    url = Connector.createGetChatMessagesUrl() + "?chat_id=" + mChatModel.getChatId() + "&request_id=" + mRequestModel.getId();
                }
                mConnectorMessages.getRequest(TAG, url);


                if (mAdapter != null)
                    mConnectorMessages.getRequest(TAG, url);
                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                DatabaseReference databaseReference = firebaseDatabase.getReference();
                String key = databaseReference.push().getKey();
                HashMap<String, String> set = new HashMap<>();
                set.put("messageId", key);
                if (key != null) {
                    if (getIntent().getStringExtra("type").equals("admin")) {
                        databaseReference.child("users/" + "1").child(key).setValue(set);
                    } else {
                        databaseReference.child("users/" + mChatModel.getToId()).child(key).setValue(set);
                    }

                }

            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                mMessagesRecycler.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.INVISIBLE);
                mSendParent.setVisibility(View.VISIBLE);
                Helper.showSnackBarMessage(getString(R.string.error), ChatActivity.this);
            }
        });

        mProgressDialog = Helper.showProgressDialog(this, getString(R.string.loading), false);
        if (getIntent().getStringExtra("type").equals("admin")) {
            mMessagesRecycler.setVisibility(View.INVISIBLE);
            mSendParent.setVisibility(View.INVISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);
            String url = "";
            if (getIntent().getStringExtra("type").equals("admin")) {
                url = Connector.createGetChatMessagesUrl() + "?user_id=" + mUserModel.getId() + "&to_id=1&chat_id=0";
            } else {
                url = Connector.createGetChatMessagesUrl() + "?chat_id=" + mChatModel.getChatId() + "&request_id=" + mRequestModel.getId();
            }
            mConnectorMessages.getRequest(TAG, url);
        } else {
            mConnector.getRequest(TAG, Connector.createGetRequestUrl() + "?id=" + mRequestModel.getId());
        }

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mReady.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(ChatActivity.this, ConfirmDeliveryActivity.class).putExtra("shopModel", mShopModel).putExtra("request", mRequestModelDetails).putExtra("user", mUserModel), 5);
            }
        });

        mUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomerDialog();
            }
        });

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mChatModel != null) {
                    mMessagesRecycler.setVisibility(View.INVISIBLE);
                    mSendParent.setVisibility(View.INVISIBLE);
                    mProgressBar.setVisibility(View.VISIBLE);
                    String url = Connector.createGetChatMessagesUrl() + "?chat_id=" + mChatModel.getChatId() + "&request_id=" + mRequestModel.getId();
                    if (mAdapter != null)
                        mConnectorMessages.getRequest(TAG, url);
                }

                if(getIntent().getStringExtra("type").equals("admin")) {
                    mMessagesRecycler.setVisibility(View.INVISIBLE);
                    mSendParent.setVisibility(View.INVISIBLE);
                    mProgressBar.setVisibility(View.VISIBLE);
                    String url = Connector.createGetChatMessagesUrl() + "?user_id=" + mUserModel.getId() + "&request_id=0&to_id=1";
                    mConnectorMessages.getRequest(TAG, url);
                }
            }
        });

        mSendPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users/" + mUserModel.getId());

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "Value is: " + dataSnapshot.getValue());
                if (mChatModel != null && dataSnapshot.getValue() != null && mRequestModel != null) {
                    String url = Connector.createGetChatMessagesUrl() + "?chat_id=" + mChatModel.getChatId() + "&request_id=" + mRequestModel.getId();
                    if (mAdapter != null)
                        mConnectorMessages.getRequest(TAG, url);
                }

                if (getIntent().getStringExtra("type").equals("admin")) {
                    String url = Connector.createGetChatMessagesUrl() + "?chat_id=0" + "&request_id=0" + "&user_id=" + mUserModel.getId() + "&to_id=1";
                    mConnectorMessages.getRequest(TAG, url);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            List<Image> images = ImagePicker.getImages(data);
            Image img = images.get(0);
            try {
                Bitmap bitmapImage = Helper.getBitmap(img.getPath(), 200);
                mSelectedFile = bitmapToFile(img.getName(), checkImage(img.getPath(), getBitmap(img.getPath())));
                UploadImage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (requestCode == 5) {
            if (resultCode == Activity.RESULT_OK)
                finish();
        }
    }

    private void UploadImage() {
        mProgressDialog = Helper.showProgressDialog(this,
                getString(R.string.uploading_photo),
                false);

        mProgressDialog.show();
        Ion.with(this)
                .load(Connector.createUploadImageUrl())
                .setMultipartFile("parameters[0]", "image", mSelectedFile)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        Helper.writeToLog(result);
                        if (Connector.checkImages(result)) {
                            if (mProgressDialog.isShowing() && mProgressDialog != null)
                                mProgressDialog.dismiss();
                            ArrayList<String> imagePaths = Connector.getImages(result);
                            mImage = imagePaths.get(0);
                            mMessagesRecycler.setVisibility(View.INVISIBLE);
                            mSendParent.setVisibility(View.INVISIBLE);
                            mProgressBar.setVisibility(View.VISIBLE);
                            String url = "";
                            Uri builder = null;
                            if (getIntent().getStringExtra("type").equals("admin")) {
                                url = Connector.createSendMessageUrl() + "?chat_id=0" +
                                        "&user_id=" + mUserModel.getId() + "&to_id=1" + "&type=image&request_id=0";
                                builder = Uri.parse(url)
                                        .buildUpon()
                                        .appendQueryParameter("message", mImage).build();
                            } else {
                                url = Connector.createSendMessageUrl() + "?chat_id=" + mChatModel.getChatId() +
                                        "&user_id=" + mUserModel.getId() + "&to_id=" + mChatModel.getToId() + "&type=image" + "&request_id=" + mRequestModel.getId();
                                builder = Uri.parse(url)
                                        .buildUpon()
                                        .appendQueryParameter("message", mImage).build();
                            }
                            mConnectorSendMessage.getRequest(TAG, builder.toString());
                        } else {
                            Helper.showSnackBarMessage(getString(R.string.error), ChatActivity.this);
                            if (mProgressDialog.isShowing() && mProgressDialog != null)
                                mProgressDialog.dismiss();
                        }
                    }
                });
    }

    private Bitmap getBitmap(String path) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(path, bmOptions);
        return getResizedBitmap(bitmap, 600);
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    private File bitmapToFile(String name, Bitmap bmap) throws IOException {
        if (ChatActivity.this.getExternalCacheDir() != null) {
            File f = new File(ChatActivity.this.getExternalCacheDir().getAbsolutePath() + "/" + name);
            boolean crated = f.createNewFile();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bmap.compress(Bitmap.CompressFormat.JPEG, 70, bos);
            byte[] bitmapData = bos.toByteArray();

            FileOutputStream fos = new FileOutputStream(f);
            fos.write(bitmapData);
            fos.flush();
            fos.close();
            return f;
        }
        return null;
    }

    private Bitmap checkImage(String path, Bitmap bitmap) throws IOException {

        ExifInterface ei = new ExifInterface(path);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);
        Bitmap rotatedBitmap = null;

        switch (orientation) {

            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap = rotateImage(bitmap, 90);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = rotateImage(bitmap, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = rotateImage(bitmap, 270);
                break;

            case ExifInterface.ORIENTATION_NORMAL:
            default:
                rotatedBitmap = bitmap;
        }


        return rotatedBitmap;
    }

    private void pickImage() {
        ImagePicker.create(this)
                .folderMode(true) // folder mode (false by default)
                .toolbarFolderTitle("Image Folder") // folder selection title
                .toolbarImageTitle("Select Image") // image selection title
                .single() //  Max images can be selected
                .showCamera(true) // show camera or not (true by default)
                .start(); // start image picker activity with Request code
    }


    public void showDeliveryDialog() {

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev).commit();
        }
        ft.addToBackStack(null);

        DeliveryDialogFragment deliveryDialogFragment = new DeliveryDialogFragment();


        deliveryDialogFragment.show(this.getSupportFragmentManager(), "dialog");

    }

    public void showCustomerDialog() {

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev).commit();
        }
        ft.addToBackStack(null);

        CustomerDialogFragment customerDialogFragment = new CustomerDialogFragment();
        Bundle bundle = new Bundle();
        if (mRequestModel.getUser().getId().equals(mUserModel.getId())) {
            bundle.putSerializable("user", mRequestModelDetails.getDelivery());
        } else {
            bundle.putSerializable("user", mRequestModelDetails.getUser());
        }

        customerDialogFragment.setArguments(bundle);

        customerDialogFragment.show(this.getSupportFragmentManager(), "dialog");

    }


    public void showRequestSettingsDialog() {

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev).commit();
        }
        ft.addToBackStack(null);

        RequestSettingsDialogFragment requestSettingsDialogFragment = new RequestSettingsDialogFragment();


        requestSettingsDialogFragment.show(this.getSupportFragmentManager(), "dialog");

    }

    public void showAlertDialog() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        AlertDialog dialog = builder.setMessage("طلبك تم ارساله بنجاح,سوف يقوم مندوبنا بارسال عروض التوصيل بعد قليل,وعليك اختيار العرض الانسب لك,الطلب لن يبدأ حتي تقوم بقبول احد العروض!\n\n الرجاء الانتظار")
                .setPositiveButton("حسنا", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                    }
                })
                .setNegativeButton("الغاء الطلب", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .show();
        TextView messageView = dialog.findViewById(android.R.id.message);
        if (messageView != null)
            messageView.setGravity(Gravity.CENTER);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mChatModel != null) {
            getMenuInflater().inflate(R.menu.chat_activity_menu, menu);
            mMenu = menu;
            return true;
        } else {
            return false;
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.order_canceled) {
            mProgressDialogCancelOrder = Helper.showProgressDialog(this, getString(R.string.loading), false);
            mConnectorCancelOrder.getRequest(TAG, "http://www.as.cta3.com/waslk/api/cancel_offer?price=" + mRequestModelDetails.getPrice() + "&id=" + mRequestModelDetails.getId() + "&delivery_id=" + mRequestModelDetails.getDeliveryId() + "&user_id=" + mRequestModelDetails.getUser_id());
            mType = "cancel";
            return true;
        } else if (id == R.id.done_order) {
            mProgressDialogCancelOrder = Helper.showProgressDialog(this, getString(R.string.loading), false);
            if (mRequestModelDetails.getDeliveryId().equals(mUserModel.getId()))
                mConnectorCancelOrder.getRequest(TAG, "http://www.as.cta3.com/waslk/api/complete_offer?price=" + mRequestModelDetails.getPrice() + "&id=" + mRequestModelDetails.getId() + "&delivery_id=" + mRequestModelDetails.getDeliveryId() + "&user_id=" + mRequestModelDetails.getUser_id() + "&delivery=true");
            else
                mConnectorCancelOrder.getRequest(TAG, "http://www.as.cta3.com/waslk/api/complete_offer?price=" + mRequestModelDetails.getPrice() + "&id=" + mRequestModelDetails.getId() + "&delivery_id=" + mRequestModelDetails.getDeliveryId() + "&user_id=" + mRequestModelDetails.getUser_id());
            mType = "done";
            return true;
        } else if (id == R.id.call) {
            if (!mRequestModelDetails.getDeliveryId().equals("0")) {
                if (mRequestModelDetails.getDeliveryId().equals(mUserModel.getId())) {
                    dialPhoneNumber(mRequestModelDetails.getUser().getMobile());
                } else {
                    dialPhoneNumber(mRequestModelDetails.getDelivery().getMobile());
                }
            }
            return true;
        } else if (id == R.id.track_order) {
            startActivity(new Intent(ChatActivity.this, TrackOrderActivity.class).putExtra("shopModel", mShopModel).putExtra("request", mRequestModelDetails).putExtra("user", mUserModel));
            return true;
        }
        return false;
    }

    private void show() {
        final android.app.AlertDialog.Builder dialogBuilder = new android.app.AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_rating, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        final RatingBar rating = dialogView.findViewById(R.id.rating_bar_2);
        final Button rate = dialogView.findViewById(R.id.btn_rate);
        final EditText comment = dialogView.findViewById(R.id.comment);
        rating.setIsIndicator(false);
        alertDialog = dialogBuilder.create();
        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
        alertDialog.show();
        rating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                mRatingNumber = rating;
            }
        });
        rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String commentText = comment.getText().toString();
                if (TextUtils.isEmpty(commentText)) {
                    Helper.showSnackBarMessage(getString(R.string.enter_comment), ChatActivity.this);
                } else {
                    if (mUserModel.getId().equals(mRequestModel.getDeliveryId())) {
                        mConnectorRate.getRequest(TAG, "http://www.as.cta3.com/waslk/api/add_comment?comment=" + Uri.encode(commentText) + "&rating=" + mRatingNumber + "&request_id=" + mRequestModel.getId() + "&delivery_id=" + mUserModel.getId());
                    } else {
                        mConnectorRate.getRequest(TAG, "http://www.as.cta3.com/waslk/api/add_comment?comment=" + Uri.encode(commentText) + "&rating=" + mRatingNumber + "&request_id=" + mRequestModel.getId() + "&user_id=" + mUserModel.getId());
                    }
                }
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        /*if (mChatModel != null) {
            String url = Connector.createGetChatMessagesUrl() + "?chat_id=" + mChatModel.getChatId() + "&request_id=" + mRequestModel.getId();
            mConnectorMessages.getRequest(TAG, url);
        }*/

    }

    public void dialPhoneNumber(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }


}
