package com.angrydoughnuts.android.alarmclock;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DbAccessor {
  private DbHelper db;
  private SQLiteDatabase rDb;
  private SQLiteDatabase rwDb;

  public DbAccessor(Context context) {
    db = new DbHelper(context);
    rwDb = db.getWritableDatabase();
    rDb = db.getReadableDatabase();
  }

  public void closeConnections() {
    rDb.close();
    rwDb.close();
  }

  public Cursor getAlarmList() {
    return rDb.query(
        DbHelper.DB_TABLE_ALARMS,
        new String[] {
          DbHelper.ALARMS_COL__ID,
          DbHelper.ALARMS_COL_TIME,
          DbHelper.ALARMS_COL_ENABLED
        }, null, null, null, null,
        DbHelper.ALARMS_COL_TIME + " DESC");
  }

  public long newAlarm(int minutesAfterMidnight) {
    // TODO(cgallek) make sure this time doesn't exist yet.
    ContentValues values = new ContentValues(2);
    values.put(DbHelper.ALARMS_COL_TIME, minutesAfterMidnight);
    values.put(DbHelper.ALARMS_COL_ENABLED, false);
    long id = rwDb.insert(DbHelper.DB_TABLE_ALARMS, null, values);
    if (id < 0) {
      throw new IllegalStateException("Unable to insert into database");
    }
    return id;
  }

  public boolean deleteAlarm(long alarmId) {
    int count = rDb.delete(DbHelper.DB_TABLE_ALARMS,
        DbHelper.ALARMS_COL__ID + " = " + alarmId, null);
    return count > 0;
  }

  public boolean enableAlarm(long alarmId, boolean enabled) {
    ContentValues values = new ContentValues(1);
    values.put(DbHelper.ALARMS_COL_ENABLED, enabled);
    int count = rwDb.update(DbHelper.DB_TABLE_ALARMS, values,
        DbHelper.ALARMS_COL__ID + " = " + alarmId, null);
    return count != 0;
  }

  public int alarmTime(long alarmId) {
    Cursor cursor = rDb.query(DbHelper.DB_TABLE_ALARMS,
        new String[] { DbHelper.ALARMS_COL_TIME },
        DbHelper.ALARMS_COL__ID + " = " + alarmId, null, null, null, null);
    if (cursor.getCount() != 1) {
      return -1;
    }
    cursor.moveToFirst();
    int time = cursor.getInt(0);
    cursor.close();
    return time;
  }
}
