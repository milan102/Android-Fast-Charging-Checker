package me.milanisaweso.fastchargingchecker;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.SystemClock;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import static me.milanisaweso.fastchargingchecker.StringHelper.stringValueOf;

public class NotificationService extends NotificationListenerService {
    private BatteryManager batteryManager;
    private static NotificationService self = null;

    public NotificationService() {
        self = this;
    }

    public static NotificationService getNotificationService() {
        return self;
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public void onListenerConnected() {
        super.onListenerConnected();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getActiveNotifications();
            batteryManager = (BatteryManager) this.getSystemService(Context.BATTERY_SERVICE);
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification statusBarNotification) {
        if(isAndroidSystemNotification(statusBarNotification)) {
            StringBuilder stringBuilder = new StringBuilder();
            addNotificationToStringBuilder(statusBarNotification, stringBuilder);
            handleNotificationCheck(this, stringBuilder.toString().toLowerCase());
        }
    }

    public boolean isAndroidSystemNotification(StatusBarNotification statusBarNotification) {
        return  statusBarNotification.getPackageName().toLowerCase().contains("android");
    }

    public void addNotificationToStringBuilder(StatusBarNotification statusBarNotification,
                                               StringBuilder stringBuilder) {
        Notification notification = statusBarNotification.getNotification();
        stringBuilder.append(stringValueOf(notification.tickerText));
        stringBuilder.append(" ");
        stringBuilder.append(stringValueOf(notification.extras, Notification.EXTRA_TITLE));
        stringBuilder.append(" ");
        stringBuilder.append(stringValueOf(notification.extras, Notification.EXTRA_TEXT));
        stringBuilder.append(" ");
    }

    public void handleNotificationCheck(Context context, String notificationString) {
        if(notificationString.contains("fast charging") ||
                notificationString.contains("fast charger") ||
                notificationString.contains("fast-charging") ||
                notificationString.contains("fast-charger")) {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            final Ringtone r = RingtoneManager.getRingtone(context.getApplicationContext(), notification);
            r.play();
            SystemClock.sleep(500);
            r.stop();
        }
    }

    public String getAndroidSystemNotifications() {
        StatusBarNotification[] statusBarNotifications = getActiveNotifications();
        StringBuilder stringBuilder = new StringBuilder();

        for(StatusBarNotification notification : statusBarNotifications) {
            if(isAndroidSystemNotification(notification)) {
                addNotificationToStringBuilder(notification, stringBuilder);
            }
        }
        return stringBuilder.toString();
    }

    @Override
    @TargetApi(Build.VERSION_CODES.N)
    public void onListenerDisconnected() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            requestRebind(new ComponentName(this, NotificationListenerService.class));
        }
    }
}
