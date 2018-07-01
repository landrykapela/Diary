package tz.co.neelansoft.diary;

import android.content.res.Resources;
import android.support.test.espresso.intent.Intents;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.closeSoftKeyboard;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertTrue;

/**
 * Created by landre on 01/07/2018.
 */
@RunWith(AndroidJUnit4.class)
public class AddEntryActivityTest {

    private Resources resources;
    @Rule
    public ActivityTestRule<AddEntryActivity> mAddEntryActivityTestRule =
            new ActivityTestRule<>(AddEntryActivity.class);

    @Test
    public void clickSaveButtonTest(){
        resources = mAddEntryActivityTestRule.getActivity().getResources();

        onView(withId(R.id.etTitle)).perform(typeText("Mood Title"));
        closeSoftKeyboard();
        onView(withId(R.id.etThoughts)).perform(typeText(resources.getString(R.string.sample_thought)));

        closeSoftKeyboard();


        onView(withId(R.id.btnSave))
                .perform(click());

        assertTrue(mAddEntryActivityTestRule.getActivity().isFinishing());

    }

    @Test
    public void clickCancelButtonTest() throws Exception{
        closeSoftKeyboard();
        onView(withId(R.id.btnDiscard)).perform(click());

        assertTrue(mAddEntryActivityTestRule.getActivity().isFinishing());
    }

}
