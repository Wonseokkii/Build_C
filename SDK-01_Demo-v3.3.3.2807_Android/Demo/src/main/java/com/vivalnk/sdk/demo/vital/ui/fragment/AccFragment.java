package com.vivalnk.sdk.demo.vital.ui.fragment;

import butterknife.BindView;
import com.vivalnk.sdk.common.eventbus.ThreadMode;
import com.vivalnk.sdk.demo.base.app.Layout;
import com.vivalnk.sdk.common.eventbus.Subscribe;
import com.vivalnk.sdk.demo.vital.R;
import com.vivalnk.sdk.demo.base.widget.AccView;
import com.vivalnk.sdk.model.SampleData;

public class AccFragment extends ConnectedFragment {

  @BindView(R.id.accView)
  AccView accView;

  @Override
  protected Layout getLayout() {
    return Layout.createLayoutByID(R.layout.fragment_acc_graphic);
  }

  @Subscribe(threadMode = ThreadMode.BACKGROUND)
  public void onEcgDataEvent(SampleData ecgData) {
    if (ecgData == null || !ecgData.getDeviceID().equals(mDevice.getId())) {
      return;
    }
    if (!ecgData.isFlash()) {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          accView.addAccData(ecgData.getACC());
        }
      });
    }
  }

}
