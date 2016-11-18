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

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
