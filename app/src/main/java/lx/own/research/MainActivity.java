package lx.own.research;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startService(new Intent(this, ServerService.class));
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        SocketChannel socketChannel = SocketChannel.open();
                        socketChannel.connect(new InetSocketAddress("127.0.0.1", 3333));
                        ByteBuffer buffer = ByteBuffer.allocate(48);
                        buffer.put("testData".getBytes());
                        buffer.flip();
                        socketChannel.write(buffer);
                        socketChannel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }
}
