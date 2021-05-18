package com.boruminc.borumjot.android;

import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

public class AppBarFragment extends Fragment {
    private TextView title;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.appbar_appname_fragment, container, false);

        title = root.findViewById(R.id.appbar_title);

        return root;
    }

    void passTitle(String titleTxt) {
        title.setText(titleTxt);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    void passTitleLeftAligned(String titleTxt) {
        title.setText(titleTxt);
        RelativeLayout.LayoutParams layoutParams =
                (RelativeLayout.LayoutParams)title.getLayoutParams();
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, 0);
        title.setLayoutParams(layoutParams);
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
