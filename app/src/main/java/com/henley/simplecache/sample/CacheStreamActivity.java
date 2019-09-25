package com.henley.simplecache.sample;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 缓存Stream
 */
public class CacheStreamActivity extends BaseActivity implements Runnable {

    private static final String CACHE_KEY = "cache_stream";
    private static final String URL = "http://www.largesound.com/ashborytour/sound/brobob.mp3";

    private TextView text;

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_cache_stream;
    }

    @Override
    protected void initViews() {
        text = findViewById(R.id.text);
    }

    /**
     * 点击save事件
     */
    @Override
    public void save(View v) {
        text.setText("Loading...");
        new Thread(this).start();
    }

    /**
     * 点击read事件
     */
    @Override
    public void read(View v) {
        InputStream stream = null;
        try {
            stream = mCache.get(CACHE_KEY);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (stream == null) {
            Toast.makeText(this, "Stream cache is null ...", Toast.LENGTH_SHORT).show();
            text.setText("file not found");
            return;
        }
        try {
            text.setText("file size: " + stream.available());
        } catch (IOException e) {
            text.setText("error " + e.getMessage());
        }
    }

    /**
     * 点击clear事件
     */
    @Override
    public void clear(View v) {
        mCache.remove(CACHE_KEY);
    }

    @Override
    public void run() {
        OutputStream ostream = null;
        try {
            ostream = mCache.put(CACHE_KEY);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (ostream == null) {
            Toast.makeText(this, "Open stream error!", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            URL url = new URL(URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            InputStream stream = conn.getInputStream();

            byte[] buff = new byte[1024];
            int counter;
            while ((counter = stream.read(buff)) > 0) {
                ostream.write(buff, 0, counter);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                // cache update
                ostream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    text.setText("done...");
                    Toast.makeText(text.getContext(), "Stream cache successfully.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
