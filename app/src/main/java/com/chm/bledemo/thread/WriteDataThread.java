package com.chm.bledemo.thread;


import android.content.Context;
import android.nfc.Tag;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 将蓝牙上传的数据写到内存OR持久化存储
 */
public class WriteDataThread  extends  Thread{

    private String content = "";

    private Context context;

    private String path;

    public WriteDataThread(String content, Context context, String path) {
        this.content = content;
        this.context = context;
        this.path = path;
    }

    // TODO: 2021/11/26 后边等点击停止按钮后统一写到文件中去
    @Override
    public void run() {
        File file = new File(path);
        if (!file.exists()) {
            try {
                boolean newFile = file.createNewFile();
                Log.i("create", "创建文件成功");
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }

        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file, true);
            //fileOutputStream = context.openFileOutput(path, Context.MODE_APPEND);
            fileOutputStream.write(this.content.getBytes());
        } catch (Exception e) {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (Exception ignored) {

                }
            }
            Log.e("exception", e.toString());
        }
    }
}
