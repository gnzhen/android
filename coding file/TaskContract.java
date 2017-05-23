package com.example.gd.to_dolist;

import android.provider.BaseColumns;

/**
 * Created by GD on 8/2/2016.
 */
public class TaskContract {

    public TaskContract(){}

    public static abstract class TaskEntry implements BaseColumns{

        public static final String TABLE_NAME = "task";
        public static final String COLUMN_NAME_DESC = "desc";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_TIME = "time";
        public static final String COLUMN_NAME_STATUS = "status";
        public static final String COLUMN_NAME_REMINDER = "reminder";
    }
}
