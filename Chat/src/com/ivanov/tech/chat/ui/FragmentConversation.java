package com.ivanov.tech.chat.ui;


import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.ivanov.tech.chat.R;
import com.ivanov.tech.chat.multipletypesadapter.ItemHolderConversationDate;
import com.ivanov.tech.chat.multipletypesadapter.ItemHolderConversationLeft;
import com.ivanov.tech.chat.multipletypesadapter.ItemHolderConversationNotification;
import com.ivanov.tech.chat.multipletypesadapter.ItemHolderConversationRight;
import com.ivanov.tech.multipletypesadapter.cursoradapter.CursorMultipleTypesAdapter;
import com.ivanov.tech.profile.Profile;
import com.ivanov.tech.profile.provider.DBContentProvider;
import com.ivanov.tech.session.Session;

/**
 * Created by Igor on 09.05.15.
 */
public abstract class FragmentConversation extends SherlockDialogFragment implements OnScrollListener, OnClickListener, OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor>{

    private static final String TAG = FragmentConversation.class
            .getSimpleName();    

    private static final int CONTEXT_MENU_GROUP_ID = 1;
    
	public static final int LOADER_USERS = 10;
    
    protected static final int TYPE_LEFT = 0;
    protected static final int TYPE_RIGHT =1;
    protected static final int TYPE_DATE =2;
    protected static final int TYPE_NOTIFICATION =3;
          
    protected ListView listview;
    protected CursorMultipleTypesAdapter adapter=null;
        
    protected Button button_send,button_smile;
    protected EditText edittext;
	
    //To check if scrollviews bottom is reached
    protected boolean bottomreached=true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setHasOptionsMenu(true);
        
        getLoaderManager().initLoader(LOADER_USERS, null, this);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = null;
        
        view=inflater.inflate(R.layout.conversation, container, false);
                
        button_send = (Button) view.findViewById(R.id.conversation_button_send);
        button_send.setOnClickListener(this);
        
        button_smile = (Button) view.findViewById(R.id.conversation_button_smile);
        button_smile.setOnClickListener(this);
        
        edittext=(EditText)view.findViewById(R.id.conversation_edittext);
       
        adapter=new CursorMultipleTypesAdapter(getActivity(),null,adapter.FLAG_AUTO_REQUERY);
        
        //Prepare map of types and set listeners for them. There are different ways in which you can define ItemHolder      
        adapter.addItemHolder(TYPE_LEFT, new ItemHolderConversationLeft(getActivity(),this,this));                
        adapter.addItemHolder(TYPE_RIGHT, new ItemHolderConversationRight(getActivity(),this,this));       
        adapter.addItemHolder(TYPE_DATE, new ItemHolderConversationDate(getActivity(),this));
        adapter.addItemHolder(TYPE_NOTIFICATION, new ItemHolderConversationNotification(getActivity(),this));
                
        
        listview = (ListView)view.findViewById(R.id.conversation_listview);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(adapter);
        
        listview.setOnScrollListener(this);        
        registerForContextMenu(listview);
              
        adapter.changeCursor(createMergeCursor());
        
        return view;
    }
    
    @Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		
	}
    
    @Override
	public void onClick(View v) {
		
    	if(v.getTag(R.id.conversation_item_right_imageview_failed)!=null){
    		
    		int position=(Integer)v.getTag(R.id.conversation_item_right_imageview_failed);
    		
    		adapter.getCursor().moveToPosition(position);
    		JSONObject json;
			try {
				json = new JSONObject(adapter.getValue(adapter.getCursor()));
			
	    		int messageid=json.getInt("message_id");
		        String text=json.getJSONObject("message").getString("text");
		        
	    		resendMessage(messageid, text);
			} catch (JSONException e) {
				Log.e(TAG, "onClick JSONException e="+e);
			}
			
    	}
    	
    	if(v.getTag(R.id.conversation_order_right_imageview_failed)!=null){
    		
    		int position=(Integer)v.getTag(R.id.conversation_order_right_imageview_failed);
    		
    		Log.e(TAG, "onClick conversation_order_right_imageview_failed Not implemented yet");
			
    	}
    	
    	if(v.getTag(R.string.conversation_adapter_icon_tag)!=null){
    		int user_id=(Integer)v.getTag(R.string.conversation_adapter_icon_tag);    		
    		showUserProfile(user_id);
    		
    		return;
    	}    	
    	
    	if(v.getId()==button_send.getId()){
    		String text=edittext.getText().toString();
    		
    		//Callback on abstract method
    		sendMessage(text);
    		
    		edittext.setText("");
    		
    		scrollDown();
    		
//    		InputMethodManager inputManager = 
//    		        (InputMethodManager) getActivity().
//    		            getSystemService(Context.INPUT_METHOD_SERVICE); 
//    		inputManager.hideSoftInputFromWindow(
//    				getActivity().getCurrentFocus().getWindowToken(),
//    		        InputMethodManager.HIDE_NOT_ALWAYS); 
    	}    
    	
    	if(v.getId()==button_smile.getId()){
    		Toast.makeText(getActivity(), "Sorry, function is unavailable on this version", Toast.LENGTH_LONG).show();
    	}   
    	
	}
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        
        menu.add(CONTEXT_MENU_GROUP_ID, R.id.conversation_context_menu_copy, 1, R.string.conversation_context_menu_copy);
		menu.add(CONTEXT_MENU_GROUP_ID, R.id.conversation_context_menu_delete, 2, R.string.conversation_context_menu_delete);
        
    }
    
    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
    	
    	if (item.getGroupId() == CONTEXT_MENU_GROUP_ID) {
    	
	        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	        
	        adapter.getCursor().moveToPosition(info.position);
	        
	        JSONObject json;
			try {
				json = new JSONObject(adapter.getValue(adapter.getCursor()));
			
		    	int messageid=json.getInt("message_id");
		    	
		        if(item.getItemId() ==R.id.conversation_context_menu_copy){	        		
		        	String value=json.getJSONObject("message").getString("text");
		            copyMessage(value);            	
		            return true;
		        }
		        else if(item.getItemId() ==R.id.conversation_context_menu_delete){
		            deleteMessage(messageid);
		            return true;
		        }        
	        
			} catch (JSONException e) {
				Log.e(TAG, "onContextItemSelected JSONException e="+e);	
			}
    	}
    	
		return super.onContextItemSelected(item);
    }
    
    @Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {}
    
    @Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    	try{
    		bottomreached=(listview.getLastVisiblePosition() == adapter.getCount() - 1
    				&& listview.getChildAt(listview.getChildCount() - 1).getBottom() <= listview.getHeight());
    	}catch(NullPointerException e){
    		
    	}
			
	}
    
    //------------Conversation Building---------------------------------
    
    protected abstract Cursor createMergeCursor();
    
    //-----------------WebSocket---------------------------------
    
    protected abstract void sendMessage(String text);
    
    protected abstract void resendMessage(int messageId,String text); //It depends on DB Table (whether Private or Group)
   
    //-----------------Utilities--------------
    
    public boolean isBottomReachedAndIdle(){
    	return ( (bottomreached));
    }
    
    public void scrollDown(){
    	
    	bottomreached=true;
    	
    	listview.postDelayed(new Runnable() {
		    public void run() {
		    	listview.setSelection(adapter.getCount());
		    }
		}, 100L);
    }
    
    protected abstract void deleteMessage(int messageId);//It depends on DB Table (whether Private or Group)
        
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    void copyMessage(String value){
    	
    	int sdk = android.os.Build.VERSION.SDK_INT;
    	if(sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
    	    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
    	    clipboard.setText(value);
    	} else {
    	    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE); 
    	    android.content.ClipData clip = android.content.ClipData.newPlainText(value,value);
    	    clipboard.setPrimaryClip(clip);
    	}
    	
    	Toast.makeText(getActivity(), "Copied to clipboard", Toast.LENGTH_SHORT).show();
    	
    }
    
    void showUserProfile(int user_server_id){
    	
    	if((user_server_id==Session.getUserId())){
    		Profile.showMe(getActivity(), getFragmentManager(), R.id.main_container);
    	}else{
    		Profile.showDetailsUser(user_server_id, getActivity(), getFragmentManager(), R.id.main_container);        		
    	}
    	
    }
 
    //--------DateTime Utils------------------------
	
  	protected String timestampToString(long timestamp){
  		
  	 	Date date=new Date(timestamp);
  	 	
  	 	SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  	 	String asd=format.format(date);
  	 	
  	 	//Log.d(TAG, "timestampToString timestamp="+timestamp+" asd="+asd);
  	 	
  	 	return asd;
  	}
  	
  	protected String getSmartString(long timestamp){
  		Date date=new Date(timestamp);
  		Date now=new Date();
  		
  		SimpleDateFormat format;
  		
  		// 7/31/97 7:08 AM
  		if(timestamp>now.getTime()){
  			format=new SimpleDateFormat("M/d/yy K:mm a");
  			return format.format(date);
  		}
  		
  		//Today 2:00 PM
  		if(DateUtils.isToday(timestamp)){
  			format=new SimpleDateFormat("K:mm a");
  			return format.format(date);
  		}
  		
  		//Yesterday 1:28 AM
  		if(now.getTime()-date.getTime()<24*60*60*1000){
  			format=new SimpleDateFormat("K:mm a");
  			return "Yesterday "+format.format(date);
  		}
  			
  		//Wed 1:28 AM
  		if(now.getTime()-date.getTime()<7*24*60*60*1000){
  			format=new SimpleDateFormat("E K:mm a");
  			return format.format(date);
  		}
  		
  		// 7/31/97 7:08 AM
  		format=new SimpleDateFormat("yy/M/d K:mm a");
  		return format.format(date);
  	}
  	 	
  	protected long stringToTimestamp(String date_time_string){
  		
  	 	long timestamp=Timestamp.valueOf(date_time_string).getTime();
  	 	
  	 	//Log.d(TAG, "stringToTimestamp string="+date_time_string+" timestamp="+timestamp);
  	 	
  	 	return timestamp;
  	}
  	
  	//------------Loader<Users>-------------------
  	
  	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
	    	
		switch(id) {
            case LOADER_USERS:
            	          
                Log.d(TAG, "onCreateLoader LOADER_USERS");
                
                CursorLoader cursorLoader = new CursorLoader(getActivity(),
                DBContentProvider.URI_USER, null, null, null, null);
                
                return cursorLoader;                
	    }
		
		return null;				
	}
	
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
	    	    
		switch(loader.getId()){            
	        case LOADER_USERS:    
	        	Log.d(TAG, "onLoadFinished LOADER_USERS");
	        	
	        	//На случай если вдруг изменилась иконка или имя собеседника
	        	adapter.changeCursor(createMergeCursor());
	            	
	            break;            
	    }
	        
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		  
	}
}
