package org.autojs.autojs.network.util;

import android.util.Log;

import org.autojs.autojs.App;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;


/**
 * 公共参数生成工厂
 * Created by wangjing on 2017/5/16.
 */
public class ParamsFactory {

    private static String KEY_SCRET = "9a55b9bd9c92de5010360c906fab751e";

    /**
     * 获取公共参数
     *
     * @return
     */
    public static Map<String, String> getCommonParams(Map<String, String> formParams) {

        formParams = getNotNullValueMap(formParams);

        Map<String, String> signParams = new TreeMap<>(
                (obj1, obj2) -> {
                    // 降序排序
                    return obj1.compareTo(obj2);
                });

        // Unix时间戳
        String timestamp = System.currentTimeMillis() + "";
        String imei = DeviceUtils.getImei(App.Companion.getApp().getApplicationContext());
        signParams.put("ts", timestamp);
        //设备ID
        signParams.put("fpi", imei);
        //基础参数
        signParams.putAll(formParams);


        Map<String,String> commonParams = new HashMap<>();

        commonParams.put("si", getSi(signParams));
        commonParams.put("ts", timestamp);
        commonParams.put("fpi", imei);

        return commonParams;
    }

    private static String getSi(Map<String, String> map) {
        StringBuilder si = new StringBuilder();
        for (String key : map.keySet()) {
            si.append(key).append(map.get(key));
        }
        Log.d("zsj", "getSi: " + si.toString());
        return MD5Utils.getMD5(si.toString() + KEY_SCRET).toLowerCase();

    }

    private static Map getNotNullValueMap(Map map) {
        if (map == null || map.size() == 0) {
            return map;
        }
        Iterator<String> iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Object value = map.get(key);
            if (value == null) {
                map.put(key, "");
            }
        }
        return map;
    }

}
