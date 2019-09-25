package com.henley.simplecache.sample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import com.henley.simplecache.ACache;

/**
 * Activity基类
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected ACache mCache;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentLayoutId());
        mCache = ACache.get(this);
        initActionBar();
        initViews();
    }

    private void initActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(getClass().getSimpleName().replace("Activity", ""));
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 返回页面布局文件资源ID
     */
    protected abstract int getContentLayoutId();

    /**
     * 初始化View
     */
    protected abstract void initViews();

    /**
     * 点击save事件
     */
    public abstract void save(View v);

    /**
     * 点击read事件
     */
    public abstract void read(View v);

    /**
     * 点击clear事件
     */
    public abstract void clear(View v);

}
