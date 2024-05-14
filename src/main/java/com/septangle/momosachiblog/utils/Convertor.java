package com.septangle.momosachiblog.utils;

import com.septangle.momosachiblog.domain.dto.ArticleDTO;
import com.septangle.momosachiblog.domain.entity.Article;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.DigestUtils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Slf4j
public class Convertor {
    public static String imgToBase64(String imagePath){
        byte[]bytes={};
        try(FileInputStream inputStream = new FileInputStream(imagePath)){
            bytes=new byte[inputStream.available()];
            int len=0;
            inputStream.read(bytes);
            return Base64.getEncoder().encodeToString(bytes);
        }catch (IOException e){
            e.printStackTrace();
        }
        return new String(bytes);
    }
    public static String imgStreamToBase64(BufferedInputStream inputStream) throws IOException{
        byte []bytes=new byte[inputStream.available()];
        log.info("{}", inputStream.read(bytes));
        return new String(bytes);
    }
}
