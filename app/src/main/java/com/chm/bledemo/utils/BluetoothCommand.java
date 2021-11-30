package com.chm.bledemo.utils;

public class BluetoothCommand {
    //温度开始命令
    public static final byte[] START_TEMP = new byte[]{(byte) 0xff, 0x11};

    //震动数据开始命令
    public static final byte[] START_OTHER = new byte[]{(byte) 0xff, 0x12};

    //震动数据结束命令
    public static final byte[] END_OTHER = new byte[]{(byte) 0xff, 0x03};


    //默认类型：00
    public static final byte DATA_TYPE_DEFALUT  = 0x00;

    //数据类型：温度
    public static final byte DATA_TYPE_TEMP  = 0x01;

    //数据类型：音频
    public static final byte DATA_TYPE_AUDIO  = 0x02;

    //数据类型：x震动
    public static final byte DATA_TYPE_VIBRATION_X  = 0x03;

    //数据类型：y震动
    public static final byte DATA_TYPE_VIBRATION_Y  = 0x04;

    //数据类型：z震动
    public static final byte DATA_TYPE_VIBRATION_Z  = 0x05;

    //数据最后一帧
    public static final int LAST_DATA_FRAME = 0;


}
