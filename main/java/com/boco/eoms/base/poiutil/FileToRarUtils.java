package com.boco.eoms.base.poiutil;



import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * 将一个或多个文件压缩成RAR格式 
 * @author Administrator
 *
 */

public class FileToRarUtils {
    
    private static final Logger log = LoggerFactory.getLogger(FileToRarUtils.class);  
    
    static final int BUFFER = 8192;     
    
    private File rarFile;
    
    public FileToRarUtils() {
    }
    
    /**
     * 生成的rar文件存放路径,压缩后的压缩文件存放路径（包括文件名）
     * @param pathName
     */
    public FileToRarUtils(String rarPathName) {    
        if (File.separatorChar == '/') {
            rarPathName = rarPathName.replace("\\", "/");
        }
        else {
            rarPathName = rarPathName.replace("/", "\\");
        } 
        rarFile = new File(rarPathName);     
    }
    
    
    
    
    /**
     * srcPathNames 文件或文件夹路径,需要压缩的文件名(必须包含路径) 
     * 解析多个文件
     * @param filePath
     * @return 
     */
    public void compress(String  localPath) {
        rarFile = new File(localPath);
        if(!rarFile.exists()) {
            log.error("指定rar包存放的路径不存在,自动新建目标路径！");
            File parent = rarFile.getParentFile();  
            System.out.println(parent.getName());
            if(parent!=null&&!parent.exists()){  
                parent.mkdirs();  
            }  
        }
    }     
    
    /**
     * srcPathNames 文件或文件夹路径,需要压缩的文件名(必须包含路径) 
     * 解析多个文件
     * @param filePath
     * @return 
     */
    public String compress(List filePath,String  localPath) {
        if (File.separatorChar == '/') {
            localPath = localPath.replace("\\", "/");
        }
        else {
            localPath = localPath.replace("/", "\\");
        }
        rarFile = new File(localPath);
        
        if(!rarFile.exists()) {
            log.error("指定rar包存放的路径不存在,自动新建目标路径！");
            File parent = rarFile.getParentFile();  
            if(parent!=null&&!parent.exists()){  
                parent.mkdirs();  
            }  
            try {
                rarFile.createNewFile();
            }
            catch (IOException e) {
                e.printStackTrace();
            }  
        }
        
        ZipOutputStream out = null;
        try {    
            FileOutputStream fileOutputStream = new FileOutputStream(localPath);     
            CheckedOutputStream cos = new CheckedOutputStream(fileOutputStream,new CRC32());     
            out = new ZipOutputStream(cos);     
            String basedir = ""; 
            for (int i=0;i<filePath.size();i++){
                isDirFile(new File((String)filePath.get(i)), out, basedir);     
            }  
            out.close();    
        } catch (Exception e) {
            log.error("RAR文件失败",e);  
            throw new RuntimeException(e);     
        }
        
        return rarFile.getAbsolutePath();
    }     
    
    /**
     * srcPathNames 文件或文件夹路径,需要压缩的文件名(必须包含路径) 
     * 解析多个文件
     * @param filePath
     * @return 
     */
    public String getCompress(String  localPath) {
        if (File.separatorChar == '/') {
            localPath = localPath.replace("\\", "/");
        }
        else {
            localPath = localPath.replace("/", "\\");
        }
        rarFile = new File(localPath);
        if(!rarFile.exists()) {
            log.error("指定rar包存放的路径不存在,自动新建目标路径！");
            File parent = rarFile.getParentFile();  
            if(parent!=null&&!parent.exists()){  
                parent.mkdirs();  
            }  
            try {
                rarFile.createNewFile();
            }
            catch (IOException e) {
                e.printStackTrace();
            }  
        }
        
        try {    
            FileOutputStream fileOutputStream = new FileOutputStream(localPath);     
            CheckedOutputStream cos = new CheckedOutputStream(fileOutputStream,new CRC32());     
        } catch (Exception e) {
            log.error("RAR文件失败",e);  
            throw new RuntimeException(e);     
        }
        
        return rarFile.getAbsolutePath();
    }   
    /**
     * 解析一个文件或文件夹
     * 文件或文件夹路径,需要压缩的文件名(必须包含路径)
     * srcPathName
     * @param srcPathName
     */
    public void compress(String srcPathName,String path) {
        File file = new File(srcPathName);     
        if (!file.exists()) {
            log.error("不存在要压缩的文件！");
            throw new RuntimeException("文件" + srcPathName + "不存在！");     
        }
        try {     
            FileOutputStream fileOutputStream = new FileOutputStream(path);     
            /*CheckedOutputStream:需要维护写入数据校验和的输出流。校验和可用于验证输出数据的完整性。 
             * CRC32:
             * 可用于计算数据流的 CRC-32 的类。 
             * */
            CheckedOutputStream cos = new CheckedOutputStream(fileOutputStream, new CRC32());     
            ZipOutputStream out = new ZipOutputStream(cos);     
            String basedir = "";     
            isDirFile(file, out, basedir);     
            out.close();
        } catch (Exception e) {
            log.error("RAR文件失败",e);
            throw new RuntimeException(e);     
        }     
    }     
    
    /**
     * 分别解析List文件：
     * 判断是目录还是文件
     * @param file
     * @param out
     * @param basedir
     */
    private void isDirFile(File file, ZipOutputStream out, String basedir) {
        /* 判断是目录还是文件 */    
        if (file.isDirectory()) {     
            System.out.println("压缩是目录：" + basedir + file.getName());     
            this.compressDirectory(file, out, basedir);     
        } else {     
            System.out.println("压缩是文件：" + basedir + file.getName());     
            this.compressFile(file, out, basedir);     
        }     
    }     
    
    /**
     * 压缩一个目录
     * @param dir
     * @param out
     * @param basedir
     */
    private void compressDirectory(File dir, ZipOutputStream out, String basedir) {     
        if (!dir.exists())  { 
            log.error("不存在要压缩的文件！");
            throw new RuntimeException(dir + "不存在！");  
        }
        File[] files = dir.listFiles();     
        for (int i = 0; i < files.length; i++) {     
            //递归遍历子文件夹  
            isDirFile(files[i], out, basedir + dir.getName() + "/");     
        }     
    }     
    
    /**
     * 压缩一个文件   
     * @param file
     * @param out
     * @param basedir
     */
    private void compressFile(File file, ZipOutputStream out, String basedir) {     
        if (!file.exists()) {     
            throw new RuntimeException(file + "不存在！");   
        }     
        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));     
            ZipEntry entry = new ZipEntry(basedir + file.getName()); // 创建rar压缩进入路径文件     
            out.putNextEntry(entry);
            int count;     
            byte data[] = new byte[BUFFER];     
            while ((count = bis.read(data, 0, BUFFER)) != -1) {     
                out.write(data, 0, count);     
            }     
            bis.close();     
        } catch (Exception e) {
            log.error("创建RAR压缩文件失败！",e);
            throw new RuntimeException(e);     
        }
    }     
    
    
}
