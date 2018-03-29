package com.dsrobot;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.dsrobot.utils.BaseActivity;

public class SplashActivity extends BaseActivity {
    private static final String TAG = "MainActivity";
    ImageView iv_behind, iv_before;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        iv_behind = findViewById(R.id.iv_behind);
        iv_before = findViewById(R.id.iv_before);

        startAnimation();
    }

    private void startAnimation() {

        @SuppressLint("ObjectAnimatorBinding") final ObjectAnimator beforeAnim = ObjectAnimator
                .ofFloat(iv_before, "zk", 0, 5)
                .setDuration(10000);
        beforeAnim.start();
        beforeAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float cVal = (Float) animation.getAnimatedValue();
                Log.d(TAG, "onAnimationUpdate: " + cVal);
                if (cVal <= 1) {

                    float percent = cVal / 2;
                    iv_before.setY(-percent * 28);

                    iv_before.setAlpha((float) ((cVal / 2) * 0.4 + 0.6));

                } else if (cVal <= 2) {

                    float percent = cVal / 2;
                    iv_before.setY(-percent * 28);

                    iv_before.setAlpha((float) ((cVal / 2) * 0.4 + 0.6));

                } else if (cVal <= 3) {

                    float percent = (cVal - 2) / 2;
                    iv_before.setY(-28 + percent * 40);

                    iv_before.setAlpha((float) (1 - (cVal - 2) / 2 * 0.4));

                } else if (cVal <= 4) {

                    float percent = (cVal - 2) / 2;
                    iv_before.setY(-28 + percent * 40);

                    float scaleRotate = 1 - (cVal - 3) / 2;
                    iv_before.setScaleX(scaleRotate);
                    iv_before.setScaleY(scaleRotate);

                    iv_before.setAlpha((float) (1 - (cVal - 2) / 2 * 0.4));

                } else if (cVal <= 5) {

                    iv_before.setAlpha((float) ((5 - cVal) * 0.6));

                }

                beforeAnim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        final Intent intent = new Intent();
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setClass(SplashActivity.this, LoginAcivity.class);
                        startActivity(intent);
                        removeActivity();
                    }
                });
            }
        });



        @SuppressLint("ObjectAnimatorBinding") ObjectAnimator behindAnim = ObjectAnimator
                .ofFloat(iv_behind, "behind", 0, 5)
                .setDuration(10000);

        behindAnim.start();
        behindAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float cVal = (Float) animation.getAnimatedValue();
                Log.d(TAG, "onAnimationUpdate: " + cVal);
                if (cVal <= 1) {
                    iv_behind.setScaleX((float) (cVal*0.4 + 1));
                    iv_behind.setScaleY((float) (cVal*0.4 + 1));
                    iv_behind.setRotation((cVal - 4) * 90);

                } else if (cVal <= 2) {
                    iv_behind.setScaleX((float) (1.4+(cVal-1)*0.1));
                    iv_behind.setScaleY((float) (1.4+(cVal-1)*0.1));
                    iv_behind.setRotation((cVal - 4) * 90);

                } else if (cVal <= 3) {

                    iv_behind.setScaleX((float) (1.5 - (cVal-2)*0.5));
                    iv_behind.setScaleY((float) (1.5 - (cVal-2)*0.5));
                    iv_behind.setRotation((cVal - 4) * 90);
                    iv_behind.setAlpha((4-cVal)/2);

                } else if (cVal <= 4) {

                    iv_behind.setScaleX((float) (1- (cVal-3)*0.2));
                    iv_behind.setScaleY((float) (1- (cVal-3)*0.2));
                    iv_behind.setRotation((cVal - 4) * 90);
                    iv_behind.setAlpha((4-cVal)/2);

                } else if (cVal <= 5) {
                    iv_behind.setScaleX((float) ((5-cVal)*0.8));
                    iv_behind.setScaleY((float) ((5-cVal)*0.8));

                }
            }
        });

    }
}
