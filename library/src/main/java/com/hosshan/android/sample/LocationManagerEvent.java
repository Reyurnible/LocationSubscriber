package com.hosshan.android.sample;

import android.location.Location;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by shunhosaka on 2015/11/10.
 */
public class LocationManagerEvent {
    public static final String TAG = LocationManagerEvent.class.getSimpleName();

    public enum Kind {
        LOCATION_CHANGED, STATUS_CHANGED, PROVIDER_CHANGED
    }

    @NonNull
    private final Kind kind;
    @Nullable
    private Location location;
    @Nullable
    private String provider;

    @CheckResult @NonNull
    public static LocationManagerEvent create(@NonNull Kind kind, @Nullable Location location, @Nullable String provider) {
        return new LocationManagerEvent(kind, location, provider);
    }

    public LocationManagerEvent(@NonNull Kind kind, @Nullable Location location, @Nullable String provider) {
        this.kind = kind;
        this.location = location;
        this.provider = provider;
    }
}
