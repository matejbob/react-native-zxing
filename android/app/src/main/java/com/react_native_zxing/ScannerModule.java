package com.react_native_zxing;

import android.app.Activity;
import android.content.Intent;
import androidx.annotation.NonNull;
import android.util.Log;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ScannerModule extends ReactContextBaseJavaModule implements ActivityEventListener {

    private final ReactApplicationContext mReactContext;
    private Callback mCallback;
    private boolean mCallbackInvoked;

    public ScannerModule(ReactApplicationContext reactContext) {
        super(reactContext);
        mReactContext = reactContext;        
    }

    @Override
    public String getName() {
        return "ScannerModule";
    }

    @ReactMethod
    void openScanner(boolean isBeepEnable,
                     String prompt,
                     Callback callback) {
        mCallback = callback;
        mCallbackInvoked = false;

        Activity activity = getCurrentActivity();

        if (activity != null) {
            new IntentIntegrator(activity)
                    .setPrompt(prompt == null ? "" : prompt)
                    .setBeepEnabled(isBeepEnable)
                    .initiateScan();

            mReactContext.addActivityEventListener(this);
        }
    }

    @ReactMethod
    void openCustomScanner(boolean isBeepEnable,
                     boolean isOrientationLocked,
                     ReadableArray barcodeTypes,
                     Callback callback) {
        mCallback = callback;
        mCallbackInvoked = false;

        Activity activity = getCurrentActivity();

        List<String> types = getBarcodesTypes(barcodeTypes);

        if (activity != null) {
            IntentIntegrator intentIntegrator = new IntentIntegrator(activity);
            intentIntegrator
                    .setBeepEnabled(isBeepEnable)
                    .setDesiredBarcodeFormats(types)
                    .setOrientationLocked(isOrientationLocked)
                    .setCaptureActivity(ScannerActivity.class)
                    .initiateScan();

            mReactContext.addActivityEventListener(this);
        }
    }

    private List<String> getBarcodesTypes(ReadableArray barcodeTypes) {
        if (barcodeTypes == null) {
            return null;
        }

        ArrayList<Object> objects = barcodeTypes.toArrayList();

        List<String> types = new ArrayList<>();

        for (Object type: objects) {
            types.add((String) type);
        }
        return types;
    }

    @Override
    public synchronized void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (!mCallbackInvoked) {
            IntentResult result = IntentIntegrator.parseActivityResult(resultCode, data);
            
            mCallbackInvoked = true;
            mCallback.invoke(result.getContents());

            mReactContext.removeActivityEventListener(this);
        }
    }

    @Override
    public void onNewIntent(Intent intent) {}
}
