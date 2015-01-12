package com.link.platform.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.link.platform.R;
import com.link.platform.file.FileManager;
import com.link.platform.item.MenuItem;
import com.link.platform.item.MessageItem;
import com.link.platform.media.audio.AudioManager;
import com.link.platform.message.BaseMessage;
import com.link.platform.message.MessageCenter;
import com.link.platform.message.MessageListenerDelegate;
import com.link.platform.message.MessageTable;
import com.link.platform.message.MessageWithObject;
import com.link.platform.network.BaseClient;
import com.link.platform.network.ServerService;
import com.link.platform.network.util.IOHelper;
import com.link.platform.network.util.MsgType;
import com.link.platform.ui.adapter.AddMoreAdapter;
import com.link.platform.ui.adapter.EmojAdapter;
import com.link.platform.ui.adapter.MessageAdapter;
import com.link.platform.util.SmilyManager;
import com.link.platform.util.UIHelper;
import com.link.platform.util.Utils;
import com.link.platform.wifi.ap.APManager;
import com.link.platform.wifi.wifi.WiFiManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ConversationActivity extends Activity implements MessageListenerDelegate , View.OnClickListener
                                                , TextWatcher , AdapterView.OnItemClickListener
                                                , View.OnTouchListener {

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

    private ImageView audio_record;
    private ImageView audio_cancel;

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

    private boolean isVoice = false;

    private Handler handler = new Handler();

    private File tempFile;

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
        MessageCenter.getInstance().registerListener(this , MessageTable.MSG_SERVER_CLOSE );

        MessageCenter.getInstance().registerListener(this , MessageTable.MSG_TEXT );
        MessageCenter.getInstance().registerListener(this , MessageTable.MSG_IMG );
        MessageCenter.getInstance().registerListener(this , MessageTable.MSG_VOICE );
        MessageCenter.getInstance().registerListener(this , MessageTable.MSG_FILE );
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
        audio_record = (ImageView)findViewById(R.id.audio_record_button);
        audio_cancel = (ImageView)findViewById(R.id.audio_cancel);

        back.setOnClickListener(this);
        settings.setOnClickListener(this);
        emoj.setOnClickListener(this);
        add_more.setOnClickListener(this);
        send.setOnClickListener(this);
        voice_send.setOnClickListener(this);
        audio_record.setOnTouchListener(this);
        audio_cancel.setOnTouchListener(this);

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
            if( IOHelper.isHost(host_ip) ) {
                own = host_ip;
            } else {
                own = IOHelper.ipIntToString(WiFiManager.getInstance().getIP());
            }
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
        else if( id.equals(MessageTable.MSG_VOICE ) ) {
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
        else if( id.equals(MessageTable.MSG_FILE ) ) {
            Log.d(TAG, "recv FILE");
            MessageItem item = (MessageItem)msg.getObject();
            list.add(item);
            adapter.notifyDataSetChanged();
        }
        else if( id.equals(MessageTable.MSG_IMG ) ) {

            MessageItem item = (MessageItem)msg.getObject();
            Log.d(TAG, "recv IMG " + item.msg_type );
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
            case R.id.voice_send:
            {
                clickVoice();
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

    private void clickVoice() {
        if( isVoice ) {
            showKeyboard();
            findViewById(R.id.voice_panel).setVisibility(View.GONE);

            voice_send.setImageResource(R.drawable.voice_nor);
            isVoice = false;
        }
        else {
            reset();
            hideKeyBoard();
            findViewById(R.id.voice_panel).setVisibility(View.VISIBLE);

            voice_send.setImageResource(R.drawable.keyboard);
            isVoice = true;
        }
    }

    private void reset() {
        if( !isSendText ) {
            voice_send.setImageResource(R.drawable.voice_nor);
        }
        emoj.setImageResource(R.drawable.emoj);
        add_more.setImageResource(R.drawable.add_more);
        voice_send.setImageResource(R.drawable.voice_nor);

        add_view.setVisibility(View.GONE);
        findViewById(R.id.emoj_menu).setVisibility(View.GONE);
        findViewById(R.id.voice_panel).setVisibility(View.GONE);

        isEmojShow = false;
        isAddMoreShow = false;
        isVoice = false;
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
            tempFile = FileManager.getInstance().createTempFile(Utils.IMG_CACHE);
            switch (i) {
                case MenuItem.MENU_FILE:
                {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    try {
                        startActivityForResult(Intent.createChooser(intent, "请选择一个要上传的文件"),
                                Utils.FILE_SELECT_CODE);
                    } catch (android.content.ActivityNotFoundException ex) {
                        UIHelper.makeToast("请安装文件管理器");
                    }
                    break;
                }
                case MenuItem.MENU_PIC:
                {
                    Intent intent = new Intent(Intent.ACTION_PICK);// 打开相册
                    intent.setDataAndType(MediaStore.Images.Media.INTERNAL_CONTENT_URI, "image/*");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
                    startActivityForResult(intent, Utils.OPEN_GALLERY_CODE);
                    break;
                }
                case MenuItem.MENU_CAMERA:
                {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);// 打开相机
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
                    startActivityForResult(intent, Utils.OPEN_CAMERA_CODE);
                    break;
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( resultCode == RESULT_OK ) {
            switch (requestCode) {
                case Utils.FILE_SELECT_CODE:
                {
                    Uri uri = data.getData();
                    Cursor cursor = getContentResolver().query(uri, null, null, null,null);
                    if (cursor != null && cursor.moveToFirst()) {
                        String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                        FileManager.getInstance().sendFile(path, MsgType.MSG_FILE);
                        list.add( MessageItem.fileMessage("" , true, uri.getPath()) );
                        adapter.notifyDataSetChanged();
                    }
                    break;
                }
                case Utils.OPEN_GALLERY_CODE:
                {
                    Uri uri = data.getData();
                    Cursor cursor = getContentResolver().query(uri, null, null, null,null);
                    if (cursor != null && cursor.moveToFirst()) {
                        String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                        FileManager.getInstance().sendFile(path, MsgType.MSG_IMG);

                        list.add( MessageItem.imgMessage("" , true, path) );
                        adapter.notifyDataSetChanged();
                    }

                    break;
                }
                case Utils.OPEN_CAMERA_CODE:
                {
                    FileManager.getInstance().sendFile(tempFile.getPath(), MsgType.MSG_IMG);
                    list.add( MessageItem.imgMessage("" , true, tempFile.getPath()) );
                    adapter.notifyDataSetChanged();
                    break;
                }
            }
        }
    }

        private void clickable( boolean clickable ) {
        emoj.setEnabled(clickable);
        add_more.setEnabled(clickable);
        message_input.setEnabled(clickable);
        voice_send.setEnabled(clickable);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
            {
                Log.d(TAG, "action down: ");
                clickable(false);
                AudioManager.getInstance().startRecording(AudioManager.MODE_SHORT_VOICE);
                handler.postDelayed( new Runnable() {
                    @Override
                    public void run() {
                        AudioManager.getInstance().isValid();
                    }
                } , 500 );
                audio_cancel.setVisibility(View.INVISIBLE);
                return true;
            }
            case MotionEvent.ACTION_MOVE:
            {
                float x = motionEvent.getX();
                float y = motionEvent.getY();
                // TODO make del icon bigger or smaller
                return true;
            }
            case MotionEvent.ACTION_UP:
            {
                Log.d(TAG, "action up: ");
                clickable(true);
                audio_cancel.setVisibility(View.GONE);
                if( view.getId() == R.id.audio_cancel ) {
                    // TODO 取消录音
                    Log.d(TAG, "cancel recording");
                    AudioManager.getInstance().cancelRecording();

                }
                else if( view.getId() == R.id.audio_record_button ) {
                    boolean flag = AudioManager.getInstance().stopRecording();
                    if( !flag ) {
                        //TODO 时间过短的提醒
                    }
                }
                return true;
            }
        }
        return false;
    }

    private void cropPhoto(Uri uri) {
        File tempFile = FileManager.getInstance().createTempFile(Utils.IMG_CACHE);
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("output", Uri.fromFile(tempFile));
        intent.putExtra("crop", true);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
        startActivityForResult(intent, Utils.CROP_PHOTO_CODE);
    }
}
