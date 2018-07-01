package tz.co.neelansoft.diary;

import android.content.res.Resources;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertTrue;

/**
 * Created by landre on 01/07/2018.
 */
@RunWith(AndroidJUnit4.class)
public class FirebaseBackupActivityTest {
    private Resources resources;


    @Rule
    public ActivityTestRule<FirebaseBackupActivity> mFirebaseBackupActivityRule = new ActivityTestRule<>(FirebaseBackupActivity.class);

    @Before
    public void init(){
        resources = mFirebaseBackupActivityRule.getActivity().getResources();
    }

    @Test
    public void clickCancelButtom() throws Exception{
        onView(withId(R.id.cancel_backup_button)).perform(click());

       assertTrue(mFirebaseBackupActivityRule.getActivity().isFinishing());
    }
    @Test
    public void clickUploadImage() throws Exception{

        onView(withId(R.id.ivCloudBuckup)).perform(click());

        String checkText = resources.getString(R.string.dialog_backup_confirmation);

        onView(withText(checkText)).check(matches(isDisplayed()));
    }

}
