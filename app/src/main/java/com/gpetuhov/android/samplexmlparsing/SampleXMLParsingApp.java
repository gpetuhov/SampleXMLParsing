package com.gpetuhov.android.samplexmlparsing;

import android.app.Application;

import com.gpetuhov.android.samplexmlparsing.dagger.AppComponent;
import com.gpetuhov.android.samplexmlparsing.dagger.AppModule;
import com.gpetuhov.android.samplexmlparsing.dagger.DaggerAppComponent;

// Yellowstone application class.
// Builds and keeps instance of AppComponent,
// which is used to inject fields into application activities and fragments
public class SampleXMLParsingApp extends Application {

    // Keeps instance of AppComponent
    private static AppComponent mAppComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        // Build and keep AppComponent instance.
        // DaggerAppComponent is generated by Dagger.
        mAppComponent = DaggerAppComponent.builder()
                .appModule(new AppModule())
                .build();
    }

    public static AppComponent getAppComponent() {
        return mAppComponent;
    }
}
