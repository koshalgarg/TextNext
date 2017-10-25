package com.textnext.koshal.asl;

import android.app.Activity;
import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.textnext.koshal.asl.DataBase.ASLContract;
import com.textnext.koshal.asl.DataObjects.Users;

/**
 * Created by me on 10/19/2017.
 */

public class ASL extends Application {

    public static  FirebaseDatabase mDatabase;
    public static StorageReference mStorage;

    public static  DatabaseReference mLoginReference;
    public static  DatabaseReference mOnlineReference;
    public static  DatabaseReference mConnectionReference;

    public static String mDeviceId;
    public static SharedPreferences mSharedPreference;
    public static SharedPreferences.Editor mSharedEditor ;
    public static String prefSex;
    public static Users mUser;
    public static Users mTuser;


    @Override
    public void onCreate() {
        super.onCreate();


        mDatabase = FirebaseDatabase.getInstance();
        //mDatabase.setPersistenceEnabled(true);
        mLoginReference = mDatabase.getReference().child("users");
        mOnlineReference = mDatabase.getReference().child("online");
        mConnectionReference = mDatabase.getReference().child("connections");
        mDeviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        mSharedPreference = getApplicationContext().getSharedPreferences(Constants.SHAREDPREFNAME, Context.MODE_PRIVATE);
        mSharedEditor = mSharedPreference.edit();


        mUser = Users.getDataFromSharedPref();


        mStorage = FirebaseStorage.getInstance().getReference();


        try{


            Picasso.Builder builder = new Picasso.Builder(this);
            builder.downloader(new OkHttpDownloader(this, Integer.MAX_VALUE));
            Picasso built = builder.build();
            built.setLoggingEnabled(true);
            Picasso.setSingletonInstance(built);


        }
        catch (Exception e){

        }



    }
}
