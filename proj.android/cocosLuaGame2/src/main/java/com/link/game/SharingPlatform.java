package com.link.game;

import android.content.Intent;
import android.util.Log;

import com.link.game.activity.GameServerService;
import com.link.game.message.MessageTable;
import com.link.game.network.GameClient;
import com.link.game.network.MsgType;
import com.link.game.util.Utils;
import com.link.platform.MainApplication;
import com.link.platform.item.MessageItem;
import com.link.platform.message.BaseMessage;
import com.link.platform.message.MessageCenter;
import com.link.platform.message.MessageListenerDelegate;
import com.link.platform.network.ServerService;

import org.cocos2dx.lib.Cocos2dxLuaJavaBridge;
import org.cocos2dx.lua.AppActivity;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by danyang.ldy on 2015/1/21.
 */
public class SharingPlatform implements MessageListenerDelegate {

    public final static String TAG = "SharingPlatform";

    public final static int SERVER = 1;
    public final static int CLIENT = 2;

    public final static String SEND_MSG = "SEND_MSG";

    private static SharingPlatform Instance;
    public static SharingPlatform getInstance() {
        if( Instance == null ) {
            synchronized( SharingPlatform.class ) {
                if( Instance == null ) {
                    Instance = new SharingPlatform();
                }
            }
        }
        return Instance;
    }

    private int role;
    private Map<String, Integer> callback_map;
    private GameClient client;
    
    private SharingPlatform() {
        callback_map = new HashMap<String, Integer>();
    }

    public static void INIT(int role) {
        getInstance().init(role);
    }

    public static void RegisteCallback(String tag, int callback) {
        getInstance().registeCallback(tag,callback);
    }

    public static void SendMsg(String msg) {
        getInstance().sendMsg(msg);
    }

    public static int GetRole() {
        return getInstance().getRole();
    }

    public static void RELEASE() {
        getInstance().release();
    }

    public static void Log(String msg) {
        Log.d(TAG, msg);
    }

    private void init(int role) {
        setRole(role);
        MessageCenter.getInstance().registerListener(this, MessageTable.MSG_SERVER_START);
        if( role == SERVER ) {
            launchServer();
        } else {
            launchClient();
        }
    }


    private void release() {
        MessageCenter.getInstance().removeListener(this);
        releaseClient();
        if( role == SERVER ) {
            releaseServer();
        }
    }

    private void launchServer() {
        GameServerService.isInitServer = true;
        Intent server = new Intent(AppActivity.Instance, GameServerService.class);
        AppActivity.Instance.startService(server);
    }

    private void releaseServer() {
        Intent server = new Intent(AppActivity.Instance, GameServerService.class);
        AppActivity.Instance.stopService(server);
    }

    private void launchClient() {
        String hostIp = "192.168.43.1";
        if( role == SERVER ) {
            hostIp = "127.0.0.1";
        }
        client = new GameClient(hostIp, Utils.GAME_PORT);
        client.start();
    }

    private void releaseClient() {
        if( client != null ) {
            client.stop();
        }
    }

    private void registeCallback(String tag, int callback) {
        callback_map.put(tag, callback);
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public void sendMsgToLua(String msg) {
        Log.d(TAG, msg);
        int callback = callback_map.get(SEND_MSG);
        Cocos2dxLuaJavaBridge.callLuaFunctionWithString(callback, msg);
    }

    public void sendMsg(String msg) {
        MessageItem item = new MessageItem(client.getIP(), true, msg, MsgType.MSG_MOVE, false);
        client.sendMessage(item);
    }

    @Override
    public void onListenerExit() {

    }

    @Override
    public void getMessage(BaseMessage baseMessage) {
        if( )01239189271s(MessageTable.MSG_SERVER_START) ) {
            Log.d(TAG, "Server start");
            launchClient();
        }
    }
}
