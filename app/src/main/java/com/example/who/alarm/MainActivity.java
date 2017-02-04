package com.example.who.alarm;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.who.alarm.utils.Calendar;
import com.example.who.alarm.utils.Event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import static android.R.attr.id;
import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    Calendar mCalendar;
    java.util.Calendar javaCalendar = java.util.Calendar.getInstance();
    TextView currentDateTime;
    TextView timeFromText;
    TextView dateText;
    EditText title;
    EditText description;
//    TimePicker timeStart;
//    DatePicker dateDate;
    Button ok;
    Button chooseTime;
    Button chooseDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setCustomActionBar();

        title = (EditText) findViewById(R.id.title);
        description = (EditText) findViewById(R.id.description);
//        timeStart = (TimePicker) findViewById(R.id.timeStart);
//        timeStart.setIs24HourView(true);
        timeFromText = (TextView) findViewById(R.id.timeFromText);
        //setupUI(timeFromText);
        dateText = (TextView) findViewById(R.id.dateText);
        //setupUI(dateText);
        //dateDate = (DatePicker) findViewById(R.id.dateAlarm);
        //setupUI(dateDate);
        ok = (Button) findViewById(R.id.good);
        chooseTime = (Button) findViewById(R.id.chooseTime);
        chooseDate = (Button) findViewById(R.id.chooseDate);
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR},
                1);
        currentDateTime=(TextView)findViewById(R.id.currentDateTime);
        setInitialDateTime();
    }

    private void setCustomActionBar() {
        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);
        LayoutInflater mInflater = LayoutInflater.from(this);
        View mCustomView = mInflater.inflate(R.layout.custom_title_bar, null);
        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FFC0CB")));

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCalendar();
                getDateStartMillis();
            } else {
                Toast.makeText(MainActivity.this, "Permission denied to read your Calendar", Toast.LENGTH_SHORT).show();
                onBackPressed();
                }
                return;
            }
        }
    }

    private void getCalendar() {
        final List<Calendar> calendars = Calendar.getWritableCalendars(getContentResolver());
        mCalendar = calendars.get(0);
    }



    private long createEvent(long calendarID) throws SecurityException {
        ContentResolver cr = getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CalendarContract.Events.CALENDAR_ID, calendarID);
        contentValues.put(CalendarContract.Events.ALL_DAY, false);
        contentValues.put(CalendarContract.Events.TITLE, title.getText().toString());
        contentValues.put(CalendarContract.Events.DESCRIPTION, description.getText().toString());
        contentValues.put(CalendarContract.Events.DTSTART, getDateStartMillis() + 60000);
        contentValues.put(CalendarContract.Events.DTEND, getDateStartMillis() + 120000);
        contentValues.put(CalendarContract.Events.HAS_ALARM, true);
        contentValues.put(CalendarContract.Events.EVENT_TIMEZONE, CalendarContract.Calendars.CALENDAR_TIME_ZONE);
        Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, contentValues);
        long id = Integer.parseInt(uri.getLastPathSegment());
        return id;
    }


    public void setDate(View v) {
        new DatePickerDialog(MainActivity.this, d,
                javaCalendar.get(java.util.Calendar.YEAR),
                javaCalendar.get(java.util.Calendar.MONTH),
                javaCalendar.get(java.util.Calendar.DAY_OF_MONTH))
                .show();
    }
    public void setTime(View v) {
        new TimePickerDialog(MainActivity.this, t,
                javaCalendar.get(java.util.Calendar.HOUR_OF_DAY),
                javaCalendar.get(java.util.Calendar.MINUTE), true)
                .show();
    }
    private void setInitialDateTime() {

        currentDateTime .setText(DateUtils.formatDateTime(this,
                javaCalendar.getTimeInMillis(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
                        | DateUtils.FORMAT_SHOW_TIME));
    }
    TimePickerDialog.OnTimeSetListener t=new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            javaCalendar.set(java.util.Calendar.HOUR_OF_DAY, hourOfDay);
            javaCalendar.set(java.util.Calendar.MINUTE, minute);
            setInitialDateTime();
        }
    };

    // установка обработчика выбора даты
    DatePickerDialog.OnDateSetListener d=new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            javaCalendar.set(java.util.Calendar.YEAR, year);
            javaCalendar.set(java.util.Calendar.MONTH, monthOfYear);
            javaCalendar.set(java.util.Calendar.DAY_OF_MONTH, dayOfMonth);
            setInitialDateTime();
        }
    };


    private long getDateStartMillis(){
        if(javaCalendar!=null){
        return javaCalendar.getTimeInMillis();}
        else return 0;
    }

//    @TargetApi(15)
//    private long getDateStartMillis() {
//        java.util.Calendar calendar = java.util.Calendar.getInstance();
//        int year = dateDate.getYear();
//        int monthOfYear = dateDate.getMonth();
//        int dayOfMonth = dateDate.getDayOfMonth();
//        calendar.set(year, monthOfYear, dayOfMonth);
//        @Deprecated
//        int hourOfDay = timeStart.getCurrentHour();
//        @Deprecated
//        int minute = timeStart.getCurrentMinute();
//        calendar.set(java.util.Calendar.HOUR_OF_DAY, hourOfDay);
//        calendar.set(java.util.Calendar.MINUTE, minute);
//        return calendar.getTimeInMillis();
//    }


    private void setReminder(long eventID, int timeBefore) throws SecurityException {
        try {
            ContentResolver cr = getContentResolver();
            ContentValues values = new ContentValues();
            values.put(CalendarContract.Reminders.MINUTES, timeBefore);
            values.put(CalendarContract.Reminders.EVENT_ID, eventID);
            values.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
            Uri uri = cr.insert(CalendarContract.Reminders.CONTENT_URI, values);
            long id = Integer.parseInt(uri.getLastPathSegment());
            Cursor c = CalendarContract.Reminders.query(cr, eventID,
                    new String[]{CalendarContract.Reminders.MINUTES});
            if (c.moveToFirst()) {
            }
            values.notifyAll();
            c.notifyAll();
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void runForest(View v) {
        setReminder(createEvent(mCalendar.id), 1);
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setTitle("ЩЕ ЩОСЬ ТРЕБА?");
        dialog.setPositiveButton("ТАК", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which){
                finish();
                startActivity(getIntent());
            }
        }).setNegativeButton("НІ", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which){
                onBackPressed();
                finish();
            }
        });
        dialog.setCancelable(true);
        dialog.create();
        dialog.show();
    }

//    public void setupUI(View view) {
//        if (!(view instanceof EditText)) {
//            view.setOnTouchListener(new View.OnTouchListener() {
//                public boolean onTouch(View v, MotionEvent event) {
//                    hideSoftKeyboard(MainActivity.this);
//                    return false;
//                }
//            });
//        }
//    }
//
//    public static void hideSoftKeyboard(Activity activity) {
//        InputMethodManager inputMethodManager =
//                (InputMethodManager) activity.getSystemService(
//                        Activity.INPUT_METHOD_SERVICE);
//        inputMethodManager.hideSoftInputFromWindow(
//                activity.getCurrentFocus().getWindowToken(), 0);
//    }
}
