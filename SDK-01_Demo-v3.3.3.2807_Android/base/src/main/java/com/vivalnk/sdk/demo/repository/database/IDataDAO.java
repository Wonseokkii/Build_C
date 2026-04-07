package com.vivalnk.sdk.demo.repository.database;

import com.vivalnk.sdk.common.utils.ListUtils;
import com.vivalnk.sdk.model.SampleData;
import java.util.List;

public interface IDataDAO {
  void insert(SampleData... data);

  void update(SampleData... data);

  void delete(SampleData... data);

  void deleteAll();

  default long getCount() {
    return 0;
  }

  default long getCount(String deviceId) {
    return 0;
  }

  List<SampleData> queryAll();
  default List<SampleData> queryAllOrderByTimeASC(String deviceId) {
    return ListUtils.getEmptyList();
  }
  default List<SampleData> queryOldestAll(long count) {
    return ListUtils.getEmptyList();
  }
  default List<SampleData> queryLatestAll(long count) {
    return ListUtils.getEmptyList();
  }

  default List<SampleData> queryAll(String deviceID) {
    return ListUtils.getEmptyList();
  }
  default List<SampleData> queryOldestAll(String deviceID, long count) {
    return ListUtils.getEmptyList();
  }
  default List<SampleData> queryLatestAll(String deviceID, long count) {
    return ListUtils.getEmptyList();
  }

  SampleData query(String deviceID, long time);

  List<SampleData> query(String deviceID, long startTime, long endTime);
  
}
