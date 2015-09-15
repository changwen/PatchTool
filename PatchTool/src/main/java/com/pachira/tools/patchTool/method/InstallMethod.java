package com.pachira.tools.patchTool.method;

import com.pachira.tools.patchTool.common.Common;

import java.io.*;

/**
 *  检验--备份--安装--检验--(回滚).确认无误后，删除补丁包<br>
 1、第一次检验是确认补丁包在传输过程中有无修改，手动获得<br>
 2、第二次检验是确认安装过程有文件有无修改
 *
 * @author    changwen
 * @version   1.0-SNAPSHOT
 * @since     JDK1.7
 */
public class InstallMethod {
    /**
     * 将旧版本有变化的文件在补丁包里备份
     *
     * @param patchRootPath  补丁包的根目录
     * @param oldRootPath  旧版本的根目录
     */
    public void backUp(String patchRootPath, String oldRootPath) {
        try {
            /*只是读写文件，和文件内容无关的,用字符流*/
            String patchList = patchRootPath + File.separatorChar + "patchList.txt";
            Reader in = new FileReader(patchList);
            BufferedReader bufferedReader = new BufferedReader(in);

            //在patch里创建backup目录,用来备份旧版本相关文件
            File backupDir = new File(patchRootPath + File.separatorChar + "backup");
            if (!backupDir.exists()) {
                backupDir.mkdir();
            }

            String str ;
            while ((str = bufferedReader.readLine()) != null) {
                //记录的字符串如果有add，表明在旧版本里将要增加的，不用备份
                if (!str.matches("^\\badd\\b.*")) {
                    if (str.matches("^\\bdelete\\b.*")) {   //有delete,表明在旧版本将要删除的，在/backup里创建；
                        String relativePath = spiltTxt(str);
                        common.copyFile(oldRootPath + relativePath,
                                patchRootPath + File.separatorChar + "backup" + relativePath);

                    } else if (str.matches("^\\bmodify\\b.*")) {//有modify，表明需要修改该旧版本的文件内容，将旧版本的文件在/backup创建
                        String relativePath = spiltTxt(str);
                        common.copyFile(oldRootPath + relativePath,
                                patchRootPath + File.separatorChar + "backup" + relativePath);
                    }
                }
            }

            bufferedReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("backUp FileNotFoundException");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("backUp IOException");
            e.printStackTrace();
        }
    }

    /**
     * 安装补丁
     *
     * @param patchRootPath  补丁包的根目录
     * @param oldRootPath   旧版本的根目录
     */
    public void installPatch(String patchRootPath, String oldRootPath) {
        try {
            /*只是读写文件，和文件内容无关的,用字符流*/
            String patchList = patchRootPath + File.separatorChar + "patchList.txt";
            Reader in = new FileReader(patchList);
            BufferedReader bufferedReader = new BufferedReader(in);

            String str ;
            while ((str = bufferedReader.readLine()) != null) {
                if (str.matches("^\\badd\\b.*")) {       //有add，在旧版本增加该文件
                    String patchFilePath = patchRootPath + spiltTxt(str);
                    String oldAddPath = oldRootPath + spiltTxt(str);
                    common.copyFile(patchFilePath, oldAddPath);

                    /*判断是否要回滚*/
                    File oldAddFile = new File(oldAddPath);
                    if (oldAddFile.exists()) {
                        if (!common.getFileMD5(oldAddPath).equals(common.getFileMD5(patchFilePath))) {
                            System.out.println("增加的文件跟补丁包里的文件不同，回滚");
                            rollback(patchFilePath, oldAddPath);
                            return;          //break 的含义是结束循环的执行，return 的含义是结束方法的执行。
                        }
                    } else {
                        System.out.println("需要增加的文件没有增加，回滚");
                        rollback(patchFilePath, oldAddPath);
                        return;
                    }

                } else if (str.matches("^\\bdelete\\b.*")) {    //有delete,将旧版本的该文件删除
                    String patchFilePath = patchRootPath + spiltTxt(str);
                    String oldDeletePath = oldRootPath + spiltTxt(str);
                    deleteFile(oldDeletePath);

                    /*判断是否要回滚*/
                    File oldDeleteFile = new File(oldDeletePath);
                    if (oldDeleteFile.exists()) {
                        System.out.println("旧版本需要删除的文件没有删除，回滚");
                        rollback(patchFilePath, oldDeletePath);
                        return;
                    }

                } else if (str.matches("^\\bmodify\\b.*")) {     //有modify，将旧版本的该文件修改
                    String patchFilePath = patchRootPath + spiltTxt(str);
                    String oldModifyPath = oldRootPath + spiltTxt(str);
                    common.copyFile(patchFilePath, oldModifyPath);

                    /*判断是否要回滚*/
                    if (!common.getFileMD5(oldModifyPath).equals(common.getFileMD5(patchFilePath))) {
                        System.out.println("需要修改文件修改后，文件还不同回滚");
                        rollback(patchFilePath, oldModifyPath);
                        return;
                    }
                }
            }

            bufferedReader.close();
            //System.out.println("补丁安装成功，删除补丁包");
            //deleteFileDir(patchPath);
        } catch (FileNotFoundException e) {
            System.out.println("installPatch FileNotFoundException");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("installPatch IOException");
            e.printStackTrace();
        }

    }

    /**
     * 回滚:将补丁里backup目录下的文件还原到旧版本里
     * @param patchRootPath 补丁包的根目录
     * @param oldRootPath  旧版本的根目录
     */
    public void rollback(String patchRootPath, String oldRootPath) {
        try {
            /*只是读写文件，和文件内容无关的,用字符流*/
            String patchList = patchRootPath + File.separatorChar + "patchList.txt";
            Reader in = new FileReader(patchList);
            BufferedReader bufferedReader = new BufferedReader(in);

            String str ;
            while ((str = bufferedReader.readLine()) != null) {
                if (str.matches("^\\badd\\b.*")) {   //有add，表明是旧版本新增加的，回滚时删除
                    deleteFile(oldRootPath + spiltTxt(str));

                } else if (str.matches("^\\bdelete\\b.*")) {    //有delete,表明是旧版本有的文件已经删除，回滚时从备份里取出创建；
                    common.copyFile(patchRootPath + File.separatorChar + "backup" + spiltTxt(str),
                            oldRootPath + spiltTxt(str));

                } else if (str.matches("^\\bmodify\\b.*")) { //有modify，表明旧版本的该文件内容已修改，回滚时从备份里取出创建并复制
                    common.copyFile(patchRootPath + File.separatorChar + "backup" + spiltTxt(str),
                            oldRootPath + spiltTxt(str));
                }
            }

            bufferedReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("rollback FileNotFoundException");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("rollback IOException");
            e.printStackTrace();
        }

    }

    /**
     * 删除文件
     * @param deleteFilePath 要删除的文件的绝对路径
     * @return 如果是文件并且存在，删除返回true
     *         否则返回false
     */
    private boolean deleteFile(String deleteFilePath) {
        File deleteFile = new File(deleteFilePath);
        if (deleteFile.isFile() && deleteFile.exists()) {
            deleteFile.delete();
            return true;
        }
        return false;
    }

    /**
     * 将含有空格的字符串按空格拆分成两部分，取后段的相对路径
     * @param str 是patchList.txt里的每一行的内容转成的字符串
     * @return 返回相对路径
     */
    private String spiltTxt(String str) {
        return str.split("\\s")[1];
    }

    /*创建Common对象，用以调用Common类的方法*/
    private Common common = new Common();

}
