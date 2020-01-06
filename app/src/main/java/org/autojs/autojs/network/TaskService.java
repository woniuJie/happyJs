package org.autojs.autojs.network;

import org.autojs.autojs.network.api.TaskApi;
import org.autojs.autojs.network.entity.task.BlankResponse;
import org.autojs.autojs.network.entity.task.HeartBeatResponse;
import org.autojs.autojs.network.entity.task.ReportResponse;


import java.util.Map;

import io.reactivex.Observable;
import retrofit2.Retrofit;

/**
 * Created by zhangshijie on 2019/11/20
 */
public class TaskService {
    private static final TaskService mInstance = new TaskService();
    private Retrofit mRetrofit;
    private
    TaskService(){
        mRetrofit = TaskNet.getInstance().getRetrofit();
    }

    public static TaskService getInstance(){
        return mInstance;
    }

    public Observable<HeartBeatResponse> getHeartBeat(Map<String,String> map,Map<String,String> filedMap){
        return mRetrofit.create(TaskApi.class)
                .heartBeat(map,filedMap);
    }

    public Observable<ReportResponse> getReport(Map<String,String> map,Map<String,String> filedMap){
        return mRetrofit.create(TaskApi.class)
                .report(map,filedMap);
    }

    public Observable<BlankResponse> sms(Map<String,String> map,Map<String,String> fieldMap){
        return mRetrofit.create(TaskApi.class)
                .sms(map,fieldMap);
    }
}
