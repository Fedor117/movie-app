package com.example.user.movieapp.data.sync;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.example.user.movieapp.data.MovieDbUrl;
import com.example.user.movieapp.ui.fragment.MovieFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FetchMovieTask extends AsyncTask<String, Void, String[]> {

    public static final int NUM_OF_MOVIES = 20;

    private static final String LOG_TAG = FetchMovieTask.class.getSimpleName();

    private Context mContext;
    private ArrayAdapter<String> mAdapter;

    public FetchMovieTask(Context context, ArrayAdapter<String> adapter) {
        mContext = context;
        mAdapter = adapter;
    }

    @Override
    protected String[] doInBackground(String... params) {
        if (params.length == 0) {
            return null;
        }

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String moviesJsonStr = null;

        try {
            MovieDbUrl movieDbUrl = MovieDbUrl.getInstance();
            URL url = new URL(movieDbUrl.createUrl(params[0]));

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                return null;
            }
            moviesJsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        try {
            return getMovieDescriptionFromJson(moviesJsonStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String[] result) {
        if (result != null) {
            mAdapter.clear();
            for (String movieDescrStr : result) {
                mAdapter.add(movieDescrStr);
            }
        }
    }

    private String[] getMovieDescriptionFromJson(String moviesJsonStr)
            throws JSONException {
        Log.d(LOG_TAG, moviesJsonStr);
        final String TMDB_RESULTS = "results";
        final String TMDB_POSTER_PATH = "poster_path";
        final String TMDB_OVERVIEW = "overview";
        final String TMDB_RELEASE_DATE = "release_date";
        final String TMDB_ORIGINAL_TITLE = "original_title";
        final String TMDB_VOTE_AVG = "vote_average";

        JSONObject movieJson = new JSONObject(moviesJsonStr);
        JSONArray movieArray = movieJson.getJSONArray(TMDB_RESULTS);

        String[] resultStrs = new String[NUM_OF_MOVIES]; // size = 20;

        for (int i = 0; i < resultStrs.length; i++) { // TODO: 24.10.2016 Get full info
            String title;
            String posterPath;
            String overview;
            String releaseDate;
            double rating;

            JSONObject movieInformationObject = movieArray.getJSONObject(i);
            title = movieInformationObject.getString(TMDB_ORIGINAL_TITLE);
            posterPath = movieInformationObject.getString(TMDB_POSTER_PATH);
            overview = movieInformationObject.getString(TMDB_OVERVIEW);
            releaseDate = movieInformationObject.getString(TMDB_RELEASE_DATE);
            rating = movieInformationObject.getDouble(TMDB_VOTE_AVG);

            resultStrs[i] = title + " RELEASED IN " + releaseDate +
                    " AND HAS RATING " + rating;
        }
        return resultStrs;
    }

}
