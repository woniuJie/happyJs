package org.autojs.autojs.timing.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.FragmentActivity;

/**
 * Created by zhangshijie on 2019/11/22
 */
public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, AlarmRunningService.class);
        context.startService(i);

    }
}
