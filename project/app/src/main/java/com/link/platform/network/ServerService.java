package com.link.platform.network;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.link.platform.message.BaseMessage;
import com.link.platform.message.MessageCenter;
import com.link.platform.message.MessageListenerDelegate;
import com.link.platform.util.Utils;

public class ServerService extends Service implements MessageListenerDelegate {

    public final static String TAG = "ServerService";

    public static boolean isInitServer = false;

    private BaseServer server = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        flags = START_STICKY;

        Log.d(TAG, "Start Server service");
        if( server == null && isInitServer ) {
            server = new ChatServer(Utils.CHAT_PORT);
            server.start();
        }

        return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Stop Server");
        Intent localIntent = new Intent();
        localIntent.setClass(this, ServerService.class);
        MessageCenter.getInstance().removeListener( this );
        if( server != null ) {
            server.stop();
            server = null;
        }
        this.startService(localIntent);
        super.onDestroy();
    }


    @Override
    public void onListenerExit() {

    }

    @Override
    public void getMessage(BaseMessage message) {

    }
}
