package com.example.gd.to_dolist;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by GD on 8/2/2016.
 */
public class TaskDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "task.db";

    public static final String SQL_CREATE_ENTRIES = "CREATE TABLE " +
            TaskContract.TaskEntry.TABLE_NAME + "(" +
            TaskContract.TaskEntry._ID + " INTEGER PRIMARY KEY," +
            TaskContract.TaskEntry.COLUMN_NAME_DESC + " TEXT," +
            TaskContract.TaskEntry.COLUMN_NAME_DATE + " TEXT," +
            TaskContract.TaskEntry.COLUMN_NAME_TIME + " TEXT," +
            TaskContract.TaskEntry.COLUMN_NAME_STATUS + " TEXT," +
            TaskContract.TaskEntry.COLUMN_NAME_REMINDER + " INTEGER" + ")";

    public TaskDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db){
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS " + TaskContract.TaskEntry.TABLE_NAME);
        if (newVersion > oldVersion) {
            db.execSQL("ALTER TABLE task ADD COLUMN reminder INTEGER DEFAULT 0");
        }
        onCreate(db);
    }
}
