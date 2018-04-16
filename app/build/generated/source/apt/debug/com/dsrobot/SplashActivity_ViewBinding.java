// Generated code from Butter Knife. Do not modify!
package com.dsrobot;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.ImageView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import java.lang.IllegalStateException;
import java.lang.Override;

public class SplashActivity_ViewBinding implements Unbinder {
  private SplashActivity target;

  @UiThread
  public SplashActivity_ViewBinding(SplashActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public SplashActivity_ViewBinding(SplashActivity target, View source) {
    this.target = target;

    target.iv_before = Utils.findRequiredViewAsType(source, R.id.iv_before, "field 'iv_before'", ImageView.class);
    target.iv_behind = Utils.findRequiredViewAsType(source, R.id.iv_behind, "field 'iv_behind'", ImageView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    SplashActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.iv_before = null;
    target.iv_behind = null;
  }
}
