package com.henley.simplecache.sample;

import android.support.annotation.NonNull;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.henley.simplecache.sample.beans.User;

/**
 * 缓存Object
 */
public class CacheObjectActivity extends BaseActivity {

    private static final String CACHE_KEY = "cache_object";

    private TextView tvOriginal, tvRes;
    private User user;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_cache_object;
    }

    @Override
    protected void initViews() {
        tvOriginal = findViewById(R.id.tv_object_original);
        tvRes = findViewById(R.id.tv_object_res);

        user = new User();
        user.setName("Lucy");
        user.setAge("18");
        tvOriginal.setText(user.toString());
    }

    /**
     * 点击save事件
     */
    @Override
    public void save(View v) {
        boolean result = mCache.put(CACHE_KEY, user);
        if (result) {
            Toast.makeText(this, "Object cache successfully.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 点击read事件
     */
    @Override
    public void read(View v) {
        User cacheObject = (User) mCache.getAsObject(CACHE_KEY);
        if (cacheObject == null) {
            Toast.makeText(this, "Object cache is null ...", Toast.LENGTH_SHORT).show();
            tvRes.setText(null);
            return;
        }
        tvRes.setText(cacheObject.toString());
    }

    /**
     * 点击clear事件
     */
    @Override
    public void clear(View v) {
        mCache.remove(CACHE_KEY);
    }

}
