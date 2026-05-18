package com.springgrpc.server.util; 
public class CorrelationIdHolder { 
    private static final ThreadLocal<String> HOLDER = new ThreadLocal<>(); 
    public static void set(String id) { HOLDER.set(id); } 
    public static String get() { return HOLDER.get(); } 
    public static void clear() { HOLDER.remove(); } 
} 
