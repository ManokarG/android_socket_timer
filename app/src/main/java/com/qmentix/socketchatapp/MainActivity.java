package com.qmentix.socketchatapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final int SOCKET_MSG = 1;
    private static final int SOCKET_CONNECT = 2;
    private static final int SOCKET_DISCONNECT = 3;

    private TextView tvTitle;

    private Socket mSocket;

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
                    break;
                case SOCKET_DISCONNECT:
                    showToast(R.string.disconnect_server_msg);
                    break;
                case SOCKET_MSG:
                    tvTitle.setText((String) msg.obj);
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvTitle = (TextView) findViewById(R.id.tvText);
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
