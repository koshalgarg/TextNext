package com.textnext.koshal.asl;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.textnext.koshal.asl.DataObjects.Online;
import com.textnext.koshal.asl.DataObjects.Connection;
import com.textnext.koshal.asl.DataObjects.Users;


import java.util.Map;

public class MainActivity extends AppCompatActivity implements RewardedVideoAdListener {

    Button mStart, mStartSpecial;
    int onlineCount = 0;
    TextView mOnline;

    private AdView mAdView;
    private RewardedVideoAd mAd;
    RelativeLayout hintLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();
    }

    private void initialize() {


        hintLayout = (RelativeLayout) findViewById(R.id.hint_layout);
        if (ASLUtils.getShared(Constants.HINT_DISPLAYED).equals("")) {
            hintLayout.setVisibility(View.VISIBLE);
            ASLUtils.setShared(Constants.HINT_DISPLAYED, "1");
        }

        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mAd = MobileAds.getRewardedVideoAdInstance(this);
        mAd.setRewardedVideoAdListener(this);

        loadVideo();


        mStart = (Button) findViewById(R.id.start);
        mStartSpecial = (Button) findViewById(R.id.startSpecial);
        mOnline = (TextView) findViewById(R.id.online);


        if(ASL.mUser.getIsAdmin()!=null && ASL.mUser.getIsAdmin().equals("1")) {


            ASL.mOnlineReference.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    onlineCount++;

                    if (onlineCount == 0)
                        mOnline.setText("");
                    else if (onlineCount == 1) {
                        mOnline.setText("1 User Online");
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    onlineCount--;
                    mOnline.setText(onlineCount + " users online");

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }


        mStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ASL.mOnlineReference.orderByChild("ts").limitToFirst(3).addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {
                            for (DataSnapshot d : dataSnapshot.getChildren()) {
                                Online online = d.getValue(Online.class);

                                if (online.getPriority().length() == 0 || online.getPriority().equals(ASL.mUser.getSex())) {
                                    establishConnection(d.getKey(), dataSnapshot, 0);
                                    return;
                                }
                            }
                            enqueUser(0);
                        } else {
                            enqueUser(0);
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }
        });


        mStartSpecial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAd.isLoaded()) {
                    mAd.show();
                }

            }
        });


    }

    private void loadVideo() {
        mAd.loadAd("ca-app-pub-6690454024464967/2098707744", new AdRequest.Builder().build());
    }

    private void enqueUser(int coin) {


        ASLUtils.deleteAllMessage(MainActivity.this);
        if (coin == 0)
            ASL.mOnlineReference.child(ASL.mDeviceId).setValue(new Online(ASL.mUser.getSex(), String.valueOf(System.currentTimeMillis()), ""));
        else
            ASL.mOnlineReference.child(ASL.mDeviceId).setValue(new Online(ASL.mUser.getSex(), String.valueOf(System.currentTimeMillis()), ASL.mUser.getPriority()));

        Intent i = new Intent(MainActivity.this, ChatActivity.class);
       // i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
       // finish();
    }


    private void establishConnection(String uid, DataSnapshot dataSnapshot,  int coin) {

        if (uid.equals(ASL.mDeviceId)) {
            enqueUser(coin);
            return;
        }

        dataSnapshot.getRef().removeValue();

        ASLUtils.setShared(Constants.P_UID, uid);

        Connection connection = new Connection(uid, ASL.mDeviceId, String.valueOf(System.currentTimeMillis()), "1");
        DatabaseReference temp = ASL.mConnectionReference.push().getRef();
        String cid = temp.getKey();
        ASLUtils.setShared(Constants.CID, cid);
        temp.setValue(connection);
        DatabaseReference d = ASL.mLoginReference.child(ASL.mDeviceId);
        d.child("cid").setValue(cid);
        d.child("status").setValue("1");
        d.child("online").setValue("Online");

        ASL.mUser.setCid(cid);
        ASL.mUser.setStatus("1");
        ASL.mUser.setOnline("Online");

        DatabaseReference d2 = ASL.mLoginReference.child(uid).getRef();
        d2.child("cid").setValue(cid);
        d2.child("status").setValue("1");



        ASLUtils.deleteAllMessage(MainActivity.this);

        Intent i = new Intent(MainActivity.this, ChatActivity.class);
       // i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }


    @Override
    public void onRewardedVideoAdLoaded() {
        mStartSpecial.setAlpha(1);
    }

    @Override
    public void onRewardedVideoAdOpened() {


    }

    @Override
    public void onRewardedVideoStarted() {

    }

    @Override
    public void onRewardedVideoAdClosed() {
        loadVideo();
        mStartSpecial.setAlpha((float) 0.75);

    }

    @Override
    public void onRewarded(RewardItem rewardItem) {
        startPrioritySearch();

    }


    @Override
    public void onRewardedVideoAdLeftApplication() {

    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {
        loadVideo();
        mStartSpecial.setAlpha((float) 0.75);
    }


    private void startPrioritySearch() {

        ASL.mOnlineReference.orderByChild("ts").limitToLast(10).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                if (dataSnapshot.exists()) {
                    for (DataSnapshot d : dataSnapshot.getChildren()) {
                        Online online = d.getValue(Online.class);
                        if (online.getSex().equals(ASL.mUser.getPriority())) {
                            if (online.getPriority().equals(ASL.mUser.getSex()) || online.getPriority().equals("")) {
                                establishConnection(d.getKey(), d, 1);
                                return;
                            }
                        }
                    }
                    enqueUser(1);
                } else {
                    enqueUser(1);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

}
