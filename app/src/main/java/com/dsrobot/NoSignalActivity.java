package com.dsrobot;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.dsrobot.utils.BaseActivity;

/**
 * Created by Yang on 2018/3/19.
 */

public class NoSignalActivity extends BaseActivity{

    private long firstTime = 0;

    private ConstraintLayout constraintLayout;
    private ImageView iv_before;
    private ImageButton mainToLogin_noSignal;
    private ImageButton mainToSetting_noSignal;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);// 强制横屏显示
        setContentView(R.layout.nosignal);

        constraintLayout = findViewById(R.id.noSignal);
        mainToLogin_noSignal = findViewById(R.id.maintologin_nosignal);
        mainToSetting_noSignal = findViewById(R.id.maintosetting_nosignal);
        iv_before = findViewById(R.id.iv_before_main);
        startAnimation();

        mainToSetting_noSignal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NoSignalActivity.this, Config.class));
                mainToLogin_noSignal.setClickable(false);
                removeActivity();
            }
        });

        mainToLogin_noSignal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NoSignalActivity.this, LoginAcivity.class));
                mainToLogin_noSignal.setClickable(false);
                removeActivity();
            }
        });
    }

    private void startAnimation() {

        @SuppressLint("ObjectAnimatorBinding") ObjectAnimator behindAnim = ObjectAnimator
                .ofInt(iv_before, "translationY", 0, 100, 0)
                .setDuration(4000);

        behindAnim.setRepeatCount(ValueAnimator.INFINITE);
        behindAnim.start();
        behindAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer cVal = (Integer) animation.getAnimatedValue();
                iv_before.layout(constraintLayout.getWidth()/2-iv_before.getWidth()/2,constraintLayout.getHeight()/2-iv_before.getHeight()/2-cVal, constraintLayout.getWidth()/2+iv_before.getWidth()/2, constraintLayout.getHeight()/2+iv_before.getHeight()/2-cVal);

            }
        });
    }

    /**
     * 按两次退回键退出应用
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            long secondTime = System.currentTimeMillis();
            if (secondTime - firstTime > 1500) {
                Toast.makeText(NoSignalActivity.this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                firstTime = secondTime;
                return true;
            } else {
                removeAllActivity();
            }
        }

        return super.onKeyUp(keyCode, event);
    }
}
