package tz.co.neelansoft.diary;

import android.content.res.Resources;
import android.support.test.espresso.intent.Intents;
import android.support.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.closeSoftKeyboard;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.Intents.release;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by landre on 30/06/2018.
 */

public class SignupActivityTest {
    private Resources resources;
    @Rule
    public final ActivityTestRule<SignupActivity> mSignupActivityTestRule = new ActivityTestRule<>(SignupActivity.class);



    @Test
    public void clickSignupSubmitButtonFailure() throws Exception{

        resources = mSignupActivityTestRule.getActivity().getResources();

        onView(withId(R.id.etSignupPassword)).perform(typeText("password2"));
        onView(withId(R.id.etSignupName)).perform(typeText("Landry Kapela"));
        onView(withId(R.id.etSignupEmail)).perform(typeText("landrykapela@gmail.com"));
        closeSoftKeyboard();


        onView(withId(R.id.btnSignupSubmit))
                .perform(click());

        onView(withId(R.id.tvSignupError)).check(matches(withText(resources.getString(R.string.signup_error))));

    }


    @Test
    public void clickSigninButton() throws Exception{
        Intents.init();
        onView(withId(R.id.btnSignupSigninButton)).perform(click());

        intended(hasComponent("tz.co.neelansoft.diary.SigninActivity"));
        release();
    }

    @Test
    public void clickSignupSubmitButtonSuccess() throws Exception{
        Intents.init();
        resources = mSignupActivityTestRule.getActivity().getResources();

        onView(withId(R.id.etSignupPassword)).perform(typeText("password2"));
        onView(withId(R.id.etSignupName)).perform(typeText("Landry Kapela"));
        onView(withId(R.id.etSignupEmail)).perform(typeText("lan_dre@outlook.com"));
        closeSoftKeyboard();

        onView(withId(R.id.btnSignupSubmit))
                .perform(click());

       // onView(withId(R.id.tvSignupError)).check(matches(withText(resources.getString(R.string.signup_error))));
        intended(hasComponent("tz.co.neelansoft.diary.JournalEntriesActivity"));
        release();
    }

}
