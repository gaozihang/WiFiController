package com.example.gaonii.wificontroller;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.nio.ByteBuffer;

public class Controller extends  MainActivity{
    ImageButton forward, back, left, right;
    boolean bool_forward = false, bool_back = false, bool_left = false, bool_right = false, bool_stop = true;
    Button stop;

    byte[] send_byte = new byte[4];
    boolean isSended = true;
    SendMsgThread sendMsgThread;
    Intent toMain = new Intent();

    SeekBar seekBar;
    TextView text_speed;
    int speed = 0;

    CheckBox check_box;
    SensorManager sensorManager = null;
    Sensor sensor;
    float x_offset, y_offset, z_offset;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.controller);

        forward = (ImageButton) findViewById(R.id.forward);
        back = (ImageButton) findViewById(R.id.back);
        left = (ImageButton) findViewById(R.id.left);
        right = (ImageButton) findViewById(R.id.right);
        stop = (Button) findViewById(R.id.stop);

        forward.setOnTouchListener(forward_touch_listener);
        back.setOnTouchListener(back_touch_listener);
        left.setOnTouchListener(left_touch_listener);
        right.setOnTouchListener(right_touch_listener);
        stop.setOnClickListener(button_stop_click);

        seekBar = (SeekBar) findViewById(R.id.progress);
        text_speed = (TextView) findViewById(R.id.speed);
        seekBar.setOnSeekBarChangeListener(seekBar_change_listener);

        check_box = (CheckBox) findViewById(R.id.checkBox);
        check_box.setOnCheckedChangeListener(checkBox_change_listener);

        sendMsgThread = new SendMsgThread();
        sendMsgThread.start();
    }

    private View.OnTouchListener forward_touch_listener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // TODO Auto-generated method stub
            if (event.getAction()==MotionEvent.ACTION_DOWN) {
                bool_stop = false;
                bool_forward = true;
                bool_back = false;
                forward.setImageResource(R.drawable.forward2);
            }
            if (event.getAction()==MotionEvent.ACTION_UP) {
                bool_forward = false;
                forward.setImageResource(R.drawable.forward);
            }
            return false;
        }
    };

    private View.OnTouchListener back_touch_listener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // TODO Auto-generated method stub
            if (event.getAction()==MotionEvent.ACTION_DOWN) {
                bool_stop = false;
                bool_back = true;
                bool_forward = false;
                back.setImageResource(R.drawable.back2);
            }
            if (event.getAction()==MotionEvent.ACTION_UP) {
                bool_back = false;
                back.setImageResource(R.drawable.back);
            }
            return false;
        }
    };

    private View.OnTouchListener left_touch_listener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // TODO Auto-generated method stub
            if (event.getAction()==MotionEvent.ACTION_DOWN) {
                bool_stop = false;
                bool_left = true;
                bool_right = false;
                left.setImageResource(R.drawable.left2);
            }
            if (event.getAction()==MotionEvent.ACTION_UP) {
                bool_left=false;
                left.setImageResource(R.drawable.left);
            }
            return false;
        }
    };

    private View.OnTouchListener right_touch_listener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // TODO Auto-generated method stub
            if (event.getAction()==MotionEvent.ACTION_DOWN) {
                bool_stop = false;
                bool_right = true;
                bool_left = false;
                right.setImageResource(R.drawable.right2);
            }
            if (event.getAction()==MotionEvent.ACTION_UP) {
                bool_right = false;
                right.setImageResource(R.drawable.right);
            }
            return false;
        }
    };

    private View.OnClickListener button_stop_click = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            bool_stop = true;
            Toast.makeText(Controller.this, "紧急停止中...", Toast.LENGTH_SHORT).show();
        }
    };

    private SeekBar.OnSeekBarChangeListener seekBar_change_listener = new SeekBar.OnSeekBarChangeListener(){
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            speed = progress;
            text_speed.setText("当前速度:" + Integer.toString(progress) + "%");
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };

    private CompoundButton.OnCheckedChangeListener checkBox_change_listener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            // TODO Auto-generated method stub
            if (isChecked)
            {
                sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
                sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                sensorManager.registerListener(mySensorEventListener, sensor,  SensorManager.SENSOR_DELAY_NORMAL);
            }
            else
            {
                sensorManager.unregisterListener(mySensorEventListener);
            }
        }
    };

    SensorEventListener mySensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            // TODO Auto-generated method stub
            if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            {
                x_offset = event.values[0];
                y_offset = event.values[1];
                z_offset = event.values[2];
                if(x_offset < - 5.0)
                {
                    //forward
                    bool_stop = false;
                    bool_forward = true;
                    bool_back = false;
                }
                else if(x_offset < 0 )
                {
                    bool_forward = false;
                }

                if(x_offset > 5.0)
                {
                    //back
                    bool_stop = false;
                    bool_back = true;
                    bool_forward = false;
                }
                else if(x_offset > 0 )
                {
                    bool_back = false;
                }

                if(y_offset < -5.0)
                {
                    //left
                    bool_stop = false;
                    bool_left = true;
                    bool_right = false;
                }
                else if(y_offset < 0)
                {
                    bool_left=false;
                }

                if(y_offset > 5.0)
                {
                    //right
                    bool_stop = false;
                    bool_right = true;
                    bool_left = false;
                }
                else if(y_offset > 0 )
                {
                    bool_right = false;
                }
                //Toast.makeText(Controller.this, "x: " + x_offset + " y: " + y_offset + " z: " + z_offset, Toast.LENGTH_SHORT).show();
            }
            else
            {
                sensorManager.unregisterListener(mySensorEventListener);
                runOnUiThread(new Runnable() {
                    public void run()
                    {
                        check_box.setChecked(false);
                        Toast.makeText(Controller.this, "不存在重力传感器!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub

        }
    };

    class SendMsgThread extends Thread
    {
        public void run()
        {
            while(isSended)
            {
                send_byte[0] = (byte)0xaa;
                send_byte[1] = (byte)0x05;

                if (bool_forward) {
                    send_byte[2] = (byte)0x01;

                }
                if (bool_back) {
                    send_byte[2] = (byte)0x02;
                }
                if (bool_left) {
                    send_byte[2] = (byte)0x03;

                }
                if (bool_right) {
                    send_byte[2] = (byte)0x04;
                }
                if(bool_stop){
                    send_byte[2] = (byte)0x05;
                }

                if(bool_stop)
                {
                    bool_stop = false;
                    send_byte[3] = (byte)0x00;
                    SendData(send_byte);
                }
                else if (bool_forward || bool_back || bool_left || bool_right)
                {
                    send_byte[3] = (byte)speed;
                    SendData(send_byte);
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    private void SendData(byte[] byt)
    {
        int crc = 0;
        ByteBuffer Crcbyte = ByteBuffer.allocate(4);
        byte[] sendbyte = new byte[byt.length + 2];

        for (int i = 0; i < byt.length; i++)
        {
            sendbyte[i] = byt[i];
        }
        crc = crc16_modbus(byt, byt.length);
        Crcbyte.putInt(crc);
        sendbyte[sendbyte.length - 2] = Crcbyte.get(3);
        sendbyte[sendbyte.length - 1] = Crcbyte.get(2);

        try
        {
            outputStream = socket.getOutputStream();
            outputStream.write(sendbyte);
        }
        catch (IOException e)
        {
            isSended = false;
            socket = null;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    Toast.makeText(Controller.this, "连接已断开,请重新连接...", Toast.LENGTH_SHORT).show();
                }
            });
            toMain.setClass(Controller.this, MainActivity.class);
            Controller.this.startActivity(toMain);

        }
    }

    protected int crc16_modbus(byte[] modbusdata, int length)
    {
        int i=0, j=0;
        int crc = 0;
        try
        {
            for (i = 0; i < length; i++)
            {
                crc ^= (modbusdata[i]&(0xff));
                for (j = 0; j < 8; j++)
                {
                    if ((crc & 0x01) == 1)
                    {
                        crc = (crc >> 1) ;
                        crc = crc ^ 0xa001;
                    }
                    else
                    {
                        crc >>= 1;
                    }
                }
            }
        }
        catch (Exception e)
        {

        }
        return crc;
    }

    protected int crc16_flage(byte[] modbusdata, int length)
    {
        int Receive_CRC = 0, calculation = 0;

        Receive_CRC = crc16_modbus(modbusdata, length);
        calculation = modbusdata[length + 1];
        calculation <<= 8;
        calculation += modbusdata[length];
        if (calculation != Receive_CRC)
        {
            return 0;
        }
        return 1;
    }

    protected void onPause()
        {
            isSended = false;
            if (sensorManager!=null)
            {
                sensorManager.unregisterListener(mySensorEventListener);
            }
            super.onPause();
        }
}
