package com.vivalnk.sdk.demo.repository.database.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = { VitalData_Room.class, VitalDevice_Room.class}, version = 2, exportSchema = true)
public abstract class VitalDatabase extends RoomDatabase {
  public abstract IRoomDataDAO getVitalDataDAO();
  public abstract IRoomDeviceDAO getVitalDeviceDAO();
}
