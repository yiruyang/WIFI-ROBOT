// Generated code from Butter Knife. Do not modify!
package com.dsrobot;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import java.lang.IllegalStateException;
import java.lang.Override;

public class MainActivity_ViewBinding implements Unbinder {
  private MainActivity target;

  @UiThread
  public MainActivity_ViewBinding(MainActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public MainActivity_ViewBinding(MainActivity target, View source) {
    this.target = target;

    target.radar = Utils.findRequiredViewAsType(source, R.id.radar, "field 'radar'", ImageButton.class);
    target.mainToSetting = Utils.findRequiredViewAsType(source, R.id.maintosetting, "field 'mainToSetting'", ImageButton.class);
    target.mainToLogin = Utils.findRequiredViewAsType(source, R.id.maintologin, "field 'mainToLogin'", ImageButton.class);
    target.mjpegView = Utils.findRequiredViewAsType(source, R.id.view3D, "field 'mjpegView'", MjpegView.class);
    target.purifier = Utils.findRequiredViewAsType(source, R.id.purifier, "field 'purifier'", Switch.class);
    target.haveSignal = Utils.findRequiredViewAsType(source, R.id.haveSignal, "field 'haveSignal'", RelativeLayout.class);
    target.mHumidity = Utils.findRequiredViewAsType(source, R.id.humidity, "field 'mHumidity'", TextView.class);
    target.mTemperature = Utils.findRequiredViewAsType(source, R.id.temperature, "field 'mTemperature'", TextView.class);
    target.charged_batteryView = Utils.findRequiredViewAsType(source, R.id.charged_battery, "field 'charged_batteryView'", BatteryView.class);
    target.charging_batteryView = Utils.findRequiredViewAsType(source, R.id.charging_battery, "field 'charging_batteryView'", BatteryView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    MainActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.radar = null;
    target.mainToSetting = null;
    target.mainToLogin = null;
    target.mjpegView = null;
    target.purifier = null;
    target.haveSignal = null;
    target.mHumidity = null;
    target.mTemperature = null;
    target.charged_batteryView = null;
    target.charging_batteryView = null;
  }
}
