package com.qmentix.socketchatapp.model;

/**
 * Project SocketChatApp Created by Qmentix on 18-02-2017/0018.
 */

public class MessageModel {
    public static final int SERVER=1;
    public static final int CLIENT=0;
    String msg;
    int user;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getUser() {
        return user;
    }

    public void setUser(int user) {
        this.user = user;
    }
}
