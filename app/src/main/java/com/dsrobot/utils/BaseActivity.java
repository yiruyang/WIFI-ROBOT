package com.dsrobot.utils;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

/**
 * Created by Administrator on 2018/3/16.
 */

public class BaseActivity extends Activity {

    private MyApplication application;

    private BaseActivity allContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (application == null){
            application = (MyApplication) getApplication();
        }

        allContext = this;
        addActivity();
    }

    /**
     * 添加活动
     */
    public void addActivity(){

        application.addActivity(allContext);
    }

    /**
     * 移除活动
     */
    public void removeActivity(){
         application.removeActivity(allContext);
    }

    /**
     * 移除全部活动
     */
    public void removeAllActivity(){

        application.removeAllActivity();//用application中的方法销毁所有活动
    }

    /**
     * Toast定义成一个方法,可以重复使用
     * @param text
     */
    public void show_Toast(String text){
        Toast.makeText(allContext, text, Toast.LENGTH_SHORT).show();
    }

    public void show_Toast_Long(String text){
        Toast.makeText(allContext, text, Toast.LENGTH_LONG).show();
    }
}
