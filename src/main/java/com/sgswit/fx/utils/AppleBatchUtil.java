package com.sgswit.fx.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class AppleBatchUtil {

    public static final String PATH =  System.getProperty("user.home") + "/.apple_batch/";
    public static final String LOCAL_FILE_STORAGE_PATH =  PATH + "fileSystem";

    static {
        File dir = new File(PATH);
        if (!dir.exists()) {
            dir.mkdir();
        }
        File dir2 = new File(LOCAL_FILE_STORAGE_PATH);
        if (!dir2.exists()) {
            dir2.mkdir();
        }
    }

    public static File getNewsFile(){
        return new File(PATH, "news.ini");
    }

    public static void writeNews(String title, String content) throws IOException {
        File newsFile = getNewsFile();
        if(newsFile.exists()){
            newsFile.delete();
        }
        String path = PATH + "news.ini";
        BufferedWriter writer = new BufferedWriter(new FileWriter(path, true));
        try{
            writer.write(title+":");
            writer.write("\n");
            writer.write("\n");
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            writer.flush();
            writer.close();
        }
    }

    public static FileLock getLock() throws IOException {
        FileLock lock = FileChannel.open(
                        Paths.get(PATH, "single_instance.lock"),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE)
                .tryLock();
        return lock;
    }
}
