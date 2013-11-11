package com.link.ui;

import com.link.R;
import com.link.listener.DialogListener;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

public class CreationDialog extends Dialog{
	
	private DialogListener listener;
	private EditText SSID , password;
	private Button login , cancel;

	public CreationDialog( Context context , DialogListener listener ) {
		super(context ,	R.style.DialogStyle );
		
		this.listener = listener;
		this.setTitle("Create a Room");
	}

	@Override
	public void onCreate( Bundle savedInstanceState ){
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.createroom);
		setProperty();
		
		SSID = ( EditText ) this.findViewById( R.id.roomname );
		
		password = ( EditText ) this.findViewById( R.id.password );
		
		login = ( Button ) this.findViewById( R.id.room_login );
		login.setOnClickListener( new android.view.View.OnClickListener(){

			@Override
			public void onClick(View view) {
				String[] message = new String[2];
				message[0] = SSID.getText().toString();
				message[1] = password.getText().toString();
				listener.onClick(message, 2);
				cancel();
			}
				
		});
		
		
		cancel = ( Button ) this.findViewById( R.id.room_cancel );
		cancel.setOnClickListener( new android.view.View.OnClickListener(){

			@Override
			public void onClick(View view) {
				cancel();
			}
				
		});
		
	}
	
	public void setProperty(){
		Window window=getWindow();				//　　　得到对话框的窗口．
		WindowManager.LayoutParams wl = window.getAttributes();
		wl.x = 0;						//这两句设置了对话框的位置．0为中间
		wl.y = 0;
		wl.gravity=Gravity.CENTER ;         
		window.setAttributes(wl); 
	}
}
