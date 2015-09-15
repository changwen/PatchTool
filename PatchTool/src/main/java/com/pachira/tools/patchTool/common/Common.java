package com.pachira.tools.patchTool.common;

import java.io.BufferedInputStream;

import java.io.*;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;

/**
 * 生成和安装补丁都需要用到的方法
 *
 * @author   changwen
 * @version  1.0-SNAPSHOT
 * @since    JDK1.7
 */
public class Common {
    /**
     * 创建文档并复制
     *
     * @param sourcePath  被复制的源文件路径
     * @param copyPath    创建复制的目标文件路径
     */
    public void copyFile(String sourcePath, String copyPath) {
        try {
            /*在读写文件需要对内容按行处理，比如比较特定字符，处理某一行数据的时候一般会选择字符流*/
            BufferedInputStream bufferedInputStream = new BufferedInputStream(
                    new FileInputStream(sourcePath));

            int len ;
            byte[] buffer = new byte[1024 * 10];
            createFile(copyPath);    //创建复制的目标文件

            //必须放在copyPath的文件创建好的后面，,不然出异常,因为copyPath对应的文件可能没有
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
                    new FileOutputStream(new File(copyPath)));
            while ((len = bufferedInputStream.read(buffer)) != -1) {
                bufferedOutputStream.write(buffer, 0, len);
            }

            bufferedInputStream.close();
            bufferedOutputStream.close();
        } catch (FileNotFoundException e) {
            System.out.println("Common copyFile FileNotFoundException");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Common copyFile IOException");
            e.printStackTrace();
        }
    }

    /**
     * 获取文件的MD5值：特别注意！！如果调用该方法生成MD5值比较文件是否修改后，删除文件，就会有异常
     * <p>原因：MappedByteBuffer缓冲流没有关闭
     * @param filePath  要获取MD5值的文件的绝对路径
     * @return  返回一个16进制的字符串
     */
    public String getMd5ByFile(String filePath) {
        String value = null;
        File file = new File(filePath);
        FileInputStream in = null;

        try {
            in = new FileInputStream(file);
            //MappedByteBuffer进行缓冲，这样可以保证边读取大文件，边进行处理
            MappedByteBuffer byteBuffer = in.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            //生成实现指定摘要算法
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(byteBuffer);
            //生成无符号整数的方法BigInteger(1,dd);
            BigInteger bi = new BigInteger(1, md5.digest());
            //toString(16)将数字转换为字符串,返回16进制
            value = bi.toString(16);

        } catch (Exception e) {
            System.out.println("Common getMd5ByFile Exception");
            e.printStackTrace();
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    System.out.println("Common getMd5ByFile IOException");
                    e.printStackTrace();
                }
            }

        }
        return value;
    }

    /**
     * 获取文件的MD5值:这个方法用于安装补丁，
     *
     * @param filePath  要获取MD5值的文件的绝对路径
     * @return  返回一个16进制的字符串
     */
    public String getFileMD5(String filePath){
        String value = null;
        File file = new File(filePath);
        if(!file.isFile()){
            return null;
        }
        FileInputStream in=null;
        int len;
        byte buffer[] = new byte[1024 * 8];

        try {
            MessageDigest  digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);

            while ((len = in.read(buffer)) != -1){
                digest.update(buffer,0,len);
            }

            BigInteger bigInt = new BigInteger(1,digest.digest());
            value= bigInt.toString(16);
        } catch (Exception e) {
            System.out.println("getMd5ByFile Exception");
            e.printStackTrace();

        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    System.out.println("getMd5ByFile IOException");
                    e.printStackTrace();
                }
            }

        }
        return value;
    }

    /**
     * 创建单个文件及目录：被copyFile所调用
     *
     * @param filePath  要创建的文件的绝对路径
     * @return  创建文件成功返回true; 目标文件存在、创建文件失败返回false
     */
    private boolean createFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            System.out.println("目标文件已存在" + filePath);
            return false;
        }

        if (!file.getParentFile().exists()) {
            System.out.println("目标文件所在目录不存在，准备创建父文件夹！");
            if (!file.getParentFile().mkdirs()) {
                System.out.println("创建目标文件所在的目录失败！");
                return false;
            }
        }

        try {
            if (file.createNewFile()) {
                System.out.println("创建文件成功:" + filePath);
                return true;
            } else {
                System.out.println("创建文件失败！");
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("createFile 创建文件失败！" + e.getMessage());
            return false;
        }
    }
}
