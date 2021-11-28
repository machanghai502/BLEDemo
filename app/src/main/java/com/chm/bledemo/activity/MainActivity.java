package com.chm.bledemo.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.chm.bledemo.R;
import com.chm.bledemo.adspter.MyAdapter;
import com.chm.bledemo.bleutils.BleController;
import com.chm.bledemo.bleutils.callback.ConnectCallback;
import com.chm.bledemo.bleutils.callback.ScanCallback;
import com.chm.bledemo.thread.WriteDataThread;
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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private static final String TAG = "MainActivity";

    private String path;

    private HBluetooth mHBluetooth;

    ThreadPoolExecutor threadPoolExecutor;

    private ListView              listView;
    //扫到的所有设备
    private List<com.hjy.bluetooth.entity.BluetoothDevice> list = new ArrayList<>();
    private MyAdapter adapter;

    private static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;
    private ProgressDialog progressDialog;
    private BleController mBleController;//蓝牙工具类
    private String mDeviceAddress;//当前连接的mac地址

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //ButterKnife.bind(this);
        //initview();

        mHBluetooth = HBluetooth.getInstance();


        findViewById(R.id.btn_scan_ble).setOnClickListener(this);
        findViewById(R.id.btn_disconnect).setOnClickListener(this);

        findViewById(R.id.btn_device_access).setOnClickListener(this);

        findViewById(R.id.btn_complete_check).setOnClickListener(this);

        listView = findViewById(R.id.listView);


        adapter = new MyAdapter(this, list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);





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
                .notifyDelay(200)
                .setMtu(200, new BleMtuChangedCallback() {
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


        initListener();

        // TODO: 2021/11/26 不同手机不一样
        path =  "/sdcard/Download/bluetooth.txt";
        Log.i(TAG, "====" + path);
        File file = new File(path);
        /*Log.i(TAG, "exists:" + file.exists());
        if (!file.exists()) {
            try {
                boolean newFile = file.createNewFile();
                Log.i(TAG, "newFile:" + newFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/

        threadPoolExecutor = new ThreadPoolExecutor(4,4, 60, TimeUnit.SECONDS,new LinkedBlockingDeque<Runnable>());

        //Android 6.0中，某些权限属于Protected Permission,动态获取权限
        if (Build.VERSION.SDK_INT >= 23) {
            int REQUEST_CODE_CONTACT = 101;
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            //验证是否许可权限
            for (String str : permissions) {
                if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    //申请权限
                    this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
                }
            }
        }

    }


    public void initListener() {
        HBluetooth.getInstance().setReceiver(new ReceiveCallBack() {
            @Override
            public void onReceived(DataInputStream dataInputStream, byte[] result) {
                // 打开通知后，设备发过来的数据将在这里出现
                //Log.e("mylog", "收到蓝牙设备返回数据->" + Tools.bytesToHexString(result));
                //Log.e("mylog", "收到蓝牙设备返回数据->" + new String(result));
                Log.e("mylog", System.currentTimeMillis() + ":" + new String(result));

                WriteDataThread writeDataThread = new WriteDataThread(System.currentTimeMillis() + ":" + new String(result), getBaseContext(), path);
                threadPoolExecutor.submit(writeDataThread);
            }
        });
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.btn_disconnect) {
            mHBluetooth.release();
        }  else if (view.getId() == R.id.btn_device_access) {
            startActivity(MainActivity.this, DeviceAccessActivity.class);
        } else if (view.getId() == R.id.btn_complete_check) {
            startActivity(MainActivity.this, CompleteCheckActivity.class);
        } else {
            //todo 每次连接的时候全部删除记录文件，后边根据连接的不同蓝牙删除不同的文件
            File file = new File(path);
            if (file.exists()) {
                boolean delete = file.delete();
                Log.i(TAG, "删除文件成功");
            }

            if (list != null && list.size() > 0) {
                list.clear();
                adapter.notifyDataSetChanged();
            }

            int type = com.hjy.bluetooth.entity.BluetoothDevice.DEVICE_TYPE_LE;

            //如果没有设置扫描时间，低功耗蓝牙扫描需要手动调用stopScan()方法停止扫描，否则会一直扫描下去
            //                new Handler().postDelayed(new Runnable() {
            //                    @Override
            //                    public void run() {
            //                        mHBluetooth.scanner().stopScan();
            //                    }
            //                }, 10000);

            boolean setScanTimeUse = true;
            if (setScanTimeUse) {
                //有设置扫描时间的扫描，时间到会自动结束扫描
                //目前之后BLE蓝牙一种
                scanWithTimeUse(type);
            } else {
                Log.i(TAG, "aaaa");
                //扫描蓝牙设备,没有设置扫描时间,低功耗蓝牙会一直扫描下去
                /*mHBluetooth.scan(type, new ScanCallBack() {
                    @Override
                    public void onScanStart() {
                        Log.i(TAG, "开始扫描");
                    }

                    @Override
                    public void onScanning(List<com.hjy.bluetooth.entity.BluetoothDevice> scannedDevices, com.hjy.bluetooth.entity.BluetoothDevice currentScannedDevice) {
                        Log.i(TAG, "扫描中");
                        if (scannedDevices != null && scannedDevices.size() > 0) {
                            list.clear();
                            list.addAll(scannedDevices);
                            adapter.notifyDataSetChanged();
                        }
                    }


                    @Override
                    public void onError(int errorType, String errorMsg) {
                        Log.e(TAG, "errorType:"+errorType+"  errorMsg:"+errorMsg);
                    }

                    @Override
                    public void onScanFinished(List<BluetoothDevice> bluetoothDevices) {
                        Log.i(TAG, "扫描结束");
                        Toast.makeText(MainActivity.this, "扫描结束", Toast.LENGTH_LONG).show();
                        if (bluetoothDevices != null && bluetoothDevices.size() > 0) {
                            list.clear();
                            list.addAll(bluetoothDevices);
                            adapter.notifyDataSetChanged();
                        }
                    }
                });*/
            }

        }

    }


    private void scanWithTimeUse(int type) {
        //扫描蓝牙设备，扫描6秒就自动停止扫描
        mHBluetooth.scan(type, 10000, new ScanCallBack() {
            @Override
            public void onScanStart() {
                Log.i(TAG, "开始扫描");
            }

            @Override
            public void onScanning(List<BluetoothDevice> scannedDevices, BluetoothDevice currentScannedDevice) {
                Log.i(TAG, "扫描中");
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
                Toast.makeText(MainActivity.this, "BLE扫描结束", Toast.LENGTH_LONG).show();
                if (bluetoothDevices != null && bluetoothDevices.size() > 0) {
                    list.clear();
                    list.addAll(bluetoothDevices);
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "onItemClick: 点解了Item");

        BluetoothDevice device = list.get(position);
        //调用连接器连接蓝牙设备
        mHBluetooth.connect(device, new ConnectCallBack() {

            @Override
            public void onConnecting() {
                Log.i(TAG, "连接中...");
            }

            @Override
            public void onConnected(Sender sender) {
                Log.i(TAG, "连接成功,isConnected:" + mHBluetooth.isConnected());

                //跳转下一页
                //startActivity(MainActivity.this, TestActivity.class);

                //调用发送器发送命令
                byte[] demoCommand = new byte[]{0x01, 0x02};
                sender.send(demoCommand, new SendCallBack() {
                    @Override
                    public void onSending(byte[] command) {
                        Log.i(TAG, "命令发送中...");
                    }

                    @Override
                    public void onSendFailure(BluetoothException bleException) {
                        Log.e("mylog", "发送命令失败->" + bleException.getMessage());
                    }
                });
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

            //低功耗蓝牙才需要BleNotifyCallBack
            //经典蓝牙可以只调两参方法connect(BluetoothDevice device, ConnectCallBack connectCallBack)
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














    /*private void initview() {
        setToolbar();

        mBleController = BleController.getInstance().initble(this);

        initListviev();

        scanDevices(true);
    }*/



    /**
     * 初始化listview
     */
    /*private void initListviev() {
        mListAdspter = new ListViewAdspter(MainActivity.this);
        mListviev.setAdapter(mListAdspter);
        mListviev.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showProgressDialog("请稍候!", "正在连接...");
                final BluetoothDevice device = mListAdspter.getDevice(position);
                if (device == null)
                    return;
                mDeviceAddress = device.getAddress();
                mBleController.Connect(mDeviceAddress, new ConnectCallback() {
                    @Override
                    public void onConnSuccess() {
                        hideProgressDialog();
                        startActivity(MainActivity.this, TestActivity.class);
                    }

                    @Override
                    public void onConnFailed() {
                        hideProgressDialog();
                        Toast.makeText(MainActivity.this, "连接超时，请重试", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
    }


    @OnClick({R.id.btn_new})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_new:
                mListAdspter.clear();
                scanDevices(true);
                break;
        }
    }

    *//**
     * 扫描
     *
     * @param enable
     *//*
    private void scanDevices(final boolean enable) {
        mBleController.ScanBle(enable, new ScanCallback() {
            @Override
            public void onSuccess() {
                if (mListAdspter.mBleDevices.size() < 0) {
                    Toast.makeText(MainActivity.this, "未搜索到Ble设备", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onScanning(BluetoothDevice device, int rssi, byte[] scanRecord) {
                mListAdspter.addDevice(device, getDistance(rssi));
            }
        });
    }
*/

    private static final double A_Value = 60; // A - 发射端和接收端相隔1米时的信号强度
    private static final double n_Value = 2.0; //  n - 环境衰减因子

    public static double getDistance(int rssi) { //根据Rssi获得返回的距离,返回数据单位为m
        int iRssi = Math.abs(rssi);
        double power = (iRssi - A_Value) / (10 * n_Value);
        return Math.pow(10, power);
    }

    public void showProgressDialog(String title, String message) {
        if (progressDialog == null) {
            progressDialog = ProgressDialog.show(this, title, message, true, false);
        } else if (progressDialog.isShowing()) {
            progressDialog.setTitle(title);
            progressDialog.setMessage(message);
        }
        progressDialog.show();
    }

    public void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkGps();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHBluetooth.release();
    }

    /**
     * 开启位置权限
     */
    private void checkGps() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CODE_ACCESS_COARSE_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_ACCESS_COARSE_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //scanDevices(true);
                Toast.makeText(this, "位置权限已开启", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "未开启位置权限", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }



}
