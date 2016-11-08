package com.robot.et.slamtek.base;

import android.app.Activity;
import android.os.Bundle;

import com.robot.et.slamtek.app.CustomerApplication;
import com.slamtec.slamware.SlamwareCorePlatform;

public class BaseActivity extends Activity {

    private CustomerApplication application;
    public SlamwareCorePlatform slamwareCorePlatform;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        application = (CustomerApplication)getApplication();
        slamwareCorePlatform = application.getSlamwareCorePlatform();
    }
}
