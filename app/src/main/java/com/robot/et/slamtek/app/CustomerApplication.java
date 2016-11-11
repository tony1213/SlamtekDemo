package com.robot.et.slamtek.app;

import android.app.Application;
import android.util.Log;

import com.slamtec.slamware.SlamwareCorePlatform;
import com.slamtec.slamware.discovery.AbstractDiscover;
import com.slamtec.slamware.discovery.Device;
import com.slamtec.slamware.discovery.DeviceManager;
import com.slamtec.slamware.discovery.DiscoveryMode;

import java.util.UUID;

/**
 * Created by Tony on 2016/11/2.
 */

public class CustomerApplication extends Application {

    private static final String ROBOT_IP = "192.168.11.1";
    private static final int PORT = 1445;
    private static final String WIFI_SSID = "ROBOTAI_MINO";
    private static final String WIFI_PASSWORD = "robotai@1234";

    @Override
    public void onCreate() {
        super.onCreate();


        Device device = new Device() {
            @Override
            public boolean canBeFoundWith(DiscoveryMode discoveryMode) {
                Log.e("connect","canBeFoundWith:"+discoveryMode.name());
                return false;
            }
        };
        UUID uuid = device.getDeviceId();

        new DeviceManager(getApplicationContext()).pair(new Device() {
            @Override
            public boolean canBeFoundWith(DiscoveryMode discoveryMode) {
                return false;
            }
        }, WIFI_SSID, WIFI_PASSWORD, new AbstractDiscover.BleConfigureListener() {
            @Override
            public void onConfigureSuccess() {
                Log.e("connect","配置成功");
            }

            @Override
            public void onConfigureFailure(String s) {
                Log.e("connect","错误信息："+s.toString());
            }
        });
    }

    public SlamwareCorePlatform getSlamwareCorePlatform(){
        return SlamwareCorePlatform.connect(ROBOT_IP,PORT);
    }
}
