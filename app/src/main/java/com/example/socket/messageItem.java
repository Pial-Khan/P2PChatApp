package com.example.socket;

public class messageItem {
    String msg;
    String ip;

    public messageItem(String msg, String ip) {
        this.msg = msg;
        this.ip = ip;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
