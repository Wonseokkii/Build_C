package com.vivalnk.sdk.demo.repository.database;

import java.util.List;

public interface IDeviceDAO {
  void insert(VitalDevice... data);

  void update(VitalDevice... data);

  void deleteAll();

  void delete(VitalDevice... data);

  void delete(String... deviceIDs);

  List<VitalDevice> queryAll();

  List<VitalDevice> query(String... deviceIDs);
}
