package com.chm.bledemo.bleutils.callback;

import android.util.Log;

import com.hjy.bluetooth.exception.BluetoothException;
import com.hjy.bluetooth.inter.SendCallBack;

public class BleMsgSendCallBack implements SendCallBack {

    private static  final String TAG = "BleMsgSendCallBack";

    @Override
    public void onSending(byte[] command) {

    }

    @Override
    public void onSendFailure(BluetoothException bleException) {
        Log.i(TAG, "发送温度开始失败");
    }
}
