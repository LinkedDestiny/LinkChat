package com.link;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.link.audio.AudioManager;
import com.link.info.User;
import com.link.listener.ChatOptionViewListener;
import com.link.thread.MessageClient;
import com.link.thread.MessageHandle;
import com.link.thread.MessageServer;
import com.link.tools.Method;
import com.link.ui.MessageAdapter;
import com.link.ui.OptionView;
import com.link.util.Constant;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ChatroomActivity extends Activity {

	private ImageView room_top , room_bg_chat , room_chat , line;
	private ImageView back , menu;
	private ImageView plus , upload , send;
	private EditText message;
	private TextView roomname;
	private Button talk;
	private OptionView option;
	
	private ListView message_list;
	private List<Map<String, Object>> listItems = new ArrayList<Map<String , Object>>();
	private MessageAdapter adapter;
	
	private int HostIP;
	private int IP;
	private String Username;
	private String Roomname;
	
	private MessageHandle handle;
	
	private AudioManager audio;
	
	private static String TAG = "Chatroom";
	private boolean isTalk = false;
	private boolean isOption = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);		//设置全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, 
        		WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	        		
		setContentView(R.layout.chatroom);
		Intent intent =  this.getIntent();
		Roomname = intent.getStringExtra("RoomName");
		Username = intent.getStringExtra("UserName");
		HostIP = intent.getIntExtra("HostIP", 0);
		IP = intent.getIntExtra("IP", 0);
		int type = intent.getIntExtra("UserType", -1 );
		
		Log.v(TAG , "Username: " + Username );
		Log.v(TAG , "HostIP: " + Method.ipIntToString( HostIP ) );
		
		if( Method.getAndroidSDKVersion() > 8 ){
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
	        .detectDiskReads()
	        .detectDiskWrites()
	        .detectNetwork()   // or .detectAll() for all detectable problems
	        .penaltyLog()
	        .build());
			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
			.detectLeakedSqlLiteObjects() //探测SQLite数据库操作
			.penaltyLog() //打印logcat
			.penaltyDeath()
			.build());
		}
		
		audio = new AudioManager( this );
		
		init_View();
		
		if( type == User.USER_TYPE_CLIENT ){
			handle = new MessageClient( this ,  HostIP , Username , IP );
			handler.sendEmptyMessage(2);
		} else if ( type == User.USER_TYPE_SERVER ){
			handle = new MessageServer( this ,  HostIP , Username );
			handler.sendEmptyMessage(2);
		}
		
	}
	
	private void init_View(){
		message_list = ( ListView )this.findViewById( R.id.room_messagelist );
		adapter = new MessageAdapter( this , listItems );
		message_list.setAdapter(adapter);
		message_list.setOnItemClickListener( new ChatVoiceListener() );
		
		room_top = ( ImageView ) this.findViewById( R.id.room_top );
		Method.scaleView(room_top);
		
		room_bg_chat = ( ImageView ) this.findViewById( R.id.room_bg_chat );
		Method.scaleView(room_bg_chat);
		
		room_chat = ( ImageView ) this.findViewById( R.id.room_chat );
		Method.scaleView(room_chat);
		
		back = ( ImageView ) this.findViewById( R.id.room_back );
		Method.scaleView(back);
		
		menu = ( ImageView ) this.findViewById( R.id.room_menu );
		Method.scaleView(menu);
		
		plus = ( ImageView ) this.findViewById( R.id.room_plus );
		Method.scaleView(plus);
		
		upload = ( ImageView ) this.findViewById( R.id.room_upload );
		Method.scaleView(upload);
		
		send = ( ImageView ) this.findViewById( R.id.room_send);
		Method.scaleView(send);
		send.setFocusable( true );
		send.setFocusableInTouchMode( true );
		send.requestFocus();
		
		line = ( ImageView ) this.findViewById( R.id.room_line);
		Method.scaleView(line);
		
		message = ( EditText )this.findViewById( R.id.message_text );
		Method.scaleView(message);
		
		roomname = ( TextView )this.findViewById( R.id.room_roomname );
		roomname.setText(Roomname);
		
		talk = ( Button ) this.findViewById( R.id.room_talk );
		talk.setVisibility(View.INVISIBLE);
		talk.setClickable( false );
		isTalk = false;
		
		option = ( OptionView )this.findViewById( R.id.optionview );
		option.setOnClickOptionListener( new OptionViewListener() );
		option.setVisibility( View.VISIBLE );
		
		setClickListener();
	}
	
	private void setClickListener(){
		send.setOnClickListener( new OnClickListener(){

			@Override
			public void onClick(View v) {
				String msg = message.getText().toString();
				message.setText("");
				
				Map< String , Object > newMessage = new HashMap<String , Object>();
				newMessage.put("Username", Username );
				newMessage.put("Usermessage", msg );
				newMessage.put("MessageType", 1 );
				
				updateMessageList( newMessage );
				
				Message info = handler.obtainMessage(1,  msg );
				handler.sendMessage(info);
			}
			
		});
		
		upload.setOnClickListener( new OnClickListener(){

			@Override
			public void onClick(View v) {
				Log.d(TAG, " CLICK upload : " + isOption );
				if ( isOption ) {
					option.setVisibility(View.GONE );
					isOption = false;
				}else {
					option.setVisibility(View.VISIBLE);
					isOption = true;
				}
			}
			
		});
		
		
		talk.setOnTouchListener( new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				
				System.out.println( event.getEventTime() );
				
				switch( event.getAction() ) {
				
					case MotionEvent.ACTION_DOWN:{
						Log.d(TAG , "Press to talk");
						audio.startRecording( Username );
						audio.showVoiceDialog();
						break;
					}
					
					case MotionEvent.ACTION_UP:{
						System.out.println( event.getEventTime() );
						Log.d(TAG , ( v.getId() == R.id.room_talk ) + "  talk stop");
						if( audio.isRecording() ){
							audio.stopRecording();
							
							Log.d(TAG , "talk success");
							Map< String , Object > newMessage = new HashMap<String , Object>();
							newMessage.put("Username", Username );
							newMessage.put("Usermessage", "Voice: press to play..." );
							newMessage.put("MessageType", 2 );
							newMessage.put("CacheName", audio.getCacheName() );
							
							updateMessageList( newMessage );
							
							
						}
						break;
					}
				}
				return true;
			}
		});
	}
	public void updateMessageList( Map<String , Object> newObject ){
		
		listItems.add(newObject);
		handler.sendEmptyMessage(0);
	}
	
	Handler handler = new Handler(){     
		
        @Override  
	    public void handleMessage(Message msg) {     
        	
        	synchronized( this ){
		        if (msg.what == 0) {
		        	adapter.notifyDataSetChanged();
		        	
		        }else if( msg.what == 1 ){
		        	String message = msg.obj.toString();
		        	
		        	handle.sendMessage(message, -1 , Constant.MESSAGE );
		        }else if( msg.what == 2 ){
		        	handle.startThread();
		        }
        	}
	    }   
         
	};
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		
		if( keyCode == KeyEvent.KEYCODE_BACK ) {
			handle.stopThread();
		}	
		return super.onKeyDown(keyCode, event);
		
	}
	
	private class OptionViewListener implements ChatOptionViewListener{

		@Override
		public void onClickFile() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onClickTalk() {
			if ( isTalk ) {
				talk.setVisibility( View.INVISIBLE );
				talk.setClickable( false );
				
				room_chat.setVisibility( View.VISIBLE );
				message.setVisibility( View.VISIBLE );
				
				isTalk = false;
			} else {
				talk.setVisibility( View.VISIBLE );
				talk.setClickable( true );
				
				room_chat.setVisibility( View.INVISIBLE );
				message.setVisibility( View.INVISIBLE );
				
				isTalk = true;
				
			}
		}
		
	}
	
	public class ChatVoiceListener implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			
			Map<String , Object> temp = listItems.get(position);
			if( (Integer)temp.get("MessageType") == 2 ) {
				audio.playVoiceByName( (String) temp.get("CacheName") );
			}
			
		}
		
	}
}
