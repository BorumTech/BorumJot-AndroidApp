package com.boruminc.borumjot.android;

import com.boruminc.borumjot.Jotting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JottingsListDataPump {
    public static HashMap<String, ArrayList<Jotting>> getData() {
        HashMap<String, List<Jotting>> expandableListDetail = new HashMap<>();

        List<Jotting> usersOwnJottings = new ArrayList<Jotting>();

        List<Jotting> sharedJottings = new ArrayList<Jotting>();

        expandableListDetail.put("own", usersOwnJottings);
        expandableListDetail.put("shared", sharedJottings);

        return expandableListDetail;
    }
}
