package com.robot.et.sltekdemo;

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
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

    private Button forward, backward, left, right, clearMap, test,execTurn;
    private EditText angle;
    private ScaleImageView imageView;
    private TextView battery, quality, charing;
    private SlamwareCorePlatform slamwareCorePlatform;
    private Map map;
    private int mapWidth, mapHeight;

    private Bitmap bitmap;


    int SIGN = 17;

    static {
        Log.e("MainActivity", "load library");
        System.loadLibrary("rpsdk");
    }

    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == SIGN) {
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
        init();
        execConnect();
//        execGetRobotStatus();
//        execTest();
//        new Thread(new Runnable() {
//
//			@Override
//			public void run() {
//				Log.e("bitmap", "exec get bitmap");
//				bitmap=showMap();
//				try {
//					Thread.sleep(500);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//		}).start();
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
                    msg.what = SIGN;
                    handler.sendMessage(msg);
                }
            }, 0, 1000);
        }
    }

    private void init() {
        battery = (TextView) findViewById(R.id.battery);
        quality = (TextView) findViewById(R.id.quality);
        charing = (TextView) findViewById(R.id.charing);
        angle = (EditText)findViewById(R.id.editText1);
        imageView = (ScaleImageView) findViewById(R.id.imageView1);
        forward = (Button) findViewById(R.id.forward);
        backward = (Button) findViewById(R.id.backward);
        left = (Button) findViewById(R.id.left);
        right = (Button) findViewById(R.id.right);
        clearMap = (Button) findViewById(R.id.clearMap);
        test = (Button) findViewById(R.id.test);
        execTurn = (Button)findViewById(R.id.execTurn);



        clearMap.setOnClickListener(this);
        forward.setOnClickListener(this);
        backward.setOnClickListener(this);
        left.setOnClickListener(this);
        right.setOnClickListener(this);
        test.setOnClickListener(this);
        execTurn.setOnClickListener(this);
    }

    private void execConnect() {
        try {
            slamwareCorePlatform = SlamwareCorePlatform.connect("192.168.11.1", 1445);
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

        displayMapInfo(map);

        double mapLeft = (double)map.getMapArea().left/0.05;
        double mapTop = (double)map.getMapArea().top/0.05;
        Log.e("Test","mapLeft:"+mapLeft+",mapTop:"+mapTop);

        double logicPointX = Math.abs(mapLeft);
        double logicPointY = Math.abs(mapTop);
        Log.e("Test","logicX:"+logicPointX+",logicY:"+logicPointY);

        Pose robotPose = slamwareCorePlatform.getPose();
        double robotX = logicPointX+robotPose.getX()/0.05;
        double robotY = logicPointY+robotPose.getY()/0.05;

        displayLocalizationInfo(robotPose);

        bitmap = Bitmap.createBitmap(map.getDimension().getWidth() + 1, map.getDimension().getHeight() + 1, Bitmap.Config.ARGB_4444);

        for (int posY = 0; posY < map.getDimension().getHeight(); ++posY) {
            for (int posX = 0; posX < map.getDimension().getWidth(); ++posX) {
                int color = 127 + map.getData()[posX + (map.getDimension().getHeight() - posY - 1) * map.getDimension().getWidth()];
                    bitmap.setPixel(posX, posY, Color.rgb(color, color, color));
            }
        }




        for (int posY = 0; posY < map.getDimension().getHeight(); ++posY) {
            for (int posX = 0; posX < map.getDimension().getWidth(); ++posX) {
                int color = 127 + map.getData()[posX + (map.getDimension().getHeight() - posY - 1) * map.getDimension().getWidth()];
//				bitmap.setPixel(posX, posY, Color.argb(0, color, color, color));
                if (Math.sqrt(Math.pow(robotX-posX,2)+Math.pow(robotY-posY,2))<1){
                    bitmap.setPixel(posX,posY,Color.BLUE);
                }else {
                    bitmap.setPixel(posX, posY, Color.rgb(color, color, color));
                };
            }
        }
        return bitmap;
    }

    private void updateContent() {
        ViewGroup.LayoutParams lp = imageView.getLayoutParams();
        lp.width = LayoutParams.MATCH_PARENT;
        lp.height = LayoutParams.MATCH_PARENT;
        imageView.setLayoutParams(lp);
//		imageView.setMaxWidth(LayoutParams.MATCH_PARENT);
//		imageView.setMaxHeight(LayoutParams.MATCH_PARENT);
        imageView.setImageBitmap(bitmap);
        imageView.invalidate();
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
        Log.e("MapInfo", "-----------RobotPose--------------");
        Log.e("MapInfo", "poseX:" + robotPose.getX());
        Log.e("MapInfo", "poseY:" + robotPose.getY());
        Log.e("MapInfo", "poseZ:" + robotPose.getZ());
        Log.e("MapInfo", "yaw:" + robotPose.getYaw());
        Log.e("MapInfo", "pitch:" + robotPose.getPitch());
        Log.e("MapInfo", "roll:" + robotPose.getRoll());
        Log.e("MapInfo", "==================================");
    }

    private void execGetRobotStatus() {
        Log.e("RobotStatus", "===================RobotStatus==================");
        Log.e("RobotStatus", "IsCharging:" + slamwareCorePlatform.getBatteryIsCharging());
        Log.e("RobotStatus", "BatteryPercent:" + slamwareCorePlatform.getBatteryPercentage());
        Log.e("RobotStatus", "DCISConnected:" + slamwareCorePlatform.getDCIsConnected());
        Log.e("RobotStatus", "SlamwareVersion" + slamwareCorePlatform.getSlamwareVersion());
        Log.e("RobotStatus", "SDKVersion:" + slamwareCorePlatform.getSDKVersion());
        Log.e("RobotStatus", "================================================");
    }

    private void execTest() {
        Log.e("test", "=====================execTest=====================");
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

    private void execTurn(){
        double radians=Math.toRadians(Double.valueOf(angle.getText().toString()));
        Rotation rotation = new Rotation((float)radians,0,0);
        slamwareCorePlatform.rotate(rotation);
    }

    @Override
    public void onClick(View view) {
        if (null == slamwareCorePlatform) {
            Toast.makeText(MainActivity.this, "请先点击Connect按钮", Toast.LENGTH_LONG).show();
            return;
        }
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
            case R.id.clearMap:
                slamwareCorePlatform.clearMap();
            case R.id.test:
                execTest();
            case R.id.execTurn:
                execTurn();

            default:
                break;
        }
    }
}
