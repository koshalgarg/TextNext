package com.textnext.koshal.asl.DataObjects;

import com.textnext.koshal.asl.ASLUtils;
import com.textnext.koshal.asl.Constants;

/**
 * Created by me on 10/19/2017.
 */

public class Users {
    private String age,sex,location,cid,ts="0",status="0",priority,online="0",isAdmin="0";

    public Users(String age, String sex, String location, String cid, String ts, String status, String priority,String online) {
        this.age = age;
        this.sex = sex;
        this.location = location;
        this.cid = cid;
        this.ts = ts;
        this.status = status;
        this.priority = priority;
        this.online=online;
    }

    public String getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(String isAdmin) {
        this.isAdmin = isAdmin;
    }

    public Users() {
    }

   public static Users getDataFromSharedPref() {

        Users user=new Users();
        user.setAge(ASLUtils.getShared(Constants.AGE));
        user.setLocation(ASLUtils.getShared(Constants.LOCATION));
        user.setCid(ASLUtils.getShared(Constants.CID));
        user.setSex(ASLUtils.getShared(Constants.SEX));
        user.setTs(String.valueOf(System.currentTimeMillis()));
        user.setStatus(user.getCid().equals("")?"0":"1");
        user.setOnline("0");
        user.setPriority(ASLUtils.getShared(Constants.PRIORITY));


        return user;
    }

    public static void setSharePref(Users user) {

        ASLUtils.setShared(Constants.SEX,user.getSex());
        ASLUtils.setShared(Constants.AGE,user.getAge());
        ASLUtils.setShared(Constants.LOCATION,user.getLocation());
        ASLUtils.setShared(Constants.PRIORITY,user.getPriority());

    }

    public String getOnline() {
        return online;
    }

    public void setOnline(String online) {
        this.online = online;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
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

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }
}
