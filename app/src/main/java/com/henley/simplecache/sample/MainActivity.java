package com.henley.simplecache.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * 主界面
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void string(View v) {
        startActivity(new Intent().setClass(this, CacheStringActivity.class));
    }

    public void jsonObject(View v) {
        startActivity(new Intent().setClass(this, CacheJsonObjectActivity.class));
    }

    public void jsonArray(View v) {
        startActivity(new Intent().setClass(this, CacheJsonArrayActivity.class));
    }

    public void bitmap(View v) {
        startActivity(new Intent().setClass(this, CacheBitmapActivity.class));
    }

    public void drawable(View v) {
        startActivity(new Intent().setClass(this, CacheDrawableActivity.class));
    }

    public void object(View v) {
        startActivity(new Intent().setClass(this, CacheObjectActivity.class));
    }

    public void stream(View v) {
        startActivity(new Intent().setClass(this, CacheStreamActivity.class));
    }

}
