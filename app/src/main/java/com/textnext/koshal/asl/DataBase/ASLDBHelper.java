package com.textnext.koshal.asl.DataBase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by me on 10/20/2017.
 */

public class ASLDBHelper extends SQLiteOpenHelper {


    private static final String LOG_TAG = "ASLDBHELPER";

    public static final int VER_1 = 1; //1.0.2_19

    public static final int DATABASE_CURRENT_VERSION = VER_1;

    public static final String DATABASE_NAME = "asl.db";
    private Context mContext;

    private static ASLDBHelper dbHelper;

    public ASLDBHelper(Context applicationContext) {
        super(applicationContext, DATABASE_NAME, null, DATABASE_CURRENT_VERSION);
        mContext=applicationContext;
    }

    public static synchronized ASLDBHelper getInstance(Context ctx) {
        if (dbHelper == null) {
            dbHelper = new ASLDBHelper(ctx.getApplicationContext());
        }
        return dbHelper;
    }


    public ASLDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        ver1(db);

    }

    private void ver1(SQLiteDatabase db) {
        createTableMessages(db);
    }

    private void createTableMessages(SQLiteDatabase db) {
        final String SQL_CREATE_ITEM_FEED_TABLE = "CREATE TABLE IF NOT EXISTS " + ASLContract.MessagesEntry.TABLE_NAME + " (" +
                ASLContract.MessagesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                ASLContract.MessagesEntry.COL_ID + " TEXT UNIQUE NOT NULL, " +
                ASLContract.MessagesEntry.COL_USER_ID + " TEXT NOT NULL, " +
                ASLContract.MessagesEntry.COL_MESSAGE + " TEXT NOT NULL, " +
                ASLContract.MessagesEntry.COL_TYPE + " TEXT, " +
                ASLContract.MessagesEntry.COL_STATUS + " TEXT , " +
                ASLContract.MessagesEntry.COL_TS + " TEXT NOT NULL " +
                " );";

        db.execSQL(SQL_CREATE_ITEM_FEED_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
