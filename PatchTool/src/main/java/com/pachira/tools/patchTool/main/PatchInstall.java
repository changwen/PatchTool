package com.pachira.tools.patchTool.main;

import com.pachira.tools.patchTool.common.Common;
import com.pachira.tools.patchTool.method.InstallMethod;
import org.apache.commons.cli.*;

import java.io.File;

/**
 * 补丁包安装的主方法
 *
 * @author    changwen
 * @version   1.0-SNAPSHOT
 * @since     JDK1.7
 */
public class PatchInstall extends Common {
    public static void main(String[] args) {
        /*CLI 定义阶段*/
        Options options = new Options();
        options.addOption("o", true, "old version of the location");
        options.addOption("p", true, "patch version of the location");

        try {
             /*CLI 解析阶段*/
            CommandLineParser parser = new PosixParser();
            CommandLine cmd = parser.parse(options, args);

             /*CLI 询问阶段*/
            if (cmd.hasOption("o") && cmd.hasOption("p")) {
                String oldVersion = cmd.getOptionValue("o");
                String patchVersion = cmd.getOptionValue("p");

                File oldDir = new File(oldVersion);
                File patchDir = new File(patchVersion);

                if (oldDir.isDirectory() && patchDir.isDirectory()
                        && oldDir.exists() && patchDir.exists()) {
                    install.backUp(patchVersion, oldVersion);
                    install.installPatch(patchVersion, oldVersion);
                    System.exit(0);

                } else {
                    System.out.println("输入目录不存在，或者格式错误,请重新输入。" +
                            "Linux格式： -o 旧版本所在的位置  " +
                            "Windows格式：-o 旧版本所在的位置 -p 补丁包所在的位置");
                    System.exit(1);
                }

            } else {
                System.out.println("输入参数个数不正确，Linux输入：-o 旧版本所在的位置  \\n\\r" +
                        " Windows输入：-o 旧版本所在的位置 -p 补丁包所在的位置");
                System.exit(1);

            }

        } catch (ParseException e) {
            System.out.println("PatchInstall main ParseException:" +
                    "Linux下不是指定的参数格式:-o 旧版本所在的位置 " +
                    "Windows下不是指定的参数格式：-o 旧版本所在的位置 -p 补丁包所在的位置");
            System.exit(1);
        }

    }

    /*创建InstallMethod对象*/
    private static InstallMethod install = new InstallMethod();
}
