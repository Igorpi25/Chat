package com.ivanov.tech.chat.ui;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.MergeCursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.ivanov.tech.chat.Chat;
import com.ivanov.tech.chat.R;
import com.ivanov.tech.multipletypesadapter.cursoradapter.CursorItemHolderHeader;
import com.ivanov.tech.multipletypesadapter.cursoradapter.CursorItemHolderLink;
import com.ivanov.tech.multipletypesadapter.cursoradapter.CursorMultipleTypesAdapter;

public class FragmentContacts extends com.ivanov.tech.profile.ui.FragmentContacts{
	
	private static final String TAG = FragmentContacts.class
            .getSimpleName();
	
	protected View bottom_menu_recent=null;
	protected View bottom_menu_contacts=null;
	protected View bottom_menu_me=null;
    
	protected SubMenu menuAdd;
	protected MenuItem menuAddGroup;
	protected MenuItem menuAddContact;
	
	public static FragmentContacts newInstance() {
		FragmentContacts f = new FragmentContacts(); 
        
        return f;
    }
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = null;
        view = inflater.inflate(R.layout.contacts, container, false);
                
        Log.d(TAG,"onCreateView");
        
        listview=(ListView)view.findViewById(R.id.contacts_listview);
        
        adapter=new CursorMultipleTypesAdapter(getActivity(),null,adapter.FLAG_AUTO_REQUERY);
        
        //Prepare map of types and set listeners for them. There are different ways in which you can define ItemHolder      
        adapter.addItemHolder(TYPE_LINK_USER, new CursorItemHolderLink(getActivity(),this,this));                
       
        adapter.addItemHolder(TYPE_LINK_GROUP, new CursorItemHolderLink(getActivity(),this,null));
       
        adapter.addItemHolder(TYPE_HEADER, new CursorItemHolderHeader(getActivity(),this));
        
        listview.setAdapter(adapter);
        
        listview.setOnItemClickListener(adapter);
        
        adapter.changeCursor(createMergeCursor());
        
        return view;
    }
	
	@Override
	public void onClick(View v) {
		Log.d(TAG, "onClick");
		if(v.getId()==bottom_menu_recent.getId()){
			Log.d(TAG, "onClick bottom_menu_recent");
			Chat.showRecentList(getActivity(), getFragmentManager(), R.id.main_container, false);
			return;
		}
		
		if(v.getId()==bottom_menu_contacts.getId()){
			//Nothing спец-заглушка
			Log.d(TAG, "onClick bottom_menu_contacts");
			return;
		}
		
		if(v.getId()==bottom_menu_me.getId()){
			Log.d(TAG, "onClick bottom_menu_me");
			Chat.showMe(getActivity(), getFragmentManager(), R.id.main_container, false);
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
								 
        getSherlockActivity().getSupportActionBar().show();
        getSherlockActivity().getSupportActionBar().setHomeButtonEnabled(false);
        getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSherlockActivity().getSupportActionBar().setTitle("LetsRace-Chat");
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

	//------------Preparing cursor----------------------------

	@Override
	protected Cursor createMergeCursor(){
    	
    	List<Cursor> cursors_list=new ArrayList<Cursor>();	
    	
    	int _id=1;
    	    	
    	if(cursor_groups!=null){
    		cursors_list.add(getGroupsMatrixCursor(_id));
		}
	
    	if(cursor_users!=null) {
    		cursors_list.add(getUsersMatrixCursor(_id));
    	}
    	
    	if(cursors_list.size()==0)return null;
    	
    	Cursor[] cursors_array=new Cursor[cursors_list.size()];
    	MergeCursor mergecursor=new MergeCursor(cursors_list.toArray(cursors_array));
    	
    	return mergecursor;    	
    }

}
