package com.googlecastdata;

import android.support.annotation.Nullable;
import android.support.v7.media.MediaRouter;
import android.util.Log;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.UiThreadUtil;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.annotations.VisibleForTesting;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.uimanager.IllegalViewOperationException;
import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.cast.companionlibrary.cast.CastConfiguration;
import com.google.android.libraries.cast.companionlibrary.cast.DataCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.callbacks.DataCastConsumer;
import com.google.android.libraries.cast.companionlibrary.cast.callbacks.DataCastConsumerImpl;
import com.google.android.libraries.cast.companionlibrary.cast.exceptions.NoConnectionException;
import com.google.android.libraries.cast.companionlibrary.cast.exceptions.TransientNetworkDisconnectionException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jayesh Malviya on 31/05/17.
 */

public class GoogleCastDataModule extends ReactContextBaseJavaModule implements LifecycleEventListener {
    private DataCastManager mCastManager;
    private DataCastConsumer mCastConsumer;
    Map<String, MediaRouter.RouteInfo> currentDevices = new HashMap<>();
    private WritableMap deviceAvailableParams;


    @VisibleForTesting
    public static final String REACT_CLASS = "GoogleCastDataModule";

    private static final String DEVICE_AVAILABLE = "GoogleCast:DeviceAvailable";
    private static final String DEVICE_CONNECTED = "GoogleCast:DeviceConnected";
    private static final String DEVICE_DISCONNECTED = "GoogleCast:DeviceDisconnected";
    private static final String MEDIA_LOADED = "GoogleCast:MediaLoaded";
    private static final String APP_FAILED = "GoogleCast:AppFailed";
    private static final String MSG_SEND_FAILED = "GoogleCast:MsgSendFailed";
    private static final String MSG_RECEIVED = "GoogleCast:MsgReceived";


    public GoogleCastDataModule(ReactApplicationContext reactContext) {
        super(reactContext);
        getReactApplicationContext().addLifecycleEventListener(this);
    }

    @Override
    public String getName() {
        return "GoogleCastData";
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put("DEVICE_AVAILABLE", DEVICE_AVAILABLE);
        constants.put("DEVICE_CONNECTED", DEVICE_CONNECTED);
        constants.put("DEVICE_DISCONNECTED", DEVICE_DISCONNECTED);
        constants.put("MEDIA_LOADED", MEDIA_LOADED);
        constants.put("APP_FAILED", APP_FAILED);
        constants.put("MSG_SEND_FAILED", MSG_SEND_FAILED);
        constants.put("MSG_RECEIVED", MSG_RECEIVED);
        return constants;
    }


    private void emitMessageToRN(ReactContext reactContext, String eventName, @Nullable WritableMap params) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }

    private void addDevice(MediaRouter.RouteInfo info) {
        currentDevices.put(info.getId(), info);
    }

    private void removeDevice(MediaRouter.RouteInfo info) {
        currentDevices.remove(info.getId());
    }



    @ReactMethod
    public void startScan(@Nullable String ApplicationID) {
        Log.e(REACT_CLASS, "start scan Chromecast ");
        if (mCastManager != null) {
            mCastManager = DataCastManager.getInstance();
            UiThreadUtil.runOnUiThread(new Runnable() {
                public void run() {
                    mCastManager.incrementUiCounter();
                    mCastManager.startCastDiscovery();
                }
            });

            Log.e(REACT_CLASS, "Chromecast Initialized by getting instance");
        } else {
            final CastConfiguration options = new CastConfiguration.Builder(ApplicationID)
                    .enableAutoReconnect()
                    .build();
            UiThreadUtil.runOnUiThread(new Runnable() {
                public void run() {
                    DataCastManager.initialize(getCurrentActivity(), options);
                    mCastManager = DataCastManager.getInstance();
                    mCastConsumer = new DataCastConsumerImpl() {
                        @Override
                        public void onApplicationConnected(ApplicationMetadata appMetadata, String applicationStatus, String sessionId, boolean wasLaunched) {
                            super.onApplicationConnected(appMetadata, applicationStatus, sessionId, wasLaunched);
                            Log.e(REACT_CLASS, "onApplicationConnected ");
                            emitMessageToRN(getReactApplicationContext(), DEVICE_CONNECTED, null);
                        }

                        @Override
                        public void onApplicationDisconnected(int errorCode) {
                            super.onApplicationDisconnected(errorCode);
                            Log.e(REACT_CLASS, "onApplicationDisconnected");
                            emitMessageToRN(getReactApplicationContext(), DEVICE_DISCONNECTED, null);
                        }

                        @Override
                        public void onApplicationStopFailed(int errorCode) {
                            super.onApplicationStopFailed(errorCode);
                            Log.e(REACT_CLASS, "onApplicationStopFailed :( with error code " + errorCode);
                            emitMessageToRN(getReactApplicationContext(), APP_FAILED, null);
                        }

                        @Override
                        public void onApplicationConnectionFailed(int errorCode) {
                            super.onApplicationConnectionFailed(errorCode);
                            Log.e(REACT_CLASS, "onApplicationConnectionFailed :( with error code " + errorCode);
                            emitMessageToRN(getReactApplicationContext(), APP_FAILED, null);
                        }

                        @Override
                        public void onApplicationStatusChanged(String appStatus) {
                            super.onApplicationStatusChanged(appStatus);
                            Log.e(REACT_CLASS, "onApplicationStatusChanged :( " + appStatus);
                            emitMessageToRN(getReactApplicationContext(), APP_FAILED, null);
                        }

                        @Override
                        public void onVolumeChanged(double value, boolean isMute) {
                            super.onVolumeChanged(value, isMute);
                            Log.e(REACT_CLASS, "onVolumeChanged :( " + value + isMute);
                        }

                        @Override
                        public void onMessageReceived(CastDevice castDevice, String namespace, String message) {
                            super.onMessageReceived(castDevice, namespace, message);
                            Log.e(REACT_CLASS, "onMessageReceived : " + namespace + " " + message);
                            deviceAvailableParams = Arguments.createMap();
                            deviceAvailableParams.putString("namespace", namespace);
                            deviceAvailableParams.putString("message", message);
                            emitMessageToRN(getReactApplicationContext(), MSG_RECEIVED, deviceAvailableParams);
                        }

                        @Override
                        public void onMessageSendFailed(Status status) {
                            super.onMessageSendFailed(status);
                            Log.e(REACT_CLASS, "onMessageSendFailed " + status);
                            emitMessageToRN(getReactApplicationContext(), MSG_SEND_FAILED, null);
                        }

                        @Override
                        public void onRemoved(CastDevice castDevice, String namespace) {
                            super.onRemoved(castDevice, namespace);
                            emitMessageToRN(getReactApplicationContext(), APP_FAILED, null);
                        }


                        @Override
                        public void onRouteRemoved(MediaRouter.RouteInfo info) {
                            super.onRouteRemoved(info);
                            removeDevice(info);
                        }


                        @Override
                        public void onCastDeviceDetected(MediaRouter.RouteInfo info) {
                            super.onCastDeviceDetected(info);
                            deviceAvailableParams = Arguments.createMap();
                            Log.e(REACT_CLASS, "onCastDeviceDetected " + info.getName());
                            deviceAvailableParams.putBoolean("device_available", true);
                            emitMessageToRN(getReactApplicationContext(), DEVICE_AVAILABLE, deviceAvailableParams);
                            addDevice(info);
                        }

                        @Override
                        public void onCastAvailabilityChanged(boolean castPresent) {
                            deviceAvailableParams = Arguments.createMap();
                            Log.e(REACT_CLASS, "onCastAvailabilityChanged: exists? " + Boolean.toString(castPresent));
                            deviceAvailableParams.putBoolean("device_available", castPresent);
                            emitMessageToRN(getReactApplicationContext(), DEVICE_AVAILABLE, deviceAvailableParams);
                        }
                    };
                    mCastManager.addDataCastConsumer(mCastConsumer);
                    mCastManager.incrementUiCounter();
                    mCastManager.startCastDiscovery();
                    Log.e(REACT_CLASS, "Chromecast Initialized for the first time!");
                }
            });
        }
    }

    @ReactMethod
    public void stopScan() {
        Log.e(REACT_CLASS, "Stopping Scan");
        if (mCastManager != null) {
            mCastManager.decrementUiCounter();
        }
    }

    @ReactMethod
    public void isConnected(Promise promise) {
        boolean isConnected = DataCastManager.getInstance().isConnected();
        Log.e(REACT_CLASS, "Am I connected ? " + isConnected);
        promise.resolve(isConnected);
    }

    @ReactMethod
    public void getDevices(Promise promise) {
        WritableArray devicesList = Arguments.createArray();
        try {
            Log.e(REACT_CLASS, "devices size " + currentDevices.size());
            for (MediaRouter.RouteInfo existingChromecasts : currentDevices.values()) {
                WritableMap singleDevice = Arguments.createMap();
                singleDevice.putString("id", existingChromecasts.getId());
                singleDevice.putString("name", existingChromecasts.getName());
                devicesList.pushMap(singleDevice);
            }
            promise.resolve(devicesList);
        } catch (IllegalViewOperationException e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void connectToDevice(@Nullable String deviceId) {
        Log.e(REACT_CLASS, "received deviceName " + deviceId);
        try {
            Log.e(REACT_CLASS, "devices size " + currentDevices.size());
            MediaRouter.RouteInfo info = currentDevices.get(deviceId);
            CastDevice device = CastDevice.getFromBundle(info.getExtras());
            mCastManager.onDeviceSelected(device, info);
        } catch (IllegalViewOperationException e) {
            e.printStackTrace();
        }
    }

    @ReactMethod
    public void disconnect() {
        UiThreadUtil.runOnUiThread(new Runnable() {
            public void run() {
                try {
                    mCastManager.stopApplication();
                    mCastManager.disconnect();
                } catch (TransientNetworkDisconnectionException | NoConnectionException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @ReactMethod
    public  void sendDataMessage(@Nullable String message, @Nullable String namespace) {
        try {

            Log.e(REACT_CLASS, "Casting media... ");
            mCastManager.sendDataMessage(message, namespace);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onHostResume() {

    }

    @Override
    public void onHostPause() {

    }

    @Override
    public void onHostDestroy() {

    }
}
