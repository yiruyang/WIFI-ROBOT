// Generated code from Butter Knife. Do not modify!
package com.dsrobot;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import java.lang.IllegalStateException;
import java.lang.Override;

public class LoginAcivity_ViewBinding implements Unbinder {
  private LoginAcivity target;

  @UiThread
  public LoginAcivity_ViewBinding(LoginAcivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public LoginAcivity_ViewBinding(LoginAcivity target, View source) {
    this.target = target;

    target.et_inputname = Utils.findRequiredViewAsType(source, R.id.et_inputname, "field 'et_inputname'", EditText.class);
    target.et_inputpwd = Utils.findRequiredViewAsType(source, R.id.et_inputpwd, "field 'et_inputpwd'", EditText.class);
    target.btn_login = Utils.findRequiredViewAsType(source, R.id.btn_login, "field 'btn_login'", Button.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    LoginAcivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.et_inputname = null;
    target.et_inputpwd = null;
    target.btn_login = null;
  }
}
