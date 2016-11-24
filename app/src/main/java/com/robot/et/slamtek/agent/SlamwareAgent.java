package com.robot.et.slamtek.agent;

import com.google.common.base.Preconditions;
import com.robot.et.slamtek.event.ConnectedEvent;
import com.robot.et.slamtek.event.ConnectionLostEvent;
import com.slamtec.slamware.AbstractSlamwarePlatform;
import com.slamtec.slamware.discovery.DeviceManager;

import org.greenrobot.eventbus.EventBus;

/**
 * @author Tony
 * @date 2016/11/24
 */

public class SlamwareAgent {

    private AbstractSlamwarePlatform robotPlatform_;
    private Worker worker;

    private String ip_;
    private int port_;
    private static boolean connected_;   //Robot连接状态

    private static JobConnect jobConnect;            //连接底盘JOB
    private static JobUpdateStatus jobUpdateStatus;  //更新状态JOB

    public SlamwareAgent(){
        worker = new Worker();
        jobConnect = new JobConnect();
        jobUpdateStatus = new JobUpdateStatus();
    }

    // 连接
    public void connectTo(String ip, int port) {
        this.ip_ = ip;
        this.port_ = port;
        pushJob(jobConnect);
    }

    private class JobConnect implements Runnable{
        @Override
        public void run() {
            try{
                String ip;
                int port;
                synchronized (SlamwareAgent.this) {
                    ip = ip_;
                    port = port_;
                    Preconditions.checkNotNull(ip, "ip is null");
                    Preconditions.checkArgument(port > 0 && port <= 65535, "port is not in 0 < port <= 65535");
                    AbstractSlamwarePlatform robotPlatform = DeviceManager.connect(ip, port);
                    robotPlatform_ = robotPlatform;
                    connected_ = true;
                }
            }catch (Exception exception){
                onRequestError(exception);
                return;
            }
            EventBus.getDefault().post(new ConnectedEvent("Connect Successful"));
        }
    }

    private class JobUpdateStatus implements Runnable{
        @Override
        public void run() {


        }
    }

    private synchronized void pushJobHead(Runnable job) {
        worker.pushHead(job);
    }

    private synchronized void pushJob(Runnable job) {
        worker.push(job);
    }

    private void onRequestError(Exception e) {
        e.printStackTrace();
        synchronized (this) {
            worker.clear();
            robotPlatform_ = null;
            connected_ = false;
        }
        EventBus.getDefault().post(new ConnectionLostEvent());
    }
}
