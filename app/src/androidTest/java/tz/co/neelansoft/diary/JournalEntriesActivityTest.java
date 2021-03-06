package tz.co.neelansoft.diary;

import android.content.Context;
import android.content.res.Resources;
import android.support.test.espresso.intent.Intents;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import tz.co.neelansoft.Utils.DiaryPreferenceUtils;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by landre on 01/07/2018.
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class JournalEntriesActivityTest {
    @Rule
    public final ActivityTestRule<JournalEntriesActivity> mJournalEntriesActivityRule =
            new ActivityTestRule(JournalEntriesActivity.class);


    @Before
    public void init(){
        Resources resources = mJournalEntriesActivityRule.getActivity().getResources();


    }
    @Test
    public void clickRecyclerViewItem() throws Exception{
        Intents.init();
        final Context context = mJournalEntriesActivityRule.getActivity().getApplicationContext();
        final DiaryPreferenceUtils dPreference = new DiaryPreferenceUtils(context);
        dPreference.setUserName("Landry Kapela");
        dPreference.setUserId("84THLUBmfBNnSd6OPqOZjaz88HG3");

        final int TEST_ITEM_POSITION = 0;
        final String EXTRA_ENTRY_KEY = "entry_id";
        final int TEST_ITEM_VALUE = 3;

        onView(withId(R.id.recyclerView1)).perform(actionOnItemAtPosition(TEST_ITEM_POSITION, click()));

        intended(hasExtra(EXTRA_ENTRY_KEY,TEST_ITEM_VALUE));

    }
}
