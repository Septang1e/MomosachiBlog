package com.septangle.momosachiblog.controller;

import com.septangle.momosachiblog.domain.R;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/")
public class FileController {

    @Value("${constant.tempFile.avatar}")
    private String defaultPath;
    private final Long targetSizeKb = 600L;
    @PostMapping("/api/upload/image")
    public R<String> uploadImage(@RequestParam("file") MultipartFile image) {

        if(Objects.isNull(image)) {
            return R.error("文件不存在");
        }
        if(image.getSize() > 10485760) {
            return R.error("文件大小不能超过10MB");
        }


        String result = "";
        try(InputStream inputStream = image.getInputStream()) {
            result = Base64.getEncoder().encodeToString(inputStream.readAllBytes());
        }catch (IOException e) {
            e.printStackTrace();
        }
        return R.success("data:" + image.getContentType() + ";base64," + result);
    }

    @PostMapping("/api/upload/avatar")
    public R<String> uploadAvatar(@RequestParam("file") MultipartFile avatar) {

        if (Objects.isNull(avatar)) {
            return R.error("文件不存在");
        }
        if(avatar.getSize() > 10485760) {
            return R.error("文件大小不能超过10MB");
        }


        String base64Result = "";
        try(InputStream inputStream = avatar.getInputStream()){

            byte []imageBytes = inputStream.readAllBytes();
            while(imageBytes.length / 1024 > targetSizeKb) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageBytes);
                Thumbnails.of(byteArrayInputStream)
                        .scale(0.5)
                        .outputFormat("jpeg")
                        .toOutputStream(outputStream);
                imageBytes = outputStream.toByteArray();
                outputStream.reset();
                log.info("{}", imageBytes.length);
            }

            base64Result = Base64.getEncoder().encodeToString(imageBytes);

        }catch (IOException e) {
            e.printStackTrace();
        }

        return R.success("data:" + avatar.getContentType() + ";base64," + base64Result);
    }

    public R<String> downloadAvatar(MultipartFile avatar) {

        String path = "";

        return R.success(path);
    }

}
