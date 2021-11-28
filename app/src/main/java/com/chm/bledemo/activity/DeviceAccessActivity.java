package com.chm.bledemo.activity;

import android.app.ActionBar;
import android.content.Context;
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

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

public class DeviceAccessActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private static final String TAG = "DeviceAccessActivity";

    private ImageView radarImageView;

    private LinearLayout middleLine;

    private GridView gridView;

    private Context context;

    private String[] names = {"京东商城","QQ","QQ斗地主","新浪微博","天猫","UC浏览器","微信"};

    private List<String> list = Arrays.asList(names);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_access2);

        context = getBaseContext();

        //发现设备按钮
        findViewById(R.id.btn_find_device).setOnClickListener(this);

        radarImageView = findViewById(R.id.img_radar);

        middleLine = findViewById(R.id.device_access_line_middle);

        //gridView
        gridView = findViewById(R.id.grid_device_access);
        gridView.setOnItemClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_find_device) {

            //更改btn文字
            Button btnFindDevice = (Button) v;
            btnFindDevice.setText("正在搜索附近设备...");

            //扫描所有的蓝牙设备

            //隐藏ImageView
            middleLine.removeView(radarImageView);

            //模拟展示发现的设备列表
            GridAdapter gridAdapter = new GridAdapter(getBaseContext(), null, list);

            gridView.setVisibility(View.VISIBLE);

            gridView.setAdapter(gridAdapter);
            gridView.setNumColumns(3);
            ViewGroup.LayoutParams layoutParams = gridView.getLayoutParams();

            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;

            gridView.setLayoutParams(layoutParams);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //点击跳转下一页。
        //建议优化成直接连接蓝牙即可
        Log.d(TAG, "onItemClick: 点解了Item");
        String s = list.get(position);
        Toast.makeText(context, s, Toast.LENGTH_LONG).show();
    }
}