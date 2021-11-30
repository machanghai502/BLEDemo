package com.chm.bledemo.activity;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.chm.bledemo.R;
import com.chm.bledemo.adspter.GridAdapter;
import com.chm.bledemo.utils.HexUtil;
import com.chm.bledemo.utils.Tools;
import com.hjy.bluetooth.HBluetooth;
import com.hjy.bluetooth.entity.BluetoothDevice;
import com.hjy.bluetooth.exception.BluetoothException;
import com.hjy.bluetooth.inter.BleMtuChangedCallback;
import com.hjy.bluetooth.inter.BleNotifyCallBack;
import com.hjy.bluetooth.inter.ConnectCallBack;
import com.hjy.bluetooth.inter.ReceiveCallBack;
import com.hjy.bluetooth.inter.ScanCallBack;
import com.hjy.bluetooth.inter.SendCallBack;
import com.hjy.bluetooth.operator.abstra.Sender;

import java.io.DataInputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeviceAccessActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private static final String TAG = "DeviceAccessActivity";

    private HBluetooth mHBluetooth;

    private Button findDeviceBtn;

    private ImageView radarImageView;

    private LinearLayout middleLine;

    private GridView gridView;

    private Context context;

    //扫到的所有设备
    private List<com.hjy.bluetooth.entity.BluetoothDevice> bluetoothDeviceList = new ArrayList<>();

    private  GridAdapter gridAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_access2);

        context = getBaseContext();

        //发现设备按钮
        findDeviceBtn = findViewById(R.id.btn_find_device);
        findDeviceBtn.setOnClickListener(this);


        //中间雷达图
        radarImageView = findViewById(R.id.img_radar);

        //雷达图所在的layout
        middleLine = findViewById(R.id.device_access_line_middle);

        //gridView
        gridView = findViewById(R.id.grid_device_access);
        gridView.setOnItemClickListener(this);


        //蓝牙相关处理
        processBlueTooth();

        String s = "ff 01";
        byte[] bytes = HexUtil.hexStringToBytes(s);
        Log.i(TAG, bytes.length + "=====");
    }


    private void processBlueTooth() {
        mHBluetooth = HBluetooth.getInstance();
        // TODO: 2021/11/29 UUID动态获取
        //请填写你自己设备的UUID
        //低功耗蓝牙才需要如下配置BleConfig,经典蓝牙不需要new HBluetooth.BleConfig()
        HBluetooth.BleConfig bleConfig = new HBluetooth.BleConfig();
        bleConfig.withServiceUUID("0003cdd0-0000-1000-8000-00805f9b0131")
                .withWriteCharacteristicUUID("0003cdd2-0000-1000-8000-00805f9b0131")
                .withNotifyCharacteristicUUID("0003cdd1-0000-1000-8000-00805f9b0131")
                //命令长度大于20个字节时是否分包发送，默认false,分包时可以调两参方法设置包之间发送间隔
                //默认false,注释部分为默认值
                //.splitPacketToSendWhenCmdLenBeyond(false)
                //.useCharacteristicDescriptor(false)
                //连接后开启通知的延迟时间，单位ms，默认200ms
                //.notifyDelay(200)
                .setMtu(100, new BleMtuChangedCallback() {
                    @Override
                    public void onSetMTUFailure(int realMtuSize, BluetoothException bleException) {
                        Log.i(TAG, "bleException:" + bleException.getMessage() + "  realMtuSize:" + realMtuSize);
                    }

                    @Override
                    public void onMtuChanged(int mtuSize) {
                        Log.i(TAG, "Mtu set success,mtuSize:" + mtuSize);
                    }
                });

        mHBluetooth
                //开启蓝牙功能
                .enableBluetooth()
                //低功耗蓝牙才需要调setBleConfig
                .setBleConfig(bleConfig);


    }

    @Override
    public void onClick(View v) {

        //发现设备点击事件处理
        if (v.getId() == R.id.btn_find_device) {
            //扫描所有的蓝牙设备并显示
            scanWithTimeUse();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //优化成直接连接蓝牙即可
        Log.d(TAG, "onItemClick: 点解了Item");

        //当前要连接的蓝牙设备
        BluetoothDevice device = bluetoothDeviceList.get(position);

        //调用连接器连接蓝牙设备
        mHBluetooth.connect(device, new ConnectCallBack() {

            @Override
            public void onConnecting() {
                Log.i(TAG, "连接中...");
            }

            @Override
            public void onConnected(Sender sender) {
                Log.i(TAG, "连接成功,isConnected:" + mHBluetooth.isConnected());

                //返回到工作台或者上一页
                //startActivity(MainActivity.this, TestActivity.class);

                //暂时跳转到点检页面
                Intent intent = new Intent(DeviceAccessActivity.this, CompleteCheckActivity.class);
                startActivity(intent);
            }

            @Override
            public void onDisConnecting() {
                Log.i(TAG, "断开连接中...");
            }

            @Override
            public void onDisConnected() {
                Log.i(TAG, "已断开连接,isConnected:" + mHBluetooth.isConnected());
            }

            @Override
            public void onError(int errorType, String errorMsg) {
                Log.i(TAG, "错误类型：" + errorType + " 错误原因：" + errorMsg);
            }
        }, new BleNotifyCallBack() {
            @Override
            public void onNotifySuccess() {
                Log.i(TAG, "打开通知成功");
            }

            @Override
            public void onNotifyFailure(BluetoothException bleException) {
                Log.i(TAG, "打开通知失败：" + bleException.getMessage());
            }
        });
    }


    //扫描蓝牙设备
    @SuppressLint("WrongConstant")
    private void scanWithTimeUse() {
        int type = com.hjy.bluetooth.entity.BluetoothDevice.DEVICE_TYPE_LE;
        //扫描蓝牙设备，扫描6秒就自动停止扫描
        mHBluetooth.scan(type, 6000, new ScanCallBack() {
            @Override
            public void onScanStart() {
                Log.i(TAG, "正在搜索附近设备...");
                //更改btn文字
                findDeviceBtn.setText("正在搜索附近设备...");
            }

            @Override
            public void onScanning(List<BluetoothDevice> scannedDevices, BluetoothDevice currentScannedDevice) {
                Log.i(TAG, "正在搜索附近设备...");
                //更改btn文字
                findDeviceBtn.setText("正在搜索附近设备...");
                //todo 实时刷新grid？
                /*if (scannedDevices != null && scannedDevices.size() > 0) {
                    list.clear();
                    list.addAll(scannedDevices);
                    adapter.notifyDataSetChanged();
                }*/
            }

            @Override
            public void onError(int errorType, String errorMsg) {
                Log.e(TAG, "errorType:"+errorType+"  errorMsg:"+errorMsg);
            }

            @Override
            public void onScanFinished(List<BluetoothDevice> bluetoothDevices) {
                Log.i(TAG, "BLE扫描结束");

                //隐藏ImageView
                middleLine.removeView(radarImageView);

                //todo 页面要体现扫描结束，不管通过按钮还是toast
                //Toast.makeText(DeviceAccessActivity.this, "BLE扫描结束", Toast.LENGTH_LONG).show();

                if (bluetoothDevices != null && bluetoothDevices.size() > 0) {
                    bluetoothDeviceList.clear();
                    bluetoothDeviceList.addAll(bluetoothDevices);

                    GridAdapter gridAdapter = new GridAdapter(getBaseContext(), bluetoothDeviceList);

                    gridView.setVisibility(View.VISIBLE);

                    gridView.setAdapter(gridAdapter);
                    gridView.setNumColumns(3);
                    ViewGroup.LayoutParams layoutParams = gridView.getLayoutParams();

                    layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                    layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;

                    gridView.setLayoutParams(layoutParams);
                    gridAdapter.notifyDataSetChanged();
                }
            }
        });
    }



}