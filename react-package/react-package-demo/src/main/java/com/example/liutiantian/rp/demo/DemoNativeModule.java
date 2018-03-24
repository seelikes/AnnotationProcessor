package com.example.liutiantian.rp.demo;

import com.android.lib.rp.api.RPNativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;

/**
 * Created by liutiantian on 2018/3/24.
 */

@RPNativeModule("Demo")
public class DemoNativeModule extends ReactContextBaseJavaModule {
    public DemoNativeModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "Demo";
    }
}
