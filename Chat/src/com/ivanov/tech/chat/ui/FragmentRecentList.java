package com.ivanov.tech.chat.ui;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.bumptech.glide.Glide;
import com.ivanov.tech.chat.Chat;
import com.ivanov.tech.chat.R;
import com.ivanov.tech.chat.multipletypesadapter.ItemHolderConversationDate;
import com.ivanov.tech.chat.multipletypesadapter.ItemHolderConversationLeft;
import com.ivanov.tech.chat.multipletypesadapter.ItemHolderConversationNotification;
import com.ivanov.tech.chat.multipletypesadapter.ItemHolderConversationRight;
import com.ivanov.tech.chat.multipletypesadapter.ItemHolderRecentList;
import com.ivanov.tech.chat.provider.DBContentProvider;
import com.ivanov.tech.chat.provider.DBContract;
import com.ivanov.tech.multipletypesadapter.cursoradapter.CursorMultipleTypesAdapter;
import com.ivanov.tech.profile.Profile;
import com.ivanov.tech.profile.Profile.CreateGroupResultListener;
import com.ivanov.tech.profile.ui.FragmentSelectUsers;
import com.ivanov.tech.session.Session;


public class FragmentRecentList extends SherlockDialogFragment implements LoaderManager.LoaderCallbacks<Cursor>, OnClickListener, OnItemClickListener {
	
	private static final String TAG = FragmentRecentList.class
            .getSimpleName();    

	protected static final int LOADER_USER = 1;
	protected static final int LOADER_GROUP = 2;
    protected static final int LOADER_RECENTLIST = 3;
        
    protected static final int TYPE_PRIVATE=0;
    protected static final int TYPE_GROUP=1;
    
    //COLUMN_STATUS values
  	public static final int STATUS_SENT = 0;
  	public static final int STATUS_PROCESSED = 1;
  	public static final int STATUS_FAILED = 2;

    protected ListView listview;
    protected CursorMultipleTypesAdapter adapter=null;
    protected Cursor cursor_recent;
        
    protected SubMenu menuAdd;
    protected MenuItem menuAddGroup;
    protected MenuItem menuAddContact; 
    
    protected View bottom_menu_recent=null;
    protected View bottom_menu_contacts=null;
    protected View bottom_menu_me=null;
    
    protected View recent_empty=null;
             
    public static FragmentRecentList newInstance() {
    	FragmentRecentList f = new FragmentRecentList();   
    	
        return f;
    }    
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = null;
        view = inflater.inflate(R.layout.recent, container, false);
        
        recent_empty=view.findViewById(R.id.recent_empty);
        
        listview=(ListView)view.findViewById(R.id.recent_listview);
        
        bottom_menu_recent=view.findViewById(R.id.recent_recent);
        bottom_menu_contacts=view.findViewById(R.id.recent_contacts);
        bottom_menu_me=view.findViewById(R.id.recent_me);
        
        bottom_menu_recent.setOnClickListener(this);
        bottom_menu_contacts.setOnClickListener(this);
        bottom_menu_me.setOnClickListener(this);
        
        adapter=new CursorMultipleTypesAdapter(getActivity(),null,adapter.FLAG_AUTO_REQUERY);
        
        //Prepare map of types and set listeners for them. There are different ways in which you can define ItemHolder      
        adapter.addItemHolder(TYPE_PRIVATE, new ItemHolderRecentList(getActivity(),this,this));                
        adapter.addItemHolder(TYPE_GROUP, new ItemHolderRecentList(getActivity(),this,this)); 
                
        listview = (ListView)view.findViewById(R.id.recent_listview);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(adapter);
        adapter.changeCursor(createMergeCursor());
        
        return view;
    }
    
    @Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    	
    	adapter.getCursor().moveToPosition(position);
    	
    	switch(adapter.getType(adapter.getCursor())){
		
			case TYPE_PRIVATE:{
				int user_id=adapter.getKey(adapter.getCursor());
				
				if(Session.getUserId()==user_id)
					Profile.showDetailsUser(user_id, getActivity(), getFragmentManager(), R.id.main_container);
				else
					Chat.showConversationPrivate(user_id, getActivity(), getFragmentManager(), R.id.main_container);
			}break;
			
			case TYPE_GROUP:{
				int group_id=adapter.getKey(adapter.getCursor());
				
				Chat.showConversationGroup(group_id, getActivity(), getFragmentManager(), R.id.main_container);
				
			}break;
		
		}
	}
	
	@Override
	public void onClick(View v) {
		
		if(v.getId()==bottom_menu_recent.getId()){
			//Nothing спец-заглушка
			return;
		}
		
		if(v.getId()==bottom_menu_contacts.getId()){
			Chat.showContacts(getActivity(), getFragmentManager(), R.id.main_container,false);
			return;
		}		
		
		if(v.getId()==bottom_menu_me.getId()){
			Chat.showMe(getActivity(), getFragmentManager(), R.id.main_container, false);
			return;
		}

	} 
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setHasOptionsMenu(true);
        
        getLoaderManager().initLoader(LOADER_RECENTLIST, null, this);
        getLoaderManager().initLoader(LOADER_USER, null, this);
        getLoaderManager().initLoader(LOADER_GROUP, null, this);

    }
		
	@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        
		menu.clear();
		 
		menuAdd = menu.addSubMenu(R.id.menu_add, Menu.NONE, 1, R.string.menu_add).setIcon(R.drawable.ic_menu_add);
		menuAdd.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		
		menuAddContact=menuAdd.add(Menu.NONE, R.id.menu_add_contact, Menu.NONE,R.string.menu_add_contact);
		menuAddContact.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		menuAddContact.setIcon(R.drawable.ic_menu_add_contact);
		
		menuAddGroup=menuAdd.add(Menu.NONE, R.id.menu_add_group, Menu.NONE,R.string.menu_add_group);
		menuAddGroup.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		menuAddGroup.setIcon(R.drawable.ic_menu_add_group);
		
        getSherlockActivity().getSupportActionBar().show();
        getSherlockActivity().getSupportActionBar().setHomeButtonEnabled(false);
        getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSherlockActivity().getSupportActionBar().setTitle(R.string.app_name);
    }
	    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	   
		int id = item.getItemId();
	     
		if(id==menuAddGroup.getItemId()){
			addGroup();
			return true;
		}
		
		if(id==menuAddContact.getItemId()){
			addContact();
			return true;
		}
		
		return false;
	}
		
	//------------Preparing cursor---------------------------	
		
	protected Cursor createMergeCursor(){
		
		Log.d(TAG, "createMergeCursor");
    	
		if(cursor_recent==null)return null;
		
		if(cursor_recent.getCount()==0){
			recent_empty.setVisibility(View.VISIBLE);
		}else{
			recent_empty.setVisibility(View.INVISIBLE);
		}		
		
    	List<Cursor> cursors_list=new ArrayList<Cursor>();	
    	
    	int _id=1;
    	
    	MatrixCursor matrixcursor=getMatrixCursor(_id);
    	
    	if(matrixcursor==null)return null;
    	
    	cursors_list.add(matrixcursor);
    	
    	if(cursors_list.size()==0)return null;
    	
    	Cursor[] cursors_array=new Cursor[cursors_list.size()];
    	MergeCursor mergecursor=new MergeCursor(cursors_list.toArray(cursors_array));
    	
    	
    	return mergecursor;    	
    }
	
    protected MatrixCursor getMatrixCursor(int _id){

    	Log.d(TAG, "getMatrixCursor");
    	
    	if(cursor_recent==null)return null;
    	
    	MatrixCursor matrixcursor=new MatrixCursor(new String[]{adapter.COLUMN_ID, adapter.COLUMN_TYPE, adapter.COLUMN_KEY, adapter.COLUMN_VALUE});    	
    	
    	if((cursor_recent==null)||(cursor_recent.getCount()<1))return matrixcursor;
    	
    	Log.d(TAG, "getMatrixCursor count="+cursor_recent.getCount());
    	
    	cursor_recent.moveToFirst();    	
    	do{    	
    		
    		try{
    		
	    		String icon_url=cursor_recent.getString(cursor_recent.getColumnIndex("icon"));
	    		String name_text=getNameText();    		
	    		
	    		String date_text=getSmartString(stringToTimestamp(cursor_recent.getString(cursor_recent.getColumnIndex("date"))));
	    		boolean marker_visible=(cursor_recent.getInt(cursor_recent.getColumnIndex("status"))==DBContract.STATUS_UNREAD);
	    		int profile_id=cursor_recent.getInt(cursor_recent.getColumnIndex("profile_id"));    		
	    		
	    		switch(cursor_recent.getInt(cursor_recent.getColumnIndex("recent_type"))){
	    		
		    		case TYPE_PRIVATE :{ 
			    			
			    		String status_text=getValueText();	    		
			    		
			    		JSONObject json=new JSONObject("{profile_id:"+profile_id+", status:{text:'"+status_text+"'}, marker:{visible:"+marker_visible+", image_res:"+R.drawable.drawable_marker_red+"}, icon:{image_url:'"+icon_url+"'}, name:{text:'"+name_text+"'}, date:{text:'"+date_text+"'} }");   
			    		Log.d(TAG, "getMatrixCursor TYPE_PRIVATE "+json.toString());
			    		matrixcursor.addRow(new Object[]{++_id,TYPE_PRIVATE,profile_id,json.toString()});
			    			    		
		    		}break;
			    		
		    		case TYPE_GROUP :{ 
	
		        		String sender_name=getSenderNameText();
			    		String status_text="["+sender_name+"] "+getValueText();	    		
			    		
			    		JSONObject json=new JSONObject("{profile_id:"+profile_id+", status:{text:'"+status_text+"'}, marker:{visible:"+marker_visible+", image_res:"+R.drawable.drawable_marker_red+"}, icon:{image_url:'"+icon_url+"'}, name:{text:'"+name_text+"'}, date:{text:'"+date_text+"'} }");
			    		Log.d(TAG, "getMatrixCursor TYPE_GROUP "+json.toString());
			        	matrixcursor.addRow(new Object[]{++_id,TYPE_GROUP,profile_id,json.toString()});
			    		
		    		}break;
	    		
	    		}
    		
    		}catch(JSONException e){
    			Log.e(TAG, "getMatrixCursor JSONException e="+e);
    		}
    		
    	}while(cursor_recent.moveToNext());
    	    	
    	return matrixcursor;
    }
      
    //--------------Utilities----------------------
    
    public String getNameText(){
		Log.e(TAG, "name="+cursor_recent.getString(cursor_recent.getColumnIndex("name")).replaceAll("\"", "\\\\\"").replaceAll("\'", "\\\\\'"));
    	return cursor_recent.getString(cursor_recent.getColumnIndex("name")).replaceAll("\"", "\\\\\"").replaceAll("\'", "\\\\\'");
    }
    
    public String getValueText(){
		Log.e(TAG, "value="+cursor_recent.getString(cursor_recent.getColumnIndex("value")).replaceAll("\"", "\\\\\"").replaceAll("\'", "\\\\\'"));
    	return cursor_recent.getString(cursor_recent.getColumnIndex("value")).replaceAll("\"", "\\\\\"").replaceAll("\'", "\\\\\'");
    }
    
    public String getSenderNameText(){
		Log.e(TAG, "sender_name="+cursor_recent.getString(cursor_recent.getColumnIndex("sender_name")).replaceAll("\"", "\\\\\"").replaceAll("\'", "\\\\\'"));
    	return cursor_recent.getString(cursor_recent.getColumnIndex("sender_name")).replaceAll("\"", "\\\\\"").replaceAll("\'", "\\\\\'");
    }
    
    protected void addContact(){
    	Profile.showSearchContact(getActivity(),getActivity().getSupportFragmentManager(),R.id.main_container);
    }
    
    protected void addGroup(){
    	/*
    	 * First here we get users-list, then we send it the server to create the group of these users.
    	 * When server response us groupid, we take it and showGroup. At this point, 
    	 * server will have to send to us the group-info and group-users through the Communicator.
    	 * So when we get gropid by from response we already have the group-info and group-users list, and users-info in group
    	*/
    	
    	Profile.showSelectFriends(getString(R.string.selectusers_textview_empty), new FragmentSelectUsers.ResultListener(){

			@Override
			public void onSuccess(ArrayList<Integer> usersid) {
				//Add selected users to group. For that we create message to server
				
				Log.e(TAG, "showSelectFriends onSuccess usersid.size = "+usersid.size());
				
				JSONArray users=new JSONArray();
				try {					
					
					//List of users that have to be added to group
					for(Integer userid : usersid){
						users.put(new JSONObject().put("id", userid));
					}				
					
				} catch (JSONException e) {
					Log.e(TAG, "showSelectFriends onSuccess JSONException e = "+e);
				}
				
				//Call TransportProfile of Communicator protocol
				Profile.createGroupRequest(users, getActivity(),new CreateGroupResultListener(){

					@Override
					public void onCreated(int groupid) {
						Profile.showGroup(groupid, getActivity(), getFragmentManager(), R.id.main_container);
					}

					@Override
					public void onFailed(String message) {
						Log.e(TAG, "addGroup createGroupRequest onFailed message="+message);						
					}
					
				});
			}
			
		}, getActivity(), getFragmentManager(), R.id.main_container);
    	    	
    }
  
    //---------Date Time Utils-----------------

    protected  long stringToTimestamp(String date_time_string){
		
	 	long timestamp=Timestamp.valueOf(date_time_string).getTime();
	 	
	 	//Log.d(TAG, "stringToTimestamp string="+date_time_string+" timestamp="+timestamp);
	 	
	 	return timestamp;
	}
	
    protected  String getSmartString(long timestamp){
		Date date=new Date(timestamp);
		Date now=new Date();
		
		SimpleDateFormat format;
		
		// 7/31/97 7:08 AM
		if(timestamp>now.getTime()){
			format=new SimpleDateFormat("M/d/yy");
			return format.format(date);
		}
		
		//Today 2:00 PM
		if(DateUtils.isToday(timestamp)){
			format=new SimpleDateFormat("K:mm a");
			return format.format(date);
		}
		
		//Yesterday 1:28 AM
		if(now.getTime()-date.getTime()<24*60*60*1000){			
			return "Yesterday";
		}
			
		//Wed 1:28 AM
		if(now.getTime()-date.getTime()<7*24*60*60*1000){
			format=new SimpleDateFormat("E");
			return format.format(date);
		}
		
		// 7/31/97 7:08 AM
		format=new SimpleDateFormat("yy/M/d");
		return format.format(date);
	}
	
    //-------------Loader<Cursor>------------------
	
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    	
    	//Log.d(TAG, "onCreateLoader");

        String[] projection=null;
        Uri uri=null;

        switch(id) {
            case LOADER_RECENTLIST:
                projection = new String[]{
                		"recent_type",
                		"profile_id",
                		"sender",
                		"message",
                		"value",
                		"date",
                		"status",
                		"icon",
                		"name",
                		"sender_name"
                };
                uri = DBContentProvider.URI_RECENTLIST;
                break;
            
            case LOADER_USER:  	          
                Log.d(TAG, "onCreateLoader LOADER_USER");                
                uri = com.ivanov.tech.profile.provider.DBContentProvider.URI_USER;                
                break;              
                
            case LOADER_GROUP:    	          
                Log.d(TAG, "onCreateLoader LOADER_GROUP");                
                uri = com.ivanov.tech.profile.provider.DBContentProvider.URI_GROUP;                
                break;   
        }

        //Log.d(TAG, "onCreateLoader uri="+uri.toString());
        
        CursorLoader cursorLoader = new CursorLoader(getActivity(),
                uri, projection, null, null, null);
        
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    	
    	//Log.d(TAG, "onLoadFinished uri="+data.toString());

        switch(loader.getId()){
            case LOADER_RECENTLIST:
            	//Log.d(TAG, "onLoadFinished LOADER_RECENTLIST count="+data.getCount());
            	
            	cursor_recent=data;            	
            	//Build conversationcursor and swap adapter 
	        	adapter.swapCursor(createMergeCursor());
            	
                break;
            case LOADER_USER: 
            case LOADER_GROUP:    
        		Log.d(TAG, "onLoadFinished LOADER_GROUP");
        		

	        	//На случай если вдруг изменилась иконка или имя контакта
	        	getLoaderManager().restartLoader(LOADER_RECENTLIST, null, this);
        		
                break;            
            
        }
    }
	
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    	
    	//Log.d(TAG, "onLoaderReset");    
    }

	
}

