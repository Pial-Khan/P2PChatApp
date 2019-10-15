package com.example.socket;

import android.content.Context;
import android.graphics.Color;
import android.nfc.Tag;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;


public class ChatMessage extends BaseAdapter {
    private List<messageItem> messageList;
    Context context;
    static final String TAG = "P2PChatApp";

    public ChatMessage(Context context, List<messageItem>messageList){

        this.context = context;
        this.messageList = messageList;

    }

    @Override
    public int getCount() {
        return messageList.size();
    }

    @Override
    public Object getItem(int position) {
        return messageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @NonNull View convertView, @NonNull ViewGroup parent) {
        View v = View.inflate(context, R.layout.chat_messages, null);

        TextView messageText = v.findViewById(R.id.message_userText);
        TextView messageText2 = v.findViewById(R.id.message_userText2);
        LinearLayout sender = v.findViewById(R.id.sender);
        LinearLayout receiver = v.findViewById(R.id.receiver);

        if(messageList.get(position).getIp().equals("10")){
            messageText2.setText(messageList.get(position).getMsg());
            messageText.setVisibility(View.INVISIBLE);
            receiver.setVisibility(View.INVISIBLE);

        }
        else{
            messageText2.setVisibility(View.INVISIBLE);
            messageText.setText(messageList.get(position).getMsg());
            sender.setVisibility(View.INVISIBLE);
        }



        return v;
    }
}

