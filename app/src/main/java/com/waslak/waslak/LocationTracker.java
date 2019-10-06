package com.waslak.waslak;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.volley.VolleyError;
import com.waslak.waslak.networkUtils.Connector;
import com.waslak.waslak.networkUtils.Constants;
import com.waslak.waslak.utils.Helper;

public class LocationTracker extends Service implements LocationListener {

    private static final String TAG = "LocationTracker";

    private Context mContext = this;

    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    // flag for GPS status
    boolean canGetLocation = false;

    Location location; // location
    double latitude; // latitude
    double longitude; // longitude
    Connector mUpdateAddress;

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000;

    // Declaring a Location Manager
    protected LocationManager locationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        buildNotification();
        mUpdateAddress = new Connector(this, new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {

            }
        });
        getLocation();
        if (canGetLocation() && getLatitude() != 0) {
            Helper.writeToLog("Update : " + getLatitude() + " " + getLongitude());
            mUpdateAddress.getRequest(TAG, Constants.WASLAK_BASE_URL + "/mobile/api/update_address?longitude=" + getLatitude() + "&latitude=" + getLongitude() + "&id=" + Helper.getUserSharedPreferences(this).getId());
        }
    }

    public LocationTracker() {
    }

    public LocationTracker(Context context) {
        this.mContext = context;
        getLocation();
    }


    private void buildNotification() {

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel("0", "test", NotificationManager.IMPORTANCE_HIGH);
            mChannel.setSound(null, null);
            if (mNotificationManager != null) {
                mNotificationManager.createNotificationChannel(mChannel);
            }
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, new Intent(LocationTracker.this, SplashActivity.class), 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "0")
                .setContentTitle(this.getResources().getString(R.string.app_name))
                .setContentText("Tracking")
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setVibrate(new long[]{0L})
                .setShowWhen(false)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis());

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId("0"); // Channel ID
        }

        startForeground(1234, builder.build());

    }

    public Location getLocation() {
        try {
            locationManager = (LocationManager) mContext
                    .getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("Network", "Network");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }


    public void stopUsingGPS() {
        if (locationManager != null) {
            locationManager.removeUpdates(LocationTracker.this);
        }
    }


    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }

        // return latitude
        return latitude;
    }


    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }

        // return longitude
        return longitude;
    }


    public boolean canGetLocation() {
        return this.canGetLocation;
    }


    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
                dialog.cancel();
            }
        });


        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    @Override
    public void onLocationChanged(Location location) {
        Helper.writeToLog("Update : " + location.getLatitude() + " " + location.getLongitude());
        mUpdateAddress.getRequest(TAG, Constants.WASLAK_BASE_URL + "/mobile/api/update_address?longitude=" + location.getLatitude() + "&latitude=" + location.getLongitude() + "&id=" + Helper.getUserSharedPreferences(LocationTracker.this).getId());
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

}


//package com.waslak.waslak;
//
//import android.Manifest;
//import android.app.Notification;
//import android.app.PendingIntent;
//import android.app.Service;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.os.Build;
//import android.os.IBinder;
//import android.support.annotation.Nullable;
//import android.support.v4.app.NotificationCompat;
//import android.support.v4.content.ContextCompat;
//
//import com.android.volley.VolleyError;
//import com.google.android.gms.location.FusedLocationProviderClient;
//import com.google.android.gms.location.LocationCallback;
//import com.google.android.gms.location.LocationRequest;
//import com.google.android.gms.location.LocationResult;
//import com.google.android.gms.location.LocationServices;
//import com.waslak.waslak.networkUtils.Connector;
//import com.waslak.waslak.networkUtils.Constants;
//import com.waslak.waslak.utils.Helper;
//
//public class LocationTracker extends Service {
//
//    Connector mUpdateAddress;
//
//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//
//
//    private static final String TAG = LocationTracker.class.getSimpleName();
//
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        mUpdateAddress = new Connector(this, new Connector.LoadCallback() {
//            @Override
//            public void onComplete(String tag, String response) {
//            }
//        }, new Connector.ErrorCallback() {
//            @Override
//            public void onError(VolleyError error) {
//
//            }
//        });
//        buildNotification();
//        requestLocationUpdates();
//    }
//
////Create the persistent notification//
//
//    private void buildNotification() {
//
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, new Intent(LocationTracker.this, SplashActivity.class), 0);
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "0")
//                .setContentTitle(this.getResources().getString(R.string.app_name))
//                .setContentText("Tracking")
//                .setOngoing(true)
//                .setOnlyAlertOnce(true)
//                .setVibrate(new long[]{0L})
//                .setShowWhen(false)
//                .setPriority(Notification.PRIORITY_HIGH)
//                .setSmallIcon(R.mipmap.ic_launcher)
//                .setContentIntent(pendingIntent)
//                .setWhen(System.currentTimeMillis());
//
//        // Set the Channel ID for Android O.
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            builder.setChannelId("0"); // Channel ID
//        }
//
//        startForeground(1234,builder.build());
//
//    }
//
//
//    //Initiate the request to track the device's location//
//
//    private void requestLocationUpdates() {
//        LocationRequest request = new LocationRequest();
//
//        //Specify how often your app should request the device’s location//
//
//        request.setInterval(1000);
//
//        //Get the most accurate location data available//
//
//        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
//        int permission = ContextCompat.checkSelfPermission(this,
//                Manifest.permission.ACCESS_FINE_LOCATION);
//
//        //If the app currently has access to the location permission...//
//
//        if (permission == PackageManager.PERMISSION_GRANTED) {
//            //...then request location updates//
//
//            client.requestLocationUpdates(request, new LocationCallback() {
//                @Override
//                public void onLocationResult(LocationResult locationResult) {
//
//                    Helper.writeToLog("Update : " + locationResult.getLastLocation().getLatitude() + " " + locationResult.getLastLocation().getLongitude());
//                    mUpdateAddress.getRequest(TAG, Constants.WASLAK_BASE_URL + "/mobile/api/update_address?longitude=" + locationResult.getLastLocation().getLatitude() + "&latitude=" + locationResult.getLastLocation().getLongitude() + "&id=" + Helper.getUserSharedPreferences(LocationTracker.this).getId());
//
//
//                }
//            }, null);
//        }
//    }
//}
