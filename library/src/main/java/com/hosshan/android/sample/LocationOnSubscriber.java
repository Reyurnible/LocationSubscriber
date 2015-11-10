package com.hosshan.android.sample;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by shunhosaka on 2015/11/10.
 */
public class LocationOnSubscriber implements Observable.OnSubscribe<LocationManagerEvent> {
    public static final String TAG = LocationOnSubscriber.class.getSimpleName();
    private static final int TWO_MINUTES = 1000 * 60 * 2;

    private Context mContext;
    // デフォルトは15秒にする
    private int mPeriodTime = 15 * 1000;
    private String mProvider;
    private Location mCurrentBestLocation;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;

    public static Observable<LocationManagerEvent> getObservable(Context context) {
        return Observable.create(new LocationOnSubscriber(context));
    }

    public LocationOnSubscriber(Context context) {
        mContext = context;
    }

    @Override
    public void call(final Subscriber<? super LocationManagerEvent> subscriber) {
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mCurrentBestLocation = isBetterLocation(location, mCurrentBestLocation) ? location : mCurrentBestLocation;
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onNext(LocationManagerEvent.create(LocationManagerEvent.Kind.LOCATION_CHANGED, mCurrentBestLocation, mProvider));
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onNext(LocationManagerEvent.create(LocationManagerEvent.Kind.STATUS_CHANGED, null, null));
                }
            }

            @Override
            public void onProviderEnabled(String provider) {
                if (mProvider == null) return;
                mProvider = provider;
                registerProvider();
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onNext(LocationManagerEvent.create(LocationManagerEvent.Kind.PROVIDER_CHANGED, null, provider));
                }
            }

            @Override
            public void onProviderDisabled(String provider) {
                if (mProvider.equals(provider)) {
                    start();
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onNext(LocationManagerEvent.create(LocationManagerEvent.Kind.PROVIDER_CHANGED, null, mProvider));
                    }
                }
            }
        };
        start();
    }

    public void start() {
        // LocationManagerを取得
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        // Criteriaオブジェクトを生成
        Criteria criteria = new Criteria();
        // Accuracyを指定(低精度)
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        // PowerRequirementを指定(低消費電力)
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        // ロケーションプロバイダの取得
        mProvider = mLocationManager.getBestProvider(criteria, true);
        registerProvider();
    }

    public void stop() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocationManager.removeUpdates(mLocationListener);
    }

    public int getPeriodTime() {
        return mPeriodTime;
    }

    public void setPeriodTime(int periodTime) {
        this.mPeriodTime = periodTime;
    }

    private void registerProvider() {
        if (mProvider == null) return;
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // LocationListenerを登録
        mLocationManager.requestLocationUpdates(mProvider, mPeriodTime, 10, mLocationListener);
        mCurrentBestLocation = mLocationManager.getLastKnownLocation(mProvider);
    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            return true;
        }
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;
        if (isSignificantlyNewer) {
            return true;
        } else if (isSignificantlyOlder) {
            return false;
        }
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

}
