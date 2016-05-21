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

public class ItemHolderConversationLeft extends CursorItemHolder {

private static final String TAG = CursorItemHolderLink.class.getSimpleName();
	
	OnClickListener onclicklistener=null;
	
	TextView textview_name,textview_message;
	ImageView imageview_icon;
	
	public ItemHolderConversationLeft(Context context, OnItemClickListener onitemclicklistener,OnClickListener onclicklistener) {
		this.context=context;
		this.onitemclicklistener=onitemclicklistener;
		this.onclicklistener=onclicklistener;
	}
	
	public ItemHolderConversationLeft createClone(){	
		//Log.d(TAG, "createClone");
		return new ItemHolderConversationLeft(context,onitemclicklistener,onclicklistener);
	}
	
	@Override
	public View getView(View convertView, ViewGroup parent, Cursor cursor) {
				
		View view;
		
		if(convertView==null){
		
			LayoutInflater layoutinflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			view= layoutinflater.inflate(R.layout.conversation_item_left, parent, false);
		
			textview_name = (TextView) view.findViewById(R.id.conversation_item_left_name);
	        textview_message = (TextView) view.findViewById(R.id.conversation_item_left_message);
	        imageview_icon = (ImageView) view.findViewById(R.id.conversation_item_left_icon);
	        
        
		}else{
			view=convertView;
		}
		
        JSONObject json;
		try {
			json = new JSONObject(CursorMultipleTypesAdapter.getValue(cursor));
			
			//Log.d(TAG, "getView TYPE_LEFT json="+json+"imageview_icon.id");
			
			if(new BinderTextView(context).bind(textview_name, json.getJSONObject("name"))){
			}
			
			if(new BinderTextView(context).bind(textview_message, json.getJSONObject("message"))){
			}
			
			if(new BinderImageView(context).bind(imageview_icon, json.getJSONObject("icon"))){	
				imageview_icon.setTag(imageview_icon.getId(), json.getInt("userid"));				
				if(onclicklistener!=null)
					imageview_icon.setOnClickListener(onclicklistener);
			}
										
		} catch (JSONException e) {
			Log.e(TAG, "getView TYPE_LEFT JSONException e="+e);
		}
		
		
		return view;
	}

	@Override
	public boolean isEnabled() {
		return false;
	}

}
