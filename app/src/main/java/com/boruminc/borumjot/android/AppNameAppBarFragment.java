package com.boruminc.borumjot.android;

import android.graphics.Paint;
import android.os.Build;
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

    /**
     * Sets a strikethrough on the title by adding a background
     * @param on Whether to strikethrough or remove strikethrough
     */
    void displayStrikethrough(boolean on) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (on)
                // Add a strikethrough to the already existing paint flags using "|" bitwise operator
                title.setPaintFlags(title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            else
                // Remove the strikethrough from the paint flags using "&" bitwise operator
                title.setPaintFlags(title.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }
}
