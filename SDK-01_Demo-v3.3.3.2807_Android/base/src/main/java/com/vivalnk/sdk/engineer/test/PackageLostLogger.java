package com.vivalnk.sdk.engineer.test;

import com.vivalnk.sdk.common.eventbus.Subscribe;
import com.vivalnk.sdk.common.eventbus.ThreadMode;
import com.vivalnk.sdk.common.utils.EventBusHelper;
import com.vivalnk.sdk.data.stream.packagelost.DisContinuousEvent;
import com.vivalnk.sdk.model.Device;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PackageLostLogger extends AbsLogger {

  private CopyOnWriteArrayList<DisContinuousEvent> disContinuousEvents;
  private CopyOnWriteArrayList<String> disContinuousEventsString;
  public PackageLostLogger(Device device) {
    super(device);
    this.disContinuousEvents = new CopyOnWriteArrayList<>();
    this.disContinuousEventsString = new CopyOnWriteArrayList<>();
  }

  @Subscribe(threadMode = ThreadMode.BACKGROUND)
  public void onDisContinuousEvent(DisContinuousEvent event) {
    if (!event.device.equals(mDevice)) {
      return;
    }
    disContinuousEvents.add(event);
    disContinuousEventsString.add(event.msg);
    while (disContinuousEvents.size() > 2500) {
      disContinuousEvents.remove(0);
    }
    while (disContinuousEventsString.size() > 2500) {
      disContinuousEventsString.remove(0);
    }
  }

  public List<DisContinuousEvent> getDisContinuousEvents() {
    return disContinuousEvents;
  }

  public List<String> getDisContinuousEventsString() {
    return disContinuousEventsString;
  }

  @Override
  public boolean isStarted() {
    return EventBusHelper.getDefault().isRegistered(this);
  }

  @Override
  protected void register(Object object) {
    if (EventBusHelper.getDefault().isRegistered(this) == false) {
      EventBusHelper.getDefault().register(this);
    }
  }

  @Override
  protected void unregister(Object object) {
    if (EventBusHelper.getDefault().isRegistered(this) == true) {
      EventBusHelper.getDefault().unregister(this);
    }
  }


}
