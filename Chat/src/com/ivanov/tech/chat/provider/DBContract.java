package com.ivanov.tech.chat.provider;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

public final class DBContract {

    private static final String TAG = "DBContract";

    public DBContract(){}

    public static final String DATABASE_NAME = "chat.db";
    public static final int DATABASE_VERSION = 1;
    
    //COLUMN_STATUS values
    public static final int STATUS_SENT = 0;
    public static final int STATUS_PROCESSED = 1;
    public static final int STATUS_FAILED = 2;  
    
    public static final int STATUS_UNREAD=3;
    public static final int STATUS_READ=4;
    
        
    public static abstract class Private implements BaseColumns {

        public static final String TABLE_NAME = "chat_private";
        
        public static final String COLUMN_NAME_USER_ID = "user_id";
        public static final String COLUMN_NAME_STATUS = "status";
        public static final String COLUMN_NAME_DIRECTION = "direction";
        public static final String COLUMN_NAME_MESSAGE = "message";
        public static final String COLUMN_NAME_VALUE = "value";
        public static final String COLUMN_NAME_DATE = "date";
        
        
        public static final int DIRECTION_INCOMING = 0;
        public static final int DIRECTION_OUTGOING = 1;
        
        public static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_NAME_USER_ID + " INTEGER DEFAULT 0, " +
                        COLUMN_NAME_STATUS + " INTEGER DEFAULT 0, " +
                        COLUMN_NAME_DIRECTION + " INTEGER DEFAULT 0, " +
                        COLUMN_NAME_MESSAGE + " INTEGER DEFAULT 0, " + 
                        COLUMN_NAME_VALUE + " STRING DEFAULT NULL, " +
                        COLUMN_NAME_DATE+ " TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                 ");";
        
        
    }
    
    public static abstract class Group implements BaseColumns {

        public static final String TABLE_NAME = "chat_group";
        public static final String COLUMN_NAME_SENDER = "sender";
        public static final String COLUMN_NAME_GROUP_ID = "group_id";
        public static final String COLUMN_NAME_STATUS = "status";
        public static final String COLUMN_NAME_MESSAGE = "message";
        public static final String COLUMN_NAME_VALUE = "value";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_SERVER_ID = "server_id";
        

        public static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_NAME_SENDER + " INTEGER DEFAULT 0, " +
                        COLUMN_NAME_GROUP_ID + " INTEGER DEFAULT 0, " +
                        COLUMN_NAME_STATUS + " INTEGER DEFAULT 0, " +
                        COLUMN_NAME_MESSAGE + " INTEGER DEFAULT 0, " + 
                        COLUMN_NAME_VALUE + " STRING DEFAULT NULL, " +
                        COLUMN_NAME_DATE+ " TIMESTAMP DEFAULT NULL, " +
                        COLUMN_NAME_SERVER_ID + " INTEGER DEFAULT 0 " +
                 ");";        
        
    }

   
    public static void onCreate(SQLiteDatabase db) {
        Log.w(TAG, "onCreate");

        db.execSQL(Private.CREATE_TABLE);
        db.execSQL(Group.CREATE_TABLE);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);

    }

}