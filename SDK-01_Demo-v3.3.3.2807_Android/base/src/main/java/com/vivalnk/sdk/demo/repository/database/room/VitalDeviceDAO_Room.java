package com.vivalnk.sdk.demo.repository.database.room;

import androidx.room.Transaction;
import com.vivalnk.sdk.demo.repository.database.IDeviceDAO;
import com.vivalnk.sdk.demo.repository.database.VitalDevice;
import com.vivalnk.sdk.common.utils.ListUtils;
import com.vivalnk.sdk.common.utils.log.VitalLog;
import java.util.ArrayList;
import java.util.List;

public class VitalDeviceDAO_Room implements IDeviceDAO {

  IRoomDeviceDAO dao;

  public VitalDeviceDAO_Room(VitalDatabase database) {
    this.dao = database.getVitalDeviceDAO();
  }

  @Override
  public void insert(VitalDevice... data) {
    try {
      VitalDevice_Room[] temp = convertToArray(data);
      dao.insert(temp);
    } catch (Exception e) {
      VitalLog.e(e.getMessage());
    }
  }

  @Override
  public void update(VitalDevice... data) {
    try {
      VitalDevice_Room[] temp = convertToArray(data);
      dao.update(temp);
    } catch (Exception e) {
      VitalLog.e(e.getMessage());
    }
  }

  @Override
  public void deleteAll() {
    try {
      dao.deleteAll();
    } catch (Exception e) {
      VitalLog.printE(e.getMessage());
    }
  }

  @Override
  public void delete(VitalDevice... data) {
    try {
      VitalDevice_Room[] temp = convertToArray(data);
      dao.delete(temp);
    } catch (Exception e) {
      VitalLog.e(e.getMessage());
    }
  }

  @Override
  @Transaction
  public void delete(String... deviceIDs) {
    for (int i = 0; i < deviceIDs.length; i++) {
      try {
        String deviceID = deviceIDs[i];
        VitalDevice_Room temp = dao.query(deviceID);
        dao.delete(temp);
      } catch (Exception e) {
        VitalLog.e(e.getMessage());
      }
    }
  }

  @Override
  @Transaction
  public List<VitalDevice> query(String... deviceIDs) {
    List<VitalDevice> ret = new ArrayList<>();
    for (int i = 0; i < deviceIDs.length; i++) {
      try {
        String deviceID = deviceIDs[i];
        VitalDevice_Room temp = dao.query(deviceID);
        if (temp != null) {
          ret.add(new VitalDevice(temp));
        }
      } catch (Exception e) {
        VitalLog.e(e.getMessage());
      }
    }

    return ret;
  }

  @Override
  public List<VitalDevice> queryAll() {
    return convertToList(dao.queryAll());
  }

  private VitalDevice_Room[] convertToArray(
      VitalDevice... data) {
    if (ListUtils.isEmpty(data)) {
      return new VitalDevice_Room[0];
    }
    VitalDevice_Room[] ret = new VitalDevice_Room[data.length];
    for (int i = 0; i < data.length; i++) {
      ret[i] = new VitalDevice_Room(data[i]);
    }
    return ret;
  }

  private List<VitalDevice> convertToList(List<VitalDevice_Room> data) {
    List<VitalDevice> ret = new ArrayList<>();
    if (ListUtils.isEmpty(data)) {
      return ret;
    }
    for (int i = 0; i < data.size(); i++) {
      ret.add(new VitalDevice(data.get(i)));
    }
    return ret;
  }
}
