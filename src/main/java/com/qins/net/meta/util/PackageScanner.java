package com.qins.net.meta.util;

import com.qins.net.meta.annotation.Meta;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@AllArgsConstructor
public class PackageScanner {
    @Getter
    ArrayList<String> paths;

    //扫描一般的包。
    private Class<?> scanPackage(String path, File currentfile, String name) {
        File[] filelist = currentfile.listFiles(new FileFilter() {
            //FileFilter是文件过滤器,源代码只写了一个accapt的抽象方法。
            @Override
            public boolean accept(File pathName) {
                if(pathName.isDirectory()) {    //判断是否是目录
                    return true;
                }
                return pathName.getName().endsWith(".class");
            }
        });

        for(File file:filelist) {
            if(file.isDirectory()) {
                scanPackage( path + "." + file.getName(),file,name);
            }else {
                String fileName = file.getName().replace(".class", "");
                String className = path + "." + fileName;
                try {
                    Class<?> klass = Class.forName(className);//取出所有的类
                    if(klass.isAnnotation() //不扫描注解类、枚举类、接口和八大基本类型。
                            ||klass.isEnum()
                            ||klass.isInterface()
                            ||klass.isPrimitive()) {
                        continue;
                    }
                    Meta meta = klass.getAnnotation(Meta.class);
                    String klassName = meta == null || "".equals(meta.value()) ? klass.getName() : meta.value();
                    if(klassName.equals(name)){
                        return klass;
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    //扫描jar包方法。
    private Class<?> scanPackage(URL url,String name) throws IOException {
        JarURLConnection urlConnection = (JarURLConnection) url.openConnection();
        JarFile jarfile = urlConnection.getJarFile();
        Enumeration<JarEntry> jarEntries = jarfile.entries();
        while(jarEntries.hasMoreElements()) {
            JarEntry jarEntry = jarEntries.nextElement();
            String jarName = jarEntry.getName();
            if(!jarName.endsWith(".class")) {
                continue;
            }
            String className = jarName.replace(".class", "").replaceAll("/", ".");
            try {
                Class<?> klass = Class.forName(className);
                if (klass.isAnnotation()
                        || klass.isInterface()
                        || klass.isEnum()
                        || klass.isPrimitive()) {
                    continue;
                }
                Meta meta = klass.getAnnotation(Meta.class);
                String klassName = meta == null || "".equals(meta.value()) ? klass.getName() : meta.value();
                if(klassName.equals(name)){
                    return klass;
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    //用类名扫描
    public void packageScan(Class<?> klass) {
        packageScan(klass.getPackage().getName());
    }

    //用包名进行扫描
    public Class<?> packageScan(String name) {
        for (String path : paths){
            //线程上下文类加载器得到当前的classpath的绝对路径.（动态加载资源）
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            try {
                //(Thread.currentThread().getContextClassLoader().getResource(""))
                //(来得到当前的classpath的绝对路径的URI表示法。)
                Enumeration<URL> resources = classloader.getResources(path);
                while(resources.hasMoreElements()) {
                    //先获得本类的所在位置
                    URL url = resources.nextElement();
                    //url.getProtocol()是获取URL的HTTP协议。
                    if(url.getProtocol().equals("jar")) {
                        //判断是不是jar包
                        return scanPackage(url,name);
                    }
                    else {
                        //此方法不会自动将链接中的非法字符转义。
                        //而在File转化成URI的时候，会将链接中的特殊字符如#或!等字符进行编码。
                        File file = new File(url.toURI());
                        if(!file.exists()) {
                            continue;
                        }
                        return scanPackage(path,file,name);
                    }
                }
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
