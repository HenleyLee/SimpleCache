package com.henley.simplecache.sample;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 缓存JSONArray
 */
public class CacheJsonArrayActivity extends BaseActivity {

    private static final String CACHE_KEY = "cache_json_array";

    private TextView tvOriginal, tvRes;
    private JSONArray jsonArray;

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_cache_jsonarray;
    }

    @Override
    protected void initViews() {
        tvOriginal = findViewById(R.id.tv_jsonarray_original);
        tvRes = findViewById(R.id.tv_jsonarray_res);

        jsonArray = new JSONArray();
        JSONObject jsonObject1 = new JSONObject();

        try {
            jsonObject1.put("name", "Johnny");
            jsonObject1.put("age", 18);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject jsonObject2 = new JSONObject();
        try {
            jsonObject2.put("name", "Denny");
            jsonObject2.put("age", 20);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        jsonArray.put(jsonObject1);
        jsonArray.put(jsonObject2);

        tvOriginal.setText(jsonArray.toString());
    }

    /**
     * 点击save事件
     */
    @Override
    public void save(View v) {
        boolean result = mCache.put(CACHE_KEY, jsonArray);
        if (result) {
            Toast.makeText(this, "JSONArray cache successfully.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 点击read事件
     */
    @Override
    public void read(View v) {
        JSONArray cacheJsonArray = mCache.getAsJSONArray(CACHE_KEY);
        if (cacheJsonArray == null) {
            Toast.makeText(this, "JSONArray cache is null ...", Toast.LENGTH_SHORT).show();
            tvRes.setText(null);
            return;
        }
        tvRes.setText(cacheJsonArray.toString());
    }

    /**
     * 点击clear事件
     */
    @Override
    public void clear(View v) {
        mCache.remove(CACHE_KEY);
    }

}
