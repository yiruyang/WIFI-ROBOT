package com.dsrobot.utils;

import android.app.Activity;
import android.app.Application;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/3/16.
 */

public class MyApplication extends Application {

    private List<Activity> allActivity;

    @Override
    public void onCreate() {
        super.onCreate();

        allActivity = new ArrayList<Activity>();
    }

    public void addActivity(Activity activity){
        /**
         * 判断当前list是否存在此活动
         */
        if (!allActivity.contains(activity)){
            allActivity.add(activity);
        }
    }

    /**
     * 从集合中移除活动
     * 销毁当前活动
     * @param activity
     */
    public void removeActivity(Activity activity){
        if (allActivity.contains(activity)){
            allActivity.remove(activity);
            activity.finish();
        }
    }

    /**
     * 通过遍历销毁当前所有活动
     */
    public void removeAllActivity(){
        for (Activity activity:allActivity){
            activity.finish();
        }
    }


    public List<Activity> getActivityList(){
        return this.allActivity;
    }

}
