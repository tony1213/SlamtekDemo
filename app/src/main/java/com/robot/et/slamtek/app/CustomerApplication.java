package com.robot.et.slamtek.app;

import android.app.Application;

import com.slamtec.slamware.SlamwareCorePlatform;

/**
 * Created by Tony on 2016/11/2.
 */

public class CustomerApplication extends Application {

    private static final String ROBOT_IP = "192.168.11.1";
    private static final int PORT = 1445;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public SlamwareCorePlatform getSlamwareCorePlatform(){
        return SlamwareCorePlatform.connect(ROBOT_IP,PORT);
    }
}
