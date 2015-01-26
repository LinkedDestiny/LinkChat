package com.link.game.activity;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.link.game.message.MessageTable;
import com.link.game.network.GameServer;
import com.link.game.util.Utils;
import com.link.platform.message.MessageCenter;
import com.link.platform.message.MessageWithObject;

/**
 * Created by danyang.ldy on 2015/1/21.
 */
public class GameServerService extends Service {
    public final static String TAG = "GameServerService";

    public static boolean isInitServer = false;

    private GameServer server = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        flags = START_STICKY;
        Log.d(TAG, "Start Server service");
        if( server == null && isInitServer ) {
            Log.d(TAG, "Start Server");
            server = new GameServer(Utils.GAME_PORT);
            server.start();
        }
        MessageWithObject msg = new MessageWithObject();
        msg.setMsgId(MessageTable.MSG_SERVER_START);
        MessageCenter.getInstance().sendMessage(msg);

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
        localIntent.setClass(this, GameServerService.class);
        if( server != null ) {
            server.stop();
            server = null;
        }
        this.startService(localIntent);
        super.onDestroy();
    }
}
