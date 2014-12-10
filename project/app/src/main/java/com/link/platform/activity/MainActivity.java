package com.link.platform.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.link.platform.R;
import com.link.platform.item.WiFiItem;
import com.link.platform.message.BaseMessage;
import com.link.platform.message.MessageCenter;
import com.link.platform.message.MessageListenerDelegate;
import com.link.platform.message.MessageTable;
import com.link.platform.message.MessageWithObject;
import com.link.platform.ui.adapter.ConversationAdapter;
import com.link.platform.util.Utils;
import com.link.platform.wifi.wifi.WiFiManager;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity implements MessageListenerDelegate , AdapterView.OnItemClickListener {

    private ListView conversation_listview;
    private ConversationAdapter adapter;

    private EditText search_bar;
    private List<WiFiItem> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_conversation_list);

        conversation_listview = (ListView)findViewById(R.id.conversation_list);
        conversation_listview.setOnItemClickListener(this);
        adapter = new ConversationAdapter(this);
        adapter.setData(null);
        conversation_listview.setAdapter(adapter);
        search_bar = (EditText)findViewById(R.id.search_bar);

        search_bar.addTextChangedListener(mTextWatcher);

        search_bar.clearFocus();
        conversation_listview.requestFocus();

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

    }

    @Override
    public void onResume() {
        super.onResume();

        MessageCenter.getInstance().registerListener(this , MessageTable.MSG_OPEN_WIFI_FINISH );
        MessageCenter.getInstance().registerListener(this , MessageTable.MSG_CLOSE_WIFI_FINISH );
        MessageCenter.getInstance().registerListener(this , MessageTable.MSG_GET_SCAN_RESULT );

        WiFiManager.getInstance().setContext(this);
        WiFiManager.getInstance().openWiFi();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MessageCenter.getInstance().removeListener(this);
        WiFiManager.getInstance().clear();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem makesure = menu.findItem( R.id.action_create );

        makesure.setShowAsAction( MenuItem.SHOW_AS_ACTION_IF_ROOM );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_create) {
            Intent intent = new Intent(this , CreateRoomActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private TextWatcher mTextWatcher = new TextWatcher() {

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void afterTextChanged(Editable s) {
            quickSearch(s.toString());
        }
    };

    private void quickSearch(String key) {
        adapter.getFilter().filter(key);
    }

    @Override
    public void onListenerExit() {

    }

    @Override
    public void getMessage(BaseMessage message) {
        String id = message.getMsgId();
        MessageWithObject msg = (MessageWithObject)message;
        if( id.equals( MessageTable.MSG_OPEN_WIFI_FINISH) ) {
            boolean result = Boolean.valueOf( msg.getObject().toString() );
            if( result ) {
                WiFiManager.getInstance().StartScan();
            }
        }
        else if( id.equals( MessageTable.MSG_GET_SCAN_RESULT ) ) {
            List<WiFiItem> list = new ArrayList<WiFiItem>();
            List<ScanResult> wifi_list = WiFiManager.getInstance().GetWifiList();
            for( ScanResult wifi : wifi_list ) {
                String SSID = wifi.SSID;
                boolean isLock = false;
                if( wifi.capabilities.contains("WEP") || wifi.capabilities.contains("WPA" ) ) {
                    isLock = true;
                }
                list.add( new WiFiItem( SSID, isLock ) );
            }
            this.list = list;
            adapter.setData( list );
            adapter.notifyDataSetChanged();
        }
        else if( id.equals( MessageTable.MSG_CLOSE_WIFI_FINISH) ) {

        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

    }
}
