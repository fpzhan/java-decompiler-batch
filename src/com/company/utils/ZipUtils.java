package com.company.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipUtils {


    public static boolean unZip(String zipFilePath,String descDir){
        return unZip(new File(zipFilePath),descDir);
    }

    /**
     * 解压zip文件
     *
     * @return true/false
     */
    public static boolean unZip(File zipFile, String descDir) {

        boolean flag = false;
        File pathFile = new File(descDir);
        if (!pathFile.exists()) {
            pathFile.mkdirs();
        }
        ZipFile zip = null;
        try {
            // 指定编码，否则压缩包里面不能有中文目录
            zip = new ZipFile(zipFile, Charset.forName("gbk"));
            for (Enumeration entries = zip.entries(); entries.hasMoreElements();) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                String zipEntryName = entry.getName();
                InputStream in = zip.getInputStream(entry);
                String outPath = (descDir + zipEntryName).replace("/",
                        File.separator);
                // 判断路径是否存在,不存在则创建文件路径
                File file = new File(outPath.substring(0,
                        outPath.lastIndexOf(File.separator)));
                if (!file.exists()) {
                    file.mkdirs();
                }
                // 判断文件全路径是否为文件夹,如果是上面已经上传,不需要解压
                if (new File(outPath).isDirectory()) {
                    continue;
                }

                OutputStream out = new FileOutputStream(outPath);
                byte[] buf1 = new byte[2048];
                int len;
                while ((len = in.read(buf1)) > 0) {
                    out.write(buf1, 0, len);
                }
                in.close();
                out.close();
            }
            flag = true;
            // 必须关闭，否则无法删除该zip文件
            zip.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return flag;
    }
}
