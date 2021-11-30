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
}
