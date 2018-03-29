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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;

import com.dsrobot.utils.BaseActivity;

public class Config extends BaseActivity{

	private EditText txt_VideoUrl = null;
	private EditText txt_ControlUrl = null;
	private EditText txt_port = null;
	private ImageButton setting_back;
	private ImageButton btn_SaveButton = null;
	private ImageButton qr_code;
	private CheckBox cbx_DisplayMode = null;
	public static final String PREFS_NAME = "WIFI-Robot";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.main);
		setContentView(R.layout.config);
		

		txt_VideoUrl = (EditText)findViewById(R.id.VideoUrl);
		txt_ControlUrl = (EditText)findViewById(R.id.ControlUrl);
		txt_port = (EditText)findViewById(R.id.port);
		btn_SaveButton = findViewById(R.id.SaveButton_Control);
		setting_back = findViewById(R.id.setting_back);
		txt_VideoUrl = findViewById(R.id.VideoUrl);
		txt_ControlUrl = findViewById(R.id.ControlUrl);
		txt_port = findViewById(R.id.port);
		qr_code = findViewById(R.id.qr_code);
		onShow();

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
				btn_SaveButton.setClickable(false);
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
//		btn_SaveButton.setOnTouchListener(new OnTouchListener() {
//
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				// TODO Auto-generated method stub
//				switch (event.getAction()) {
//				case MotionEvent.ACTION_DOWN:
//
//					break;
//				}
//				return false;
//			}
//		});

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
		}
	}
}
