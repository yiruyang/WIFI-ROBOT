package com.dsrobot;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import com.dsrobot.utils.BaseActivity;

import java.io.BufferedInputStream;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends BaseActivity {

    private MjpegInputStream mis = null;
    private MjpegView mjpegView = null;
    private SocketClient socketClient = null;
    private long firstTime = 0;
    private int power;

    private boolean mQuitFlag = false;
    private boolean Send_status = true;
    private String videoUrl;
    private String controlUrl;
    private int port;
    private String forwardMsg = null;
    private String backMsg = null;
    private String rightMsg = null;
    private String leftMsg = null;
    private String stopMsg = null;
    private String speedHigh = null;
    private String speedLow = null;
    private String cbx_DisplayMode;
    private String purifierOn = null;
    private String purifierOff = null;

    private ImageButton radar;
    private ImageButton mainToSetting;
    private ImageButton mainToLogin;
    private Switch purifier;
    private BatteryView charged_batteryView, charging_batteryView;

    private CheckBox speed;
    private NavController navController;


    private RadioGroup group = null;


    int cmdStyle = 0;
    private float x = 0;
    private float y = 0;

    private final int MSG_ID_ERR_RECEIVE = 1003;
    private final int MSG_ID_CON_READ = 1004;

    private Context mContext;
    private Thread mThreadClient = null;
    private boolean mThreadFlag = false;
    private Handler stateHandler;

    private Handler mmHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    charging_batteryView.setPower(power += 5);
                    if (power == 100) {
                        power = 0;
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private String DEFULT_PRES = "0";
    public static final String PREFS_NAME = "WIFI-Robot";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        mContext = this;
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐去标题
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);// 强制横屏显示
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//强制全屏
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//保持屏幕常量
        getWindow().getDecorView().setBackgroundResource(R.color.begin);
        startMainPage();
        setContentView(R.layout.main);

        navController = findViewById(R.id.control_view);
        mainToLogin = findViewById(R.id.maintologin);
        mainToSetting = findViewById(R.id.maintosetting);
        mjpegView = findViewById(R.id.view3D);
        speed = findViewById(R.id.speed);
        radar = findViewById(R.id.radar);
        purifier = findViewById(R.id.purifier);
        charged_batteryView = findViewById(R.id.charged_battery);
        charging_batteryView = findViewById(R.id.charging_battery);
        charging_batteryView.setVisibility(View.GONE);
        charged_batteryView.setVisibility(View.GONE);
        onShow();
        connectToRouter();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                mmHandler.sendEmptyMessage(0);
            }
        }, 0, 100);


        //高低速开关
        speed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sendCommand(speedHigh);
                } else if (!isChecked) {
                    sendCommand(speedLow);
                }
            }
        });

        //净化器开关
        purifier.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if ((isChecked)) {
                    sendCommand(purifierOn);
                } else {
                    sendCommand(purifierOff);
                }
            }
        });

		if (mis == null){
			startActivity(new Intent(MainActivity.this, NoSignalActivity.class));
			removeActivity();
		}

        mainToSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Config.class));
                mainToSetting.setClickable(false);
            }
        });


        mainToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LoginAcivity.class));
                mainToLogin.setClickable(false);
                removeActivity();
            }
        });
        /**
         * 轮盘控制
         */
        navController.setOnNavAndSpeedListener(new NavController.OnNavAndSpeedListener() {
            @Override
            public void onNavAndSpeed(float nav, float speed, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        final float na = nav;
                        Log.i("navController", String.valueOf(nav));
                        Timer timer = new Timer();// 实例化Timer类
                        timer.schedule(new TimerTask() {
                            public void run() {
                                sendMessage(na);
                            }
                        }, 200);// 这里百毫秒
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onStop(float nav, float speed, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        Timer timer = new Timer();// 实例化Timer类
                        timer.schedule(new TimerTask() {
                            public void run() {
                                sendCommand(stopMsg);
                            }
                        }, 300);

                    default:
                        break;
                }
            }
        });

        /**
         * 触屏事件
         */
        mjpegView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                switch (event.getAction()) {
                    //当触摸的时候
                    case MotionEvent.ACTION_MOVE:
                        if (cbx_DisplayMode.contains("true"))
                            Display("ACTION_MOVE", event);
                }
                return true;
            }
        });

    }

    /**
     * 视屏云台
     *
     * @param eventType
     * @param event
     */
    public void Display(String eventType, MotionEvent event) {

        int x = (int) event.getX();
        int y = (int) event.getY();
        int width = this.getWindowManager().getDefaultDisplay().getWidth();
        int height = this.getWindowManager().getDefaultDisplay().getHeight();
        int yuntai_x = (int) map(x, 0, width, 180, 1);
        int yuntai_y = (int) map(y, 0, height, 1, 180);
        String Gear7_data = null;
        if (Integer.toHexString(yuntai_x).length() <= 1) {
            Gear7_data = "FF01070" + Integer.toHexString(yuntai_x) + "FF";
            sendCommand(Gear7_data);
        } else {
            Gear7_data = "FF0107" + Integer.toHexString(yuntai_x) + "FF";
            sendCommand(Gear7_data);
        }

        String Gear8_data = null;
        if (Integer.toHexString(yuntai_y).length() <= 1) {
            Gear8_data = "FF01080" + Integer.toHexString(yuntai_y) + "FF";
            sendCommand(Gear8_data);
        } else {
            Gear8_data = "FF0108" + Integer.toHexString(yuntai_y) + "FF";
            sendCommand(Gear8_data);
        }
    }

    /**
     * map函数
     */
    long map(long x, long in_min, long in_max, long out_min, long out_max) {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }

    /**
     * 连接路由器
     */
    private void connectToRouter() {
        // TODO Auto-generated method stub
        mThreadFlag = true;
        mThreadClient = new Thread(mRunnable);
        mThreadClient.start();
        mis.read(videoUrl);
        mis = MjpegInputStream.getInstance();
        mjpegView.setSource(mis);

    }

    private Runnable mRunnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            BufferedInputStream is = null;
            try {
                initWifiConnection();
                Log.d("buffer", "getInputStream");
                is = new BufferedInputStream(socketClient.getInputStream());

                byte[] buffer = new byte[11];
                byte[] command = new byte[11];
                byte[] compare = new byte[7];
                int sum = 0;
                int dpr = 0;
                while (mThreadFlag) {
                    try {
                        int ret = is.read(buffer);
                        Log.i("buffer", String.valueOf(buffer) + ret);
                        Message msg = new Message();
                        msg.what = MSG_ID_CON_READ;
                        for (int i = 0; i < ret; i++) {
                            command[dpr + i] = buffer[i];
                            sum++;
                        }
                        dpr = dpr + ret;
                        if (sum == 7) {
                            sum = 0;
                            dpr = 0;
                            msg.obj = command;
                            byte cmd0 = command[0];
                            byte cmd1 = command[1];
                            byte cmd2 = command[2];
                            byte cmd3 = command[3];
                            byte cmd4 = command[4];
                            byte cmd5 = command[5];
                            byte cmd6 = command[6];
                            int cmd61 = cmd6 & 0xff;
                            int cmdTotal1 = 0;
                            int cmdTotal = cmd0 + cmd1 + cmd2 + cmd3 + cmd4 + cmd5;
                            if (cmdTotal >255){
                                cmdTotal1 = cmdTotal-256;
                            }else {
                                cmdTotal1 = cmdTotal;
                            }
                            Log.i("cmd", String.valueOf(cmd61));
                            Log.i("cmd", Integer.toHexString(cmd0));
                            Log.i("cmd", Integer.toHexString(cmd1));
                            if (cmd61 == cmdTotal1) {
                                Log.i("callback", "成功");
                                int left = 0, mid = 0, right = 0, fallLeft, fallRight = 0;
                                right = cmd5 >> 0 & 1;
                                mid = cmd5 >> 1 & 1;
                                left = cmd5 >> 2 & 1;
                                fallRight = cmd5 >> 3 & 1;
                                fallLeft = cmd5 >> 4 & 1;
                                compare[0] = (byte) right;
                                compare[1] = (byte) mid;
                                compare[2] = (byte) left;
                                compare[3] = (byte) fallRight;
                                compare[4] = (byte) fallLeft;
                                /**
                                 * 出现障碍物情况A33 + 1= 7
                                 */
                                if (compare[0] == 0 & compare[1] == 0 & compare[2] == 0)
                                {
                                    command[0] = -1;
                                }else if (compare[0] == 1 & compare[1] == 0 & compare[2] == 0) {
                                    command[0] = 0;

                                } else if (compare[0] == 0 & compare[1] == 1 & compare[2] == 0) {
                                    command[0] = 1;

                                } else if (compare[0] == 0 & compare[1] == 0 & compare[2] == 1) {
                                    command[0] = 2;

                                } else if (compare[0] == 1 & compare[1] == 1 & compare[2] == 0) {
                                    command[0] = 3;
                                } else if (compare[0] == 1 & compare[1] == 0 & compare[2] == 1) {
                                    command[0] = 4;
                                } else if (compare[0] == 0 & compare[1] == 1 & compare[2] == 1) {
                                    command[0] = 5;

                                } else if (compare[0] == 1 & compare[1] == 1 & compare[2] == 1) {
                                    command[0] = 6;
                                }
                                if (compare[4] == 0 & compare[3] == 1) {
                                    command[0] = 7;

                                } else if (compare[4] == 1 & compare[3] == 0) {
                                    command[0] = 8;

                                } else if (compare[4] == 1 & compare[3] == 1) {
                                    command[0] = 9;

                                }
                                msg.obj = command;
                            } else {
                                command[0] = -2;
                                msg.obj = command;
                            }
                            mHandler.sendMessage(msg);
                        } else if (sum == 11) {
                            sum = 0;
                            dpr = 0;
                            msg.obj = command;
                            mHandler11.sendMessage(msg);
                        }
                    } catch (Exception e) {
                        Message msg = new Message();
                        msg.what = MSG_ID_ERR_RECEIVE;
                        mHandler.sendMessage(msg);
                    }
                }
                is.close();
            } catch (Exception e) {
                e.getStackTrace();
            }
        }

        Handler mHandler = new Handler() {
            public void handleMessage(Message msg) {
                Log.i("Main", "handle internal Message, id=" + msg.what);
                switch (msg.what) {
                    case MSG_ID_ERR_RECEIVE:
                        break;
                    case MSG_ID_CON_READ:
                        byte[] command = (byte[]) msg.obj;
//					  Log.i("Main", "handle internal Message, id="+command.toString());
                        handleCallback(command);
                        break;
                    default:
                        break;
                }
                super.handleMessage(msg);
            }


        };

        Handler mHandler11 = new Handler() {
            public void handleMessage(Message msg) {
                Log.i("Main", "handle internal Message, id=" + msg.what);
                switch (msg.what) {
                    case MSG_ID_ERR_RECEIVE:
                        break;
                    case MSG_ID_CON_READ:
                        byte[] command = (byte[]) msg.obj;
                        Log.i("Main", "handle internal Message, id=" + command.toString());
                        handleCallback11(command);
                        break;
                    default:
                        break;
                }
                super.handleMessage(msg);
            }
        };

        private void handleCallback11(byte[] command) {
            // TODO Auto-generated method stub
            if (command == null) {
                return;
            }
            Log.i("handleback", String.valueOf(command[1]));
            byte cmd0 = command[0];
            byte cmd1 = command[1];
            byte cmd2 = command[2];
            byte cmd3 = command[3];
            byte cmd4 = command[4];
            byte cmd5 = command[5];
            byte cmd6 = command[6];
            byte cmd7 = command[7];
            byte cmd8 = command[8];
            byte cmd9 = command[9];
            byte cmd10 = command[10];
            int cmd100 = cmd10 & 0xff;
            Log.i("cmd", String.valueOf(cmd100));
            Log.i("cmd", Integer.toHexString(cmd0));
            Log.i("cmd", Integer.toHexString(cmd1));
            int total = 0, total1 = 0;
            total = cmd0 + cmd1 + cmd2 + cmd3 + cmd4 + cmd5 + cmd6 + cmd7 + cmd8 + cmd9;
            if (total > 255) {
                total1 = total - 256;
            }
            if (cmd100 == total1) {
                Log.i("callback", "成功");
                /**
                 *  工作状态,速度状态,充电状态,电量信息,故障代码
                 */
                int workState = 0, speedState = 0, rechargeState = 0, electState, malFunction = 0;
                workState = cmd5;
                speedState = cmd6;
                rechargeState = cmd7;
                electState = cmd8;
                malFunction = cmd9;
                Log.d("state", workState + "/" + speedState + "/" + rechargeState + "/" + electState + "/" + malFunction);
                String xianshi = null, workStateString = null, speedStateString = null, rechargeStateString = null, malFunctionString = null;
                if (workState == 0) {
                    workStateString = "处于待机状态,";
                } else if (workState == 1) {
                    workStateString = "处于工作状态,";
                } else if (workState == 2) {
                    workStateString = "发生了故障,";
                }
                if (speedState == 0) {
                    speedStateString = "低速模式,";
                } else if (speedState == 1) {
                    speedStateString = "高速模式,";
                }
                if (rechargeState == 0) {
                    charged_batteryView.setVisibility(View.VISIBLE);
                    charged_batteryView.setPower(electState);
                    charging_batteryView.setVisibility(View.GONE);
                    rechargeStateString = "未充电状态,";
                } else if (rechargeState == 1) {
                    charging_batteryView.setVisibility(View.VISIBLE);
                    charged_batteryView.setVisibility(View.GONE);
                    rechargeStateString = "充电中,";
                }
                if (malFunction == 0) {
                    malFunctionString = "无故障";
                } else if (malFunction == 3) {
                    malFunctionString = "电量不足";
                }
                xianshi = workStateString + speedStateString + rechargeStateString + malFunctionString;
                show_Toast_Long(xianshi);
            } else {
                Toast.makeText(MainActivity.this, "数据接收错误！请检查....", Toast.LENGTH_SHORT).show();
            }
        }

        private void handleCallback(byte[] command) {
            // TODO Auto-generated method stub
            switch (command[0]) {
                case -2:
                    Toast.makeText(MainActivity.this, "数据接收错误！请检查....", Toast.LENGTH_SHORT).show();
                case -1:
                    radar.setBackgroundResource(R.drawable.all_off);//右侧出现障碍物
                    break;
                case 0:
                    radar.setBackgroundResource(R.drawable.right_on);//右侧出现障碍物
//                    initState();
                    break;
                case 1:
                    radar.setBackgroundResource(R.drawable.middle_on);//正前方出现障碍物
//                    initState();
                    break;
                case 2:
                    radar.setBackgroundResource(R.drawable.left_on);//左侧出现障碍物
//                    initState();
                    break;
                case 3:
                    radar.setBackgroundResource(R.drawable.left_off);//正前方和左侧出现障碍物
//                    initState();
                    break;
                case 4:
                    radar.setBackgroundResource(R.drawable.middle_off);//右侧和左侧出现障碍物
//                    initState();
                    break;
                case 5:
                    radar.setBackgroundResource(R.drawable.right_off);//正前方和左侧出现障碍物
//                    initState();
                    break;
                case 6:
                    radar.setBackgroundResource(R.drawable.all_light);//四周出现障碍物
//                    initState();
                    break;
                case 7:
                    show_Toast("小心右侧坠落！！");
                    break;
                case 8:
                    show_Toast("小心左侧坠落！！");
                    break;
                case 9:
                    show_Toast("小心坠落！！");
                    break;
                default:
                    break;
            }

        }

        private void initWifiConnection() {
            // TODO Auto-generated method stub
            Log.i("Socket", "initWifiConnection");
            try {
                if (socketClient != null) {
                    socketClient.closeSocket();
                }
                socketClient = new SocketClient(controlUrl, port);
            } catch (Exception e) {
                Log.d("Socket", "initWifiConnection return exception! ");
            }
        }
    };

    /**
     * 发送指令
     */
    private void sendCommand(String data) {
        if (null == socketClient) {
            return;
        }
        try {
            socketClient.sendMsg(HexStringToByte(data));
        } catch (Exception e) {
            Log.i("Socket", e.getMessage() != null ? e.getMessage().toString() : "sendCommand error!");
            Toast.makeText(mContext, "发送消息给路由器失败  ：" + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 十六进制字符串转Byte数组
     */
    private byte[] HexStringToByte(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] returnByte = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            returnByte[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return returnByte;
    }

    private byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }


    private void onShow() {
        // TODO Auto-generated method stub
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String mPreferences = null;

        mPreferences = settings.getString("VideoUrl", DEFULT_PRES);
        videoUrl = mPreferences;

        mPreferences = settings.getString("ControlUrl", DEFULT_PRES);
        controlUrl = mPreferences;

        mPreferences = settings.getString("port", DEFULT_PRES);
        port = Integer.parseInt(mPreferences);

        mPreferences = settings.getString("Forward", DEFULT_PRES);
        forwardMsg = mPreferences;

        mPreferences = settings.getString("Back", DEFULT_PRES);
        backMsg = mPreferences;

        mPreferences = settings.getString("Left", DEFULT_PRES);
        leftMsg = mPreferences;

        mPreferences = settings.getString("Right", DEFULT_PRES);
        rightMsg = mPreferences;

        mPreferences = settings.getString("Stop", DEFULT_PRES);
        stopMsg = mPreferences;

        mPreferences = settings.getString("HighSpeed", DEFULT_PRES);
        speedHigh = mPreferences;

        mPreferences = settings.getString("LowSpeed", DEFULT_PRES);
        speedLow = mPreferences;

        mPreferences = settings.getString("PurifierOn", DEFULT_PRES);
        purifierOn = mPreferences;

        mPreferences = settings.getString("PurifierOff", DEFULT_PRES);
        purifierOff = mPreferences;

        mPreferences = settings.getString("cbx_DisplayMode", DEFULT_PRES);
        cbx_DisplayMode = mPreferences;

    }

    /**
     * 按两次退回键退出应用
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            long secondTime = System.currentTimeMillis();
            if (secondTime - firstTime > 1500) {
                Toast.makeText(MainActivity.this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                firstTime = secondTime;
                return true;
            } else {
                removeAllActivity();
            }
        }

        return super.onKeyUp(keyCode, event);
    }


    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        if (mjpegView != null) {
            mjpegView.stopPlayback();
        }
        if (null != socketClient) {
            try {
                socketClient.closeSocket();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mThreadFlag = false;
            mThreadClient.interrupt();
        }
        super.onDestroy();
    }

    public void startMainPage() {
        Intent intent = getIntent();
        String name = intent.getStringExtra("begin");
        Log.i("MainActivity", "name:" + name);
        if ("1" == name) {
            getWindow().getDecorView().setBackgroundResource(R.color.main_bg);
        }
    }

    public void sendMessage(float number) {
        if (number >= 45.0 && number <= 135.0) {
            sendCommand(forwardMsg);
        } else if (number > 135.0 & number < 225.0) {
            sendCommand(leftMsg);
        } else if (number >= 225.0 && number <= 315.0) {
            sendCommand(backMsg);
        } else if (number >= 0 && number < 45.0 || number > 315.0 && number <= 360.0) {
            sendCommand(rightMsg);
        }
    }

    public void initState() {
        stateHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0:
                        radar.setBackgroundResource(R.drawable.all_off);
                        break;
                    default:
                        break;
                }
            }
        };
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                stateHandler.sendEmptyMessage(0);
            }
        }, 0, 500);
    }
}
