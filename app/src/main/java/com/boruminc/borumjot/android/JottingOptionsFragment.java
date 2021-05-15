package com.boruminc.borumjot.android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.boruminc.borumjot.Jotting;
import com.boruminc.borumjot.Task;
import com.boruminc.borumjot.Note;
import com.boruminc.borumjot.android.customviews.SerializableImage;
import com.boruminc.borumjot.android.server.ApiRequestExecutor;
import com.boruminc.borumjot.android.server.ApiResponseExecutor;
import com.boruminc.borumjot.android.server.TaskRunner;
import com.boruminc.borumjot.android.server.requests.DeleteJottingRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

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
                return true;
            case R.id.delete_btn: {

                if (getJotData() instanceof Task) {
                    AlertDialog.Builder builder = buildDeleteJottingDialog("task");
                    builder.setPositiveButton("Delete", (dialog, which) -> {
                        deleteJotting("task");
                    });
                    builder.create().show();

                } else if (getJotData() instanceof Note) {
                    AlertDialog.Builder builder = buildDeleteJottingDialog("note");
                    builder.setPositiveButton("Delete", (dialog, which) -> {
                        deleteJotting("note");
                    });
                    builder.create().show();
                }

                return true;
            }
            case R.id.exit_jotting_options_btn:
                Toolbar normalToolbar = requireActivity().findViewById(R.id.my_toolbar);
                ((AppCompatActivity) requireActivity()).setSupportActionBar(normalToolbar);

                if (normalToolbar != null) {
                    root.setVisibility(View.GONE);
                    normalToolbar.setVisibility(View.VISIBLE);
                }

                View pin = (SerializableImage) requireArguments().getSerializable("view");

                assert pin != null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ((View) pin.getParent()).setBackground(
                            getResources().getDrawable(
                                    R.drawable.orange_border,
                                    Objects.requireNonNull(getActivity()).getTheme()
                            )
                    );
                }
                return true;
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

    private AlertDialog.Builder buildDeleteJottingDialog(String jotType) {
        android.app.AlertDialog.Builder deleteDialog = new android.app.AlertDialog.Builder(getActivity());
        deleteDialog
                .setTitle("Delete Task")
                .setMessage("Are you sure you would like to delete this task?")
                .setNegativeButton("Cancel", (dialog, which) -> {});
        return deleteDialog;
    }

    private void deleteJotting(String jotType) {
        new TaskRunner().executeAsync(
                new DeleteJottingRequest(getJotData().getId(), userApiKey, jotType),
                data -> {
                    try {
                        if (data != null) {
                            if (data.has("error") || data.getInt("statusCode") == 500) {
                                Toast.makeText(getActivity(), "The " + jotType + " could not be deleted due to a system error", Toast.LENGTH_SHORT).show();
                            } else {
                                startActivity(new Intent(getActivity(), HomeActivity.class));
                                Toast.makeText(getActivity(), "Task deleted", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "The " + jotType + " could not be deleted due to a system error", Toast.LENGTH_LONG).show();
                    }
                }
        );
    }
}
