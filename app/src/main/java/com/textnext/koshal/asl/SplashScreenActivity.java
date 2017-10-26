package com.textnext.koshal.asl;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.ads.MobileAds;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.textnext.koshal.asl.DataBase.ASLDBHelper;
import com.textnext.koshal.asl.DataObjects.Users;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        MobileAds.initialize(this, Constants.ADD_ID);

        ASLDBHelper dbHelper = new ASLDBHelper(this);
        dbHelper.getWritableDatabase();

        Intent i;
        if(!ASLUtils.getShared(Constants.LOGIN_STAUS).equals("1"))
        {
            i=new Intent(SplashScreenActivity.this,LoginActivity.class);
        }
        else{
            if(ASLUtils.getShared(Constants.CID).equals(""))
            {
                i=new Intent(SplashScreenActivity.this, MainActivity.class);
            }
            else{
                i=new Intent(SplashScreenActivity.this,ChatActivity.class);
            }
        }
        startActivity(i);
        finish();

        ASL.mLoginReference.child(ASL.mDeviceId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ASL.mUser=dataSnapshot.getValue(Users.class);
                if(ASL.mUser==null && ASLUtils.getShared(Constants.LOGIN_STAUS).equals("1"))
                {
                    ASLUtils.setShared(Constants.LOGIN_STAUS,"0");
                    ASLUtils.setShared(Constants.CID,"");
                    Intent i=new Intent(SplashScreenActivity.this,LoginActivity.class);
                    startActivity(i);
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
