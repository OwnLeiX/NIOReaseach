package lx.own.research;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private FloatingActionButton fab_connect, fab_disconnect, fab_verify;
    private EditText et_address;
    private RecyclerView rv_sended,rv_received;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fab_connect = findViewById(R.id.fab_connect);
        fab_disconnect = findViewById(R.id.fab_disconnect);
        fab_verify = findViewById(R.id.fab_verify);

        et_address = findViewById(R.id.et_address);

        rv_sended = findViewById(R.id.rv_sended);
        rv_received = findViewById(R.id.rv_received);

        fab_connect.setOnClickListener(this);
        fab_disconnect.setOnClickListener(this);
        fab_verify.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_connect:
                break;
            case R.id.fab_verify:
                break;
            case R.id.fab_shut:
                break;
        }
    }

    private void connect() {
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
