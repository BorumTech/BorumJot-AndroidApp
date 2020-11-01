package com.boruminc.borumjot.android;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.boruminc.borumjot.Jotting;
import com.boruminc.borumjot.Task;
import com.boruminc.borumjot.Note;
import com.boruminc.borumjot.android.customviews.SerializableImage;
import com.boruminc.borumjot.android.server.ApiRequestExecutor;
import com.boruminc.borumjot.android.server.TaskRunner;

import org.json.JSONObject;

public class JottingOptionsFragment extends Fragment {
    private View root;
    private String userApiKey;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.jotting_options_fragment, container, false);
        setHasOptionsMenu(true);

        // Get/set the user api key from the preferences because it is otherwise inaccessible
        userApiKey = requireActivity().getSharedPreferences("user identification", Context.MODE_PRIVATE)
                .getString("apiKey", "");

        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        if (root.findViewById(R.id.jotting_options_toolbar).getVisibility() == View.VISIBLE) {
            inflater.inflate(R.menu.jotting_options_menu, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.pin_btn:
                new TaskRunner().executeAsync(getPinRequest(), data -> {
                    if (data != null && data.optInt("statusCode") == 200) {
                        // Set priority to new value
                        getJotData().setPriority(getJotData().getPriority() >= 1 ? 0 : 1);

                        View pin = (SerializableImage) requireArguments().getSerializable("view");
                        assert pin != null;
                        // Reverse visibility of the pin
                        pin.setVisibility(getJotData().getPriority() == 1 ? View.VISIBLE : View.INVISIBLE);
                    }
                });
            break;
            case R.id.exit_jotting_options_btn:
                Toolbar normalToolbar = requireActivity().findViewById(R.id.my_toolbar);
                ((AppCompatActivity) requireActivity()).setSupportActionBar(normalToolbar);

                if (normalToolbar != null) {
                    root.setVisibility(View.GONE);
                    normalToolbar.setVisibility(View.VISIBLE);
                }
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    private Jotting getJotData() {
        return (Jotting) requireArguments().getSerializable("data");
    }

    private ApiRequestExecutor getPinRequest() {
        return new ApiRequestExecutor() {
            @Override
            protected void initialize() {
                super.initialize();
                setRequestMethod("PUT");
                addAuthorizationHeader(userApiKey);
            }

            @Override
            public JSONObject call() {
                super.call();

                String[] params = new String[2];
                params[0] = "id=" + getJotData().getId();
                params[1] = "priority=" + (getJotData().getPriority() == 0 ? 1 : 0);

                if (getJotData() instanceof Task) {
                    return this.connectToApi(this.encodeQueryString("task", params));
                } else if (getJotData() instanceof Note) {
                    return this.connectToApi(this.encodeQueryString("note", params));
                }

                return null;
            }
        };
    }
}
