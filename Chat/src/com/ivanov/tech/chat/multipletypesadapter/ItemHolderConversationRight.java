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

public class ItemHolderConversationRight extends CursorItemHolder {

	private static final String TAG = CursorItemHolderLink.class.getSimpleName();
	
	//COLUMN_STATUS values
	public static final int STATUS_SENT = 0;
	public static final int STATUS_PROCESSED = 1;
	public static final int STATUS_FAILED = 2;
	
	OnClickListener onclicklistener=null;
	
	TextView textview_message;
	ImageView imageview_icon;
	View processed,failed;
	
	public ItemHolderConversationRight(Context context, OnItemClickListener onitemclicklistener,OnClickListener onclicklistener) {
		this.context=context;
		this.onitemclicklistener=onitemclicklistener;
		this.onclicklistener=onclicklistener;
	}
	
	public ItemHolderConversationRight createClone(){	
		//Log.d(TAG, "createClone");
		return new ItemHolderConversationRight(context,onitemclicklistener,onclicklistener);
	}
	
	@Override
	public View getView(View convertView, ViewGroup parent, Cursor cursor) {
				
		View view;
		
		if(convertView==null){
		
			LayoutInflater layoutinflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			view= layoutinflater.inflate(R.layout.conversation_item_right, parent, false);
		
	        textview_message = (TextView) view.findViewById(R.id.conversation_item_right_message);
	        imageview_icon = (ImageView) view.findViewById(R.id.conversation_item_right_icon);
	        processed = (View)view.findViewById(R.id.progressbar_processing);
	        failed = (View)view.findViewById(R.id.imageview_failed);
	        
        
		}else{
			view=convertView;
		}
		
        JSONObject json;
		try {
			json = new JSONObject(CursorMultipleTypesAdapter.getValue(cursor));
			
			//Log.d(TAG, "getView TYPE_RIGHT json="+json+"imageview_icon.id");
									
			if(new BinderTextView(context).bind(textview_message, json.getJSONObject("message"))){
			}
			
			if(new BinderImageView(context).bind(imageview_icon, json.getJSONObject("icon"))){	
				imageview_icon.setTag(imageview_icon.getId(), json.getInt("userid"));				
				if(onclicklistener!=null)
					imageview_icon.setOnClickListener(onclicklistener);
			}
						
			switch(json.getInt("status")){
        	case STATUS_PROCESSED:
        		failed.setVisibility(View.GONE);
        		processed.setVisibility(View.VISIBLE);
        		break;
        		
        	case STATUS_FAILED:
        		failed.setVisibility(View.VISIBLE);
        		processed.setVisibility(View.GONE);
        		
        		failed.setTag(failed.getId(), cursor.getPosition());
        		if(onclicklistener!=null)
        			failed.setOnClickListener(onclicklistener);
        		
        		break;
        		
        	default:
        		failed.setVisibility(View.GONE);
        		processed.setVisibility(View.GONE);
        }
										
		} catch (JSONException e) {
			Log.e(TAG, "getView TYPE_RIGHT JSONException e="+e);
		}
		
		
		return view;
	}

	@Override
	public boolean isEnabled() {
		return false;
	}

}
