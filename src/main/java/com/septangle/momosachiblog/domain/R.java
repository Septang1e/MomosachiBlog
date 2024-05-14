package com.septangle.momosachiblog.domain;

import lombok.*;


@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class R<T> {

    private Integer code;

    private T data;

    private String message;//error msg

    public static  <T> R<T> success(T data){
        R<T> r=new R<>();
        r.setCode(1);
        r.setData(data);
        return r;
    }

    public static  <T> R<T> success(T data, String msg){
        R<T> r=new R<>();
        r.setCode(1);
        r.setMessage(msg);
        r.setData(data);
        return r;
    }
    public static <T> R<T> userNotLogin(String msg) {
        R<T> r=new R<>();
        r.setCode(2);
        r.setMessage(msg);
        return r;
    }

    public static  <T>R<T> error(String message){
        R<T>r=new R<>();
        r.setCode(0);
        r.setMessage(message);
        return r;
    }
}
