package com.boruminc.borumjot.android;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AppNameAppBarFragment extends Fragment {
    private View root;
    private TextView title;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.appbar_appname_fragment, container, false);

        title = root.findViewById(R.id.appbar_title);

        return root;
    }

    void passTitle(String titleTxt) {
        title.setText(titleTxt);
    }

}
