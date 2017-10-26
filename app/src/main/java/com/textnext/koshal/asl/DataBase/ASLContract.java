package com.textnext.koshal.asl.DataBase;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.compat.BuildConfig;

/**
 * Created by me on 10/20/2017.
 */

public class ASLContract{

        public static final String CONTENT_AUTHORITY = BuildConfig.APPLICATION_ID;
        public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);


    public static final class MessagesEntry implements BaseColumns {


        public static final String TABLE_NAME = "messages";
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TABLE_NAME;

        public static final String COL_ID = "id";
        public static final String COL_USER_ID = "uid";
        public static final String COL_MESSAGE = "message";
        public static final String COL_TYPE = "type";
        public static final String COL_STATUS = "status";
        public static final String COL_TS = "ts";
        public static final String COL_URI = "uri";


        public static Uri buildUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }
}
