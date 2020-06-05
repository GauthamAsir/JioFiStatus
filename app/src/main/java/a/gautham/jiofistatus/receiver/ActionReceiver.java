package a.gautham.jiofistatus.receiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import a.gautham.jiofistatus.service.BackgroundService;

public class ActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent==null)
            return;

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            notificationManager.cancel(1);
            android.os.Process.killProcess(android.os.Process.myPid());
            context.stopService(new Intent(context, BackgroundService.class));
        }
    }

}