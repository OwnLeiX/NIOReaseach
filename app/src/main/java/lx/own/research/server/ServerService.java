package lx.own.research.server;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * <b> </b><br/>
 *
 * @author LeiXun
 *         Created on 2017/10/26.
 */

public class ServerService extends Service {
    private boolean running = true;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        new Thread() {
            @Override
            public void run() {
                try {
                    ServerSocketChannel socketChannel = ServerSocketChannel.open();
                    socketChannel.socket().bind(new InetSocketAddress(3333));
                    while (running) {
                        SocketChannel accept = socketChannel.accept();
                        ByteBuffer buffer = ByteBuffer.allocate(48);
                        StringBuilder builder = new StringBuilder();
                        int read = accept.read(buffer);
                        while (read != -1) {
                            buffer.flip();
                            while (buffer.hasRemaining()) {
                                builder.append(buffer.get());
                            }
                            Log.wtf("TTTTTT", "run: " + builder.toString());
                            buffer.clear();
                            read = accept.read(buffer);
                        }
                        accept.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        running = false;
    }
}
