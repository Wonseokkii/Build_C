package com.vivalnk.sdk.demo.repository.database.room;

import androidx.room.Transaction;
import com.vivalnk.sdk.demo.repository.database.IDataDAO;
import com.vivalnk.sdk.common.utils.ListUtils;
import com.vivalnk.sdk.common.utils.log.VitalLog;
import com.vivalnk.sdk.model.SampleData;
import java.util.ArrayList;
import java.util.List;

public class VitalDataDAO_Room implements IDataDAO {

  IRoomDataDAO dao;

  public VitalDataDAO_Room(VitalDatabase database) {
    this.dao = database.getVitalDataDAO();
  }

  @Override
  public void insert(SampleData... data) {
    try {
      VitalData_Room[] temp = convertToArray(data);
      dao.insert(temp);
    } catch (Exception e) {
      VitalLog.printE(e.getMessage());
    }
  }

  @Override
  public void update(SampleData... data) {
    try {
      VitalData_Room[] temp = convertToArray(data);
      dao.update(temp);
    } catch (Exception e) {
      VitalLog.printE(e.getMessage());
    }
  }

  @Override
  @Transaction
  public void delete(SampleData... data) {
    try {
      VitalData_Room[] temp = convertToArray(data);
      dao.delete(temp);
    } catch (Exception e) {
      VitalLog.printE(e.getMessage());
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
  public long getCount() {
    return dao.getCount();
  }

  @Override
  public long getCount(String deviceId) {
    return dao.getCount(deviceId);
  }

  @Override
  public List<SampleData> queryAll() {
    return convertToList(dao.queryAll());
  }

  @Override
  public List<SampleData> queryAllOrderByTimeASC(String deviceId) {
    return convertToList(dao.queryAllOrderByTimeASC(deviceId));
  }

  @Override
  public List<SampleData> queryOldestAll(long count) {
    return convertToList(dao.queryOldestAll(count));
  }

  @Override
  public List<SampleData> queryLatestAll(long count) {
    return convertToList(dao.queryLatestAll(count));
  }

  @Override
  public List<SampleData> queryAll(String deviceID) {
    return convertToList(dao.queryAll(deviceID));
  }

  @Override
  public SampleData query(String deviceID, long time) {
    VitalData_Room
        temp = dao.query(deviceID, time);
    return temp == null ? null : new SampleData(temp);
  }

  @Override
  public List<SampleData> query(String deviceID, long startTime, long endTime) {
    return convertToList(dao.query(deviceID, startTime, endTime));
  }

  private VitalData_Room[] convertToArray(
      SampleData... data) {
    if (ListUtils.isEmpty(data)) {
      return new VitalData_Room[0];
    }
    VitalData_Room[] ret = new VitalData_Room[data.length];
    for (int i = 0; i < data.length; i++) {
      ret[i] = new VitalData_Room(data[i]);
    }
    return ret;
  }

  private List<SampleData> convertToList(List<VitalData_Room> data) {
    List<SampleData> ret = new ArrayList<>();
    if (ListUtils.isEmpty(data)) {
      return ret;
    }
    for (int i = 0; i < data.size(); i++) {
      ret.add(new SampleData(data.get(i)));
    }
    return ret;
  }

}
