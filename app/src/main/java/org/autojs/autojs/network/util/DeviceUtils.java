package org.autojs.autojs.network.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import androidx.core.content.ContextCompat;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import static android.text.TextUtils.isEmpty;

/**
 * Created by zhangshijie on 2019/6/17;
 */
public class DeviceUtils {

    /**
     * 1.将imei号+手机硬件信息+androidID拼接成一个字符串
     * 2.再用MD5把以上信息处理成32位的字符串；
     *
     * @param context
     * @return
     */
    public static String getDeviceId(Context context) {
        StringBuilder deviceId = new StringBuilder();
        try {
            boolean hasPhoneStatePermission = ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
            if (hasPhoneStatePermission) {
                //1.IMEI（imei）
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                String imei = tm.getDeviceId();
                if (!isEmpty(imei)) {
                    deviceId.append(imei);
                }

                //2.android 设备信息（主要是硬件信息）
                String hardwareInfo = Build.ID + Build.DISPLAY + Build.PRODUCT
                        + Build.DEVICE + Build.BOARD /*+ Build.CPU_ABI*/
                        + Build.MANUFACTURER + Build.BRAND + Build.MODEL
                        + Build.BOOTLOADER + Build.HARDWARE /* + Build.SERIAL */
                        + Build.TYPE + Build.TAGS + Build.FINGERPRINT + Build.HOST
                        + Build.USER;
                if (!isEmpty(hardwareInfo)) {
                    deviceId.append(hardwareInfo);
                }

                //3.android id
                String androidId = Settings.Secure.getString(context.getContentResolver(),
                        Settings.Secure.ANDROID_ID);
                if (!isEmpty(androidId)) {
                    deviceId.append(androidId);
                }

                // 创建一个 messageDigest 实例
                MessageDigest msgDigest = null;
                try {
                    msgDigest = MessageDigest.getInstance("MD5");
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }

                //用 MessageDigest 将 deviceId 处理成32位的16进制字符串
                msgDigest.update(deviceId.toString().getBytes(), 0, deviceId.length());
                byte md5ArrayData[] = msgDigest.digest();

                String deviceUniqueId = new String();
                for (int i = 0; i < md5ArrayData.length; i++) {
                    int b = (0xFF & md5ArrayData[i]);
                    if (b <= 0xF) deviceUniqueId += "0";
                    deviceUniqueId += Integer.toHexString(b);
                }
                return deviceUniqueId;
            } else {
                //如果上面都没有， 则生成一个id：随机码
                String uuid = getUUID();
                if (!isEmpty(uuid)) {
                    deviceId.append(uuid);
                    return deviceId.toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        deviceId.append(getUUID());
        return deviceId.toString();
    }

    /**
     * 得到全局唯一UUID
     */
    public static String getUUID() {
        String uuid = UUID.randomUUID().toString();
        return uuid;
    }

    public static String getImei(Context context) {
        String imei = "";
        try {
            boolean hasPhoneStatePermission = ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
            if (hasPhoneStatePermission) {
                //1.IMEI（imei）
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                imei = tm.getDeviceId();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return imei;
    }
}
