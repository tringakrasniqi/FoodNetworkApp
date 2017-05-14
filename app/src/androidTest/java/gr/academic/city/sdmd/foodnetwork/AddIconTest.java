package gr.academic.city.sdmd.foodnetwork;

import android.support.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;

import gr.academic.city.sdmd.foodnetwork.ui.activity.MealsActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;


public class AddIconTest {

    @Rule
    public ActivityTestRule<MealsActivity> activityTestRule = new ActivityTestRule<>(MealsActivity.class);

    @Test
    public void testClickInsertItem() {
        onView(withId(R.id.action_add)).perform(click());
    }

}
