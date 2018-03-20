package com.example.liutiantian.compiler.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.android.lib.bind.api.annotation.BindModule;
import com.android.lib.bind.api.annotation.BindName;
import com.android.lib.bind.api.annotation.OnClick;

public class MainActivity extends AppCompatActivity implements BindModule {
    @BindName("activity_main_hello_world")
    private TextView helloText;

    @BindName("activity_main_hello_android")
    private TextView helloAndroid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MainActivityBinder.bind(this, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        helloText.setText(R.string.activity_main_hello_world_dynamic);
    }

    @Override
    public Object provideTarget(String name) {
        int id = getResources().getIdentifier(name, "id", getPackageName());
        Log.i(MainActivity.class.getSimpleName(), "provideTarget.DI1211, name: " + name + "; id > 0: " + (id > 0));
        if (id > 0) {
            return findViewById(id);
        }
        return null;
    }

    @OnClick("activity_main_hello_world")
    public void onHelloClick(TextView helloText) {

    }
}
