package com.textnext.koshal.asl.DataBase;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by me on 10/20/2017.
 */

public class ASLProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = ASLContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, ASLContract.MessagesEntry.TABLE_NAME, MESSAGES);
        return matcher;
    }

    private static final int MESSAGES = 100;
    public ASLProvider() {
    }
    private ASLDBHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        mOpenHelper = ASLDBHelper.getInstance(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor retCursor = null;
        switch (sUriMatcher.match(uri)) {
            case MESSAGES: {
                retCursor = defaultQuery(ASLContract.MessagesEntry.TABLE_NAME, projection, selection, selectionArgs, sortOrder);
                break;
            }
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MESSAGES:
                return ASLContract.MessagesEntry.CONTENT_TYPE;

        }
        return "";
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri = null;

        switch (match) {
            case MESSAGES: {
                long _id = db.insertWithOnConflict(
                        ASLContract.MessagesEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                break;
            }

        }
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        int rowsDeleted = 0;
        switch (match) {
            case MESSAGES:
                rowsDeleted = db.delete(
                        ASLContract.MessagesEntry.TABLE_NAME, selection, selectionArgs);
                break;


        }
        return match;

    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated = 0;

        switch (match) {
            case MESSAGES:
                rowsUpdated = db.update(ASLContract.MessagesEntry.TABLE_NAME, values, selection,
                        selectionArgs);
        }
        return rowsUpdated;
    }

    private Cursor defaultQuery(String tableName, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return mOpenHelper.getReadableDatabase().query(
                tableName,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private int bulkInsertWithConflictReplace(SQLiteDatabase db, ContentValues[] values, String tableName) {
        db.beginTransaction();
        int count = 0;
        try {
            for (ContentValues value : values) {
                long _id = db.insertWithOnConflict(
                        tableName, null, value, SQLiteDatabase.CONFLICT_REPLACE);
                if (_id != -1) {
                    count++;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return count;
    }
}
