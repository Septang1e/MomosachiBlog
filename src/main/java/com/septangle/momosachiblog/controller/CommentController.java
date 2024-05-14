package com.septangle.momosachiblog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.septangle.momosachiblog.constant.UserConstant;
import com.septangle.momosachiblog.domain.R;
import com.septangle.momosachiblog.domain.dto.CommentDTO;
import com.septangle.momosachiblog.domain.dto.CommentTransDTO;
import com.septangle.momosachiblog.domain.entity.Article;
import com.septangle.momosachiblog.domain.entity.ArticleTag;
import com.septangle.momosachiblog.domain.entity.Comment;
import com.septangle.momosachiblog.domain.entity.User;
import com.septangle.momosachiblog.service.ArticleService;
import com.septangle.momosachiblog.service.ArticleTagService;
import com.septangle.momosachiblog.service.CommentService;
import com.septangle.momosachiblog.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.yaml.snakeyaml.scanner.Constant;

import javax.annotation.Resource;
import javax.annotation.Resources;
import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;


@RestController
@Slf4j
@RequestMapping("/api/comment")
public class CommentController {

    @Autowired
    UserService userService;
    @Autowired
    CommentService commentService;
    @Autowired
    ArticleService articleService;
    @Resource
    ArticleTagService articleTagService;

    @PostMapping("/add")
    public R<String> addComment(@RequestBody CommentDTO commentInfo, HttpServletRequest request){

        //处理ip属地信息

        /**
         * 获取IP归属地的API
         * http://ip-api.com/json/24.48.0.1?lang=zh-CN
         */

        LambdaQueryWrapper<User>duplicateUserFilter = new LambdaQueryWrapper<>();
        duplicateUserFilter
                .eq(User::getNickname, commentInfo.getNickname())
                .eq(User::getIsDelete, 0);
        User duplicateUser = userService.getOne(duplicateUserFilter);
        if(duplicateUser != null && !duplicateUser.getEmail().equals(commentInfo.getEmail())){
            return R.error("该用户已存在!!!");
        }
        log.info("{}", commentInfo);
        //更新头像
        if(duplicateUser != null && !commentInfo.getAvatar().equals("not-update")){
            duplicateUser.setAvatar(commentInfo.getAvatar());
        }

        //若用户不存在则创建一个新用户
        if(duplicateUser == null){
            User user = new User();
            user.setUserByCommentDTO(commentInfo);
            duplicateUser = user;
        }
        userService.saveOrUpdate(duplicateUser);
        Long userID = duplicateUser.getId();

        Comment comment = new Comment();

        comment.setArticleId(articleService.getByPid(commentInfo.getPid()).getId());


        comment.setContent(commentInfo.getContent());
        comment.setFatherId(commentInfo.getFatherId());
        comment.setReplyBy(userID);
        comment.setIpAddress(commentInfo.getIpAddress());
        comment.setRootParentId(commentInfo.getRootParentId());

        commentService.save(comment);

        return R.success("添加成功");
    }


    @GetMapping("/get")
    public R<Page<CommentDTO>> getComment(
            @RequestParam(required = false) Long articleId,
            @RequestParam(required = false) Long rootParentId,
            @RequestParam(required = false) String articlePid,
            @RequestParam Integer pageSize,
            @RequestParam Integer pageNum){
        if(articlePid == null){
            return getChildrenComment(articleId, rootParentId, pageSize, pageNum);
        }else{
            return getRootCommentWithArticleID(articlePid, pageNum, pageSize);
        }
    }


    @GetMapping("/count/{articlePid}")
    public R<Long> getCommentCount(@PathVariable String articlePid) {

        LambdaQueryWrapper<Article> articleIdGetter = new LambdaQueryWrapper<>();
        articleIdGetter
                .eq(Article::getPid, articlePid);

        Long articleId = articleService.getOne(articleIdGetter).getId();

        LambdaQueryWrapper<Comment> commentCountGetter = new LambdaQueryWrapper<>();
        commentCountGetter
                .eq(Comment::getArticleId, articleId)
                        .eq(Comment::getStatus, 0)
                                .eq(Comment::getIsDelete, 0);

        return R.success(commentService.count(commentCountGetter));
    }

    @PostMapping("/increase/{commentId}")
    public R<String> updateUserLikeStatusIncrease(@PathVariable Long commentId){
        Comment comment = commentService.getById(commentId);
        comment.setLikeCount(comment.getLikeCount() + 1);
        commentService.saveOrUpdate(comment);
        return R.success("点赞成功");
    }
    @PostMapping("/decrease/{commentId}")
    public R<String> updateUserLikeStatusDecrease(@PathVariable Long commentId){
        Comment comment = commentService.getById(commentId);
        comment.setLikeCount(comment.getLikeCount() - 1);
        commentService.saveOrUpdate(comment);
        return R.success("取消点赞成功");
    }



    //utils

    private R<Page<CommentDTO>> getRootCommentWithArticleID(
            String articlePid,
            Integer pageNum, Integer pageSize){

        //将 articlePid 转换为 articleId
        LambdaQueryWrapper<Article>articleIDGetter = new LambdaQueryWrapper<>();
        articleIDGetter.eq(Article::getStatus, 0).eq(Article::getPid, articlePid);
        Long articleId = articleService.getOne(articleIDGetter).getId();

        //初始的分页结果
        LambdaQueryWrapper<Comment>commentDefaultDataGetter = new LambdaQueryWrapper<>();
        commentDefaultDataGetter
                .eq(Comment::getIsDelete, 0)
                .eq(Comment::getStatus, 0)
                .eq(Comment::getArticleId, articleId)
                .eq(Comment::getFatherId, -1)
                .orderByDesc(Comment::getCreateTime);

        Page<Comment>rootDefalutPage = new Page<>(pageNum, pageSize);
        commentService.page(rootDefalutPage, commentDefaultDataGetter);

        //集成的分页结果
        List<CommentDTO>records = new ArrayList<>();
        long floor = 1L;
        for(Comment comment : rootDefalutPage.getRecords()){
            User commentUser = userService.getById(comment.getReplyBy());
            CommentDTO commentDTO = setCommentDTO(commentUser, comment);
            commentDTO.setFloor(floor++);
            records.add(commentDTO);
        }
        Page<CommentDTO>results = new Page<>();
        results.setRecords(records);
        results.setCurrent(rootDefalutPage.getCurrent());
        results.setTotal(rootDefalutPage.getTotal());
        results.setSize(rootDefalutPage.getSize());

        return R.success(results);
    }

    private R<Page<CommentDTO>>getChildrenComment(
            Long articleId,
            Long rootParentId,
            Integer pageSize,
            Integer pageNum){

        /**
         * 获取root评论下的所有评论
         */
        LambdaQueryWrapper<Comment>defaultPageDataGetter = new LambdaQueryWrapper<>();
        defaultPageDataGetter
                .eq(Comment::getStatus, 0)
                .eq(Comment::getIsDelete, 0)
                .eq(Comment::getArticleId, articleId)
                .eq(Comment::getRootParentId, rootParentId)
                .orderByDesc(Comment::getCreateTime);

        Page<Comment>defaultPage = new Page<>(pageNum, pageSize);
        commentService.page(defaultPage, defaultPageDataGetter);

        /**
         * 将comment转换到commentDTO中，
         */
        List<CommentDTO>records = new ArrayList<>();
        List<Comment>defaultPageRecords = defaultPage.getRecords();


        long floor = 1L;
        for(Comment comment : defaultPageRecords){
            //获取回复人 与 回复给谁的信息
            User commentUser = userService.getById(comment.getReplyBy());
            log.info("commentUser={}",commentUser);
            Comment replyToComment = commentService.getById(comment.getFatherId());
            log.info("{}",replyToComment);
            User replyToUser = userService.getById(replyToComment.getReplyBy());
            CommentDTO commentDTO = setCommentDTO(commentUser, comment);

            commentDTO.setReplyToFeature(replyToComment.getCreateTime().getTime());
            commentDTO.setReplyTo(replyToUser.getNickname());
            commentDTO.setFloor(floor++);

            //用时间来作为特征码
            //先获取回复人的ID
            commentDTO.setReplyToFeature(commentService.getById(comment.getFatherId()).getCreateTime().getTime());
            records.add(commentDTO);
        }
        Page<CommentDTO>result = new Page<>(pageNum, pageSize, defaultPage.getTotal());
        result.setRecords(records);
        return R.success(result);
    }
    private CommentDTO setCommentDTO(User commentUser, Comment comment) {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setWebsite(commentUser.getWebsite());
        commentDTO.setAvatar(commentUser.getAvatar());
        commentDTO.setNickname(commentUser.getNickname());
        commentDTO.setIpAddress(comment.getIpAddress());
        commentDTO.setLikeCount(comment.getLikeCount());
        commentDTO.setCreateTime(comment.getCreateTime());
        commentDTO.setRootParentId(comment.getRootParentId());
        commentDTO.setContent(comment.getContent());
        commentDTO.setCommentId(comment.getId());
        commentDTO.setArticleId(comment.getArticleId());
        return commentDTO;
    }
}
