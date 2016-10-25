package com.example.user.movieapp.data;

import com.example.user.movieapp.BuildConfig;

public class MovieDbUrl {

    private volatile static MovieDbUrl uniqueInstance;

    private final String mBaseUrl = "https://api.themoviedb.org/3/";
    private final String mApiKey = BuildConfig.TMDB_API_KEY_V3;

    private MovieDbUrl() {

    }

    public static MovieDbUrl getInstance() {
        if (uniqueInstance == null) {
            synchronized (MovieDbUrl.class) {
                if (uniqueInstance == null) {
                    uniqueInstance = new MovieDbUrl(); //Once in the block, check again and if still null, create the instance
                }
            }
        }
        return uniqueInstance;
    }

    public String createUrl(String sortOrder) {
        return mBaseUrl + "discover/movie?api_key=" + mApiKey + "&language=en-US" +
                "&sort_by=" + sortOrder + "&include_adult=false" +
                "&include_video=false&page=1";
    }

}
