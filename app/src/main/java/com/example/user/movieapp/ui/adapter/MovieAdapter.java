package com.example.user.movieapp.ui.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.user.movieapp.R;
import com.example.user.movieapp.data.provider.MovieContract;
import com.squareup.picasso.Picasso;

public class MovieAdapter extends CursorAdapter {

    private static final String LOG_TAG = MovieAdapter.class.getSimpleName();
    private static final String BASE_TMDB_IMAGE_URI = "https://image.tmdb.org/t/p/w185";

    public static class ViewHolder {

        public final ImageView imageView;
        public final TextView titleView;
        public final TextView ratingView;
        public final TextView dateView;

        public ViewHolder(View view) {
            imageView = (ImageView) view.findViewById(R.id.list_item_icon);
            titleView = (TextView) view.findViewById(R.id.list_item_title_textview);
            ratingView = (TextView) view.findViewById(R.id.list_item_rating_textview);
            dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
        }

    }

    public MovieAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        Log.d(LOG_TAG, "In new view");
        int layoutId = R.layout.list_item_movie;

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Log.d(LOG_TAG, "In bind view");
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        int posterIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER_PATH);
        String posterPath = cursor.getString(posterIndex);
        Picasso.with(context)
                .load(BASE_TMDB_IMAGE_URI + posterPath)
                .into(viewHolder.imageView);

        int titleIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_TITLE);
        String title = cursor.getString(titleIndex);
        viewHolder.titleView.setText(title);

        int ratingIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_RATING);
        double rating = cursor.getDouble(ratingIndex);
        viewHolder.ratingView.setText(String.valueOf(rating));

        int dateIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_RELEASE_DATE);
        String date = cursor.getString(dateIndex);
        viewHolder.dateView.setText(date);
    }

}
