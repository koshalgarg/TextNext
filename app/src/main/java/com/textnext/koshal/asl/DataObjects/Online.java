package com.textnext.koshal.asl.DataObjects;

/**
 * Created by me on 10/20/2017.
 */

public class Online {
    String sex;
    String ts;
    String priority="";


    public Online() {
    }

    public Online(String sex, String ts, String priority) {
        this.sex = sex;
        this.ts = ts;
        this.priority=priority;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getTs() {
        return ts;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }
}
