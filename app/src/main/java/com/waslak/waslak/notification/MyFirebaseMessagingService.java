package com.waslak.waslak.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.waslak.waslak.ChatActivity;
import com.waslak.waslak.HomeActivity;
import com.waslak.waslak.R;
import com.waslak.waslak.SplashActivity;
import com.waslak.waslak.utils.Helper;

import java.net.URI;
import java.util.Map;

import me.leolin.shortcutbadger.ShortcutBadger;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCM Service";
    private LocalBroadcastManager broadcaster;

    @Override
    public void onCreate() {
        broadcaster = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if (Helper.getNotificationSharedPreferences(this)) {

            Map<String, String> notificationMessage = remoteMessage.getData();

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "0");
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            String toneUri = preferences.getString("notification_tone", "null");
            Helper.writeToLog(toneUri);
            if (toneUri.equals("null")) {
                Helper.writeToLog("default ringtone");
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                final Ringtone ringtone = RingtoneManager.getRingtone(this, notification);
                if (ringtone != null)
                    ringtone.play();
                Thread th = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(6000);
                            if (ringtone != null)
                                if (ringtone.isPlaying())
                                    ringtone.stop();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
                th.start();
            } else {
                Helper.writeToLog("custom ringtone");
                final Ringtone ringtone = RingtoneManager.getRingtone(this, Uri.parse(toneUri));
                ringtone.play();
                Thread th = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(6000);
                            if (ringtone.isPlaying())
                                ringtone.stop();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
                th.start();
            }

            mBuilder.setSmallIcon(R.drawable.ic_notification_image);
            mBuilder.setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(notificationMessage.get("body")));
            mBuilder.setContentTitle(notificationMessage.get("title"));
            mBuilder.setContentText(notificationMessage.get("body"));
            mBuilder.setChannelId("0");
            mBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
            mBuilder.setDefaults(0);
            mBuilder.setSound(null);

            int notificationCount = Helper.getNotificationCount(this);
            notificationCount++;
            Helper.setNotificationCount(this, notificationCount);
            ShortcutBadger.applyCount(this, notificationCount);
            int color = ContextCompat.getColor(this, android.R.color.white);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mBuilder.setColor(color);
                mBuilder.setSmallIcon(R.drawable.ic_notification_image);
            } else {
                mBuilder.setSmallIcon(R.drawable.ic_notification_image);
            }
            Intent resultIntent = new Intent(this, SplashActivity.class);
            Helper.writeToLog(notificationMessage.get("target_screen"));
            if (notificationMessage.containsKey("target_screen")) {
                if (notificationMessage.get("target_screen").contains("admin")) {
                    resultIntent = new Intent(this,ChatActivity.class).putExtra("type","admin");
                } else if (notificationMessage.get("target_screen").contains("chat")) {
                    resultIntent = new Intent(this, ChatActivity.class).putExtra("goToChat", true).putExtra("chat_id", notificationMessage.get("chat_id")).putExtra("request_id", notificationMessage.get("request_id"));
                } else if (notificationMessage.get("target_screen").contains("request")) {
                    resultIntent = new Intent(this, ChatActivity.class).putExtra("goToRequest", true).putExtra("request_id", notificationMessage.get("request_id"));
                } else if (notificationMessage.get("target_screen").contains("offer")) {
                    resultIntent = new Intent(this, SplashActivity.class).putExtra("notification","1");
                } else {
                    resultIntent = new Intent(this, SplashActivity.class);
                }
            }
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(HomeActivity.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel mChannel = new NotificationChannel("0", "test", NotificationManager.IMPORTANCE_HIGH);
                mChannel.setSound(null, null);
                if (mNotificationManager != null) {
                    mNotificationManager.createNotificationChannel(mChannel);
                }
            }

            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);
            mBuilder.setAutoCancel(true);


            int random = (int) System.currentTimeMillis();
            if (mNotificationManager != null) {
                mNotificationManager.notify(random, mBuilder.build());
            }
        }

    }


    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        sendRegistrationToServer(s);
    }


    private void sendRegistrationToServer(String token) {
        // Add custom implementation, as needed.
        Helper.saveTokenToSharePreferences(this,token);
    }
}
