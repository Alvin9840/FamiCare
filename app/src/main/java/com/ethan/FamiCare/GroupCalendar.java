package com.ethan.FamiCare;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import org.checkerframework.checker.units.qual.C;
import org.json.JSONException;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class GroupCalendar extends AppCompatActivity {

    private int selected_date;


    private CalendarView calendar;
    private TextView caldate;
    private Button addtime;
    private EditText addevent;
    private Button savecal;
    private String time_text;
    private Button noti;

    //Listview??????????????????
    private ListView listView;
    private ArrayList<HashMap<String, String>> arrayList;
    private ArrayList<HashMap<String, String>> arrayListnull;
    private SimpleAdapter adapter;
    private String[] from = {"date", "event", "time", "email"};
    private int[] to = {R.id.item_id, R.id.item_event, R.id.item_time, R.id.item_email};

    //???firebase?????????
    DatabaseReference myRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://famicare-375914-default-rtdb.firebaseio.com/");
    //?????????????????????
    FirebaseUser user;
    String token;


    //?????????time
    TimePickerDialog.OnTimeSetListener timeDialog;
    Calendar calendar1 = Calendar.getInstance();

    //notification
    private static final String channelId = "channeId";
    private static final String channelName = "channelName";
    private NotificationManager notificationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_calendar);

        calendar = findViewById(R.id.cal1);
        caldate = findViewById(R.id.caldate);
        addevent = findViewById(R.id.addevent);
        addtime = findViewById(R.id.addtime);
        listView = findViewById(R.id.listview);
        savecal = findViewById(R.id.savecal);
        noti = findViewById(R.id.noti);


        //?????????????????????
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please login", Toast.LENGTH_SHORT).show();
            finish();
        }


        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }
                        token = task.getResult();
                        System.out.println("Token=" + token);
                    }
                });

/*
        if(isTokenEmpty(token)==true){
            try {
                FCMaddgroup.addgroup(
                        GroupCalendar.this,
                        "add",
                        token
                );
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            Toast.makeText(GroupCalendar.this,"???????????????", Toast.LENGTH_SHORT).show();
        }


 */

        //??????????????????
        calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                caldate.setText((month + 1) + "/" + dayOfMonth);
                selected_date = getSelected_date(year, month, dayOfMonth);
                String sd = String.valueOf(selected_date);
                arrayList = new ArrayList<>();
                arrayListnull = new ArrayList<>();

                myRef.child("Calendar").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            CalendarDB calendarDB = ds.getValue(CalendarDB.class);
                            if (calendarDB.getId().equals(sd)) {
                                setAdapter();
                                HashMap<String, String> hashMap = new HashMap<>();
                                hashMap.put(from[0], calendarDB.getId());
                                hashMap.put(from[1], calendarDB.getEvent());
                                hashMap.put(from[2], calendarDB.getTime());
                                hashMap.put(from[3], calendarDB.getUser());
                                arrayList.add(hashMap);
                                adapter.notifyDataSetChanged();

                            } else {
                                setnullAdapter();
                                HashMap<String, String> hashMap = new HashMap<>();
                                arrayListnull.add(hashMap);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.w(TAG, "Failed to read value.", error.toException());
                    }
                });


            }
        });

        //????????????????????????
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                final int which_position = position;

                new AlertDialog.Builder(GroupCalendar.this)
                        .setTitle("?????????????????????????")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                HashMap hashMap = arrayList.get(which_position);
                                String event = (String) hashMap.get("event");
                                String date = (String) hashMap.get("date");
                                arrayList.remove(which_position);
                                adapter.notifyDataSetChanged();

                                myRef.child("Calendar").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot ds : snapshot.getChildren()) {
                                            CalendarDB calendarDB = ds.getValue(CalendarDB.class);
                                            if (calendarDB.getEvent().equals(event) && calendarDB.getId().equals(date)) {
                                                String path = ds.getKey();
                                                Toast.makeText(GroupCalendar.this, path, Toast.LENGTH_SHORT).show();
                                                myRef.child("Calendar").child(path).removeValue();

                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Log.w(TAG, "Failed to read value.", error.toException());
                                    }
                                });

                            }
                        })
                        .setNegativeButton("No", null)
                        .show();

                return true;
            }
        });

        //time??????dialog??????????????????Calendar.xxx??????????????????
        timeDialog = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                calendar1.set(Calendar.HOUR, hourOfDay);//??????
                calendar1.set(Calendar.MINUTE, minute);//??????

                String addevent_text = addevent.getText().toString();
                time_text = addevent_text + "\t" + "?????????\t" + hourOfDay + ":" + minute;
                addtime.setText(hourOfDay + ":" + minute);
            }
        };

        database();

        //???notification
        noti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String addevent_text = addevent.getText().toString();
                String time4 = addtime.getText().toString();
                String date = caldate.getText().toString();//ex:2/6
                String[] date1 = date.split("/");
                int month = Integer.parseInt(date1[0]) - 1;

                if (!(addevent_text.isEmpty() && time4.isEmpty())) {
                    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

                    //???????????????????????????ID????????????
                    int alarmId = SharedPreUtils.getInt(GroupCalendar.this, "alarm_id", 0);
                    SharedPreUtils.setInt(GroupCalendar.this, "alarm_id", ++alarmId);


                    //notificationId & message
                    Intent intent = new Intent(GroupCalendar.this,alarmReceiver.class);
                    intent.putExtra("event", addevent_text);
                    intent.putExtra("time", time4);

                    //PendingIntent pendingIntent = PendingIntent.getBroadcast(GroupCalendar.this, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(GroupCalendar.this, alarmId, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

                    String[] time5 = time4.split(":");//ex:14:28
                    int hour = Integer.parseInt(time5[0]);
                    int minute = Integer.parseInt(time5[1]);

                    //create time
                    Calendar starttime = Calendar.getInstance();
                    starttime.set(Calendar.MONTH, month);
                    starttime.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date1[1]));
                    starttime.set(Calendar.HOUR_OF_DAY, hour);
                    starttime.set(Calendar.MINUTE, minute);
                    starttime.set(Calendar.SECOND, 0);
                    starttime.set(Calendar.MILLISECOND, 0);
                    long alarmStartTime = starttime.getTimeInMillis();


                    //Set Alarm
                    alarmManager.set(AlarmManager.RTC_WAKEUP, alarmStartTime, pendingIntent);


                    String text = (starttime.get(Calendar.MONTH) + 1) + "???"
                            + starttime.get(Calendar.DAY_OF_MONTH) + "???\n"
                            + starttime.get(Calendar.HOUR_OF_DAY) + ":"
                            + starttime.get(Calendar.MINUTE);
                    Toast.makeText(GroupCalendar.this, text, Toast.LENGTH_SHORT).show();

                }
            }
        });


    }


    public int getSelected_date(int year, int month, int dayOfMonth) {
        String s = String.format("%4d%02d%02d", year, month + 1, dayOfMonth);
        return Integer.parseInt(s);
    }

    public void timePicker(View v) {
        //??????time???dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(v.getContext(),
                timeDialog,
                calendar1.get(Calendar.HOUR),
                calendar1.get(Calendar.MINUTE),
                false);
        timePickerDialog.show();
    }

    //???????????????
    private void database() {
        savecal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id_date = String.valueOf(selected_date);
                String event = addevent.getText().toString();

                String time1 = time_text;
                if (event.isEmpty() || time1.isEmpty() || id_date.isEmpty()) {
                    Toast.makeText(GroupCalendar.this, "????????????????????????", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String[] time2 = time1.split("\t");
                    String time3 = time2[2];

                    String email = user.getEmail();


                    CalendarDB calevent = new CalendarDB(id_date, event, time3, email, token);


                    myRef.child("Calendar").push().setValue(calevent);
                    Toast.makeText(GroupCalendar.this, "????????????", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void setAdapter() {
        adapter = new SimpleAdapter(this, arrayList, R.layout.event_item, from, to);
        listView.setAdapter(adapter);
    }

    private void setnullAdapter() {
        adapter = new SimpleAdapter(this, arrayListnull, R.layout.event_item, from, to);
        listView.setAdapter(adapter);
    }

    private boolean isTokenEmpty(String token) {
        myRef.child("Calendar").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    CalendarDB calendarDB = ds.getValue(CalendarDB.class);
                    if (calendarDB.getToken() == token) {
                        return;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return true;
    }
}