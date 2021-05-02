package com.boruminc.borumjot.android;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.boruminc.borumjot.Jotting;
import com.boruminc.borumjot.android.customviews.SwipableTextView;
import com.boruminc.borumjot.android.server.ApiRequestExecutor;
import com.boruminc.borumjot.android.server.ApiResponseExecutor;
import com.boruminc.borumjot.android.server.JSONToModel;
import com.boruminc.borumjot.android.server.TaskRunner;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;

public class ShareActivity extends FragmentActivity {
    String userApiKey;
    private Jotting jottingData;
    private String jotType;

    LinearLayout shareesList;

    /* Overriden Framework Callback Methods */

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.share_activity);

        AppBarFragment shareAppBar = ((AppBarFragment) getSupportFragmentManager().findFragmentById(R.id.share_appbar));
        if (shareAppBar != null) shareAppBar.passTitle("Share");

        userApiKey = getSharedPreferences("user identification", Context.MODE_PRIVATE).getString("apiKey", "");
        jottingData = (Jotting) getIntent().getSerializableExtra("jotting");
        Log.d("jottingData", String.valueOf(jottingData));

        shareesList = findViewById(R.id.current_sharees_list);
        jotType = getIntent().getStringExtra("jotType");
    }

    @Override
    protected void onResume() {
        super.onResume();
        new TaskRunner().executeAsync(getJotShareesRequest(), getJotShareesResponse());
    }

    /* Request and Responses */

    private ApiRequestExecutor getShareRequest(int id, String email) {
        return new ApiRequestExecutor(String.valueOf(id), email) {
            @Override
            protected void initialize() {
                super.initialize();
                setRequestMethod("POST");
                setQuery(encodePostQuery("id=%s&email=%s"));
                addAuthorizationHeader(userApiKey);
            }

            @Override
            public JSONObject call() {
                super.call();
                return this.connectToApi(encodeQueryString(jotType + "/share"));
            }
        };
    }

    private ApiResponseExecutor getShareResponse() {
        return new ApiResponseExecutor() {
            @Override
            public void onComplete(JSONObject result) {
                super.onComplete(result);
                if (ranOk()) {
                    finish();
                    Toast.makeText(getApplicationContext(), jotType + " successfully shared!", Toast.LENGTH_SHORT).show();
                } else if (result == null) {
                    Toast.makeText(getApplicationContext(), "The " + jotType + " could not be shared because of an unknown server error", Toast.LENGTH_LONG).show();
                } else {
                    String message = "";
                    try {
                        message = "The " + jotType + " could not be shared because " + result.getJSONObject("error").getString("message").toLowerCase();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        message = "The " + jotType + " could not be shared for an unknown reason";
                    } finally {
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                    }
                }
            }
        };
    }

    private ApiRequestExecutor getJotShareesRequest() {
        return new ApiRequestExecutor() {
            @Override
            protected void initialize() {
                super.initialize();
                setRequestMethod("GET");
                addAuthorizationHeader(userApiKey);
            }

            @Override
            public JSONObject call() {
                super.call();
                return this.connectToApi(encodeQueryString(jotType + "/share", "id=" + jottingData.getId()));
            }
        };
    }

    private ApiResponseExecutor getJotShareesResponse() {
        return new ApiResponseExecutor() {
            @Override
            public void onComplete(JSONObject result) {
                super.onComplete(result);
                if (ranOk()) {
                    try {
                        setJotSharees(JSONToModel.convertJSONToUserEmails(result.getJSONArray("data")));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "A system error occurred", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "A server error occurred", Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    private ApiRequestExecutor getShareeRemoveRequest(String email) {
        return new ApiRequestExecutor(String.valueOf(jottingData.getId()), email) {
            @Override
            protected void initialize() {
                super.initialize();
                setRequestMethod("DELETE");
                addAuthorizationHeader(userApiKey);
                setQuery(encodePostQuery("id=%s&email=%s"));
            }

            @Override
            public JSONObject call() {
                super.call();
                return this.connectToApi(encodeQueryString(jotType + "/share"));
            }
        };
    }

    private ApiResponseExecutor getShareeRemoveResponse() {
        return new ApiResponseExecutor() {
            @Override
            public void onComplete(JSONObject result) {
                super.onComplete(result);

            }
        };
    }

    /* UI Getters */

    private HashSet<String> getDisplayedJotSharees() {
        HashSet<String> displayedSharees = new HashSet<>();

        for (int i = 0; i < shareesList.getChildCount(); i++) {
            TextView row = (TextView) shareesList.getChildAt(i);
            displayedSharees.add(row.getText().toString());
        }

        return displayedSharees;
    }

    /* UI Setters And Adders */

    /**
     * Sets the jot sharees in the jottingData and in the UI.
     * Calls the <code>addSharee(String)</code> method
     * @param emails The list without duplicates of the emails of the users who received the jot
     */
    private void setJotSharees(HashSet<String> emails) {
        // If nothing has changed, exit the method
        if (jottingData.getSharees().equals(emails) && getDisplayedJotSharees().equals(emails)) {
            return;
        }

        jottingData.setSharees(emails);
        shareesList.removeAllViews();

        for (String email : emails) {
            shareesList.addView(addSharee(email));
        }
    }

    private TextView addSharee(String email) {
        SwipableTextView emailAddressView = new SwipableTextView(this);

        emailAddressView.setText(email);

        // Center each row
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        layoutParams.topMargin = 20;

        emailAddressView.setLayoutParams(layoutParams);
        emailAddressView.setOnDragListener((v, event) -> true);
        emailAddressView.setTextSize(24);

        // Get size of display and store in Point object
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);

        emailAddressView.setOnSwipeTouchListener(new OnSwipeTouchListener(this, point.x) {
            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                onSwipe();
            }

            @Override
            public void onSwipeRight() {
                super.onSwipeRight();
                onSwipe();
            }

            private void onSwipe() {
                TranslateAnimation animation = new TranslateAnimation(0.0f, point.x, 0.0f, 0.0f);
                animation.setDuration(500);
                animation.setFillAfter(true);
                emailAddressView.startAnimation(animation);

                new TaskRunner().executeAsync(getShareeRemoveRequest(email), getShareeRemoveResponse());
            }
        });

        return emailAddressView;
    }

    /* Event Handlers */

    public void onShareClick(View view) {
        EditText emailView = findViewById(R.id.share_email_field);

        String userEmail = getSharedPreferences("user identification", Context.MODE_PRIVATE)
                .getString("email", "");
        if (userEmail.isEmpty()) {
            Toast.makeText(getApplicationContext(), "An error occurred", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userEmail.equals(emailView.getText().toString())) {
            Toast.makeText(getApplicationContext(), "You cannot share with yourself", Toast.LENGTH_SHORT).show();
            return;
        }

        new TaskRunner().executeAsync(
                getShareRequest(jottingData.getId(), emailView.getText().toString()),
                getShareResponse()
        );
    }
}
