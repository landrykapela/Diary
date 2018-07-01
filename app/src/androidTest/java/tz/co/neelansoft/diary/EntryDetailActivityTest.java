package tz.co.neelansoft.diary;

import android.content.res.Resources;
import android.support.test.espresso.intent.Intents;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by landre on 01/07/2018.
 */
@RunWith(AndroidJUnit4.class)
public class EntryDetailActivityTest {
    private Resources resources;
    @Rule
    public ActivityTestRule<EntryDetailActivity> mEntryDetailActivityRule = new ActivityTestRule<>(EntryDetailActivity.class);

    @Before
    public void init(){
        resources = mEntryDetailActivityRule.getActivity().getResources();
    }

    @Test
    public void clickEditEntryFloatingActionButton() throws Exception{
        Intents.init();

        intended(hasComponent("tz.co.neelansoft.diary.AddEntryActivity"));

    }
}
