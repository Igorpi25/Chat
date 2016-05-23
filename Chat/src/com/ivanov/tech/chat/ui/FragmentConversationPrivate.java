package com.ivanov.tech.chat.ui;

import java.net.URI;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.codebutler.android_websockets.WebSocketClient;
import com.codebutler.android_websockets.WebSocketClient.Listener;
import com.codebutler.android_websockets.WebSocketClient.OutgoingListener;
import com.ivanov.tech.chat.Chat;
import com.ivanov.tech.chat.R;
import com.ivanov.tech.chat.provider.DBContentProvider;
import com.ivanov.tech.chat.provider.DBContract;
import com.ivanov.tech.chat.reciever.MessageReciever;
import com.ivanov.tech.chat.service.ChatService;
import com.ivanov.tech.chat.service.TransportChat;
import com.ivanov.tech.profile.Profile;
import com.ivanov.tech.session.Session;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class FragmentConversationPrivate extends FragmentConversation implements LoaderManager.LoaderCallbacks<Cursor> {

	private static final String TAG = FragmentConversationPrivate.class
            .getSimpleName();    

	public static final int LOADER_PRIVATE = 12;
    
    public static final long DATE_SPAN=60*30*1000;//30 minites in milliseconds
    
    public static final String JSON_TRANSPORT="transport";
    public static final String JSON_VALUE="value";
    public static final String JSON_DATE="date";
    public static final String JSON_MESSAGE_ID="message_id";
    public static final String JSON_INTERLOCUTOR_ID="interlocutor_id";
    
    public int interlocutor_id=0;//user_id with whom you talking
    
    public Cursor privatecursor=null;
    
    public MenuItem menuDetails;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setHasOptionsMenu(true);
        
        getLoaderManager().initLoader(LOADER_PRIVATE, null, this);
    }

    public static FragmentConversationPrivate newInstance(int user_id){
    	FragmentConversationPrivate f=new FragmentConversationPrivate();    	
    	f.interlocutor_id=user_id;
		return f;
    }
    
//------------Conversation Building---------------------------------
	
    @Override
	protected Cursor createMergeCursor() {
    	if(privatecursor==null)return null;
    	
    	MatrixCursor matrixcursor=new MatrixCursor(new String[]{adapter.COLUMN_ID, adapter.COLUMN_TYPE, adapter.COLUMN_KEY, adapter.COLUMN_VALUE});    	
    	
    	if((privatecursor.getCount()<1))return matrixcursor;
    	privatecursor.moveToFirst();
    	    	    	    	
		int _id=1;
		long last_date=0;
		
		int type;
		String user_name=" ",user_status=" ",user_path_icon;
		
		privatecursor.moveToPosition(-1);
        while( privatecursor.moveToNext() ){
        	try{
	        	//Log.d(TAG, "getConversationCursor getDate="+getDate()+" getDate()-last_date="+(getDate()-last_date));        	
	        	if(getDate()-last_date>DATE_SPAN)
	        	{
	        		Log.d(TAG, "getConversationCursor getDate="+getDate());   
	        		type=TYPE_DATE;
		        	user_name=" ";
		        	user_status=" ";
		        	user_path_icon=" ";	  
		        	
	
		        	JSONObject json=new JSONObject("{date:{text:'"+getSmartString(getDate())+"'}}");   
		        	matrixcursor.addRow(new Object[]{++_id,TYPE_DATE,0,json.toString()});
		        	
	        	}
	        	
	        	last_date=getDate();
	        	
	        	switch(getMessage()){   
	        	
		        	case Chat.TRANSPORT_TEXT:{
		        	
			        	type=( getDirection()==DBContract.Private.DIRECTION_INCOMING )?TYPE_LEFT:TYPE_RIGHT;
			        	user_name=null;//Hides user's name in Private conversation
			        	//user_name=usersmap.get(getUserId()).get(USERSMAP_NAME);
			        	//user_status=usersmap.get(getUserId()).get(USERSMAP_STATUS);
			        	
			        	JSONObject json=new JSONObject("{userid:"+getUserId()+", message_id:"+getPrivateMessageId()+" status:"+getStatus()+", message:{text:'"+getValue()+"'}, icon:{visible:true, image_url:'"+getIcon()+"'}, name:{visible:false}}");   
			        	matrixcursor.addRow(new Object[]{++_id,type,0,json.toString()});
			        	
			        	
		        	}break;
		        	
		        	case Chat.TRANSPORT_NOTIFICATION:{
			        	
			        	type=TYPE_NOTIFICATION;
			        	user_name=" ";
			        	user_status=" ";
			        	user_path_icon=" ";
			        				        	
			        	JSONObject json=new JSONObject("{date:{text:'"+getValue()+"'}}");   
			        	matrixcursor.addRow(new Object[]{++_id,TYPE_DATE,0,json.toString()});
			        	
			        	
		        	}break;	        
	        	}
        	}catch(JSONException e){
        		Log.e(TAG, "createMergeCursor JSONException e="+e);
        	}
        }
        
        makePrivateIncomingMessagesRead(interlocutor_id);
		
		return matrixcursor;
	}
    
//------------Notification--------
    
    @Override
    public void onResume() {
      super.onResume();
      
      //Set MESSAGE broadcast reciever to prevent notification of current user
      IntentFilter filter=new IntentFilter(TransportChat.BROADCAST_MESSAGE);      
      filter.setPriority(2);
      getActivity().registerReceiver(recievermessage, filter);
      
      
      //If there is message already on status-bar, then cancel it
      NotificationManager notificationmanager =
  		    (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
      notificationmanager.cancel(10*interlocutor_id+MessageReciever.REST_PRIVATE);

    }
    
    @Override
    public void onPause() {
      super.onPause();
      
      //Unset MESSAGE bradcast reciever
      getActivity().unregisterReceiver(recievermessage);
    }
    
    protected BroadcastReceiver recievermessage=new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
        	Log.d(TAG, "recievermessage.onReceive intent="+intent.getExtras().toString());
        	if(intent.hasExtra(MessageReciever.EXTRA_TYPE)){
            	//If we are inside of the required chat already, then abort notification 
        		if( (intent.getIntExtra(MessageReciever.EXTRA_TYPE, 0)==MessageReciever.TYPE_PRIVATE) && (intent.getIntExtra(MessageReciever.EXTRA_USERID, 0)==interlocutor_id) ){
            		//Preventing MessageReciever-notification
        			abortBroadcast();
        			
        			//Play default sound
        			playSoundMessage();
            	}
        	}
        	
        	
        	
        }
    };
    
    protected void playSoundMessage(){
    	try {
    	    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    	    
    	    Ringtone r = RingtoneManager.getRingtone(getActivity().getApplicationContext(), notification);
    	    r.play();
    	} catch (Exception e) {
    	    e.printStackTrace();
    	}
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        
		menu.clear();
		
		menuDetails=menu.add(Menu.NONE, R.id.menu_details_private, Menu.NONE,R.string.menu_details);
        menuDetails.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menuDetails.setIcon(R.drawable.ic_menu_private);
        
        getSherlockActivity().getSupportActionBar().show();
        getSherlockActivity().getSupportActionBar().setHomeButtonEnabled(true);
        getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSherlockActivity().getSupportActionBar().setTitle("Pivate conversation");
    }
	    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	   
		int id = item.getItemId();
	     
	
		if(id==menuDetails.getItemId()){
			
			showUserProfile(interlocutor_id);
			
			return true;
		}
		
		return false;
	}
    
//-----------------Context Menu methods--------------
        
    @Override
    protected void deleteMessage(int messageId){
    	Uri uri=DBContentProvider.URI_PRIVATE;   		
   		
    	if(getActivity().getContentResolver().delete(uri, DBContract.Private._ID+" = "+messageId, null)>0)
   			Toast.makeText(getActivity(), "Message has been removed", Toast.LENGTH_SHORT).show();
    	else 
    		Toast.makeText(getActivity(), "Message is not removed", Toast.LENGTH_SHORT).show();
    }
    
//------------Make incoming messages of user read------------------------------
   	
    void makePrivateIncomingMessagesRead(int user_id) {
  		
   		//Log.e(TAG, "makePrivateIncomingMessagesRead interlocutor_id="+interlocutor_id);
   		
   		Uri uri=Uri.parse(DBContentProvider.URI_PRIVATE+"/"+user_id+"/makeread");
   		
   		getActivity().getContentResolver().update(uri,null,null,null);
   	}	
    
//--------------WebSocket-------------------------
    
	@Override
	protected void sendMessage(String text) {
		
		Log.d(TAG, "sendMessage text="+text);
		
		JSONObject json=new JSONObject();
	    try {		
	    	json.put(JSON_TRANSPORT, Chat.TRANSPORT_TEXT);
	    	json.put(JSON_VALUE, text);	    	
	    	json.put(JSON_INTERLOCUTOR_ID, interlocutor_id);
							    
	    } catch (JSONException e) {
	    	Log.d(TAG,"sendMessage JSONException e="+e);
		}
	    
	    Intent intent=new Intent(getActivity().getApplicationContext(),ChatService.class);
	    intent.putExtra("userid", Session.getUserId());
	    intent.putExtra("transport", Chat.TRANSPORT_TEXT);
	    intent.putExtra("json", json.toString());
		
		getActivity().startService(intent);
	}
	
	@Override
	protected void resendMessage(int messageId,String text){
		
		Log.d(TAG, "resendMessage messageId="+messageId+" text="+text);
		
		JSONObject json=new JSONObject();
	    try {		
	    	json.put(JSON_TRANSPORT, Chat.TRANSPORT_TEXT);
	    	json.put(JSON_VALUE, text);	    	
	    	json.put(JSON_INTERLOCUTOR_ID, interlocutor_id);
	    	json.put(JSON_MESSAGE_ID, messageId); 
							    
	    } catch (JSONException e) {
	    	Log.d(TAG,"resendMessage JSONException e="+e);
		}
	    
	    Intent intent=new Intent(getActivity().getApplicationContext(),ChatService.class);
	    intent.putExtra("userid", Session.getUserId());
	    intent.putExtra("transport", Chat.TRANSPORT_TEXT);
	    intent.putExtra("json", json.toString());
		
		getActivity().startService(intent);
	}
		
	public int getUserId(){
		int user_id=0;
		
		if(getDirection()==DBContract.Private.DIRECTION_INCOMING){
			user_id=privatecursor.getInt(privatecursor.getColumnIndex(DBContract.Private.COLUMN_NAME_USER_ID));
		}else {
			user_id=Session.getUserId();
		}
		
    	return user_id;
    }
	
	public int getStatus(){
    	return privatecursor.getInt(privatecursor.getColumnIndex(DBContract.Private.COLUMN_NAME_STATUS));
    }
	
	public int getDirection(){
    	return privatecursor.getInt(privatecursor.getColumnIndex(DBContract.Private.COLUMN_NAME_DIRECTION));
    }
	
	public int getMessage(){
    	return privatecursor.getInt(privatecursor.getColumnIndex(DBContract.Private.COLUMN_NAME_MESSAGE));
    }
	
	public String getValue(){
    	return privatecursor.getString(privatecursor.getColumnIndex(DBContract.Private.COLUMN_NAME_VALUE));
    }
	
	public long getDate(){
    	String date_time_string=privatecursor.getString(privatecursor.getColumnIndex(DBContract.Private.COLUMN_NAME_DATE));
    	
    	long timestamp=stringToTimestamp(date_time_string);
    	
    	return timestamp;
    }

	public int getPrivateMessageId(){
		return privatecursor.getInt(privatecursor.getColumnIndex(DBContract.Private._ID));		   
	}
	
	public String getIcon(){
    	return privatecursor.getString(privatecursor.getColumnIndex("icon"));
    }
	
	public String getName(){
    	return privatecursor.getString(privatecursor.getColumnIndex("name"));
    }

	//------------Loader<Private>-------------------
    
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
	    
		switch(id) {
            case LOADER_PRIVATE:
            	String[] projection=null;
                Uri uri=null;

                projection = new String[]{
                        DBContract.Private._ID,
                        DBContract.Private.COLUMN_NAME_USER_ID,
                        DBContract.Private.COLUMN_NAME_STATUS,
                        DBContract.Private.COLUMN_NAME_DIRECTION,                        
                        DBContract.Private.COLUMN_NAME_MESSAGE,
                        DBContract.Private.COLUMN_NAME_VALUE,
                        DBContract.Private.COLUMN_NAME_DATE,
                        "icon",
                        "name"
                        
                };
                uri = Uri.parse(DBContentProvider.URI_PRIVATE+"/"+interlocutor_id);
                                
               // Log.d(TAG, "onCreateLoader LOADER_PRIVATE uri="+uri.toString());
                
                CursorLoader cursorLoader = new CursorLoader(getActivity(),
                        uri, projection, null, null, null);
                
                return cursorLoader;                
	    }
		
		return super.onCreateLoader(id, args);//LOADER_USER		
	}
	    
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
	    
		super.onLoadFinished(loader, data);//LOADER_USER
		
		switch(loader.getId()){            
	        case LOADER_PRIVATE:    
	        	//Log.d(TAG, "onLoadFinished LOADER_PRIVATE");
	        	
	        	privatecursor=data;
	        	
	        	//Build conversationcursor and swap adapter 
	        	adapter.changeCursor(createMergeCursor());
	            	
	            break;            
	    }
	        
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		  
	}
	
}
