package com.example.user.movieapp.ui.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by User on 27.10.2016.
 */

public class MovieAdapter extends CursorAdapter {

    private static final String LOG_TAG = MovieAdapter.class.getSimpleName();
    private Context mContext;
    private static int sLoaderId;

    public static class ViewHolder {

        public final ImageView imageView;
        public final TextView textView;

        // FIXME: 27.10.2016
        public ViewHolder(View view) {
            imageView = (ImageView) view.findViewById(0);
            textView = (TextView) view.findViewById(1);
        }

    }

    public MovieAdapter(Context context, Cursor c, int flags, int loaderId) {
        super(context, c, flags);
        mContext = context;
        sLoaderId = loaderId;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return null;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

    }

}
