package org.autojs.autojs.network.entity.task;

import java.io.Serializable;

/**
 * Created by zhangshijie on 2019/11/25
 */
public class ReportResponse implements Serializable {

    /**
     * code : 1
     * mess : 签名错误
     * data : {}
     */

    private int code;
    private String mess;
//    private DataBean data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMess() {
        return mess;
    }

    public void setMess(String mess) {
        this.mess = mess;
    }

//    public DataBean getData() {
//        return data;
//    }
//
//    public void setData(DataBean data) {
//        this.data = data;
//    }
//
//    public static class DataBean implements Serializable{
//        private static final long serialVersionUID = -5828806293195468917L;
//    }
}
