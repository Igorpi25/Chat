package com.ivanov.tech.chat.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;

import java.util.Date;

import com.ivanov.tech.chat.Chat;
import com.ivanov.tech.session.Session;

public class DBContentProvider extends ContentProvider{

    private DBHelper dbHelper;

    public static final String AUTHORITY = "com.ivanov.tech.chat.provider.contentprovider_db";
   
    public static final Uri URI_PRIVATE = Uri.parse("content://" + AUTHORITY + "/" + DBContract.Private.TABLE_NAME);
    public static final Uri URI_GROUP = Uri.parse("content://" + AUTHORITY + "/" + DBContract.Group.TABLE_NAME);
    public static final Uri URI_RECENTLIST = Uri.parse("content://" + AUTHORITY + "/recentlist");
    
    
    private static final UriMatcher uriMatcher;
    
    
    private static final int PRIVATE = 1;
    private static final int PRIVATE_USER_ID = 11;
    private static final int PRIVATE_USER_ID_LAST = 111;
    private static final int PRIVATE_USER_ID_MAKEREAD = 112;
    private static final int PRIVATE_LAST = 12;
        
    private static final int GROUP = 2;
    private static final int GROUP_GROUP_ID = 21;
    private static final int GROUP_GROUP_ID_MAKEREAD = 211;
    
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        // a content URI pattern matches content URIs using wildcard characters:
        // *: Matches a string of any valid characters of any length.
        // #: Matches a string of numeric characters of any length.

        
        //All private messages
        uriMatcher.addURI(AUTHORITY, DBContract.Private.TABLE_NAME, PRIVATE);
        //Private messages with one user
        uriMatcher.addURI(AUTHORITY, DBContract.Private.TABLE_NAME+"/#", PRIVATE_USER_ID);        
        //Last private message of user 
        uriMatcher.addURI(AUTHORITY, DBContract.Private.TABLE_NAME+"/#/last", PRIVATE_USER_ID_LAST);
        //Make private messages READ of user_id
        uriMatcher.addURI(AUTHORITY, DBContract.Private.TABLE_NAME+"/#/makeread", PRIVATE_USER_ID_MAKEREAD);
         
        //All messages from group table 
        uriMatcher.addURI(AUTHORITY, DBContract.Group.TABLE_NAME, GROUP);
        //Messages of one group
        uriMatcher.addURI(AUTHORITY, DBContract.Group.TABLE_NAME+"/#", GROUP_GROUP_ID);
        //Make group messages READ of group_id
        uriMatcher.addURI(AUTHORITY, DBContract.Group.TABLE_NAME+"/#/makeread", GROUP_GROUP_ID_MAKEREAD);
        

    }

    // system calls onCreate() when it starts up the provider.
    @Override
    public boolean onCreate() {
    	
        dbHelper = new DBHelper(getContext());
        
        return false;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }
    
    @Override
    public Uri insert(Uri uri, ContentValues values) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id;
        Uri resultUri;
        switch (uriMatcher.match(uri)) {
            
            case PRIVATE:
            	
            	id = db.insert(DBContract.Private.TABLE_NAME, null, values);
            	
                resultUri=Uri.parse(URI_PRIVATE+"/"+id);

                Log.d("DBContentProvider","insert resultUri="+resultUri.toString());

                getContext().getContentResolver().notifyChange(URI_PRIVATE, null);
                getContext().getContentResolver().notifyChange(URI_RECENTLIST, null);

                return resultUri;   
            
            case GROUP:
            	
            	id = db.insert(DBContract.Group.TABLE_NAME, null, values);
            	
                resultUri=Uri.parse(URI_GROUP+"/"+id);

                Log.d("DBContentProvider","insert resultUri="+resultUri.toString());

                getContext().getContentResolver().notifyChange(URI_GROUP, null);
                getContext().getContentResolver().notifyChange(URI_RECENTLIST, null);

                return resultUri;   
            
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

    }
    
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        
        
        if( uri.equals(URI_RECENTLIST) ){
        	
        	Log.d("DBContentProvider", "query URI_RECENTLIST_PRIVATE");
        	
        	Cursor cursor=null;
        	
        	try {
                
                String sql=
                	"SELECT 0 AS recent_type, p.user_id AS profile_id, CASE WHEN p.direction = 0 THEN p.user_id ELSE "+Session.getUserId()+" END AS sender, p.message AS message, p.value AS value, p.date AS date, p.status AS status, user.url_icon AS icon, user.name AS name, user.name AS sender_name " +                	
                	"FROM 	( SELECT user_id, max( _id ) AS max_id "+
		           			"FROM " + DBContract.Private.TABLE_NAME + " " +
		           			"WHERE ( message = "+Chat.TRANSPORT_TEXT+" ) "+
		           			"GROUP BY user_id "+
		           			") AS u "+
		           	"INNER JOIN " + DBContract.Private.TABLE_NAME + " AS p ON p._id = u.max_id "+
		           	"LEFT JOIN db_profile.user AS user ON u.user_id = user.server_id "+
		            
		           	
		           	"UNION ALL "+
		           	
		           	"SELECT 1 AS recent_type, p.group_id AS profile_id, p.sender AS sender, p.message AS message, p.value AS value, p.date AS date, p.status AS status, groups.url_icon AS icon, groups.name AS name, user.name AS sender_name  " +
                	"FROM 	( SELECT group_id, sender, max( _id ) AS max_id "+
		           			"FROM " + DBContract.Group.TABLE_NAME + " " +
		           			"WHERE ( message = "+Chat.TRANSPORT_TEXT+" ) "+
		           			"GROUP BY group_id "+
		           			") AS u "+
		           	"INNER JOIN " + DBContract.Group.TABLE_NAME + " AS p ON p._id = u.max_id "+
		           	"LEFT JOIN db_profile.groups AS groups ON u.group_id = groups.server_id "+
		           	"LEFT JOIN db_profile.user AS user ON u.sender = user.server_id "+
		           	
		           	"ORDER BY date DESC";
                
                cursor=db.rawQuery(sql,null);
            	cursor.setNotificationUri(getContext().getContentResolver(),URI_RECENTLIST);
            	
            	
            } catch (SQLException e) {
            	Log.e("DBContentProvider", "query URI_RECENTLIST SQLException e="+e);
            	e.printStackTrace();
            } catch (Exception e) {
            	Log.e("DBContentProvider", "query URI_RECENTLIST Exception e="+e);
            	e.printStackTrace();
            } 
        	
        	return cursor;
        }       
        
        if( uriMatcher.match(uri)==PRIVATE_USER_ID ){
        	
        	Log.d("DBContentProvider", "query PRIVATE_USER_ID");
        	Cursor cursor=null;
        	
        	String interlocutor_id = uri.getPathSegments().get(1);
        	
        	try{        	
        		db.execSQL("ATTACH DATABASE ? AS db_profile", new String[]{ getContext().getDatabasePath("profile.db").getPath() });
        		    
                String sql=
                	"SELECT chat_private.*, user.url_icon AS icon, user.name AS name  " +                	
                	"FROM chat_private AS chat_private "+
		           	"INNER JOIN db_profile.user AS user ON chat_private.user_id = user.server_id "+
                	"WHERE chat_private.user_id = "+interlocutor_id+" "+
		           	"ORDER BY chat_private.date ASC";
                
                cursor=db.rawQuery(sql,null);
            	cursor.setNotificationUri(getContext().getContentResolver(),uri);
            	            	
            } catch (SQLException e) {
            	Log.e("DBContentProvider", "query PRIVATE_USER_ID SQLException e="+e);
            	e.printStackTrace();
            } catch (Exception e) {
            	Log.e("DBContentProvider", "query PRIVATE_USER_ID Exception e="+e);
            	e.printStackTrace();
            } 
        	
        	return cursor;
        }    
        
        if( uriMatcher.match(uri)==GROUP_GROUP_ID ){
        	
        	Log.d("DBContentProvider", "query GROUP_GROUP_ID");
        	Cursor cursor=null;
        	
        	String group_id = uri.getPathSegments().get(1);
        	
        	try{        	
        		db.execSQL("ATTACH DATABASE ? AS db_profile", new String[]{ getContext().getDatabasePath("profile.db").getPath() });
        		    
                String sql=
                	"SELECT chat_group.*, user.url_icon AS icon, user.name AS name  " +
                	"FROM chat_group AS chat_group "+
		           	"INNER JOIN db_profile.user AS user ON chat_group.sender = user.server_id "+
                	"WHERE chat_group.group_id = "+group_id+" "+
		           	"ORDER BY chat_group.date ASC";
                
                cursor=db.rawQuery(sql,null);
            	cursor.setNotificationUri(getContext().getContentResolver(),uri);
            	            	
            } catch (SQLException e) {
            	Log.e("DBContentProvider", "query GROUP_GROUP_ID SQLException e="+e);
            	e.printStackTrace();
            } catch (Exception e) {
            	Log.e("DBContentProvider", "query GROUP_GROUP_ID Exception e="+e);
            	e.printStackTrace();
            } 
        	
        	return cursor;
        }    
        
        
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        String user_server_id=null;
        String group_id=null;        
        
        switch (uriMatcher.match(uri)) {            
            
            case PRIVATE:
                queryBuilder.setTables(DBContract.Private.TABLE_NAME);
                break;
                            
            case PRIVATE_USER_ID_LAST:
            	queryBuilder.setTables(DBContract.Private.TABLE_NAME);
                user_server_id = uri.getPathSegments().get(1);
                
                if(user_server_id.equals("0")){            		
                	user_server_id=String.valueOf(Session.getUserId());
            		Log.d("DBContentProvider", "query User_id replaced rom 0 to "+user_server_id);
            	}
                
                queryBuilder.appendWhere(DBContract.Private.COLUMN_NAME_USER_ID + " = " + user_server_id);
                Cursor c = queryBuilder.query(db, projection, selection,selectionArgs, null, null, sortOrder, "1");
                c.setNotificationUri(getContext().getContentResolver(),Uri.parse(URI_PRIVATE+"/"+user_server_id));
                
                return c;
                
            case GROUP:
                queryBuilder.setTables(DBContract.Group.TABLE_NAME);
                break;
            
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        Cursor cursor = queryBuilder.query(db, projection, selection,selectionArgs, null, null, sortOrder);

        switch (uriMatcher.match(uri)) {            
           
            case PRIVATE:
                cursor.setNotificationUri(getContext().getContentResolver(),URI_PRIVATE);
                break;            
            case GROUP:
                cursor.setNotificationUri(getContext().getContentResolver(),URI_GROUP);
                break;  
            
        }

        Log.d("DBContentProvider","query Uri="+uri.toString());

        return cursor;

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int deleteCount;
        switch (uriMatcher.match(uri)) {

        	//Delete messages of private conversation with user
            case PRIVATE_USER_ID:
                String user_server_id = uri.getPathSegments().get(1);
                //Delete conversation
                selection = DBContract.Private.COLUMN_NAME_USER_ID + " = " + user_server_id;
                deleteCount = db.delete(DBContract.Private.TABLE_NAME, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(URI_PRIVATE, null);

                break;
                
            //Delete all messages
            case PRIVATE:
                deleteCount = db.delete(DBContract.Private.TABLE_NAME, selection, selectionArgs);

                break;
                
              //Delete one group's messages
            case GROUP_GROUP_ID:
                String group_id = uri.getPathSegments().get(1);
                //Delete group conversation
                selection = DBContract.Group.COLUMN_NAME_GROUP_ID + " = " + group_id;
                deleteCount = db.delete(DBContract.Group.TABLE_NAME, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(URI_GROUP, null);

                break;
                
            //Delete all messages of group chat
            case GROUP:
                deleteCount = db.delete(DBContract.Group.TABLE_NAME, selection, selectionArgs);

                break;
            
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        Log.d("DBContentProvider","delete Uri="+uri.toString());
        getContext().getContentResolver().notifyChange(uri, null);
        getContext().getContentResolver().notifyChange(URI_RECENTLIST, null);
        
        return deleteCount;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        
    	SQLiteDatabase db = dbHelper.getWritableDatabase();
        int updateCount;

        switch (uriMatcher.match(uri)) {
        	case PRIVATE:
        		updateCount = db.update(DBContract.Private.TABLE_NAME, values, selection, selectionArgs);

                getContext().getContentResolver().notifyChange(URI_PRIVATE, null);
                
                break;
                
        	case GROUP:
        		updateCount = db.update(DBContract.Group.TABLE_NAME, values, selection, selectionArgs);

                getContext().getContentResolver().notifyChange(URI_GROUP, null);
                
                break;
                                
        	case PRIVATE_USER_ID_MAKEREAD:{
        		        		
        		String user_id = uri.getPathSegments().get(1);
        		String where="( "+DBContract.Private.COLUMN_NAME_USER_ID+" = ? ) AND ( "+DBContract.Private.COLUMN_NAME_STATUS+" = ? )";
           		String[] args={String.valueOf(user_id),String.valueOf(DBContract.STATUS_UNREAD)};
           		
           		values=new ContentValues();
           		values.put(DBContract.Private.COLUMN_NAME_STATUS,DBContract.STATUS_READ);
           		
           		
           		updateCount = db.update(DBContract.Private.TABLE_NAME, values, where, args);           		
           		if(updateCount>0)
           		getContext().getContentResolver().notifyChange(URI_RECENTLIST, null);
                
           		break;} 
                
        	case GROUP_GROUP_ID_MAKEREAD:{
        		
        		String group_id = uri.getPathSegments().get(1);
        		String where="( "+DBContract.Group.COLUMN_NAME_GROUP_ID+" = ? ) AND ( "+DBContract.Group.COLUMN_NAME_STATUS+" = ? )";
           		String[] args={String.valueOf(group_id),String.valueOf(DBContract.STATUS_UNREAD)};
           		
           		values=new ContentValues();
           		values.put(DBContract.Group.COLUMN_NAME_STATUS,DBContract.STATUS_READ);
           		
           		updateCount = db.update(DBContract.Group.TABLE_NAME, values, where, args);
           		
           		if(updateCount>0)
           			getContext().getContentResolver().notifyChange(URI_RECENTLIST, null);
                break;}
                
        	default:
        		throw new IllegalArgumentException("Unsupported URI: " + uri);
        }


        Log.d("DBContentProvider","update Uri="+uri.toString());
       
        return updateCount;
    	
    }
    

}