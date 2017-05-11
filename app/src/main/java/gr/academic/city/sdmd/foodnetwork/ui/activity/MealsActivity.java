package gr.academic.city.sdmd.foodnetwork.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import gr.academic.city.sdmd.foodnetwork.R;
import gr.academic.city.sdmd.foodnetwork.db.FoodNetworkContract;
import gr.academic.city.sdmd.foodnetwork.service.MealService;
import gr.academic.city.sdmd.foodnetwork.service.MealTypeService;
import gr.academic.city.sdmd.foodnetwork.ui.fragment.MealDetailsFragment;
import gr.academic.city.sdmd.foodnetwork.ui.fragment.MealListFragment;

/**
 * Created by trumpets on 4/24/17.
 */
public class MealsActivity extends ToolBarActivity implements LoaderManager.LoaderCallbacks<Cursor>, MealDetailsFragment.OnFragmentInteractionListener {

    private static final String EXTRA_MEAL_TYPE_SERVER_ID = "meal_type_server_id";
    private static final String TAG_LOG = "MEALS ACTIVITY";
    private static final String[] PROJECTION = {
            FoodNetworkContract.Meal._ID,
            FoodNetworkContract.Meal.COLUMN_TITLE,
            FoodNetworkContract.Meal.COLUMN_PREP_TIME_HOUR,
            FoodNetworkContract.Meal.COLUMN_PREP_TIME_MINUTE,
            FoodNetworkContract.Meal.COLUMN_MEAL_UPVOTE};

    private static final String SORT_ORDER = FoodNetworkContract.Meal.COLUMN_MEAL_UPVOTE + " DESC";

    private static final int MEALS_LOADER = 10;

    private final static String[] FROM_COLUMNS = {
            FoodNetworkContract.Meal.COLUMN_TITLE,
            FoodNetworkContract.Meal.COLUMN_PREP_TIME_HOUR,
            FoodNetworkContract.Meal.COLUMN_MEAL_UPVOTE};

    private final static int[] TO_IDS = {
            R.id.tv_meal_title,
            R.id.tv_meal_prep_time,
            R.id.tv_meal_upvote};

    public static Intent getStartIntent(Context context, long mealTypeServerId) {
        Intent intent = new Intent(context, MealsActivity.class);
        intent.putExtra(EXTRA_MEAL_TYPE_SERVER_ID, mealTypeServerId);

        return intent;
    }

    private long mealTypeServerId;
    private CursorAdapter adapter;

    private SwipeRefreshLayout swipeRefreshLayout;

    private int activityBrequestCode = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.mealTypeServerId = getIntent().getLongExtra(EXTRA_MEAL_TYPE_SERVER_ID, -1);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initiateMealsRefresh();
            }
        });

        adapter = new SimpleCursorAdapter(this, R.layout.item_meal, null, FROM_COLUMNS, TO_IDS, 0);
        ((SimpleCursorAdapter) adapter).setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {

                TextView textViewUpvote = (TextView) view;
                textViewUpvote.setText(getString(
                        R.string.meal_upvote_w_placeholder,
                        cursor.getInt(cursor.getColumnIndexOrThrow(FoodNetworkContract.Meal.COLUMN_MEAL_UPVOTE))));

                if (columnIndex == cursor.getColumnIndexOrThrow(FoodNetworkContract.Meal.COLUMN_PREP_TIME_HOUR) && view instanceof TextView) {
                    // we have to build a human readable string of prep time

                    TextView textView = (TextView) view;
                    textView.setText(getString(
                            R.string.prep_time_w_placeholder,
                            cursor.getInt(columnIndex),  // we know this is prep time hour
                            cursor.getInt(cursor.getColumnIndexOrThrow(FoodNetworkContract.Meal.COLUMN_PREP_TIME_MINUTE))));

                    return true;
                } else {
                    return false;
                }


            }
        });

        ListView resultsListView = (ListView) findViewById(android.R.id.list);
        resultsListView.setAdapter(adapter);
        resultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = adapter.getCursor();
                if (cursor.moveToPosition(position)) {
                    startActivity(MealDetailsActivity.getStartIntent(MealsActivity.this,
                            cursor.getLong(cursor.getColumnIndexOrThrow(FoodNetworkContract.Meal._ID))));
                }
            }
        });

        getSupportLoaderManager().initLoader(MEALS_LOADER, null, this);

        MealService.startFetchMeals(this, mealTypeServerId);



    }

    @Override
    protected int getContentView() {
        return R.layout.activity_meals;
    }

    @Override
    protected int getTitleResource() {
        return R.string.meals_title;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        Cursor c = getContentResolver().query(FoodNetworkContract.MealType.CONTENT_URI, new String[0], FoodNetworkContract.MealType._ID + " = " + mealTypeServerId, null, null );
        while(c.moveToNext()){
            getSupportActionBar().setTitle(c.getString(c.getColumnIndexOrThrow(FoodNetworkContract.MealType.COLUMN_NAME)));
        }

        Log.d(TAG_LOG, "log: " + mealTypeServerId);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == activityBrequestCode && resultCode == RESULT_OK){

            this.mealTypeServerId = data.getLongExtra(EXTRA_MEAL_TYPE_SERVER_ID, -1);

        }
        Log.d(TAG_LOG, "onActivityResult: " + mealTypeServerId);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                startActivity(CreateMealActivity.getStartIntent(MealsActivity.this, mealTypeServerId));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case MEALS_LOADER:
                return new CursorLoader(this,
                        FoodNetworkContract.Meal.CONTENT_URI,
                        PROJECTION,
                        FoodNetworkContract.Meal.COLUMN_MEAL_TYPE_SERVER_ID + " = ?",
                        new String[]{String.valueOf(mealTypeServerId)},
                        SORT_ORDER
                );

            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.changeCursor(null);
    }

    private void initiateMealsRefresh() {
        if (!swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(true);
        }

        new FetchMealsAsyncTask().execute(mealTypeServerId);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private class FetchMealsAsyncTask extends AsyncTask<Long, Void, Void> {

        @Override
        protected Void doInBackground(Long... params) {
            MealService.startFetchMeals(MealsActivity.this, params[0]);

            try {
                // giving the service ample time to finish
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}
