package com.romancedawn.module;

import android.database.sqlite.SQLiteDatabase;
import com.romancedawn.MainActivity;
import com.yandex.mapkit.map.PlacemarkMapObject;

import java.util.ArrayList;
import java.util.List;

public class Factory {
    private static FileHelper fileHelper;
    private static UpdateGuide updateGuide;
    private static MainActivity mainActivity;
    private static SQLiteDatabase db;

    private static List<PlacemarkMapObject> placemarkList = new ArrayList<>();

    //инициализация статического объекта работы с файлами
    public static FileHelper getFileHelper(){
        if (fileHelper == null)
            fileHelper = new FileHelper(mainActivity);
        return fileHelper;
    }

    //сохранение главного окна в статическую переменную
    public static void setMainActivity(MainActivity mainActivity) {
        Factory.mainActivity = mainActivity;
    }

    // получение главного окна по запросу
    public static MainActivity getMainActivity() {
        return mainActivity;
    }

    //инициализация бд
    public static void initDatabase(String fullPathFile) {
        if (db == null)
            db = SQLiteDatabase.openDatabase(fullPathFile, null, SQLiteDatabase.CREATE_IF_NECESSARY);

    }

    //получение бд
    public static SQLiteDatabase getDatabase(){
        return db;
    }

    //инициализация процедуры обновления
    public static UpdateGuide getUpdateGuide(){
        if ( updateGuide == null)
            updateGuide = new UpdateGuide();
        return updateGuide;
    }

    //инициализация списка хранения меток
    public static List<PlacemarkMapObject> getPlacemarkList() {
        return placemarkList;
    }

}
