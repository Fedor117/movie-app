package com.example.user.movieapp.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.user.movieapp.R;
import com.example.user.movieapp.data.sync.MovieSyncAdapter;
import com.example.user.movieapp.ui.fragment.MovieFragment;

public class MainActivity extends AppCompatActivity implements MovieFragment.Callback {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String MOVIEFRAGMENT_TAG = "MFTAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new MovieFragment(), MOVIEFRAGMENT_TAG)
                    .commit();
        }
        MovieSyncAdapter.initializeSyncAdapter(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Log.d(LOG_TAG, "Menu item settings pressed");
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(Uri movieUri) {
        Log.d(LOG_TAG, "Movie URI = " + movieUri);
        Intent intent = new Intent(this, DetailActivity.class)
                .setData(movieUri);
        startActivity(intent);
    }

    @Override
    public void updateFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new MovieFragment(), MOVIEFRAGMENT_TAG)
                .commit();
    }

}
