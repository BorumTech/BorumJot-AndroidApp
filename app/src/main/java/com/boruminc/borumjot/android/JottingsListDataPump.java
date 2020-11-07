package com.boruminc.borumjot.android;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.boruminc.borumjot.Jotting;

import java.util.ArrayList;
import java.util.HashMap;

@RequiresApi(api = Build.VERSION_CODES.N)
class JottingsListDataPump {
    private HashMap<String, ArrayList<Jotting>> data;

    JottingsListDataPump() {
        data = loadData();
    }

    private HashMap<String, ArrayList<Jotting>> loadData() {
        HashMap<String, ArrayList<Jotting>> data = new HashMap<>();

        for (String title : getKeys())
            data.put(title, new ArrayList<>());

        return data;
    }

    static ArrayList<String> getKeys() {
        ArrayList<String> titles = new ArrayList<String>();
        titles.add("own");
        titles.add("shared");

        return titles;
    }

    HashMap<String, ArrayList<Jotting>> getData() {
        return data;
    }

    ArrayList<Jotting> getOwnData() {
        return data.get("own");
    }

    void setOwnData(ArrayList<Jotting> newOwnData) {
        data.replace("own", newOwnData);
    }

    ArrayList<Jotting> getSharedData() {
        return data.get("shared");
    }

    void setSharedData(ArrayList<Jotting> newSharedData) {
        data.replace("shared", newSharedData);
    }
}
