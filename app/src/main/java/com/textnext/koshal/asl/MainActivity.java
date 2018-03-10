package com.textnext.koshal.asl;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Vibrator;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.textnext.koshal.asl.DataObjects.Feedback;
import com.textnext.koshal.asl.DataObjects.Message;
import com.textnext.koshal.asl.DataObjects.Online;
import com.textnext.koshal.asl.DataObjects.Connection;
import com.textnext.koshal.asl.DataObjects.Users;


import java.util.ArrayList;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements RewardedVideoAdListener {

    Button mStart, mStartSpecial;
    int onlineCount = 0;
    TextView mOnline;

    private AdView mAdView;
    AdRequest adRequest;
    private RewardedVideoAd mAd;
    RelativeLayout hintLayout;

    ProgressDialog mProgressDialog;
    Vibrator v;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();
    }

    private void initialize() {
        v = (Vibrator) MainActivity.this.getSystemService(Context.VIBRATOR_SERVICE);


        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                ASL.mOnlineReference.child(ASL.mDeviceId).removeValue();
            }
        });

        hintLayout = (RelativeLayout) findViewById(R.id.hint_layout);
        if (ASLUtils.getShared(Constants.HINT_DISPLAYED).equals("")) {
            hintLayout.setVisibility(View.VISIBLE);
            ASLUtils.setShared(Constants.HINT_DISPLAYED, "1");
        }

        mAdView = (AdView) findViewById(R.id.adView);
        adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mAd = MobileAds.getRewardedVideoAdInstance(this);
        mAd.setRewardedVideoAdListener(this);

        loadVideo();


        mStart = (Button) findViewById(R.id.start);
        mStartSpecial = (Button) findViewById(R.id.startSpecial);
        mOnline = (TextView) findViewById(R.id.online);


        if (ASL.mUser.getIsAdmin() != null && ASL.mUser.getIsAdmin().equals("1")) {


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

                mProgressDialog.setMessage("Searching");
                mProgressDialog.show();
                enqueUser(0);

                final DatabaseReference mOnlineReference= ASL.mDatabase.getReference().child("online");


                final ChildEventListener childListener=new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        if(!dataSnapshot.getKey().equals(ASL.mDeviceId)) {
                            Online online = dataSnapshot.getValue(Online.class);

                            String p_priority=online.getPriority();
                            if(p_priority.equals(ASL.mUser.getSex()) || p_priority.equals(""))
                            {
                                establishConnection(dataSnapshot.getKey(), dataSnapshot, 1);
                            }
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                };


                mOnlineReference.addChildEventListener(childListener);

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

        if (coin == 0)
            ASL.mOnlineReference.child(ASL.mDeviceId).setValue(new Online(ASL.mUser.getSex(), String.valueOf(System.currentTimeMillis()), ""));
        else
            ASL.mOnlineReference.child(ASL.mDeviceId).setValue(new Online(ASL.mUser.getSex(), String.valueOf(System.currentTimeMillis()), ASL.mUser.getPriority()));


        ASL.mDatabase.getReference().child("users").child(ASL.mDeviceId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ASL.mUser = dataSnapshot.getValue(Users.class);

                if (ASL.mUser.getStatus().equals("1")) {

                    mProgressDialog.dismiss();
                    v.vibrate(200);
                    ASLUtils.setShared(Constants.CID, ASL.mUser.getCid());
                    ASLUtils.deleteAllMessage(MainActivity.this);
                    Intent i = new Intent(MainActivity.this, ChatActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    finish();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


    private void establishConnection(String uid, DataSnapshot dataSnapshot, int coin) {


        if (uid.equals(ASL.mDeviceId)) {
            enqueUser(coin);
            return;
        }

        v.vibrate(200);
        mProgressDialog.dismiss();
        dataSnapshot.getRef().removeValue();

        ASLUtils.setShared(Constants.P_UID, uid);

        Connection connection = new Connection(uid, ASL.mDeviceId, String.valueOf(System.currentTimeMillis()), "1");
        DatabaseReference temp = ASL.mConnectionReference.push().getRef();
        String cid = temp.getKey();
        temp.setValue(connection);
        ASLUtils.setShared(Constants.CID, cid);

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
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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

        mProgressDialog.setMessage("Searching");
        mProgressDialog.show();

        ASL.mOnlineReference.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {


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

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.feedback: {
                feedbackDialog();
                break;
            }

            case R.id.logout: {
                logout();
                break;
            }

        }
        return true;
    }


    private void feedbackDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this);
        View view = LayoutInflater.from(this).inflate(R.layout.feedback_form_layout, null);
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();

        final EditText text = (EditText) view.findViewById(R.id.feedbackText);
        TextView send = (TextView) view.findViewById(R.id.send);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (text.getText().toString().length() > 0) {

                    Feedback feed = new Feedback();
                    feed.setFeedback(text.getText().toString().trim());
                    feed.setUid(ASL.mDeviceId);
                    feed.setTs(String.valueOf(System.currentTimeMillis()));
                    ASL.mDatabase.getReference().child("feedback").push().setValue(feed);
                    bottomSheetDialog.dismiss();


                } else {
                }
            }
        });

    }


    private void logout() {

        ASLUtils.setShared(Constants.LOGIN_STAUS, "");
        ASLUtils.setShared(Constants.CID, "");
        ASLUtils.setShared(Constants.P_UID, "");


        Intent i = new Intent(MainActivity.this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }

    @Override
    protected void onPause() {

        if(mProgressDialog!=null)
            mProgressDialog.dismiss();

        ASL.mOnlineReference.child(ASL.mDeviceId).removeValue();
        super.onPause();
    }
}
