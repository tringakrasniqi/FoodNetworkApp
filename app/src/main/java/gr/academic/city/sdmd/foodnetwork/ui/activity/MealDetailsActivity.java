package gr.academic.city.sdmd.foodnetwork.ui.activity;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import gr.academic.city.sdmd.foodnetwork.R;
import gr.academic.city.sdmd.foodnetwork.db.FoodNetworkContract;
import gr.academic.city.sdmd.foodnetwork.service.MealService;

/**
 * Created by trumpets on 4/13/16.
 */
public class MealDetailsActivity extends ToolBarActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EXTRA_MEAL_ID = "meal_id";

    private static final int MEAL_LOADER = 20;

    private static final String LOG_TAG = "MealDetailsActivity ";

    public static Intent getStartIntent(Context context, long mealId) {
        Intent intent = new Intent(context, MealDetailsActivity.class);
        intent.putExtra(EXTRA_MEAL_ID, mealId);

        return intent;
    }

    private long mealId;

    private TextView tvTitle;
    private TextView tvRecipe;
    private TextView tvNumberOfServings;
    private TextView tvPrepTime;
    private TextView tvCreationDate;
    private TextView tvUpvote;
    private ImageView imgView;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mealId = getIntent().getLongExtra(EXTRA_MEAL_ID, -1);

        tvTitle = (TextView) findViewById(R.id.tv_meal_title);
        tvRecipe = (TextView) findViewById(R.id.tv_meal_recipe);
        tvNumberOfServings = (TextView) findViewById(R.id.tv_number_of_servings);
        tvPrepTime = (TextView) findViewById(R.id.tv_prep_time);
        tvCreationDate = (TextView) findViewById(R.id.tv_meal_creation_date);
        tvUpvote = (TextView) findViewById(R.id.tv_upvote);
        imgView = (ImageView) findViewById(R.id.image);

        getSupportLoaderManager().initLoader(MEAL_LOADER, null, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Cursor c = getContentResolver().query(FoodNetworkContract.Meal.CONTENT_URI, new String[0], FoodNetworkContract.Meal._ID + " = " + mealId, null, null);
        while (c.moveToNext()) {
            getSupportActionBar().setTitle(c.getString(c.getColumnIndexOrThrow(FoodNetworkContract.Meal.COLUMN_TITLE)));
        }
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_meal_details;
    }

    @Override
    protected int getTitleResource() {
        return R.string.meal_details_title;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case MEAL_LOADER:
                return new CursorLoader(this,
                        ContentUris.withAppendedId(FoodNetworkContract.Meal.CONTENT_URI, mealId),
                        null,
                        null,
                        null,
                        null
                );

            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        updateView(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        updateView(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.meal_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        long mealServerId = 0;
        Cursor c = getContentResolver().query(FoodNetworkContract.Meal.CONTENT_URI,
                new String[0],
                FoodNetworkContract.Meal._ID + " = " + mealId,
                null,
                null);

        if (c.moveToFirst()) {
            mealServerId = c.getLong(c.getColumnIndexOrThrow(FoodNetworkContract.Meal.COLUMN_SERVER_ID));
        }
        switch (item.getItemId()) {
            case R.id.action_upvote:
                MealService.startUpvoteMeal(this, mealServerId);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateView(Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {
            tvTitle.setText(cursor.getString(cursor.getColumnIndexOrThrow(FoodNetworkContract.Meal.COLUMN_TITLE)));
            tvRecipe.setText(cursor.getString(cursor.getColumnIndexOrThrow(FoodNetworkContract.Meal.COLUMN_RECIPE)));
            tvNumberOfServings.setText(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(FoodNetworkContract.Meal.COLUMN_NUMBER_OF_SERVINGS))));

            int prepTimeHour = cursor.getInt(cursor.getColumnIndexOrThrow(FoodNetworkContract.Meal.COLUMN_PREP_TIME_HOUR));
            int prepTimeMinute = cursor.getInt(cursor.getColumnIndexOrThrow(FoodNetworkContract.Meal.COLUMN_PREP_TIME_MINUTE));

            tvPrepTime.setText(getString(R.string.prep_time_w_placeholder, prepTimeHour, prepTimeMinute));
            tvCreationDate.setText(dateFormat.format(new Date(cursor.getLong(cursor.getColumnIndexOrThrow(FoodNetworkContract.Meal.COLUMN_CREATED_AT)))));

            startImageDownload(cursor.getString(cursor.getColumnIndexOrThrow(FoodNetworkContract.Meal.COLUMN_PREVIEW)));

            tvUpvote.setText(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(FoodNetworkContract.Meal.COLUMN_MEAL_UPVOTE))));
        }

        if (cursor != null) {
            cursor.close();
        }
    }


    private void startImageDownload(String imageUrl) {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // fetch data
            new DownloadImageTask().execute(imageUrl);
        } else {
            // display error
            Toast.makeText(this, "Could not fetch image", Toast.LENGTH_SHORT).show();
        }

    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadImage(urls[0]);
            } catch (IOException e) {
                return null;
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(Bitmap result) {
            if (result == null) {
                Log.e(LOG_TAG, "Unable to retrieve image. URL may be invalid.");
                return;
            }

            ImageView imageView = (ImageView) findViewById(R.id.image);
            imageView.setImageBitmap(result);
        }
    }

    private Bitmap downloadImage(String imageUrl) throws IOException {
        InputStream is = null;

        try {
            URL url = new URL(imageUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            // Starts the query
            conn.connect();

            int response = conn.getResponseCode();
            Log.d(LOG_TAG, "The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a bitmap
            Bitmap bitmap = BitmapFactory.decodeStream(is);

            return bitmap;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }
}























