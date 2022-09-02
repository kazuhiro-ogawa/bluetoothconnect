package com.example.echobt;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBtDevice;
    private BluetoothSocket  mBtSocket;
    private InputStream mInput;
    private OutputStream mOutput;
    // ESP32のMACアドレス
    String macDevice = "24:62:AB:E2:56:AA";
    TextView txtRcv;
    Button sendBtn;
    EditText sendTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // BluetoothAdapterのインスタンスを得る
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // MACアドレスからBluetoothDeviceのインスタンスを得る
        mBtDevice = mBluetoothAdapter.getRemoteDevice(macDevice);

        txtRcv = findViewById(R.id.txtRcv);
        sendBtn = findViewById(R.id.sendBtn);
        sendTxt = findViewById(R.id.sendTxt);

        try {
            // 接続に使用するプロファイルを指定してBluetoothSocketのインスタンスを得る
            // この例ではSPP(シリアルポートプロファイル)のUUIDを使用している
            mBtSocket = mBtDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));

            // ソケットを接続する
            mBtSocket.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // 出力のためのストリームオブジェクトを得る
                            mOutput = mBtSocket.getOutputStream();
                            // 送信処理
                            mOutput.write(sendTxt.getText().toString().getBytes());
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });


        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] bytes = new byte[1024];

                    // 入力のためのストリームオブジェクトを得る
                    mInput = mBtSocket.getInputStream();

                    while (true) {
                        // 受信処理
                        Arrays.fill(bytes, (byte)0);
                        mInput.read(bytes);

                        handler.post(new Runnable() {
                            // 受信した文字列を表示
                            @Override
                            public void run() {
                                txtRcv.setText(new String(bytes));
                            }
                        });

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        try{
            // 使わなくなった時点でソケットを閉じる
            mBtSocket.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}