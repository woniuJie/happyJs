package org.autojs.autojs.external.receiver;

import java.io.Serializable;

/**
 * Created by zhangshijie on 2019/11/26
 */
public class SmsEvent implements Serializable {

    private static final long serialVersionUID = 3013994555935170204L;

    public SmsEvent() {
    }

    private String sender;
    private String content;
    private long time;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
