package com.ivanov.tech.chat.service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.codebutler.android_websockets.WebSocketClient;
import com.ivanov.tech.chat.Chat;
import com.ivanov.tech.chat.provider.DBContentProvider;
import com.ivanov.tech.chat.provider.DBContract;
import com.ivanov.tech.chat.reciever.MessageReciever;
import com.ivanov.tech.communicator.service.TransportBase;

public class TransportChat extends TransportBase{


	private static final String TAG = TransportChat.class
            .getSimpleName();    
    
    private static final String JSON_MESSAGE_ID="message_id";//To confirm outgoing message in ChatService
    
    public static final int TYPE_PRIVATE=1;
    public static final int TYPE_GROUP=2;
    
    private static final String JSON_TRANSPORT="transport";
    private static final String JSON_VALUE="value";
    private static final String JSON_DATE="date";
    
    private static final String JSON_INTERLOCUTOR_ID="interlocutor_id";
            
    private static final String JSON_GROUP_ID="group_id";
    private static final String JSON_SENDER="sender";
    private static final String JSON_ID="id";//Server id of group-message
        
    public static final String BROADCAST_MESSAGE="com.ivanov.tech.chat.reciever.MessageReciever.MESSAGE";
        
    public TransportChat(Context context) {    		
		super(context);
	}
        
//--------------------TransportProtocol----------------------
    
    @Override
    public boolean onOutgoingMessage(int transport, JSONObject json){
	    	    
	    //Если это текстовое сообщение
	    if(transport==Chat.TRANSPORT_TEXT) {

	    	Log.d(TAG, "onOutgoingMessage transport="+transport+" json="+json);
		    
	    	int message_id=-1;
	    	int outgoing_failed_type=-1;
	    	
			try {
				json.put(JSON_TRANSPORT, Chat.TRANSPORT_TEXT);
				json.put(JSON_DATE, new Date().getTime());				
				
				//Если это личное сообщение
				if(json.has(JSON_INTERLOCUTOR_ID)){
											
					if(json.has(JSON_MESSAGE_ID)){	
						message_id=json.getInt(JSON_MESSAGE_ID);
						makePrivateFailedMessageProcessed(message_id);
					} else {
						message_id=createPrivateOutgoingMessage(json);
					}
					
					outgoing_failed_type=TYPE_PRIVATE;
				}
				
				//Если это групповое сообщение
				if(json.has(JSON_GROUP_ID)){
					
					if(json.has(JSON_MESSAGE_ID)){	
						message_id=json.getInt(JSON_MESSAGE_ID);
						makeGroupFailedMessageProcessed(message_id);
					} else {
						message_id=createGroupOutgoingMessage(json);
					}
					
					outgoing_failed_type=TYPE_GROUP;
				}
			
				//Удаляем дату, оно не нужно на сервере. Там серверная дата
				json.remove(JSON_DATE);
				
				//Удаляем sender. На сервере определяется по Api-Key во время подключения сокета. 
				//Все что получено по сокету, отправил user создавший сокет 
				json.remove(JSON_SENDER);
				
				//Добавляем message_id, нужно для confirming в ChatService
				json.put(JSON_MESSAGE_ID, message_id);				
				
			}catch(JSONException e) {
				Log.e(TAG, "onOutgoingMessage JSONException_2 e="+e);			
			}
					
			sendMessage(outgoing_failed_type,message_id, json);		
			
			return true;
	    }
	    
	    return false;
	}
    
    @Override
    public boolean onIncomingMessage(int transport, JSONObject json){
		Log.d(TAG, "onIncomingMessage transport="+transport+" json="+json);
		
		if(transport==Chat.TRANSPORT_TEXT){
			try {
	        	
	        	if(json.getInt(JSON_TRANSPORT)==Chat.TRANSPORT_TEXT){
	        		     
	        		//Коррекция времени//Correction  Linux UTC time from seconds to milliseconds
		        	if(json.has(JSON_DATE)){
		        		long date_timestamp=json.getLong(JSON_DATE);
		        		//Переводим из секунд (на сервере Linux UTC в секундах) к миллисикундам(Андроид-клиенте timestamp в миллисекундах)
		        		date_timestamp=date_timestamp*1000;
		        		//Подменяем JSON
		        		json.remove(JSON_DATE);
		        		json.put(JSON_DATE, date_timestamp);
		        	}
		        	
			        if(json.has(JSON_MESSAGE_ID)){//Confirm outgoing message
			        	
			        	if(json.has(JSON_INTERLOCUTOR_ID)){		        	
			        		makePrivateOutgoingMessageSent(json.getInt(JSON_MESSAGE_ID));
			        	}
			        	
			        	if(json.has(JSON_GROUP_ID)){		        	
			        		makeGroupOutgoingMessageSent(json.getInt(JSON_MESSAGE_ID),json.getInt(JSON_ID));
			        	}
			        }else{//Incoming message
				        			        	
			        	if(json.has(JSON_INTERLOCUTOR_ID)){		        	
			        		createPrivateIncomingMessage(json);
			        		sendPrivateMessageBroadcast(json.getInt(JSON_INTERLOCUTOR_ID),0,json.getString(JSON_VALUE));
			        	}
			        	
			        	if(json.has(JSON_GROUP_ID)){		        	
			        		createGroupIncomingMessage(json);
			        		sendGroupMessageBroadcast(json.getInt(JSON_GROUP_ID),json.getInt(JSON_SENDER),0,json.getString(JSON_VALUE));
			        	}
			        	
			        }
			        
	        	}
	        
	        } catch (JSONException e) {
				Log.d(TAG, "onIncomingMessage JSONException e="+e);
			}
			return true;
		}
		return false;
	}    
    
    @Override
	public void onOutgoingFailed(int outgoing_failed_type, int message_id) {
		switch(outgoing_failed_type){
			case TYPE_PRIVATE: 
				makePrivateOutgoinMessageFailed(message_id);
				break;
			case TYPE_GROUP: 
				makeGroupOutgoinMessageFailed(message_id);
				break;
		}
	}	
    
//------------WebsocketClientListener------------------------
    
    @Override
	public void onCreate(WebSocketClient websocketclient) {
		Log.d(TAG, "onConnect(WebSocketClient)");
		
		//Чтобы "вечно-PROCESSED" сообщений не было
    	makeAllProcessedMessagesFailed();
	}
    
    @Override
    public void onDisconnect(int code, String reason) {
        super.onDisconnect(code, reason);                
        Log.d(TAG, "onDisconnect");
    	
        //Чтобы "вечно-PROCESSED" сообщений не было
        makeAllProcessedMessagesFailed();
    }

    @Override
    public void onError(Exception error) {
    	super.onError(error);        
        Log.d(TAG, "onError");
    	
        //Чтобы "вечно-PROCESSED" сообщений не было
        makeAllProcessedMessagesFailed();
    }
    
//-------------------Incoming Message Broadcast------------------------------
    
    void sendPrivateMessageBroadcast(int userid,int message,String value){
    	Intent intent=new Intent(BROADCAST_MESSAGE);
    	
    	intent.putExtra(MessageReciever.EXTRA_TYPE, MessageReciever.TYPE_PRIVATE);
    	intent.putExtra(MessageReciever.EXTRA_USERID, userid);
    	intent.putExtra(MessageReciever.EXTRA_MESSAGE, message);
    	intent.putExtra(MessageReciever.EXTRA_VALUE, value);   	
    	
    	sendOrderedBroadcast(intent, null);
    }
    
    void sendGroupMessageBroadcast(int groupid,int userid,int message,String value){
    	Intent intent=new Intent(BROADCAST_MESSAGE);
    	
    	intent.putExtra(MessageReciever.EXTRA_TYPE, MessageReciever.TYPE_GROUP);
    	intent.putExtra(MessageReciever.EXTRA_GROUPID, groupid);
    	intent.putExtra(MessageReciever.EXTRA_USERID, userid);    	
    	intent.putExtra(MessageReciever.EXTRA_MESSAGE, message);
    	intent.putExtra(MessageReciever.EXTRA_VALUE, value);   	
    	
    	sendOrderedBroadcast(intent, null);
    }
   
//------------Private DB processing-------------------------------
	
    //Insert incoming message into DB.PrivateTable  
 	void createPrivateIncomingMessage(JSONObject json) {
 		  		
 		Log.e(TAG, "createIncomingMessagePrivate");
// 		
// 		
// 		//If user doesn't exist then load him
// 		int interlocutor_id=0;
// 		try {			
// 			interlocutor_id=json.getInt(JSON_INTERLOCUTOR_ID);
// 		} catch (Exception e) {
//			Log.e(TAG, "createIncomingMessage Exception e1="+e);
//		}
// 		if(!Profile.isUserExists(this, interlocutor_id)){
// 			Profile.getUsersRequest(Profile.URL_USERS_ALL, this, null);
// 		}
 		
 		ContentValues values=new ContentValues();	
		
			values.put(DBContract.Private.COLUMN_NAME_DIRECTION, DBContract.Private.DIRECTION_INCOMING);
			values.put(DBContract.Private.COLUMN_NAME_STATUS, DBContract.STATUS_UNREAD);
		try {			
			values.put(DBContract.Private.COLUMN_NAME_USER_ID, json.getInt(JSON_INTERLOCUTOR_ID));
			values.put(DBContract.Private.COLUMN_NAME_MESSAGE, json.getInt(JSON_TRANSPORT));//one of DBContract COLUMN_NAME_MESSAGE values
			values.put(DBContract.Private.COLUMN_NAME_VALUE, json.getString(JSON_VALUE));
			values.put(DBContract.Private.COLUMN_NAME_DATE, timestampToString(json.getLong(JSON_DATE)));
		} catch (JSONException e) {
			Log.e(TAG, "createIncomingMessage JSONException e2="+e);
		}
		
		getContentResolver().insert(DBContentProvider.URI_PRIVATE, values);
	}
 	
 	//Insert PROCESSED-message into DB.PrivateTable  
 	int createPrivateOutgoingMessage(JSONObject json) {
 	 		  			
 		Log.d(TAG, "createOutgoingMessage STATUS_PROCESSED json="+json);
 		
 		ContentValues values=new ContentValues();	
 		
 		values.put(DBContract.Private.COLUMN_NAME_DIRECTION, DBContract.Private.DIRECTION_OUTGOING);
 		values.put(DBContract.Private.COLUMN_NAME_STATUS, DBContract.STATUS_PROCESSED);
 		try {
 			values.put(DBContract.Private.COLUMN_NAME_USER_ID, json.getInt(JSON_INTERLOCUTOR_ID));
 			values.put(DBContract.Private.COLUMN_NAME_MESSAGE, json.getInt(JSON_TRANSPORT));
 			values.put(DBContract.Private.COLUMN_NAME_VALUE, json.getString(JSON_VALUE));
 			values.put(DBContract.Private.COLUMN_NAME_DATE, timestampToString(json.getLong(JSON_DATE)));
 		} catch (JSONException e) {
 			Log.e(TAG, "createOutgoingMessage STATUS_PROCESSED JSONException e="+e);
 		}
 		
 		Uri uri=getContentResolver().insert(DBContentProvider.URI_PRIVATE, values);
 		
 		int message_id=Integer.parseInt(uri.getLastPathSegment());
 		Log.d(TAG, "createOutgoingMessage STATUS_PROCESSED message_id="+message_id);
 		
 		return message_id; 		
 	}
 	
 	//Update message to FAILED in DB.PrivateTable  
 	void makePrivateOutgoinMessageFailed(int message_id) {
			
 		Log.e(TAG, "makeOutgoinMessageFailed STATUS_FAILED message_id="+message_id);
 		
 		ContentValues values=new ContentValues();	
 		
 		values.put(DBContract.Private.COLUMN_NAME_STATUS, DBContract.STATUS_FAILED); 		
 		String where=DBContract.Private._ID+" = "+message_id;
 		getContentResolver().update(DBContentProvider.URI_PRIVATE, values,where,null);
 	}	

 	//Update message SENT in DB.PrivateTable  
 	void makePrivateOutgoingMessageSent(int message_id) {
		
 		Log.e(TAG, "makeOutgoingMessageSent STATUS_SENT message_id="+message_id);
 		
 		ContentValues values=new ContentValues();	
 		
 		values.put(DBContract.Private.COLUMN_NAME_STATUS, DBContract.STATUS_SENT); 		
 		String where=DBContract.Private._ID+" = "+message_id;
 		getContentResolver().update(DBContentProvider.URI_PRIVATE, values,where,null);
 	}	
 	
 	//Update message FAILED to PROCESSED in DB.PrivateTable  
 	void makePrivateFailedMessageProcessed(int message_id) {
		
 		Log.e(TAG, "makePrivateFailedMessageProcessed STATUS_PROCESSED message_id="+message_id);
 		
 		ContentValues values=new ContentValues();	
 		
 		values.put(DBContract.Private.COLUMN_NAME_STATUS, DBContract.STATUS_PROCESSED); 		
 		String where=DBContract.Private._ID+" = "+message_id;
 		getContentResolver().update(DBContentProvider.URI_PRIVATE, values,where,null);
 	}	

 //------------Group DB processing-------------------------------
 	
 	 //Insert incoming message into DB.GroupTable  
 	void createGroupIncomingMessage(JSONObject json) {
 		  		
 		Log.e(TAG, "createGroupIncomingMessage");
 				
 		ContentValues values=new ContentValues();	
 		
 		//Если есть дубликаты, то удаляем их 		
 		try {
			getContentResolver().delete(DBContentProvider.URI_GROUP," ( ? = ? ) AND ( ? = ? ) ", new String[]{ JSON_GROUP_ID, json.getString(JSON_GROUP_ID), JSON_ID,json.getString(JSON_ID) });
		} catch (JSONException e) {
			Log.e(TAG, "createGroupIncomingMessage delete  JSONException e="+e);
		}
 		
		//Потом добавляем
		values.put(DBContract.Group.COLUMN_NAME_STATUS, DBContract.STATUS_UNREAD);
		try {			
			values.put(DBContract.Group.COLUMN_NAME_SENDER, json.getInt(JSON_SENDER));
			values.put(DBContract.Group.COLUMN_NAME_GROUP_ID, json.getInt(JSON_GROUP_ID));
			values.put(DBContract.Group.COLUMN_NAME_SERVER_ID, json.getInt(JSON_ID));
			
			values.put(DBContract.Group.COLUMN_NAME_MESSAGE, json.getInt(JSON_TRANSPORT));//one of DBContract COLUMN_NAME_MESSAGE values
			values.put(DBContract.Group.COLUMN_NAME_VALUE, json.getString(JSON_VALUE));
			values.put(DBContract.Group.COLUMN_NAME_DATE, timestampToString(json.getLong(JSON_DATE)));
		} catch (JSONException e) {
			Log.e(TAG, "createGroupIncomingMessage add JSONException e="+e);
		}
		
		getContentResolver().insert(DBContentProvider.URI_GROUP, values);
	}
 	
 	//Insert PROCESSED-message into DB.GroupTable  
 	int createGroupOutgoingMessage(JSONObject json) {
 	 		  			
 		Log.d(TAG, "createGroupOutgoingMessage STATUS_PROCESSED json="+json);
 		
 		ContentValues values=new ContentValues();	
 		
 		values.put(DBContract.Group.COLUMN_NAME_STATUS, DBContract.STATUS_PROCESSED);
 		try {
 			values.put(DBContract.Group.COLUMN_NAME_SERVER_ID, 0);
 			values.put(DBContract.Group.COLUMN_NAME_GROUP_ID, json.getString(JSON_GROUP_ID));
 			values.put(DBContract.Group.COLUMN_NAME_SENDER, json.getInt(JSON_SENDER));
 			values.put(DBContract.Group.COLUMN_NAME_MESSAGE, json.getInt(JSON_TRANSPORT));
 			values.put(DBContract.Group.COLUMN_NAME_VALUE, json.getString(JSON_VALUE));
 			values.put(DBContract.Group.COLUMN_NAME_DATE, timestampToString(json.getLong(JSON_DATE)));
 		} catch (JSONException e) {
 			Log.e(TAG, "createGroupOutgoingMessage STATUS_PROCESSED JSONException e="+e);
 		}
 		
 		Uri uri=getContentResolver().insert(DBContentProvider.URI_GROUP, values);
 		
 		int message_id=Integer.parseInt(uri.getLastPathSegment());
 		Log.d(TAG, "createGroupOutgoingMessage STATUS_PROCESSED message_id="+message_id);
 		
 		return message_id; 		
 	}
 	
 	//Update message to FAILED in DB.GroupTable  
 	void makeGroupOutgoinMessageFailed(int message_id) {
			
 		Log.e(TAG, "makeGroupOutgoinMessageFailed STATUS_FAILED message_id="+message_id);
 		
 		ContentValues values=new ContentValues();	
 		
 		values.put(DBContract.Group.COLUMN_NAME_STATUS, DBContract.STATUS_FAILED); 		
 		String where=DBContract.Group._ID+" = "+message_id;
 		getContentResolver().update(DBContentProvider.URI_GROUP, values,where,null);
 	}	

 	//Update message SENT in DB.GroupTable  
 	void makeGroupOutgoingMessageSent(int message_id, int server_id) {
		
 		Log.e(TAG, "makeGroupOutgoingMessageSent STATUS_SENT message_id="+message_id);
 		
 		ContentValues values=new ContentValues();	
 		
 		values.put(DBContract.Group.COLUMN_NAME_STATUS, DBContract.STATUS_SENT); 
 		values.put(DBContract.Group.COLUMN_NAME_SERVER_ID, server_id);
 		
 		String where=DBContract.Group._ID+" = "+message_id;
 		getContentResolver().update(DBContentProvider.URI_GROUP, values,where,null);
 	}	
 	
 	//Update message FAILED to PROCESSED in DB.GroupTable  
 	void makeGroupFailedMessageProcessed(int message_id) {
		
 		Log.e(TAG, "makeGroupFailedMessageProcessed PROCESSED message_id="+message_id);
 		
 		ContentValues values=new ContentValues();	
 		
 		values.put(DBContract.Group.COLUMN_NAME_STATUS, DBContract.STATUS_PROCESSED); 		
 		String where=DBContract.Group._ID+" = "+message_id;
 		getContentResolver().update(DBContentProvider.URI_GROUP, values,where,null);
 	}	
 
 //-------------All DB processing------------------
 
 	//Update all PROCESSED to FAIL
 	void makeAllProcessedMessagesFailed() {
 				
 	 	Log.e(TAG, "makeAllProcessedMessagesFailed");
 	 		
 	 	
 	 	//Private messages
 	 	ContentValues valuesPrivate=new ContentValues(); 	 		
 	 	valuesPrivate.put(DBContract.Private.COLUMN_NAME_STATUS, DBContract.STATUS_FAILED); 		
 	 	String wherePrivate=DBContract.Private.COLUMN_NAME_STATUS+" = "+DBContract.STATUS_PROCESSED; 	 	
 	 	getContentResolver().update(DBContentProvider.URI_PRIVATE, valuesPrivate,wherePrivate,null);
 	 	 	 	
 	 	//Group messages
 	 	ContentValues valuesGroup=new ContentValues(); 	 		
 	 	valuesGroup.put(DBContract.Group.COLUMN_NAME_STATUS, DBContract.STATUS_FAILED); 		
 	 	String whereGroup=DBContract.Group.COLUMN_NAME_STATUS+" = "+DBContract.STATUS_PROCESSED; 	 	
 	 	getContentResolver().update(DBContentProvider.URI_GROUP, valuesGroup,whereGroup,null);
 	 	
 	}	
 //---------------Timestamp Utilities----------------------------
 	
 	private String timestampToString(long timestamp){
		
	 	Date date=new Date(timestamp);
	 	
	 	SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	 	String asd=format.format(date);
	 	
	 	Log.d(TAG, "timestampToString timestamp="+timestamp+" asd="+asd);
	 	
	 	return asd;
	}
 	
 	private long stringToTimestamp(String date_time_string){
		
	 	long timestamp=Timestamp.valueOf(date_time_string).getTime();
	 	
	 	Log.d(TAG, "stringToTimestamp string="+date_time_string+" timestamp="+timestamp);
	 	
	 	return timestamp;
	}

}
