package com.henley.simplecache.sample;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * 缓存Drawable
 */
public class CacheDrawableActivity extends BaseActivity {

    private static final String CACHE_KEY = "cache_drawable";

    private ImageView ivDrawableRes;

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_cache_drawable;
    }

    @Override
    protected void initViews() {
        ivDrawableRes = findViewById(R.id.iv_drawable_res);
    }

    /**
     * 点击save事件
     */
    @Override
    public void save(View v) {
        Drawable drawable = getResources().getDrawable(R.drawable.img_test);
        boolean result = mCache.put(CACHE_KEY, drawable);
        if (result) {
            Toast.makeText(this, "Drawable cache successfully.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 点击read事件
     */
    @Override
    public void read(View v) {
        Drawable testDrawable = mCache.getAsDrawable(CACHE_KEY);
        if (testDrawable == null) {
            Toast.makeText(this, "Drawable cache is null ...", Toast.LENGTH_SHORT).show();
            ivDrawableRes.setImageDrawable(null);
            return;
        }
        ivDrawableRes.setImageDrawable(testDrawable);
    }

    /**
     * 点击clear事件
     */
    @Override
    public void clear(View v) {
        mCache.remove(CACHE_KEY);
    }

}