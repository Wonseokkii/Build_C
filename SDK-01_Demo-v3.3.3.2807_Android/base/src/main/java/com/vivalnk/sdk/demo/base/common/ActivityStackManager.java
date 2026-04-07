package com.vivalnk.sdk.demo.base.common;

import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.os.Bundle;
import java.util.Stack;

/**
 * Created by JakeMo on 18-4-28.
 */
public class ActivityStackManager {

  private static volatile ActivityStackManager sStackManager;

  private static Stack<Activity> activityStack;

  public static ActivityStackManager getInstance() {
    if (sStackManager == null) {
      synchronized (ActivityStackManager.class) {
        if (sStackManager == null) {
          sStackManager = new ActivityStackManager();
        }
      }
    }
    return sStackManager;
  }

  public void init(Application application) {
    application.registerActivityLifecycleCallbacks(activityLifecycleCallbacks);
  }

  private ActivityLifecycleCallbacks activityLifecycleCallbacks = new ActivityLifecycleCallbacks() {
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
      addActivity(activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
      removeActivity(activity);
    }
  };


  /**
   * add Activity 添加Activity到栈
   */
  public void addActivity(Activity activity){
    synchronized (ActivityStackManager.class) {
      if(activityStack == null){
        activityStack = new Stack<>();
      }
    }
    activityStack.push(activity);
  }
  /**
   * get current Activity 获取当前Activity（栈中最后一个压入的）
   */
  public Activity peekActivity() {
    return activityStack.peek();
  }
  /**
   * 结束当前Activity（栈中最后一个压入的）
   */
  public void popActivity() {
    activityStack.pop();
  }

  /**
   * 结束指定的Activity
   */
  public void removeActivity(Activity activity) {
    if (activity != null) {
      synchronized (activityStack){
        activityStack.remove(activity);
        activity.finish();
      }
    }
  }

  /**
   * 结束指定类名的Activity
   */
  public void removeActivity(Class<?> cls) {
    for (Activity activity : activityStack) {
      if (activity.getClass().equals(cls)) {
        removeActivity(activity);
      }
    }
  }

  /**
   * 结束所有Activity
   */
  public void popAllActivity() {
    while (!activityStack.isEmpty()) {
      activityStack.pop().finish();
    }
  }

  /**
   * 退出应用程序
   */
  public void exitApp() {
    try {
      popAllActivity();
    } catch (Exception e) {
    }
  }


}
