package com.zebra.jamesswinton.apntoggleservice;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import androidx.core.app.NotificationCompat;

public class ProfileSwitchMonitoringService extends Service implements APNToggleManager.OnApnToggledListener {

    // Debugging
    private static final String TAG = "MonitoringService";

    //
    private static final String CURRENT_APN_NOTIFICATION_CHANNEL_ID = "com.zebra.currentapn";
    private static final String FOREGROUND_SERVICE_NOTIFICATION_CHANNEL_ID = "com.zebra.apntoggle";

    private static final String FRIENDLY_APN_NOTIFICATION_CHANNEL_NAME = "Current Active APN";
    private static final String FRIENDLY_FOREGROUND_SERVICE_NOTIFICATION_CHANNEL_NAME = "APN Toggle Service";

    // Notification
    private static final int BACKGROUND_SERVICE_NOTIFICATION = 1;
    private static final String ACTION_STOP_SERVICE = "com.zebra.hudinterface.STOP_SERVICE";

    // Constants
    private static final String CUSTOM_PROFILE_NAME = "Profile0 (LastMile)";
    private static final String DW_ACTION = "com.symbol.datawedge.api.ACTION";
    private static final String APPLICATION_NAME_EXTRA = "com.symbol.datawedge.api.APPLICATION_NAME";
    private static final String NOTIFICATION_TYPE_EXTRA = "com.symbol.datawedge.api.NOTIFICATION_TYPE";
    public static final String NOTIFICATION_ACTION  = "com.symbol.datawedge.api.NOTIFICATION_ACTION";
    public static final String NOTIFICATION_TYPE_PROFILE_SWITCH = "PROFILE_SWITCH";
    private static final String REGISTER_FOR_NOTIFICATION_EXTRA = "com.symbol.datawedge.api.REGISTER_FOR_NOTIFICATION";

    // Variables
    private APNToggleManager.ApnType mCurrentApn = null;
    private APNToggleManager mApnToggleManager = null;

    // Loading Overlay
    private View mWindowOverlayView = null;
    private WindowManager mWindowManager = null;
    private WindowManager.LayoutParams mWindowManagerParams = null;

    // Notification
    NotificationManager mNotificationManager = null;

    @Override
    public void onCreate() {
        super.onCreate();

        // Init Window Manager
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            mWindowManagerParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        } else {
            mWindowManagerParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        }

        // Init Toggle Manager
        mApnToggleManager = new APNToggleManager(this);

        // Register for Notification
        Bundle profileSwitchBundle = new Bundle();
        profileSwitchBundle.putString(APPLICATION_NAME_EXTRA, getPackageName());
        profileSwitchBundle.putString(NOTIFICATION_TYPE_EXTRA, NOTIFICATION_TYPE_PROFILE_SWITCH);
        Intent registerProfileSwitchIntent = new Intent();
        registerProfileSwitchIntent.setAction(DW_ACTION);
        registerProfileSwitchIntent.putExtra(REGISTER_FOR_NOTIFICATION_EXTRA, profileSwitchBundle);
        sendBroadcast(registerProfileSwitchIntent);

        // Register Broadcast Receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(NOTIFICATION_ACTION);
        registerReceiver(mDataWedgeProfileSwitchReceiver, filter);

        // Start Service
        startForeground(BACKGROUND_SERVICE_NOTIFICATION, createServiceNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service Started");
        if (intent != null) {
            if (intent.getAction() != null) {
                if (intent.getAction().equals(ACTION_STOP_SERVICE)) {
                    stopSelf();
                    return START_NOT_STICKY;
                } else {
                    return START_STICKY;
                }
            }
            return START_STICKY;
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mDataWedgeProfileSwitchReceiver);
    }

    private Notification createServiceNotification() {

        // Create Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    FOREGROUND_SERVICE_NOTIFICATION_CHANNEL_ID,
                    FRIENDLY_FOREGROUND_SERVICE_NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);


            NotificationChannel currentApnChannel = new NotificationChannel(
                    CURRENT_APN_NOTIFICATION_CHANNEL_ID,
                    FRIENDLY_APN_NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            // Set Channel
            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (mNotificationManager != null) {
                mNotificationManager.createNotificationChannel(notificationChannel);
                mNotificationManager.createNotificationChannel(currentApnChannel);
            }
        }

        // Build Notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,
                FOREGROUND_SERVICE_NOTIFICATION_CHANNEL_ID);

        // Build StopService action
        Intent stopIntent = new Intent(this, ProfileSwitchMonitoringService.class);
        stopIntent.setAction(ACTION_STOP_SERVICE);
        PendingIntent stopPendingIntent = PendingIntent.getService(this,
                0, stopIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Action stopServiceAction = new NotificationCompat.Action(
                R.drawable.ic_toggle,
                "Stop Service",
                stopPendingIntent
        );

        // Return Build Notification object
        return notificationBuilder
                .setContentTitle("APNToggle Active")
                .setSmallIcon(R.drawable.ic_toggle)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setOngoing(true)
                .addAction(stopServiceAction)
                .build();
    }

    private BroadcastReceiver mDataWedgeProfileSwitchReceiver = new BroadcastReceiver() {

        // Constants
        private static final String PROFILE_NAME_EXTRA = "PROFILE_NAME";
        private static final String NOTIFICATION_EXTRA = "com.symbol.datawedge.api.NOTIFICATION";
        private static final String NOTIFICATION_TYPE_EXTRA = "NOTIFICATION_TYPE";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action != null && action.equals(NOTIFICATION_ACTION)){
                Bundle notificationBundle = intent.getBundleExtra(NOTIFICATION_EXTRA);
                if(notificationBundle != null) {
                    String notificationType  = notificationBundle.getString(NOTIFICATION_TYPE_EXTRA);
                    if (notificationType != null && notificationType.equals(NOTIFICATION_TYPE_PROFILE_SWITCH)) {
                        String profileName = notificationBundle.getString(PROFILE_NAME_EXTRA);

                        if (profileName != null) {
                            if (profileName.equals(CUSTOM_PROFILE_NAME)
                                    && (mCurrentApn == null || mCurrentApn != APNToggleManager.ApnType.MOBILEDADE)) {

                                displayOverlay();
                                mApnToggleManager.toggleApn(APNToggleManager.ApnType.MOBILEDADE, ProfileSwitchMonitoringService.this);
                            } else if (!profileName.equals(CUSTOM_PROFILE_NAME)
                                    && (mCurrentApn == null || mCurrentApn == APNToggleManager.ApnType.MOBILEDADE)) {

                                displayOverlay();
                                mApnToggleManager.toggleApn(APNToggleManager.ApnType.INTERNET,
                                        ProfileSwitchMonitoringService.this);
                            }
                        }
                    }
                }
            }
        }
    };

    @Override
    public void onApnToggled(APNToggleManager.ApnType apnType) {
        clearOverlay();
        this.mCurrentApn = apnType;
        Notification notification;
        if (mNotificationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notification = new NotificationCompat.Builder(this,
                        CURRENT_APN_NOTIFICATION_CHANNEL_ID)
                        .setContentTitle("APN Toggle Active")
                        .setContentText("Current APN: " + apnType.name())
                        .setSmallIcon(R.drawable.ic_toggle)
                        .build();
            } else {
                notification = new Notification.Builder(this)
                        .setContentTitle("APN Toggle Active")
                        .setContentText("Current APN: " + apnType.name())
                        .setSmallIcon(R.drawable.ic_toggle)
                        .build();
            }

            mNotificationManager.notify(BACKGROUND_SERVICE_NOTIFICATION, notification);
        }
    }

    @Override
    public void onApnToggleFailed() {
        clearOverlay();
    }

    private void clearOverlay() {
        // Remove View Overlay
        try{
            mWindowManager.removeView(mWindowOverlayView);
        }catch(IllegalArgumentException e){
            Log.e(TAG, "view not found");
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void displayOverlay() {
        // Remove View Overlay
        clearOverlay();

        // Build View
        Log.i(TAG, "Building WindowManager View...");
        mWindowOverlayView = LayoutInflater.from(this).inflate(R.layout.toggle_apn_overlay, null);

        // Show View
        Log.i(TAG, "Updating WindowManager View...");
        mWindowManager.addView(mWindowOverlayView, mWindowManagerParams);
    }

    /**
     * Unused
     */

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
