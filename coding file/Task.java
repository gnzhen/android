package com.example.gd.to_dolist;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by GD on 7/21/2016.
 */
public class Task implements java.io.Serializable {

    //property
    private long id;        //task id
    private String desc;    //task description
    private String date;    //task duedate
    private String time;    //task duetime
    private String status;  //done/overdue/normal("")
    private int reminder;   //reminder on-1/off-0

    public Task(){}
    //constructor
    public Task(long id, String desc, String date, String time, String status, int reminder){
        this.id = id;
        this.desc = desc;
        this.date = date;
        this.time = time;
        this.status = status;
        this.reminder = reminder;
    }

    public long getId() { return id; }
    public String getDesc() { return desc; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getStatus() { return status; }
    public int getReminder(){return reminder;}

    public void setId(long id) {
        this.id = id;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public void setTime(String time) {
        this.time = time;
    }
    public void setDesc(String desc) {
        this.desc = desc;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public void setReminder(int reminder) {this.reminder = reminder;}

    //return true if task overdue
    public boolean checkOverdue() {
        Calendar c = Calendar.getInstance();
        Long time_now = c.getTimeInMillis();

        return (Double.parseDouble(time) < Double.parseDouble(Long.toString(time_now))
                && Double.parseDouble(date) <= Double.parseDouble(Long.toString(time_now)));
    }

    //convert millisecond to displayable date
    public String convertDate(){
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
        return dateFormatter.format(Long.parseLong(date));
    }

    //convert millisecond to displayable time
    public String convertTime()
    {
        SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm a");
        return timeFormatter.format(Long.parseLong(time));
    }
}
