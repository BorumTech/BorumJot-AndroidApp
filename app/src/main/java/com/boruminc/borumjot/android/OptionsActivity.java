package com.boruminc.borumjot.android;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.boruminc.borumjot.android.server.ApiRequestExecutor;
import com.boruminc.borumjot.android.server.ApiResponseExecutor;
import com.boruminc.borumjot.android.server.TaskRunner;

import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class OptionsActivity extends OptionsMenuItemActivity {
    String userApiKey;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.options_activity);

        AppBarFragment frag = (AppBarFragment) getSupportFragmentManager().findFragmentById(R.id.appbar);
        if (frag != null) frag.passTitle("Options");

        userApiKey = getSharedPreferences("user identification", Context.MODE_PRIVATE).getString("apiKey", "");
    }

    /**
     * Logs the user out
     * @param view The button that triggered this click event
     */
    public void onLogoutClick(View view) {
        getSharedPreferences("user identification", Context.MODE_PRIVATE).edit().clear().apply();
        startActivity(new Intent(this, LoginActivity.class));
        Toast.makeText(this, "Logged out successfully!", Toast.LENGTH_SHORT).show();
    }

    /**
     * Opens the Meta Borum "Borum Jot" Topic page in user's browser
     * @param view The button that triggered this click event
     */
    public void onForumClick(View view) {
        Intent borumIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.forum_link)));
        startActivity(borumIntent);
    }

    /**
     * Exports the user's data to a txt file in the downloads folder
     * @param view The button that triggered this click event
     */
    public void onExportDataClick(View view) {
        new TaskRunner().executeAsync(
                new ApiRequestExecutor() {
                    @Override
                    protected void initialize() {
                        super.initialize();
                        setRequestMethod("GET");
                        addAuthorizationHeader(userApiKey);
                    }

                    @Override
                    public JSONObject call() {
                        super.call();
                        return this.connectToApi(encodeQueryString("jottings"));
                    }
                }, new ApiResponseExecutor() {
                    @Override
                    public void onComplete(JSONObject result) {
                        super.onComplete(result);
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            File file = new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "borumjotdata.txt");
                            try (FileWriter fileWriter = new FileWriter(file)) {
                                // Write JSONObject to file
                                fileWriter.write(result.toString());

                                // Use DownloadManager to add file to downloads
                                DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                                assert downloadManager != null;
                                downloadManager.addCompletedDownload(file.getName(), file.getName(), true, "text/plain", file.getAbsolutePath(),file.length(),true);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Your phone is too old to export the data. Try on the web app. ", Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );
    }

    /**
     * Changes the user's Borum account sign in
     * @param view The button that triggered this click event
     */
    public void onChangeSignInClick(View view) {
        startActivity(new Intent(this, ChangeSignInActivity.class));
    }
}
