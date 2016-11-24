package com.robot.et.slamtek.agent;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.robot.et.slamtek.R;
import com.robot.et.slamtek.event.ConnectedEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends Activity {

    private static final String ROBOT_IP = "192.168.11.1";//slamtec 底盘的IP地址
    private static final int PORT = 1445;//slamtec 访问底盘的端口号

    private SlamwareAgent slamwareAgent;

    static {
        Log.e("MainActivity", "load library");
        System.loadLibrary("rpsdk");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_agent);
        ButterKnife.bind(this);
        slamwareAgent = new SlamwareAgent();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @OnClick(R.id.connect)
    public void execConnect(){
        Log.e("connect","execConnect()");
        slamwareAgent.connectTo(ROBOT_IP,PORT);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectedEvent(ConnectedEvent event){
        Toast.makeText(this,"连接状态："+event.message,Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}
