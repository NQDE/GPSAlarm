package a1stgroup.gpsalarm;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

/**
 * Created by student on 16.9.11.
 */

public class TrackerService extends IntentService {
    MapsActivity mapsActivity;
    NotificationManager mapNotificationManager;
    private static final int NOTIFICATION_ID = 1;

    public TrackerService() {
        super("TrackerService");
    }

    @Override
    public int onStartCommand(Intent intent,int flags, int startID) {
        mapNotificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent mapIntent = new Intent(this.getApplicationContext(), MapsActivity.class);

        PendingIntent contentIntent = PendingIntent.getActivity(this,0,
                new Intent(this, MapsActivity.class),0);

        Notification mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_light)
                .setContentTitle("Alarm manager")
                .setStyle(new NotificationCompat.BigTextStyle().bigText("AAAAAAAAAA"))
                .setContentIntent(contentIntent)
                .setContentText("AAAAAAAAAA")
                .build();

        mapNotificationManager.notify(0, mBuilder);
        return START_NOT_STICKY;
    }

    protected void onHandleIntent(Intent intent) {
        sendNotification("Tracking...");
        mapsActivity.trackLocation();
    }


    private void sendNotification(String msg) {
        mapNotificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent mapIntent = new Intent(this.getApplicationContext(), MapsActivity.class);

        PendingIntent contentIntent = PendingIntent.getActivity(this,0,
                new Intent(this, MapsActivity.class),0);

        Notification mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_light)
                .setContentTitle("Alarm manager")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setContentIntent(contentIntent)
                .setContentText(msg)
                .build();

        mapNotificationManager.notify(0, mBuilder);
    }

}
