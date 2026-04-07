package com.vivalnk.sdk.demo.repository.database.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface IRoomDataDAO {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insert(VitalData_Room... data);

  @Update
  void update(VitalData_Room... data);

  @Delete()
  void delete(VitalData_Room... data);

  @Query("DELETE FROM demo_data")
  void deleteAll();

  @Query("SELECT * FROM demo_data")
  List<VitalData_Room> queryAll();

  @Query("SELECT * FROM demo_data WHERE d_id = :deviceId ORDER BY time ASC")
  List<VitalData_Room> queryAllOrderByTimeASC(String deviceId);

  @Query("SELECT * FROM demo_data ORDER BY time ASC LIMIT :count")
  List<VitalData_Room> queryOldestAll(long count);

  @Query("SELECT * FROM demo_data ORDER BY time DESC LIMIT :count")
  List<VitalData_Room> queryLatestAll(long count);

  @Query("SELECT * FROM demo_data WHERE d_id = :deviceID")
  List<VitalData_Room> queryAll(String deviceID);

  @Query("SELECT * FROM demo_data WHERE d_id = :deviceID AND time = :time")
  VitalData_Room query(String deviceID, long time);

  @Query("SELECT * FROM demo_data WHERE time BETWEEN :startTime AND :endTime AND d_id = :deviceID ORDER BY time ASC")
  List<VitalData_Room> query(String deviceID, long startTime, long endTime);

  @Query("SELECT COUNT(*) FROM demo_data")
  long getCount();

  @Query("SELECT COUNT(*) FROM demo_data WHERE d_id = :deviceId")
  long getCount(String deviceId);
}
