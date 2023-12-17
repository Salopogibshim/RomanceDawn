package com.romancedawn.module;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

public class UpdateGuide {
    private SQLiteDatabase db;
    private Cursor sqlCursor;

    //проверка версии бд
    public UpdateGuide() {
        db = Factory.getDatabase();
        //получаем данные из бд в виде курсора
        sqlCursor = db.rawQuery("SELECT * FROM version where id = 0", null);
        if (sqlCursor.getCount() != 0) {
            sqlCursor.moveToFirst();
            String name = sqlCursor.getString(1);
            String type = sqlCursor.getString(2);
            String url = sqlCursor.getString(3);
            String version = sqlCursor.getString(4);
            Download(url, Factory.getFileHelper().getFilePath() + "files/", name, type, "0", version);
        }
        sqlCursor.close();
    }
    //скачивание новых файлов
    public void Download(String url, String path, String name, String type, String id, String version) {
        Downloader dl = new Downloader(url, path, name, type, id, version);
        dl.setDownloaderCallback(new DownloaderCallback(){
        @Override
        //переопределение методов событий
            public void onFinish(String url, String pathToFile, String name, String type, String id, String version){
                if (name.equals("version")){
                    readJSON(name + "." + type);
                } else UpdateContent(url, pathToFile, name, type, id, version);
            }
        @Override
            public void onError(String message){
                System.out.println(message);
            }
    });

dl.start();
    }

    //обновление данных
    public void UpdateContent (String url, String pathToFile, String name, String type, String id, String version){
        sqlCursor = db.rawQuery("SELECT * FROM version where id = ? and name = ? and type = ?", new String[] {id, name, type});
        if (sqlCursor.getCount() == 0) {
            String sql = "INSERT INTO version (id, name, type, url, version) VALUES(?, ?, ?, ?, ?)";
            db.execSQL(sql, new String[]{id, name, type, url, version});
        } else {
            // проверка типа данных для обновления
            if (type.equals("html")) {
                String sql="UPDATE version SET url= ?,version=? WHERE id=?";
                db.execSQL(sql, new String[]{url, version, id});
            }
            if (type.equals("jpg")) {
                String sql="UPDATE version SET url= ?,version=? WHERE id=?";
                db.execSQL(sql, new String[]{url, version, id});
            }
        }
        sqlCursor.close();

        //обновление бд
        if (type.equals("sql")) {
            //readln sql and insert to route
            BufferedReader reader;

            try {
                reader = new BufferedReader(new FileReader(pathToFile + name + "." + type));
                String line = reader.readLine();

                while (line != null) {
                    db.execSQL(line);
                    // read next line
                    line = reader.readLine();
                }

                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            String sql="UPDATE version SET url= ?,version=? WHERE id=?";
            db.execSQL(sql, new String[]{url, version, id});
        }
    }

    //чтение json файла
    public void readJSON(String pathToFile) {
        try {
            // get JSONObject from JSON file
            JSONObject obj = new JSONObject(loadJSON(pathToFile));
            // fetch JSONArray
            JSONArray versionArray = obj.getJSONArray("version");
            // implement for loop for getting version list data
            for (int i = 0; i < versionArray.length(); i++) {
                // create a JSONObject for fetching single version data
                JSONObject versionDetail = versionArray.getJSONObject(i);

                compareJSONtoSQL(versionDetail.getString("id"), versionDetail.getString("name"), versionDetail.getString("type"), versionDetail.getString("url"), versionDetail.getString("version"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //открытие для чтения json файла
    public String loadJSON(String pathToFile) {
        String json = null;
        try {
            InputStream is = Factory.getMainActivity().openFileInput(pathToFile);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    //сравнение информации json с sql
    public void compareJSONtoSQL(String id, String name, String type, String url, String version) {
        sqlCursor = db.rawQuery("SELECT * FROM version where id = ? and name = ? and type = ? and url = ? and version = ?", new String[] {id, name, type, url, version});
        if (sqlCursor.getCount() != 0) {
            // no changes
            sqlCursor.close();
        } else if (id.equals("0")) {
            String sql="UPDATE version SET url= ?,version=? WHERE id=0";
            db.execSQL(sql, new String[]{url, version});
        } else {
            if (type.equals("sql")) {
                Download(url, Factory.getFileHelper().getFilePath() + "databases/", name, type, id, version);
            } else
                Download(url, Factory.getFileHelper().getFilePath() + "files/", name, type, id, version);
        }

    }

}
