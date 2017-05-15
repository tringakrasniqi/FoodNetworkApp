package gr.academic.city.sdmd.foodnetwork.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
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
public class MealsActivity extends ToolBarActivity  implements MealListFragment.OnFragmentInteractionListener{

    private static final String EXTRA_MEAL_TYPE_SERVER_ID = "meal_type_server_id";
    private static final String TAG_LOG = "MEALS ACTIVITY";

    public static Intent getStartIntent(Context context, long mealTypeServerId) {
        Intent intent = new Intent(context, MealsActivity.class);
        intent.putExtra(EXTRA_MEAL_TYPE_SERVER_ID, mealTypeServerId);
        return intent;
    }

    private long mealTypeServerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.mealTypeServerId = getIntent().getLongExtra(EXTRA_MEAL_TYPE_SERVER_ID, -1);

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
            MealListFragment firstFragment = MealListFragment.newInstance();

            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            firstFragment.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, firstFragment).commit();
        }

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

        Cursor c = getContentResolver().query(FoodNetworkContract.MealType.CONTENT_URI,
                new String[0],
                FoodNetworkContract.MealType._ID + " = " + mealTypeServerId,
                null,
                null );
        while(c.moveToNext()){
            getSupportActionBar().setTitle(c.getString(c.getColumnIndexOrThrow(FoodNetworkContract.MealType.COLUMN_NAME)));
        }

        Log.d(TAG_LOG, "log: " + mealTypeServerId);

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
    public void onMealItemSelected(long id) {
        View fragmentContainer = findViewById(R.id.fragment_container);
        boolean isDualPane = fragmentContainer != null &&
                fragmentContainer.getVisibility() == View.VISIBLE;
        Log.d(TAG_LOG, ""+ mealTypeServerId);
        Log.d(TAG_LOG, ""+isDualPane);
        if (isDualPane) { //TODO:MEAL DETAILS FRAGMENT
//            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
//            fragmentTransaction.replace(R.id.fragment_container, MealDetailsFragment.newInstance(mealTypeServerId));
//            fragmentTransaction.commit();
        } else {
            startActivity(MealDetailsActivity.getStartIntent(this, mealTypeServerId));
        }
    }
}
