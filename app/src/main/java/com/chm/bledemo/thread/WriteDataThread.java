package com.chm.bledemo.thread;


import android.content.Context;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 将蓝牙上传的数据写到内存OR持久化存储
 */
public class WriteDataThread  extends  Thread{

    private String path = "aaaa";

    private String content = "";

    private Context context;

    public WriteDataThread(String content, Context context) {
        this.content = content;
        this.context = context;
    }

    @Override
    public void run() {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = context.openFileOutput(path, Context.MODE_APPEND);

            fileOutputStream.write(this.content.getBytes());
        } catch (Exception e) {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException ignored) {

                }
            }
        }
    }
}
