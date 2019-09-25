package com.henley.simplecache.sample;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 缓存JSONObject
 */
public class CacheJsonObjectActivity extends BaseActivity {

    private static final String CACHE_KEY = "cache_json_object";

    private TextView tvOriginal, tvRes;
    private JSONObject jsonObject;

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_cache_jsonobject;
    }

    @Override
    protected void initViews() {
        tvOriginal = findViewById(R.id.tv_jsonobject_original);
        tvRes = findViewById(R.id.tv_jsonobject_res);

        jsonObject = new JSONObject();
        try {
            jsonObject.put("name", "Alex");
            jsonObject.put("age", 18);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        tvOriginal.setText(jsonObject.toString());
    }

    /**
     * 点击save事件
     */
    @Override
    public void save(View v) {
        boolean result = mCache.put(CACHE_KEY, jsonObject);
        if (result) {
            Toast.makeText(this, "JSONObject cache successfully.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 点击read事件
     */
    @Override
    public void read(View v) {
        JSONObject cacheJsonObject = mCache.getAsJSONObject(CACHE_KEY);
        if (cacheJsonObject == null) {
            Toast.makeText(this, "JSONObject cache is null ...", Toast.LENGTH_SHORT).show();
            tvRes.setText(null);
            return;
        }
        tvRes.setText(cacheJsonObject.toString());
    }

    /**
     * 点击clear事件
     */
    @Override
    public void clear(View v) {
        mCache.remove(CACHE_KEY);
    }

}
