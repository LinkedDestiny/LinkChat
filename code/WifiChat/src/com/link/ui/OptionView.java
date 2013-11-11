package com.link.ui;

import com.link.R;
import com.link.listener.ChatOptionViewListener;
import com.link.tools.Method;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class OptionView extends LinearLayout{

	private ImageView background;
	
	private ImageView voice , file;
	
	private ChatOptionViewListener listener;
	
	public OptionView( Context context ) {
		super( context );
		
	}
	public OptionView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public void setOnClickOptionListener( ChatOptionViewListener listener ) {
		this.listener = listener;
	}
	
	protected void onFinishInflate() {
		super.onFinishInflate();
		LayoutInflater.from(getContext()).inflate( R.layout.optionview , this );
		
		init_widget();
		
		addListener();
	}
	
	private void init_widget() {
		
		background = ( ImageView )this.findViewById( R.id.option_background );
		
		voice = ( ImageView ) this.findViewById( R.id.option_talk );
		
		file = ( ImageView ) this.findViewById( R.id.option_file );
		
		Method.scaleView(background);
		Method.scaleView(voice);
		Method.scaleView(file);
	}
	
	private void addListener() {
		voice.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				listener.onClickTalk();
			}
			
		});
		
		file.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				listener.onClickFile();
			}
			
		});
	}
	
	

}
