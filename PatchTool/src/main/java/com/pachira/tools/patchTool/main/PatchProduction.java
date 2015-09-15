package com.pachira.tools.patchTool.main;

import com.pachira.tools.patchTool.common.Common;
import com.pachira.tools.patchTool.method.PatchMethod;
import org.apache.commons.cli.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 补丁包生成的主方法
 *
 * @author    changwen
 * @version   1.0-SNAPSHOT
 * @since     JDK1.7
 */
public class PatchProduction extends Common {
    public static void main(String[] args) {
         /*CLI 定义阶段*/
        Options options = new Options();
        //Options 实例的 addOption方法
        options.addOption("n", true, "new version of the location");
        options.addOption("o", true, "old version of the location");
        options.addOption("p", true, "patch production of the location");
        options.addOption("f", true, "filter");
        try {
             /*CLI 解析阶段*/
            CommandLineParser parser = new PosixParser();    //PosixParser：解析参数及值(-s10)
            CommandLine cmd = parser.parse(options, args);   //使用CommandLine对象可以得到参数值

             /*CLI 询问阶段*/
            if (cmd.hasOption("n") && cmd.hasOption("o")
                    && cmd.hasOption("p") && cmd.hasOption("f")) {
                String newVersion = cmd.getOptionValue("n");
                String oldVersion = cmd.getOptionValue("o");
                String patchPath = cmd.getOptionValue("p");
                String filterPath = cmd.getOptionValue("f");

                File newDir = new File(newVersion);
                File oldDir = new File(oldVersion);
                File patchDir = new File(patchPath);
                File filterFile = new File(filterPath);
                if (!patchDir.exists())
                    patchDir.mkdir();

                if (newDir.isDirectory() && oldDir.isDirectory() && patchDir.isDirectory() && filterFile.isFile()
                        && newDir.exists() && oldDir.exists() && patchDir.exists() && filterFile.exists()) {
                    patchProduction(newVersion, oldVersion, patchPath, filterPath);
                    System.exit(0);

                } else {
                    System.out.println("输入目录不存在，或者格式错误,请重新输入。" +
                            "格式：-n 新版本所在的位置 -o 旧版本所在的位置 -p 要生成的补丁包所在的位置 -f 过滤文件所在的位置");
                    System.exit(1);
                }

            } else {
                System.out.println("输入参数个数不正确，需要输入四个参数-n  -o  -p  -f ");
                System.exit(1);
            }

        } catch (ParseException e) {    //如果不是指定的参数格式，则抛出异常
            System.out.println("PatchProduction main ParseException:" +
                    "不是指定的参数格式：-n 新版本所在的位置 -o 旧版本所在的位置 -p 要生成的补丁包所在的位置 -f 过滤文件所在的位置" );
            System.exit(1);
        }

    }
    /**
     * 补丁包生成的方法
     *
     * @param newRootPath   旧版本的根目录的绝对路径
     * @param oldRootPath   新版本的根目录的绝对路径
     * @param patchPath     生成补丁包的绝对路径
     * @param filterPath    存放过渡的正则表达式的过滤文件
     */
    private static void patchProduction(String newRootPath, String oldRootPath, String patchPath, String filterPath) {
        /*new跟old相比，new有的而old没有的,将new有的复制到补丁包里*/
        diffFile = method.complement(
                method.stackPop(newRootPath, filterPath),
                method.stackPop(oldRootPath, filterPath));
        if (diffFile.size() == 0) {
            System.out.println("没有需要往旧版本里增加的文件");

        } else {
            for (String relativePath : diffFile) {
                patch.copyFile(newRootPath + relativePath, patchPath + relativePath);
                patchList.add("add " + relativePath);
            }
        }

        /*new跟old相比，有相同名的文件但通过md5检验后不同，将new的该文件放到补丁包里*/
        sameFile = method.intersection(
                method.stackPop(newRootPath, filterPath),
                method.stackPop(oldRootPath, filterPath));
        for (String relativePath : sameFile) {
            //用MD5检验文件是否相同
            if (!patch.getMd5ByFile(newRootPath + relativePath).
                    equals(patch.getMd5ByFile(oldRootPath + relativePath))) {

                patch.copyFile(newRootPath + relativePath, patchPath + relativePath);
                patchList.add("modify " + relativePath);
            }
        }

        /*old跟new相比，多的要删除的，在文件里记录就行*/
        deleteFile = method.complement(
                method.stackPop(oldRootPath, filterPath),
                method.stackPop(newRootPath, filterPath));
        if (deleteFile.size() == 0) {
            System.out.println("旧版本没有需要删除的文件");
        } else {
            for (String relativePath : deleteFile) {
                patchList.add("delete " + relativePath);
            }
        }

        /*在补丁包里创建记录文件，记录新旧版本有变化的文件*/
        method.patchList(patchList, patchPath);
    }

    /*新跟旧相比，新里面有旧里面没有的文件，将相对路径存放里*/
    protected static List<String> diffFile = new ArrayList<String>();
    /*新跟旧相比，新旧里面都有的文件，将相对路径存放里*/
    protected static List<String> sameFile = new ArrayList<String>();
    /*新跟旧相比，旧里面有而新里面没有要删除的，将相对路径存放里*/
    protected static List<String> deleteFile = new ArrayList<String>();

    /*新跟旧相比，记录有变化的文件，将相对路径存放里*/
    private static List<String> patchList = new ArrayList<String>();

    private static PatchMethod method = new PatchMethod();
    private static PatchProduction patch = new PatchProduction();

}
