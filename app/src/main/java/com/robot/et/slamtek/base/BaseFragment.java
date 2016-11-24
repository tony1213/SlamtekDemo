package com.robot.et.slamtek.base;

import android.app.Fragment;
import android.os.Bundle;

import com.robot.et.slamtek.other.SlamtecLoader;
import com.slamtec.slamware.SlamwareCorePlatform;

/**
 * Created by Tony on 2016/11/2.
 */

public class BaseFragment extends Fragment {

    public SlamwareCorePlatform slamwareCorePlatform;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        slamwareCorePlatform = SlamtecLoader.getInstance().execConnect();
    }
}
