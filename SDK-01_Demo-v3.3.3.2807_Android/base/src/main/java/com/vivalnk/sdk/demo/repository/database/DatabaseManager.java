package com.vivalnk.sdk.demo.repository.database;

import android.content.Context;
import com.vivalnk.sdk.demo.repository.database.room.DatabaseManager_Room;
import com.vivalnk.sdk.demo.repository.database.room.VitalDataDAO_Room;
import com.vivalnk.sdk.demo.repository.database.room.VitalDeviceDAO_Room;
import com.vivalnk.sdk.model.SampleData;

public class DatabaseManager implements IDBManager {

  IDeviceDAO mIDeviceDAO;
  IDataDAO mIDataDAO;

  public static boolean isLogData = false;

  public void init(Context context) {
    DatabaseManager_Room.getInstance().init(context);
    mIDeviceDAO = new VitalDeviceDAO_Room(DatabaseManager_Room.getInstance().getDatabase());
    mIDataDAO = new VitalDataDAO_Room(DatabaseManager_Room.getInstance().getDatabase());
  }

  public void insert(SampleData sampleData) {
    if (sampleData != null) {
      getDataDAO().insert(sampleData);
    }
  }

  private static class SingletonHolder {
    private static final DatabaseManager
        INSTANCE = new DatabaseManager();
  }
  private DatabaseManager(){
  }

  public static DatabaseManager getInstance() {
    return SingletonHolder.INSTANCE;
  }

  public void init(IDeviceDAO mIDeviceDAO,
      IDataDAO mIDataDAO) {
    this.mIDeviceDAO = mIDeviceDAO;
    this.mIDataDAO = mIDataDAO;
  }

  public IDataDAO getDataDAO() {
    return mIDataDAO;
  }

  public IDeviceDAO getDeviceDAO() {
    return mIDeviceDAO;
  }

}
