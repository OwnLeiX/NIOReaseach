package lx.own.research;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private FloatingActionButton fab_connect, fab_disconnect, fab_instruction1;
    private EditText et_address;
    private RecyclerView rv_sended, rv_received;

    private Selector mSelector;
    private Thread mReaderThread;
    private final ArrayMap<String, ArrayList<SocketChannel>> mAddressMap = new ArrayMap<>();
    private Charset mCharset = Charset.forName("UTF-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fab_connect = findViewById(R.id.fab_connect);
        fab_disconnect = findViewById(R.id.fab_disconnect);
        fab_instruction1 = findViewById(R.id.fab_instruction1);

        et_address = findViewById(R.id.et_address);

        rv_sended = findViewById(R.id.rv_sended);
        rv_received = findViewById(R.id.rv_received);

        fab_connect.setOnClickListener(this);
        fab_disconnect.setOnClickListener(this);
        fab_instruction1.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_connect:
                connect();
                break;
            case R.id.fab_disconnect:
                disconnect();
                break;
            case R.id.fab_instruction1:
                instruction1();
                break;
        }
    }

    private void instruction1() {
        final Editable text = et_address.getText();
        if (!TextUtils.isEmpty(text)) {
            final String address = text.toString();
            new Thread() {
                @Override
                public void run() {
                    ArrayList<SocketChannel> socketChannels = mAddressMap.get(address);
                    if (socketChannels != null && socketChannels.size() > 0) {
                        SocketChannel socketChannel = socketChannels.get(0);
                        try {
                            socketChannel.write(buildInstruction1());
                        } catch (IOException e) {

                        }
                    }
                }
            }.start();
        }
    }

    private ByteBuffer buildInstruction1() {
        return mCharset.encode("shutdown");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSelector != null) {
            try {
                mSelector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (mReaderThread != null)
            mReaderThread.interrupt();
    }

    private void connect() {
        final Editable text = et_address.getText();
        if (!TextUtils.isEmpty(text)) {
            final String address = text.toString();
            new Thread() {
                @Override
                public void run() {
                    try {
                        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress(address, 3333));
                        socketChannel.configureBlocking(false);
                        ByteBuffer connectBytes = buildConnectBytes(socketChannel.socket().getLocalSocketAddress());
                        if (connectBytes != null) {
                            connectBytes.flip();
                            while (connectBytes.hasRemaining()) {
                                socketChannel.write(connectBytes);
                            }
                        }
                        ArrayList<SocketChannel> socketChannels = mAddressMap.get(address);
                        if (socketChannels == null)
                            socketChannels = new ArrayList<>();
                        socketChannels.add(socketChannel);
                        mAddressMap.put(address, socketChannels);
                        if (buildSelector()) {
                            socketChannel.register(mSelector, SelectionKey.OP_READ);
                            buildReader();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }

    private void disconnect() {
        final Editable text = et_address.getText();
        if (!TextUtils.isEmpty(text)) {
            final String address = text.toString();
            new Thread() {
                @Override
                public void run() {
                    List<SocketChannel> socketChannels = mAddressMap.remove(address);
                    if (socketChannels != null) {
                        Iterator<SocketChannel> iterator = socketChannels.iterator();
                        while (iterator.hasNext()) {
                            try {
                                SocketChannel socketChannel = iterator.next();
                                socketChannel.finishConnect();
                                socketChannel.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                iterator.remove();
                            }
                        }
                    }
                }
            }.start();
        }
    }

    private ByteBuffer buildConnectBytes(SocketAddress localAddress) {
        return mCharset.encode(localAddress.toString());
    }

    private boolean buildSelector() {
        if (mSelector == null) {
            synchronized (this) {
                if (mSelector == null) {
                    try {
                        mSelector = Selector.open();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return mSelector != null;
    }

    private void buildReader() {
        if (mReaderThread == null) {
            synchronized (this) {
                if (mReaderThread == null) {
                    mReaderThread = new Thread() {
                        @Override
                        public void run() {
                            if (buildSelector()) {
                                while (!isInterrupted()) {
                                    int count = 0;
                                    try {
                                        count = mSelector.select();
                                    } catch (IOException ignore) {

                                    }
                                    if (count <= 0) continue;
                                    Set<SelectionKey> selectionKeys = mSelector.selectedKeys();
                                    Iterator<SelectionKey> iterator = selectionKeys.iterator();
                                    while (iterator.hasNext()) {
                                        SelectionKey selectionKey = iterator.next();
                                        if (selectionKey.isReadable()) {
                                            try {
                                                read((SocketChannel) selectionKey.channel());
                                            } catch (Exception ignore) {

                                            }
                                        }
                                        iterator.remove();
                                    }
                                }
                            }
                        }
                    };
                }
            }
        }
    }

    private void read(SocketChannel channel) {

    }
}
