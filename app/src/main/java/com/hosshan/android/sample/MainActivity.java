package com.hosshan.android.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import rx.Observable;
import rx.Subscriber;

public class MainActivity extends AppCompatActivity {
    private LocationOnSubscriber mLocationOnSubscriber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLocationOnSubscriber = new LocationOnSubscriber(getApplicationContext());
        Observable.create(mLocationOnSubscriber)
                .subscribe(new Subscriber<LocationManagerEvent>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(LocationManagerEvent locationManagerEvent) {

                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mLocationOnSubscriber.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mLocationOnSubscriber.stop();
    }
}
