package gr.academic.city.sdmd.foodnetwork.ui.fragment;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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


public class MealDetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int MEAL_LOADER = 20;
    private static final String ARG_MEAL_ID = "arg_meal_id";

    private TextView tvTitle;
    private TextView tvRecipe;
    private TextView tvNumberOfServings;
    private TextView tvPrepTime;
    private TextView tvCreationDate;
    private TextView tvUpvote;
    private ImageView imgView;
    private Button upvote_button;

    private long mealId;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    private OnFragmentInteractionListener mListener;

    public MealDetailsFragment() {
    }

    public static MealDetailsFragment newInstance(long mealId) {
        MealDetailsFragment fragment = new MealDetailsFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_MEAL_ID, mealId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getArguments() != null) {
            mealId = getArguments().getLong(ARG_MEAL_ID, -1);
        }

        tvTitle = (TextView) getActivity().findViewById(R.id.tv_meal_title);
        tvRecipe = (TextView) getActivity().findViewById(R.id.tv_meal_recipe);
        tvNumberOfServings = (TextView) getActivity().findViewById(R.id.tv_number_of_servings);
        tvPrepTime = (TextView) getActivity().findViewById(R.id.tv_prep_time);
        tvCreationDate = (TextView) getActivity().findViewById(R.id.tv_meal_creation_date);
        tvUpvote = (TextView) getActivity().findViewById(R.id.tv_upvote);
        imgView = (ImageView) getActivity().findViewById(R.id.image);
        upvote_button = (Button) getActivity().findViewById(R.id.upvote_button);
        upvote_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                long mealServerId = 0;
                Cursor c = getActivity().getContentResolver().query(FoodNetworkContract.Meal.CONTENT_URI,
                        new String[0],
                        FoodNetworkContract.Meal._ID + " = " + mealId,
                        null,
                        null);

                if (c.moveToFirst()) {
                    mealServerId = c.getLong(c.getColumnIndexOrThrow(FoodNetworkContract.Meal.COLUMN_SERVER_ID));
                }
                MealService.startUpvoteMeal(getContext(), mealServerId);
            }
        });
        getLoaderManager().initLoader(MEAL_LOADER, null, this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_meal_details, container, false);
    }

    public void onButtonPressed(long uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case MEAL_LOADER:
                return new CursorLoader(getActivity(),
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

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(long mealId);
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

    }
    private void startImageDownload(String imageUrl) {
        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadImageTask().execute(imageUrl);
        } else {
            Toast.makeText(getContext(), "Could not fetch image", Toast.LENGTH_SHORT).show();
        }

    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls) {

            try {
                return downloadImage(urls[0]);
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result == null) {
                return;
            }

            ImageView imageView = (ImageView) getActivity().findViewById(R.id.image);
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
            conn.connect();

            int response = conn.getResponseCode();
            is = conn.getInputStream();

            Bitmap bitmap = BitmapFactory.decodeStream(is);

            return bitmap;

        } finally {
            if (is != null) {
                is.close();
            }
        }
    }
}
