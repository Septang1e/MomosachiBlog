package com.septangle.momosachiblog.utils.security;

import org.springframework.util.DigestUtils;

public class PasswordHandler {
    private final static int xor = 127;

    /**
     * 先对其进行xor加密，再进行MD5加密
     * @param password
     * @return
     */
    public static String dataEncoding(String password){
        StringBuilder builder=new StringBuilder();
        for(char ch:password.toCharArray()){
            builder.append(ch&xor);
        }
        return DigestUtils.md5DigestAsHex(builder.toString().getBytes());
    }
}
