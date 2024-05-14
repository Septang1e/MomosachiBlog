package com.septangle.momosachiblog.controller;

import com.septangle.momosachiblog.utils.Generator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

@RequestMapping("/")
@Slf4j
@RestController
public class FileDownloadTest {
    @GetMapping("/download")
    public void download(HttpServletResponse response){
        log.info("started");
        try(FileInputStream inputStream = new FileInputStream("E:\\Open_Sourse\\septangle\\MomosachiBlog\\src\\main\\resources\\templates\\1704975992843.yml")) {
            response.setContentType("application/x-msdownload");
            response.setHeader("Content-Disposition", "attachment; filename=" + "config.yml");
            OutputStream outputStream = response.getOutputStream();
            byte[]bytes=new byte[inputStream.available()];
            inputStream.read(bytes);
            outputStream.write(bytes);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/api/v1/test")
    public String pidTest() {
        return Generator.pidGenerator();
    }
}
