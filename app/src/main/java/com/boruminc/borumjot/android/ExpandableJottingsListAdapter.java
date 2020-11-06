package com.boruminc.borumjot.android;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.boruminc.borumjot.Jotting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ExpandableJottingsListAdapter extends BaseExpandableListAdapter {
    private Context context;
    private List<String> jottingsListTitles;
    private HashMap<String, List<Jotting>> allJottingsLists;

    ExpandableJottingsListAdapter(Context c) {
        context = c;
        jottingsListTitles = loadJottingsListTitles();
    }

    public void setAllJottingsLists(HashMap<String, List<Jotting>> allJottingsLists) {
        this.allJottingsLists = allJottingsLists;
    }

    private ArrayList<String> loadJottingsListTitles() {
        ArrayList<String> jottingsListTitles = new ArrayList<String>();
        jottingsListTitles.add("own");
        jottingsListTitles.add("shared");
        return jottingsListTitles;
    }

    @Override
    public int getGroupCount() {
        return this.jottingsListTitles.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return Objects.requireNonNull(this.allJottingsLists.get(this.jottingsListTitles.get(groupPosition))).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.jottingsListTitles.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        List<Jotting> currentJottingsListGroup = this.allJottingsLists.get(this.jottingsListTitles.get(groupPosition));
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
        String listTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            convertView = inflater.inflate(R.layout.list_group, null);
        }

        TextView listTitleTextView = (TextView) convertView
                .findViewById(R.id.jotting_name);
        listTitleTextView.setTypeface(null, Typeface.BOLD);
        listTitleTextView.setText(listTitle);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final String expandedListText = ((Jotting) getChild(groupPosition, childPosition)).getName();
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            convertView = inflater.inflate(R.layout.jotting_list_item, null);
        }

        TextView expandedListTextView = (TextView) convertView.findViewById(R.id.jotting_name);
        expandedListTextView.setText(expandedListText);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
