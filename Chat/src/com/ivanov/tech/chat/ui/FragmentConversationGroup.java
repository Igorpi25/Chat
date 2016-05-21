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

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
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

public class FragmentConversationGroup extends FragmentConversation{

	private static final String TAG = FragmentConversationGroup.class
            .getSimpleName();    

	public static final int LOADER_GROUP = 13;
    
	public static final long DATE_SPAN=60*30*1000;//30 minites in milliseconds
    
	public static final String JSON_TRANSPORT="transport";
	public static final String JSON_VALUE="value"; 
	public static final String JSON_GROUP_ID="group_id";
	public static final String JSON_SENDER="sender";
	public static final String JSON_MESSAGE_ID="message_id";
        
	public int group_id=0;//user_id with whom you talking
    
	public Cursor groupcursor=null;
    
	public MenuItem menuDetails;    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setHasOptionsMenu(true);
        
        getLoaderManager().initLoader(LOADER_GROUP, null, this);
    }
    
    public static FragmentConversationGroup newInstance(int group_id){
    	FragmentConversationGroup f=new FragmentConversationGroup();
    	f.group_id=group_id;
		return f;
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        
		menu.clear();
		
		menuDetails=menu.add(Menu.NONE, R.id.menu_details_group, Menu.NONE,R.string.menu_details);
        menuDetails.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menuDetails.setIcon(R.drawable.ic_menu_group);
		 
        getSherlockActivity().getSupportActionBar().show();
        getSherlockActivity().getSupportActionBar().setHomeButtonEnabled(true);
        getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSherlockActivity().getSupportActionBar().setTitle("Group conversation");
    }
	    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	   
		int id = item.getItemId();
	     
		if(id==menuDetails.getItemId()){
			Profile.showGroup(group_id, getActivity(), getFragmentManager(), R.id.main_container);
			return true;
		}
		
		return false;
	}
    
	//-------------Conversation bulding-------------------
	
	@Override
	protected Cursor createMergeCursor() {
		
		if(groupcursor==null)return null;
		
		MatrixCursor matrixcursor=new MatrixCursor(new String[]{adapter.COLUMN_ID, adapter.COLUMN_TYPE, adapter.COLUMN_KEY, adapter.COLUMN_VALUE});    	
    	
    	if((groupcursor.getCount()<1))return matrixcursor;
    	groupcursor.moveToFirst();
    	
		int _id=1;
		long last_date=0;
				
		groupcursor.moveToPosition(-1);
        loop:while( groupcursor.moveToNext() ){
        	
        	try{
        	
	        	if(getDate()-last_date>DATE_SPAN)
	        	{
	        		Log.d(TAG, "getConversationCursor getDate="+getDate());  
	
		        	JSONObject json=new JSONObject("{date:{text:'"+getSmartString(getDate())+"'}}");   
		        	matrixcursor.addRow(new Object[]{++_id,TYPE_DATE,0,json.toString()});
		        	
	        	}
	        	
	        	last_date=getDate();
	        	
	        	
	        	
	        	switch(getMessage()){   
	        	
		        	case Chat.TRANSPORT_TEXT:{
		        	
			        	int type=( (getUserId()!=Session.getUserId()) )?TYPE_LEFT:TYPE_RIGHT;
			        				        	
			        	JSONObject json=new JSONObject("{userid:"+getUserId()+", message_id:"+getGroupMessageId()+" status:"+getStatus()+", message:{text:'"+getValue()+"'}, name:{text:'"+getName()+"'}, icon:{visible:true, image_url:'"+getIcon()+"'}, name:{visible:false}}");   
			        	matrixcursor.addRow(new Object[]{++_id,type,0,json.toString()});
			        	
		        	}break;
		        	
		        	case Chat.TRANSPORT_NOTIFICATION:{
			        				        				        	
			        	JSONObject json=new JSONObject("{date:{text:'"+getValue()+"'}}");   
			        	matrixcursor.addRow(new Object[]{++_id,TYPE_NOTIFICATION,0,json.toString()});
			        	
			        	
		        	}break;	
	        	}
	        	
	    	}catch(JSONException e){
	    		Log.e(TAG, "createMergeCursor JSONException e="+e);
	    	}
    	}
        
        
        makeGroupIncomingMessagesRead(group_id);
		
		return matrixcursor;
	}	
	
	//------------------context Menu Methods---------------------
	
	@Override
    protected void deleteMessage(int messageId){
    	Uri uri=DBContentProvider.URI_GROUP;   		
   		
    	if(getActivity().getContentResolver().delete(uri, DBContract.Group._ID+" = "+messageId, null)>0)
   			Toast.makeText(getActivity(), "Message has been deleted", Toast.LENGTH_SHORT).show();
    	else 
    		Toast.makeText(getActivity(), "Message is not deleted", Toast.LENGTH_SHORT).show();
    }
	
	//------------Make incoming messages of user read------------------------------
   	
	protected void makeGroupIncomingMessagesRead(int group_id) {
  		
   		Log.e(TAG, "makeGroupIncomingMessagesRead group_id="+group_id);
   		
   		Uri uri=Uri.parse(DBContentProvider.URI_GROUP+"/"+group_id+"/makeread");
   		
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
	    	json.put(JSON_GROUP_ID, group_id);
	    	json.put(JSON_SENDER, Session.getUserId());
							    
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
	    	json.put(JSON_GROUP_ID, group_id);
	    	json.put(JSON_SENDER, Session.getUserId());
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
	
	//------------Notification---------------------
    
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
      notificationmanager.cancel(10*group_id+MessageReciever.REST_GROUP);      
      
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
        		if( (intent.getIntExtra(MessageReciever.EXTRA_TYPE, 0)==MessageReciever.TYPE_GROUP) && (intent.getIntExtra(MessageReciever.EXTRA_GROUPID, 0)==group_id) ){
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

    //----------------------------------------------

	public int getUserId(){
		int user_id=0;
		
		user_id=groupcursor.getInt(groupcursor.getColumnIndex(DBContract.Group.COLUMN_NAME_SENDER));
				
    	return user_id;
    }
	
	public int getStatus(){
    	return groupcursor.getInt(groupcursor.getColumnIndex(DBContract.Group.COLUMN_NAME_STATUS));
    }
	
	public int getMessage(){
    	return groupcursor.getInt(groupcursor.getColumnIndex(DBContract.Group.COLUMN_NAME_MESSAGE));
    }
	
	public String getValue(){
    	return groupcursor.getString(groupcursor.getColumnIndex(DBContract.Group.COLUMN_NAME_VALUE));
    }
	
	public long getDate(){
    	String date_time_string=groupcursor.getString(groupcursor.getColumnIndex(DBContract.Group.COLUMN_NAME_DATE));
    	
    	long timestamp=stringToTimestamp(date_time_string);
    	
    	return timestamp;
    }
	
	public int getGroupMessageId(){
		return groupcursor.getInt(groupcursor.getColumnIndex(DBContract.Group._ID));		   
	}

	public String getIcon(){
    	return groupcursor.getString(groupcursor.getColumnIndex("icon"));
    }
	
	public String getName(){
    	return groupcursor.getString(groupcursor.getColumnIndex("name"));
    }
	
	//------------------------Loader<Group>--------------------------
	

//------------Loader<Group>-------------------
    
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
	    	
		switch(id) {
            case LOADER_GROUP:
            	String[] projection=null;
                Uri uri=null;

                projection = new String[]{
                        DBContract.Group._ID,
                        DBContract.Group.COLUMN_NAME_GROUP_ID,
                        DBContract.Group.COLUMN_NAME_SENDER,
                        DBContract.Group.COLUMN_NAME_STATUS,                       
                        DBContract.Group.COLUMN_NAME_MESSAGE,
                        DBContract.Group.COLUMN_NAME_VALUE,
                        DBContract.Group.COLUMN_NAME_DATE,
                        "icon",
                        "name"
                        
                };
                uri = Uri.parse(DBContentProvider.URI_GROUP+"/"+group_id);
                                
                Log.d(TAG, "onCreateLoader LOADER_GROUP uri="+uri.toString());
                
                CursorLoader cursorLoader = new CursorLoader(getActivity(),
                        uri, projection, null, null, null);
                
                return cursorLoader;                
	    }		
		
	    return super.onCreateLoader(id, args);//LOADER_USER in FragmentConversation
	}
	    
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
	    
		super.onLoadFinished(loader, data);//LOADER_USER in FragmentConversation
		
		Log.d(TAG, "onLoadFinished loaderid="+loader.getId());
	    
		switch(loader.getId()){            
	        case LOADER_GROUP:    
	        	Log.d(TAG, "onLoadFinished LOADER_GROUP");
	        	
	        	groupcursor=data;
	        	
	        	//Build conversationcursor and swap adapter 
	        	adapter.changeCursor(createMergeCursor());
	            	
	            break;            
	    }
	        
	}
	

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		super.onLoaderReset(loader);//LOADER_USER in FragmentConversation
	    	
	    Log.d(TAG, "LOADER_GROUP onLoaderReset");    
	}

	

}
