package com.chm.bledemo.bleutils.callback;

import android.util.Log;

import com.chm.bledemo.activity.BleReceiveData;
import com.chm.bledemo.utils.BluetoothCommand;
import com.chm.bledemo.utils.Tools;
import com.hjy.bluetooth.HBluetooth;
import com.hjy.bluetooth.inter.ReceiveCallBack;
import com.hjy.bluetooth.inter.SendCallBack;

import java.io.DataInputStream;

public class BleMsgReceiveCallBack implements ReceiveCallBack {

    private static final String TAG = "BleMsgReceiveCallBack";

    private HBluetooth hBluetooth = HBluetooth.getInstance();
    private SendCallBack sendCallBack = new BleMsgSendCallBack();
    //所有接收到的数据，一帧数据
    private BleReceiveData bleReceiveData = new BleReceiveData();

    //是否开始温度检测，true开始
    boolean tempFlag = false;

    //音频和震动数据的采集
    boolean audioAndVibrationFlag = false;

    //是否开始音频数据，true开始
    boolean audioFlag = false;

    //是否开始震动x，true开始
    boolean vibrationXFlag = false;

    //是否开始震动y，true开始
    boolean vibrationYFlag = false;

    //是否开始震动z，true开始
    boolean vibrationZFlag = false;

    //合法数据
    byte[] audioData = new byte[0];
    byte[] vibrationXData = new byte[0];
    byte[] vibrationYData = new byte[0];
    byte[] vibrationZData = new byte[0];

    //是否处理完最后一帧
    boolean hasLastTempFlag = false;
    boolean hasLastAudioFlag = false;
    boolean hasLastVibrationXFlag = false;
    boolean hasLastVibrationYFlag = false;
    boolean hasLastVibrationZFlag = false;

    //当前处理的帧类型
    byte currentDataType = BluetoothCommand.DATA_TYPE_DEFALUT;

    //当前处理的帧数据编号
    int currentDataNo = BluetoothCommand.LAST_DATA_FRAME;

    //当前处理的帧的数据长度，不包含帧头和校验
    int currentDataLength = 0;


    //临时数据
    byte[] tempByteArr = new byte[0];

    @Override
    public void onReceived(DataInputStream dataInputStream, byte[] result) {
        Log.e("mylog", "收到蓝牙设备返回数据->" + Tools.bytesToHexString(result));
        //温度固定12个字节
        if (tempFlag) {
            currentDataType = BluetoothCommand.DATA_TYPE_TEMP;

            //获取温度处理
            if (result.length != 12) {
                //数据错误，重发
                Log.i(TAG, "数据错误，重发");
                sendFailAck();
                return;
            }

            //校验CRC
            int crcResult = 0x00;
            for (int i = 0; i < 9; i++) {
                crcResult = crcResult + (result[i] & 0xFF) ;
            }

            //获取低24位
            crcResult = crcResult & 0xffffff;
            int srcCRC = Tools.bytesToCRCInt(result, 9);
            Log.i(TAG, "CRC校验结果：" + (srcCRC == crcResult));


            if (srcCRC != crcResult) {
                //数据错误，重发
                Log.i(TAG, "数据错误，重发");
                sendFailAck();
            }

            //获取数据
            bleReceiveData.setTemp(Tools.towBytesToIntHighAhead(result[5], result[6]));
            bleReceiveData.setHumidity(Tools.towBytesToIntHighAhead(result[7], result[8]));

            Log.i(TAG, "temp:" + bleReceiveData.getTemp());
            Log.i(TAG, "humidity:" + bleReceiveData.getHumidity());

            //到这里说明已经获取到数据了，此时按钮可以重新点击，然后重新获取数据。
            tempFlag = false;
            hasLastTempFlag = true;

            //返回ACK
            sendSuccessAck();

            //todo 需要修改按钮为可点击等

            return;
        }


        //todo 应该是点击结束的时候修改这个标记
        if (audioAndVibrationFlag) {
            tempByteArr = Tools.mergeBytes(tempByteArr, result);
            //todo 暂时处理大于100的字节
            //暂不考虑最后一帧数据不足100，不考虑丢包的问题
            if (tempByteArr.length == 100) {
                //当前处理的帧类型
                currentDataType = tempByteArr[0];
                //当前处理的帧数据编号
                currentDataNo = Tools.towBytesToIntHighAhead(tempByteArr[1], tempByteArr[2]);
                //当前处理的帧数据长度
                currentDataLength = Tools.towBytesToIntHighAhead(tempByteArr[3], tempByteArr[4]);
            }


            //如果最后一针不发送了，我们需要保证最后一针是发送了，。不仅要保证每一针数据是正确的。
            //如果没有最后一针，那么标记为为处理完。同时重置相关的数据，从下一次开始。
            //数据长度最大为992
            //说明完全接受了该帧
            if (tempByteArr.length == currentDataLength + 8) {
                //当前帧接受完毕
                //发送ack或者重发命令
                int crcResult = 0x00;
                for (int i = 0; i < tempByteArr.length - 3; i++) {
                    crcResult = crcResult + (result[i] & 0xFF) ;
                }
                //获取低24位
                crcResult = crcResult & 0xffffff;

                int srcCRC = Tools.bytesToCRCInt(tempByteArr, tempByteArr.length - 3);
                Log.i(TAG, "CRC校验结果：" + (srcCRC == crcResult));


                if (srcCRC != crcResult) {
                    //数据错误，重发
                    Log.i(TAG, "crc校验失败，重发");
                    sendFailAck();
                }

                if (currentDataType == BluetoothCommand.DATA_TYPE_AUDIO) {
                    audioData = Tools.mergeBytes(audioData, tempByteArr);
                } else if (currentDataType == BluetoothCommand.DATA_TYPE_VIBRATION_X) {
                    vibrationXData = Tools.mergeBytes(vibrationXData, tempByteArr);
                } else if (currentDataType == BluetoothCommand.DATA_TYPE_VIBRATION_Y) {
                    vibrationYData = Tools.mergeBytes(vibrationYData, tempByteArr);
                } else {
                    vibrationZData = Tools.mergeBytes(vibrationZData, tempByteArr);
                }

                //如果是最后一帧
                if (currentDataNo == BluetoothCommand.LAST_DATA_FRAME){
                    if (currentDataType == BluetoothCommand.DATA_TYPE_AUDIO) {
                        hasLastAudioFlag  = true;
                    } else if (currentDataType == BluetoothCommand.DATA_TYPE_VIBRATION_X) {
                        hasLastVibrationXFlag = true;
                    } else if (currentDataType == BluetoothCommand.DATA_TYPE_VIBRATION_Y) {
                        hasLastVibrationYFlag = true;
                    } else {
                       hasLastVibrationZFlag = true;
                    }

                    resetFlag();
                }

                //成功ack
                sendSuccessAck();
            }
        }
    }


    private void resetFlag() {
        tempByteArr = new byte[0];
    }


    private void sendSuccessAck() {
        byte[] ackByte = new byte[9];
        ackByte[0] = currentDataType;
        ackByte[1] = Tools.intToBytes(currentDataNo)[2];
        ackByte[2] = Tools.intToBytes(currentDataNo)[3];
        ackByte[3] = 0x00;
        ackByte[4] = 0x01;
        ackByte[5] = 0x01; //成功


        int ackCrcResult = 0x00;
        for (int i = 0; i < 6; i++) {
            ackCrcResult = ackCrcResult + (ackByte[i] & 0xFF) ;
        }
        ackCrcResult = ackCrcResult & 0xffffff;

        ackByte[6] = Tools.intToBytes(ackCrcResult)[1];
        ackByte[7] = Tools.intToBytes(ackCrcResult)[2];
        ackByte[8] = Tools.intToBytes(ackCrcResult)[3];

        hBluetooth.send(ackByte, sendCallBack);
    }


    private void sendFailAck() {
        byte[] ackByte = new byte[9];
        ackByte[0] = currentDataType;
        ackByte[1] = Tools.intToBytes(currentDataNo)[2];
        ackByte[2] = Tools.intToBytes(currentDataNo)[3];
        ackByte[3] = 0x00;
        ackByte[4] = 0x01;
        ackByte[5] = 0x00; //失败


        int ackCrcResult = 0x00;
        for (int i = 0; i < 6; i++) {
            ackCrcResult = ackCrcResult + (ackByte[i] & 0xFF) ;
        }
        ackCrcResult = ackCrcResult & 0xffffff;

        ackByte[6] = Tools.intToBytes(ackCrcResult)[1];
        ackByte[7] = Tools.intToBytes(ackCrcResult)[2];
        ackByte[8] = Tools.intToBytes(ackCrcResult)[3];

        hBluetooth.send(ackByte, sendCallBack);
    }


    public BleReceiveData getBleReceiveData() {
        return bleReceiveData;
    }

    public void setBleReceiveData(BleReceiveData bleReceiveData) {
        this.bleReceiveData = bleReceiveData;
    }

    public boolean isTempFlag() {
        return tempFlag;
    }

    public void setTempFlag(boolean tempFlag) {
        this.tempFlag = tempFlag;
    }

    public boolean isAudioAndVibrationFlag() {
        return audioAndVibrationFlag;
    }

    public void setAudioAndVibrationFlag(boolean audioAndVibrationFlag) {
        this.audioAndVibrationFlag = audioAndVibrationFlag;
    }

    public boolean isAudioFlag() {
        return audioFlag;
    }

    public void setAudioFlag(boolean audioFlag) {
        this.audioFlag = audioFlag;
    }

    public boolean isVibrationXFlag() {
        return vibrationXFlag;
    }

    public void setVibrationXFlag(boolean vibrationXFlag) {
        this.vibrationXFlag = vibrationXFlag;
    }

    public boolean isVibrationYFlag() {
        return vibrationYFlag;
    }

    public void setVibrationYFlag(boolean vibrationYFlag) {
        this.vibrationYFlag = vibrationYFlag;
    }

    public boolean isVibrationZFlag() {
        return vibrationZFlag;
    }

    public void setVibrationZFlag(boolean vibrationZFlag) {
        this.vibrationZFlag = vibrationZFlag;
    }

    public byte[] getAudioData() {
        return audioData;
    }

    public void setAudioData(byte[] audioData) {
        this.audioData = audioData;
    }

    public byte[] getVibrationXData() {
        return vibrationXData;
    }

    public void setVibrationXData(byte[] vibrationXData) {
        this.vibrationXData = vibrationXData;
    }

    public byte[] getVibrationYData() {
        return vibrationYData;
    }

    public void setVibrationYData(byte[] vibrationYData) {
        this.vibrationYData = vibrationYData;
    }

    public byte[] getVibrationZData() {
        return vibrationZData;
    }

    public void setVibrationZData(byte[] vibrationZData) {
        this.vibrationZData = vibrationZData;
    }

    public boolean isHasLastAudioFlag() {
        return hasLastAudioFlag;
    }

    public void setHasLastAudioFlag(boolean hasLastAudioFlag) {
        this.hasLastAudioFlag = hasLastAudioFlag;
    }

    public boolean isHasLastVibrationXFlag() {
        return hasLastVibrationXFlag;
    }

    public void setHasLastVibrationXFlag(boolean hasLastVibrationXFlag) {
        this.hasLastVibrationXFlag = hasLastVibrationXFlag;
    }

    public boolean isHasLastVibrationYFlag() {
        return hasLastVibrationYFlag;
    }

    public void setHasLastVibrationYFlag(boolean hasLastVibrationYFlag) {
        this.hasLastVibrationYFlag = hasLastVibrationYFlag;
    }

    public boolean isHasLastVibrationZFlag() {
        return hasLastVibrationZFlag;
    }

    public void setHasLastVibrationZFlag(boolean hasLastVibrationZFlag) {
        this.hasLastVibrationZFlag = hasLastVibrationZFlag;
    }

    public byte getCurrentDataType() {
        return currentDataType;
    }

    public void setCurrentDataType(byte currentDataType) {
        this.currentDataType = currentDataType;
    }

    public int getCurrentDataNo() {
        return currentDataNo;
    }

    public void setCurrentDataNo(int currentDataNo) {
        this.currentDataNo = currentDataNo;
    }

    public int getCurrentDataLength() {
        return currentDataLength;
    }

    public void setCurrentDataLength(int currentDataLength) {
        this.currentDataLength = currentDataLength;
    }

    public byte[] getTempByteArr() {
        return tempByteArr;
    }

    public void setTempByteArr(byte[] tempByteArr) {
        this.tempByteArr = tempByteArr;
    }

    public boolean isHasLastTempFlag() {
        return hasLastTempFlag;
    }

    public void setHasLastTempFlag(boolean hasLastTempFlag) {
        this.hasLastTempFlag = hasLastTempFlag;
    }
}

