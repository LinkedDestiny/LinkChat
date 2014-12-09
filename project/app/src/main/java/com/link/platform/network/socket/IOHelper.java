package com.link.platform.network.socket;

import com.link.platform.util.Error;

import java.io.IOException;
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
                int len = buffer.getInt(0);
                if( ( buffer.limit() - buffer.position() ) < len ) {
                    buffer.reset();
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


}
