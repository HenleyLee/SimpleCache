package com.henley.simplecache.sample;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 缓存String
 */
public class CacheStringActivity extends BaseActivity {

    private static final String CACHE_KEY = "cache_string";

    private EditText edtInput;
    private TextView tvContent;

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_cache_string;
    }

    @Override
    protected void initViews() {
        edtInput = findViewById(R.id.et_string_input);
        tvContent = findViewById(R.id.tv_string_res);
    }

    /**
     * 点击save事件
     */
    @Override
    public void save(View v) {
        if (edtInput.getText().toString().trim().length() == 0) {
            Toast.makeText(this, "input is a null character ... So , when u press \"read\" , if do not show any result , please don't be surprise", Toast.LENGTH_SHORT).show();
        }
        boolean result = mCache.put(CACHE_KEY, edtInput.getText().toString());
        if (result) {
            Toast.makeText(this, "String cache successfully.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 点击read事件
     */
    @Override
    public void read(View v) {
        String cacheString = mCache.getAsString(CACHE_KEY);
        if (cacheString == null) {
            Toast.makeText(this, "String cache is null ...", Toast.LENGTH_SHORT).show();
            tvContent.setText(null);
            return;
        }
        tvContent.setText(cacheString);
    }

    /**
     * 点击clear事件
     */
    @Override
    public void clear(View v) {
        mCache.remove(CACHE_KEY);
    }

}
