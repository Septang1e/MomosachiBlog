package com.septangle.momosachiblog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.septangle.momosachiblog.domain.R;
import com.septangle.momosachiblog.domain.dto.CommentDTO;
import com.septangle.momosachiblog.domain.dto.CommentQueryDTO;
import com.septangle.momosachiblog.domain.dto.UserQueryDTO;
import com.septangle.momosachiblog.domain.entity.Article;
import com.septangle.momosachiblog.domain.entity.Comment;
import com.septangle.momosachiblog.domain.entity.User;
import com.septangle.momosachiblog.domain.repository.rabbitMq.producer.Producer;
import com.septangle.momosachiblog.module.rabbit.EmailCheckModule;
import com.septangle.momosachiblog.service.ArticleService;
import com.septangle.momosachiblog.service.ArticleTagService;
import com.septangle.momosachiblog.service.CommentService;
import com.septangle.momosachiblog.service.UserService;
import com.septangle.momosachiblog.utils.DTOUtils;
import com.septangle.momosachiblog.utils.Generator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
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
    private ArticleTagService articleTagService;
    @Resource
    private Producer producer;
    @Resource
    private RabbitTemplate rabbitTemplate;
    public static final String AMQP_SIMPLE_QUEUE = "foo.var.#";

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
            //保存 User 和 Comment
            userService.saveOrUpdate(duplicateUser);
        }

        //若用户不存在则创建一个新用户
        if(Objects.isNull(duplicateUser)){

            duplicateUserFilter.clear();
            duplicateUserFilter
                    .eq(User::getEmail, commentDTO.getEmail());

            User user = userService.getOne(duplicateUserFilter);
            if(Objects.nonNull(user)) {
                return R.error("该用户已存在！");
            }


            duplicateUser = new User(commentDTO);
            //保存 User 和 Comment

            userService.saveOrUpdate(duplicateUser);
            producer.emailCheckerProducer(duplicateUser.getEmail(), duplicateUser.getId());
        }

        Article article = articleService.getByPid(commentDTO.getArticlePid());
        if(Objects.isNull(article)) {
            log.error("pid为{}的文章不存在", commentDTO.getArticlePid());
            return R.error("文章PID出现异常");
        }

        Comment comment = DTOUtils.getByDTO(commentDTO, duplicateUser.getId());
        comment.setPid(Generator.pidGenerator());
        comment.setStatus(1);
        comment.setArticleId(article.getId());

        //检验邮箱是否正确

        commentService.save(comment);

        return R.success("评论提交成功，审核通过后即生效");
    }
    @DeleteMapping("/admin/comment")
    public R<String> deleteById(@RequestBody Long []idList, @RequestParam(required = false) String msg) {

        log.info("{}", (Object) idList);

        for(Long id : idList) {
            Comment comment = commentService.getById(id);
            if(Objects.isNull(comment)) {
                return R.error("评论 " + id + " 不存在");
            }

            // 发送邮件操作，告知评论被删除

            comment.setIsDelete(1);
            commentService.updateById(comment);
        }
        return R.success("删除成功，数据可以在回收站中恢复");
    }

    @PostMapping("/admin/comment/accept")
    public R<String> accept(@RequestBody Long []idList, @RequestParam(required = false) String msg) {

        for(Long id : idList) {
            Comment comment = commentService.getById(id);
            if(Objects.isNull(comment)) {
                return R.error("评论 " + id + " 不存在");
            }
            comment.setStatus(0);

            //发送邮件的操作

            commentService.updateById(comment);
        }
        return R.success("评论已发布");
    }

    @DeleteMapping("/admin/bin/comment")
    public R<String> removeFromDatabase(@RequestBody Long []idList) {

        for(Long id : idList) {
            Comment comment = commentService.getById(id);
            if(Objects.isNull(comment)) {
                return R.error("评论 " + id + " 不存在");
            }
            commentService.removeById(comment);
        }
        return R.success("删除成功");
    }

    @PostMapping("/admin/bin/comment")
    public R<String> recover(@RequestBody Long []idList) {

        for(Long id : idList) {
            Comment comment = commentService.getById(id);
            if(Objects.isNull(comment)) {
                return R.error("评论 " + id + " 不存在");
            }
            comment.setIsDelete(0);
            commentService.updateById(comment);
        }
        return R.success("数据恢复成功");
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
            return R.error(null);
        }

        return pagination(current, size, article, rootId, 0, order);
    }

    @GetMapping("/admin/comment/pagination/{current}/{size}")
    public  R<Page<CommentQueryDTO>> commentPagination(@PathVariable int current, @RequestParam(required = false) Integer status,
                                                       @PathVariable int size, @RequestParam(required = false) Integer is_deleted) {
        //发送的文章，发送人的用户信息，评论内容
        Page<CommentQueryDTO> result = pagination(current, size, status, is_deleted);
        return R.success(result);
    }


    @GetMapping("/comment/count/{articlePid}")
    public R<Long> getCommentCount(@PathVariable String articlePid) {

        Article article = articleService.getByPid(articlePid);
        if (Objects.isNull(article)) {
            return R.error(null);
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

    @GetMapping("/admin/comment/count")
    public R<Long> getCommentCount() {
        LambdaQueryWrapper<Comment> commentLambdaQueryWrapper = new LambdaQueryWrapper<>();
        commentLambdaQueryWrapper.eq(Comment::getIsDelete, 0);

        return R.success(commentService.count(commentLambdaQueryWrapper));
    }

    // 用于前端数据展示
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
                    .orderByAsc(Comment::getCreateTime);
        }
        //分页
        commentService.page(page, commentLambdaQueryWrapper);

        Page<CommentDTO> result = new Page<>(current, size, page.getTotal());
        List<CommentDTO> record = new ArrayList<>();
        for(Comment comment : page.getRecords()) {
            User user = userService.getById(comment.getReplyBy());

            if(user.getIsDelete() == 1) {
                continue;
            }

            CommentDTO commentDTO = getByComment(user, comment, article.getPid());

            // 如果 回复的Id不等于-1，那么就将回复人的名称加入到结果中
            if(Objects.nonNull(commentDTO.getToId())) {

                Long id = commentDTO.getToId();
                comment = commentService.getById(id);
                //通过评论获取评论用户名
                if(Objects.nonNull(comment)) {
                    user = userService.getById(comment.getReplyBy());
                    if(Objects.nonNull(user)){
                        commentDTO.setToName(user.getNickname());
                    }
                }

            }

            record.add(commentDTO);
        }
        result.setRecords(record);

        return R.success(result);
    }
    private Page<CommentQueryDTO> pagination(int current, int size, Integer status, Integer isDeleted) {
        Page<Comment> page = new Page<>(current, size);
        LambdaQueryWrapper<Comment> commentLambdaQueryWrapper = new LambdaQueryWrapper<>();

        //
        if(Objects.nonNull(status)) {
            commentLambdaQueryWrapper
                    .eq(Comment::getStatus, status);
        }
        if(Objects.nonNull(isDeleted)) {
            commentLambdaQueryWrapper
                    .eq(Comment::getIsDelete, isDeleted);
        }
        commentService.page(page, commentLambdaQueryWrapper);

        List<CommentQueryDTO> record = new ArrayList<>(page.getRecords().size());
        for(Comment comment : page.getRecords()) {
            User user = userService.getById(comment.getReplyBy());
            Article article = articleService.getById(comment.getArticleId());
            if(Objects.isNull(user)) {
                log.info("id为 {} 的用户不存在!!该评论Id为 {}", comment.getReplyBy(), comment.getId());
                user = new User();
                user.setNickname("用户不存在！！！");
            }
            if(Objects.isNull(article)) {
                log.info("id为 {} 的文章不存在!!该评论Id为 {}", comment.getArticleId(), comment.getId());
                article = new Article();
                article.setTitle("文章不存在！！！");
            }
            CommentQueryDTO commentQueryDTO = getByComment(user, comment, article);

            record.add(commentQueryDTO);
        }
        Page<CommentQueryDTO> result = new Page<>(current, size, page.getTotal());
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
        if(comment.getReplyTo() != -1) {
            commentDTO.setToId(comment.getReplyTo());
        }
        commentDTO.setContent(comment.getContent());
        commentDTO.setCommentId(comment.getId());
        commentDTO.setArticlePid(articlePid);
        return commentDTO;
    }

    private CommentQueryDTO getByComment(User user, Comment comment, Article article) {
        UserQueryDTO userQueryDTO = new UserQueryDTO();
        userQueryDTO.setNickname(user.getNickname());
        userQueryDTO.setUserId(user.getId());
        userQueryDTO.setEmail(user.getEmail());
        userQueryDTO.setIsAdmin(user.getIsAdmin());
        userQueryDTO.setEmailStatus(user.getEmailStatus());

        CommentQueryDTO result = new CommentQueryDTO();
        result.setUser(userQueryDTO);
        result.setArticleTitle(article.getTitle());
        result.setStatus(comment.getStatus());
        result.setCommentId(comment.getId());
        result.setContent(comment.getContent());

        return result;
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

        Page<Comment>rootDefaultPage = new Page<>(pageNum, pageSize);
        commentService.page(rootDefaultPage, commentDefaultDataGetter);

        //集成的分页结果
        List<CommentDTO>records = new ArrayList<>();
        long floor = 1L;
        for(Comment comment : rootDefaultPage.getRecords()){
            User commentUser = userService.getById(comment.getReplyBy());
            CommentDTO commentDTO = setCommentDTO(commentUser, comment);
            commentDTO.setFloor(floor++);
            records.add(commentDTO);
        }
        Page<CommentDTO>results = new Page<>();
        results.setRecords(records);
        results.setCurrent(rootDefaultPage.getCurrent());
        results.setTotal(rootDefaultPage.getTotal());
        results.setSize(rootDefaultPage.getSize());

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