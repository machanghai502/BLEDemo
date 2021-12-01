package com.chm.bledemo.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Toast;

import com.chm.bledemo.R;
import com.chm.bledemo.bleutils.callback.BleMsgReceiveCallBack;
import com.chm.bledemo.bleutils.callback.BleMsgSendCallBack;
import com.chm.bledemo.utils.BluetoothCommand;
import com.chm.bledemo.utils.HexUtil;
import com.chm.bledemo.utils.Tools;
import com.hjy.bluetooth.HBluetooth;
import com.hjy.bluetooth.exception.BluetoothException;
import com.hjy.bluetooth.inter.ReceiveCallBack;
import com.hjy.bluetooth.inter.SendCallBack;

import java.io.DataInputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 完成点检Activity
 */
public class CompleteCheckActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = "CompleteCheckActivity";

    private HBluetooth hBluetooth;

    //是否开始温度检测，true开始
    //boolean tempFlag = false;

    //是否其他数据的采集
    //boolean otherFlag = false;

    private BleMsgReceiveCallBack bleMsgReceiveCallBack = null;

    private BleReceiveData bleReceiveData = null;

    private SendCallBack sendCallBack;

    private List<Integer> audioDataList = new ArrayList<>();
    private List<Integer> vibrationXDataList = new ArrayList<>();
    private List<Integer> vibrationYDataList = new ArrayList<>();
    private List<Integer> vibrationZDataList = new ArrayList<>();


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
        findViewById(R.id.btn_get_audio_vibration_start).setOnClickListener(this);
        findViewById(R.id.btn_get_audio_vibration_end).setOnClickListener(this);

        bleMsgReceiveCallBack = new BleMsgReceiveCallBack();
        bleReceiveData = bleMsgReceiveCallBack.getBleReceiveData();

        sendCallBack = new BleMsgSendCallBack();

        //接受数据回调方法
        hBluetooth.setReceiver(bleMsgReceiveCallBack);
    }

    @Override
    public void onClick(View v) {
        //获取温度按钮处理
        if (v.getId() == R.id.btn_get_temp) {
            //发送命令
            hBluetooth.send(BluetoothCommand.START_TEMP, sendCallBack);

            bleMsgReceiveCallBack.setTempFlag(true);

            //置灰 todo
            Button btn = (Button) v;
            btn.setText("获取中");
            btn.setTextColor(0xFFD0EFC6);
            btn.setEnabled(false);
        } else if (v.getId() == R.id.btn_get_audio_vibration_start) {

            //点击采集音频和震动开始按钮
            // 如果此时还在处理音频则提示
            if (!bleMsgReceiveCallBack.isHasLastTempFlag()) {
                //todo toast

            }

            //开始获取音频和震动数据 //todo 发送失败了怎么办
            hBluetooth.send(BluetoothCommand.START_OTHER, sendCallBack);

            bleMsgReceiveCallBack.setAudioAndVibrationFlag(true);

            Button btn = (Button) v;
            btn.setText("获取中");
            btn.setTextColor(0xFFD0EFC6);
            btn.setEnabled(false);
            //todo 当返回确认数据后，或者分析完数据后，或者页面显示获取到的数据后，按钮可以重新点击以重新获取

            //todo 开始和结束按钮等处理完数据之后才开始恢复可点击状态

        } else if (v.getId() == R.id.btn_complete_check) {
            if (!bleMsgReceiveCallBack.isHasLastTempFlag()) {
                //todo 没处理完数据或者没监测数据就点击了完成的处理
            }

            //提交数据， loading，提交成功后取消loading。

            int temp = bleReceiveData.getTemp();
            int humidity = bleReceiveData.getHumidity();

            Toast.makeText(this, "当前温度:" + temp, Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "当前湿度:" + humidity, Toast.LENGTH_SHORT).show();

            //数据解析
            byte[] audioData = bleReceiveData.getAudioData();
            byte[] vibrationXData = bleReceiveData.getVibrationXData();
            byte[] vibrationYData = bleReceiveData.getVibrationYData();
            byte[] vibrationZData = bleReceiveData.getVibrationZData();

            Log.i(TAG, "音频数据:" + Tools.bytesToHexString(audioData));
            Log.i(TAG, "震动数据x:" + Tools.bytesToHexString(vibrationXData));
            Log.i(TAG, "震动数据y:" + Tools.bytesToHexString(vibrationYData));
            Log.i(TAG, "震动数据z:" + Tools.bytesToHexString(vibrationZData));


            extractingData(audioData, BluetoothCommand.DATA_TYPE_AUDIO);

            extractingData(audioData, BluetoothCommand.DATA_TYPE_VIBRATION_X);
            extractingData(audioData, BluetoothCommand.DATA_TYPE_VIBRATION_Y);
            extractingData(audioData, BluetoothCommand.DATA_TYPE_VIBRATION_Z);


            Log.i(TAG, "音频数据:" + audioDataList);
            Log.i(TAG, "震动数据x:" + vibrationXDataList);
            Log.i(TAG, "震动数据y:" + vibrationYDataList);
            Log.i(TAG, "震动数据z:" + vibrationZDataList);


        } else if (v.getId() == R.id.btn_get_audio_vibration_end) {

            if (!bleMsgReceiveCallBack.isHasLastTempFlag()) {
                //没有点击开始，就点击结束的时候，需要提示
                //todo Toast.
                return;
            }

            if (bleMsgReceiveCallBack.isAudioAndVibrationFlag()) {

            }

            //结束获取音频和震动数据
            hBluetooth.send(BluetoothCommand.END_OTHER, sendCallBack);

            //按钮置灰色
            Button btn = (Button) v;
            btn.setText("获取中");
            btn.setTextColor(0xFFD0EFC6);
            btn.setEnabled(false);

            //todo 这个地方不能重置为false，因为等发起停止命令时，才上传数据，应该等确认为最后一针完毕之后，才考虑设置为false
            //otherFlag = false;
        }
    }

    private void extractingData(byte[] audioData, byte dataType) {
        List<Integer> tempList = new ArrayList<>();

        int audioSegmentCount = audioData.length / 1000;
        int audioSegmentCountMod = audioData.length % 1000;
        for (int i = 0; i < audioSegmentCount  ; i++) {
            for (int j = i * 1000 + 5; j < (i + 1) * 1000 - 3; j=j+2) {
                byte firstByte = audioData[j];
                byte secondByte = audioData[j+1];

                int tempData = Tools.towBytesToIntHighAhead(firstByte, secondByte);
                tempList.add(tempData);
            }
        }

        //处理剩余的,说明不是整数
        if (audioSegmentCountMod != 0) {
            for (int n = audioSegmentCount * 1000 + 5; n < audioData.length - 3; n=n+2) {
                byte firstByte = audioData[n];
                byte secondByte = audioData[n+1];

                int tempData = Tools.towBytesToIntHighAhead(firstByte, secondByte);
                tempList.add(tempData);
            }
        }

        if (dataType == BluetoothCommand.DATA_TYPE_AUDIO) {
            audioDataList = tempList;
        } else  if (dataType == BluetoothCommand.DATA_TYPE_VIBRATION_X) {
            vibrationXDataList = tempList;
        } else  if (dataType == BluetoothCommand.DATA_TYPE_VIBRATION_Y) {
            vibrationYDataList = tempList;
        } else {
            vibrationZDataList = tempList;
        }
    }
}