package org.autojs.autojs.network.api;


import org.autojs.autojs.network.entity.task.BlankResponse;
import org.autojs.autojs.network.entity.task.HeartBeatResponse;
import org.autojs.autojs.network.entity.task.ReportResponse;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

/**
 * Created by zhangshijie on 2019/11/20
 *  此接口表示 自定义的接口请求
 */
public interface TaskApi {

    @FormUrlEncoded
    @POST("heart-beat")
    Observable<HeartBeatResponse> heartBeat(@QueryMap Map<String,String> map,@FieldMap Map<String,String> fieldMap);


    @FormUrlEncoded
    @POST("report")
    Observable<ReportResponse> report(@QueryMap Map<String,String> map,@FieldMap Map<String,String> fieldMap);

    @FormUrlEncoded
    @POST("sms")
    Observable<BlankResponse> sms(@QueryMap Map<String,String> map,@FieldMap Map<String,String> fieldMap);



}
