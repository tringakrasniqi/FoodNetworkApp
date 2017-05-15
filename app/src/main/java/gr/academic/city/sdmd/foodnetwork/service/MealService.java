package gr.academic.city.sdmd.foodnetwork.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import java.text.MessageFormat;

import gr.academic.city.sdmd.foodnetwork.db.FoodNetworkContract;
import gr.academic.city.sdmd.foodnetwork.domain.Meal;
import gr.academic.city.sdmd.foodnetwork.receiver.TriggerPushToServerBroadcastReceiver;
import gr.academic.city.sdmd.foodnetwork.util.Commons;
import gr.academic.city.sdmd.foodnetwork.util.Constants;
import gr.academic.city.sdmd.foodnetwork.util.GsonResponseCallback;

import static gr.academic.city.sdmd.foodnetwork.util.Commons.executeRequest;

/**
 * Created by trumpets on 4/24/17.
 */
public class MealService extends IntentService {

    private static final String LOG_TAG = "Meal Service";

    private static final String ACTION_FETCH_MEALS = "gr.academic.city.sdmd.foodnetwork.FETCH_MEALS";
    private static final String EXTRA_MEAL_TYPE_SERVER_ID = "meal_type_server_id";

    private static final String ACTION_CREATE_MEAL = "gr.academic.city.sdmd.foodnetwork.CREATE_MEAL";
    private static final String EXTRA_TITLE = "title";
    private static final String EXTRA_RECIPE = "recipe";
    private static final String EXTRA_NUMBER_OF_SERVINGS = "number_of_servings";
    private static final String EXTRA_PREP_TIME_HOUR = "prep_time_hour";
    private static final String EXTRA_PREP_TIME_MINUTE = "prep_time_minute";

    private static final String ACTION_MEAL_UPVOTE = "gr.academic.city.sdmd.foodnetwork.MEAL_UPVOTE";
    private static final String EXTRA_MEAL_ID = "meal_id";

    public static void startFetchMeals(Context context, long mealTypeServerId) {
        Intent intent = new Intent(context, MealService.class);
        intent.setAction(ACTION_FETCH_MEALS);
        intent.putExtra(EXTRA_MEAL_TYPE_SERVER_ID, mealTypeServerId);

        context.startService(intent);
    }

    public static void startCreateMeal(Context context, long mealTypeServerId, String title, String recipe, int numberOfServings, int prepTimeHour, int prepTimeMinute) {
        Intent intent = new Intent(context, MealService.class);
        intent.setAction(ACTION_CREATE_MEAL);
        intent.putExtra(EXTRA_MEAL_TYPE_SERVER_ID, mealTypeServerId);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_RECIPE, recipe);
        intent.putExtra(EXTRA_NUMBER_OF_SERVINGS, numberOfServings);
        intent.putExtra(EXTRA_PREP_TIME_HOUR, prepTimeHour);
        intent.putExtra(EXTRA_PREP_TIME_MINUTE, prepTimeMinute);

        context.startService(intent);
    }

    public static void startUpvoteMeal(Context context, long mealServerId){
        Intent intent = new Intent(context, MealService.class);
        intent.setAction(ACTION_MEAL_UPVOTE);
        intent.putExtra(EXTRA_MEAL_ID, mealServerId);
        context.startService(intent);
    }

    public MealService() {
        super("MealService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (ACTION_FETCH_MEALS.equals(intent.getAction())) {
            fetchMeals(intent);
        } else if (ACTION_CREATE_MEAL.equals(intent.getAction())) {
            createMeal(intent);
        } else if(ACTION_MEAL_UPVOTE.equals(intent.getAction())){
            upvoteMeal(intent);
        }else{
            throw new UnsupportedOperationException("Action not supported: " + intent.getAction());
        }
    }

    private void upvoteMeal(Intent intent){
        long mealServerId = intent.getLongExtra(EXTRA_MEAL_ID, -1);

            Cursor cursor = getContentResolver().query(FoodNetworkContract.Meal.CONTENT_URI,
                new String[0],
                FoodNetworkContract.Meal._ID + " = " + mealServerId,
                null,
                null);

        ContentValues contentValues = new ContentValues();

        while(cursor.moveToNext()){
            contentValues.put(FoodNetworkContract.Meal.COLUMN_MEAL_UPVOTE, cursor.getInt(cursor.getColumnIndexOrThrow(FoodNetworkContract.Meal.COLUMN_MEAL_UPVOTE))+1);
            getContentResolver().update(FoodNetworkContract.Meal.CONTENT_URI, contentValues,
                    FoodNetworkContract.Meal.COLUMN_MEAL_TYPE_SERVER_ID + " = " + mealServerId,
                    null );
            Log.d(LOG_TAG, "contentValues : " +  contentValues);
        }
        Log.d(LOG_TAG, "ID in upvoteMeal ----------------" + mealServerId);

        Intent intent1 = new Intent("mealId.upvote.action");
        intent1.putExtra(EXTRA_MEAL_ID, mealServerId);
        intent1.setAction(TriggerPushToServerBroadcastReceiver.ACTION_TRIGGER_UPVOTE);

        sendBroadcast(intent1);

    }

    private void fetchMeals(Intent intent) {
        long mealTypeServerId = intent.getLongExtra(EXTRA_MEAL_TYPE_SERVER_ID, -1);
        executeRequest(MessageFormat.format(Constants.MEALS_URL, mealTypeServerId), Commons.ConnectionMethod.GET, null, new GsonResponseCallback<Meal[]>(Meal[].class) {
            @Override
            protected void onResponse(int responseCode, Meal[] meals) {

                for (Meal meal : meals) {
                    if (getContentResolver().query(
                            FoodNetworkContract.Meal.CONTENT_URI,
                            new String[0],
                            FoodNetworkContract.Meal.COLUMN_SERVER_ID + " = ?",
                            new String[]{String.valueOf(meal.getServerId())},
                            null).getCount() == 0) {
                        // this meal activity is not in db, add it
                        Log.d(LOG_TAG, "MEALS: " + meal.toContentValues());
                        ContentValues contentValues = meal.toContentValues();
                        contentValues.put(FoodNetworkContract.Meal.COLUMN_UPLOADED_TO_SERVER, 1);

                        Log.d(LOG_TAG, " ---- "+ contentValues);

                        getContentResolver().insert(
                                FoodNetworkContract.Meal.CONTENT_URI,
                                contentValues);
                    }
                }
            }
        });
    }

    private void createMeal(Intent intent) {
        long mealTypeServerId = intent.getLongExtra(EXTRA_MEAL_TYPE_SERVER_ID, -1);
        String title = intent.getStringExtra(EXTRA_TITLE);
        String recipe = intent.getStringExtra(EXTRA_RECIPE);
        int numberOfServings = intent.getIntExtra(EXTRA_NUMBER_OF_SERVINGS, -1);
        int prepTimeHour = intent.getIntExtra(EXTRA_PREP_TIME_HOUR, -1);
        int prepTimeMinute = intent.getIntExtra(EXTRA_PREP_TIME_MINUTE, -1);

        ContentValues contentValues = new Meal(title, recipe, numberOfServings, prepTimeHour, prepTimeMinute, mealTypeServerId).toContentValues();
        contentValues.put(FoodNetworkContract.Meal.COLUMN_UPLOADED_TO_SERVER, 0);
        contentValues.put(FoodNetworkContract.Meal.COLUMN_SERVER_ID, -1);

        getContentResolver().insert(
                FoodNetworkContract.Meal.CONTENT_URI,
                contentValues
        );

        sendBroadcast(new Intent(TriggerPushToServerBroadcastReceiver.ACTION_TRIGGER));
    }
}
