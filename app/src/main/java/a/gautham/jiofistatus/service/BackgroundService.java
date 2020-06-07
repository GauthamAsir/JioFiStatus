package a.gautham.jiofistatus.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.Timer;
import java.util.TimerTask;

import a.gautham.jiofistatus.MainActivity;
import a.gautham.jiofistatus.R;
import a.gautham.jiofistatus.receiver.ActionReceiver;

public class BackgroundService extends Service {

    private Timer timer = new Timer();

    @Override
    public void onCreate() {
        super.onCreate();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                getData();
            }
        },0,10000);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void stopService(){
        stopSelf();
    }

    @Override
    public void onDestroy() {
        timer.cancel();
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("RestartService");
        broadcastIntent.setClass(this, Restarter.class);
        this.sendBroadcast(broadcastIntent);
    }

    void getData(){

        try {
            Document document = Jsoup.connect("http://jiofi.local.html/").get();
            int progress = Integer.parseInt(document.getElementById("batterylevel").val()
                    .replace("%",""));

            publishNotification(progress, null);
        } catch (Exception e){
            System.out.println("Error: " + e.toString());
            publishNotification(0, e.toString());
        }
    }

    private void publishNotification(int batteryLevel, String error) {

        // Make Channel if SDK is above or equal to Oreo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, getString(R.string.channel_id));

        builder.setOngoing(true);

        Intent actionIntent = new Intent(this, ActionReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, actionIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 12, intent, 0);

        builder
                .setSmallIcon(R.drawable.battery_alert)
                .setContentTitle("JioFi Battery Status")
                .setContentText(batteryLevel + "% left")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                .setContentIntent(contentIntent);

        if (error != null && !error.isEmpty()) {
            builder.setColor(ContextCompat.getColor(this, R.color.red));
            builder.setContentTitle("Error");
            builder.addAction(R.drawable.battery_alert, "Stop", pendingIntent);
            builder.setContentText("Not connected to Jio-Fi");
        } else if (batteryLevel <= 20) {
            builder.setColor(ContextCompat.getColor(this, R.color.red));
            builder.setContentTitle("Low Battery warning");
            builder.addAction(R.drawable.battery_alert, "Let's Charge it", pendingIntent);
            builder.setContentText("Battery running out " + batteryLevel + "% left");
        } else {
            builder.addAction(R.drawable.battery_alert, "Stop", pendingIntent);
        }

        Notification notification = builder.setCategory(Notification.CATEGORY_SERVICE).build();
        startForeground(12,notification);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {

        NotificationChannel channel =
                new NotificationChannel(getString(R.string.channel_id),
                        getString(R.string.app_name), NotificationManager.IMPORTANCE_LOW);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        assert notificationManager != null;
        notificationManager.createNotificationChannel(channel);
    }

}
