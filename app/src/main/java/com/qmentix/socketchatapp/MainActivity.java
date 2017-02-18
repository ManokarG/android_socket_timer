package com.qmentix.socketchatapp;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.qmentix.socketchatapp.model.MessageModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final int SOCKET_MSG = 1;
    private static final int SOCKET_CONNECT = 2;
    private static final int SOCKET_DISCONNECT = 3;

    private ListView lvMessage;
    private EditText etMessage;
    private Button btnSend;
    private TextView tvConnectionStatus;
    private MessageListAdapter msgListAdapter;

    private Socket mSocket;
    private List<MessageModel> mMessageList = new ArrayList<>();

    {
        try {
            mSocket = IO.socket("http://10.0.3.2:3000");
        } catch (URISyntaxException e) {
        }
    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.arg1) {
                case SOCKET_CONNECT:
                    showToast(R.string.connect_server_msg);
                    connectStatus();
                    lvMessage.setEnabled(true);
                    etMessage.setEnabled(true);
                    btnSend.setEnabled(true);

                    break;
                case SOCKET_DISCONNECT:
                    discconectStatus();
                    showToast(R.string.disconnect_server_msg);
                    lvMessage.setEnabled(false);
                    etMessage.setEnabled(false);
                    btnSend.setEnabled(false);
                    break;
                case SOCKET_MSG:
                    String message = (String) msg.obj;
                    MessageModel model = new MessageModel();
                    model.setMsg(message);
                    model.setUser(MessageModel.SERVER);
                    mMessageList.add(model);
                    msgListAdapter.notifyDataSetChanged();
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lvMessage = (ListView) findViewById(R.id.lvMessage);
        etMessage = (EditText) findViewById(R.id.etMessage);
        btnSend = (Button) findViewById(R.id.btnSend);
        tvConnectionStatus = (TextView) findViewById(R.id.tvConnectionStatus);
        discconectStatus();
        lvMessage.setEnabled(false);
        etMessage.setEnabled(false);
        btnSend.setEnabled(false);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String msg = etMessage.getText().toString();
                if (!TextUtils.isEmpty(msg)) {
                    mSocket.emit(Socket.EVENT_MESSAGE, msg);
                    MessageModel model = new MessageModel();
                    model.setMsg(msg);
                    model.setUser(MessageModel.CLIENT);
                    mMessageList.add(model);
                    msgListAdapter.notifyDataSetChanged();
                    etMessage.setText(null);
                } else {
                    showToast("Please enter message");
                }

            }
        });

        msgListAdapter = new MessageListAdapter(this, lvMessage, mMessageList);
        lvMessage.setAdapter(msgListAdapter);

    }

    private void discconectStatus() {
        tvConnectionStatus.setVisibility(View.VISIBLE);
        tvConnectionStatus.setText(R.string.disconnect_status);
        tvConnectionStatus.setBackgroundColor(getColor(MainActivity.this, R.color.disconnect_bg_color));
        animateStatusTextView();
    }

    private void connectStatus() {
        tvConnectionStatus.setVisibility(View.VISIBLE);
        tvConnectionStatus.setText(R.string.connect_status);
        tvConnectionStatus.setBackgroundColor(getColor(MainActivity.this, R.color.connect_bg_color));
        animateStatusTextView();
    }

    private void animateStatusTextView() {
        ViewCompat.animate(tvConnectionStatus)
                .scaleY(tvConnectionStatus.getY())
                .setDuration(3000)
                .setListener(new ViewPropertyAnimatorListener() {
                    @Override
                    public void onAnimationStart(View view) {

                    }

                    @Override
                    public void onAnimationEnd(View view) {
                        tvConnectionStatus.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(View view) {

                    }
                });
    }

    private void connectingStatus() {
        tvConnectionStatus.setVisibility(View.VISIBLE);
        tvConnectionStatus.setText(R.string.connecting_status);
        tvConnectionStatus.setBackgroundColor(getColor(MainActivity.this, R.color.connecting_bg_color));
        animateStatusTextView();
    }

    public static final int getColor(Context context, int id) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            return ContextCompat.getColor(context, id);
        } else {
            return context.getResources().getColor(id);
        }
    }

    @Override
    protected void onStart() {

        mSocket.on(Socket.EVENT_MESSAGE, messageListener);
        mSocket.on(Socket.EVENT_CONNECT, connectListener);
        mSocket.on(Socket.EVENT_DISCONNECT, disconnectListener);

        mSocket.connect();

        super.onStart();
    }

    @Override
    protected void onStop() {
        mSocket.off(Socket.EVENT_CONNECT);
        mSocket.off(Socket.EVENT_DISCONNECT);
        mSocket.off(Socket.EVENT_MESSAGE);
        mSocket.disconnect();
        super.onStop();
    }

    private final Emitter.Listener disconnectListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Message msg = handler.obtainMessage();
            msg.arg1 = SOCKET_DISCONNECT;
            handler.sendMessage(msg);
        }
    };

    private void showToast(@StringRes int msg) {
        showToast(getString(msg));
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private final Emitter.Listener connectListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Message msg = handler.obtainMessage();
            msg.arg1 = SOCKET_CONNECT;
            handler.sendMessage(msg);
        }
    };

    private final Emitter.Listener messageListener = new Emitter.Listener() {

        @Override
        public void call(final Object... args) {

            JSONObject data = (JSONObject) args[0];
            try {
                String strMsg = data.getString("message");
                Message msg = handler.obtainMessage();
                msg.arg1 = SOCKET_MSG;
                msg.obj = strMsg;
                handler.sendMessage(msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    };

}
