package com.link.audio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.link.R;
import com.link.util.Constant;
import com.link.util.UtilDate;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Tool to use AudioEncoder and AudioPlayer
 * This class provides interfaces to record voice , send voice data,
 * download voice file and play voice.
 * It alse provides other config functions.
 * @author lancelot
 *
 */
public class AudioManager {
	
	private Activity activity;
	private AmrAudioEncoder encoder;
	private AudioPlayer player;
	
	private double voiceValue = 0.0;
	private double recordTime = 0.0;
	
	private Dialog dialog;
	private ImageView dialog_img;
	private String current_cache_filename;
	
	private boolean isRecordingSucceed = false;
	
	public AudioManager( Activity activity ){
		
		this.activity = activity;
		
		encoder = AmrAudioEncoder.getInstance();
		encoder.initAmrAudioEncoder(activity);
		
		player = AudioPlayer.getInstance();
		player.initAudioPlayer(activity);
	}
	
	public void startRecording( String name ) {
		File cache = new File( Constant.VOICE_PATH + UtilDate.getDate() + UtilDate.getTime() + name );
		
		current_cache_filename = Constant.VOICE_PATH + UtilDate.getDate() + UtilDate.getTime() + name;
		
		FileOutputStream output = null;
		try {
			output = new FileOutputStream(cache);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		encoder.addOutputStream("Cache", output );
		encoder.start();
		
		new Thread( ImgThread ).start();
	}
	
	public void stopRecording() {
		Log.d("Manager" , "Manager stop recording...");
		imgHandle.sendEmptyMessage(0);
	}
	
	public void playVoiceByName( String name ) {
		System.out.println( name );
		
		File file = new File( name );
		if( file.exists() ) {
			try {
				
				FileInputStream input = new FileInputStream(file);
				player.setInputStream(input);
				player.start(false);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public byte[] getVoiceFileBytes( String name ) {
		File file = new File( Constant.VOICE_PATH + name );
		byte[] buff = new byte[1024];
		
		try {
			FileInputStream input = new FileInputStream(file);
			input.read(buff);
			input.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return buff;
	}
	
	public boolean isRecording(){
		return encoder.isRecording();
	}
	
	public boolean isSuccess() {
		return this.isRecordingSucceed;
	}
	
	public String getCacheName(){
		return this.current_cache_filename;
	}
	
	public void showVoiceDialog(){
		dialog = new Dialog( activity , R.style.DialogStyle);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		dialog.setContentView(R.layout.my_dialog);
		dialog_img=(ImageView)dialog.findViewById(R.id.dialog_img);
		dialog.show();
	}
	
	private void setDialogImage(){
		System.out.println("VoiceValue = " + voiceValue );
		if (voiceValue < 200.0) {
			dialog_img.setImageResource(R.drawable.record_animate_01);
		}else if (voiceValue > 200.0 && voiceValue < 400) {
			dialog_img.setImageResource(R.drawable.record_animate_02);
		}else  if (voiceValue > 400.0 && voiceValue < 800) {
			dialog_img.setImageResource(R.drawable.record_animate_03);
		}else if (voiceValue > 800.0 && voiceValue < 1600) {
			dialog_img.setImageResource(R.drawable.record_animate_04);
		}else if (voiceValue > 1600.0 && voiceValue < 3200) {
			dialog_img.setImageResource(R.drawable.record_animate_05);
		}else if (voiceValue > 3200.0 && voiceValue < 5000) {
			dialog_img.setImageResource(R.drawable.record_animate_06);
		}else if (voiceValue > 5000.0 && voiceValue < 7000) {
			dialog_img.setImageResource(R.drawable.record_animate_07);
		}else if (voiceValue > 7000.0 && voiceValue < 10000.0) {
			dialog_img.setImageResource(R.drawable.record_animate_08);
		}else if (voiceValue > 10000.0 && voiceValue < 14000.0) {
			dialog_img.setImageResource(R.drawable.record_animate_09);
		}else if (voiceValue > 14000.0 && voiceValue < 17000.0) {
			dialog_img.setImageResource(R.drawable.record_animate_10);
		}else if (voiceValue > 17000.0 && voiceValue < 20000.0) {
			dialog_img.setImageResource(R.drawable.record_animate_11);
		}else if (voiceValue > 20000.0 && voiceValue < 24000.0) {
			dialog_img.setImageResource(R.drawable.record_animate_12);
		}else if (voiceValue > 24000.0 && voiceValue < 28000.0) {
			dialog_img.setImageResource(R.drawable.record_animate_13);
		}else if (voiceValue > 28000.0) {
			dialog_img.setImageResource(R.drawable.record_animate_14);
		}
	}
	
	private void deleteCacheFile( String name ) {
		File cache = new File( name );
		if( cache.exists() ) {
			cache.delete();
		}
	}
	
	private void showWarnToast(){
		Toast toast = new Toast(activity);
		 LinearLayout linearLayout = new LinearLayout(activity);
		 linearLayout.setOrientation(LinearLayout.VERTICAL); 
		 linearLayout.setPadding(20, 20, 20, 20);
		
		// define a ImageView
		 ImageView imageView = new ImageView(activity);
		 imageView.setImageResource(R.drawable.voice_to_short); // icon
		 
		 TextView mTv = new TextView(activity);
		 mTv.setText("Too short! record again!");
		 mTv.setTextSize(14);
		 mTv.setTextColor(Color.WHITE);
		 
		 //mTv.setPadding(0, 10, 0, 0);
		 
		 // add ImageView and ToastView to Layout
		 linearLayout.addView(imageView);
		 linearLayout.addView(mTv);
		 linearLayout.setGravity(Gravity.CENTER);
		 linearLayout.setBackgroundResource(R.drawable.record_bg);
		 
		 toast.setView(linearLayout); 
		 toast.setGravity(Gravity.CENTER, 0,0);
		 toast.show();				
	}
	
	private Runnable ImgThread = new Runnable() {


		@Override
		public void run() {
			recordTime = 0.0f;
			
			while ( encoder.isRecording() ) {
				
				try {
					Thread.sleep(200);
					recordTime += 0.2;
					
					if ( encoder.isRecording() ) {
						
						voiceValue = encoder.getVoiceValue();
						imgHandle.sendEmptyMessage(1);
					}
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	};
	
	Handler imgHandle = new Handler() {
		@Override
		public void handleMessage(Message msg) {
        
			switch (msg.what) {
			case 0:
				if ( encoder.isRecording() ) {
					
					if (dialog.isShowing()) {
						
						dialog.dismiss();
					}
					encoder.stop();
					if (recordTime < 1.0) {
						
						showWarnToast();
						
						deleteCacheFile( current_cache_filename );
						current_cache_filename = null;
						
						isRecordingSucceed = false;
					}else{
					
						isRecordingSucceed = true;
					}
				}
				break;
			case 1:
				setDialogImage();
				break;
			default:
				break;
			}
			
		}
	};
}
