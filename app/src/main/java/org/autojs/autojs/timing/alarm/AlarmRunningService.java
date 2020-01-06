package org.autojs.autojs.timing.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by zhangshijie on 2019/11/22
 *
 * 定时任务，每十秒执行一次心跳监控
 */
public class AlarmRunningService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        EventBus.getDefault().post(new HeartBeatEvent());

        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 10000; // 10秒
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);

        return super.onStartCommand(intent, flags, startId);
    }
}
