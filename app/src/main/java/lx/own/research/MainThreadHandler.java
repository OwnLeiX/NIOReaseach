package lx.own.research;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * <b> </b><br/>
 *
 * @author LeiXun
 * Created on 2019/1/9.
 */
public class MainThreadHandler extends Handler {

    public static final int MSG_APPEND_RECEIVED = 1;
    public static final int MSG_APPEND_SENDED = 2;

    private final WeakReference<MAdapter> mReceivedRef, mSendedRef;

    public MainThreadHandler(MAdapter received, MAdapter sended) {
        super(Looper.getMainLooper());
        mReceivedRef = new WeakReference<>(received);
        mSendedRef = new WeakReference<>(sended);
    }

    @Override
    public void handleMessage(Message msg) {
        try {
            if (msg.what == MSG_APPEND_RECEIVED) {
                final MAdapter a = mReceivedRef.get();
                if (a != null) a.append((String) msg.obj);
            } else if (msg.what == MSG_APPEND_SENDED) {
                final MAdapter a = mSendedRef.get();
                if (a != null) a.append((String) msg.obj);
            }
        } catch (ClassCastException ignore) {

        }
    }
}
