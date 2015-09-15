package com.pachira.tools.patchTool.method;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

/**
 * 生成补丁包的方法
 <p>新版本跟旧版本文件相比：若新里有的文件而旧没有，则旧需要增加 <br>
 若旧里有的文件而新没有，则旧里的该文件需删除<br>
 若新旧里文件都有，用MD5检验后文件不同。则旧版本文件要修改

 * @author    changwen
 * @version   1.0-SNAPSHOT
 * @since     JDK1.7
 */
public class PatchMethod {
    /**
     * 两个List的补集，newList有的内容，oldList没有
     *
     * @param newList  遍历新版本目录，得到的所有文件的相对路径的集合
     * @param oldList  遍历旧版本目录，得到的所有文件的相对路径的集合
     * @return  返回的是newList有的而newList没有的文件的相对路径的集合
     */
    public List<String> complement(List<String> newList, List<String> oldList) {
        //removeAll得到的newList是一个新的集合，里面的数据是两个集合的补集的数据
        newList.removeAll(oldList);
        return newList;
    }

    /**
     * 两个List的交集：newList和oldList都有的文件
     *
     * @param newList  遍历新版本目录，得到的所有文件的相对路径的集合
     * @param oldList  遍历旧版本目录，得到的所有文件的相对路径的集合
     * @return 返回的是newList和oldList都有的文件的相对路径的集合
     */
    public List<String> intersection(List<String> newList, List<String> oldList) {
        //retainAll得到的newList是一个新的集合，里面的数据是两个集合的交集的数据
        newList.retainAll(oldList);
        return newList;
    }

    /**
     * 在补丁包里创建记录文件，记录新旧版本有变化的文件
     * @param patchList  新旧版本比较有变化的文件的信息记录到该集合中
     * @param patchRootPath  补丁包根目录的绝对路径
     */
    public void patchList(List<String> patchList,String patchRootPath) {
        File patchListTxt = new File(patchRootPath + File.separatorChar + "patchList.txt");
        try {
            OutputStream out = new FileOutputStream(patchListTxt);
            //将patchList记录的数据写到txt文件里，每一个数据对应一行
            for (String content : patchList) {
                out.write((content + "\r\n").getBytes());    //\n 新行（换行）符,\r 回车符
            }

            out.close();
        } catch (FileNotFoundException e) {
            System.out.println("patchList FileNotFoundException");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("patchList IOException");
            e.printStackTrace();
        }
    }

    /**
     * 出栈并过滤指定的文件
     * @param folder  准备获取所有文件的目录
     * @param filterFilePath  记录正则表达式的过滤文件路径
     * @return  返回一个已经获取所有文件的相对路径并且过滤指定文件的String类型的集合
     */
    public List<String> stackPop(String folder , String filterFilePath) {
        //用来存放目录下的所有文件，除要过渡的文件外
        List<String> allFiles = new ArrayList<String>();
        File dir = new File(folder);
        //装载顶层目录到堆栈
        stackPush(dir.listFiles(), allFiles, folder);
        //文件对象缓存
        File fileTemp ;
        while (!stack.empty()) {
            fileTemp = (File) stack.pop();
            File[] fileList = fileTemp.listFiles();

            if (fileList != null) {
                stackPush(fileList, allFiles,folder);

            }
        }

        return filter(filterFilePath,allFiles);
    }

    /**
     * 进栈:获得当前目录下的所有文件的相对路径
     *
     * @param fileList   当前目录下的所有文件和目录
     * @param currentFiles  用来存放当前目录下的所有文件的相对路径
     * @param folder 将输入的目录截取掉，只要里面的文件
     * @return  返回一个已经获取当前目录下所有文件的相对路径的String类型的集合
     */
    private List<String> stackPush(File[] fileList, List<String> currentFiles, String folder) {
        int len = folder.split("/").length;
        for (File fileOrDir : fileList) {
            if (fileOrDir.isDirectory())
                stack.push(fileOrDir);

            else   //如果是文件，截取相对路径放到currentFiles
                currentFiles.add(spiltString(fileOrDir.getAbsolutePath(), len));
        }

        return currentFiles;
    }

    /**
     *  过滤指定的文件
     *
     * @param filterFilePath  存放正则表达式的文件路径
     * @param fileList  出栈后获取目录下所有文件的相对路径的集合
     * @return 返回已经过滤指定的文件的List
     */
    private List<String> filter(String filterFilePath, List<String> fileList){
        try {
            /*只是读写文件，和文件内容无关的,用字符流*/
            Reader in = new FileReader(filterFilePath);
            BufferedReader bufferedReader = new BufferedReader(in);
            String str ;

            while ((str = bufferedReader.readLine()) !=null) {
                /*List删除时有可能会遗漏某个元素,因为删除元素后List的size在
                 * 变化，元素的索引也在变化，比如你循环到第2个元素的时候你把它删了，
                 * 接下来你去访问第3个元素，实际上访问到的是原先的第4个元素。当访问的元素
                 * 索引超过了当前的List的size后还会出现数组越界的异常.
                 */
                Iterator<String> it = fileList.iterator();
                while (it.hasNext()){
                    String string = it.next();
                    if (string.matches(str))
                        it.remove();  //这里要使用Iterator的remove方法移除当前对象，如果使用List的remove方法，
                    // 则同样会出现ConcurrentModificationException
                }

            }

            bufferedReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("filter FileNotFoundException");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("filter IOException");
            e.printStackTrace();
        }

        return fileList;
    }

    /**
     * 拆分字符串并组装：用于获取相对路径
     * @param string  要拆分的字符串
     * @param cutNum  拆分时要去掉前面字符的个数
     * @return  返回值是相对路径
     */
    private String spiltString(String string, int cutNum) {
        String relativePath = "";
        int len = string.split("/").length;
        String[] array = new String[len - cutNum];

        for (int i = cutNum, j = 0; i < len; i++, j++) {
            array[j] = string.split("/")[i];
        }

        for (String character : array) {
            relativePath += File.separator + character;
        }

        return relativePath;
    }

    /*创建一个栈，用以获取当前目录下的所有的文件*/
    protected static Stack stack = new Stack();
}
