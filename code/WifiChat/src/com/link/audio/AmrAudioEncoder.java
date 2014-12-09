package com.link.audio;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.link.tools.Method;

import android.app.Activity;
import android.media.MediaRecorder;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.Log;
import android.widget.Toast;

/**
 * Record and encode voice, save the record as file into sdcard.
 * The file path is " Environment.getExternalStorageDirectory() + '/voicecache/' "
 * The voice file is named by time, such as "18_25_32_5_15_2013.arm"
 * 
 * @author lancelot
 *
 * @date 5/15/2013
 */
public class AmrAudioEncoder {
	
	private static final String TAG = "ArmAudioEncoder";
	
	private static AmrAudioEncoder amrAudioEncoder = null;
	
	private static String SERVERNAME = "armServer";
	
	private static int BUFFER_SIZE = 1024;
	
	private Activity activity = null;
	
	private MediaRecorder audio_recorder;
	
	private boolean isAudioRecording;
	
	private LocalServerSocket local;
	private LocalSocket sender , receiver;
	
	// All Amr record OutputStreams
	private Map<String , OutputStream> outputs = new HashMap<String , OutputStream>();
	
	public AmrAudioEncoder(){
		
	}
	
	public static AmrAudioEncoder getInstance(){
		if ( amrAudioEncoder == null ) {
			synchronized( AmrAudioEncoder.class ) {
				if ( amrAudioEncoder == null ) {
					amrAudioEncoder = new AmrAudioEncoder();
				}
			}
		}
		return amrAudioEncoder;
	}
	
	public void initAmrAudioEncoder( Activity activity ) {
		this.activity = activity;
		this.isAudioRecording = false;
	}
	
	/**
	 * interface of encoder
	 * 
	 */
	public void start(){
		
		if ( activity == null ) {
			this.showToastText("Please run init method first!");
			return;
		}
		
		if ( isAudioRecording ) {
			this.showToastText("Encoding now, return");
			return;
		}
		
		if ( !initLocalSocket() ) {
			this.showToastText("open Local server failed");
			releaseAll();
			return;
		}
		
		if ( !initAudioRecorder() ) {
			this.showToastText("Init Recorder failed");
			releaseAll();
			return;
		}
		
		this.isAudioRecording = true;
		this.startAudioRecording();
	}
	
	public void stop() {
		if ( isAudioRecording ) {
			isAudioRecording = false;
		}
		releaseAll();
	}
	
	public void addOutputStream( String name , OutputStream stream ) {
		Log.d(TAG , "Add a new OutputStream : " + stream.toString() );
		
		synchronized( outputs ){	// Thread Lock
			outputs.put(name, stream);
		}
	}
	
	public void removeOutputStream( String name ) {
		Log.d( TAG , "Remove a OutputStream : " + name );
		
		synchronized( outputs ){	// Thread Lock
			outputs.remove(name);
		}
	}
	
	public boolean isRecording(){
		return isAudioRecording;
	}
	
	public double getVoiceValue() {
		return audio_recorder.getMaxAmplitude();
	}
	
	/**
	 * init the local pipe for recording stream
	 * The pipe is used for more OutputStreams because MediaRecorder can only have one OutputStream
	 * The local pipe can transfer data to two or more OutputStreams in order to multiplex
	 * @return
	 * 	true if init succeed
	 */
	private boolean initLocalSocket() {
		boolean ret = true;
		
		try {
			releaseLocalSocket();
			
			// set a new LocalServerSocket
			local = new LocalServerSocket( SERVERNAME );
			
			// set a socket to receiver data from local server
			receiver = new LocalSocket();
			receiver.connect( new LocalSocketAddress( SERVERNAME ));
			receiver.setReceiveBufferSize(BUFFER_SIZE);
			receiver.setSendBufferSize(BUFFER_SIZE);
			
			// set a socket to send data to receiver
			sender = local.accept();
			sender.setReceiveBufferSize(BUFFER_SIZE);
			sender.setSendBufferSize(BUFFER_SIZE);
			
		}catch ( IOException e ){
			e.printStackTrace();
		}
		return ret;
	}
	
	/**
	 * initialize the MediaRecorder
	 * @return ret
	 * 	if init succeed or not
	 */
	@SuppressWarnings("deprecation")
	private boolean initAudioRecorder() {
		
		boolean ret = true;
		
		// if MediaRecord has been set, reset it and release the resources
		if ( audio_recorder != null ) {
			audio_recorder.reset();
			audio_recorder.release();
		}
		
		audio_recorder = new MediaRecorder();
		audio_recorder.setAudioSource( MediaRecorder.AudioSource.MIC );
		if( Method.getAndroidSDKVersion() >= 10 )
			audio_recorder.setOutputFormat( MediaRecorder.OutputFormat.AMR_NB );
		else
			audio_recorder.setOutputFormat( MediaRecorder.OutputFormat.RAW_AMR );
		audio_recorder.setAudioChannels( 1 );
		audio_recorder.setAudioSamplingRate( 8000 );
		audio_recorder.setAudioEncoder( MediaRecorder.AudioEncoder.AMR_NB );
		audio_recorder.setOutputFile( sender.getFileDescriptor() );

		try {
			audio_recorder.prepare();
			audio_recorder.start();
		} catch (Exception e) {
			e.printStackTrace();
			this.showToastText("Your device doesn't support recording function");
			ret = false;
		}
		
		return ret;
	}
	
	private void startAudioRecording() {
		new Thread( new AudioCaptureAndSendThread() ).start();
	}
	
	private void releaseAll(){
		releaseMediaRecorder();
		releaseLocalSocket();
		
		amrAudioEncoder = null;
		
	}
	
	/**
	 * release recorder
	 */
	private void releaseMediaRecorder() {
		try {
			if ( audio_recorder == null )
				return;
			
			if ( isAudioRecording ) {
				audio_recorder.stop();
				isAudioRecording = false;
			}
			
			audio_recorder.reset();
			audio_recorder.release();
			audio_recorder = null;
			
		}catch ( Exception e ) {
			Log.e( TAG , e.toString() );
		}
	}
	
	/**
	 * release local socket pipe
	 */
	private void releaseLocalSocket() {
		
		try {
			if ( sender != null ) {
				sender.close();
			}
			if ( receiver != null ) {
				receiver.close();
			}
			
			if ( local != null ) {
				local.close();
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
		
		sender = receiver = null;
		local = null;
	}
	
	/**
	 * Show msg to user through toast
	 * @param msg
	 * 	message to user
	 */
	private void showToastText( String msg ) {
		Toast.makeText(activity, msg , Toast.LENGTH_SHORT ).show();
	}
	
	private class AudioCaptureAndSendThread implements Runnable {

		@Override
		public void run() {
			
			try {
				transferAudio();
			} catch (Exception e) {
				Log.e( TAG , "handleAudio error");
				e.printStackTrace();
			}
		}
		
		/**
		 * receive and transfer record to different outputstream
		 * @throws Exception
		 */
		private void transferAudio() throws Exception {
			
			// get the InputStream of MediaRecorder
			DataInputStream dataInput = new DataInputStream( receiver.getInputStream() );
			
			skipAmrHead( dataInput );
			
			final int SEND_FRAME_COUNT_ONE_TIME = 10;// 每次发送10帧的数据，1帧大约32B
			final int BLOCK_SIZE[] = { 12, 13, 15, 17, 19, 20, 26, 31, 5, 0, 0, 0, 0, 0, 0, 0 };
			
			byte[] sendBuffer = new byte[ BUFFER_SIZE ];
			
			while( isAudioRecording ) {
				
				int offset = 0;
				
				for( int index = 0 ; index < SEND_FRAME_COUNT_ONE_TIME ; ++ index ) {
					
					if ( !isAudioRecording ) { 		// break when stop
						break;
					}
					// read head of every amr frame
					dataInput.read(sendBuffer, offset, 1);
					// get the FT of Amr frame and the length of frame
					int blockIndex = (int) ( sendBuffer[offset] >> 3 ) & 0x0f;
					int frameLength = BLOCK_SIZE[ blockIndex ];
					
					Log.d("Audio" , "Length" + frameLength );
					Log.d("AudioEncoder", "receiver is " + receiver.isConnected() + " " );
					Log.d("AudioEncoder", "sender is " + sender.isConnected() + " " );
					// read the frame
					readFrame( sendBuffer , offset + 1 , frameLength , dataInput );
					offset += frameLength + 1;
				}
				
				// send buffer to all OutputStreams in the outputs map
				sendData( sendBuffer , offset );
			}
			dataInput.close();
		}
		
		/**
		 * remove the Amr head
		 * @param dataInput
		 * 		get data from the dataInput
		 */
		private void skipAmrHead(DataInputStream dataInput) {
			final byte[] AMR_HEAD = new byte[] { 0x23, 0x21, 0x41, 0x4D, 0x52, 0x0A };
			int result = -1;
			int state = 0;
			try {
				while (-1 != (result = dataInput.readByte())) {
					Log.d("Audio" , result + "" );
					if (AMR_HEAD[0] == result) {
						state = (0 == state) ? 1 : 0;
					} else if (AMR_HEAD[1] == result) {
						state = (1 == state) ? 2 : 0;
					} else if (AMR_HEAD[2] == result) {
						state = (2 == state) ? 3 : 0;
					} else if (AMR_HEAD[3] == result) {
						state = (3 == state) ? 4 : 0;
					} else if (AMR_HEAD[4] == result) {
						state = (4 == state) ? 5 : 0;
					} else if (AMR_HEAD[5] == result) {
						state = (5 == state) ? 6 : 0;
					}

					if (6 == state) {
						break;
					}
				}
			} catch (Exception e) {
				Log.e(TAG, "read mdat error...");
			}
		}
		
		/**
		 * read a frame from dataInput
		 * @param buffer	
		 * @param offset	
		 * @param length
		 * @param dataInput
		 */
		private void readFrame(byte[] buffer, int offset, int length, DataInputStream dataInput) {
			int numOfRead = -1;
			while (true) {
				try {
					numOfRead = dataInput.read(buffer, offset, length);
					if (numOfRead == -1) {
						Log.d(TAG, "amr...no data get wait for data coming.....");
						Thread.sleep(100);
					} else {
						offset += numOfRead;
						length -= numOfRead;
						if (length <= 0) {
							break;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					Log.e(TAG, "amr..error readSomeData");
					break;
				}
			}
		}
		
		/**
		 * send buffer to all OutputStreams in outputs.
		 * @param buffer
		 * @param sendLength
		 */
		private void sendData( byte[] buffer , int sendLength ) {
			
			synchronized( outputs ) {
				try {
					// get a copy of buffer
					byte[] sendBuffer = new byte[ sendLength ];
					System.arraycopy(buffer, 0, sendBuffer, 0, sendLength);
					
					Iterator<OutputStream> iter = outputs.values().iterator();
					while( iter.hasNext() ) {
						OutputStream temp = (OutputStream) iter.next();
						if( temp != null ) {
							temp.write(sendBuffer);
							temp.flush();
						}
					}
					
				} catch ( IOException e ) {
					e.printStackTrace();
				}
			}
		}
		
	}
}
