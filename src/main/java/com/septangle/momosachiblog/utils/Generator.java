package com.septangle.momosachiblog.utils;

import java.time.Instant;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;

public class Generator {

    public static String pidGenerator() {
        /*
        byte[] uuid = UUID.randomUUID().toString().replace("-", "").getBytes();
        byte[] bytes = new byte[12];
        int n = uuid.length;
        int j = 0, k = 0;
        Random random = new Random();
        for(int i = 0; i < 12; i += 2) {
            int idx = Math.abs(random.nextInt()) % n;
            bytes[i] = uuid[idx];
        }
        return Base64.getEncoder().encodeToString(bytes);
        */
        Random random = new Random();
        StringBuilder res = new StringBuilder();
        int len = 12;
        for(int i = 0; i < len; ++i) {
            int num = Math.abs(random.nextInt()) % 61;
            if(num <= 9) {
                res.append(num);
            }else if(num <= 9 + 25) {
                res.append((char)('a' + num - 9));
            }else{
                res.append((char)('A' + num - 9 - 26));
            }
        }
        return res.toString();
    }
}
