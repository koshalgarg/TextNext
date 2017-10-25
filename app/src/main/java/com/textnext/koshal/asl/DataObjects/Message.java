package com.textnext.koshal.asl.DataObjects;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

import com.textnext.koshal.asl.DataBase.ASLContract;

/**
 * Created by me on 10/20/2017.
 */

public class Message {

   private String msg,type,ts,uid,status,uri="";

    public Message() {
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTs() {
        return ts;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static Message fromCursor(Cursor cursor) {
        Message m=new Message();
        m.setStatus(cursor.getString(cursor.getColumnIndex(ASLContract.MessagesEntry.COL_STATUS)));
        m.setType(cursor.getString(cursor.getColumnIndex(ASLContract.MessagesEntry.COL_TYPE)));
        m.setTs(cursor.getString(cursor.getColumnIndex(ASLContract.MessagesEntry.COL_TS)));
        m.setMsg(cursor.getString(cursor.getColumnIndex(ASLContract.MessagesEntry.COL_MESSAGE)));
        m.setUid(cursor.getString(cursor.getColumnIndex(ASLContract.MessagesEntry.COL_USER_ID)));


        return m;
    }


}
