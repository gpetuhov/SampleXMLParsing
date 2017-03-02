package com.gpetuhov.android.samplexmlparsing;

import android.util.Log;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Query;

// Fetches XML with list of earthquakes from the USGS server
public class QuakeFetcher implements Callback<ResponseBody> {

    private static final String LOG_TAG = QuakeFetcher.class.getName();

    private Retrofit mRetrofit;

    // True if quake already fetched
    private boolean mQuakeFetchedFlag = false;

    // Location of the most recent quake
    private String mQuakeLocation = "";

    // Listener to QuakeFetcher callbacks
    private QuakeFetchedListener mQuakeFetchedListener;

    // User of QuakeFetcher must implement this interface to receive callbacks
    public interface QuakeFetchedListener {
        void onQuakeFetcherSuccess(String quakeLocation);
        void onQuakeFetcherError();
    }

    // USGS API interface to be used in Retrofit
    private interface QuakeFetchService {
        // For USGS query parameters see http://earthquake.usgs.gov/fdsnws/event/1/
        // If magnitude and number of days are not specified,
        // the server returns all magnitudes for the last 30 days.

        @GET("query")   // USGS URL for queries is http://earthquake.usgs.gov/fdsnws/event/1/query
        Call<ResponseBody> getQuakes(
                @Query("format") String format,                 // Response format
                @Query("minmagnitude") String minMagnitude);    // Minimum magnitude
    }

    public QuakeFetcher(Retrofit retrofit) {
        mRetrofit = retrofit;
    }

    // Fetch list of earthquakes from USGS server
    public void fetchQuakes(QuakeFetchedListener listener) {

        // Save reference to the listener
        mQuakeFetchedListener = listener;

        // Check if quakes already fetched
        if (mQuakeFetchedFlag) {
            reportSuccess(mQuakeLocation);
        } else {
            requestQuakes();
        }
    }

    // Return XML response from USGS server
    private void requestQuakes() {

        // Create instance of the USGS API interface implementation
        QuakeFetchService service = mRetrofit.create(QuakeFetchService.class);

        // Create call to USGS server
        Call<ResponseBody> call = service.getQuakes(
                "xml",      // Response format = XML
                "5"         // Minimum magnitude = 5
        );

        // Execute call asynchronously
        // (retrofit performs and handles the method execution in a separate thread).
        // If no converter is specified, Retrofit returns OkHttp ResponseBody.
        call.enqueue(this);
    }

    // --- RETROFIT CALLBACKS ----------

    @Override
    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

        Log.d(LOG_TAG, "Received response from server");

        mQuakeFetchedFlag = true;

        try {
            // Get OkHttp ResponseBody from Retrofit Response and convert it to String
            mQuakeLocation = response.body().string();
            reportSuccess(mQuakeLocation);
        } catch (IOException e) {
            Log.d(LOG_TAG, "Error converting response to string");
            mQuakeFetchedFlag = false;
            reportError();
        }
    }

    @Override
    public void onFailure(Call<ResponseBody> call, Throwable t) {

        Log.d(LOG_TAG, "Error receiving response from server");

        mQuakeFetchedFlag = false;
        reportError();
    }

    private void reportSuccess(String quakeLocation) {
        if (mQuakeFetchedListener != null) {
            mQuakeFetchedListener.onQuakeFetcherSuccess(quakeLocation);
            unregisterListener();
        }
    }

    private void reportError() {
        if (mQuakeFetchedListener != null) {
            mQuakeFetchedListener.onQuakeFetcherError();
            unregisterListener();
        }
    }

    private void unregisterListener() {
        mQuakeFetchedListener = null;
    }

    private void parseXMLResponse() {
    }
}
