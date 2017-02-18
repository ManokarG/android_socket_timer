package com.qmentix.socketchatapp;

import android.content.Context;
import android.database.DataSetObserver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.qmentix.socketchatapp.model.MessageModel;

import java.util.List;

/**
 * Project SocketChatApp Created by Qmentix on 18-02-2017/0018.
 */

public class MessageListAdapter extends ArrayAdapter<MessageModel> {

    private final List<MessageModel> mMsgList;

    public MessageListAdapter(Context context, final ListView list, List<MessageModel> msgList) {
        super(context, android.R.layout.simple_list_item_1);
        mMsgList = msgList;
        registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
//                list.smoothScrollToPosition(getCount());
            }
        });
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View view;
        final MessageModel msg = mMsgList.get(position);
        if (msg.getUser() == MessageModel.CLIENT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_msg_right, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_msg_left, parent, false);
        }
        TextView tvMsg = (TextView) view.findViewById(R.id.tvMessage);
        tvMsg.setText(msg.getMsg());
        return view;
    }

    @Override
    public int getCount() {
        return mMsgList.size();
    }

    @Nullable
    @Override
    public MessageModel getItem(int position) {
        return mMsgList.get(position);
    }
}
