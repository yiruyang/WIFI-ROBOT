package com.dsrobot;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.dsrobot.utils.BaseActivity;
import com.dsrobot.utils.NoScrollViewPager;

import java.io.BufferedInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";

    private MjpegInputStream mis = null;
    private SocketClient socketClient = null;
    private long firstTime = 0;
    private int power;

    private String videoUrl;
    private String controlUrl;
    private int port;
    private String forwardMsg = null;
    private String backMsg = null;
    private String rightMsg = null;
    private String leftMsg = null;
    private String stopMsg = null;
    private String stopSuddenly = null;
    private String speedHigh = null;
    private String speedLow = null;
    private String cbx_DisplayMode;
    private String purifierOn = null;
    private String purifierOff = null;

    //每一个界面
    private List<View> viewList;
    private TabLayout.Tab one, two, three, four;

    @BindView(R.id.radar)
    ImageButton radar;
    @BindView(R.id.maintosetting)
    ImageButton mainToSetting;
    @BindView(R.id.maintologin)
    ImageButton mainToLogin;
    @BindView(R.id.view3D)
    MjpegView mjpegView;
    @BindView(R.id.purifier)
    Switch purifier;
    @BindView(R.id.haveSignal)
    RelativeLayout haveSignal;
    @BindView(R.id.humidity)
    TextView mHumidity;
    @BindView(R.id.temperature)
    TextView mTemperature;
    @BindView(R.id.charged_battery)
    BatteryView charged_batteryView;
    @BindView(R.id.charging_battery)
    BatteryView charging_batteryView;

    private NoScrollViewPager viewPager;
    private TabLayout tabLayout;
    private CheckBox speed;
    private NavController navController;
    private ImageButton scram;
    private TextView room_bulk;
    private Button start_sterilize, finish_sterilize;

    private final int MSG_ID_ERR_RECEIVE = 1003;
    private final int MSG_ID_CON_READ = 1004;
    private String DEFULT_PRES = "0";
    public static final String PREFS_NAME = "WIFI-Robot";

    private Context mContext;
    private Thread mThreadClient = null;
    private Thread mThread = null;
    private Thread navThread = null;
    private boolean mThreadFlag = false;
    private boolean mmThreadFlag = false;
    private boolean mMis = false;
    private Handler stateHandler;
    private float angle = -1; // -1 表示没有意义的值  做判断使用


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        mContext = this;
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐去标题
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);// 强制竖屏显示
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//保持屏幕常量
        getWindow().getDecorView().setBackgroundResource(R.color.begin);
        setContentView(R.layout.main);

        initListView();
        initLayout();
        onShow();
        connectToRouter();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                mmHandler.sendEmptyMessage(0);
            }
        }, 0, 100);

        navThread = new Thread(new Runnable() {
            @Override
            public void run() {

                while (mmThreadFlag) {
                    if (angle != -1) {
                        sendMessage(angle);
                    }
                    try {
                        navThread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


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

        //开始消毒
        start_sterilize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CharSequence c = room_bulk.getText();
                int number = 0;
                if (TextUtils.isEmpty(c)) {
                    show_Toast("请输入参数");
                } else {
                    number = Integer.valueOf(String.valueOf(c));
                    if (800 < number) {
                        show_Toast("超出最大消毒范围");
                    } else {
                        if (number > 256) {
                            sendCommand(endHaveData(0x53, 0x4B, 0x08, 0x00, 0x0C, number / 256, number % 256));
                        } else {
                            sendCommand(endHaveData(0x53, 0x4B, 0x08, 0x00, 0x0C, 0x00, number));
                        }
                        show_Toast("消毒开始！");
                    }
                }

            }
        });

        finish_sterilize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand(purifierOff);
                show_Toast("消毒停止");
            }
        });

        //急停按钮
        scram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("senddata", stopSuddenly);
                sendCommand(stopSuddenly);
            }
        });

        //净化器开关
        purifier.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if ((isChecked)) {
                    sendCommand(purifierOn);
                    show_Toast("消毒机已经打开");
                } else {
                    sendCommand(purifierOff);
                    show_Toast("消毒机已经关闭");
                }
            }
        });


        //跳转到设置界面
        mainToSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Config.class));
                mainToSetting.setClickable(false);
                removeActivity();
            }
        });

        //跳转到登录界面
        mainToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LoginAcivity.class));
                mainToLogin.setClickable(false);
                removeActivity();
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
        mThread = new Thread(runnable);
        mThread.start();
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            mis.read(videoUrl);
            mis = MjpegInputStream.getInstance();
            if (mis == null) {
                Message me = new Message();
                mMis = false;
                me.obj = mMis;
                mHandlerVideo.sendMessage(me);
            } else {
                mjpegView.setSource(mis);
            }
        }
    };

    private Runnable mRunnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            BufferedInputStream is = null;
            try {
                initWifiConnection();
                is = new BufferedInputStream(socketClient.getInputStream());
                byte[] buffer = new byte[255];
                byte[] command = new byte[255];
                byte[] compare = new byte[255];
                int sum = 0;
                int dpr = 0;
                int cop = 0;
                while (mThreadFlag) {
                    try {
                        int ret = is.read(buffer);
                        Message msg = new Message();
                        msg.what = MSG_ID_CON_READ;
                        cop = buffer[2];
                        for (int i = 0; i < cop; i++) {
                            command[i] = buffer[i];
                            sum++;
                        }
                        if (sum == 7) {
                            sum = 0;
                            dpr = 0;
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
                            if (cmdTotal > 255) {
                                cmdTotal1 = cmdTotal - 256;
                            } else {
                                cmdTotal1 = cmdTotal;
                            }
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
                                if (compare[0] == 0 & compare[1] == 0 & compare[2] == 0) {
                                    command[0] = -1;
                                } else if (compare[0] == 1 & compare[1] == 0 & compare[2] == 0) {
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
                            } else {
                                command[0] = -2;
                            }
                            msg.obj = command;
                            mHandler.sendMessage(msg);
                            command = new byte[255];
                            buffer = new byte[255];
                        } else if (sum == 10) {
                            sum = 0;
                            dpr = 0;
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
                            int cmd90 = cmd9 & 0xff;
                            int total = 0, total1 = 0;
                            total = cmd0 + cmd1 + cmd2 + cmd3 + cmd4 + cmd5 + cmd6 + cmd7 + cmd8;
                            if (total > 255) {
                                total1 = total - 256;
                            } else {
                                total1 = total;
                            }
                            if (cmd90 == total1) {
                                Log.i("callback", "成功");
                                /**
                                 *  工作状态,速度状态,充电状态,电量信息,故障代码
                                 */
                                command[1] = cmd5;
                                command[2] = cmd6;
                                command[3] = cmd7;
                                command[4] = cmd8;
                            } else {
                                command[0] = 1;
                            }
                            msg.obj = command;
                            mHandler10.sendMessage(msg);
                            command = new byte[255];
                            buffer = new byte[255];
                        } else if (sum == 11) {
                            sum = 0;
                            dpr = 0;
                            msg.obj = command;
                            mHandler11.sendMessage(msg);
                            command = new byte[255];
                            buffer = new byte[255];
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
                switch (msg.what) {
                    case MSG_ID_ERR_RECEIVE:
                        break;
                    case MSG_ID_CON_READ:
                        byte[] command = (byte[]) msg.obj;
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
                        handleCallback11(command);
                        break;
                    default:
                        break;
                }
                super.handleMessage(msg);
            }
        };

        Handler mHandler10 = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_ID_ERR_RECEIVE:
                        break;
                    case MSG_ID_CON_READ:
                        byte[] command = (byte[]) msg.obj;
                        handleCallback10(command);
                        break;
                    default:
                        break;
                }
                super.handleMessage(msg);
            }
        };

        private void handleCallback10(byte[] command) {
            // TODO Auto-generated method stub
            if (command == null) {
                return;
            }
            int a = command[1];
            int b = command[2];
            int c = command[3];
            int d = command[4];
            BigDecimal all = new BigDecimal("0.1");
            if (command[0] == 1) {
                for (int i = 0; i < command.length; i++) {
                    Log.d("datawenshidu", String.valueOf(command[i]));
                }
                Toast.makeText(MainActivity.this, "温湿度校验码错误！请检查....", Toast.LENGTH_SHORT).show();
            } else {
                if (a < 0 & b > 0) {
                    BigDecimal end = BigDecimal.valueOf(a * 256 + b);
                    mHumidity.setText(end.multiply(all) + "%RH");
                } else if (a < 0 & b < 0) {
                    BigDecimal end = BigDecimal.valueOf((b + 256) + (a * 256));
                    mHumidity.setText(end.multiply(all) + "%RH");
                } else if (a > 0 & b < 0) {
                    BigDecimal end = BigDecimal.valueOf((b + 256) + (a * 256));
                    mHumidity.setText(end.multiply(all) + "%RH");
                } else {
                    mHumidity.setText((a * 256 + b) * 0.1 + "%RH");
                }
                if (c < 0 & d > 0) {
                    BigDecimal end = BigDecimal.valueOf(c * 256 + d);
                    mTemperature.setText(end.multiply(all) + "℃");
                } else if (c < 0 & d < 0) {
                    BigDecimal end = BigDecimal.valueOf((d + 256) + (c * 256));
                    mTemperature.setText(end.multiply(all) + "℃");
                } else if (c > 0 & d < 0) {
                    BigDecimal end = BigDecimal.valueOf((d + 256) + (c * 256));
                    mTemperature.setText(end.multiply(all) + "℃");
                } else {
                    mTemperature.setText((d + c * 256) * 0.1 + "℃");
                }
            }
        }

        private void handleCallback11(byte[] command) {
            // TODO Auto-generated method stub
            if (command == null) {
                return;
            }
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
            int total = 0, total1 = 0;
            total = cmd0 + cmd1 + cmd2 + cmd3 + cmd4 + cmd5 + cmd6 + cmd7 + cmd8 + cmd9;
            if (total > 255) {
                total1 = total - 256;
            } else {
                total1 = total;
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
                String xianshi = null, workStateString = null, speedStateString = null, rechargeStateString = null, malFunctionString = null;
                if (workState == 0) {
                    workStateString = "处于待机状态,";
                } else if (workState == 1) {
                    workStateString = "处于工作状态,";
                } else if (workState == 2) {
                    workStateString = "发生了故障,";
                } else {
                    workStateString = "";
                }
                if (speedState == 0) {
                    speedStateString = "低速模式,";
                } else if (speedState == 1) {
                    speedStateString = "高速模式,";
                } else {
                    speedStateString = "";
                }
                if (rechargeState == 0) {
                    charged_batteryView.setVisibility(View.VISIBLE);
                    if (electState < 0 | electState > 100) {
                        return;
                    } else {
                        charged_batteryView.setPower(electState);
                        charging_batteryView.setVisibility(View.GONE);
                        rechargeStateString = "未充电状态,";
                    }
                } else if (rechargeState == 1) {
                    charging_batteryView.setVisibility(View.VISIBLE);
                    charged_batteryView.setVisibility(View.GONE);
                    rechargeStateString = "充电中,";
                } else {
                    rechargeStateString = "";
                }
                if (malFunction == 0) {
                    malFunctionString = "无故障";
                } else if (malFunction == 3) {
                    malFunctionString = "电量不足";
                } else {
                    malFunctionString = "";
                }
                xianshi = workStateString + speedStateString + rechargeStateString + malFunctionString;
                show_Toast_Long(xianshi);
            } else {
                for (int i = 0; i < command.length; i++) {
                    Log.d("datastate", String.valueOf(command[i]));
                }
                Toast.makeText(MainActivity.this, "小车状态校验码错误！请检查....", Toast.LENGTH_SHORT).show();
            }
        }

        private void handleCallback(byte[] command) {
            // TODO Auto-generated method stub
            switch (command[0]) {
                case -2:
                    for (int i = 0; i < command.length; i++) {
                        Log.d("databizhang", String.valueOf(command[i]));
                    }
                    Toast.makeText(MainActivity.this, "避障校验码错误！请检查....", Toast.LENGTH_SHORT).show();
                case -1:
                    radar.setBackgroundResource(R.drawable.all_off);//无障碍物
                    break;
                case 0:
                    radar.setBackgroundResource(R.drawable.right_on);//右侧出现障碍物
                    break;
                case 1:
                    radar.setBackgroundResource(R.drawable.middle_on);//正前方出现障碍物
                    break;
                case 2:
                    radar.setBackgroundResource(R.drawable.left_on);//左侧出现障碍物
                    break;
                case 3:
                    radar.setBackgroundResource(R.drawable.left_off);//正前方和左侧出现障碍物
                    break;
                case 4:
                    radar.setBackgroundResource(R.drawable.middle_off);//右侧和左侧出现障碍物
                    break;
                case 5:
                    radar.setBackgroundResource(R.drawable.right_off);//正前方和左侧出现障碍物
                    break;
                case 6:
                    radar.setBackgroundResource(R.drawable.all_light);//四周出现障碍物
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
    };


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

    private void initWifiConnection() {
        // TODO Auto-generated method stub
        try {
            if (socketClient != null) {
                socketClient.closeSocket();
            }
            socketClient = new SocketClient(controlUrl, port);
        } catch (Exception e) {
            Log.d("Socket", "initWifiConnection return exception! ");
        }
    }

    Handler mHandlerVideo = new Handler() {
        public void handleMessage(Message msg) {
            boolean b = (boolean) msg.obj;
            if (!b) {
                mjpegView.setBackgroundResource(R.drawable.nosignalbg);
                radar.setVisibility(View.GONE);
            }
            super.handleMessage(msg);
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

        mPreferences = settings.getString("StopSuddenly", DEFULT_PRES);
        stopSuddenly = mPreferences;

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
            mThread.interrupt();
        }
        super.onDestroy();
    }


    /**
     * 轮盘的角度转化为发送的信息
     *
     * @param number
     */
    public void sendMessage(float number) {
        if (number == 45.0 || number > 45.0 && number <= 135.0 || number == 135.0) {
            sendCommand(forwardMsg);
        } else if (number > 135.0 && number < 225.0) {
            sendCommand(leftMsg);
        } else if (number == 225.0 || number >= 225.0 && number <= 315.0 || number == 315.0) {
            sendCommand(backMsg);
        } else if (number == 0 || number > 0 && number < 45.0 || number > 315.0 && number < 360.0 || number == 360.0) {
            sendCommand(rightMsg);
        }
    }

    public void initLayout() {
        ButterKnife.bind(this);
        charging_batteryView.setVisibility(View.GONE);
        charged_batteryView.setVisibility(View.GONE);
        //给ViewPager设置适配器
        PagerAdapter adapter = new PagerAdapter() {

            @Override
            public int getCount() {
                return viewList.size();
            }


            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView(viewList.get(position));
            }

            //对显示的资源进行初始化
            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                container.addView(viewList.get(position));
                return viewList.get(position);
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }
        };

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:

                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        viewPager.setAdapter(adapter);

        tabLayout.setupWithViewPager(viewPager);

        one = tabLayout.getTabAt(0);
        two = tabLayout.getTabAt(1);
        three = tabLayout.getTabAt(2);
        four = tabLayout.getTabAt(3);

        one.setIcon(R.mipmap.control_checked);
        two.setIcon(R.mipmap.speak);
        three.setIcon(R.mipmap.userinfo);
        four.setIcon(R.mipmap.robot);

        //tab监听事件
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab == one) {
                    tab.setIcon(R.mipmap.control_checked);
                } else if (tab == two) {
                    tab.setIcon(R.mipmap.speak_checked);
                } else if (tab == three) {
                    tab.setIcon(R.mipmap.userinfo_checked);
                } else if (tab == four) {
                    tab.setIcon(R.mipmap.robot_checked);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab == one) {
                    tab.setIcon(R.mipmap.control);
                } else if (tab == two) {
                    tab.setIcon(R.mipmap.speak);
                } else if (tab == three) {
                    tab.setIcon(R.mipmap.userinfo);
                } else if (tab == four) {
                    tab.setIcon(R.mipmap.robot);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        /*
        设置MjpegView的高度为手机屏幕的宽度
         */
        DisplayMetrics dm = getResources().getDisplayMetrics();
        Log.i("ping", dm.widthPixels+"/"+dm.heightPixels);
        int width = dm.heightPixels * 8 / 17;
        ViewGroup.LayoutParams params = haveSignal.getLayoutParams();
        params.height = width;//设置当前控件布局的高度
        haveSignal.setLayoutParams(params);//将设置好的布局参数应用到控件中
    }


    public void initListView() {
        viewList = new ArrayList<>();
        LayoutInflater layoutInflater = getLayoutInflater();
        View layout1 = layoutInflater.inflate(R.layout.layout_1, null);
        View layout2 = layoutInflater.inflate(R.layout.layout_2, null);
        View layout3 = layoutInflater.inflate(R.layout.layout_3, null);
        View layout4 = layoutInflater.inflate(R.layout.layout_4, null);
        //获取子项的控件
        speed = layout1.findViewById(R.id.speed);
        navController = layout1.findViewById(R.id.control_view);
        scram = layout1.findViewById(R.id.scram);
        room_bulk = layout4.findViewById(R.id.room_bulk);
        start_sterilize = layout4.findViewById(R.id.start_sterilize);
        finish_sterilize = layout4.findViewById(R.id.finish_sterilize);

        setNavController();
        viewList.add(layout1);
        viewList.add(layout2);
        viewList.add(layout3);
        viewList.add(layout4);
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
    }

    /**
     * 添加监听
     */
    private void setNavController() {
        /**
         * 轮盘控制
         */
        Log.d(TAG, "setNavController:  time a " + Calendar.getInstance().getTimeInMillis());
        navController.setOnNavAndSpeedListener(new NavController.OnNavAndSpeedListener() {
            @Override
            public void onNavAndSpeed(float nav, float speed, MotionEvent event) {
                angle = nav;
            }

            @Override
            public void onStop(float nav, float speed, MotionEvent event) {
                mmThreadFlag = false;
                Timer timer = new Timer();// 实例化Timer类
                timer.schedule(new TimerTask() {
                    public void run() {
                        sendCommand(stopMsg);
                    }
                }, 300);
            }

            @Override
            public void onStartController() {
                // 控制thread.start
                mmThreadFlag = true;
                navThread.start();
            }
        });
    }

    /**
     * 生成校验码序列
     *
     * @param frameHeaderHigh
     * @param frameHeaderLow
     * @param length
     * @param commandHigh
     * @param commandLow
     * @param data1           数据第一字节
     * @param data2           数据第二个字节
     * @return
     */
    public String endHaveData(Integer frameHeaderHigh, Integer frameHeaderLow, Integer length,
                              Integer commandHigh, Integer commandLow, Integer data1,
                              Integer data2) {
        Integer m = frameHeaderHigh + frameHeaderLow + length
                + commandHigh + commandLow + data1 + data2;
        String mm = Integer.toHexString(m);
        String h = jiaL(frameHeaderHigh) + jiaL(frameHeaderLow) + jiaL(length)
                + jiaL(commandHigh) + jiaL(commandLow) + jiaL(data1) + jiaL(data2);

        if (Integer.toHexString(m).length() > 2) {
            return h + mm.substring(mm.length() - 2);
        }

        return h + mm;
    }

    /**
     * 拼接十六进制字符串
     */
    public String jiaL(Integer test) {
        String t = Integer.toHexString(test);
        if (test < 16) {

            t = "0" + t;
        }
        return t;
    }


}
