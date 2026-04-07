package com.vivalnk.sdk.demo.repository.database.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

/**
 * Created by JakeMo on 18-4-27.
 */
@Dao
public interface IRoomDeviceDAO {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insert(VitalDevice_Room... data);

  @Update
  void update(VitalDevice_Room... data);

  @Delete
  void delete(VitalDevice_Room... data);

  @Query("DELETE FROM demo_device")
  void deleteAll();

  @Query("SELECT * FROM demo_device")
  List<VitalDevice_Room> queryAll();

  @Query("SELECT * FROM demo_device WHERE d_id = :deviceID")
  VitalDevice_Room query(String deviceID);

}
