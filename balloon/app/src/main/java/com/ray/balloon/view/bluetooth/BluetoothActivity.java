package com.ray.balloon.view.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;

import com.corelibs.base.BaseActivity;
import com.corelibs.views.SplideBackLinearLayout;
import com.ray.balloon.R;
import com.ray.balloon.adapter.BluetoothDevicesAdapter;
import com.ray.balloon.callback.RecyclerViewCallback;
import com.ray.balloon.presenter.BluetoothPresenter;

import java.lang.reflect.Method;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import carbon.beta.TransitionLayout;
import carbon.widget.FrameLayout;
import carbon.widget.ImageView;
import carbon.widget.LinearLayout;
import carbon.widget.RecyclerView;
import carbon.widget.TextView;
import carbon.widget.Toolbar;

/**
 * Created by Administrator on 2016/3/3.
 */
public class BluetoothActivity extends BaseActivity<BluetoothView, BluetoothPresenter> implements BluetoothView, RecyclerViewCallback {
    private boolean isTurnOn = false;
    private BluetoothDevicesAdapter adapter;
    private int postion = -1;
    @Bind(R.id.spl_back)
    SplideBackLinearLayout spl_back;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.icon_bluetooth)
    ImageView icon_bluetooth;
    @Bind(R.id.powerMenu)
    View powerMenu;
    @Bind(R.id.transition)
    TransitionLayout transitionLayout;
    @Bind(R.id.ll_turn_on)
    LinearLayout ll_turn_on;
    @Bind(R.id.tv_bluetooth_notice)
    TextView tv_bluetooth_notice;
    @Bind(R.id.recyclerView)
    RecyclerView recyclerView;
    @Bind(R.id.icon_bluetooth_message)
    ImageView icon_bluetooth_message;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_bluetooth;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        spl_back.setBackListener(this);
        toolbar.setText(R.string.bluetooth_title);

        toolbar.setBackgroundColor(getResources().getColor(R.color.main));
        toolbar.setIcon(R.drawable.img_back);
        initLayout();
        initReceiver();

        if (!getPresenter().getEnableBluetooth()) {
            showToast("您的设备暂不支持蓝牙");
        }


        isTurnOn = getPresenter().getBluetoothTurnOnState();
        if (!isTurnOn) {
            showToast("您暂未开启蓝牙");
        } else {
            getPresenter().start();
        }


    }

    private void initLayout() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        adapter = new BluetoothDevicesAdapter();
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(this);

    }


    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        getPresenter().cancleDiscovery();
        getPresenter().stop();
        super.onDestroy();
    }

    private void initReceiver() {
        // 注册BroadcastReceiver
        IntentFilter filter = new IntentFilter();

        filter.addAction(BluetoothDevice.ACTION_FOUND);//发现设备
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter); // 不要忘了之后解除绑定

    }

    @Override
    protected BluetoothPresenter createPresenter() {
        return new BluetoothPresenter();
    }

    @OnClick(R.id.icon_bluetooth_message)
    protected void sendMessage() {
        getPresenter().write("hello ray".getBytes());
    }

    @OnClick(R.id.icon_bluetooth)
    protected void makeTureTurnOn() {
        isTurnOn = getPresenter().getBluetoothTurnOnState();
        if (isTurnOn) {
            tv_bluetooth_notice.setText(R.string.bluetooth_is_on);
        }

        if (powerMenu.getVisibility() == View.VISIBLE)
            return;
        transitionLayout.setCurrentChild(0);
        final List<View> viewsWithTag = ((LinearLayout) transitionLayout.getChildAt(0)).findViewsWithTag("animate");
        for (int i = 0; i < viewsWithTag.size(); i++)
            viewsWithTag.get(i).setVisibility(View.INVISIBLE);
        powerMenu.setVisibility(View.VISIBLE);
        icon_bluetooth.getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < viewsWithTag.size(); i++) {
                    final int finalI = i;
                    icon_bluetooth.getHandler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            viewsWithTag.get(finalI).setVisibility(View.VISIBLE);
                        }
                    }, i * 40);
                }
            }
        }, 200);


    }

    @OnClick(R.id.ll_turn_on)
    protected void turnOn() {
        if (isTurnOn) {
            powerMenu.setVisibility(View.INVISIBLE);
            return;
        }
        final List<View> viewsWithTag = ((FrameLayout) transitionLayout.getChildAt(1)).findViewsWithTag("animate");
        for (int i = 0; i < viewsWithTag.size(); i++)
            viewsWithTag.get(i).setVisibility(View.INVISIBLE);
        ll_turn_on.getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < viewsWithTag.size(); i++) {
                    final int finalI = i;
                    ll_turn_on.getHandler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            viewsWithTag.get(finalI).setVisibility(View.VISIBLE);
                        }
                    }, i * 20);
                }
            }
        }, 400);
        transitionLayout.setHotspot(ll_turn_on.findViewById(R.id.blueTooth_Icon));
        transitionLayout.startTransition(1, TransitionLayout.TransitionType.Radial);
        ll_turn_on.getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                powerMenu.setVisibility(View.INVISIBLE);
            }
        }, 3000);
        getPresenter().turnOn(this);
    }

    @Override
    public void onBackPressed() {
        if (powerMenu.getVisibility() == View.VISIBLE) {
            powerMenu.setVisibility(View.INVISIBLE);
            return;
        }
        super.onBackPressed();
    }


    // 创建一个接收ACTION_FOUND广播的BroadcastReceiver

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i("wanglei", "action:" + action);
            // 发现设备
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // 从Intent中获取设备对象
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // 将设备名称和地址放入array adapter，以便在ListView中显示
                Log.i("wanglei", device.getName() + "\n" + device.getAddress() + "\n" + device.getBondState());
                adapter.addDevice(device);

//                if (device.getName().equalsIgnoreCase("红米手机")) {
//                    // 搜索蓝牙设备的过程占用资源比较多，一旦找到需要连接的设备后需要及时关闭搜索
//                    getPresenter().cancleDiscovery();


            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                Log.i("wanglei", "状态改变：" + getPresenter().getBluetoothTurnOnState());
                if (getPresenter().getBluetoothTurnOnState()) {
                    getPresenter().start();
                }
            }
        }
    };


    @Override
    public void onItemClick(int position) {
        this.postion = position;
        BluetoothDevice device = adapter.getItem(position);
        // 获取蓝牙设备的连接状态
        int connectState = device.getBondState();
        switch (connectState) {
            // 未配对
            case BluetoothDevice.BOND_NONE:
                Log.i("wanglei", "配对");
                // 配对
                try {
                    Method createBondMethod = BluetoothDevice.class.getMethod("createBond");
                    createBondMethod.invoke(device);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            // 已配对
            case BluetoothDevice.BOND_BONDED:
                Log.i("wanglei", "连接");
                // 连接
                getPresenter().connect(device);
                break;
        }
    }

    @Override
    public void connectSuccess() {

    }

    @Override
    public void changeState(int state) {
        adapter.setState(state, postion);
    }
}