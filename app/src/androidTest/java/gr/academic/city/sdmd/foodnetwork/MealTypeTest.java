package gr.academic.city.sdmd.foodnetwork;


import android.support.test.rule.ActivityTestRule;

import org.junit.Rule;

import gr.academic.city.sdmd.foodnetwork.db.FoodNetworkContract;
import gr.academic.city.sdmd.foodnetwork.ui.activity.MainActivity;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.CursorMatchers.withRowString;


public class MealTypeTest {
    @Rule
    public ActivityTestRule<MainActivity> activityTestRule = new ActivityTestRule<>(MainActivity.class);

    @org.junit.Test
    public void clickProperName() {
        onData(withRowString(FoodNetworkContract.MealType.COLUMN_NAME, "Breakfast")).perform(click());

}}




