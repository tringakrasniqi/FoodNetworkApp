package gr.academic.city.sdmd.foodnetwork;


import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.widget.TimePicker;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import gr.academic.city.sdmd.foodnetwork.ui.activity.CreateMealActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class CreateMealTest {

    @Rule
    public ActivityTestRule<CreateMealActivity> mActivityRule = new ActivityTestRule<>(CreateMealActivity.class);
    @Test
    public void createNewMeal() {
        String breakfast="Eggs";
        String recipe="2 eggs";
        String noServings="4";


        onView(withId(R.id.txt_meal_title)).perform(typeText(breakfast), closeSoftKeyboard());
        onView(withId(R.id.txt_recipe)).perform(typeText(recipe), closeSoftKeyboard());
        onView(withId(R.id.txt_number_of_servings)).perform(typeText(noServings), closeSoftKeyboard());
        onView(withId(R.id.tv_prep_time)).perform(click());

        ViewInteraction perform = onView(isAssignableFrom(TimePicker.class)).perform(setTime(0,15));
        onView(withText("OK")).perform(click());

        onView(withClassName(Matchers.equalTo(TimePicker.class.getName())));
        onView(withId(R.id.btn_ok)).perform(click());

    }

public static ViewAction setTime(final int hour, final int minute) {
    return new ViewAction() {
        @Override
        public void perform(UiController uiController, View view) {
            TimePicker tp = (TimePicker) view;
            tp.setCurrentHour(hour);
            tp.setCurrentMinute(minute);
        }

        @Override
        public String getDescription() {
            return "Set the passed time into the TimePicker";
        }

        @Override
        public Matcher<View> getConstraints() {
            return isAssignableFrom(TimePicker.class);
        }
    };

}}


