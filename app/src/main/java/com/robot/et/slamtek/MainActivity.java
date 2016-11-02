package com.robot.et.slamtek;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import com.slamtec.slamware.SlamwareCorePlatform;
import com.slamtec.slamware.action.MoveDirection;
import com.slamtec.slamware.robot.LaserPoint;
import com.slamtec.slamware.robot.LaserScan;
import com.slamtec.slamware.robot.Location;
import com.slamtec.slamware.robot.Map;
import com.slamtec.slamware.robot.MapKind;
import com.slamtec.slamware.robot.MapType;
import com.slamtec.slamware.robot.Pose;
import com.slamtec.slamware.robot.Rotation;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends Activity{

    @BindView(R.id.forward) Button forward;
    @BindView(R.id.backward) Button backward;
    @BindView(R.id.left) Button left;
    @BindView(R.id.right) Button right;
    @BindView(R.id.clearMap) Button clearMap;
    @BindView(R.id.test) Button test;
    @BindView(R.id.execTurn) Button execTurn;
    @BindView(R.id.moveGoal) Button moveGoal;
    @BindView(R.id.battery) TextView battery;
    @BindView(R.id.quality) TextView quality;
    @BindView(R.id.charing) TextView charing;
    @BindView(R.id.angle) EditText angle;
    @BindView(R.id.goalx) EditText goalX;
    @BindView(R.id.goaly) EditText goalY;

    private ScaleImageView mapView;

    private SlamwareCorePlatform slamwareCorePlatform;
    private Pose robotPose;
    private Map map;
    private Bitmap bitmap;

    private static final int UPATE_MAP_SIGN = 17;
    private static final String ROBOT_IP = "192.168.11.1";
    private static final int PORT = 1445;

    static {
        Log.e("MainActivity", "load library");
        System.loadLibrary("rpsdk");
    }

    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == UPATE_MAP_SIGN) {
                showMap();
                updateContent();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//横屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mapView = (ScaleImageView) findViewById(R.id.imageView1);
        execConnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null == slamwareCorePlatform) {
            Toast.makeText(MainActivity.this, "连接异常", Toast.LENGTH_LONG).show();
        } else {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    Message msg = new Message();
                    msg.what = UPATE_MAP_SIGN;
                    handler.sendMessage(msg);
                }
            }, 0, 1000);
        }
    }

    private void execConnect() {
        try {
            slamwareCorePlatform = SlamwareCorePlatform.connect(ROBOT_IP, PORT);
        } catch (Exception e) {
            Log.e("MainActivity", "Exception:" + e.toString());
            Toast.makeText(MainActivity.this, "连接失败", Toast.LENGTH_LONG).show();
            return;
        } finally {
            if (null == slamwareCorePlatform) {
                Log.e("MainActivity", "Error:slamwareCorePlatform is null");
                return;
            }
            Toast.makeText(MainActivity.this, "SlamwareVersion:" + slamwareCorePlatform.getSDKVersion(), Toast.LENGTH_LONG).show();
        }
    }

    private Bitmap showMap() {
        Log.e("MainActivity", "execShowMap()");
        RectF knowArea = slamwareCorePlatform.getKnownArea(MapType.BITMAP_8BIT, MapKind.EXPLORE_MAP);
        map = slamwareCorePlatform.getMap(MapType.BITMAP_8BIT, MapKind.EXPLORE_MAP, knowArea);

        double mapLeft = (double)map.getMapArea().left/0.05;
        double mapTop = (double)map.getMapArea().top/0.05;
        Log.e("Test","mapLeft:"+mapLeft+",mapTop:"+mapTop);

        double logicPointX = Math.abs(mapLeft);
        double logicPointY = Math.abs(mapTop);
        Log.e("Test","logicX:"+logicPointX+",logicY:"+logicPointY);

        robotPose = slamwareCorePlatform.getPose();
        double robotX = logicPointX+robotPose.getX()/0.05;
        double robotY = logicPointY+robotPose.getY()/0.05;

        displayLocalizationInfo(robotPose);

        bitmap = Bitmap.createBitmap(map.getDimension().getWidth() + 1, map.getDimension().getHeight() + 1, Bitmap.Config.ARGB_4444);
        for (int posY = 0; posY < map.getDimension().getHeight(); ++posY) {
            for (int posX = 0; posX < map.getDimension().getWidth(); ++posX) {
                int color = 127 + map.getData()[posX + (map.getDimension().getHeight() - posY - 1) * map.getDimension().getWidth()];
//				bitmap.setPixel(posX, posY, Color.argb(0, color, color, color));
                if (Math.sqrt(Math.pow(robotX-posX,2)+Math.pow(robotY-posY,2))<2){
                    bitmap.setPixel(posX,posY,Color.BLUE);
                }else {
                    bitmap.setPixel(posX, posY, Color.rgb(color, color, color));
                };
            }
        }
        return bitmap;
    }

    private void updateContent() {
        ViewGroup.LayoutParams lp = mapView.getLayoutParams();
        lp.width = LayoutParams.MATCH_PARENT;
        lp.height = LayoutParams.MATCH_PARENT;
        mapView.setLayoutParams(lp);
        mapView.setImageBitmap(bitmap);
        mapView.invalidate();
        battery.setText("Battery:" + slamwareCorePlatform.getBatteryPercentage() + "%");
        quality.setText("Quality:" + slamwareCorePlatform.getLocalizationQuality());
        charing.setText("Charing:" + slamwareCorePlatform.getBatteryIsCharging());
    }

    private void displayMapInfo(Map map) {
        Log.e("MapInfo", "==================================");
        Log.e("MapInfo", "-----------Map Area---------------");
        Log.e("MapInfo", "left=" + map.getMapArea().left);
        Log.e("MapInfo", "right=" + map.getMapArea().right);
        Log.e("MapInfo", "top=" + map.getMapArea().top);
        Log.e("MapInfo", "bottom=" + map.getMapArea().bottom);
//        Log.e("MapInfo","MapPosition:"+map.)
        Log.e("MapInfo", "centerX=" + map.getMapArea().centerX());
        Log.e("MapInfo", "centerY=" + map.getMapArea().centerY());
        Log.e("MapInfo", "width=" + map.getMapArea().width());
        Log.e("MapInfo", "height=" + map.getMapArea().height());

        Log.e("MapInfo", "-----------Cell Dimension----------");
        Log.e("MapInfo", "dimension_Width==" + map.getDimension().getWidth());
        Log.e("MapInfo", "dimension_Height==" + map.getDimension().getHeight());

        Log.e("MapInfo", "-----------Cell Resolution----------");
        Log.e("MapInfo", "resolutionX==" + map.getResolution().getX());
        Log.e("MapInfo", "resolutionY==" + map.getResolution().getY());

        Log.e("MapInfo", "-------------Origin---------------");
        Log.e("MapInfo", "originX==" + map.getOrigin().getX());
        Log.e("MapInfo", "originY==" + map.getResolution().getY());

        Log.e("MapInfo", "-----------Timestamp--------------");
        Log.e("MapInfo", "Timestamp:" + map.getTimestamp());

        Log.e("MapInfo", "--------------Test-----------------");
        Log.e("MapInfo", "Test_width:" + map.getDimension().getWidth() * map.getResolution().getX());
        Log.e("MapInfo", "Test_height:" + map.getDimension().getHeight() * map.getResolution().getY());
    }

    private void displayLocalizationInfo(Pose robotPose) {
        Log.e("Pose", "-----------RobotPose--------------");
        Log.e("Pose", "poseX:" + robotPose.getX());
        Log.e("Pose", "poseY:" + robotPose.getY());
        Log.e("Pose", "poseZ:" + robotPose.getZ());
        Log.e("Pose", "yaw:" + robotPose.getYaw());
        Log.e("Pose", "pitch:" + robotPose.getPitch());
        Log.e("Pose", "roll:" + robotPose.getRoll());
        Log.e("Pose", "==================================");
    }

    @OnClick(R.id.test)
    public void execTest() {
        displayMapInfo(map);
        Log.e("test", "=====================execTest=====================");
        Log.e("RobotStatus", "===================RobotStatus==================");
        Log.e("RobotStatus", "IsCharging:" + slamwareCorePlatform.getBatteryIsCharging());
        Log.e("RobotStatus", "BatteryPercent:" + slamwareCorePlatform.getBatteryPercentage());
        Log.e("RobotStatus", "DCISConnected:" + slamwareCorePlatform.getDCIsConnected());
        Log.e("RobotStatus", "SlamwareVersion" + slamwareCorePlatform.getSlamwareVersion());
        Log.e("RobotStatus", "SDKVersion:" + slamwareCorePlatform.getSDKVersion());
        Log.e("RobotStatus", "================================================");
        List<MapType> list = slamwareCorePlatform.getAvailableMaps();
        for (int i = 0; i < list.size(); i++) {
            Log.e("MapType", "MapType:" + list.get(i));
        }
        Location location = slamwareCorePlatform.getLocation();
        float locationX = location.getX();
        float locationY = location.getY();
        float locationZ = location.getZ();
        Log.e("location", "location:(" + locationX + "," + locationY + "," + locationZ + ")");

        Log.e("mapLocalization", "mapLocalization:" + slamwareCorePlatform.getMapLocalization());
        Log.e("RobotStatus", "===================RobotStatus==================");
        Log.e("RobotStatus", "IsCharging:" + slamwareCorePlatform.getBatteryIsCharging());
        Log.e("RobotStatus", "BatteryPercent:" + slamwareCorePlatform.getBatteryPercentage());
        Log.e("RobotStatus", "DCISConnected:" + slamwareCorePlatform.getDCIsConnected());
        Log.e("RobotStatus", "SlamwareVersion" + slamwareCorePlatform.getSlamwareVersion());
        Log.e("RobotStatus", "SDKVersion:" + slamwareCorePlatform.getSDKVersion());
        Log.e("RobotStatus", "================================================");

        Pose robotPose = slamwareCorePlatform.getPose();
        displayLocalizationInfo(robotPose);

        LaserScan laserScan = slamwareCorePlatform.getLaserScan();
        Vector<LaserPoint> Vpoint = laserScan.getLaserPoints();
        for (int i = 0; i < Vpoint.size(); i++) {
            Log.e("LaserPoint", "Distance:" + Vpoint.get(i).getDistance() + ",angle:" + Vpoint.get(i).getAngle());
        }
    }

    @OnClick(R.id.execTurn)
    public void execTurn(){
        String data=angle.getText().toString().trim();
        if (TextUtils.equals("",data)){
            return;
        }
        double radians=Math.toRadians(Double.valueOf(data));
        Rotation rotation = new Rotation((float)radians,0,0);
        slamwareCorePlatform.rotate(rotation);
    }

    @OnClick(R.id.moveGoal)
    public void execMoveGoal(){
        String dataX = goalX.getText().toString().trim();
        String dataY = goalY.getText().toString().trim();
        if (TextUtils.equals("",dataX) || TextUtils.equals("",dataY)){
            return;
        }
        Location location = new Location();
        location.setX(Float.valueOf(dataX));
        location.setY(Float.valueOf(dataY));
        location.setZ(0.0f);
        slamwareCorePlatform.moveTo(location);
    }
    @OnClick({R.id.forward,R.id.backward,R.id.left,R.id.right})
    public void execForward(View view){
        switch (view.getId()) {
            case R.id.forward:
                slamwareCorePlatform.moveBy(MoveDirection.FORWARD);
                break;
            case R.id.backward:
                slamwareCorePlatform.moveBy(MoveDirection.BACKWARD);
                break;
            case R.id.left:
                slamwareCorePlatform.moveBy(MoveDirection.TURN_LEFT);
                break;
            case R.id.right:
                slamwareCorePlatform.moveBy(MoveDirection.TURN_RIGHT);
                break;
            default:
                break;
        }
    }
}
