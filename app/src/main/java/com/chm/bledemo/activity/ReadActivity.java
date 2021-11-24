package com.chm.bledemo.activity;

import android.nfc.Tag;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.chm.bledemo.R;
import com.chm.bledemo.thread.ReadDataThread;

public class ReadActivity extends AppCompatActivity {

    private final String TAG = "ReadActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);

        Button readBtn1 = findViewById(R.id.read_btn1);

        final EditText editText = findViewById(R.id.editTextTextMultiLine);

        readBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG,"点击了读取数据按钮");
                ReadDataThread readDataThread = new ReadDataThread(getApplicationContext());
                readDataThread.setEditText(editText);
                readDataThread.start();
            }
        });
    }
}