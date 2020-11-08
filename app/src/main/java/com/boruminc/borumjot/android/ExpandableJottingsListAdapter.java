package com.boruminc.borumjot.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.boruminc.borumjot.Jotting;
import com.boruminc.borumjot.Note;
import com.boruminc.borumjot.Task;
import com.boruminc.borumjot.android.customviews.SerializableImage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.N)
public class ExpandableJottingsListAdapter extends BaseExpandableListAdapter {
    private Context context;

    private List<String> jottingsListTitles;
    private List<String> jottingsListKeys;
    private HashMap<String, ArrayList<Jotting>> allJottingsLists;

    ExpandableJottingsListAdapter(Context c) {
        context = c;
        jottingsListKeys = JottingsListDataPump.getKeys();
        jottingsListTitles = loadTitles();
    }

    private ArrayList<String> loadTitles() {
        ArrayList<String> titles = new ArrayList<String>();
        titles.add("My Jottings");
        titles.add("Shared Jottings");
        return titles;
    }

    void setAllJottingsLists(HashMap<String, ArrayList<Jotting>> allJottingsLists) {
        this.allJottingsLists = allJottingsLists;
    }

    public ExpandableJottingsListAdapter.GroupTitleViewHolder onCreateDividerViewHolder(ViewGroup parent) {
        // Create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_group, parent, false);
        return new GroupTitleViewHolder(v, context);
    }

    public ExpandableJottingsListAdapter.ChildViewHolder onCreateChildViewHolder(ViewGroup parent) {
        // Create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.jotting_list_item, parent, false);
        return new ChildViewHolder(v, context);
    }

    /*
     * Replace the contents of a view (invoked by the layout manager)
     * Get element from the dataset at this position
     * Replace the contents of the view with that element
     */
    public void onBindViewHolder(ExpandableListViewHolder holder, String key, int position) {
        holder.bindView(Objects.requireNonNull(allJottingsLists.get(key)).get(position));
    }

    @Override
    public int getGroupCount() {
        return this.jottingsListTitles.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return Objects.requireNonNull(this.allJottingsLists.get(this.jottingsListKeys.get(groupPosition))).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.jottingsListTitles.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        List<Jotting> currentJottingsListGroup = this.allJottingsLists.get(this.jottingsListKeys.get(groupPosition));
        if (currentJottingsListGroup != null)
            return currentJottingsListGroup.get(childPosition);
        else
            return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            convertView = inflater.inflate(R.layout.list_group, null);
        }

        GroupTitleViewHolder holder = new GroupTitleViewHolder(convertView, context);
        holder.bindView(jottingsListTitles.get(groupPosition));

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final Jotting jotting = (Jotting) getChild(groupPosition, childPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            convertView = inflater.inflate(R.layout.jotting_list_item, null);
        }

        ChildViewHolder holder = new ChildViewHolder(convertView, context);
        holder.bindView(jotting);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    abstract static class ExpandableListViewHolder {
        private Context context;

        ExpandableListViewHolder(View vg, Context c) {
            context = c;
            setViewProperties(vg);
        }

        Context getContext() {
            return context;
        }

        abstract void setViewProperties(View vg);
        abstract void bindView(Object data);
    }

    // TODO GroupTitleViewHolder
    static class GroupTitleViewHolder extends ExpandableListViewHolder {
        private TextView titleTextView;

        GroupTitleViewHolder(View vg, Context c) {
            super(vg, c);
        }

        @Override
        void setViewProperties(View vg) {
            titleTextView = vg.findViewById(R.id.list_group_header);
            titleTextView.setTypeface(null, Typeface.BOLD);
        }

        void bindView(Object data) {
            titleTextView.setText((String) data);
        }
    }

    // DONE ChildViewHolder
    static class ChildViewHolder extends ExpandableListViewHolder implements View.OnLongClickListener {
        private TextView textView;
        private SerializableImage pinIcon;

        ChildViewHolder(View v, Context c) {
            super(v, c);
        }

        @Override
        protected void setViewProperties(View v) {
            pinIcon = v.findViewById(R.id.pin_icon);
            pinIcon.setLongClickable(false);

            textView = v.findViewById(R.id.jotting_name);
            textView.setOnClickListener(this::navToJot);
            textView.setTextColor(Color.BLACK);

            pinIcon.setOnLongClickListener(this);
            textView.setOnLongClickListener(this);
            v.setOnLongClickListener(this);

            // Surround each Jottings List item view with padding on all except the right side
            int padding = (int) getContext().getResources().getDimension(R.dimen.activity_horizontal_margin);
            textView.setPadding(padding, padding, 0, padding);
        }

        void bindView(Object data) {
            Jotting jottingInst = (Jotting) data;
            textView.setText(jottingInst.getName());
            textView.setTag(jottingInst);
            pinIcon.setVisibility(jottingInst.getPriority() > 0 ? View.VISIBLE : View.INVISIBLE);
        }

        /**
         * Navigate to the right "Jotting" type activity.
         * Cannot be dynamic because AAB changes class names
         * @param v The list item
         */
        void navToJot(View v) {
            // Create empty intent to the activity
            Intent jottingIntent = new Intent();

            // Set data and Class<?> destination for intent
            if (v.getTag() instanceof Task) {
                jottingIntent.setClass(getContext(), TaskActivity.class);
                jottingIntent.putExtra("data", (Task) v.getTag());
            } else if (v.getTag() instanceof Note) {
                jottingIntent.setClass(getContext(), NoteActivity.class);
                jottingIntent.putExtra("data", (Note) v.getTag());
            }

            if (jottingIntent.hasExtra("data")) {
                getContext().startActivity(jottingIntent);
                ((Activity) getContext()).overridePendingTransition(0, 0);
            } // Navigate to the proper Jotting type activity if data exists, otherwise, display error
            else Toast.makeText(getContext(), "An error occurred in viewing your jotting", Toast.LENGTH_SHORT).show();
        }

        @Override
        public boolean onLongClick(View v) {
            AppCompatActivity currentActivity = (AppCompatActivity) getContext();
            Toolbar temporaryAppBar = currentActivity.findViewById(R.id.jotting_options_toolbar);

            // Set activity to properly display new action bar
            currentActivity.setSupportActionBar(temporaryAppBar);
            currentActivity.findViewById(R.id.my_toolbar).setVisibility(View.INVISIBLE);

            // Make visible and set data to jotting options toolbar
            temporaryAppBar.setVisibility(View.VISIBLE);

            Bundle bundle = new Bundle();
            bundle.putSerializable("data", (Serializable) v.getTag());
            bundle.putSerializable("view", pinIcon);

            currentActivity.getSupportFragmentManager().findFragmentById(R.id.jotting_options_toolbar).setArguments(bundle);

            return true;
        }
    }
}
