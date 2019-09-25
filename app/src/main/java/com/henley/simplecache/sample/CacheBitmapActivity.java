package com.henley.simplecache.sample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * 缓存Bitmap
 */
public class CacheBitmapActivity extends BaseActivity {

    private static final String CACHE_KEY = "cache_bitmap";

    private ImageView ivBitmapRes;

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_cache_bitmap;
    }

    @Override
    protected void initViews() {
        ivBitmapRes = findViewById(R.id.iv_bitmap_res);
    }

    /**
     * 点击save事件
     */
    @Override
    public void save(View v) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_test);
        boolean result = mCache.put(CACHE_KEY, bitmap);
        if (result) {
            Toast.makeText(this, "Bitmap cache successfully.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 点击read事件
     */
    @Override
    public void read(View v) {
        Bitmap testBitmap = mCache.getAsBitmap(CACHE_KEY);
        if (testBitmap == null) {
            Toast.makeText(this, "Bitmap cache is null ...", Toast.LENGTH_SHORT).show();
            ivBitmapRes.setImageBitmap(null);
            return;
        }
        ivBitmapRes.setImageBitmap(testBitmap);
    }

    /**
     * 点击clear事件
     */
    @Override
    public void clear(View v) {
        mCache.remove(CACHE_KEY);
    }

}
