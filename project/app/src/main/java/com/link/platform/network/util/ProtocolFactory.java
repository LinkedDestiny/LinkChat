package com.link.platform.network.util;

import com.link.platform.item.ContactItem;
import com.link.platform.network.socket.IOHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by danyang.ldy on 2014/12/17.
 */
public class ProtocolFactory {

    public static ByteBuffer parseProtocol(int type, String IP, byte[] content) {
        return parseProtocol(type, IP, content, content.length);
    }

    public static ByteBuffer parseProtocol(int type, String IP, byte[] content, int size) {
        int buff_size = 8 + content.length;
        ByteBuffer buffer = ByteBuffer.allocate(buff_size);
        buffer.putInt(type);
        buffer.putInt(IOHelper.ipToInt( IP ) );
        buffer.put(content, 0 , size);
        return buffer;
    }

    public static String parseOnlieList(Map<String, ContactItem> list) {
        JSONArray jsonObject = new JSONArray();
        try {
            Iterator<ContactItem> iter = list.values().iterator();
            while( iter.hasNext() ) {
                ContactItem item = iter.next();
                JSONObject contact = new JSONObject();
                contact.put("IP", item.IP);
                contact.put("name", item.name);

                jsonObject.put(contact);
            }
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static Map<String, ContactItem> getOnlineList(String str) {
        Map<String, ContactItem> list = new HashMap<String, ContactItem>();
        try {
            JSONArray jsonObject = new JSONArray(str);
            for(int i = 0 ; i < jsonObject.length(); i ++ ) {
                JSONObject contact = jsonObject.getJSONObject(i);
                ContactItem item = new ContactItem( contact.optString("IP"), contact.optString("name") , "");
                list.put(item.IP, item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }
}
