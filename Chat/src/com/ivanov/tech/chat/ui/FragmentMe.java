package com.ivanov.tech.chat.ui;

import android.os.Bundle;
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
import com.ivanov.tech.multipletypesadapter.cursoradapter.CursorItemHolderButton;
import com.ivanov.tech.multipletypesadapter.cursoradapter.CursorItemHolderImageView;
import com.ivanov.tech.multipletypesadapter.cursoradapter.CursorItemHolderText;
import com.ivanov.tech.multipletypesadapter.cursoradapter.CursorMultipleTypesAdapter;

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
	

}
