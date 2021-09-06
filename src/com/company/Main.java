package com.company;

import com.company.utils.CmdUtils;
import com.company.utils.FileUtils;
import com.company.utils.PathUtils;
import com.company.utils.ZipUtils;

import java.io.*;
import java.util.*;

public class Main {


    private static String libFolderName = "lib";

    private static String srcFolderName = "src";

    private static String batFolderName = "bat";



    public static void main(String[] args)throws Exception {
        System.out.println("===========================重复反编译会自动清除原来数据,不用刻意删除反编译结果目录================================");
        //根据系统变量或环境变量，获取IDEA路径，并拼接出JAVA Decompiler 包路径。
        boolean getJavaDecompilerJar = false;
        String initJavaDecompilerJarPath = "";
        Map<String,String> map = System.getenv();
        Iterator it = map.entrySet().iterator();
        String ideaPath="";
        while(it.hasNext())
        {
            Map.Entry<String,String> entry = (Map.Entry) it.next();
            if("IntelliJ IDEA".equals(entry.getKey())){
                ideaPath=entry.getValue();
            }
        }
        if(ideaPath!=null && !"".equals(ideaPath.trim())){
            if(ideaPath.endsWith(";")){
                ideaPath=ideaPath.substring(0,ideaPath.length()-1);
                File ideaBinFolder= new File(ideaPath);
                if(ideaBinFolder.exists()){
                    String javaDecompilerJarPath =
                            ideaBinFolder.getParentFile().getAbsolutePath() + File.separator+"plugins"+
                                    File.separator+"java-decompiler"+File.separator+"lib"+
                                    File.separator+"java-decompiler.jar";
                    File javaDecompilerJar = new File(javaDecompilerJarPath);
                    if(javaDecompilerJar.exists() && javaDecompilerJar.isFile()){
                        getJavaDecompilerJar=true;
                        initJavaDecompilerJarPath=javaDecompilerJar.getAbsolutePath();
                    }
                }
            }
        }

        String javaDecompilerPath;
        Scanner input = new Scanner(System.in);
        if(getJavaDecompilerJar){
            System.out.println("java-decompiler jar 包路径："+initJavaDecompilerJarPath);
            System.out.println("已经自动获取到java-decompiler jar包路径，是否需要覆盖？");
            System.out.printf("java-decompiler jar包路径 【不覆盖请按Enter跳过】 ：");
            javaDecompilerPath= input.nextLine();
            if(javaDecompilerPath==null || "".equals(javaDecompilerPath.trim())){
                javaDecompilerPath=initJavaDecompilerJarPath;
            }
        }else{
            System.out.println("未获得java-decompiler jar包路径，可能以下情况导致:");
            System.out.println("1.未安装IDEA");
            System.out.println("2.已安装IDEA,未安装Java Bytecode Decompiler(Java Decompiler)插件");
            System.out.println("3.未配置IDEA环境变量,环境变量key=IntelliJ IDEA,value=IDEA安装目录下的bin目录");
            System.out.println("安装后重新启动程序  或者   直接手动输入!!!!!!!");
            System.out.printf("请输入java-decompiler jar包路径【必填】：");
            javaDecompilerPath= input.nextLine();
            File javaDecompilerJar = new File(javaDecompilerPath);
            if(!javaDecompilerJar.exists()){
                throw new Exception("java-decompiler jar包不存在！");
            }
        }

        System.out.print("请输入反编译运行的主类(默认org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler)【不覆盖请按Enter跳过】:");
        String decompilerMainClas = input.nextLine();
        if(decompilerMainClas==null || "".equals(decompilerMainClas.trim())){
            decompilerMainClas="org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler";
        }
        System.out.print("请输入所有jar包所在目录【必填】:");
        String jarsFolderPath =input.nextLine();
        jarsFolderPath= PathUtils.removeLastPathSeparator(jarsFolderPath);
        System.out.print("请输入反编译后生成结果的目录(可以不存在)【必填】:");
        String decompilerFolderPath = input.nextLine();
        decompilerFolderPath = PathUtils.removeLastPathSeparator(decompilerFolderPath);

        //lib 目录路径
        String decompilerLibFolderPath = decompilerFolderPath+ File.separator+libFolderName;
        //bat 目录路径
        String batFolderPath=decompilerFolderPath+File.separator+batFolderName;
        //src 目录路径
        String decompilerSrcFolderPath = decompilerFolderPath+ File.separator+srcFolderName;

        if(new File(decompilerFolderPath).exists()){
            System.out.println("===========================开始删除以往结果的数据==============================");
            System.out.println("正在删除 bat 目录。。。。。。。。。。。。");
            FileUtils.delete(batFolderPath);
            System.out.println("正在删除 lib 目录。。。。。。。。。。。。");
            FileUtils.delete(decompilerLibFolderPath);
            System.out.println("正在删除 src 目录。。。。。。。。。。。。");
            FileUtils.delete(decompilerSrcFolderPath);
            System.out.println("===========================删除完成=========================================");

        }



        System.out.println("================================开始生成bat脚本=====================================");
        //生成decompiler lib 目录
        File decompilerLibFolder = new File(decompilerLibFolderPath);
        if(!decompilerLibFolder.exists()){
            decompilerLibFolder.mkdirs();
        }
        //end

        //获取所有待编译jar包路径
        File jarsFolder = new File(jarsFolderPath);
        if(!jarsFolder.exists()){
            throw new Exception("待编译jar包路径不存在！");
        }
        File [] jars=jarsFolder.listFiles();
        if(Arrays.stream(jars).filter(file->file.getAbsolutePath().endsWith(".jar")).count()==0){
            throw new Exception("无待编译jar包！");
        }
        //end



        //开始往bat脚本中输入内容
        String batPath=decompilerFolderPath+File.separator+batFolderName+File.separator+"decompiler.bat";
        File batFile = new File(batPath);

        if(batFile.exists()){
            batFile.deleteOnExit();
        }
        batFile.getParentFile().mkdirs();
        batFile.createNewFile();
        PrintStream printStream = new PrintStream(new FileOutputStream(batFile));
        List<String> cmdCommands = new ArrayList<>();
        for(File jar : jars){
            if(jar.getAbsolutePath().endsWith(".jar")){
                String cmdCommand = "java -cp \""+javaDecompilerPath+"\" "+decompilerMainClas+" -dgs=true  "+jar.getAbsolutePath()+" "+decompilerLibFolderPath;
                printStream.println(cmdCommand);
                cmdCommands.add(cmdCommand);
            }
        }
        printStream.close();

        //END
        System.out.println("================================生成bat脚本完成=====================================");
        System.out.println("bat脚本路径："+batPath);
        System.out.println("================================开始执行bat脚本=====================================");
        for(String cmd:cmdCommands){
            CmdUtils.callCmd(cmd);
        }
        System.out.println("================================执行bat脚本完成=====================================");

        System.out.println("===========================开始解压反编译后生成的压缩包================================");
        File [] decompilerJars=decompilerLibFolder.listFiles();
        for(File decompilerJar:decompilerJars){
            System.out.println("正在解压："+decompilerJar.getAbsolutePath());
            ZipUtils.unZip(decompilerJar,decompilerSrcFolderPath+File.separator);
        }
        System.out.println("===========================反编译后生成的压缩包解压完成================================");

    }
}
