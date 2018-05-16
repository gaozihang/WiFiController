package com.example.gaonii.wificontroller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    Button button_link, button_cancel;
    EditText edit_ip, edit_port;

    String string_ip;
    int int_port;
    InetAddress ipAddress;

    static Socket socket = null;
    InputStream inputStream=null;
    OutputStream outputStream=null;

    boolean isConnect = true;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    Intent toController = new Intent();

    boolean isReceived = false;
    byte[] Received_Byte = new byte[1024];
    int Received_Byte_Len = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button_link = (Button) findViewById(R.id.button_link);
        button_cancel = (Button) findViewById(R.id.button_cancel);

        button_link.setOnClickListener(button_link_click);
        button_cancel.setOnClickListener(button_cancel_click);

        edit_ip = (EditText)findViewById(R.id.edit_ip);
        edit_port = (EditText)findViewById(R.id.edit_port);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean saved= sharedPreferences.getBoolean("saved", false);
        if (saved)
        {
            String string_ip = sharedPreferences.getString("string_ip", "192.168.1.100");
            String string_port = sharedPreferences.getString("string_port", "8080");
            edit_ip.setText(string_ip);
            edit_port.setText(string_port);
        }
    }

    private View.OnClickListener button_link_click = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            string_ip = edit_ip.getText().toString().replace(" ", "");
            int_port = Integer.valueOf(edit_port.getText().toString().replace(" ", ""));
            isConnect = true;
            tcpClientCountDownTimer.cancel();
            tcpClientCountDownTimer.start();
            ConnectSeverThread connectSeverThread = new ConnectSeverThread();
            connectSeverThread.start();

            editor = sharedPreferences.edit();
            editor.putString("string_ip", string_ip);
            editor.putString("string_port", edit_port.getText().toString());
            editor.putBoolean("saved", true);
            editor.commit();
        }
    };

    private View.OnClickListener button_cancel_click = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            isConnect = false;
            tcpClientCountDownTimer.cancel();
            //System.exit(0);
        }
    };

    private CountDownTimer tcpClientCountDownTimer = new CountDownTimer(3000,1500) {
        @Override
        public void onTick(long millisUntilFinished) {
            if(isConnect){
                Toast.makeText(MainActivity.this, "连接中...", Toast.LENGTH_SHORT).show();
            }
        }
        @Override
        public void onFinish() {
            if(isConnect)
            {
                isConnect = false;
                Toast.makeText(MainActivity.this, "连接失败!", Toast.LENGTH_SHORT).show();
                tcpClientCountDownTimer.cancel();
            }
        }
    };

    class ConnectSeverThread extends Thread
    {
        public void run()
        {
            while(isConnect)
            {
                try
                {
                    ipAddress = InetAddress.getByName(string_ip);
                    socket = new Socket(ipAddress, int_port);
                    isConnect = false;
                    tcpClientCountDownTimer.cancel();
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            Toast.makeText(MainActivity.this, "连接成功!", Toast.LENGTH_SHORT).show();
                        }
                    });
                    inputStream = socket.getInputStream();

                    isReceived = true;
                    ReceiveDataThread receiveDataThread = new ReceiveDataThread();
                    receiveDataThread.start();

                    toController.setClass(MainActivity.this, Controller.class);
                    MainActivity.this.startActivity(toController);
                }
                catch (IOException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    class  ReceiveDataThread extends Thread
    {
        public void run()
        {
            while(isReceived)
            {
                try
                {
                    Received_Byte_Len = inputStream.read(Received_Byte);
                    if (Received_Byte_Len == -1)
                    {
                        Log.e("MainActivity", "接收数据错误");
                        socket = null;
                        isReceived = false;
                    }
                } catch (IOException e) {
                    Log.e("MainActivity", "接收任务错误");
                    isReceived = false;
                    socket = null;
                }
            }
        }
    }
}
