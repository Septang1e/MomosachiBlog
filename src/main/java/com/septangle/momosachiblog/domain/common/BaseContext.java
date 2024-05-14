package com.septangle.momosachiblog.domain.common;

public class BaseContext {
    private static ThreadLocal<Long>userId = new ThreadLocal<>();
    public static void setCurrentId(Long id){
        userId.set(id);
    }
    public static Long getCurrentId(){
        return userId.get();
    }
}
