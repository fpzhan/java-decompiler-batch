package com.company.utils;

import java.io.File;

public class PathUtils {

    public static String removeLastPathSeparator(String path){
        while(path.endsWith(File.separator)){
            path=path.substring(0,path.length()-1);
        }
        return path;
    }
}
