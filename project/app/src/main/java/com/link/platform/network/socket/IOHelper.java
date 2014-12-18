package com.link.platform.network.socket;

import com.link.platform.util.Error;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by danyang.ldy on 2014/12/8.
 */
public class IOHelper {

    public static int read( SocketChannel channel , ByteBuffer buffer )
            throws IOException {

        if( channel == null )
            return Error.IO_NO_CHANNEL;

        long count = channel.read(buffer);
        if ( count > 0 ) {
            int position = buffer.position();
            buffer.flip();
            int readn = buffer.limit() - buffer.position();
            if( readn > 4 ) {
                int len = buffer.getInt();
                if( ( buffer.limit() - buffer.position() ) < len ) {
                    buffer.clear();
                    buffer.position(position);
                    return Error.IO_PROTOCOL_NO_COMPLETE;
                }
                return len;
            } else {
                return Error.IO_PROTOCOL_NO_COMPLETE;
            }
        } else if( count == -1 ) {
            return Error.IO_CLOSE;
        } else {
            return Error.IO_FAILURE;
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

}
