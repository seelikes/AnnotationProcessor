package com.example.liutiantian.rp.demo;

import android.widget.TextView;

import com.android.lib.rp.api.RPViewManager;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;

/**
 * Created by liutiantian on 2018/3/24.
 */

@RPViewManager("Demo")
public class DemoViewManager extends SimpleViewManager<TextView> {
    @Override
    public String getName() {
        return "TextView";
    }

    @Override
    protected TextView createViewInstance(ThemedReactContext reactContext) {
        return new TextView(reactContext);
    }
}
