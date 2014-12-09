package com.link.platform;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;

import com.link.platform.message.MessageCenter;
import com.link.platform.message.MessageTable;
import com.link.platform.message.MessageWithObject;

/**
 * Created by danyang.ldy on 2014/12/8.
 */
public class MainReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if( action.equals( WifiManager.SCAN_RESULTS_AVAILABLE_ACTION ) ) {
            MessageWithObject msg = new MessageWithObject();
            msg.setMsgId(MessageTable.MSG_GET_SCAN_RESULT);
            MessageCenter.getInstance().sendMessage(msg);
        }
    }
}
