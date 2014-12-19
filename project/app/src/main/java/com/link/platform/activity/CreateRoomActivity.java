package com.link.platform.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.link.platform.R;
import com.link.platform.message.BaseMessage;
import com.link.platform.message.MessageCenter;
import com.link.platform.message.MessageListenerDelegate;
import com.link.platform.message.MessageTable;
import com.link.platform.message.MessageWithObject;
import com.link.platform.network.BaseController;
import com.link.platform.network.ServerService;
import com.link.platform.util.StringUtil;
import com.link.platform.util.UIHelper;
import com.link.platform.wifi.ap.APManager;
import com.link.platform.wifi.wifi.WiFiManager;

public class CreateRoomActivity extends Activity implements MessageListenerDelegate {

    private EditText name , password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_room);

        name = (EditText)findViewById(R.id.activity_room_name_edit);
        password = (EditText)findViewById(R.id.activity_room_password_edit);
    }

    @Override
    public void onResume() {
        super.onResume();
        MessageCenter.getInstance().registerListener(this , MessageTable.MSG_OPEN_AP_FINISH );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MessageCenter.getInstance().removeListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.create_room, menu);
        MenuItem next = menu.findItem( R.id.action_next );

        next.setShowAsAction( MenuItem.SHOW_AS_ACTION_IF_ROOM );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_next) {
            String password = this.password.getText().toString();
            if( !StringUtil.isBlank(password) && password.length() < 8 ) {
                UIHelper.makeToast("密码长度不能小于8位");
                return true;
            }
            APManager.getInstance().setWiFiAPInfo( name.getText().toString(), password );
            APManager.getInstance().toggleWiFiAP(this, true);
            hideKeyBoard();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void hideKeyBoard() {
        View view = getCurrentFocus();
        if (view != null) {
            ((InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onListenerExit() {

    }

    @Override
    public void getMessage(BaseMessage message) {
        String id = message.getMsgId();
        if( id.equals(MessageTable.MSG_OPEN_AP_FINISH)) {
            MessageWithObject msg = (MessageWithObject)message;
            boolean result = Boolean.valueOf(msg.getObject().toString());
            if( result ) {
                ServerService.isInitServer = true;
                Intent server = new Intent(this, ServerService.class);
                startService(server);

                Intent intent = new Intent(this , ConversationActivity.class );
                intent.putExtra(ConversationActivity.PARAM_ROOM_NAME , name.getText().toString() );
                intent.putExtra(ConversationActivity.PARAM_IS_HOST, true);

                startActivity(intent);
                this.finish();
            } else {
                UIHelper.makeToast("创建失败！请重试...");
            }
        }
    }
}
