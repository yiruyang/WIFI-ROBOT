package com.dsrobot;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.dsrobot.utils.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class Config extends BaseActivity{

	@BindView(R.id.VideoUrl)
	EditText txt_VideoUrl;
	@BindView(R.id.ControlUrl)
	EditText txt_ControlUrl;
	@BindView(R.id.port)
	EditText txt_port;
	@BindView(R.id.setting_back)
	TextView setting_back;
	@BindView(R.id.SaveButton_Control)
	Button btn_SaveButton;
	@BindView(R.id.qr_code)
	ImageButton qr_code;
	@BindView(R.id.setting_to_login)
	TextView setting_to_login;

	public static final String PREFS_NAME = "WIFI-Robot";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.main);
		setContentView(R.layout.config);

		ButterKnife.bind(this);

		onShow();

		//退出登录
		setting_to_login.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(Config.this, LoginAcivity.class));
				removeActivity();
			}
		});

		/**
		 * 保存事件
		 */
		btn_SaveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				onSave();
				Intent intent = new Intent();
				intent.setClass(Config.this, MainActivity.class);
				startActivity(intent);
				removeActivity();
			}
		});
		/**
		 * 扫描二维码
		 */
		qr_code.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(Config.this, CaptureActivity.class), 0);
			}
		});

		setting_back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(Config.this, MainActivity.class));
				setting_back.setClickable(false);
				removeActivity();
			}
		});
	}

	private void onShow() {

			// TODO Auto-generated method stub
			SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
			String mPreferences = null;

			mPreferences = settings.getString("VideoUrl", "http://192.168.1.1:8080/?action=stream");
			txt_VideoUrl.setText(mPreferences);

			mPreferences = settings.getString("ControlUrl", "192.168.1.1");
			txt_ControlUrl.setText(mPreferences);

			mPreferences = settings.getString("port", "2001");
			txt_port.setText(mPreferences);

		
	}
	
	private void onSave() {
		// TODO Auto-generated method stub
		if(TextUtils.isEmpty(txt_VideoUrl.getText().toString())){
			setPreferences("VideoUrl","http://192.168.1.1:8080/?action=stream");
		}else{
			String mPreferences = txt_VideoUrl.getText().toString();
			setPreferences("VideoUrl", mPreferences);
		}
		if(TextUtils.isEmpty(txt_ControlUrl.getText().toString())){
			setPreferences("ControlUrl","192.168.1.1");
		}else{
			String mPreferences = txt_ControlUrl.getText().toString();
			setPreferences("ControlUrl", mPreferences);
		}
		if(TextUtils.isEmpty(txt_port.getText().toString())){
			setPreferences("port","2001");
		}else{
			String mPreferences = txt_port.getText().toString();
			setPreferences("port", mPreferences);
		}
		setPreferences("cbx_DisplayMode", String.valueOf(false));
	}
	
	private void setPreferences(String Key,String mPreferences){
        // set preference
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Key, mPreferences);
        editor.commit();
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			Bundle bundle = data.getExtras();
			String strs = bundle.getString("result").trim();
			String[] strs1=strs.split("/");
			String[] strs2 = strs1[2].split(":");
			String videoUrl = strs1[0]+"/"+strs1[1]+"/"+strs1[2]+"/"+strs1[3];
			Log.i("QRRESULT", videoUrl);
			String controlUrl = strs2[0];
			Log.i("QRRESULT", controlUrl);
			String port = strs1[4];
			Log.i("QRRESULT", port);
			txt_VideoUrl.setText(videoUrl);
			txt_ControlUrl.setText(controlUrl);
			txt_port.setText(port);
			show_Toast("数据已生成！");
		}
	}
}
