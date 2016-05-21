package com.ivanov.tech.chat.multipletypesadapter;

import org.json.JSONException;
import org.json.JSONObject;

import com.ivanov.tech.chat.R;

import com.ivanov.tech.multipletypesadapter.BinderButton;
import com.ivanov.tech.multipletypesadapter.BinderImageView;
import com.ivanov.tech.multipletypesadapter.BinderTextView;
import com.ivanov.tech.multipletypesadapter.ItemHolder;

import com.ivanov.tech.multipletypesadapter.cursoradapter.CursorItemHolder;
import com.ivanov.tech.multipletypesadapter.cursoradapter.CursorItemHolderLink;
import com.ivanov.tech.multipletypesadapter.cursoradapter.CursorMultipleTypesAdapter;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ItemHolderConversationNotification extends CursorItemHolder {

private static final String TAG = CursorItemHolderLink.class.getSimpleName();
	
	TextView textview_notification;
	
	public ItemHolderConversationNotification(Context context, OnItemClickListener onitemclicklistener) {
		this.context=context;
		this.onitemclicklistener=onitemclicklistener;
	}
	
	public ItemHolderConversationNotification createClone(){	
		//Log.d(TAG, "createClone");
		return new ItemHolderConversationNotification(context,onitemclicklistener);
	}
	
	@Override
	public View getView(View convertView, ViewGroup parent, Cursor cursor) {
				
		View view;
		
		if(convertView==null){
		
			LayoutInflater layoutinflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			view= layoutinflater.inflate(R.layout.conversation_item_notification, parent, false);
		
			textview_notification = (TextView) view.findViewById(R.id.conversation_item_notification_textview);
	        
        
		}else{
			view=convertView;
		}
		
        JSONObject json;
		try {
			json = new JSONObject(CursorMultipleTypesAdapter.getValue(cursor));
			
			//Log.d(TAG, "getView TYPE_NOTIFICATION json="+json+"imageview_icon.id");
			
			if(new BinderTextView(context).bind(textview_notification, json.getJSONObject("notification"))){
			}
								
		} catch (JSONException e) {
			Log.e(TAG, "getView TYPE_NOTIFICATION JSONException e="+e);
		}
		
		
		return view;
	}

	@Override
	public boolean isEnabled() {
		return false;
	}

}
