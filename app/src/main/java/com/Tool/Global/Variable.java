package com.Tool.Global;

import android.os.Environment;

public class Variable {
    public static boolean isBigEnding = false;

    public static String StorageDirectoryPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static String ErrorFilePath;
}
