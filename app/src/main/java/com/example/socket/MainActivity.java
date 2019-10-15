package com.example.socket;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity{

    EditText receivePortEditText, targetPortEditText, messageEditText, targetIPEditText;
    Button hostButton, connectButton;
    Button changeColor;
    ListView chatList;

    Dialog dialog;

    ServerClass serverClass;
    ClientClass clientClass;
    SendReceive sendReceive;

    static final int MESSAGE_READ=1;
    static final String TAG = "P2PChatApp";

    String textFromFile= "";

    String chatMessage = "";
    String colorCode =  "#*white";

    Boolean setColor = false;

    private ChatMessage chatAdapter;
    private List<messageItem> ownChatList = new ArrayList<>();
    private List<messageItem> chatFullList = new ArrayList<>();

    private static final String FILE_NAME = "saveconvo.txt";

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    Handler handler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what)
            {
                case MESSAGE_READ:
                    byte[] readBuff= (byte[]) msg.obj;
                    String tempMsg=new String(readBuff,0,msg.arg1);
                    if(tempMsg.charAt(0) == '#') {
                        if (tempMsg.charAt(1) == '*') {

                            Log.d(TAG, "Color is: " + tempMsg);
                            if (tempMsg.equals("#*white")) {
                                setColor = false;
                                Log.d(TAG, "SetColor is: " + setColor);
                                chatList.setBackgroundColor(Color.parseColor("#FFFFFF"));
                            } else {
                                setColor = true;
                                Log.d(TAG, "SetColor is: " + setColor);
                                chatList.setBackgroundColor(Color.parseColor("#91ff47"));

                            }

                        }
                        if (tempMsg.charAt(1) == '@') {
                            tempMsg = tempMsg.replace("#@", "");
                            Toast.makeText(getApplicationContext(), "File saved in " + getFilesDir(), Toast.LENGTH_SHORT).show();
                            String fileText = tempMsg;
                            writeToFile("file", fileText, true);
                        }
                    }

                    else {
                        displayChat(tempMsg);
                    }
                    break;
            }
            return true;
        }
    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        messageEditText = findViewById(R.id.messageEditText);

        chatList = findViewById(R.id.list_of_message);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);


        changeColor = findViewById(R.id.buttonColor);

        dialog = new Dialog(this);

        verifyDataFolder();
        verifyStoragePermissions();

    }

    public void onConnectFriendClicked(View view){
        dialog.setContentView(R.layout.contact_user);

        receivePortEditText = dialog.findViewById(R.id.receiveEditText);
        targetPortEditText = dialog.findViewById(R.id.targetPortEditText);
        targetIPEditText = dialog.findViewById(R.id.targetIPEditText);

        hostButton = dialog.findViewById(R.id.hostButton);
        connectButton = dialog.findViewById(R.id.connectButton);

        hostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String port = receivePortEditText.getText().toString();

                serverClass = new ServerClass(Integer.parseInt(port));
                serverClass.start();
            }
        });

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String port = targetPortEditText.getText().toString();

                clientClass = new ClientClass(targetIPEditText.getText().toString(), Integer.parseInt(port));
                clientClass.start();
            }
        });


        dialog.show();

    }


    public void onColorClicked(View v){
        if(setColor == false){
            colorCode = "#*dark";
            setColor = true;
            chatList.setBackgroundColor(Color.parseColor("#91ff47"));

        }
        else {
            colorCode = "#*white";
            setColor = false;
            chatList.setBackgroundColor(Color.parseColor("#FFFFFF"));
        }

        new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    sendReceive.write(colorCode.getBytes());
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
    }

    public void onSaveClicked(View v) {

        String path = Environment.getExternalStorageDirectory().toString();
        File file = null;
        String newline = "\n";


        file = new File(path + "/Peer 2 Peer/Saved txt files", FILE_NAME);

        Toast.makeText(this, "Chat conversation is saved successfully", Toast.LENGTH_SHORT).show();

        FileOutputStream stream;
        try {
            stream = new FileOutputStream(file, false);
            for (int i = 0; i < chatFullList.size(); i++) {
                String a ="";
                String l = chatFullList.get(i).getIp();
                if(l.equals("10")){
                    a = a.concat("me : ");
                }
                else a =  a.concat("sender : ");
                stream.write(a.getBytes());
                stream.write(chatFullList.get(i).getMsg().getBytes());
                stream.write(newline.getBytes());
                Log.d(TAG, chatFullList.get(i).getIp() + " : " + chatFullList.get(i).getMsg() + "\n"); }
                stream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onSendTextFileClicked(View v){
        generateFileManagerWindow();
    }

    private void generateFileManagerWindow() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, 7);
    }


    public void onSendClicked(View v){
        chatMessage=messageEditText.getText().toString();
        Log.d(TAG,"Message is: "+ chatMessage);

        chatFullList.add(new messageItem(chatMessage, "10"));

        chatAdapter = new ChatMessage(this, chatFullList);
        chatList.setAdapter(chatAdapter);

        new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    sendReceive.write(chatMessage.getBytes());
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
    }

    public class ServerClass extends Thread{
        Socket socket;
        ServerSocket serverSocket;
        int port;

        public ServerClass(int port) {

            this.port = port;
        }

        @Override
        public void run() {
            try {
                serverSocket=new ServerSocket(port);
                Log.d(TAG, "Waiting for client...");
                socket=serverSocket.accept();
                Log.d(TAG, "Connection established from server");
                sendReceive= new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "ERROR/n"+e);
            }
        }
    }

    private class SendReceive extends Thread{
        private Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;


        public SendReceive(Socket skt)
        {
            socket=skt;
            try {
                inputStream=socket.getInputStream();
                outputStream=socket.getOutputStream();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (socket != null) {
                try {
                    bytes = inputStream.read(buffer);

                    if (bytes > 0) {
                        handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] msgbytes){

            try {
                outputStream.write(msgbytes);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public class ClientClass extends Thread{
        Socket socket;
        String hostAdd;
        int port;

        public  ClientClass(String hostAddress, int port)
        {
            this.port = port;
            this.hostAdd = hostAddress;
        }

        @Override
        public void run() {
            try {

                socket=new Socket(hostAdd, port);
                Log.d(TAG, "Client is connected to server");
                sendReceive = new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "Can't connect from client/n"+e);
            }
        }
    }

    public void displayChat(String m){

        chatFullList.add(new messageItem(m ,"11"));

        chatAdapter = new ChatMessage(this, chatFullList);
        chatList.setAdapter(chatAdapter);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub

        super.onActivityResult(requestCode, resultCode, data);


        if (resultCode == RESULT_OK) {

            Uri uri = data.getData();
            String fileText = getTextFromUri(uri);
            textFromFile = "#@"+fileText;

            new Thread(new Runnable() {
                @Override
                public void run() {

                    Log.d(TAG, textFromFile);

                    sendReceive.write(textFromFile.getBytes());
                }
            }).start();

        }


    }

    public String getTextFromUri(Uri uri){
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(uri)));
            String line = "";

            while ((line = reader.readLine()) != null) {
                builder.append("\n"+line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return builder.toString();
    }

    public void verifyStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(
                        PERMISSIONS_STORAGE,
                        REQUEST_EXTERNAL_STORAGE
                );
            }
        }
    }
    private void verifyDataFolder() {
        File folder = new File(Environment.getExternalStorageDirectory() + "/Peer 2 Peer");
        File folder1 = new File(folder.getPath() + "/Conversations");
        File folder2 = new File(folder.getPath() + "/Saved txt files");
        if(!folder.exists() || !folder.isDirectory()) {
            folder.mkdir();
            folder1.mkdir();
            folder2.mkdir();
        }
        else if(!folder1.exists())
            folder1.mkdir();
        else if(!folder2.exists())
            folder2.mkdir();
    }
    private void writeToFile(String fileName, String data, boolean timeStamp) {

        Long time= System.currentTimeMillis();
        String timeMill = " "+time.toString();
        String path = Environment.getExternalStorageDirectory().toString();
        File file = null;
        if(timeStamp)
            file = new File(path+"/Peer 2 Peer/Conversations", fileName+timeMill+".txt");
        else
            file = new File(path+"/Peer 2 Peer/Saved txt files", fileName);
        FileOutputStream stream;
        try {
            stream = new FileOutputStream(file, false);
            stream.write(data.getBytes());
            stream.close();
            Toast.makeText(this, "Saving Your Conversation.....", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            Log.d(TAG, e.toString());
        } catch (IOException e) {
            Log.d(TAG, e.toString());
        }
    }

}
