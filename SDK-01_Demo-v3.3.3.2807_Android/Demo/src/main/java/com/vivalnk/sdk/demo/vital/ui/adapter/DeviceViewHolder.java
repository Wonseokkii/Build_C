package com.vivalnk.sdk.demo.vital.ui.adapter;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.vivalnk.sdk.demo.vital.R;
import com.vivalnk.sdk.model.DeviceModel;

/**
 * Created by JakeMo on 18-5-2.
 */
public class DeviceViewHolder extends RecyclerView.ViewHolder {

  @BindView(R.id.tvDeviceName)
  public TextView tvDeviceName;
  @BindView(R.id.tvRSSI)
  public TextView tvRSSI;
  @BindView(R.id.tvDeviceMac)
  public TextView tvDeviceMac;

  @BindView(R.id.cardView)
  public View cardView;

  @BindView(R.id.tvConnected)
  public TextView tvConnected;

  public DeviceViewHolder(View itemView) {
    super(itemView);
    ButterKnife.bind(this, itemView);
  }

  public void bind(final int position, final ScanListAdapter.StatusDevice item,
      final OnItemClickListener listener) {
    String deviceName = item.device.getName();
    if (item.device.getModel() == DeviceModel.Checkme_O2) {
      if (item.device.getSn() != null && item.connect) {
        deviceName = deviceName + "(" + item.device.getSn().substring(item.device.getSn().length() - 4, item.device.getSn().length()) + ")";
      }
    }
    tvDeviceName.setText(deviceName);
    tvRSSI.setText(String.valueOf(item.device.getRssi()));
    tvDeviceMac.setText(item.device.getId());
    tvConnected.setVisibility(item.connect ? View.VISIBLE : View.GONE);
    cardView.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        listener.onItemClick(v, position, item.device);
      }
    });
  }
}
