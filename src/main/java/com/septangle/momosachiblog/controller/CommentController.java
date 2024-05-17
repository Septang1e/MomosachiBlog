package com.septangle.momosachiblog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.septangle.momosachiblog.constant.UserConstant;
import com.septangle.momosachiblog.domain.R;
import com.septangle.momosachiblog.domain.dto.CommentDTO;
import com.septangle.momosachiblog.domain.entity.Article;
import com.septangle.momosachiblog.domain.entity.ArticleTag;
import com.septangle.momosachiblog.domain.entity.Comment;
import com.septangle.momosachiblog.domain.entity.User;
import com.septangle.momosachiblog.service.ArticleService;
import com.septangle.momosachiblog.service.ArticleTagService;
import com.septangle.momosachiblog.service.CommentService;
import com.septangle.momosachiblog.service.UserService;
import com.septangle.momosachiblog.utils.DTOUtils;
import com.septangle.momosachiblog.utils.Generator;
import lombok.Builder;
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
@RequestMapping("/api")
public class CommentController {

    @Autowired
    UserService userService;
    @Autowired
    CommentService commentService;
    @Autowired
    ArticleService articleService;
    @Resource
    ArticleTagService articleTagService;

    @PostMapping("/comment")
    public R<String> addComment(@RequestBody CommentDTO commentDTO, HttpServletRequest request){

        //处理ip属地信息

        /**
         * 获取IP归属地的API
         * http://ip-api.com/json/24.48.0.1?lang=zh-CN
         */

        // 检查是否存在 相同昵称 不同邮箱 的用户
        LambdaQueryWrapper<User>duplicateUserFilter = new LambdaQueryWrapper<>();
        duplicateUserFilter
                .eq(User::getNickname, commentDTO.getNickname())
                .eq(User::getIsDelete, 0);
        User duplicateUser = userService.getOne(duplicateUserFilter);
        if(duplicateUser != null && !duplicateUser.getEmail().equals(commentDTO.getEmail())){
            return R.error("该用户已存在!!!");
        }

        //更新头像
        if(Objects.nonNull(duplicateUser) && !commentDTO.getAvatar().equals("not-update")){
            duplicateUser.setAvatar(commentDTO.getAvatar());
        }

        //若用户不存在则创建一个新用户
        if(Objects.isNull(duplicateUser)){
            User user = new User();
            user.setUserByCommentDTO(commentDTO);
            duplicateUser = user;
        }

        //保存 User 和 Comment
        userService.saveOrUpdate(duplicateUser);
        Comment comment = DTOUtils.getByDTO(commentDTO, duplicateUser.getId());
        comment.setStatus(1);
        commentService.save(comment);

        return R.success("评论提交成功，审核通过后即生效");
    }


    @GetMapping("/comment/pagination/{current}/{size}")
    public R<Page<CommentDTO>> getComment(
            @RequestParam Long rootId,
            @RequestParam String articlePid,
            @RequestParam(required = false, value = "create-time") String order,
            @PathVariable Integer size,
            @PathVariable Integer current){

        Article article = articleService.getByPid(articlePid);
        if(Objects.isNull(article)) {
            return R.error("文章不存在，或已被删除");
        }

        return pagination(current, size, article, rootId, 0, order);
    }

    @GetMapping("/admin/comment/pagination/{current}/{size}")
    public  R<Page<CommentDTO>> commentPagination(@PathVariable int current,
                                                  @PathVariable int size) {
        //发送的文章，发送人的用户信息，评论内容
        Page<CommentDTO> result = pagination(current, size, null, 0);
        return R.success(result);
    }


    @GetMapping("/comment/count/{articlePid}")
    public R<Long> getCommentCount(@PathVariable String articlePid) {

        Article article = articleService.getByPid(articlePid);
        if (Objects.isNull(article)) {
            return R.error("文章不存在");
        }
        Long articleId = article.getId();

        LambdaQueryWrapper<Comment> commentCountGetter = new LambdaQueryWrapper<>();
        commentCountGetter
                .eq(Comment::getArticleId, articleId)
                .eq(Comment::getStatus, 0)
                .eq(Comment::getIsDelete, 0);

        return R.success(commentService.count(commentCountGetter));
    }

    @PostMapping("/comment/like/{commentId}")
    public R<String> likeComment(@PathVariable Long commentId, @RequestParam int status) {
        if(status == 0) {
            return updateUserLikeStatusIncrease(commentId);
        }else{
            return updateUserLikeStatusDecrease(commentId);
        }
    }

    private R<Page<CommentDTO>> pagination(int current, int size,
                                           Article article, Long rootId,
                                           int isDeleted, String order
    ) {
        //创建筛选条件
        Page<Comment> page = new Page<>(current, size);
        LambdaQueryWrapper<Comment> commentLambdaQueryWrapper = new LambdaQueryWrapper<>();
        commentLambdaQueryWrapper
                .eq(Comment::getArticleId, article.getId())
                .eq(Comment::getRootId, rootId)
                .eq(Comment::getIsDelete, isDeleted)
                .eq(Comment::getStatus, 0);
        if("hot".equals(order)) {
            commentLambdaQueryWrapper
                    .orderByDesc(Comment::getLikeCount);
        }else if("create-time".equals(order)) {
            commentLambdaQueryWrapper
                    .orderByDesc(Comment::getCreateTime);
        }
        //分页
        commentService.page(page, commentLambdaQueryWrapper);

        Page<CommentDTO> result = new Page<>(current, size, page.getTotal());
        List<CommentDTO> record = new ArrayList<>();
        for(Comment comment : page.getRecords()) {
            User user = userService.getById(comment.getReplyBy());
            CommentDTO commentDTO = getByComment(user, comment, article.getPid());
            record.add(commentDTO);
        }
        result.setRecords(record);

        return R.success(result);
    }
    private Page<CommentDTO> pagination(int current, int size, Integer status, Integer isDeleted) {
        Page<Comment> page = new Page<>(current, size);
        LambdaQueryWrapper<Comment> commentLambdaQueryWrapper = new LambdaQueryWrapper<>();

        //
        if(Objects.nonNull(status)) {
            commentLambdaQueryWrapper
                    .eq(Comment::getStatus, 0);
        }
        if(Objects.nonNull(isDeleted)) {
            commentLambdaQueryWrapper
                    .eq(Comment::getIsDelete, isDeleted);
        }
        commentService.page(page, commentLambdaQueryWrapper);

        List<CommentDTO> record = new ArrayList<>(page.getRecords().size());
        for(Comment comment : page.getRecords()) {
            User user = userService.getById(comment.getReplyBy());
            Article article = articleService.getById(comment.getArticleId());
            if(Objects.isNull(user)) {
                log.info("id为 {} 的用户不存在!!该评论Id为 {}", comment.getReplyBy(), comment.getId());
                continue;
            }
            if(Objects.isNull(article)) {
                log.info("id为 {} 的文章不存在!!该评论Id为 {}", comment.getArticleId(), comment.getId());
                continue;
            }

            CommentDTO commentDTO = getByComment(user, comment, article.getPid());
            commentDTO.setArticle(article.getTitle());
            commentDTO.setEmail(user.getEmail());
            record.add(commentDTO);
        }
        Page<CommentDTO> result = new Page<>(current, size, page.getTotal());
        result.setRecords(record);
        return result;
    }

    //utils
    public R<String> updateUserLikeStatusIncrease(@PathVariable Long commentId){
        Comment comment = commentService.getById(commentId);
        comment.setLikeCount(comment.getLikeCount() + 1);
        commentService.saveOrUpdate(comment);
        return R.success("点赞成功");
    }
    public R<String> updateUserLikeStatusDecrease(@PathVariable Long commentId){
        Comment comment = commentService.getById(commentId);
        comment.setLikeCount(comment.getLikeCount() - 1);
        commentService.saveOrUpdate(comment);
        return R.success("取消点赞成功");
    }

    private CommentDTO getByComment(User commentUser, Comment comment, String articlePid) {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setWebsite(commentUser.getWebsite());
        commentDTO.setAvatar(commentUser.getAvatar());
        commentDTO.setNickname(commentUser.getNickname());
        commentDTO.setIpAddress(comment.getIpAddress());
        commentDTO.setLikeCount(comment.getLikeCount());
        commentDTO.setCreateTime(comment.getCreateTime());
        commentDTO.setRootId(comment.getRootId());
        commentDTO.setToId(commentDTO.getToId());
        commentDTO.setContent(comment.getContent());
        commentDTO.setCommentId(comment.getId());
        commentDTO.setArticlePid(articlePid);
        return commentDTO;
    }


    /*
    private R<Page<CommentDTO>> getRootCommentWithArticleID(
            String articlePid,
            Integer pageNum, Integer pageSize){

        //将 articlePid 转换为 articleId
        Long id = articleService.getByPid(articlePid).getId();


        //初始的分页结果
        LambdaQueryWrapper<Comment>commentDefaultDataGetter = new LambdaQueryWrapper<>();
        commentDefaultDataGetter
                .eq(Comment::getIsDelete, 0)
                .eq(Comment::getStatus, 0)
                .eq(Comment::getArticleId, id)
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

*/

    /*
    private Comment getByPid(String pid, int isDeleted) {
        LambdaQueryWrapper<Comment>commentLambdaQueryWrapper = new LambdaQueryWrapper<>();
        commentLambdaQueryWrapper
                .eq(Comment::getPid, pid)
                .eq(Comment::getIsDelete, isDeleted);

        return commentService.getOne(commentLambdaQueryWrapper);
    }
    */

}