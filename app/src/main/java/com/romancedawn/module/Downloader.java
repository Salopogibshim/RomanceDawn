package com.romancedawn.module;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class Downloader extends Thread implements Runnable{
        private String url;
        private String path;
        private String name;
        private String type;
        private String id;
        private String version;
        private DownloaderCallback listener=null;

        //конструктор Downloader
        public Downloader(String url, String path, String name, String type, String id, String version){
            this.path=path;
            this.url=url;
            this.name=name;
            this.type=type;
            this.id=id;
            this.version=version;
        }

        //запуск скачивания
        public void run() {
            try {
                URL url = new URL(this.url);
                URLConnection urlConnection = url.openConnection();
                urlConnection.connect();

                int count;

                InputStream input = new BufferedInputStream(url.openStream());
                new File(path).mkdirs();
                OutputStream output = new FileOutputStream(path + name + "." + type);

                byte data[] = new byte[4096];
                long current = 0;

                while ((count = input.read(data)) != -1) {
                    current += count;
                    output.write(data, 0, count);
                }

                output.flush();

                output.close();
                input.close();

                if(listener!=null){
                    listener.onFinish(this.url, this.path, this.name, this.type, this.id, this.version);
                }
            } catch (Exception e) {
                if(listener!=null)
                    listener.onError(e.getMessage());
            }
        }

        //функция обратного вызова
        public void setDownloaderCallback(DownloaderCallback listener){
            this.listener=listener;
        }
}
