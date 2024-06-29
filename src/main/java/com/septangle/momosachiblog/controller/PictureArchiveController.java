package com.septangle.momosachiblog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.septangle.momosachiblog.domain.R;
import com.septangle.momosachiblog.domain.entity.PictureArchive;
import com.septangle.momosachiblog.service.PictureArchiveService;
import com.septangle.momosachiblog.utils.Convertor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.InputBuffer;
import org.apache.ibatis.util.MapUtil;
import org.apache.tomcat.util.http.fileupload.FileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.http.HttpRequest;
import java.util.*;

@RestController
@RequestMapping("/api")
@Slf4j
public class PictureArchiveController {
    @Autowired
    private PictureArchiveService pictureArchiveService;

    //upload
    @PostMapping("/upload")
    public R<String> upload(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        //把图片加密为Base64的形式
        String base64 = Base64.getEncoder().encodeToString(multipartFile.getBytes());

        PictureArchive pictureArchive = new PictureArchive();
        //设置图片的基本信息
        pictureArchive.setPictureBase64(base64);
        pictureArchive.setFormat(multipartFile.getContentType());
        pictureArchive.setName(multipartFile.getName());

        pictureArchiveService.save(pictureArchive);
        return R.success("图片上传成功");
    }

    @GetMapping("/get/{id}")
    public R<PictureArchive> getImg(@PathVariable Long id){
        PictureArchive pictureArchive = pictureArchiveService.getById(id);
        if(pictureArchive==null){
            return R.error("图片不存在");
        }
        return R.success(pictureArchive);
    }

    @GetMapping("/pagination/{pageNum}/{pageSize}")
    public R<Page<PictureArchive>> page(@PathVariable int pageNum,@PathVariable int pageSize){

        //构造分页条件
        Page<PictureArchive> pageInfo = new Page<>(pageNum,pageSize);

        //创建筛选条件、排序
        LambdaQueryWrapper<PictureArchive> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PictureArchive::getStatus,0)
                .orderByAsc(PictureArchive::getCreateTime);

        pictureArchiveService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    /**
     *  "<img src=\""+
     *                 "data:"+pictureArchive.getFormat()+";base64,"+pictureArchive.getPictureBase64()
     *                 +"\" />";
     * @param pid
     * @return
     */
    @GetMapping("/markdown/pictureArchive/{pid}")
    public String getPictureInMarkdown(@PathVariable String pid,HttpServletResponse response) throws IOException {
        log.info("starting to get the picture");
        LambdaQueryWrapper<PictureArchive>queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PictureArchive::getId,Long.valueOf(pid));
        PictureArchive pictureArchive = pictureArchiveService.getOne(queryWrapper);

        String pictureInfo = "data:"+pictureArchive.getFormat()+";base64,"+pictureArchive.getPictureBase64();
        byte []pictureData = Base64.getDecoder().decode(pictureArchive.getPictureBase64().getBytes());

        response.setContentType(pictureArchive.getFormat());
        OutputStream imgFileData =  response.getOutputStream();
        imgFileData.write(pictureData);
        return "<img src=\"data:"+pictureArchive.getFormat()+";base64,"+pictureArchive.getPictureBase64()+"\" />";
    }
}
