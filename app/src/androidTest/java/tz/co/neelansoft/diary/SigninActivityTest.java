package tz.co.neelansoft.diary;

import android.content.Intent;
import android.content.res.Resources;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.closeSoftKeyboard;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.ComponentNameMatchers.hasClassName;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import android.support.test.espresso.intent.matcher.IntentMatchers;

/**
 * Created by landre on 30/06/2018.
 */
@RunWith(AndroidJUnit4.class)
public class SigninActivityTest {
private Resources resources;
    @Rule
    public ActivityTestRule<SigninActivity> mSigninActivityTestRule = new ActivityTestRule<SigninActivity>(SigninActivity.class);


    @Before
    public void init(){
        resources = mSigninActivityTestRule.getActivity().getResources();
/*        onView(withId(R.id.etSignInEmail)).perform(typeText("lan_dre@outlook.com"));
        onView(withId(R.id.etSignInPassword)).perform(typeText("password"));
        closeSoftKeyboard();*/
    }
/*
    @Test
    public void clickSigninWithEmailAndPasswordButton() throws Exception{

        onView(withId(R.id.btnSignInWithEmailAndPassword))
                .perform(click());

        onView(withId(R.id.tvSignInError)).check(matches(withText(resources.getString(R.string.signin_error))));


    }
*/
    @Test
    public void clickSignupWithEmailAndPasswordButton(){
        onView(withId(R.id.btnSignupWithEmailAndPassword)).perform(click());

        intended(hasComponent("tz.co.neelansoft.diary.SignupActivity"));
    }


}
