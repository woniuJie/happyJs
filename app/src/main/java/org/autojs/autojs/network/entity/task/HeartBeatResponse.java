package org.autojs.autojs.network.entity.task;

import java.io.Serializable;

/**
 * Created by zhangshijie on 2019/11/20
 */
public class HeartBeatResponse implements Serializable{

    private static final long serialVersionUID = 962034204229442170L;
    /**
     * code : 0
     * mess :
     * data : {"js_text":"http://qk.api.ztxwd.com/index.js"}
     */

    private int code;
    private String mess;
    private DataBean data;

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

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean implements Serializable {
        private static final long serialVersionUID = 8940123581372169154L;
        /**
         * js_url : http://qk.api.ztxwd.com/index.js
         */

        private String js_url;

        public String getJs_url() {
            return js_url;
        }

        public void setJs_url(String js_url) {
            this.js_url = js_url;
        }
    }
}
