package com.ivanov.tech.chat.reciever;

import org.json.JSONObject;

import com.ivanov.tech.chat.R;
import com.ivanov.tech.chat.demo.DemoActivity;
import com.ivanov.tech.profile.provider.DBContentProvider;
import com.ivanov.tech.profile.provider.DBContract;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

public class MessageReciever extends BroadcastReceiver {

  private static final String TAG="MessageReciever";
	
  public static final String EXTRA_TYPE="type";
  public static final String EXTRA_USERID="userid";
  public static final String EXTRA_GROUPID="groupid";
  public static final String EXTRA_MESSAGE="message";
  public static final String EXTRA_VALUE="value";
  
  public static final int TYPE_PRIVATE=0;
  public static final int TYPE_GROUP=1;
  
//Additinal digit at last of notification-id
  public static final int REST_PRIVATE=1;
  public static final int REST_GROUP=2;
  
  Context mContext;
  
  @Override
  public void onReceive(Context context, Intent intent) {
	  
	  
	  mContext=context;
	  
	  int userid=intent.getIntExtra(EXTRA_USERID, 0);
	  
	  Log.d(TAG, "onReceive userid="+userid);
	  
	  //Get username by userid from DB
	  Cursor cursor_user=context.getContentResolver().query(DBContentProvider.URI_USER, new String[]{DBContract.User.COLUMN_NAME_NAME}, DBContract.User.COLUMN_NAME_SERVER_ID+" = "+userid, null, null);
	  if(!cursor_user.moveToFirst()){
		  //User not found. Possible if first start application
		  return;
	  }
		  
	  String username=cursor_user.getString(0);
	  
	  if(intent.getIntExtra(EXTRA_TYPE, TYPE_PRIVATE)==TYPE_PRIVATE){	  
		  sendPrivateNotification(intent,userid,username,intent.getStringExtra(EXTRA_VALUE));
	  }else{
		  int groupid=intent.getIntExtra(EXTRA_GROUPID, 0);
		  
		  //Get groupname by groupid from DB
		  Cursor cursor_group=context.getContentResolver().query(DBContentProvider.URI_GROUP, new String[]{DBContract.Group.COLUMN_NAME_NAME}, DBContract.Group.COLUMN_NAME_SERVER_ID+" = "+intent.getIntExtra(EXTRA_GROUPID, 0), null, null);
		  cursor_group.moveToFirst();
		  String groupname=cursor_group.getString(0);
		  		  
		  sendGroupNotification(intent,groupid,groupname,username,intent.getStringExtra(EXTRA_VALUE));
	  }
	  
  }
  
//-------------------------Notification------------------------------
  
  void sendPrivateNotification(Intent sourceintent,int id,String username,String value) {
  	
	  NotificationCompat.Builder notification = new NotificationCompat.Builder(mContext);
      
      // Pending intent to call MainActivity with arguments
      Intent intent = new Intent(mContext, DemoActivity.class);
      intent.putExtras(sourceintent);
      PendingIntent pendingintent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_ONE_SHOT);
      notification.setContentIntent(pendingintent);
      
      notification.setContentTitle(username);
      notification.setContentText(value);
      notification.setTicker(username+":"+value);
            
      notification.setSmallIcon(R.drawable.ic_launcher);
      notification.setVisibility(Notification.VISIBILITY_PUBLIC);      
      notification.setAutoCancel(true);//Disappear when click
      notification.setLights(Color.GREEN,200,3000);
      notification.setPriority(Notification.PRIORITY_HIGH);
      notification.setCategory(Notification.CATEGORY_MESSAGE);
      //notification.setGroup("com.ivanov.tech.chat");      
      notification.setDefaults(Notification.DEFAULT_SOUND);      
         
      NotificationManager notificationmanager =
    		    (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);


      //Sending
      notificationmanager.notify(10*id+REST_PRIVATE, notification.build());
      
      Log.d(TAG, "sendPrivateNotification");
    }
  
  void sendGroupNotification(Intent sourceintent,int id,String groupname,String username,String value) {
	  	
	  NotificationCompat.Builder notification = new NotificationCompat.Builder(mContext);
      
      // Pending intent to call MainActivity with arguments
      Intent intent = new Intent(mContext, DemoActivity.class);
      intent.setAction(Long.toString(System.currentTimeMillis()));
      
      intent.putExtras(sourceintent);
      PendingIntent pendingintent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_ONE_SHOT);
      
      notification.setContentIntent(pendingintent);
      
      notification.setContentTitle(groupname);
      notification.setContentText(username+":"+value);
      notification.setTicker("["+groupname+"]"+username+":"+value);
      
      notification.setSmallIcon(R.drawable.ic_launcher);
      notification.setVisibility(Notification.VISIBILITY_PUBLIC);      
      notification.setAutoCancel(true);//Disappear when click
      notification.setLights(Color.GREEN,200,3000);
      notification.setPriority(Notification.PRIORITY_HIGH);
      notification.setCategory(Notification.CATEGORY_MESSAGE);
      //notification.setGroup("com.ivanov.tech.chat");      
      notification.setDefaults(Notification.DEFAULT_SOUND);      
      
      NotificationManager notificationmanager =
  		    (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
      
      //Sending
      notificationmanager.notify(10*id+REST_GROUP, notification.build());
    }
  
}
