package com.gpetuhov.android.samplexmlparsing;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class MainFragment extends Fragment implements QuakeFetcher.QuakeFetchedListener {

    // Keeps instance of QuakeFetcher. Injected by Dagger.
    @Inject QuakeFetcher mQuakeFetcher;

    // Displays most recent quake
    @BindView(R.id.recent_quake_location) TextView mQuakeLocation;

    // Keeps ButterKnife Unbinder object to properly unbind views in onDestroyView of the fragment
    private Unbinder mUnbinder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inject QuakeFetcher instance into this fragment field
        SampleXMLParsingApp.getAppComponent().inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_main, container, false);

        // Bind views and save reference to Unbinder object
        mUnbinder = ButterKnife.bind(this, v);

        mQuakeFetcher.fetchQuakes(this);

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // This is recommended to do here when using Butterknife in fragments
        mUnbinder.unbind();
    }

    // --- QUAKEFETCHER CALLBACKS ----------

    @Override
    public void onQuakeFetcherSuccess(String quakeLocation) {
        mQuakeLocation.setText(quakeLocation);
    }

    @Override
    public void onQuakeFetcherError() {
        mQuakeLocation.setText("Error fetching quake");
    }
}
