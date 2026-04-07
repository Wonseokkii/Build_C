package com.vivalnk.sdk.demo.repository.database.room;

import androidx.annotation.NonNull;
import androidx.room.Room;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

/**
 * Created by JakeMo on 18-4-26.
 */
public class DatabaseManager_Room implements Handler.Callback {

  public static final String TAG = "DatabaseManager";

  //一个小时检查一次数据库大小
  private static final int time = 60 * 60 * 1;


  private Context mContext;

  private VitalDatabase mDatabase;

  private static class SingletonHolder {
    private static final DatabaseManager_Room
        INSTANCE = new DatabaseManager_Room();
  }
  private DatabaseManager_Room(){
  }
  public static DatabaseManager_Room getInstance() {
    return SingletonHolder.INSTANCE;
  }

  public void init(Context context) {
    this.mContext = context.getApplicationContext();

    Migration migration_1_2 = new Migration(1, 2) {
      @Override
      public void migrate(@NonNull SupportSQLiteDatabase database) {
        database.execSQL("CREATE TABLE IF NOT EXISTS `demo_data_new` (`id` INTEGER NOT NULL, `d_id` TEXT NOT NULL, `d_sn` TEXT, `d_name` TEXT, `model` INTEGER NOT NULL, `time` INTEGER NOT NULL, `extras` TEXT, PRIMARY KEY(`d_id`, `model`, `time`))");
        database.execSQL("INSERT INTO 'demo_data_new' (id, d_id, d_sn, d_name, model, time, extras) SELECT id, d_id, d_sn, d_name, model, time, extras FROM 'demo_data'");
        database.execSQL("DROP TABLE 'demo_data'");
        database.execSQL("ALTER TABLE 'demo_data_new' RENAME TO 'demo_data'");

        database.execSQL("CREATE TABLE IF NOT EXISTS `demo_device_new` (`id` INTEGER NOT NULL, `d_id` TEXT NOT NULL, `d_sn` TEXT, `d_name` TEXT, `d_model` INTEGER NOT NULL, `hw_v` TEXT, `fw_v` TEXT, `info` TEXT, PRIMARY KEY(`d_id`, `d_model`))");
        database.execSQL("INSERT INTO 'demo_device_new' (id, d_id, d_sn, d_name, d_model, hw_v, fw_v, info) SELECT id, d_id, d_sn, d_name, d_model, hw_v, fw_v, info FROM 'demo_device'");
        database.execSQL("DROP TABLE 'demo_device'");
        database.execSQL("ALTER TABLE 'demo_device_new' RENAME TO 'demo_device'");

        database.execSQL("CREATE UNIQUE INDEX index_demo_data_d_id_model_time ON demo_data (d_id, model, time)");
        database.execSQL("CREATE UNIQUE INDEX index_demo_device_d_id_d_model ON demo_device (d_id, d_model)");
      }
    };
    mDatabase = Room.databaseBuilder(context, VitalDatabase.class, "demo_vital_db")
        .allowMainThreadQueries()
        .addMigrations(migration_1_2)
        //.openHelperFactory(new FrameworkSQLiteOpenHelperFactory())
        .build();
  }

  public VitalDatabase getDatabase() {
    return mDatabase;
  }

  public IRoomDataDAO getVitalDataDAO() {
    return mDatabase.getVitalDataDAO();
  }

  public IRoomDeviceDAO getVitalDeviceDAO() {
    return mDatabase.getVitalDeviceDAO();
  }

  @Override
  public boolean handleMessage(Message msg) {

    //一天设备记录约200M， 5天为1G, 7个设备则为7G
    //
    //long dataCount = VitalDataDAO.getInstance().getCount();
    ////上限是3024000条数据，即7个设备连续记录5天的数据
    ////一个设备一天86400条数据，连续记录5天，7台设备同时记录，则存储的条目为
    ////86400 * 5 * 7 = 3024000
    //if (dataCount > 86400 * 5 * 7) {
    //  VitalDataDAO.getInstance().clear();
    //}

    return false;
  }

}
