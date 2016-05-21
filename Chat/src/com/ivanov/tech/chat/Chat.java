package com.ivanov.tech.chat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.ivanov.tech.chat.service.ChatService;
import com.ivanov.tech.chat.ui.FragmentContacts;
import com.ivanov.tech.chat.ui.FragmentConversationGroup;
import com.ivanov.tech.chat.ui.FragmentConversationPrivate;
import com.ivanov.tech.chat.ui.FragmentMe;
import com.ivanov.tech.chat.ui.FragmentRecentList;
import com.ivanov.tech.session.Session;

public class Chat {
	
	
	private static final String TAG = "Chat";
	
	public static final int TRANSPORT_TEXT=1;
	public static final int TRANSPORT_NOTIFICATION=100;
	
	public static void showConversationPrivate(final int user_server_id, final Context context, final FragmentManager fragmentManager, final int container){

		final ProgressDialog pDialog = new ProgressDialog(context);
    	    	
		FragmentConversationPrivate fragment=FragmentConversationPrivate.newInstance(user_server_id);

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(container, fragment, "ConversationPrivate");
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.addToBackStack("ConversationPrivate");
        fragmentTransaction.commit();
    	Chat.startChatService(context);
    }
	
	public static void showConversationGroup(final int group_id, final Context context, final FragmentManager fragmentManager, final int container){

		final ProgressDialog pDialog = new ProgressDialog(context);
		
		FragmentConversationGroup fragment=FragmentConversationGroup.newInstance(group_id);

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(container, fragment, "ConversationGroup");
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.addToBackStack("ConversationGroup");
        fragmentTransaction.commit();
    	Chat.startChatService(context);
    }
	
	public static void showContacts(final Context context, final FragmentManager fragmentManager, final int container,final boolean backStack){
		
		
    	FragmentContacts fragmentcontacts=FragmentContacts.newInstance();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(container, fragmentcontacts, "Contacts");
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_NONE);
        if(backStack)
        	fragmentTransaction.addToBackStack("Contacts");            
        fragmentTransaction.commit();
       
		
		Chat.startChatService(context);
    	
    }
	
	public static void showRecentList(final Context context, final FragmentManager fragmentManager, final int container,final boolean backStack){
		
		FragmentRecentList fragmentdetails=FragmentRecentList.newInstance();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(container, fragmentdetails, "RecentList");
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_NONE);
        if(backStack)fragmentTransaction.addToBackStack("RecentList");
        fragmentTransaction.commit();
        
		Chat.startChatService(context);
	
		
    }
	
	public static void showMe(final Context context, final FragmentManager fragmentManager, final int container,final boolean backStack){

		FragmentMe fragment=FragmentMe.newInstance();
		
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(container, fragment, "Me");
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_NONE);
        if(backStack)fragmentTransaction.addToBackStack("Me");
        fragmentTransaction.commit();
        
		Chat.startChatService(context);
		
    }	
	
//--------------Start Chat service-------------------- 
	
	public static void startChatService(Context context){
    	Intent intent=new Intent(context,ChatService.class);
    	intent.putExtra("userid", Session.getUserId());
    	Log.d(TAG,"startChatService userid="+Session.getUserId());
    	context.startService(intent);
    }
	

}
