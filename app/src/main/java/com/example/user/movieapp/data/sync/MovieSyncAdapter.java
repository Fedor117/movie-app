package com.example.user.movieapp.data.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.example.user.movieapp.R;
import com.example.user.movieapp.data.MovieDbUrl;
import com.example.user.movieapp.data.provider.MovieContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

public class MovieSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String LOG_TAG = MovieSyncAdapter.class.getSimpleName();
    private static final String SORT_ORDER = "popularity.desc";

    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;

    public MovieSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String moviesJsonStr;

        try {
            MovieDbUrl movieDbUrl = MovieDbUrl.getInstance();
            URL url = new URL(movieDbUrl.createUrl(SORT_ORDER));

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                return;
            }

            moviesJsonStr = buffer.toString();
            getMovieDescriptionFromJson(moviesJsonStr);

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            return;
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
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
    }

    private void getMovieDescriptionFromJson(String moviesJsonStr)
            throws JSONException {
        Log.d(LOG_TAG, moviesJsonStr);
        final String TMDB_RESULTS = "results";
        final String TMDB_POSTER_PATH = "poster_path";
        final String TMDB_OVERVIEW = "overview";
        final String TMDB_RELEASE_DATE = "release_date";
        final String TMDB_ORIGINAL_TITLE = "original_title";
        final String TMDB_VOTE_AVG = "vote_average";

        try {
            JSONObject movieJson = new JSONObject(moviesJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(TMDB_RESULTS);

            Vector<ContentValues> valuesVector = new Vector<>(movieArray.length());

            for (int i = 0; i < 20; i++) {
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

                ContentValues values = new ContentValues();

                values.put(MovieContract.MovieEntry.COLUMN_TITLE, title);
                values.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, posterPath);
                values.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, overview);
                values.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, releaseDate);
                values.put(MovieContract.MovieEntry.COLUMN_RATING, rating);

                valuesVector.add(values);
            }

            int inserted = 0;
            if (valuesVector.size() > 0) {
                ContentValues[] valuesArray = new ContentValues[valuesVector.size()];
                valuesVector.toArray(valuesArray);

                getContext().getContentResolver()
                        .delete(MovieContract.MovieEntry.CONTENT_URI, null, null);

                inserted = getContext().getContentResolver()
                        .bulkInsert(MovieContract.MovieEntry.CONTENT_URI, valuesArray);
            }
            Log.d(LOG_TAG, "FetchWeatherTask Complete. " + inserted + " inserted");

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if (accountManager.getPassword(newAccount) == null) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        MovieSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
}
