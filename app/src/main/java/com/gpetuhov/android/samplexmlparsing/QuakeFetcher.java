package com.gpetuhov.android.samplexmlparsing;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

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

    // Keeps response from the server converted to InputStream
    // (this is needed for XMLPullParser).
    private InputStream mXMLResponse;

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
            reportSuccess();
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

        Log.d(LOG_TAG, "Received response from the server");

        // Get OkHttp ResponseBody from Retrofit Response and convert it to InputStream
        mXMLResponse = response.body().byteStream();

        // Parse received response
        parseXMLResponse();
    }

    @Override
    public void onFailure(Call<ResponseBody> call, Throwable t) {

        Log.d(LOG_TAG, "Error receiving response from server");

        reportError();
    }

    private void reportSuccess() {

        Log.d(LOG_TAG, "Reporting success");

        mQuakeFetchedFlag = true;

        if (mQuakeFetchedListener != null) {
            mQuakeFetchedListener.onQuakeFetcherSuccess(mQuakeLocation);
            unregisterListener();
        }
    }

    private void reportError() {

        Log.d(LOG_TAG, "Reporting error");

        mQuakeFetchedFlag = false;

        if (mQuakeFetchedListener != null) {
            mQuakeFetchedListener.onQuakeFetcherError();
            unregisterListener();
        }
    }

    private void unregisterListener() {
        mQuakeFetchedListener = null;
    }

    private void parseXMLResponse() {
        if (mXMLResponse != null) {

            Log.d(LOG_TAG, "Parsing XML...");

            try {
                // Create new XML parser (ExpatPullParser is used)
                XmlPullParser parser = Xml.newPullParser();

                // Do not process namespaces
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);

                // Set received response as input for the parser
                parser.setInput(mXMLResponse, null);

                // Move to first tag (start the parsing process)
                parser.nextTag();

                // Get location info of the most recent quake
                extractMostRecentQuake(parser);

            } catch (XmlPullParserException e) {
                Log.d(LOG_TAG, "XmlPullParserException");
                reportError();
            } catch (IOException e) {
                Log.d(LOG_TAG, "IOException");
                reportError();
            }
        } else {
            reportError();
        }
    }

    // Search for the first quake in the list and extract its location
    private void extractMostRecentQuake(XmlPullParser parser) throws XmlPullParserException, IOException {

        Log.d(LOG_TAG, "Searching most recent quake");

        // Get type of current parser event
        int event = parser.getEventType();

        // Look through entire XML response
        while (event != XmlPullParser.END_DOCUMENT) {

            // If current event is START_TAG
            if (event == XmlPullParser.START_TAG) {
                // Get name of the tag
                String name = parser.getName();

                // If name of the tag is "text"
                if (name.equals("text")) {

                    // Move to TEXT event
                    parser.next();

                    Log.d(LOG_TAG, "Most recent quake found. Getting location");

                    // Get text of the current event (this is quake location info)
                    mQuakeLocation = parser.getText();

                    // Pass quake location info to the listener and stop parsing
                    reportSuccess();
                    return;
                }
            }

            // Move to next event
            event = parser.next();
        }

        // If we got here, there were no quakes in XML response
        Log.d(LOG_TAG, "End of document. No quakes found");
        reportError();
    }
}
