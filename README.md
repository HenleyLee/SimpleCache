# SimpleCache —— SimpleCache 是一个为 Android 制定的轻量级的缓存框架。

## Download ##
### Gradle ###
```gradle
dependencies {
    implementation 'com.henley.android:simplecache:1.0.0'
}
```

### APK Demo ###

下载 [APK-Demo](https://github.com/HenleyLee/SimpleCache/raw/master/app/app-release.apk)

## 缓存对象 ##
缓存对象：String、JsonObject、JsonArray、byte[]、Bitmap、Drawable、序列化的 Java 对象。

## 缓存路径 ##
缓存路径：缓存路径默认为 `/data/data/<package-name>/cache/path`，可以在获取 `ACache` 实例时指定。

## 特点 ##
 * 轻，轻到只有一个 Java 文件。
 * 可配置，可以配置缓存路径、缓存大小、缓存数量等。
 * 可以设置缓存超时时间，缓存超时自动失效，并删除缓存文件。
 * 支持多进程。
 
 ## 致谢 ##
 [ASimpleCache](https://github.com/yangfuhai/ASimpleCache)
 
 