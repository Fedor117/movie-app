package com.example.user.movieapp.ui.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.user.movieapp.R;
import com.example.user.movieapp.data.provider.MovieContract;
import com.squareup.picasso.Picasso;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String DETAIL_URI = "URI";

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    private static final String BASE_TMDB_IMAGE_URI = "https://image.tmdb.org/t/p/w500";
    private static final String SHARE_HASHTAG = "#movieapp";
    private static final int DETAIL_LOADER = 0;

    private static final String[] DETAIL_COLUMNS = {
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH,
            MovieContract.MovieEntry.COLUMN_RATING,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_OVERVIEW,
    };

    private ShareActionProvider mShareActionProvider;
    private String mDescriptionStr;
    private Uri mUri;

    private ImageView mPosterView;
    private TextView mTitleView;
    private TextView mRatingView;
    private TextView mReleaseDateView;
    private TextView mOverviewView;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
            Log.d(LOG_TAG, "Movie URI = " + mUri);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mPosterView = (ImageView) rootView.findViewById(R.id.detail_poster_imageview);
        mTitleView = (TextView) rootView.findViewById(R.id.detail_title_textview);
        mRatingView = (TextView) rootView.findViewById(R.id.detail_rating_textview);
        mReleaseDateView = (TextView) rootView.findViewById(R.id.detail_date_textview);
        mOverviewView = (TextView) rootView.findViewById(R.id.detail_overview_textview);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detailfragment, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);

        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareIntent());
        } else {
            Log.d(LOG_TAG, "ShareActionProvider is null");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mUri != null) {
            return new CursorLoader(getActivity(),
                    mUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null);
        }
        return  null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {
            int posterIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER_PATH);
            String posterPath = cursor.getString(posterIndex);
            Picasso.with(getContext())
                    .load(BASE_TMDB_IMAGE_URI + posterPath)
                    .error(R.drawable.noprofile)
                    .into(mPosterView);

            int titleIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_TITLE);
            String title = cursor.getString(titleIndex);
            mTitleView.setText(title);

            int ratingIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_RATING);
            double rating = cursor.getDouble(ratingIndex);
            mRatingView.setText(String.valueOf(rating));

            int dateIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_RELEASE_DATE);
            String date = cursor.getString(dateIndex);
            mReleaseDateView.setText(date);

            int overviewIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_OVERVIEW);
            String overview = cursor.getString(overviewIndex);
            mOverviewView.setText("\t" + overview);

            mDescriptionStr = title + " has rating " + rating + " in The Movie DB!";

            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareIntent());
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                mDescriptionStr + SHARE_HASHTAG);
        return shareIntent;
    }

}
