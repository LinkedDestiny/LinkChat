package com.link.platform.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.link.platform.R;
import com.link.platform.item.MessageItem;
import com.link.platform.message.BaseMessage;
import com.link.platform.message.MessageCenter;
import com.link.platform.message.MessageListenerDelegate;
import com.link.platform.message.MessageTable;
import com.link.platform.message.MessageWithObject;
import com.link.platform.network.BaseClient;
import com.link.platform.network.ServerService;
import com.link.platform.network.socket.IOHelper;
import com.link.platform.ui.adapter.AddMoreAdapter;
import com.link.platform.ui.adapter.EmojAdapter;
import com.link.platform.ui.adapter.MessageAdapter;
import com.link.platform.util.SmilyManager;
import com.link.platform.util.UIHelper;
import com.link.platform.util.Utils;
import com.link.platform.wifi.ap.APManager;
import com.link.platform.wifi.wifi.WiFiManager;

import java.util.ArrayList;
import java.util.List;

public class ConversationActivity extends Activity implements MessageListenerDelegate , View.OnClickListener
                                                , TextWatcher , AdapterView.OnItemClickListener {

    public final static String TAG = "ConversationActivity";
    public final static String PARAM_ROOM_NAME = "room_name";
    public final static String PARAM_IS_HOST = "is_host";

    private ListView message_listview;
    private MessageAdapter adapter;
    private List<MessageItem> list = new ArrayList<MessageItem>();

    private GridView emoj_view;
    private EmojAdapter emojAdapter;

    private GridView add_view;
    private AddMoreAdapter addMoreAdapter;

    private ImageView back, settings;
    private TextView title;

    private ImageView emoj, add_more, voice_send;
    private TextView send;
    private EditText message_input;

    private String room_name;
    private String host_ip;
    private BaseClient client;
    private String own;

    private ProgressDialog d;
    private boolean isKeyboardShow;
    private InputMethodManager imm;
    private boolean isSendText = false;

    private SmilyManager smilyManager;
    private boolean isEmojShow = false;
    private boolean isAddMoreShow = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        room_name = getIntent().getStringExtra(PARAM_ROOM_NAME);

        if( getIntent().getBooleanExtra(PARAM_IS_HOST, false) ) {
            host_ip = "127.0.0.1";
        } else {
            host_ip = "192.168.43.1";
        }

        imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);

        client = new BaseClient(host_ip, Utils.CHAT_PORT);
        client.start();

        smilyManager = SmilyManager.getInstance();

        initView();
        initMenu();

        showKeyboard();
    }

    @Override
    public void onResume() {
        super.onResume();

        MessageCenter.getInstance().registerListener(this , MessageTable.MSG_CLOSE_AP_FINISH );
        MessageCenter.getInstance().registerListener(this , MessageTable.MSG_CONNECT_FINISH );

        MessageCenter.getInstance().registerListener(this , MessageTable.MSG_TEXT );
        MessageCenter.getInstance().registerListener(this , MessageTable.MSG_IMG );
        MessageCenter.getInstance().registerListener(this , MessageTable.MSG_ONLINE );
        MessageCenter.getInstance().registerListener(this , MessageTable.MSG_OFFLINE );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy ConversationActivity");
        MessageCenter.getInstance().removeListener(this);
        Intent server = new Intent(this, ServerService.class);
        stopService(server);
    }

    private void initView() {
        message_listview = (ListView)this.findViewById(R.id.message_list);
        adapter = new MessageAdapter(this, list);
        message_listview.setAdapter(adapter);

        back = (ImageView)findViewById(R.id.title_back);
        settings = (ImageView)findViewById(R.id.settings);
        emoj = (ImageView)findViewById(R.id.emoj);
        add_more = (ImageView)findViewById(R.id.add_more);
        send = (TextView)findViewById(R.id.send);
        voice_send = (ImageView)findViewById(R.id.voice_send);

        back.setOnClickListener(this);
        settings.setOnClickListener(this);
        emoj.setOnClickListener(this);
        add_more.setOnClickListener(this);
        send.setOnClickListener(this);
        voice_send.setOnClickListener(this);

        title = (TextView)findViewById(R.id.title);
        title.setText(room_name);
        message_input = (EditText)findViewById(R.id.message_input);
        message_input.addTextChangedListener(this);
        message_input.setOnClickListener(this);

        d = new ProgressDialog(this);
        d.setTitle("正在连接房间...");
        d.show();
    }

    private void initMenu() {

        emoj_view = (GridView)findViewById(R.id.grid_emoj);
        emojAdapter = new EmojAdapter(this, 0);
        emoj_view.setOnItemClickListener(this);
        emoj_view.setAdapter(emojAdapter);

        add_view = (GridView)findViewById(R.id.grid_add_more);
        addMoreAdapter = new AddMoreAdapter(this);
        add_view.setOnItemClickListener(this);
        add_view.setAdapter(addMoreAdapter);
    }



    @Override
    public void onListenerExit() {

    }

    @Override
    public void getMessage(BaseMessage message) {
        String id = message.getMsgId();
        MessageWithObject msg = (MessageWithObject)message;
        if( id.equals(MessageTable.MSG_CLOSE_AP_FINISH ) ) {

            boolean result = Boolean.valueOf(msg.getObject().toString());
            Log.d(TAG , "result = " + result );
            if( result ) {
                // TODO reset wifi connection
                this.finish();
            }
        }
        else if( id.equals(MessageTable.MSG_CONNECT_FINISH)) {
            own = IOHelper.ipIntToString(WiFiManager.getInstance().getIP());
            Log.d(TAG, "IP: " + own );
            Log.d(TAG, "Host IP: " + IOHelper.ipIntToString(WiFiManager.getInstance().getHostIP()) );
            boolean result = Boolean.valueOf(msg.getObject().toString());
            String toast = "";
            if( result ) {
                toast = "连接成功";
            } else {
                toast = "连接房间失败，请重试";
            }
            UIHelper.makeToast(toast);
            d.dismiss();
        }
        else if( id.equals(MessageTable.MSG_TEXT ) ) {
            Log.d(TAG, "recv message");
            MessageItem item = (MessageItem)msg.getObject();
            list.add(item);
            adapter.notifyDataSetChanged();
        }
        else if( id.equals(MessageTable.MSG_ONLINE ) ) {
            Log.d(TAG, "recv ONLINE");
            MessageItem item = (MessageItem)msg.getObject();
            list.add(item);
            adapter.notifyDataSetChanged();
        }
        else if( id.equals(MessageTable.MSG_OFFLINE ) ) {
            Log.d(TAG, "recv OFFLINE");
            MessageItem item = (MessageItem)msg.getObject();
            list.add(item);
            adapter.notifyDataSetChanged();
        }
        else if( id.equals(MessageTable.MSG_SERVER_CLOSE ) ) {
            UIHelper.makeToast("聊天室已关闭，正在退出...");
            onBack();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showKeyboard();
            onBack();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.title_back: {
                onBack();
                return;
            }
            case R.id.settings: {
                // TODO jump to Room Detail Page
                break;
            }
            case R.id.send: {
                String message = message_input.getText().toString();

                Log.d(TAG, message);
                MessageItem item = MessageItem.textMessage(own, true, message);
                if( client.sendMessage( item ) ) {
                    list.add(item);
                    adapter.notifyDataSetChanged();
                    message_input.setText("");
                } else {
                    UIHelper.makeToast("消息发送失败");
                }
                break;
            }
            case R.id.emoj:
            {
                clickEmoj();
                break;
            }
            case R.id.add_more:
            {
                clickAddMore();
                break;
            }
            case R.id.message_input:
            {
                showKeyboard();
                reset();
                break;
            }
        }
    }

    private void onBack() {
        if( isKeyboardShow ) {
            hideKeyBoard();
        }
        // TODO send offline
        client.stop();
        if( getIntent().getBooleanExtra(PARAM_IS_HOST, false) ) {
            ServerService.isInitServer = false;
            Intent server = new Intent(this, ServerService.class);
            stopService(server);
            APManager.getInstance().toggleWiFiAP(this, false);
        } else {
            this.finish();
        }
    }

    public void hideKeyBoard() {
        if (!isKeyboardShow) {
            return;
        }
        isKeyboardShow = false;
        View view = getCurrentFocus();
        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void showKeyboard() {
        isKeyboardShow = true;
        message_input.requestFocus();
        imm.showSoftInput(message_input, InputMethodManager.RESULT_SHOWN);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

    }

    @Override
    public void onTextChanged(CharSequence s, int i, int i2, int i3) {
        if( s.length() == 0 ) {
            switchSendButton(false);
        } else if( s.length() > 0 ) {
            switchSendButton(true);
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    private void switchSendButton(boolean enable) {
        isSendText = enable;
        if( enable ) {
            send.setVisibility(View.VISIBLE);
            voice_send.setVisibility(View.GONE);
        } else {
            voice_send.setVisibility(View.VISIBLE);
            send.setVisibility(View.GONE);
        }
    }

    private void clickEmoj() {
        if( isEmojShow ) {
            showKeyboard();
            findViewById(R.id.emoj_menu).setVisibility(View.GONE);

            emoj.setImageResource(R.drawable.emoj);
            isEmojShow = false;
        }
        else {
            reset();
            hideKeyBoard();
            findViewById(R.id.emoj_menu).setVisibility(View.VISIBLE);

            emoj.setImageResource(R.drawable.keyboard);
            isEmojShow = true;
        }
    }

    private void clickAddMore() {
        if( isAddMoreShow ) {
            showKeyboard();
            add_view.setVisibility(View.GONE);

            add_more.setImageResource(R.drawable.add_more);
            isAddMoreShow = false;
        }
        else {
            reset();
            hideKeyBoard();
            add_view.setVisibility(View.VISIBLE);

            add_more.setImageResource(R.drawable.keyboard);
            isAddMoreShow = true;
        }
    }

    private void reset() {
        if( !isSendText ) {
            voice_send.setImageResource(R.drawable.voice_nor);
        }
        emoj.setImageResource(R.drawable.emoj);
        add_more.setImageResource(R.drawable.add_more);

        add_view.setVisibility(View.GONE);
        findViewById(R.id.emoj_menu).setVisibility(View.GONE);

        isEmojShow = false;
        isAddMoreShow = false;
    }

    private void appendTextToInputText(String text, EditText mInputText) {

        if (mInputText != null && smilyManager != null && text != null) {
            int before = mInputText.getSelectionStart();
            int end = mInputText.getSelectionEnd();
            CharSequence span = smilyManager.getSmilySpan(text);
            mInputText.getText().replace(before, end, span);

        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if( adapterView.getId() == R.id.grid_emoj ) {
           int index = Integer.valueOf(emojAdapter.getItem(i).toString());
           appendTextToInputText( SmilyManager.getInstance().getShortCut( index - R.drawable.eaa ) , message_input );
        }
        if( adapterView.getId() == R.id.grid_add_more ) {
            UIHelper.makeToast("功能未完成...");
        }
    }
}
