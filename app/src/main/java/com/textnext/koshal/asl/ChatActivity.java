package com.textnext.koshal.asl;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.FileProvider;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.textnext.koshal.asl.DataBase.ASLContract;
import com.textnext.koshal.asl.DataObjects.Connection;
import com.textnext.koshal.asl.DataObjects.Feedback;
import com.textnext.koshal.asl.DataObjects.Message;
import com.textnext.koshal.asl.DataObjects.Users;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ChatActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    DatabaseReference mMessageReference;
    DatabaseReference mMyOnlineReference;
    DatabaseReference mThOnlineREference;

    EditText msg;
    ImageView send, image;
    RecyclerView mRecyclerView,mSuggestions;
    TextView detailsView;
    public ChatsAdapter mChatsAdapter;
    DatabaseReference mUserReference;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int STORAGE_REQUEST_CODE = 1;
    private Uri imageUri;
    Bitmap selectedBitmap;
    int messageCount = 0;
    MarshMallowPermission marshMallowPermission;

    private InterstitialAd mInterstitialAd;
    String MyStaus = "online";


    //menu


    private Menu menu;
    private MenuItem mi_status;
    private MenuItem mi_reconnect;

    String P_UID;

    boolean disconnected = false;
    String mCID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mCID=ASLUtils.getShared(Constants.CID);

        if(mCID.length()<5)
        {
            ASLUtils.deleteAllMessage(ChatActivity.this);
            Intent i=new Intent(ChatActivity.this,MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        }

        ASL.mConnectionReference.child(mCID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Connection c=dataSnapshot.getValue(Connection.class);
                if(c==null || !c.getStatus().equals("1"))
                {
                    if(!disconnected) {

                        disconnectedByUser();
                    }
                }
                else
                {
                    getP_UID(c);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        initialize();

        connect();
          }

    private void disconnectedByUser() {

        ASL.mUser.setStatus("0");
        ASL.mUser.setCid("");
        ASL.mUser.setOnline(String.valueOf(System.currentTimeMillis()));
        mUserReference.setValue(ASL.mUser);

        ASLUtils.setShared(Constants.CID, "");
        ASLUtils.setShared(Constants.P_UID, "");
        ASLUtils.deleteAllMessage(ChatActivity.this);
        Toast.makeText(ChatActivity.this, "User Disconnected", Toast.LENGTH_SHORT).show();
        Intent i = new Intent(ChatActivity.this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();

    }

    private void initialize() {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        }


        marshMallowPermission = new MarshMallowPermission(this);

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-6690454024464967/1228602912");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        msg = (EditText) findViewById(R.id.et_msg);
        send = (ImageView) findViewById(R.id.send);
        image = (ImageView) findViewById(R.id.image);

        msg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                count=msg.getText().toString().length();
                if(count>=2)
                {
                    image.setVisibility(View.GONE);
                    return;
                }

                if (count == 0) {
                    MyStaus = "Online";
                    if (mMyOnlineReference != null)
                        mMyOnlineReference.setValue(MyStaus);
                    //send.setVisibility(View.GONE);
                    image.setVisibility(View.VISIBLE);
                } else if (count == 1) {
                        MyStaus = "Typing...";
                        if (mMyOnlineReference != null)
                            mMyOnlineReference.setValue(MyStaus);


                    image.setVisibility(View.GONE);
                   // send.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mes = msg.getText().toString();
                if (mes.length() == 0 || mMessageReference == null || ASL.mUser.getCid().equals(""))
                    return;

                sendMessage(msg.getText().toString().trim());
                msg.getText().clear();
            }
        });

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                imagePickerDialog();
            }
        });

        mUserReference = ASL.mDatabase.getReference().child("users").child(ASL.mDeviceId);
        mUserReference.child("status").setValue("Online");


        detailsView= (TextView) findViewById(R.id.tv_details);

        mRecyclerView = (RecyclerView) findViewById(R.id.rvChat);
        getSupportLoaderManager().initLoader(100, new Bundle(), this);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(manager);
        //  manager.setStackFromEnd(true);
        manager.setReverseLayout(true);

        mRecyclerView.setHasFixedSize(true);
        mChatsAdapter = new ChatsAdapter(this, null);
        mRecyclerView.setAdapter(mChatsAdapter);



        mSuggestions= (RecyclerView) findViewById(R.id.suggestions);
        LinearLayoutManager manager2 = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        mSuggestions.setLayoutManager(manager2);

        SuggestionsAdapter suggestionAdapter=new SuggestionsAdapter(ChatActivity.this,getSuggestions());
        mSuggestions.setAdapter(suggestionAdapter);

    }

    public void sendMessage(String msg) {
        Message message = new Message(

        );
        message.setMsg(msg);
        message.setTs(String.valueOf(System.currentTimeMillis()));
        message.setUid(ASL.mDeviceId);
        message.setType("text");
        message.setStatus("0");

        DatabaseReference d=mMessageReference.push();
        insertMessage(message,d.getKey());
        d.setValue(message);

    }


    private void connect() {


        if(ASL.mUser.getIsAdmin()!=null && ASL.mUser.getIsAdmin().equals("1"))
        {

            ASL.mLoginReference.child(P_UID).getRef().addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    ASL.mTuser = dataSnapshot.getValue(Users.class);
                    detailsView.setVisibility(View.VISIBLE);
                    detailsView.setText(ASL.mTuser.getAge() + " " + ASL.mTuser.getSex() + " " + ASL.mTuser.getLocation());

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        //~online status

        mMessageReference = ASL.mDatabase.getReference().child("messages").child(ASL.mUser.getCid());
        mMessageReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                messageCount++;

                Message m = dataSnapshot.getValue(Message.class);
                if (m != null && !m.getUid().equals(ASL.mDeviceId)) {
                    mMessageReference.child(dataSnapshot.getKey()).child("status").setValue("1");
                }
                if (m != null)
                    insertMessage(m, dataSnapshot.getKey());


            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                Message m = dataSnapshot.getValue(Message.class);
                insertMessage(m, dataSnapshot.getKey());


            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Message m = dataSnapshot.getValue(Message.class);
                deleteMessage(dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void getP_UID(Connection c) {

        if(c.getUser1().equals(ASL.mDeviceId))
        {
            P_UID =c.getUser2();
        }
        else{
            P_UID =c.getUser1();

        }


        mMyOnlineReference = ASL.mLoginReference.child(ASL.mDeviceId).child("online");
        mThOnlineREference = ASL.mLoginReference.child(P_UID).child("online");



        mThOnlineREference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String ThOnlineStatus = dataSnapshot.getValue(String.class);
                if (mi_status != null)
                    mi_status.setTitle(ASLUtils.getOnlineStaus(ThOnlineStatus));
                mMyOnlineReference.setValue("Online");

            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        ASL.mLoginReference.child(P_UID).child("cid").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String p_cid=dataSnapshot.getValue(String.class);
                if( p_cid==null || !p_cid.equals(mCID))
                {
                    if(!disconnected)
                    {
                        disconnectedByUser();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        ASLUtils.setShared(Constants.P_UID,P_UID);
    }


    @Override
    public void onBackPressed() {

        reconnect();
    }

    private void disconnect() {

        if (disconnected)
            return;


        disconnected = true;


        String cid = ASL.mUser.getCid();

        ASL.mUser.setStatus("0");
        ASL.mUser.setCid("");
        ASL.mUser.setOnline(String.valueOf(System.currentTimeMillis()));
        mUserReference.setValue(ASL.mUser);

        ASLUtils.setShared(Constants.CID, "");
        ASLUtils.setShared(Constants.P_UID, "");
        ASL.mConnectionReference.child(cid).child("status").setValue("0");

        if (mInterstitialAd.isLoaded() && messageCount >= 10) {
            mInterstitialAd.show();
        }
        Intent i = new Intent(ChatActivity.this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();

    }

    @Override
    protected void onResume() {
        if (mMyOnlineReference != null)
            mMyOnlineReference.setValue("Online");

        super.onResume();
    }

    @Override
    protected void onPause() {

        if (mMyOnlineReference != null)
            mMyOnlineReference.setValue(String.valueOf(System.currentTimeMillis()));

        super.onPause();

    }

    @Override
    protected void onDestroy() {
        if (mMyOnlineReference != null)
            mMyOnlineReference.setValue(String.valueOf(System.currentTimeMillis()));
        super.onDestroy();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = ASLContract.MessagesEntry.CONTENT_URI;
        CursorLoader cursorLoader = new CursorLoader(this,
                uri,
                null,
                null,
                null,
                ASLContract.MessagesEntry.COL_TS + " desc"
        );


        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if(data!=null && data.getCount()>6)
        {
            if(mSuggestions!=null)
            {
                mSuggestions.setVisibility(View.GONE);
            }
        }

        mChatsAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mChatsAdapter.swapCursor(null);
    }


    public void insertMessage(Message m, String key) {
        ContentValues[] contentValues = new ContentValues[1];
        ContentValues cv = new ContentValues();
        cv.put(ASLContract.MessagesEntry.COL_MESSAGE, m.getMsg());
        cv.put(ASLContract.MessagesEntry.COL_STATUS, m.getStatus());
        cv.put(ASLContract.MessagesEntry.COL_TS, m.getTs());
        cv.put(ASLContract.MessagesEntry.COL_USER_ID, m.getUid());
        cv.put(ASLContract.MessagesEntry.COL_TYPE, m.getType());
        cv.put(ASLContract.MessagesEntry.COL_ID, key);
        cv.put(ASLContract.MessagesEntry.COL_URI, m.getUri());



        contentValues[0] = cv;
        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        contentResolver.bulkInsert(ASLContract.MessagesEntry.CONTENT_URI, contentValues);
        contentResolver.notifyChange(ASLContract.MessagesEntry.CONTENT_URI, null);

        // mRecyclerView.scrollToPosition(0);

        //mChatsAdapter.notifyDataSetChanged();
    }

    private void imagePickerDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogLayout = inflater.inflate(R.layout.image_picker_dialog, null);
        final AlertDialog addImageDialog = new AlertDialog.Builder(this).create();
        addImageDialog.setView(dialogLayout);
        addImageDialog.show();


        RelativeLayout camera = (RelativeLayout) addImageDialog.findViewById(R.id.camera);
        RelativeLayout gallery = (RelativeLayout) addImageDialog.findViewById(R.id.gallery);

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isStoragePermissionGranted()) {
                    pickFromCamera();
                } else {
                    requestStorageAndCameraPermission();
                    if (isStoragePermissionGranted()) {
                        pickFromCamera();
                    }
                }

                addImageDialog.dismiss();
            }
        });

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ASLUtils.checkPermission(ChatActivity.this)) {
                    pickFromGallery();
                }
                addImageDialog.dismiss();
            }
        });

    }

    private void pickFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), 100);
    }

    private void pickFromCamera() {

/*
        if (!marshMallowPermission.checkPermissionForCamera()) {
            marshMallowPermission.requestPermissionForCamera();
        }
        if (!marshMallowPermission.checkPermissionForExternalStorage()) {
            marshMallowPermission.requestPermissionForExternalStorage();
        }


        if (marshMallowPermission.checkPermissionForCamera() && marshMallowPermission.checkPermissionForExternalStorage()) {
*/

    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    // Error occurred while creating the File
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(this,
                            "com.textnext.koshal.asl",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, 120);
                }
            }


    }

    String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 100 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                selectedBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                //showImageConfirmDialog(filePath);
                uploadImage(filePath);
            } catch (IOException e) {
                Toast.makeText(this, "Something went wrong !!", Toast.LENGTH_SHORT).show();
            }


        } else if (requestCode == 120 && resultCode == RESULT_OK) {

            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File f = new File(mCurrentPhotoPath);
            Uri contentUri = Uri.fromFile(f);
            mediaScanIntent.setData(contentUri);
            this.sendBroadcast(mediaScanIntent);
            try {
                selectedBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), contentUri);
                //showImageConfirmDialog(contentUri);
                uploadImage(contentUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

          /* try {
                selectedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                showImageConfirmDialog(imageUri);

            } catch (IOException e) {
                Toast.makeText(this, "Something went wrong !!", Toast.LENGTH_SHORT).show();
            }*/
        }
    }

    private void uploadImage(Uri filePath) {
        final Message m = new Message();
        m.setType("IMAGE");
        m.setUid(ASL.mDeviceId);
        m.setMsg(String.valueOf(filePath));
        m.setStatus("0");
        m.setTs(String.valueOf(System.currentTimeMillis()));
        m.setUri(String.valueOf(filePath));

        final DatabaseReference temp = mMessageReference.push();

        insertMessage(m, temp.getKey());
        StorageReference sr = ASL.mStorage.child(ASL.mUser.getCid() + System.currentTimeMillis());

        sr.putBytes(convertImageToByte(filePath)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                m.setMsg(String.valueOf(taskSnapshot.getDownloadUrl()));
                temp.setValue(m);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ChatActivity.this, "Image uploading failled", Toast.LENGTH_SHORT).show();
                deleteMessage(temp.getKey());
            }
        });

    }

    public byte[] convertImageToByte(Uri uri) {
        byte[] data = null;
        ContentResolver cr = getBaseContext().getContentResolver();
        InputStream inputStream = null;
        try {
            inputStream = cr.openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            data = baos.toByteArray();
        } catch (FileNotFoundException e) {
            Toast.makeText(this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
        }

        return data;
    }

    private void deleteMessage(String key) {
        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        contentResolver.delete(ASLContract.MessagesEntry.CONTENT_URI, ASLContract.MessagesEntry.COL_ID + "=?", new String[]{key});
        contentResolver.notifyChange(ASLContract.MessagesEntry.CONTENT_URI, null);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case ASLUtils.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                  /*  if (userChoosenTask.equals("Take Photo"))
                        cameraIntent();
                    else if (userChoosenTask.equals("Choose from Library"))
                        galleryIntent();*/
                } else {
                    //code for deny
                }
                break;
        }
    }

    private void showImageConfirmDialog(final Uri imageUri) {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(ChatActivity.this);
        View view = LayoutInflater.from(ChatActivity.this).inflate(R.layout.image_confirm, null);

        final ImageView image = (ImageView) view.findViewById(R.id.image);
        TextView send = (TextView) view.findViewById(R.id.send);

        image.setImageBitmap(selectedBitmap);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                uploadImage(imageUri);
                bottomSheetDialog.dismiss();

            }
        });

        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        this.menu = menu;
        mi_status = menu.findItem(R.id.status);
        mi_reconnect = menu.findItem(R.id.reconnect);


        if (mThOnlineREference != null) {
            mThOnlineREference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    String stat = dataSnapshot.getValue(String.class);
                    mi_status.setTitle(ASLUtils.getOnlineStaus(stat));

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        if (ASL.mUser.getCid().length() > 5) {
            mi_reconnect.setVisible(true);
        }


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.reconnect: {
                reconnect();
                break;

            }

        }
        return true;
    }

    private void reconnect() {

        if (ASL.mUser.getCid().length()<1) {

            ASL.mOnlineReference.child(ASL.mDeviceId).removeValue();
/*
            Intent i = new Intent(ChatActivity.this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);*/
            finish();
        } else {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to disconnect?")
                    .setCancelable(false)
                    .setPositiveButton("Disconnect", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            disconnect();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();

        }

    }

    public ArrayList<String> getSuggestions() {
        ArrayList<String> suggestions=new ArrayList<>();

        suggestions.add("Hi!");
        suggestions.add(ASL.mUser.getAge()+" "+ASL.mUser.getSex()+" "+ASL.mUser.getLocation());
        suggestions.add("What is your name ?");
        suggestions.add("Where are you from ?");
        suggestions.add("How are you ?");
        return suggestions;
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (this.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED &&
                    this.checkSelfPermission(android.Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_GRANTED) {

                return true;
            } else {

                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation

            return true;
        }
    }

    private void requestStorageAndCameraPermission() {
        ActivityCompat
                .requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}
                        , STORAGE_REQUEST_CODE);
    }

}
