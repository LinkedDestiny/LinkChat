package com.link.platform.network.socket;

import android.util.Log;

import com.link.platform.util.Error;
import com.link.platform.util.Utils;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by danyang.ldy on 2014/12/8.
 */
public class IOHelper {

    public static int read( SocketChannel channel , ByteBuffer buffer ) {
        if( channel == null )
            return Error.IO_NO_CHANNEL;

         long count = 0;
         try {
             count = channel.read(buffer);
         } catch (IOException e) {
             e.printStackTrace();
             return Error.IO_CLOSE;
         }
         if ( count > 0 || ( count == 0 && ( buffer.position() == buffer.limit() ) ) ) {
            buffer.flip();
            if( buffer.remaining() > 4 ) {
                int len = buffer.getInt();
                Log.d("IOHelper", "len = " + len );
                if( len < 0 || len > Utils.BUFFER_SIZE ) {
                    return Error.IO_FAILURE;
                } else if( buffer.remaining() < len ) {
                    buffer.position( buffer.position() - 4 );
                    buffer.compact();
                    return Error.IO_PROTOCOL_NO_COMPLETE;
                }
                return len;
            } else {
                return Error.IO_PROTOCOL_NO_COMPLETE;
            }
        } else {
            return Error.IO_CLOSE;
        }
    }

    // change ip from int to String
    public static  String ipIntToString(int ip) {
        try {
            byte[] bytes = new byte[4];
            bytes[0] = (byte) (0xff & ip);
            bytes[1] = (byte) ((0xff00 & ip) >> 8);
            bytes[2] = (byte) ((0xff0000 & ip) >> 16);
            bytes[3] = (byte) ((0xff000000 & ip) >> 24);
            return Inet4Address.getByAddress(bytes).getHostAddress();

        } catch (Exception e) {
            return "";
        }
    }

    // change ip from String to int
    public static int ipToInt(String ipAddr) {
        try {
            byte[] bytes = InetAddress.getByName(ipAddr).getAddress();
            int addr = bytes[0] & 0xFF;
            addr |= ((bytes[1] << 8 ) & 0xFF00);
            addr |= ((bytes[2] << 16) & 0xFF0000);
            addr |= ((bytes[3] << 24) & 0xFF000000);
            return  addr;
        } catch (Exception e) {
            throw new IllegalArgumentException(ipAddr + " is invalid IP");
        }
    }

    public static byte[] intToByteArray(int value) {
        byte[] src = new byte[4];
        src[3] =  (byte) ((value>>24) & 0xFF);
        src[2] =  (byte) ((value>>16) & 0xFF);
        src[1] =  (byte) ((value>>8) & 0xFF);
        src[0] =  (byte) (value & 0xFF);
        return src;
    }

    public static int bytesToInt(byte[] src, int offset) {
        int value;
        value = (int) ((src[offset] & 0xFF)
                | ((src[offset+1] & 0xFF)<<8)
                | ((src[offset+2] & 0xFF)<<16)
                | ((src[offset+3] & 0xFF)<<24));
        return value;
    }

}
