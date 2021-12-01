package com.chm.bledemo.activity;


/**
 * 保存每次点检后的数据
 */
public class BleReceiveData {


    /**
     * 温度
     */
    private int temp = 0;

    /**
     * 湿度
     */
    private int humidity = 0;

    //包含帧头帧尾的原始数据
    byte[] audioData = new byte[0];
    byte[] vibrationXData = new byte[0];
    byte[] vibrationYData = new byte[0];
    byte[] vibrationZData = new byte[0];

    public int getTemp() {
        return temp;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
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
}
