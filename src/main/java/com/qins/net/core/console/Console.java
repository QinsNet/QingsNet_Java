package com.qins.net.core.console;

public class Console {
    public static void log(String msg){
        System.out.println("Msg:::" + msg);
    }
    public static void info(String msg){
        System.out.println("Information:::" + msg);
    }
    public static void debug(String msg){
        System.out.println("Debug:::" + msg);
    }
    public static void error(String msg){
        System.out.println("ResponseException:::" + msg);
    }
}
