// Generated code from Butter Knife. Do not modify!
package com.dsrobot;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import java.lang.IllegalStateException;
import java.lang.Override;

public class Config_ViewBinding implements Unbinder {
  private Config target;

  @UiThread
  public Config_ViewBinding(Config target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public Config_ViewBinding(Config target, View source) {
    this.target = target;

    target.txt_VideoUrl = Utils.findRequiredViewAsType(source, R.id.VideoUrl, "field 'txt_VideoUrl'", EditText.class);
    target.txt_ControlUrl = Utils.findRequiredViewAsType(source, R.id.ControlUrl, "field 'txt_ControlUrl'", EditText.class);
    target.txt_port = Utils.findRequiredViewAsType(source, R.id.port, "field 'txt_port'", EditText.class);
    target.setting_back = Utils.findRequiredViewAsType(source, R.id.setting_back, "field 'setting_back'", TextView.class);
    target.btn_SaveButton = Utils.findRequiredViewAsType(source, R.id.SaveButton_Control, "field 'btn_SaveButton'", Button.class);
    target.qr_code = Utils.findRequiredViewAsType(source, R.id.qr_code, "field 'qr_code'", ImageButton.class);
    target.setting_to_login = Utils.findRequiredViewAsType(source, R.id.setting_to_login, "field 'setting_to_login'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    Config target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.txt_VideoUrl = null;
    target.txt_ControlUrl = null;
    target.txt_port = null;
    target.setting_back = null;
    target.btn_SaveButton = null;
    target.qr_code = null;
    target.setting_to_login = null;
  }
}
