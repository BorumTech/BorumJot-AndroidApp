package com.boruminc.borumjot.android;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class HelpActivityTest {
    @Rule
    public ActivityScenarioRule rule = new ActivityScenarioRule<>(HelpActivity.class);

    @Test
    public void testShowsCurrentBuildVersion() {
        ActivityScenario.launch(HelpActivity.class);

        PackageManager pm = ApplicationProvider.getApplicationContext().getPackageManager();

        try {
            PackageInfo info = pm.getPackageInfo("com.boruminc.borumjot.android", PackageManager.GET_ACTIVITIES);
            String expectedVersion = info.versionName;
            onView(withId(R.id.version_number)).check(((view, noViewFoundException) -> {
                assertThat("The shown version number is behind", ((TextView) view).getText(), is("Version " + expectedVersion));
            }));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}
