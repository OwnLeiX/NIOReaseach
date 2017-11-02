package lx.own.research;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onClick(View v) {
        new Thread() {
            @Override
            public void run() {
                DatagramChannel socketChannel = null;
                try {
                    socketChannel = DatagramChannel.open();
                    byte[] data = "xxxx".getBytes("UTF-8");
                    ByteBuffer buffer = ByteBuffer.allocate(data.length);
                    buffer.put(data);
                    buffer.flip();
                    socketChannel.send(buffer, new InetSocketAddress("192.168.3.19", 3333));
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (socketChannel != null) {
                        try {
                            socketChannel.close();
                        } catch (IOException ignore) {

                        } finally {
                            socketChannel = null;
                        }
                    }
                }
            }
        }.start();
    }
}
