package com.chm.bledemo.thread;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.chm.bledemo.bleutils.BleController;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class ReadDataThread  extends Thread{

    private final String TAG = "ReadDataThread";

    private String path = "aaaa";

    private Context context;

    private EditText editText;

    public EditText getEditText() {
        return editText;
    }

    public void setEditText(EditText editText) {
        this.editText = editText;
    }

    private StringBuffer result = new StringBuffer("result:");

    public ReadDataThread(Context context) {
        this.context = context;
    }

    @Override
    public void run() {

        BleController instance = BleController.getInstance();

        FileInputStream fileInputStream = null;
        try {
            fileInputStream = context.openFileInput(path);

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));


            int i = -1;
            String temp = "";
            while ((temp = bufferedReader.readLine()) != null ) {
                result.append(temp);
                Log.i(TAG, "读取到的数据:" + temp);
            }

            Log.i(TAG, "读取到的所有数据" + result.toString());

            //更新UI。
            instance.runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    editText.setText(result.toString());
                }
            });

        } catch (Exception e) {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException ignored) {

                }
            }
        }

    }
}
