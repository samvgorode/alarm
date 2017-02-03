package com.example.who.alarm;

import android.Manifest;
import android.app.Activity;
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
    TextView timeFromText;
    TextView dateText;
    EditText title;
    EditText description;
    TimePicker timeStart;
    DatePicker dateDate;
    Button ok;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setCustomActionBar();
        title = (EditText) findViewById(R.id.title);
        description = (EditText) findViewById(R.id.description);
        timeStart = (TimePicker) findViewById(R.id.timeStart);
        timeStart.setIs24HourView(true);
        setupUI(timeStart);
        timeFromText = (TextView) findViewById(R.id.timeFromText);
        setupUI(timeFromText);
        dateText = (TextView) findViewById(R.id.dateText);
        setupUI(dateText);
        dateDate = (DatePicker) findViewById(R.id.dateAlarm);
        setupUI(dateDate);
        ok = (Button) findViewById(R.id.good);
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR},
                1);

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

    @RequiresApi(api = Build.VERSION_CODES.M)
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    private long getDateStartMillis() {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int year = dateDate.getYear();
        int monthOfYear = dateDate.getMonth();
        int dayOfMonth = dateDate.getDayOfMonth();
        calendar.set(year, monthOfYear, dayOfMonth);
        int hourOfDay = timeStart.getHour();
        int minute = timeStart.getMinute();
        calendar.set(java.util.Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(java.util.Calendar.MINUTE, minute);
        return calendar.getTimeInMillis();
    }


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
        Toast.makeText(MainActivity.this, "ВСЕ ДОБРЕ, Я ТЕБЕ КОХАЮ!", Toast.LENGTH_LONG).show();
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

    public void setupUI(View view) {
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(MainActivity.this);
                    return false;
                }
            });
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }
}
