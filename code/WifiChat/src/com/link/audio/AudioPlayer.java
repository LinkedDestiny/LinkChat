package com.link.audio;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;

public class AudioPlayer {
	
	private static final String TAG = "AudioPlayer";
	
	private static AudioPlayer Instance = null;
	
	private long alreadyReadByteCount = 0;
	
	private MediaPlayer audio_player;
	private Handler handler;
	
	private String cacheFileName = "VideoCacheFile";
	
	private File cache_file;
	private int cache_count;
	private String cache_name;
	
	private boolean hasMoveCacheFlag;
	
	private boolean isPlaying;
	private boolean download;
	private Activity activity;
	
	private boolean isChaingCacheToAnother;
	
	private InputStream input = null;
	
	/***************************** static methods ***************************************/
	
	public static AudioPlayer getInstance() {
		if ( Instance == null ) {
			synchronized( AudioPlayer.class ) {
				if ( Instance == null ) {
					Instance = new AudioPlayer();
				}
			}
		}
		return Instance;
	}
	
	/***************************** public methods ***************************************/
	
	public AudioPlayer(){
		
	}

	public void initAudioPlayer( Activity activity ) {
		this.activity = activity;
		deleteExistCacheFile();
		initCacheFile();
	}
	
	public void start( boolean download ) {
		handler = new Handler();
		this.cache_count = 0;
		this.download = download;
		deleteExistCacheFile();
		initCacheFile();
		isPlaying = true;
		this.isChaingCacheToAnother = false;
		this.hasMoveCacheFlag = false;
		new Thread( new NetAudioPlayerThread()).start();
	}
	
	public void stop() {
		isPlaying = false;
		this.isChaingCacheToAnother = false;
		this.hasMoveCacheFlag = false;
		
		this.releaseAudioPlayer();

		cache_file = null;
		handler = null;
		input = null;
		handler = null;
	}
	
	public void setInputStream( InputStream stream ) {
		this.input = stream;
	}
	
	/***************************** private methods ***************************************/
	
	private void initCacheFile() {
		cache_file = null;
		cache_file = new File( activity.getCacheDir() , cacheFileName );
	}
	
	private void releaseAudioPlayer() {
		Instance = null;
		
		if( audio_player != null ) {
			try {
				if ( audio_player.isPlaying() ) {
					audio_player.pause();
				}
				audio_player.release();
				audio_player = null;
			} catch ( Exception e ) {
				e.printStackTrace();
			}
		}
	}
	
	private void deleteExistCacheFile() {
		File cacheDir = activity.getCacheDir();
		File[] needDeleteCacheFiles = cacheDir.listFiles();
		for (int index = 0; index < needDeleteCacheFiles.length; ++index) {
			File cache = needDeleteCacheFiles[index];
			if (cache.isFile()) {
				if (cache.getName().contains(cacheFileName.trim())) {
					Log.e(TAG, "delete cache file: " + cache.getName());
					cache.delete();
				}
			}
		}
		needDeleteCacheFiles = null;
	}
	
	private boolean hasMoveTheCacheToAnotherCache() {
		return this.hasMoveCacheFlag ;
	}
	
	private class NetAudioPlayerThread implements Runnable {
		// when cache size is greater than INIT_AUDIO_BUFFER , begin to playing voice
		private final int INIT_AUDIO_BUFFER = 2 * 1024;
		// when there is 1 second left, play the new cache
		private final int CHANGE_CACHE_TIME = 1000;

		public void run() {
			try {
				
				if( input != null )
					receiveNetAudioThenPlay( input );
				
			} catch (Exception e) {
				e.printStackTrace();
				Log.e(TAG, "Fail to read voice from cache file");
			}
		}
 
		private void receiveNetAudioThenPlay( InputStream inputStream ) throws Exception {
			
			FileOutputStream outputStream = new FileOutputStream(cache_file);

			final int BUFFER_SIZE = 100 * 1024;// 100kb buffer size
			byte[] buffer = new byte[BUFFER_SIZE];

			int testTime = 10;
			try {
				alreadyReadByteCount = 0;
				
				while (isPlaying) {
					
					int numOfRead = inputStream.read(buffer);
					System.out.println( numOfRead );
					if (numOfRead <= 0) {
						Log.d("AudioPLayer" , "play stop");
						break;
					}
					alreadyReadByteCount += numOfRead;
					
					outputStream.write(buffer, 0, numOfRead);
					outputStream.flush();
					
					try {
						if (testTime++ >= 10) {
							Log.d(TAG, "cacheFile=" + cache_file.length() );
							// if not download, play the voice
							if( !download )
								testWhetherToChangeCache();
							testTime = 0;
						}
					} catch (Exception e) {
						// TODO: handle exception
					}

					if (hasMoveTheCacheToAnotherCache() && !isChaingCacheToAnother) {
						System.out.println(" reset cache file");
						if (outputStream != null) {
							outputStream.close();
							outputStream = null;
						}
						
						// if download, transfer cache_file to voicecache 
						if ( download ) {
							moveFiletoVoiceCache();
						}
						
						// delete cache file received from net
						// and restore at 0
						outputStream = new FileOutputStream(cache_file);
						hasMoveCacheFlag = false;
						alreadyReadByteCount = 0;
					}

				}
			} catch (Exception e) {
				errorOperator();
				e.printStackTrace();
				throw new Exception("disconnect....");
			} finally {
				buffer = null;
				if (inputStream != null) {
					inputStream.close();
					inputStream = null;
				}
				if (outputStream != null) {
					outputStream.close();
					outputStream = null;
				}
				stop();
				Log.d("AudioPlayer" , "Call stop over");
			}
		}

		private void testWhetherToChangeCache() throws Exception {
			System.out.println("testWhetherToChangeCache");
			
			if (audio_player == null) {
				Log.i("AudioPlayer" , "First set audio player ");
				firstTimeStartPlayer();
			} else {
				Log.i("AudioPlayer" , "have set audio player ");
				changeAnotherCacheWhenEndOfCurrentCache();
			}
		}

		private void firstTimeStartPlayer() throws Exception {
			System.out.println("firstTimeStartPlayer  " + (alreadyReadByteCount >= INIT_AUDIO_BUFFER));
			
			if (alreadyReadByteCount >= INIT_AUDIO_BUFFER ) {
				System.out.println(" Runnable");
				Runnable r = new Runnable() {
					public void run() {
						try {
							File firstCacheFile = createFirstCacheFile();
							// copy the cache and delete it
							hasMoveCacheFlag = true;
							audio_player = createAudioPlayer(firstCacheFile);
							audio_player.start();
							System.out.println( " start " );
						} catch (Exception e) {
							e.printStackTrace();
							Log.e(TAG, " :in firstTimeStartPlayer() fun");
						} finally {
						}
					}
				};
				handler.post(r);
				System.out.println("post");
			}
		}

		private File createFirstCacheFile() throws Exception {
			System.out.println("createFirstCacheFile");
			String firstCacheFileName = cacheFileName + (cache_count ++);
			File firstCacheFile = new File(activity.getCacheDir(), firstCacheFileName);
			moveFile(cache_file, firstCacheFile);
			return firstCacheFile;

		}
		
		private MediaPlayer createAudioPlayer(File audioFile) throws IOException {
			MediaPlayer mPlayer = new MediaPlayer();

			// It appears that for security/permission reasons, it is better to
			// pass
			// a FileDescriptor rather than a direct path to the File.
			// Also I have seen errors such as "PVMFErrNotSupported" and
			// "Prepare failed.: status=0x1" if a file path String is passed to
			// setDataSource(). So unless otherwise noted, we use a
			// FileDescriptor here.
			@SuppressWarnings("resource")
			FileInputStream fis = new FileInputStream(audioFile);
			mPlayer.reset();
			mPlayer.setDataSource(fis.getFD());
			mPlayer.prepare();
			return mPlayer;
		}

		private void moveFiletoVoiceCache() {
			File voiceCache = new File( cache_name );
			
			try {
				moveFile( cache_file , voiceCache );
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		private void moveFile(File oldFile, File newFile) throws IOException {
			// judge if the oldFile is legal or not
			if (!oldFile.exists()) {
				throw new IOException("oldFile is not exists. in moveFile() fun");
			}
			if (oldFile.length() <= 0) {
				throw new IOException("oldFile size = 0. in moveFile() fun");
			}
			
			BufferedInputStream reader = new BufferedInputStream(new FileInputStream(oldFile));
			BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(newFile,
					false));
			
			// write the Amr header into file
			final byte[] AMR_HEAD = new byte[] { 0x23, 0x21, 0x41, 0x4D, 0x52, 0x0A };
			writer.write(AMR_HEAD, 0, AMR_HEAD.length);
			writer.flush();

			try {
				byte[] buffer = new byte[1024];
				int numOfRead = 0;

				while ((numOfRead = reader.read(buffer, 0, buffer.length)) != -1) {
					writer.write(buffer, 0, numOfRead);
					writer.flush();
				}
				
			} catch (IOException e) {
				Log.e(TAG, "moveFile error.. in moveFile() fun." + e.getMessage());
				throw new IOException("moveFile error.. in moveFile() fun.");
			} finally {
				if (reader != null) {
					reader.close();
					reader = null;
				}
				if (writer != null) {
					writer.close();
					writer = null;
				}
			}
		}

		
		
		/**
		 * if current cache file is full, create a new cache file and transfer mediaplayer 
		 * to the new cache file.
		 * 
		 * @throws IOException
		 */
		private void changeAnotherCacheWhenEndOfCurrentCache() throws IOException {
			
			// check the rest time of current cache file
			long theRestTime = audio_player.getDuration() - audio_player.getCurrentPosition();
			Log.d( TAG , "RestTime = " + theRestTime + "");
			
			// check if need to transfer to a new cache file or not
			if (!isChaingCacheToAnother && theRestTime <= CHANGE_CACHE_TIME) {
				isChaingCacheToAnother = true;

				Runnable r = new Runnable() {
					public void run() {
						try {
							System.out.println( "audio_player == null " + ( audio_player == null ));
							File newCacheFile = createNewCache();
							// set copy flag and transfer to a new cache file
							hasMoveCacheFlag = true;
							// set mediaplayer's cachefile
							transferNewCacheToAudioPlayer(newCacheFile);
							
						} catch (Exception e) {
							Log.e(TAG, e.getMessage()
									+ ":changeAnotherCacheWhenEndOfCurrentCache() fun");
						} finally {
							
							deleteOldCache();
							isChaingCacheToAnother = false;
						}
					}
				};
				// start the Runnable
				handler.post(r);
			}
		}

		private File createNewCache() throws Exception {
			String newCacheFileName = cacheFileName + (cache_count ++);
			File newCacheFile = new File(activity.getCacheDir(), newCacheFileName);
			Log.d(TAG, "before moveFile............the size=" + cache_file.length());
			moveFile(cache_file, newCacheFile);
			return newCacheFile;
		}

		private void transferNewCacheToAudioPlayer(File newCacheFile) throws Exception {
			MediaPlayer oldPlayer = audio_player;
			try {
				if( oldPlayer != null ) {
					oldPlayer.pause();
					oldPlayer.reset();
					oldPlayer.release();
				}
			} catch (Exception e) {
				e.printStackTrace();
				Log.e(TAG, "ERROR release oldPlayer.");
			} finally {
				oldPlayer = null;
			}
			
			try {
				audio_player = createAudioPlayer(newCacheFile);
				audio_player.start();
			} catch (Exception e) {
				Log.e(TAG, "filename=" + newCacheFile.getName() + " size=" + newCacheFile.length());
				Log.e(TAG, e.getMessage() + " " + e.getCause() + " error start..in transfanNer..");
			}
			
		}

		private void deleteOldCache() {
			int oldCacheFileCount = cache_count - 1;
			String oldCacheFileName = cacheFileName + oldCacheFileCount;
			File oldCacheFile = new File(activity.getCacheDir(), oldCacheFileName);
			if (oldCacheFile.exists()) {
				oldCacheFile.delete();
			}
		}

		private void errorOperator() {
		}
	}
}
