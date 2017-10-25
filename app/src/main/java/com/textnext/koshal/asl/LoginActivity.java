package com.textnext.koshal.asl;

import android.app.ProgressDialog;
import android.content.Intent;
import android.provider.Settings;
import android.provider.SyncStateContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.textnext.koshal.asl.DataObjects.Users;

public class LoginActivity extends AppCompatActivity {

    EditText mAge,mLocation;
    ImageView mMale,mFemale;
    Button mLogin;

    String gender="";
    ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAge= (EditText) findViewById(R.id.age);
        mMale= (ImageView) findViewById(R.id.male);
        mFemale= (ImageView) findViewById(R.id.female);
        mLocation= (EditText) findViewById(R.id.location);
        mLogin= (Button) findViewById(R.id.login);
        mProgress=new ProgressDialog(this);


        mMale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gender="M";
                mMale.setAlpha((float) 1);
                mFemale.setAlpha((float) 0.25);

            }
        });

        mFemale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gender="F";
                mMale.setAlpha((float) 0.25);
                mFemale.setAlpha((float) 1);

            }
        });

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mAge.getText().toString().length()==0 || mLocation.getText().toString().length()==0 || gender.length()==0)
                  return;

                mProgress.show();
                
                FirebaseLogin();
                //myLogin();


            }
        });

    }

    private void FirebaseLogin() {
         final FirebaseAuth mAuth;
// ...
        mAuth = FirebaseAuth.getInstance();
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            myLogin();
                        } else {
                            Toast.makeText(LoginActivity.this, "Firebase Login Failed", Toast.LENGTH_SHORT).show();
                            mProgress.hide();
                        }

                        // ...
                    }
                });
    }

    private void myLogin(){
        final Users user=new Users();
        user.setAge(mAge.getText().toString());
        user.setCid("");
        user.setLocation(mLocation.getText().toString());
        user.setSex(gender);
        user.setStatus("0");
        user.setTs(String.valueOf(System.currentTimeMillis()));
        user.setPriority(gender.equals("M")?"F":"M");
        user.setOnline(String.valueOf(System.currentTimeMillis()));

        Users.setSharePref(user);



        ASL.mLoginReference.child(ASL.mDeviceId).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                ASL.mUser=user;
                mProgress.hide();
                mProgress.dismiss();

                ASLUtils.setShared(Constants.LOGIN_STAUS,"1");
                Intent i=new Intent(LoginActivity.this,MainActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

}
