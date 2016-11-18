package com.robot.et.slamtek;

import android.util.Log;

import com.google.common.base.Preconditions;
import com.slamtec.slamware.SlamwareCorePlatform;
import com.slamtec.slamware.action.MoveDirection;
import com.slamtec.slamware.robot.Location;
import com.slamtec.slamware.robot.Pose;
import com.slamtec.slamware.robot.Rotation;

import java.util.List;

/**
 * Created by Tony on 2016/11/11.
 *
 */

public class SlamtecLoader {

    private static final String TAG = "slamtec";

    private static final String ROBOT_IP = "192.168.11.1";//slamtec 底盘的IP地址
    private static final int PORT = 1445;//slamtec 访问底盘的端口号

    //Basic Error Message
    private static final String ERROR_MESSAGE_CONNECT = "连接底盘出现异常，请检查，错误级别高";
    private static final String ERROR_MESSAGE_WAKEUP = "唤醒角度参数异常，请检查";
    private static final String ERROR_MESSAGE_DIRECTION = "旋转方向参数异常，请检查";
    private static final String ERROR_MESSAGE_CIRCLE = "旋转圈数参数异常，请检查";
    private static final String ERROR_MESSAGE_SET_GOAL = "导航目标参数异常，请检查";

    //Basic MoveDirection And RotateDirection
    private static final int MOVEDIRECTION_FORWARD = 1;
    private static final int MOVEDIRECTION_BACKWARD = 2;
    private static final int MOVEDIRECTION_TURN_LEFT = 3;
    private static final int MOVEDIRECTION_TURN_RIGHT = 4;
    private static final int MOVEDIRECTION_CANCEL = 5;

    public SlamwareCorePlatform slamwareCorePlatform;

    private static SlamtecLoader slamtecLoader = null;

    private SlamtecLoader(){

    }

    public static synchronized SlamtecLoader getInstance(){
        if (null == slamtecLoader){
            slamtecLoader = new SlamtecLoader();
        }
        return slamtecLoader;
    }

    //slamtec连接
    public SlamwareCorePlatform execConnect(){
        Log.e(TAG,"execConnect");
        try {
            slamwareCorePlatform = SlamwareCorePlatform.connect(ROBOT_IP,PORT);
        }catch (Exception e){
            Log.e("slamtec","Connect fail,错误级别高");
            e.printStackTrace();
        }
        return slamwareCorePlatform;
    }

    //slamtec Version
    public String getSlamtecVersion(){
        Log.e(TAG,"execGetSlamtecVersion");
        Preconditions.checkNotNull(slamwareCorePlatform,ERROR_MESSAGE_CONNECT);
        return slamwareCorePlatform.getSDKVersion();
    }

    //slamtec Battery Precent
    public int getBatteryPrecent(){
        Log.e(TAG,"execGetBatteryPrecent");
        Preconditions.checkNotNull(slamwareCorePlatform,ERROR_MESSAGE_CONNECT);
        return slamwareCorePlatform.getBatteryPercentage();
    }

    //slamtec quality
    public int getLocalizationQuality(){
        Log.e(TAG,"getLocalizationQuality");
        Preconditions.checkNotNull(slamwareCorePlatform,ERROR_MESSAGE_CONNECT);
        return slamwareCorePlatform.getLocalizationQuality();
    }

    //slamtec Charing
    public boolean getCharingState(){
        Log.e(TAG,"getCharingState");
        Preconditions.checkNotNull(slamwareCorePlatform,ERROR_MESSAGE_CONNECT);
        return slamwareCorePlatform.getBatteryIsCharging();
    }

    //slamtec 运动控制
    public void execBasicMove(int direction){
        Log.e(TAG,"execBasicMove:"+direction);
        Preconditions.checkNotNull(slamwareCorePlatform,ERROR_MESSAGE_CONNECT);
        switch (direction){
            case MOVEDIRECTION_FORWARD:
                slamwareCorePlatform.moveBy(MoveDirection.FORWARD);
                break;
            case MOVEDIRECTION_BACKWARD:
                slamwareCorePlatform.moveBy(MoveDirection.BACKWARD);
                break;
            case MOVEDIRECTION_TURN_LEFT:
                slamwareCorePlatform.moveBy(MoveDirection.TURN_LEFT);
                break;
            case MOVEDIRECTION_TURN_RIGHT:
                slamwareCorePlatform.moveBy(MoveDirection.TURN_RIGHT);
                break;
            case MOVEDIRECTION_CANCEL:
                slamwareCorePlatform.getCurrentAction().cancel();
                break;
        }
    }

    //slamtec 控制唤醒角度转向，这个方法只适用于硬件唤醒
    public void execBasicRotate(int degree){
        Log.e(TAG,"execBasicRotate:唤醒角度===="+degree);
        Preconditions.checkNotNull(slamwareCorePlatform,ERROR_MESSAGE_CONNECT);
        Preconditions.checkArgument(degree >= 0 && degree <=360,ERROR_MESSAGE_WAKEUP);
        if (degree < 180){
            slamwareCorePlatform.rotate(new Rotation((float) Math.toRadians((double) -degree),0,0));
        }else {
            slamwareCorePlatform.rotate(new Rotation((float) Math.toRadians((double) 360-degree),0,0));
        }
    }

    //slamtec 控制旋转方向和旋转角度
    //direction:3是向左，4是向右。
    public void execBasicRotate(int degree,int direction,int circle){
        Log.e(TAG,"execBasicRotate");
        Preconditions.checkNotNull(slamwareCorePlatform,ERROR_MESSAGE_CONNECT);
        Preconditions.checkNotNull(direction,ERROR_MESSAGE_DIRECTION);
        Preconditions.checkNotNull(circle,ERROR_MESSAGE_CIRCLE);
        Preconditions.checkArgument(degree >= 0 && degree <=360,ERROR_MESSAGE_WAKEUP);
        switch (direction){
            case MOVEDIRECTION_TURN_LEFT:
                slamwareCorePlatform.rotate(new Rotation((float) Math.toRadians((double) degree+circle*360),0,0));
                break;
            case MOVEDIRECTION_TURN_RIGHT:
                slamwareCorePlatform.rotate(new Rotation((float) Math.toRadians((double) -degree+circle*360),0,0));
                break;
        }
    }

    //slamtec获取当前机器人的坐标
    public Location getCurrentRobotPose(){
        Log.e(TAG,"getCurrentRobotPose");
        Preconditions.checkNotNull(slamwareCorePlatform,ERROR_MESSAGE_CONNECT);
        Pose robotPose = slamwareCorePlatform.getPose();
        Location location = new Location();
        location.setX(robotPose.getX());
        location.setY(robotPose.getY());
        location.setZ(robotPose.getZ());
        return location;
    }

    //slamtec设置导航目标  <策略一>
    public void execSetGoal(Pose pose){
        Log.e(TAG,"execSetGoal(Pose pose)");
        Preconditions.checkNotNull(slamwareCorePlatform,ERROR_MESSAGE_CONNECT);
        execSetGoal(pose.getLocation());
    }

    //slamtec设置导航目标 <策略二>
    public void execSetGoal(float robotX , float robotY){
        Log.e(TAG,"execSetGoal(float robotX , float robotY)");
        Preconditions.checkNotNull(robotX,ERROR_MESSAGE_SET_GOAL);
        Preconditions.checkNotNull(robotY,ERROR_MESSAGE_SET_GOAL);
        Location location = new Location();
        location.setX(robotX);
        location.setY(robotY);
        location.setZ(0.0f);
        execSetGoal(location);
    }

    //slamtec设置导航目标 <策略三>
    public void execSetGoal(Location location){
        Log.e(TAG,"execSetGoal(Location location)");
        Preconditions.checkNotNull(slamwareCorePlatform,ERROR_MESSAGE_CONNECT);
        slamwareCorePlatform.moveTo(location);
    }

    //slamtec设置巡逻模式 <策略一>
    public void execPatrol(List<Location> list){
        Log.e(TAG,"execPatrol(List<Location> list)");
        Preconditions.checkNotNull(slamwareCorePlatform,ERROR_MESSAGE_CONNECT);
        slamwareCorePlatform.moveTo(list);
    }

    //slamtec设置巡逻模式 <策略二>
    public void execPatrol(List<Location> list, boolean appending){
        Log.e(TAG,"execPatrol(List<Location> list,boolean appending)");
        Preconditions.checkNotNull(slamwareCorePlatform,ERROR_MESSAGE_CONNECT);
        slamwareCorePlatform.moveTo(list,appending);
    }

    //slamtec设置巡逻模式 <策略三>
    public void execPatrol(List<Location> list, boolean appending, boolean isMilestone){
        Log.e(TAG,"execPatrol(List<Location> list,boolean appending,boolean isMilestone)");
        Preconditions.checkNotNull(slamwareCorePlatform,ERROR_MESSAGE_CONNECT);
        slamwareCorePlatform.moveTo(list,appending,isMilestone);
    }
}
