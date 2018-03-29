package com.dsrobot;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dsrobot.utils.BaseActivity;

/**
 * Created by Administrator on 2018/3/9.
 */

public class LoginAcivity extends BaseActivity {

    private Integer total;
    private static final String TAG = "LoginAcivity";
    public static final String PREFS_NAME = "WIFI-Robot";
    public static final String USER_NAME = "userName";
    public static final String USER_PASSWORD = "userPassword";
    private boolean mQuitFlag = false;
    //输入用户名
    private EditText et_inputname;
    //输入密码
    private EditText et_inputpwd;
    //登录按钮
    private Button btn_login;

    private SharedPreferences preferences;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);// 强制横屏显示
        setContentView(R.layout.login);


        et_inputname = findViewById(R.id.et_inputname);
        et_inputpwd = findViewById(R.id.et_inputpwd);
        btn_login = findViewById(R.id.btn_login);
        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        //测试专用
        et_inputname.setText("root");
        et_inputpwd.setText("123456");
        onSave();


        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String userName = et_inputname.getText().toString().trim();
                final String passWord = et_inputpwd.getText().toString().trim();
                Log.i("LoginActivity","username:"+userName+"password"+passWord);
                if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(passWord)) {
                    Toast.makeText(LoginAcivity.this, R.string.namepassword_null, Toast.LENGTH_LONG).show();
                }else if (userName.equals("root")  && passWord.equals("123456") ){
                    LoginAcivity.this.startActivity(new Intent(LoginAcivity.this, MainActivity.class));
                    removeActivity();
                }else {
                    Toast.makeText(LoginAcivity.this, R.string.namepassword_incorrect, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: ");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause: ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop: ");
    }


    private void onSave() {
        // TODO Auto-generated method stub

        /**
         * 方向控制
         */
        String forward = endNoData(0x53, 0x4B , 0x06, 0x00, 0x01);
        String back = endNoData(0x53, 0x4B , 0x06, 0x00, 0x02);
        String left = endNoData(0x53, 0x4B , 0x06, 0x00, 0x03);
        String right = endNoData(0x53, 0x4B , 0x06, 0x00, 0x04);
        String stop = endNoData(0x53, 0x4B , 0x06, 0x00, 0x05);

        /**
         * 速度高低模式
         */
        String speedLow = endHaveData(0x53, 0x4B , 0x07, 0x00, 0x06, 0x00);
        String speedHigh = endHaveData(0x53, 0x4B , 0x07, 0x00, 0x06, 0x01);

        /**
         * 净化器开关
         */
        String purifierOn = endHaveData(0x53, 0x4B, 0x07, 0x00, 0x09, 0x00);
        String purifierOff = endHaveData(0x53, 0x4B, 0x07, 0x00, 0x09, 0x01);

        setPreferences("VideoUrl","http://192.168.1.1:8080/?action=stream");

        setPreferences("ControlUrl","192.168.1.1");
        setPreferences("port","2001");
        setPreferences("Forward",forward);
        Log.i("new control", forward+"/"+back+"/"+left+"/"+right+"/"+stop);
        setPreferences("Right",right);
        setPreferences("Back",back);
        setPreferences("Left",left);
        setPreferences("Stop",stop);
        setPreferences("LowSpeed", speedLow);
        setPreferences("HighSpeed", speedHigh);
        setPreferences("cbx_DisplayMode", "");
        setPreferences("PurifierOn", purifierOn);
        setPreferences("PurifierOff", purifierOff);
    }

    private void setPreferences(String Key,String mPreferences){
        // set preference
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Key, mPreferences);
        editor.commit();
    }

    private void removePreferences(String key){
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(key);
        editor.commit();
    }

    public String endNoData(Integer frameHeaderHigh, Integer frameHeaderLow, Integer length,
                      Integer commandHigh, Integer commandLow){
        String mm = Integer.toHexString(frameHeaderHigh+ frameHeaderLow+ length
                + commandHigh+ commandLow);
        return jiaL(frameHeaderHigh)+ jiaL(frameHeaderLow)+ jiaL(length)
                + jiaL(commandHigh)
                + jiaL(commandLow)+mm;
    }

    public String endHaveData(Integer frameHeaderHigh, Integer frameHeaderLow, Integer length,
                              Integer commandHigh, Integer commandLow,Integer data){
        String mm = Integer.toHexString(frameHeaderHigh+ frameHeaderLow+ length
                + commandHigh+ commandLow+ data);
        return jiaL(frameHeaderHigh)+ jiaL(frameHeaderLow)+ jiaL(length)
                + jiaL(commandHigh)
                + jiaL(commandLow)
                + jiaL(data)+ mm;
    }

    public String jiaL(Integer test){

        String t = null;

        if (test<16){

            t = "0"+test;
        }else {
            t = Integer.toHexString(test);
        }
        return t;
    }

}
