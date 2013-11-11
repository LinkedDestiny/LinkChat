package com.link;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.link.info.User;
import com.link.listener.AsyncTaskListener;
import com.link.listener.DialogListener;
import com.link.ui.CreationDialog;
import com.link.ui.WifiAdapter;
import com.link.util.Constant;
import com.link.wifi.WifiAP;
import com.link.wifi.WifiController;

import android.net.ConnectivityManager;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.text.method.PasswordTransformationMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

public class MainActivity extends Activity {

	private WifiAP APcontroller;
	private WifiController controller;
	
	private ImageView bg_top , zir , setting;
	private ImageView add , bg_search , sc , search;
	private EditText search_text;
	
	private ListView Room_List;
	private List<Map<String, Object>> listItems = new ArrayList<Map<String , Object>>();
	private WifiAdapter adapter;
	
	private String username;
	private String roomname;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);		//设置全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, 
        		WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	        		
		setContentView(R.layout.activity_main);
		
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		
		Constant.SCALE = (float)dm.widthPixels / 720;
		
		Room_List = ( ListView )this.findViewById(R.id.room_list);
		adapter = new WifiAdapter( this , listItems );
		Room_List.setAdapter(adapter);
		Room_List.setOnItemClickListener( new ListViewItemClickListener() );
		
		bg_top = ( ImageView ) this.findViewById( R.id.bg_top );
		zir = ( ImageView ) this.findViewById( R.id.zir );
		setting = ( ImageView ) this.findViewById( R.id.setting );
		add = ( ImageView ) this.findViewById( R.id.add );
		bg_search = ( ImageView ) this.findViewById( R.id.bg_search );
		sc = ( ImageView ) this.findViewById( R.id.sc );
		search = ( ImageView ) this.findViewById( R.id.search );
		
		search_text = ( EditText ) this.findViewById( R.id.search_text );
		
		scaleView();
		
		add.setFocusable( true );
		add.setFocusableInTouchMode(true);
		add.requestFocus();
		
		getUserName();
		
	}
	
	private void scaleView(){
		scaleView( bg_top );
		scaleView( zir );
		scaleView( setting );
		scaleView( add );
		scaleView( bg_search );
		scaleView( sc );
		scaleView( search );
		scaleView( search_text );
	}
	
	private void getUserName(){
		AlertDialog.Builder builder = new Builder(MainActivity.this);
			final EditText password = new EditText(MainActivity.this);
			
			builder.setTitle("登录");
			builder.setMessage("请输入用户名");
			builder.setView( password );
			
		  	builder.setPositiveButton("确认", new OnClickListener() {
			  
		  		public void onClick(DialogInterface dialog, int which) {
		  			username = password.getText().toString();
		  			controller = new WifiController( MainActivity.this );
		  			APcontroller = new WifiAP( MainActivity.this , new MyListener() );
		  			
		  			new SetWifiTask( true , 1 , MainActivity.this ).execute();
		  			dialog.dismiss();
		  			
		  		}
		  	});
		  	builder.setNegativeButton("取消", new OnClickListener() {
		  		public void onClick(DialogInterface dialog, int which) {
		  			dialog.dismiss();
		  			
		  		}
		  		
		  	});
		  	builder.create().show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	public class MyListener implements AsyncTaskListener{

		@Override
		public void onPostExecute(boolean flag) {
			Intent intent = new Intent();
  			intent.setClass( MainActivity.this ,  ChatroomActivity.class);
  			intent.putExtra("UserType", User.USER_TYPE_SERVER );
  			intent.putExtra("RoomName", roomname );
  			intent.putExtra("UserName", username );
  			intent.putExtra("HostIP" , controller.getHostIP() );
  			startActivity(intent);
		}
		
	}
	
	public void onSetting( View view ){
		
	}
	
	public void onAdd( View view ){
		CreationDialog dialog = new CreationDialog( this , new MyDialogListener() );
		dialog.show();
	}
	
	public void onSearch( View view ){
		updateWifiList();
	}
	
	public void updateWifiList(){
		if( controller.getWifiState() != WifiManager.WIFI_STATE_ENABLED )
			return;
		
		listItems.clear();
		controller.StartScan();
		
		List<ScanResult> wifilist = controller.GetWifiList();
		if( wifilist == null )
			return;
		Log.d("Search" , "Wifi list size = " + wifilist.size() );
		
		if (!wifilist.isEmpty()){
            for (ScanResult wifiConfig : wifilist){
            	Log.v("Search" , "add Wifi" + wifiConfig.SSID );
            	Map<String , Object> temp = new HashMap< String , Object >();
            	temp.put("info" , wifiConfig.capabilities );
                temp.put("SSID",  wifiConfig.SSID );
                temp.put("BSSID",  wifiConfig.BSSID );
                temp.put("type", 3 );
                listItems.add(temp);
            }
        }
		adapter.notifyDataSetChanged();
	}
	
	private void scaleView( View view ){
		LayoutParams para;  
        para = view.getLayoutParams(); 
        para.height = (int)( view.getLayoutParams().height * Constant.SCALE + 0.5f) ;
        para.width = (int)( view.getLayoutParams().width * Constant.SCALE + 0.5f);
        view.setLayoutParams(para);
	}
	
	 public class ListViewItemClickListener implements OnItemClickListener{
	 	   	
 		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 			final Map<String , Object> temp = listItems.get(position);
 			Log.v("ListView" , "onClick");
 			AlertDialog.Builder builder = new Builder(MainActivity.this);
 			final EditText password = new EditText(MainActivity.this);
 			password.setTransformationMethod(PasswordTransformationMethod.getInstance());
 			builder.setTitle("进入房间");
 			builder.setMessage("请输入密码");
 			builder.setView( password );
 			
 		  	builder.setPositiveButton("确认", new OnClickListener() {
 			  
 		  		public void onClick(DialogInterface dialog, int which) {
 		  			
 		  			controller.addNetWork( 
 		  					controller.CreateWifiConfiguration( temp.get("SSID").toString(), 
 		  							password.getText().toString() , 
 		  							temp.get("BSSID").toString(), 
 		  							(Integer)temp.get("type")));
 		  			roomname = temp.get("SSID").toString();
 		  			new SetWifiTask( true , 2 , MainActivity.this ).execute();
 		  			dialog.dismiss();
 		  			
 		  		}
 		  	});
 		  	builder.setNegativeButton("取消", new OnClickListener() {
 		  		public void onClick(DialogInterface dialog, int which) {
 		  			dialog.dismiss();
 		  			
 		  		}
 		  		
 		  	});
 		  	builder.create().show();
 		}
	}
	 
	 public class MyDialogListener implements DialogListener{

		@Override
		public void onClick(String[] message, int type) {
			if( type == 1 ){
				
				
			} else if ( type == 2 ){
				roomname = message[0];
				APcontroller.setWifiAPInfo( message[0], message[1] );
				APcontroller.toggleWiFiAP();
			}
			
		}
		 
	 }
	 
	 class SetWifiTask extends AsyncTask<Void, Void, Void> {
	        boolean mMode; 				//enable or disable wifi AP
	        int type;			
	        ProgressDialog d;
	        private boolean success = false;

	        public SetWifiTask(boolean mode, int type , Context context) {
	            mMode = mode;
	            this.type = type;
	            d = new ProgressDialog(context);
	        }

	        /**
	         * do before background task runs
	         */
	        @Override
	        protected void onPreExecute() {
	            super.onPreExecute();
	            d.setTitle("Running...");
	            d.setMessage("...please wait a moment.");
	            d.show();
	        }

	        /**
	         * do after background task runs
	         * @param aVoid
	         */
	        @Override
	        protected void onPostExecute(Void aVoid) {
	            super.onPostExecute(aVoid);
	            try {
	                d.dismiss();
	                if( type == 1 )
	                	updateWifiList();
	                else if( type == 2 && success ){
	                	Intent intent = new Intent();
	 		  			intent.setClass( MainActivity.this ,  ChatroomActivity.class);
	 		  			intent.putExtra("UserType", User.USER_TYPE_CLIENT );
	 		  			intent.putExtra("HostIP", controller.getHostIP() );
	 		  			intent.putExtra("IP", controller.getIP() );
	 		  			intent.putExtra("UserName", username );
	 		  			intent.putExtra("RoomName", roomname );
	 		  			startActivity(intent);
	                }
	            } catch (IllegalArgumentException e) {

	            }
	        }

	        /**
	         * the background task to run
	         * @param params
	         */
	        @Override
	        protected Void doInBackground(Void... params) {
	        	if( type == 1 ){
	        		controller.openWifi();
	        	} else if ( type == 2 ){
	        		ConnectivityManager conMan = (ConnectivityManager) 
							 getSystemService(Context.CONNECTIVITY_SERVICE);
	        		
	        		int loop = 60;
	        		while( loop > 0 && conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI).
	    					getDetailedState() != DetailedState.CONNECTED ){
	        			loop --;
	        			Log.v( "Test" ,  conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI).
		    					getDetailedState().toString() );
	        			try {
							Thread.sleep( 500 );
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	        		}

	        		success = ( conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI).
	    					getDetailedState() == DetailedState.CONNECTED );
	        		
					Log.v( "Test" , "Connect" + success + " " + conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI).
	    					getDetailedState() );
	        	}
	        	
	            return null;
	        }
	    }
}
