package com.romancedawn.module;

//интерфейс Downloader
public interface DownloaderCallback {
    void onFinish(String url, String pathToFile, String name, String type, String id, String version);
    void onError(String message);
}
