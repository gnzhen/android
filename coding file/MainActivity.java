package com.example.gd.to_dolist;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.text. DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static TaskDbHelper mDbHelper;
    private static SQLiteDatabase db;
    ArrayList<Task> tasks;
    Boolean edit = false;
    String desc, date, time, status, displayDate, displayTime;
    int reminder;
    ListView listView;
    Cursor cursor;
    String sortOrder;
    TaskAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //start add task activity
                    Intent intent = new Intent(getApplicationContext(), InsertActivity.class);
                    Bundle args = new Bundle();
                    args.putSerializable("edit", edit);
                    intent.putExtras(args);
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        //refresh data changes
        readFromDb(getApplicationContext());
        setListView();
        scheduleReminder(getApplicationContext());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings: {
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*Display alert dialog while onItemLongClicked*/
    public void showFunctionDialog(Context context, Task t) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        final Task task = t;
        int stringArray;

        //list view various according to task.state
        if(task.getStatus().equals("Done"))
            stringArray = R.array.function_array1;
        else if(task.getStatus().equals("Overdue")
                && task.getReminder() == 1)
            stringArray = R.array.function_array3;
        else
            stringArray = R.array.function_array2;

        //generate event while onListViewItemClicked
        builder.setItems(stringArray, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    //Mark done or mark undone
                    case 0:{
                        if(task.getStatus().equals("Done")){
                            if(task.checkOverdue())
                                task.setStatus("Overdue");
                            else
                                task.setStatus("");

                            Toast.makeText(getApplicationContext(), "Marked Undone!", Toast.LENGTH_LONG).show();
                        }
                        else{
                            task.setStatus("Done");
                            task.setReminder(0); //set reminder off for done task
                            Toast.makeText(getApplicationContext(), "Marked Done!", Toast.LENGTH_LONG).show();
                        }

                        updateDatabase(task, getApplicationContext());
                        readFromDb(getApplicationContext());
                        setListView();
                        scheduleReminder(getApplicationContext());

                        break;
                    }

                    //Edit, call add task activity
                    case 1:{
                        editTask(task);
                        break;
                    }

                    //delete selected task
                    case 2:{
                        tasks.remove(task);

                        mDbHelper = new TaskDbHelper(getApplicationContext());
                        SQLiteDatabase db = mDbHelper.getWritableDatabase();

                        db.delete(TaskContract.TaskEntry.TABLE_NAME, null, null);

                        ContentValues values = new ContentValues();

                        Collections.reverse(tasks);

                        for(Task t: tasks){
                            values.put(TaskContract.TaskEntry.COLUMN_NAME_DESC, t.getDesc());
                            values.put(TaskContract.TaskEntry.COLUMN_NAME_DATE, t.getDate());
                            values.put(TaskContract.TaskEntry.COLUMN_NAME_TIME, t.getTime());
                            values.put(TaskContract.TaskEntry.COLUMN_NAME_STATUS, t.getStatus());
                            values.put(TaskContract.TaskEntry.COLUMN_NAME_REMINDER, t.getReminder());


                            long id = db.insert(TaskContract.TaskEntry.TABLE_NAME, null, values);
                        }

                        adapter.notifyDataSetChanged();

                        Toast.makeText(getApplicationContext(), "Deleted!", Toast.LENGTH_LONG).show();

                        readFromDb(getApplicationContext());
                        setListView();
                        scheduleReminder(getApplicationContext());

                        break;
                    }

                    //cancel
                    case 3:{
                        dialog.dismiss();
                        break;
                    }
                    //turn off reminder
                    case 4:{
                        task.setReminder(0);
                        updateDatabase(task, getApplicationContext());
                        readFromDb(getApplicationContext());
                        scheduleReminder(getApplicationContext());
                        break;
                    }
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //to change data of a created task
    public void editTask(Task task) {
        edit = true;

        Intent intent = new Intent(getApplicationContext(), InsertActivity.class);
        Bundle args = new Bundle();
        args.putSerializable("task", task);
        args.putSerializable("edit", edit);
        intent.putExtras(args);
        startActivity(intent);
    }

    //write database data to arraylist 'tasks'
    public void readFromDb(Context context){
        mDbHelper = new TaskDbHelper(this);
        db = mDbHelper.getReadableDatabase();

        tasks = new ArrayList<>();
        edit = false;

        String[] projection = {
                TaskContract.TaskEntry._ID,
                TaskContract.TaskEntry.COLUMN_NAME_DESC,
                TaskContract.TaskEntry.COLUMN_NAME_DATE,
                TaskContract.TaskEntry.COLUMN_NAME_TIME,
                TaskContract.TaskEntry.COLUMN_NAME_STATUS,
                TaskContract.TaskEntry.COLUMN_NAME_REMINDER
        };

        sortOrder = TaskContract.TaskEntry._ID + " DESC"; //ascending

        cursor = db.query(
                TaskContract.TaskEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );

        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow
                        (TaskContract.TaskEntry._ID));
                desc = cursor.getString(cursor.getColumnIndexOrThrow
                        (TaskContract.TaskEntry.COLUMN_NAME_DESC));
                date = cursor.getString(cursor.getColumnIndexOrThrow
                        (TaskContract.TaskEntry.COLUMN_NAME_DATE));
                time = cursor.getString(cursor.getColumnIndexOrThrow
                        (TaskContract.TaskEntry.COLUMN_NAME_TIME));
                status = cursor.getString(cursor.getColumnIndexOrThrow
                        (TaskContract.TaskEntry.COLUMN_NAME_STATUS));
                reminder = cursor.getInt(cursor.getColumnIndexOrThrow
                        (TaskContract.TaskEntry.COLUMN_NAME_REMINDER));

                //0-reminder off, 1-reminder on
                if(status.equals("Done"))
                    reminder = 0;
                else
                    reminder = 1;

                Task task = new Task(id, desc, date, time, status, reminder);


                displayDate = task.convertDate();
                displayTime = task.convertTime();

                if(!status.equals("Done")){
                    if(task.checkOverdue())
                        task.setStatus("Overdue");
                    else
                        task.setStatus("");
                }

                tasks.add(task);

            } while (cursor.moveToNext());
        }
        db.close();
    }

    public void setListView() {
        adapter = new TaskAdapter(this, 0, tasks);

        listView = (ListView) findViewById(R.id.list_view);
        if (adapter.getCount() != 0) {
            listView.post(new Runnable() {
                @Override
                public void run() {
                    listView.setAdapter(adapter);
                    listView.smoothScrollToPosition(0);
                }
            });

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {

                    Task task = adapter.getItem(pos);

                    editTask(task);
                }
            });

            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long id) {

                    Task task = adapter.getItem(pos);
                    showFunctionDialog(MainActivity.this, task);

                    return true;
                }

            });
        }
    }

    //arrange notification
    public void scheduleReminder(Context context) {

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        ArrayList<PendingIntent> intentArray = new ArrayList<>();

        for(Task task: tasks){

            //set reminder off for done task
            if(task.getStatus().equals("Done")){
                task.setReminder(0);
                updateDatabase(task, context);
            }

            //schedule reminder for task
            if(task.getReminder() == 1){
                SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
                String alarmTimeString = task.convertDate() + " " + task.convertTime();

                String alarmTime = "";

                try {
                    Date date = dateTimeFormatter.parse(alarmTimeString);
                    alarmTime = Long.toString(date.getTime());

                } catch (ParseException e) {
                    e.printStackTrace();
                }

                //calculate overdue time in minutes
                Long now = Calendar.getInstance().getTimeInMillis();
                Long overdue = now - Long.parseLong(alarmTime);
                Long overdueMinute = (overdue / 1000) / 60;
                String overdueMin = Long.toString(overdueMinute);

                //to broadcast receiver
                Intent alarmIntent = new Intent("com.example.gd.to_do_list.Task_to_do");
                Bundle bundle = new Bundle();
                bundle.putSerializable("task", task);
                bundle.putSerializable("overdue", overdueMin);
                alarmIntent.putExtras(bundle);
                sendBroadcast(alarmIntent);

                //as unique pendingIntent id for each task
                int id = Integer.parseInt(Long.toString(task.getId()));

                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        MainActivity.this, id, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                if(!alarmTime.equals("")) {
                    long repeatingTime15min=15*60*1000;
                    long oneHour = 60*60*1000;

                    //notify 1hr before deadline
                    alarmManager.set(AlarmManager.RTC_WAKEUP, Long.parseLong(alarmTime)-oneHour, pendingIntent);
                    //notify half an hour before deadline
                    alarmManager.set(AlarmManager.RTC_WAKEUP, Long.parseLong(alarmTime)-(oneHour/2), pendingIntent);
                    //notify every 15minutes after deadline
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,Long.parseLong(alarmTime),repeatingTime15min, pendingIntent);
                }
                //store pendingIntent into array
                intentArray.add(pendingIntent);
            }
            //delete pendingIntent for reminder off tasks
            else if(task.getReminder() == 0){
                int id = Integer.parseInt(Long.toString(task.getId()));

                Intent alarmIntent = new Intent(context, AlarmReceiver.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("task", task);
                alarmIntent.putExtras(bundle);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        this, id, alarmIntent,0);

                pendingIntent.cancel();
                alarmManager.cancel(pendingIntent);
            }
        }

    }

    //update dbs after edit a task
    public void updateDatabase(Task task, Context context){
        mDbHelper = new TaskDbHelper(context);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String selection = TaskContract.TaskEntry._ID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(Long.toString(task.getId())) };

        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskEntry.COLUMN_NAME_DESC, task.getDesc());
        values.put(TaskContract.TaskEntry.COLUMN_NAME_DATE, task.getDate());
        values.put(TaskContract.TaskEntry.COLUMN_NAME_TIME, task.getTime());
        values.put(TaskContract.TaskEntry.COLUMN_NAME_STATUS, task.getStatus());
        values.put(TaskContract.TaskEntry.COLUMN_NAME_REMINDER, task.getReminder());

        int count = db.update(
                TaskContract.TaskEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);

    }
}


