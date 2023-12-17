package com.romancedawn.module;

import android.content.Context;

import com.romancedawn.MainActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileHelper {
    private static String FILE_PATH = "";
    private final Context mContext;

    //конструктор FileHelper
    public FileHelper(MainActivity context) {
        if (android.os.Build.VERSION.SDK_INT >= 17)
            FILE_PATH = context.getApplicationInfo().dataDir + "/";
        else
            FILE_PATH = "/data/data/" + context.getPackageName() + "/";
        this.mContext = context;
    }

    //проверка файла
    private boolean checkFile(String fullPathName) {
        File file = new File(fullPathName);
        return file.exists();
    }

    //копирование файла
    public String copyFile(String partPATH, String name) {
            if (!checkFile(FILE_PATH + partPATH + name)) {
                try {
                    copyBINFile(partPATH, name);
                } catch (IOException mIOException) {
                    throw new Error("ErrorCopyingDataFile");
                }
            }
        return FILE_PATH + partPATH + name;
    }

    //копирование бинарного файла
    private void copyBINFile(String partPATH, String name) throws IOException {
        InputStream mInput = mContext.getAssets().open(name);
        new File(FILE_PATH + partPATH).mkdirs();
        OutputStream mOutput = new FileOutputStream(FILE_PATH + partPATH + name);
        byte[] mBuffer = new byte[1024];
        int mLength;
        while ((mLength = mInput.read(mBuffer)) > 0)
            mOutput.write(mBuffer, 0, mLength);
        mOutput.flush();
        mOutput.close();
        mInput.close();
    }

    //получение пути к файлу
    public String getFilePath() {
        return FILE_PATH;
    }
}
