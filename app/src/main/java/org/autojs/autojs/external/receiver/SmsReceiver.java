package org.autojs.autojs.external.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;

/**
 * Created by zhangshijie on 2019/8/22;
 */
public class SmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        SmsMessage[] smsMessages;
        Object[] pdus = null;
        if (bundle != null) {
            pdus = (Object[]) bundle.get("pdus");
        }
        if (pdus != null) {
            smsMessages = new SmsMessage[pdus.length];
            String sender;
            String content;
            long time;

            for (int i = 0; i < pdus.length; i++) {
                smsMessages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                sender = smsMessages[i].getOriginatingAddress(); // 获取短信的发送者
                content = smsMessages[i].getMessageBody(); // 获取短信的内容
                time = smsMessages[i].getTimestampMillis();//时间

                if (!StringUtils.isEmpty(content)) {

                    SmsEvent smsEvent = new SmsEvent();
                    smsEvent.setSender(sender);
                    smsEvent.setContent(content);
                    smsEvent.setTime(time);

                    EventBus.getDefault().post(smsEvent);
                }
            }
        }
    }

}
