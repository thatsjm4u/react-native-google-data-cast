package com.thatsjm4u.android.gps;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.annotations.VisibleForTesting;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.mohan.location.locationtrack.LocationProvider;
import com.mohan.location.locationtrack.LocationTrack;
import com.mohan.location.locationtrack.LocationUpdateListener;
import com.mohan.location.locationtrack.pojo.LocationObj;
import com.mohan.location.locationtrack.providers.FusedLocationProvider;

import java.util.HashMap;
import java.util.Map;

import okio.Timeout;

/**
 * Created by shriramgosavi on 16/06/17.
 */

public class GpsModule extends ReactContextBaseJavaModule implements LifecycleEventListener {

    private WritableMap deviceAvailableParams;
    private ReactApplicationContext mContext;
    private LocationProvider locationProvider;
    private LocationTrack locationTrack;

    @VisibleForTesting
    public static final String REACT_CLASS = "GpsModule";

    private static final String EVENT_FOUND_LOCATION = "GpsModule:EventFoundLocation";
    private static final String EVENT_TIME_OUT = "GoogleCast:TimeOut";
//    private static final String DEVICE_DISCONNECTED = "GoogleCast:DeviceDisconnected";
//    private static final String MEDIA_LOADED = "GoogleCast:MediaLoaded";
//    private static final String APP_FAILED = "GoogleCast:AppFailed";
//    private static final String MSG_SEND_FAILED = "GoogleCast:MsgSendFailed";
//    private static final String MSG_RECEIVED = "GoogleCast:MsgReceived";


    public GpsModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.mContext = reactContext;
        getReactApplicationContext().addLifecycleEventListener(this);

    }

    @Override
    public String getName() {
        return "GpsModule";
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put("EVENT_FOUND_LOCATION", EVENT_FOUND_LOCATION);
        constants.put("TIME_OUT", EVENT_TIME_OUT);
//        constants.put("DEVICE_DISCONNECTED", DEVICE_DISCONNECTED);
//        constants.put("MEDIA_LOADED", MEDIA_LOADED);
//        constants.put("APP_FAILED", APP_FAILED);
//        constants.put("MSG_SEND_FAILED", MSG_SEND_FAILED);
//        constants.put("MSG_RECEIVED", MSG_RECEIVED);
        return constants;
    }

    @ReactMethod
    public void resetGpsObj() {

        if(locationProvider != null)
        {
            locationProvider.stop();
            locationProvider = null;
        }

        if(locationTrack != null)
        {
            locationTrack.stopLocationUpdates();
            locationTrack = null;
        }
    }

    private void emitMessageToRN(ReactContext reactContext, String eventName, @Nullable WritableMap params) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }

    @ReactMethod
    public void getLocationUpdates () {

        this.resetGpsObj();

        locationProvider = new FusedLocationProvider(this.mContext);

        locationTrack = new LocationTrack.Builder(this.mContext).withProvider(locationProvider).build().getLocationUpdates(new LocationUpdateListener() {

            @Override
            public void onLocationUpdate(Location location) {

                WritableMap params = Arguments.createMap();
                params.putDouble("longitude", location.getLongitude());
                params.putDouble("latitude", location.getLatitude());
                params.putDouble("speed", location.getSpeed());
                params.putDouble("altitude", location.getAltitude());
                params.putDouble("accuracy", location.getAccuracy());
                params.putDouble("bearing", location.getBearing());

                emitMessageToRN(GpsModule.this.mContext, EVENT_FOUND_LOCATION, params);
            }

            @Override
            public void onTimeout() {
                WritableMap params = Arguments.createMap();
                params.putBoolean("TimeOut", true);
                emitMessageToRN(GpsModule.this.mContext, EVENT_TIME_OUT, params);
            }
        });
    }

    @ReactMethod
    public void getLastKnownLocation(Promise promise) {
        Log.e(REACT_CLASS, "Last Location");
        try {

            this.resetGpsObj();
            locationProvider = new FusedLocationProvider(this.mContext);
            LocationObj location = new LocationTrack.Builder(this.mContext).withProvider(locationProvider).build().getLastKnownLocation();
            WritableMap params = Arguments.createMap();
            params.putDouble("longitude", location.getLongitude());
            params.putDouble("latitude", location.getLatitude());
            params.putDouble("speed", location.getSpeed());
            params.putDouble("altitude", location.getAltitude());
            params.putDouble("accuracy", location.getAccuracy());
            promise.resolve(params);
        }catch (Exception e) {
            promise.reject(e);
        }
//        emitMessageToRN(GpsModule.this.mContext, EVENT_FOUND_LOCATION, params);
    }

    @ReactMethod
    public void getCurrentSingleLocation() {
        Log.e(REACT_CLASS, "Current Single Location");

            this.resetGpsObj();
                locationProvider = new FusedLocationProvider(this.mContext);
                locationProvider.setCurrentLocationUpdate(true);

            locationTrack = new LocationTrack.Builder(this.mContext).withProvider(locationProvider).build().getLocationUpdates(new LocationUpdateListener() {

                @Override
                public void onLocationUpdate(Location location) {

                    WritableMap params = Arguments.createMap();
                    params.putDouble("longitude", location.getLongitude());
                    params.putDouble("latitude", location.getLatitude());
                    params.putDouble("speed", location.getSpeed());
                    params.putDouble("altitude", location.getAltitude());
                    params.putDouble("accuracy", location.getAccuracy());
                    params.putDouble("bearing", location.getBearing());
                        locationProvider.stop();
//                    promise.resolve(params);
                    emitMessageToRN(GpsModule.this.mContext, EVENT_FOUND_LOCATION, params);
                }

                @Override
                public void onTimeout() {
                    WritableMap params = Arguments.createMap();
                    params.putBoolean("TimeOut", true);
//                    promise.reject("TimeOut");
                    emitMessageToRN(GpsModule.this.mContext, EVENT_TIME_OUT, params);
                }
            });

//        emitMessageToRN(GpsModule.this.mContext, EVENT_FOUND_LOCATION, params);
    }


    @ReactMethod
    public void isGpsEnabled(Promise promise) {

        LocationManager manager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        if(manager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            promise.resolve(true);
            return;
        }
            else
        {
            promise.reject("false", "disabled");
            return;
        }
    }

    @ReactMethod
    public void stopLocationUpdate() {
        Log.e(REACT_CLASS, "Stop Location Update");
        this.resetGpsObj();
    }

    @Override
    public void onHostResume() {

    }

    @Override
    public void onHostPause() {

    }

    @Override
    public void onHostDestroy() {
        this.resetGpsObj();
    }
}
