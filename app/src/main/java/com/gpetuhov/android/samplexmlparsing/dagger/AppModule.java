package com.gpetuhov.android.samplexmlparsing.dagger;

import com.gpetuhov.android.samplexmlparsing.QuakeFetcher;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

// Dagger module tells, what instances will be instantiated
@Module
public class AppModule {

    // USGS base URL
    public static final String USGS_BASE_URL = "http://earthquake.usgs.gov/fdsnws/event/1/";

    // Returns instance of OkHttpClient
    @Provides
    @Singleton
    OkHttpClient provideOkHttpClient() {
        OkHttpClient client = new OkHttpClient();
        return client;
    }

    // Returns instance of Retrofit for fetching quakes
    @Provides
    @Singleton
    Retrofit provideRetrofitForQuakes(OkHttpClient okHttpClient) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(USGS_BASE_URL)
                .client(okHttpClient)
                .build();
        return retrofit;
    }

    // Returns instance of QuakeFetcher
    @Provides
    @Singleton
    QuakeFetcher providesQuakeFetcher(Retrofit retrofit) {
        QuakeFetcher quakeFetcher = new QuakeFetcher(retrofit);
        return quakeFetcher;
    }
}
