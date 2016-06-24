package com.ivanov.tech.chat.ui;

import android.database.MatrixCursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.support.v7.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import com.ivanov.tech.chat.Chat;
import com.ivanov.tech.chat.R;
import com.ivanov.tech.multipletypesadapter.cursoradapter.CursorItemHolderButton;
import com.ivanov.tech.multipletypesadapter.cursoradapter.CursorItemHolderImageView;
import com.ivanov.tech.multipletypesadapter.cursoradapter.CursorItemHolderText;
import com.ivanov.tech.multipletypesadapter.cursoradapter.CursorMultipleTypesAdapter;
import com.ivanov.tech.profile.provider.DBContract;

public class FragmentMe extends com.ivanov.tech.profile.ui.FragmentMe{
	
	private static final String TAG = FragmentMe.class
            .getSimpleName();    

	protected View bottom_menu_recent=null;
	protected View bottom_menu_contacts=null;
	protected View bottom_menu_me=null;
    
	protected SubMenu menuAdd;
	protected MenuItem menuAddGroup;
	protected MenuItem menuAddContact; 
	
	public static FragmentMe newInstance() {
		FragmentMe f = new FragmentMe();  
        return f;
    }
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = null;
        view = inflater.inflate(R.layout.me, container, false);
        
        bottom_menu_recent=view.findViewById(R.id.me_recent);
        bottom_menu_contacts=view.findViewById(R.id.me_contacts);
        bottom_menu_me=view.findViewById(R.id.me_me);
        
        bottom_menu_recent.setOnClickListener(this);
        bottom_menu_contacts.setOnClickListener(this);
        bottom_menu_me.setOnClickListener(this);
                
        listview=(ListView)view.findViewById(R.id.me_listview);
        
        adapter=new CursorMultipleTypesAdapter(getActivity(),null,adapter.FLAG_AUTO_REQUERY);
        
        adapter.addItemHolder(TYPE_TEXT, new CursorItemHolderText(getActivity(),this));
        adapter.addItemHolder(TYPE_TEXT_CLICKABLE, new CursorItemHolderText(getActivity(),this){
        	@Override
        	public boolean isEnabled() {
        		return true;
        	}
        });
        adapter.addItemHolder(TYPE_BUTTON, new CursorItemHolderButton(getActivity(),this));
        adapter.addItemHolder(TYPE_AVATAR, new CursorItemHolderImageView(getActivity(),R.layout.details_item_avatar,R.id.details_item_avatar_imageview,this));
        
        listview.setAdapter(adapter);
        
        listview.setOnItemClickListener(adapter);
        
        //adapter.changeCursor(createMergeCursor());
        
        return view;
    }
	
	@Override
	public void onClick(View v) {
		
		if(v.getId()==bottom_menu_recent.getId()){
			Chat.showRecentList(getActivity(), getFragmentManager(), R.id.main_container, false);
			return;
		}
		
		if(v.getId()==bottom_menu_contacts.getId()){
			Chat.showContacts(getActivity(), getFragmentManager(), R.id.main_container,false);
			return;
		}
		
		if(v.getId()==bottom_menu_me.getId()){
			//Nothing спец-заглушка
			return;
		}

		super.onClick(v);
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
								 
		((AppCompatActivity)getActivity()).getSupportActionBar().show();
		((AppCompatActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(false);
		((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("LetsRace-Chat");
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
	
	@Override
	protected MatrixCursor getMatrixCursor(int _id) throws JSONException{
	   	
    	Log.d(TAG, "getMatrixCursor");
    	
    	MatrixCursor matrixcursor=new MatrixCursor(new String[]{adapter.COLUMN_ID, adapter.COLUMN_TYPE, adapter.COLUMN_KEY, adapter.COLUMN_VALUE});    	
    	    	
    	if(cursor_user_server_id.getCount()<1)return matrixcursor;
    	cursor_user_server_id.moveToFirst();
    	
    	JSONObject json;    
    	
    	json=new JSONObject("{ imageview:{image_url:'"+cursor_user_server_id.getString(cursor_user_server_id.getColumnIndex(DBContract.User.COLUMN_NAME_URL_AVATAR))+"' } }");    	
    	matrixcursor.addRow(new Object[]{++_id,TYPE_AVATAR,IMAGEVIEW_KEY_AVATAR,json.toString()});
    	
    	json=new JSONObject("{value:{ text:'"+this.getString(R.string.fragment_me_upload_text)+"' }, key:{visible : false}, icon:{image_res:'"+android.R.drawable.ic_menu_upload+"'} }");    	
    	matrixcursor.addRow(new Object[]{++_id,TYPE_TEXT_CLICKABLE,TEXT_KEY_UPLOAD_AVATAR,json.toString()});
    	    	
    	json=new JSONObject("{key:{ text:'"+this.getString(R.string.fragment_details_user_name)+"' }, value:{text:'"+cursor_user_server_id.getString(cursor_user_server_id.getColumnIndex(DBContract.User.COLUMN_NAME_NAME))+"'}, icon:{ } }");    	
    	matrixcursor.addRow(new Object[]{++_id,TYPE_TEXT_CLICKABLE,TEXT_KEY_NAME,json.toString()});
    	
    	//json=new JSONObject("{button:{tag:'close', text:'"+getString(R.string.fragment_details_button_close_text)+"'} }");    	
    	//matrixcursor.addRow(new Object[]{++_id,TYPE_BUTTON,0,json.toString()});
    	
    	return matrixcursor;
    }
}
