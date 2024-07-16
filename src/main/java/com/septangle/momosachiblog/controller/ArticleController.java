package com.septangle.momosachiblog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.septangle.momosachiblog.constant.Constants;
import com.septangle.momosachiblog.domain.R;
import com.septangle.momosachiblog.domain.common.BaseContext;
import com.septangle.momosachiblog.domain.dto.ArticleDTO;
import com.septangle.momosachiblog.domain.dto.ArticleUploadDTO;
import com.septangle.momosachiblog.domain.dto.TagDTO;
import com.septangle.momosachiblog.domain.entity.*;
import com.septangle.momosachiblog.domain.repository.ArticleRepository;
import com.septangle.momosachiblog.domain.repository.CategoryRepository;
import com.septangle.momosachiblog.domain.repository.TagRepository;
import com.septangle.momosachiblog.module.es.ArticleModule;
import com.septangle.momosachiblog.module.es.CategoryModule;
import com.septangle.momosachiblog.module.es.TagModule;
import com.septangle.momosachiblog.service.*;
import com.septangle.momosachiblog.utils.DTOUtils;
import com.septangle.momosachiblog.utils.Generator;
import com.septangle.momosachiblog.utils.PaginationUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;

import java.util.*;


@RestController
@Slf4j
@RequestMapping("/")
public class ArticleController {
    @Autowired
    private ArticleService articleService;
    @Autowired
    private TagService tagService;
    @Autowired
    private ArticleTagService articleTagService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private CommentService commentService;

    /**
     * 分页
     * @param size
     * @param current
     * @return
     */
    @GetMapping("/api/article/pagination/{current}/{size}")
    public R<Page<ArticleDTO>> articlePagination(
            @PathVariable int current, @PathVariable int size,
            @RequestParam(required = false)String categoryPid,
            @RequestParam(required = false)String tagPid,
            @RequestParam(required = false)String keyword
    ){
        String msg = "";
        Page<ArticleDTO>result = new Page<>(current, size);
        if(categoryPid != null && !categoryPid.isBlank()){

            //通过pid获取Category
            LambdaQueryWrapper<Category>categoryGetter = new LambdaQueryWrapper<>();
            categoryGetter.eq(Category::getPid, categoryPid)
                    .eq(Category::getStatus, 0)
                    .eq(Category::getIsDelete, 0);

            Category resultCategory = categoryService.getOne(categoryGetter);

            if(resultCategory == null){
                return R.error("分类不存在");
            }

            msg = "<div class='search-info-box'><div class='search-inner-box'>分类 <p class='search_info'>"+resultCategory.getName()+"</p> 下的文章</div></div>";
            LambdaQueryWrapper<Article> basicPageGetter = new LambdaQueryWrapper<>();
            basicPageGetter
                    .eq(Article::getStatus, 0)
                    .eq(Article::getIsDelete, 0)
                    .eq(Article::getCategoryId, resultCategory.getId())
                    .orderByAsc(Article::getCreateTime);

            Page<Article>basicPage = new Page<>(current, size);
            articleService.page(basicPage, basicPageGetter);

            List<ArticleDTO>resultRecords = setArticleDTOByArticle(basicPage.getRecords());

            result.setRecords(resultRecords);
            result.setSize(basicPage.getSize());
            result.setTotal(basicPage.getTotal());
            result.setCurrent(basicPage.getCurrent());

        }else if(tagPid != null && !tagPid.isBlank()) {
            Tag resultTag = tagService.getByPid(tagPid);
            if(resultTag == null){
                return R.error("标签不存在");
            }
            msg = "<div class='search-info-box'><div class='search-inner-box'>标签 <p class='search_info'>" + resultTag.getName() + "</p> 下的文章</div></div>";

            Long tagId = resultTag.getId();


            // 通过 tag 获取 article
            List<Article> articles = articleService.getArticleWithTagId(tagId);
            Page<Article> articlePage = PaginationUtils.getByRecords(articles, current, size);

            List<ArticleDTO> resultRecords = setArticleDTOByArticle(articlePage.getRecords());

            result.setTotal(articlePage.getTotal());
            result.setRecords(resultRecords);

        }else if(keyword != null && !keyword.isBlank()) {
            //分页结果
            List<ArticleDTO> records = new ArrayList<>();
            //用于去重
            Set<Long>duplicateArticleRemover = new HashSet<>();
            List<ArticleModule> articleModules = articleRepository.findArticleModulesByTitleLike(keyword);



            //将通过 文章标题 找到的文章添加到 分页结果 中
            for (ArticleModule articleModule : articleModules) {
                Article article = articleService.getByPid(articleModule.getPid());
                if(article != null) {
                    records.add(DTOUtils.setArticleDTO(article));
                    duplicateArticleRemover.add(article.getId());

                    Category category = categoryService.getById(article.getCategoryId());
                    ArticleDTO articleDTO = DTOUtils.setArticleDTO(article);
                    articleDTO.setCategoryPid(category.getPid());
                    articleDTO.setCategory(category.getName());
                }
            }

            //将通过 标签名称 找到的文章添加到 分页结果 中
            List<TagModule> tagModules = tagRepository.findTagModulesByNameLike(keyword);

            for(TagModule tagModule : tagModules) {
                List<Article> articles = articleService.getAllByTagPid(tagModule.getPid());
                for(Article article : articles) {
                    //用于去重
                    if (article == null ||
                                    article.getStatus() != 0 ||
                                    duplicateArticleRemover.contains(article.getId())) continue;
                    duplicateArticleRemover.add(article.getId());

                    Category category = categoryService.getById(article.getCategoryId());
                    ArticleDTO articleDTO = DTOUtils.setArticleDTO(article);
                    articleDTO.setCategoryPid(category.getPid());
                    articleDTO.setCategory(category.getName());

                    records.add(articleDTO);
                }
            }

            //将通过 分类名称 找到的文章添加到 分页结果 中
            List<CategoryModule> categoryModules = categoryRepository.findCategoryModulesByNameLike(keyword);

            for(CategoryModule categoryModule : categoryModules) {
                List<Article> articles = articleService.getByCategoryPid(categoryModule.getPid());
                for(Article article : articles) {

                    if(duplicateArticleRemover.contains(article.getId())) continue;
                    duplicateArticleRemover.add(article.getId());

                    ArticleDTO articleDTO = DTOUtils.setArticleDTO(article);
                    articleDTO.setCategoryPid(categoryModule.getPid());
                    articleDTO.setCategory(categoryModule.getName());

                    records.add(articleDTO);
                }
            }
            for(ArticleDTO articleDTO : records) {

                List<Tag> tags = tagService.getAllByArticlePid(articleDTO.getPid());
                List<TagDTO> tagDTOList = new ArrayList<>();
                for(Tag tag : tags) {
                    tagDTOList.add(new TagDTO(tag, tagService.articleCount(tag.getId())));
                }
                articleDTO.setTags(tagDTOList);
            }
            result = PaginationUtils.getByRecords(records, current, size);

        }else{
            LambdaQueryWrapper<Article> basicPageGetter = new LambdaQueryWrapper<>();
            basicPageGetter
                    .eq(Article::getStatus, 0)
                    .eq(Article::getIsDelete, 0);
            msg = "none-msg";

            Page<Article> basicPage = new Page<>(current, size);

            articleService.page(basicPage, basicPageGetter);
            List<Article> records = basicPage.getRecords();
            List<ArticleDTO> resultRecords = setArticleDTOByArticle(records);

            result.setRecords(resultRecords);
            result.setTotal(basicPage.getTotal());

        }
        return R.success(result, msg);
    }


    @GetMapping("/article/{pid}")
    public R<ArticleDTO> getArticle(@PathVariable String pid) {

        Article resultArticle = articleService.getByPid(pid);

        if(resultArticle == null) {
            return R.error("文章不存在");
        }

        //获取文章信息
        ArticleDTO result = DTOUtils.setArticleDTO(resultArticle);
        result.setThumbnail("");

        List<Tag> tagList = tagService.getTagsWithArticleId(resultArticle.getId());
        List<TagDTO> tagDTOList = new ArrayList<>();
        for(Tag tag : tagList) {
            tagDTOList.add(new TagDTO(tag, tagService.articleCount(tag.getId())));
        }

        result.setTags(tagDTOList);

        return R.success(result);

    }

    @GetMapping("/api/admin/article/pagination/{current}/{size}")
    public R<Page<ArticleUploadDTO>> adminArticlePagination(@PathVariable int current
            , @PathVariable int size, @RequestParam(required = false) String keyword) {

        if(Objects.isNull(keyword)) {
            return queryAdminPage(current, size, 0);
        }else{
            return queryAdminPageWithKeyword(current, size, keyword, 0);
        }

    }


    @PostMapping("/api/v1/article/view")
    public R<String> increaseViewCount(@RequestBody String articlePid, HttpServletRequest req) {
        String ipAddress = req.getRemoteAddr();

        BaseContext.setCurrentId(Constants.likeOrViewCountUpdateUserId);
        Article article = articleService.getByPid(articlePid);
        article.setViewCount(article.getViewCount() + 1);
        articleService.updateById(article);
        return R.success("");
    }
    @PostMapping("/api/v1/article/like")
    public R<String> likeCountHandler(@RequestBody String articlePid, HttpServletRequest req, @RequestParam int status) {
        String ipAddress = req.getRemoteAddr();

        Article article = articleService.getByPid(articlePid);
        if(article == null) {
            return R.error("文章不存在");
        }
        if(status == 0) {
            article.setLikeCount(article.getLikeCount() + 1);
        }else{
            article.setLikeCount(article.getLikeCount() - 1);
        }
        BaseContext.setCurrentId(Constants.likeOrViewCountUpdateUserId);
        articleService.updateById(article);
        return R.success("点赞成功");
    }


    @DeleteMapping("/api/admin/article/delete")
    public R<String> delete(@RequestBody Long[] idList) {
        for(Long id : idList) {
            Article article = articleService.getById(id);

            if(Objects.isNull(article)) {
                return R.error("id为" + id + "的文章不存在");
            }
            article.setIsDelete(1);

            articleService.updateById(article);
        }
        return R.success("删除成功，可在回收站中恢复数据");
    }

    @DeleteMapping("/api/admin/bin/article")
    public R<String> removeFromDataBase(@RequestBody Long [] idList) {

        for(Long articleId : idList) {
            if(Objects.isNull(articleId)) {
                return R.error("Id不能为空");
            }
            //删除articleTag中和article有关的信息
            LambdaQueryWrapper<ArticleTag> articleTagLambdaQueryWrapper = new LambdaQueryWrapper<>();
            articleTagLambdaQueryWrapper
                    .eq(ArticleTag::getArticleId, articleId);

            articleTagService.remove(articleTagLambdaQueryWrapper);
            articleService.removeById(articleId);

            //删除和article有关的所有评论
            LambdaQueryWrapper<Comment> commentLambdaQueryWrapper = new LambdaQueryWrapper<>();
            commentLambdaQueryWrapper
                    .eq(Comment::getArticleId, articleId);
            commentService.remove(commentLambdaQueryWrapper);
        }

        return R.success("删除成功");
    }

    @PostMapping("/api/admin/bin/article")
    public R<String> recycle(@RequestBody Long[] idList) {

        for(Long articleId : idList) {
            Article article = articleService.getById(articleId);
            if(Objects.isNull(article)) {
                return R.error("id为 " + articleId + "的文章不存在");
            }
            article.setIsDelete(0);
            articleService.updateById(article);
        }
        return R.error("数据恢复成功");
    }

    @GetMapping("/api/admin/bin/article/pagination/{current}/{size}")
    public R<Page<ArticleUploadDTO>> articleBinPagination(@PathVariable int current
            , @PathVariable int size, @RequestParam(required = false) String keyword) {

        Page<ArticleUploadDTO> result;
        if(Objects.isNull(keyword)) {
            result = queryAdminPage(current, size, 1).getData();
        }else{
            result = queryAdminPageWithKeyword(current, size, keyword, 1).getData();
        }

        //把 封面 去掉避免占用不必要的资源
        for(ArticleUploadDTO articleUploadDTO : result.getRecords()) {
            articleUploadDTO.setThumbnail("");
        }

        return R.success(result);

    }

    @PostMapping("/api/admin/article/save")
    public R<String>handleSave(@RequestBody ArticleUploadDTO articleDTO) {

        Article article = new Article();
        log.info("currentThreadOnSave = {}", Thread.currentThread().getName());

        articleDTO.setThumbnail(Base64.getEncoder().encodeToString(articleDTO.getThumbnail().getBytes()));

        //update category
        LambdaQueryWrapper<Category> categoryLambdaQueryWrapper = new LambdaQueryWrapper<>();
        categoryLambdaQueryWrapper
                .eq(Category::getName, articleDTO.getCategory());
        Category category = categoryService.getOne(categoryLambdaQueryWrapper);
        if(category == null) {
            category = new Category();
            category.setPid(Generator.pidGenerator());
            category.setName(articleDTO.getCategory());

            categoryService.save(category);
        }

        //update Article
        article.setContent(articleDTO.getContent());
        article.setCategoryId(category.getId());
        article.setPid(Generator.pidGenerator());
        article.setTitle(articleDTO.getTitle());
        article.setThumbnail(articleDTO.getThumbnail());
        article.setDescription(articleDTO.getDescription());
        article.setStatus(articleDTO.getStatus());

        articleService.save(article);

        //处理tags
        addTagByArticleDTO(articleDTO, article);


        return R.success("添加成功");
    }

    @PostMapping("/api/admin/article/edit")
    public R<String>handelEdit(@RequestBody ArticleUploadDTO articleDTO){

        Article article = articleService.getByPid(articleDTO.getPid());

        if(article == null) return R.error("文章不存在");

        LambdaQueryWrapper<Category> categoryLambdaQueryWrapper = new LambdaQueryWrapper<>();
        categoryLambdaQueryWrapper
                .eq(Category::getName, articleDTO.getCategory());

        Category category = categoryService.getOne(categoryLambdaQueryWrapper);
        if(category == null) {
            category = new Category();
            category.setName(articleDTO.getCategory());
            category.setPid(Generator.pidGenerator());
            categoryService.save(category);
        }

        //update Article
        article.setStatus(articleDTO.getStatus());
        article.setContent(articleDTO.getContent());
        article.setCategoryId(category.getId());
        article.setTitle(articleDTO.getTitle());
        article.setThumbnail(articleDTO.getThumbnail());
        article.setDescription(articleDTO.getDescription());

        articleService.updateById(article);

        //更新articleTag
        addTagByArticleDTO(articleDTO, article);

        return R.success("文章更新成功");
    }

    @PostMapping("/api/admin/article/status")
    public R<String>statusHandler(@RequestBody Long[] idList) {

        for (Long id : idList) {

            Article article = articleService.getById(id);

            if(article == null) {
                return R.error("pid为" + id + "的文章不存在");
            }

            article.setStatus(article.getStatus() == 0 ? 1 : 0);
            log.info("status is {}", article.getStatus());
            articleService.updateById(article);
        }

        return R.success("状态改变成功");
    }

    @GetMapping("/api/admin/view/count")
    public R<Long> getViewCount() {

        List<Article> articles = articleService.list();
        Long result = 0L;
        for(Article article : articles) {
            if(article.getIsDelete() == 0){
                result += article.getViewCount();
            }
        }
        return R.success(result);
    }

    @GetMapping("/api/article/archive")
    public R<ArticleDTO> getArchiveData() {
        return null;
    }



    private void addTagByArticleDTO(ArticleUploadDTO articleDTO, Article article) {
        List<Long> tagIdList = new ArrayList<>();
        for(Tag tag : articleDTO.getTags()) {

            LambdaQueryWrapper<Tag> tagLambdaQueryWrapper = new LambdaQueryWrapper<>();
            tagLambdaQueryWrapper
                    .eq(Tag::getName, tag.getName());

            Tag resultTag = tagService.getOne(tagLambdaQueryWrapper);

            if(resultTag == null) {
                tag.setPid(Generator.pidGenerator());
                tag.setId(null);
                tagService.save(tag);
                resultTag = tag;
            }

            tagIdList.add(resultTag.getId());
        }
        articleTagService.updateArticleTag(article.getId(), tagIdList);
    }



    private R<Page<ArticleUploadDTO>> queryAdminPageWithKeyword(int current, int size, @NonNull String keyword, int status) {

        List<ArticleUploadDTO> records = new ArrayList<>();
        log.info("keyword is {}", keyword);

        List<TagModule> tagModules = tagRepository.findTagModulesByNameLike(keyword);
        List<ArticleModule> articleModules = articleRepository.findArticleModulesByTitleLike(keyword);
        List<CategoryModule> categoryModules = categoryRepository.findCategoryModulesByNameLike(keyword);

        Set<Long> duplicateArticleRemover = new HashSet<>();

        //将搜索到的tag名称所包含的文章添加到分页结果中
        for(TagModule tagModule : tagModules) {
            List<Article> articles = articleService.getAllByTagPid(tagModule.getPid());

            for(Article article : articles) {
                duplicateArticleRemover.add(article.getId());
            }
            records.addAll(DTOUtils.getArticleUploadDTOByArticle(
                    articles.stream()
                            .filter(article -> article.getIsDelete() == status)
                            .toList()
            ));
        }

        //将搜索到的文章名称所包含的文章添加到分页结果中
        for(ArticleModule articleModule : articleModules) {

            LambdaQueryWrapper<Article> articleLambdaQueryWrapper = new LambdaQueryWrapper<>();
            articleLambdaQueryWrapper
                    .eq(Article::getPid, articleModule.getPid());

            Article article = articleService.getOne(articleLambdaQueryWrapper);
            if(article == null) {
                log.info("标题为{}的文章不存在",articleModule.getTitle());
                return R.error("Elasticsearch中字段错误");
            }
            if(!duplicateArticleRemover.contains(article.getId())
                && article.getIsDelete() == status
            ) {
                records.add(DTOUtils.getArticleUploadDTOByArticle(article));
                duplicateArticleRemover.add(article.getId());
            }
        }
        //将通过名称检索到的Category添加到分页结果中
        for (CategoryModule categoryModule : categoryModules) {
            LambdaQueryWrapper<Category> categoryLambdaQueryWrapper = new LambdaQueryWrapper<>();
            //获取检索到的category
            categoryLambdaQueryWrapper
                    .eq(Category::getPid, categoryModule.getPid());

            Category category = categoryService.getOne(categoryLambdaQueryWrapper);

            LambdaQueryWrapper<Article> articleLambdaQueryWrapper = new LambdaQueryWrapper<>();
            articleLambdaQueryWrapper
                    .eq(Article::getCategoryId, category.getId());

            List<Article> articles = articleService.list(articleLambdaQueryWrapper);
            for(Article article : articles) {
                if(!duplicateArticleRemover.contains(article.getId())
                    && article.getIsDelete() == status
                ) {
                    records.add(DTOUtils.getArticleUploadDTOByArticle(article));
                }
            }
        }
        addCategoryAndTagsForArticleUploadDTOs(records);
        Page<ArticleUploadDTO> page = PaginationUtils.getByRecords(records, current, size);

        return R.success(page);
    }

    private R<Page<ArticleUploadDTO>> queryAdminPage(int current, int size, int status) {
        Page<Article> articlePage = new Page<>(current, size);
        LambdaQueryWrapper<Article> articleLambdaQueryWrapper = new LambdaQueryWrapper<>();
        articleLambdaQueryWrapper
                .eq(Article::getIsDelete, status)
                .orderByDesc(Article::getUpdateTime);
        articleService.page(articlePage, articleLambdaQueryWrapper);

        List<ArticleUploadDTO> record = DTOUtils.getArticleUploadDTOByArticle(articlePage.getRecords());
        addCategoryAndTagsForArticleUploadDTOs(record);

        Page<ArticleUploadDTO> result = new Page<>(current, size, articlePage.getTotal());
        result.setRecords(record);
        return R.success(result);
    }

    private void addCategoryAndTagsForArticleUploadDTOs(List<ArticleUploadDTO> record) {
        for(ArticleUploadDTO articleUploadDTO : record) {
            //category
            Long categoryId = articleUploadDTO.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if(Objects.isNull(category) || category.getIsDelete() == 1) {
                articleUploadDTO.setCategory("");
            }else{
                articleUploadDTO.setCategory(categoryService.getById(categoryId).getName());
            }

            //tags
            List<Tag> tagList = tagService.getAllByArticlePid(articleUploadDTO.getPid());
            articleUploadDTO.setTags(tagList);
        }
    }

    private List<ArticleDTO> setArticleDTOByArticle(List<Article> records) {
        List<ArticleDTO> resultRecords = new ArrayList<>();
        for(Article article : records) {
            ArticleDTO articleDTO = DTOUtils.setArticleDTO(article);
            Category resultCategory = categoryService.getById(article.getCategoryId());
            if(Objects.isNull(resultCategory)) {
                articleDTO.setCategory("");
                articleDTO.setCategoryPid("");
            }else{
                articleDTO.setCategory(resultCategory.getName());
                articleDTO.setCategoryPid(resultCategory.getPid());
            }

            List<TagDTO> tagDTOList = new ArrayList<>();
            List<Tag> tagList = tagService.getTagsWithArticleId(article.getId());

            for(Tag tag : tagList) {
                tagDTOList.add(new TagDTO(tag, tagService.articleCount(tag.getId())));
            }

            articleDTO.setTags(tagDTOList);
            resultRecords.add(articleDTO);
        }
        return resultRecords;
    }

}
