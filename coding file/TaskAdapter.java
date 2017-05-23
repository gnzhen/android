package com.example.gd.to_dolist;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by GD on 8/14/2016.
 */
public class TaskAdapter extends ArrayAdapter {

    private Activity activity;
    private ArrayList<Task> tasks;
    private static LayoutInflater inflater = null;

    public TaskAdapter (Activity activity, int textViewResourceId, ArrayList<Task> tasks) {
        super(activity, textViewResourceId, tasks);
        this.activity = activity;
        this.tasks = tasks;

        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return tasks.size();
    }

    public Task getItem(int position) {

        Task itemTask = new Task();

        //task added will placed on the top of list
        for(Task task: tasks){
            if (Long.toString(task.getId()).equals(Integer.toString(getCount() - position)))
                itemTask = task;
        }
        return itemTask;
    }

    public long getItemId(int position) {
        return position;
    }

    public static class ViewHolder {
        public TextView taskDesc;
        public TextView taskDate;
        public TextView taskTime;
        public TextView taskStatus;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        final ViewHolder holder;
        String status = tasks.get(position).getStatus();

        try {
            if (convertView == null) {
                if(status.equals("Done"))
                    view = inflater.inflate(R.layout.task_list_done, null);
                else if(status.equals(""))
                    view = inflater.inflate(R.layout.task_list_normal, null);
                else if(status.equals("Overdue"))
                    view = inflater.inflate(R.layout.task_list_overdue, null);

                holder = new ViewHolder();
                holder.taskDesc = (TextView) view.findViewById(R.id.task_desc);
                holder.taskDate = (TextView) view.findViewById(R.id.task_date);
                holder.taskTime = (TextView) view.findViewById(R.id.task_time);
                holder.taskStatus = (TextView) view.findViewById(R.id.task_status);

                view.setTag(holder);
            }
            else {
                holder = (ViewHolder) view.getTag();
            }

            SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm a");
            String displayDate = dateFormatter.format(Long.parseLong(tasks.get(position).getDate()));
            String displayTime = timeFormatter.format(Long.parseLong(tasks.get(position).getTime()));

            holder.taskDesc.setText(tasks.get(position).getDesc());
            holder.taskDate.setText(displayDate);
            holder.taskTime.setText(displayTime);
            holder.taskStatus.setText(tasks.get(position).getStatus());


        }
        catch (Exception e) {
        }
        return view;
    }
}
