package com.textnext.koshal.asl.DataObjects;

/**
 * Created by me on 10/20/2017.
 */

public class Connection {

    String user1;
    String user2;
    String ts;
    String status;

    public Connection() {
    }

    public Connection(String user1, String user2, String ts, String status) {
        this.user1 = user1;
        this.user2 = user2;
        this.ts = ts;
        this.status = status;
    }

    public String getUser1() {
        return user1;
    }

    public void setUser1(String user1) {
        this.user1 = user1;
    }

    public String getUser2() {
        return user2;
    }

    public void setUser2(String user2) {
        this.user2 = user2;
    }

    public String getTs() {
        return ts;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


}
