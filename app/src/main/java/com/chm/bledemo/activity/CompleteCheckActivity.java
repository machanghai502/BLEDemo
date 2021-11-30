package com.chm.bledemo.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;

import com.chm.bledemo.R;
import com.chm.bledemo.utils.BluetoothCommand;
import com.chm.bledemo.utils.HexUtil;
import com.chm.bledemo.utils.Tools;
import com.hjy.bluetooth.HBluetooth;
import com.hjy.bluetooth.exception.BluetoothException;
import com.hjy.bluetooth.inter.ReceiveCallBack;
import com.hjy.bluetooth.inter.SendCallBack;

import java.io.DataInputStream;
import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * 完成点检Activity
 */
public class CompleteCheckActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = "CompleteCheckActivity";

    private HBluetooth hBluetooth;

    //是否开始温度检测，true开始
    boolean tempFlag = false;

    //是否其他数据的采集
    boolean otherFlag = false;

    //临时数据
    byte[] tempOtherData = new byte[0];


    //是否开始音频数据，true开始
    boolean audioFlag = false;

    //是否开始震动x，true开始
    boolean vibrationXFlag = false;

    //是否开始震动y，true开始
    boolean vibrationYFlag = false;

    //是否开始震动z，true开始
    boolean vibrationZFlag = false;



    //合法数据 todo 暂时是全部数据
    byte[] audioData = new byte[0];
    //byte[] audioData = new byte[0];
    //byte[] audioData = new byte[0];
    //byte[] audioData = new byte[0];




    //当前处理的帧类型
    byte currentDataType = BluetoothCommand.DATA_TYPE_DEFALUT;

    //当前处理的帧数据编号
    int currentDataNo = BluetoothCommand.LAST_DATA_FRAME;

    //当前处理的帧的数据长度
    int currentDataLength = 0;


    //是否处理完最后一针
    boolean isGetLastAudioFlag = false;
    boolean isGetLastVibrationXFlag = false;
    boolean isGetLastVibrationYFlag = false;
    boolean isGetLastVibrationZFlag = false;

   /**
     * 按照温度、音频、x震动、y震动、z震动的顺序来获取数据
     * 每帧需要返回一个确认，然后在此
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_check);

        hBluetooth = HBluetooth.getInstance();

        findViewById(R.id.btn_get_temp).setOnClickListener(this);
        findViewById(R.id.btn_complete_check).setOnClickListener(this);
        findViewById(R.id.btn_get_other).setOnClickListener(this);
        findViewById(R.id.btn_get_other_end).setOnClickListener(this);

        //接受数据回调方法
        hBluetooth.setReceiver(new ReceiveCallBack() {
            @Override
            public void onReceived(DataInputStream dataInputStream, byte[] result) {
                Log.e("mylog", "收到蓝牙设备返回数据->" + Tools.bytesToHexString(result));
                //温度固定12个字节
                if (tempFlag) {
                    //获取温度处理
                    if (result.length != 12) {
                        //数据错误，重发
                        Log.i(TAG, "数据错误，重发");
                    } else {
                        //解析
                        //校验CRC
                        int crcResult = 0x00;
                        for (int i = 0; i < 9; i++) {
                            crcResult = crcResult + (result[i] & 0xFF) ;
                        }

                        //获取低24位
                        crcResult = crcResult & 0xffffff;

                        int srcCRC = bytesToCRCInt(result, 9);
                        Log.i(TAG, "CRC校验结果：" + (srcCRC == crcResult));


                        if (srcCRC == crcResult) {
                            //获取数据
                            int temp = Tools.towBytesToIntHighAhead(result[5], result[6]);
                            int humidity =  Tools.towBytesToIntHighAhead(result[7], result[8]);

                            Log.i(TAG, "temp:" + temp);
                            Log.i(TAG, "humidity:" + humidity);

                            //返回ACK

                            //到这里说明已经获取到数据了，此时按钮可以重新点击，然后重新获取数据。
                            tempFlag = false;
                            //todo 需要修改按钮为可点击等

                            //todo 如果按钮已经点击了但是一直没有数据就点击测试震动，那么要不就是不能测试震动，要不提示用户。

                        } else {
                            //数据错误，重发
                            Log.i(TAG, "数据错误，重发");
                            //todo 重发
                        }
                    }
                    return;
                }


                if (otherFlag) {
                    tempOtherData = Tools.mergeBytes(tempOtherData, result);
                    //todo 暂时只考虑音频 以及大于100的字节
                    //todo 最后一帧时需要重置当前编号等

                    //暂不考虑最后一帧数据不足100，不考虑丢包的问题
                    if (tempOtherData.length == 100) {
                        //当前处理的帧类型
                        currentDataType = tempOtherData[0];
                        //当前处理的帧数据编号
                        currentDataNo = Tools.towBytesToIntHighAhead(tempOtherData[1], tempOtherData[2]);
                        //当前处理的帧数据长度
                        currentDataLength = Tools.towBytesToIntHighAhead(tempOtherData[3], tempOtherData[4]);
                    }


                    //如果最后一针不发送了，我们需要保证最后一针是发送了，。不仅要保证每一针数据是正确的。
                    //如果没有最后一针，那么标记为为处理完。同时重置相关的数据，从下一次开始。
                    //数据长度最大为992
                    //说明完全接受了该帧
                    if (tempOtherData.length == currentDataLength + 8) {
                        //当前帧接受完毕
                        //发送ack或者重发命令
                        int crcResult = 0x00;
                        for (int i = 0; i < tempOtherData.length - 3; i++) {
                            crcResult = crcResult + (result[i] & 0xFF) ;
                        }
                        //获取低24位
                        crcResult = crcResult & 0xffffff;

                        int srcCRC = bytesToCRCInt(tempOtherData, tempOtherData.length - 3);
                        Log.i(TAG, "CRC校验结果：" + (srcCRC == crcResult));


                        if (srcCRC == crcResult) {
                            //todo 暂时写成非合法数据
                            audioData = Tools.mergeBytes(audioData, tempOtherData);
                        } else {
                            //数据错误，重发
                            Log.i(TAG, "数据错误，重发");
                            //todo 重发
                        }


                        //如果是最后一针
                        if (currentDataNo == BluetoothCommand.LAST_DATA_FRAME){
                            resetAllFlag();
                            Log.i(TAG, "所有音频数据为：" + Tools.bytesToHexString(audioData));
                        }

                        //ACK发送 todo

                        //return;
                    }
                }

                /*WriteDataThread writeDataThread = new WriteDataThread(System.currentTimeMillis() + ":" + new String(result), getBaseContext(), path);
                threadPoolExecutor.submit(writeDataThread);*/
            }

            //todo
            private void resetAllFlag() {

            }
        });

    }

    @Override
    public void onClick(View v) {

        //获取温度按钮处理
        if (v.getId() == R.id.btn_get_temp) {
            //发送命令
            hBluetooth.send(BluetoothCommand.START_TEMP, new SendCallBack() {
                @Override
                public void onSending(byte[] command) {

                }

                @Override
                public void onSendFailure(BluetoothException bleException) {
                    Log.i(TAG, "发送温度开始失败");
                }
            });

            tempFlag = true;

            //修改按钮状态 todo
            Button btn = (Button) v;
            btn.setText("获取中");
            btn.setTextColor(0xFFD0EFC6);
            btn.setEnabled(false);

            //todo 当返回确认数据后，或者分析完数据后，或者页面显示获取到的数据后，按钮可以重新点击以重新获取


        } else if (v.getId() == R.id.btn_get_other) {
            //其他数据的测试
            //发送命令
            hBluetooth.send(BluetoothCommand.START_OTHER, new SendCallBack() {
                @Override
                public void onSending(byte[] command) {

                }

                @Override
                public void onSendFailure(BluetoothException bleException) {
                    Log.i(TAG, "发送其他数据检测开始命令失败");
                }
            });

            otherFlag = true;

            Button btn = (Button) v;
            btn.setText("获取中");
            btn.setTextColor(0xFFD0EFC6);
            btn.setEnabled(false);

            //todo 当返回确认数据后，或者分析完数据后，或者页面显示获取到的数据后，按钮可以重新点击以重新获取

        } else if (v.getId() == R.id.btn_complete_check) {



        } else if (v.getId() == R.id.btn_get_other_end) {
            //其他数据的测试
            //发送命令
            hBluetooth.send(BluetoothCommand.END_OTHER, new SendCallBack() {
                @Override
                public void onSending(byte[] command) {

                }

                @Override
                public void onSendFailure(BluetoothException bleException) {
                    Log.i(TAG, "发送其他数据检测结束命令失败");
                }
            });

            //todo 这个地方不能重置为false，因为等发起停止命令时，才上传数据，应该等确认为最后一针完毕之后，才考虑设置为false
            //otherFlag = false;

            //todo 当返回确认数据后，或者分析完数据后，或者页面显示获取到的数据后，按钮可以重新点击以重新获取

        } else {

        }
    }


    public static int bytesToCRCInt(byte[] src, int offset) {
        int value;
        value = (int) (((src[offset] & 0xFF)<<16) | ((src[offset+1] & 0xFF)<<8) | (src[offset+2] & 0xFF));
        return value;
    }
}