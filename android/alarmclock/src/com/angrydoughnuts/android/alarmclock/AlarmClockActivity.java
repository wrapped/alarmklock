package com.angrydoughnuts.android.alarmclock;

import java.util.Calendar;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.AdapterView.OnItemClickListener;

public class AlarmClockActivity extends Activity {
  private final int TIME_PICKER_DIALOG_ID = 1;
  private final int DEFAULT_SETTINGS_MENU = 2;

  private AlarmClockServiceBinder service;
  private DbAccessor db;
  private Cursor alarmListCursor;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    service = AlarmClockServiceBinder.newBinderAndStart(getApplicationContext());
    db = new DbAccessor(getApplicationContext());
    alarmListCursor = db.getAlarmList();
    startManagingCursor(alarmListCursor);

    Button addBtn = (Button) findViewById(R.id.add_alarm);
    addBtn.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        showDialog(TIME_PICKER_DIALOG_ID);
      }
    });

    AlarmViewAdapter adapter = new AlarmViewAdapter(getApplicationContext(), R.layout.alarm_description, alarmListCursor, service);
    ListView alarmList = (ListView) findViewById(R.id.alarm_list);
    alarmList.setAdapter(adapter);
    alarmList.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> adapter, View view, int position,
          long id) {
        alarmListCursor.moveToPosition(position);
        long alarmId = alarmListCursor.getLong(
            alarmListCursor.getColumnIndex(DbHelper.ALARMS_COL__ID));
        service.deleteAlarm(alarmId);
        alarmListCursor.requery();
      }
    });
  }

  @Override
  protected Dialog onCreateDialog(int id) {
    switch (id) {
      case TIME_PICKER_DIALOG_ID:
        Calendar now = Calendar.getInstance();
        // TODO(cgallek): replace this with default alarm time.
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);

        // TODO(cgallek): set 12hr/24hr based off of locale settings.
        return new TimePickerDialog(this,
            new TimePickerDialog.OnTimeSetListener() {
              @Override
              public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                int minutesAfterMidnight =
                  TimeUtil.minutesAfterMidnight(hourOfDay, minute, 0);
                service.newAlarm(minutesAfterMidnight);
                alarmListCursor.requery();
              }
            },
            hour, minute, false);
      default:
        return super.onCreateDialog(id);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuItem defaults = menu.add(0, DEFAULT_SETTINGS_MENU, 0, R.string.default_settings);
    defaults.setIcon(android.R.drawable.ic_lock_idle_alarm);
    // TODO(cgallek): Should this still call the parent??
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case DEFAULT_SETTINGS_MENU:
        Intent i = new Intent(getApplicationContext(), DefaultSettingsActivity.class);
        startActivity(i);
    }
    // TODO(cgallek): Should this still call the parent??
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onResume() {
    super.onResume();
    service.bind();
  }

  @Override
  protected void onPause() {
    super.onPause();
    service.unbind();
    finish();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    db.closeConnections();
  }
}
