package lx.own.research;

import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final String SEND_MSG_FORMATTER = "Send To %1$s : %2$s";
    private final String RECV_MSG_FORMATTER = "Received From %1$s : %2$s";

    private FloatingActionButton fab_connect, fab_disconnect, fab_instruction1;
    private EditText et_address;
    private RecyclerView rv_sended, rv_received;
    private MAdapter mSendedAdapter, mReceivedAdapter;

    private Selector mSelector;
    private Thread mReaderThread;
    private final ArrayMap<String, ArrayList<SelectionKey>> mAddressMap = new ArrayMap<>();
    private Charset mCharset = Charset.forName("UTF-8");

    private MainThreadHandler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fab_connect = findViewById(R.id.fab_connect);
        fab_disconnect = findViewById(R.id.fab_disconnect);
        fab_instruction1 = findViewById(R.id.fab_instruction1);

        et_address = findViewById(R.id.et_address);

        rv_sended = findViewById(R.id.rv_sended);
        rv_sended.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mSendedAdapter = new MAdapter();
        rv_sended.setAdapter(mSendedAdapter);

        rv_received = findViewById(R.id.rv_received);
        rv_received.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mReceivedAdapter = new MAdapter();
        rv_received.setAdapter(mReceivedAdapter);
        mHandler = new MainThreadHandler(mReceivedAdapter, mSendedAdapter);
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
                        if (buildSelector()) {
                            ByteBuffer byteBuffer = ByteBuffer.allocate(128);
                            synchronized (MainActivity.this) {
                                if (mReaderThread != null) {
                                    mReaderThread.interrupt();
                                    mReaderThread = null;
                                    mSelector.wakeup();
                                }
                            }
                            SelectionKey selectionKey = socketChannel.register(mSelector, SelectionKey.OP_READ, byteBuffer);
                            buildReader();
                            ArrayList<SelectionKey> socketChannels = mAddressMap.get(address);
                            if (socketChannels == null)
                                socketChannels = new ArrayList<>();
                            socketChannels.add(selectionKey);
                            mAddressMap.put(address, socketChannels);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }

    private void instruction1() {
        final Editable text = et_address.getText();
        if (!TextUtils.isEmpty(text)) {
            final String address = text.toString();
            new Thread() {
                @Override
                public void run() {
                    ArrayList<SelectionKey> keys = mAddressMap.get(address);
                    if (keys != null && keys.size() > 0) {
                        final SelectionKey key = keys.get(0);
                        final SocketChannel socketChannel = (SocketChannel) key.channel();
                        try {
                            socketChannel.write(buildInstruction1("explorer c:"));
                            sendMsg(MainThreadHandler.MSG_APPEND_SENDED, String.format(SEND_MSG_FORMATTER, address, "explorer c:"));
                            key.interestOps(SelectionKey.OP_READ);
                        } catch (IOException e) {
                            try {
                                keys.remove(key);
                                key.cancel();
                                socketChannel.finishConnect();
                                socketChannel.close();
                                sendMsg(MainThreadHandler.MSG_APPEND_RECEIVED, String.format(RECV_MSG_FORMATTER, address, "write Error, finish connect."));
                            } catch (IOException e1) {
                            }
                        }
                    }
                }
            }.start();
        }
    }

    private ByteBuffer buildInstruction1(String instruction) {
        return mCharset.encode(instruction);
    }

    private void disconnect() {
        final Editable text = et_address.getText();
        if (!TextUtils.isEmpty(text)) {
            final String address = text.toString();
            new Thread() {
                @Override
                public void run() {
                    final List<SelectionKey> keys = mAddressMap.remove(address);
                    if (keys != null && keys.size() > 0) {
                        final Iterator<SelectionKey> iterator = keys.iterator();
                        while (iterator.hasNext()) {
                            try {
                                final SelectionKey key = iterator.next();
                                key.cancel();
                                final SocketChannel socketChannel = (SocketChannel) key.channel();
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

    private ByteBuffer buildConnectBytes(InetAddress localAddress) {
        return mCharset.encode(localAddress.getHostName());
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
                                    final Set<SelectionKey> selectionKeys = mSelector.selectedKeys();
                                    final Iterator<SelectionKey> iterator = selectionKeys.iterator();
                                    while (iterator.hasNext()) {
                                        final SelectionKey selectionKey = iterator.next();
                                        iterator.remove();
                                        if (selectionKey.isReadable()) {
                                            read(selectionKey);
                                        }
                                    }
                                }
                            }
                        }
                    };
                    mReaderThread.start();
                }
            }
        }
    }

    private void read(SelectionKey key) {
        ByteBuffer byteBuffer = null;
        SocketChannel channel = null;
        try {
            channel = (SocketChannel) key.channel();
            byteBuffer = ByteBuffer.allocate(1024);
            byteBuffer.order(ByteOrder.nativeOrder());
            int read = channel.read(byteBuffer);
            if (read == -1) {
                key.cancel();
            }
            byteBuffer.flip();
            final CharBuffer charBuffer = mCharset.decode(byteBuffer);
            final StringBuilder stringBuilder = new StringBuilder();
            while (charBuffer.hasRemaining()) {
                stringBuilder.append(charBuffer.get());
            }
            charBuffer.clear();
            final String text = stringBuilder.toString();
            sendMsg(MainThreadHandler.MSG_APPEND_RECEIVED, String.format(RECV_MSG_FORMATTER, channel.socket().getInetAddress().getHostName(), text));
            key.interestOps(SelectionKey.OP_READ);
        } catch (Exception e) {
            try {
                final String address = channel.socket().getInetAddress().getHostName();
                ArrayList<SelectionKey> keys = mAddressMap.get(address);
                if (keys != null)
                    keys.remove(key);
                key.cancel();
                channel.finishConnect();
                channel.close();
                sendMsg(MainThreadHandler.MSG_APPEND_RECEIVED, String.format(RECV_MSG_FORMATTER, address, "read Error, finish connect."));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } finally {
            if (byteBuffer != null)
                byteBuffer.clear();
        }
    }

    private void sendMsg(int what, Object obj) {
        final MainThreadHandler handler = this.mHandler;
        if (handler != null)
            handler.sendMessage(Message.obtain(handler, what, obj));
    }
}
