package com.gpetuhov.android.samplexmlparsing.dagger;

import com.gpetuhov.android.samplexmlparsing.MainFragment;

import javax.inject.Singleton;

import dagger.Component;

// Dagger component tells, into which classes instances instantiated by Module will be injected.
@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {
    void inject(MainFragment fragment);
}