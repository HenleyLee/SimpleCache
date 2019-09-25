package com.henley.simplecache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * {@link ACache}是一个为Android制定的轻量级的缓存框架
 * <ul>
 * <strong>注意：</strong>
 * <li>缓存对象：String、JsonObject、JsonArray、byte[]、Bitmap、Drawable、序列化的Java对象；
 * <li>缓存路径：缓存路径默认为{@code /data/data/<package-name>/cache/path}，可以在获取{@link ACache}实例时指定；
 * </ul>
 * <ul>
 * <strong>特点：</strong>
 * <li>轻，轻到只有一个JAVA文件。
 * <li>可配置，可以配置缓存路径，缓存大小，缓存数量等。
 * <li>可以设置缓存超时时间，缓存超时自动失效，并被删除。
 * <li>支持多进程。
 * </ul>
 */
public class ACache {

    /** 缓存保存时间(1小时) */
    public static final int TIME_HOUR = 60 * 60;
    /** 缓存保存时间(1天) */
    public static final int TIME_DAY = TIME_HOUR * 24;

    /** TAG */
    private static final String TAG = "ACache";
    /** 默认的缓存目录名称 */
    private static final String CACHE_NAME = "ACache";
    /** 默认的缓存大小(50 MB) */
    private static final long MAX_SIZE = 1000 * 1000 * 50;
    /** 默认的缓存数量(不限制) */
    private static final int MAX_COUNT = Integer.MAX_VALUE;

    /** 缓存管理器 */
    private ACacheManager mCacheManager;
    /** 声明一个Map，用于存放ACache对象 */
    private static Map<String, ACache> mInstanceMap = new HashMap<String, ACache>();

    /**
     * 获取{@link ACache}实例
     *
     * @param context Context对象
     * @return {@link ACache}实例
     * @see #get(Context, String)
     */
    public static ACache get(Context context) {
        return get(context, CACHE_NAME);
    }

    /**
     * 获取{@link ACache}实例
     *
     * @param context  Context对象
     * @param maxSize  缓存大小
     * @param maxCount 缓存数量
     * @return {@link ACache}实例
     * @see #get(Context, String, long, int)
     */
    public static ACache get(Context context, long maxSize, int maxCount) {
        return get(context, CACHE_NAME, maxSize, maxCount);
    }

    /**
     * 获取{@link ACache}实例
     *
     * @param context   Context对象
     * @param cacheName 缓存目录名称
     * @return {@link ACache}实例
     * @see #get(Context, String, long, int)
     */
    public static ACache get(Context context, String cacheName) {
        return get(context, cacheName, MAX_SIZE, MAX_COUNT);
    }

    /**
     * 获取{@link ACache}实例
     *
     * @param context   Context对象
     * @param cacheName 缓存目录名称
     * @param maxSize   缓存大小
     * @param maxCount  缓存数量
     * @return {@link ACache}实例
     * @see #get(File, long, int)
     */
    public static ACache get(Context context, String cacheName, long maxSize, int maxCount) {
        return get(new File(context.getCacheDir(), cacheName), maxSize, maxCount);
    }

    /**
     * 获取{@link ACache}实例
     *
     * @param cacheDir 缓存目录
     * @return {@link ACache}实例
     * @see #get(File, long, int)
     */
    public static ACache get(File cacheDir) {
        return get(cacheDir, MAX_SIZE, MAX_COUNT);
    }

    /**
     * 获取{@link ACache}实例
     *
     * @param cacheDir 缓存目录
     * @param maxSize  缓存大小
     * @param maxCount 缓存数量
     * @return {@link ACache}实例
     */
    public static ACache get(File cacheDir, long maxSize, int maxCount) {
        // Map中的Key值为cacheDir.getAbsoluteFile() + myPid()，例如：/data/data/<package-name>/cache/ACache_16609
        String instanceKey = cacheDir.getAbsolutePath() + "_" + android.os.Process.myPid();
        ACache manager = mInstanceMap.get(instanceKey);
        if (manager == null) {
            manager = new ACache(cacheDir, maxSize, maxCount);
            mInstanceMap.put(instanceKey, manager);
        }
        return manager;
    }

    /**
     * {@link ACache}类的私有构造方法，只能通过get()方式获取实例。
     *
     * @param cacheDir 缓存目录
     * @param maxSize  缓存大小
     * @param maxCount 缓存数量
     */
    private ACache(File cacheDir, long maxSize, int maxCount) {
        // 如果cacheDir文件不存在并且无法新建子目录，则抛出RuntimeException
        if (!cacheDir.exists() && !cacheDir.mkdirs()) {
            throw new RuntimeException("can't make dirs in " + cacheDir.getAbsolutePath());
        }
        // 实例化缓存管理器对象
        mCacheManager = new ACacheManager(cacheDir, maxSize, maxCount);
    }

    /**
     * 保存{@link String}数据到缓存中
     *
     * @param key   保存的key
     * @param value 保存的String数据
     */
    public boolean put(String key, String value) {
        File file = mCacheManager.newFile(key);
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(file), 1024);
            out.write(value);
            out.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            Utils.closeIOQuietly(out);
            mCacheManager.put(file);
        }
    }

    /**
     * 保存{@link String}数据到缓存中一定时间
     *
     * @param key      保存的key
     * @param value    保存的String数据
     * @param saveTime 保存的时间，单位：秒
     */
    public boolean put(String key, String value, int saveTime) {
        return put(key, Utils.newStringWithDateInfo(value, saveTime));
    }

    /**
     * 读取{@link String}数据
     *
     * @param key 保存的key
     * @return {@link String}数据
     */
    public String getAsString(String key) {
        File file = mCacheManager.get(key);
        if (!file.exists() || !file.isFile()) {
            return null;
        }
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(file));
            StringBuilder builder = new StringBuilder();
            String currentLine;
            while ((currentLine = in.readLine()) != null) {
                builder.append(currentLine);
            }
            if (Utils.isDue(builder.toString())) {
                remove(key);
                return null;
            }
            return Utils.clearDateInfo(builder.toString());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            Utils.closeIOQuietly(in);
        }
    }

    /**
     * 保存{@link JSONObject}数据到缓存中
     *
     * @param key   保存的key
     * @param value 保存的JSONObject数据
     * @see #put(String, String)
     */
    public boolean put(String key, JSONObject value) {
        return put(key, value.toString());
    }

    /**
     * 保存{@link JSONObject}数据到缓存中
     *
     * @param key      保存的key
     * @param value    保存的JSONObject数据
     * @param saveTime 保存的时间，单位：秒
     * @see #put(String, String, int)
     */
    public boolean put(String key, JSONObject value, int saveTime) {
        return put(key, value.toString(), saveTime);
    }

    /**
     * 读取{@link JSONObject}数据
     *
     * @param key 保存的key
     * @return {@link JSONObject}数据
     * @see #getAsString(String)
     */
    public JSONObject getAsJSONObject(String key) {
        String jsonStr = getAsString(key);
        if (jsonStr == null) {
            return null;
        }
        try {
            return new JSONObject(jsonStr);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 保存{@link JSONArray}数据到缓存中
     *
     * @param key   保存的key
     * @param value 保存的JSONArray数据
     * @see #put(String, String)
     */
    public boolean put(String key, JSONArray value) {
        return put(key, value.toString());
    }

    /**
     * 保存{@link JSONArray}数据到缓存中
     *
     * @param key      保存的key
     * @param value    保存的JSONArray数据
     * @param saveTime 保存的时间，单位：秒
     * @see #put(String, String, int)
     */
    public boolean put(String key, JSONArray value, int saveTime) {
        return put(key, value.toString(), saveTime);
    }

    /**
     * 读取{@link JSONArray}数据
     *
     * @param key 保存的key
     * @return {@link JSONArray}数据
     * @see #getAsString(String)
     */
    public JSONArray getAsJSONArray(String key) {
        String jsonStr = getAsString(key);
        if (jsonStr == null) {
            return null;
        }
        try {
            return new JSONArray(jsonStr);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 保存{@code byte[]}数据到缓存中
     *
     * @param key   保存的key
     * @param value 保存的数据
     */
    public boolean put(String key, byte[] value) {
        File file = mCacheManager.newFile(key);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            out.write(value);
            out.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            Utils.closeIOQuietly(out);
            mCacheManager.put(file);
        }
    }

    /**
     * 保存{@code byte[]}数据到缓存中
     *
     * @param key      保存的key
     * @param value    保存的数据
     * @param saveTime 保存的时间，单位：秒
     * @see #put(String, byte[])
     */
    public boolean put(String key, byte[] value, int saveTime) {
        return put(key, Utils.newByteArrayWithDateInfo(value, saveTime));
    }

    /**
     * 获取{@code byte[]}数据
     *
     * @param key 保存的key
     * @return {@code byte[]}数据
     */
    public byte[] getAsBinary(String key) {
        File file = mCacheManager.get(key);
        if (!file.exists() || !file.isFile()) {
            return null;
        }
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(file, "r");
            byte[] byteArray = new byte[(int) raf.length()];
            raf.read(byteArray);
            if (Utils.isDue(byteArray)) {
                remove(key);
                return null;
            }
            return Utils.clearDateInfo(byteArray);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            Utils.closeIOQuietly(raf);
        }
    }

    /**
     * 保存{@link Serializable}数据到缓存中
     *
     * @param key   保存的key
     * @param value 保存的value
     * @see #put(String, Serializable, int)
     */
    public boolean put(String key, Serializable value) {
        return put(key, value, -1);
    }

    /**
     * 保存{@link Serializable}数据到缓存中
     *
     * @param key      保存的key
     * @param value    保存的value
     * @param saveTime 保存的时间，单位：秒
     * @see #put(String, byte[], int)
     */
    public boolean put(String key, Serializable value, int saveTime) {
        ByteArrayOutputStream baos;
        ObjectOutputStream oos = null;
        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(value);
            oos.flush();
            byte[] data = baos.toByteArray();
            if (saveTime != -1) {
                return put(key, data, saveTime);
            } else {
                return put(key, data);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            Utils.closeIOQuietly(oos);
        }
    }

    /**
     * 读取{@link Serializable}数据
     *
     * @param key 保存的key
     * @return {@link Serializable}数据
     */
    public Object getAsObject(String key) {
        byte[] bytes = getAsBinary(key);
        if (bytes == null) {
            return null;
        }
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
            return ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            Utils.closeIOQuietly(ois);
        }
    }

    /**
     * 保存{@link Bitmap}到缓存中
     *
     * @param key   保存的key
     * @param value 保存的Bitmap数据
     * @see #put(String, byte[])
     */
    public boolean put(String key, Bitmap value) {
        return put(key, Utils.bitmap2Bytes(value));
    }

    /**
     * 保存{@link Bitmap}到缓存中
     *
     * @param key      保存的key
     * @param value    保存的Bitmap数据
     * @param saveTime 保存的时间，单位：秒
     * @see #put(String, byte[], int)
     */
    public boolean put(String key, Bitmap value, int saveTime) {
        return put(key, Utils.bitmap2Bytes(value), saveTime);
    }

    /**
     * 读取{@link Bitmap}数据
     *
     * @param key 保存的key
     * @return Bitmap数据
     * @see #getAsBinary(String)
     */
    public Bitmap getAsBitmap(String key) {
        byte[] bytes = getAsBinary(key);
        if (bytes == null) {
            return null;
        }
        return Utils.bytes2Bitmap(bytes);
    }

    /**
     * 保存{@link Drawable}到缓存中
     *
     * @param key   保存的key
     * @param value 保存的Drawable数据
     * @see #put(String, Bitmap)
     */
    public boolean put(String key, Drawable value) {
        return put(key, Utils.drawable2Bitmap(value));
    }

    /**
     * 保存{@link Drawable}到缓存中
     *
     * @param key      保存的key
     * @param value    保存的Drawable数据
     * @param saveTime 保存的时间，单位：秒
     * @see #put(String, Bitmap, int)
     */
    public boolean put(String key, Drawable value, int saveTime) {
        return put(key, Utils.drawable2Bitmap(value), saveTime);
    }

    /**
     * 读取{@link Drawable}数据
     *
     * @param key 保存的key
     * @return Drawable数据
     * @see #getAsBinary(String)
     */
    public Drawable getAsDrawable(String key) {
        byte[] bytes = getAsBinary(key);
        if (bytes == null) {
            return null;
        }
        return Utils.bitmap2Drawable(Utils.bytes2Bitmap(bytes));
    }

    /**
     * Cache for a stream
     *
     * @param key the file name.
     * @return OutputStream stream for writing data.
     * @throws FileNotFoundException if the file can not be created.
     */
    public OutputStream put(String key) throws FileNotFoundException {
        return new xFileOutputStream(key, mCacheManager);
    }

    /**
     * @param key the file name.
     * @return (InputStream or null) stream previously saved in cache.
     * @throws FileNotFoundException if the file can not be opened
     */
    public InputStream get(String key) throws FileNotFoundException {
        File file = mCacheManager.get(key);
        if (!file.exists() || !file.isFile()) {
            return null;
        }
        return new FileInputStream(file);
    }

    /**
     * 获取缓存文件
     *
     * @param key 保存的key
     * @return value 缓存的文件
     */
    public File file(String key) {
        File file = mCacheManager.newFile(key);
        if (file.exists()) {
            return file;
        }
        return null;
    }

    /**
     * 移除某个key
     *
     * @param key 保存的key
     * @return 是否移除成功
     */
    public boolean remove(String key) {
        return mCacheManager.remove(key);
    }

    /**
     * 清除所有数据
     */
    public void clear() {
        mCacheManager.clear();
    }

    /**
     * Provides a means to save a cached file before the data are available.
     * Since writing about the file is complete, and its close method is called,
     * its contents will be registered in the cache. Example of use:
     * <p>
     * ACache cache = new ACache(this) try { OutputStream stream =
     * cache.put("myFileName") stream.write("some bytes".getBytes()); // now
     * update cache! stream.close(); } catch(FileNotFoundException e){
     * e.printStackTrace() }
     */
    private static final class xFileOutputStream extends FileOutputStream {

        private File file;
        private ACacheManager mCacheManager;

        private xFileOutputStream(String key, ACacheManager cacheManager) throws FileNotFoundException {
            super(cacheManager.newFile(key));
            this.mCacheManager = cacheManager;
            this.file = cacheManager.newFile(key);
        }

        public void close() throws IOException {
            super.close();
            mCacheManager.put(file);
        }

    }

    /**
     * 缓存管理器
     */
    private static final class ACacheManager {

        private final AtomicLong cacheSize = new AtomicLong();
        private final AtomicInteger cacheCount = new AtomicInteger();
        private final Map<File, Long> lastUsageDates = Collections.synchronizedMap(new HashMap<File, Long>());

        private final File cacheDir;
        private final long sizeLimit;
        private final int countLimit;

        private ACacheManager(File cacheDir, long sizeLimit, int countLimit) {
            this.cacheDir = cacheDir;
            this.sizeLimit = sizeLimit;
            this.countLimit = countLimit;

            calculateCacheSizeAndCacheCount();
        }

        /**
         * 计算{@code cacheSize}和{@code cacheCount}
         */
        private void calculateCacheSizeAndCacheCount() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int size = 0;
                    int count = 0;
                    File[] cachedFiles = cacheDir.listFiles();
                    if (cachedFiles != null) {
                        for (File cachedFile : cachedFiles) {
                            size += calculateSize(cachedFile);
                            count += 1;
                            lastUsageDates.put(cachedFile, cachedFile.lastModified());
                        }
                        cacheSize.set(size);
                        cacheCount.set(count);
                    }
                }
            }).start();
        }

        /**
         * 保存文件
         */
        private void put(File file) {
            int curCacheCount = cacheCount.get();
            while (curCacheCount + 1 > countLimit) {
                long freedSize = removeNext();
                cacheSize.addAndGet(-freedSize);

                curCacheCount = cacheCount.addAndGet(-1);
            }
            cacheCount.addAndGet(1);

            long valueSize = calculateSize(file);
            long curCacheSize = cacheSize.get();
            while (curCacheSize + valueSize > sizeLimit) {
                long freedSize = removeNext();
                curCacheSize = cacheSize.addAndGet(-freedSize);
            }
            cacheSize.addAndGet(valueSize);

            long currentTime = System.currentTimeMillis();
            if (file.setLastModified(currentTime)) {
                Log.d(TAG, String.format("%s set the last-modified time %d.", file.getAbsolutePath(), currentTime));
            }
            lastUsageDates.put(file, currentTime);
        }

        /**
         * 读取文件
         */
        private File get(String key) {
            File file = newFile(key);
            long currentTime = System.currentTimeMillis();
            if (file.setLastModified(currentTime)) {
                Log.d(TAG, String.format("%s set the last-modified time %d.", file.getAbsolutePath(), currentTime));
            }
            lastUsageDates.put(file, currentTime);
            return file;
        }

        /**
         * 创建新的文件实例
         */
        private File newFile(String key) {
            return new File(cacheDir, String.valueOf(key.hashCode()));
        }

        /**
         * 移除文件
         */
        private boolean remove(String key) {
            File file = get(key);
            return file.delete();
        }

        /**
         * 清空缓存文件
         */
        private void clear() {
            cacheSize.set(0);
            lastUsageDates.clear();
            File[] files = cacheDir.listFiles();
            if (files == null || files.length == 0) {
                return;
            }
            for (File file : files) {
                if (file.delete()) {
                    Log.d(TAG, String.format("%s is successfully deleted.", file.getAbsolutePath()));
                }
            }
        }

        /**
         * 移除旧的文件
         */
        private long removeNext() {
            if (lastUsageDates.isEmpty()) {
                return 0;
            }

            Long oldestUsage = null;
            File mostLongUsedFile = null;
            Set<Entry<File, Long>> entries = lastUsageDates.entrySet();
            synchronized (lastUsageDates) {
                for (Entry<File, Long> entry : entries) {
                    if (mostLongUsedFile == null) {
                        mostLongUsedFile = entry.getKey();
                        oldestUsage = entry.getValue();
                    } else {
                        Long lastValueUsage = entry.getValue();
                        if (lastValueUsage < oldestUsage) {
                            oldestUsage = lastValueUsage;
                            mostLongUsedFile = entry.getKey();
                        }
                    }
                }
            }

            long fileSize = calculateSize(mostLongUsedFile);
            if (mostLongUsedFile != null && mostLongUsedFile.delete()) {
                lastUsageDates.remove(mostLongUsedFile);
            }
            return fileSize;
        }

        /**
         * 计算文件大小
         */
        private long calculateSize(File file) {
            return file == null ? 0 : file.length();
        }
    }

    /**
     * 时间计算工具类
     */
    private static final class Utils {

        private static final char SEPARATOR = ' ';

        /**
         * 判断缓存的String数据是否到期
         *
         * @param str String数据
         * @return true：到期了；false：还没有到期
         * @see #isDue(byte[])
         */
        private static boolean isDue(String str) {
            return isDue(str.getBytes());
        }

        /**
         * 判断缓存的byte[]数据是否到期
         *
         * @param data byte[]数据
         * @return true：到期了；false：还没有到期
         */
        private static boolean isDue(byte[] data) {
            String[] infos = getDateInfoFromDate(data);
            if (infos == null || infos.length != 2) {
                return false;
            }
            String saveTimeStr = infos[0];
            while (saveTimeStr.startsWith("0")) {
                saveTimeStr = saveTimeStr.substring(1);
            }
            long saveTime = Long.valueOf(saveTimeStr);  // 保存时的时间(单位：毫秒)
            long deleteAfter = Long.valueOf(infos[1]);  // 保存的时间(单位：秒)
            return System.currentTimeMillis() > saveTime + deleteAfter * 1000;
        }

        /**
         * 将保存的数据和保存的时间组合成新的String数据
         *
         * @param value   保存的数据
         * @param seconds 保存的时间
         * @return 组合成的新的String数据
         */
        private static String newStringWithDateInfo(String value, long seconds) {
            return createDateInfo(seconds) + value;
        }

        /**
         * 将保存的数据和保存的时间组合成新的byte[]数据
         *
         * @param data2   保存的数据
         * @param seconds 保存的时间
         * @return 组合成的新的byte[]数据
         */
        private static byte[] newByteArrayWithDateInfo(byte[] data2, int seconds) {
            byte[] data1 = createDateInfo(seconds).getBytes();
            byte[] retdata = new byte[data1.length + data2.length];
            System.arraycopy(data1, 0, retdata, 0, data1.length);
            System.arraycopy(data2, 0, retdata, data1.length, data2.length);
            return retdata;
        }

        /**
         * 将保存的时间转换为String数据
         *
         * @param seconds 保存的时间
         * @return 保存的时间转换成的String数据
         */
        private static String createDateInfo(long seconds) {
            String currentTime = String.valueOf(System.currentTimeMillis());
            while (currentTime.length() < 13) {
                currentTime = "0" + currentTime;
            }
            return currentTime + "-" + seconds + SEPARATOR;
        }

        /**
         * 清除String数据中的时间信息
         *
         * @param strInfo String数据
         * @return 清除时间信息后的String数据
         */
        private static String clearDateInfo(String strInfo) {
            if (strInfo != null && hasDateInfo(strInfo.getBytes())) {
                strInfo = strInfo.substring(strInfo.indexOf(SEPARATOR) + 1);
            }
            return strInfo;
        }

        /**
         * 清除byte[]数据中的时间信息
         *
         * @param data byte[]数据
         * @return 清除时间信息后的byte[]数据
         */
        private static byte[] clearDateInfo(byte[] data) {
            if (hasDateInfo(data)) {
                return copyOfRange(data, indexOf(data) + 1, data.length);
            }
            return data;
        }

        /**
         * 判断byte[]数据中是否包含有时间信息
         *
         * @param data byte[]数据
         * @return true：包含；false：不包含
         */
        private static boolean hasDateInfo(byte[] data) {
            return data != null && data.length > 15 && data[13] == '-' && indexOf(data) > 14;
        }

        /**
         * 获取byte[]数据中包含的时间信息(保存时的时间、保存的时间)
         *
         * @param data byte[]数据
         * @return byte[]数据中包含的时间信息
         */
        private static String[] getDateInfoFromDate(byte[] data) {
            if (hasDateInfo(data)) {
                // 保存时的时间(单位：毫秒)
                String saveDate = new String(copyOfRange(data, 0, 13));
                // 保存的时间(单位：秒)
                String deleteAfter = new String(copyOfRange(data, 14, indexOf(data)));
                return new String[]{saveDate, deleteAfter};
            }
            return null;
        }

        /**
         * 获取{@link #SEPARATOR}在byte[]数据中的索引
         *
         * @param data byte[]数据
         * @return {@link #SEPARATOR}在byte[]数据中的索引
         */
        private static int indexOf(byte[] data) {
            for (int i = 0; i < data.length; i++) {
                if (data[i] == SEPARATOR) {
                    return i;
                }
            }
            return -1;
        }

        /**
         * 将数组从指定的源数组复制到目标数组
         *
         * @param original 源数组
         * @param from     开始位置
         * @param to       结束位置
         * @return 复制后的数组
         */
        private static byte[] copyOfRange(byte[] original, int from, int to) {
            int newLength = to - from;
            if (to < from) {
                throw new IllegalArgumentException(from + " > " + to);
            }
            byte[] copy = new byte[to - from];
            System.arraycopy(original, from, copy, 0, Math.min(original.length - from, newLength));
            return copy;
        }

        /**
         * 将Bitmap转换为byte[]
         */
        private static byte[] bitmap2Bytes(Bitmap bitmap) {
            if (bitmap == null) {
                return null;
            }
            ByteArrayOutputStream baos = null;
            try {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                return baos.toByteArray();
            } catch (Exception e) {
                return null;
            } finally {
                closeIOQuietly(baos);
            }
        }

        /**
         * 将byte[]转换为Bitmap
         */
        private static Bitmap bytes2Bitmap(byte[] bytes) {
            if (bytes == null || bytes.length == 0) {
                return null;
            }
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }

        /**
         * 将Drawable转换为Bitmap
         */
        private static Bitmap drawable2Bitmap(Drawable drawable) {
            if (drawable == null) {
                return null;
            }
            // 取 drawable 的长宽
            int width = drawable.getIntrinsicWidth();
            int height = drawable.getIntrinsicHeight();
            // 取 drawable 的颜色格式
            Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
            // 建立对应 bitmap
            Bitmap bitmap = Bitmap.createBitmap(width, height, config);
            // 建立对应 bitmap 的画布
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, width, height);
            // 把 drawable 内容画到画布中
            drawable.draw(canvas);
            return bitmap;
        }

        /**
         * 将Bitmap转换为Drawable
         */
        private static Drawable bitmap2Drawable(Bitmap bitmap) {
            if (bitmap == null) {
                return null;
            }
            BitmapDrawable drawable = new BitmapDrawable(null, bitmap);
            drawable.setTargetDensity(bitmap.getDensity());
            return drawable;
        }

        private static void closeIOQuietly(Closeable... closeables) {
            if (closeables == null || closeables.length <= 0) {
                return;
            }
            for (Closeable closeable : closeables) {
                if (closeable != null) {
                    try {
                        closeable.close();
                    } catch (IOException ignored) {
                        // ignore
                    }
                }
            }
        }

    }

    //</editor-fold>

}
