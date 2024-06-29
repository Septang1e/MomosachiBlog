package com.septangle.momosachiblog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.septangle.momosachiblog.domain.R;
import com.septangle.momosachiblog.domain.entity.Article;
import com.septangle.momosachiblog.domain.entity.Category;
import com.septangle.momosachiblog.domain.entity.Tag;
import com.septangle.momosachiblog.domain.repository.ArticleRepository;
import com.septangle.momosachiblog.domain.repository.CategoryRepository;
import com.septangle.momosachiblog.domain.repository.TagRepository;
import com.septangle.momosachiblog.module.es.ArticleModule;
import com.septangle.momosachiblog.module.es.CategoryModule;
import com.septangle.momosachiblog.module.es.TagModule;
import com.septangle.momosachiblog.service.ArticleService;
import com.septangle.momosachiblog.service.CategoryService;
import com.septangle.momosachiblog.service.TagService;
import com.septangle.momosachiblog.utils.Generator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@Slf4j
@RequestMapping("/api/")
public class CommonController {
    @Autowired
    private ArticleService articleService;
    @Autowired
    private TagService tagService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * 获取
     * @return 文章总数
     */
    public Long getArticleCount(){
        LambdaQueryWrapper<Article>articleLambdaQueryWrapper=new LambdaQueryWrapper<>();
        articleLambdaQueryWrapper
                .eq(Article::getStatus,0)
                .eq(Article::getIsDelete, 0)
        ;
        return articleService.count(articleLambdaQueryWrapper);
    }

    /**
     *
     * @return 标签的总数
     */
    public Long getTagCount(){  
        LambdaQueryWrapper<Tag>tagLambdaQueryWrapper=new LambdaQueryWrapper<>();
        tagLambdaQueryWrapper
                .eq(Tag::getStatus,0)
                .eq(Tag::getIsDelete, 0);
        return tagService.count(tagLambdaQueryWrapper);
    }

    /**
     *
     * @return 分类的个数
     */
    public Long getCategoryCount() {
        LambdaQueryWrapper<Category> categoryLambdaQueryWrapper = new LambdaQueryWrapper<>();
        categoryLambdaQueryWrapper
                .eq(Category::getStatus, 0)
                .eq(Category::getIsDelete, 0);
        return categoryService.count(categoryLambdaQueryWrapper);
    }

    //
    //

    @GetMapping("v1/total/admin")
    public R<Map<String, Long>> getAllCount() {
        //构造返回内容
        Map<String, Long> countAll = new HashMap<>();
        countAll.put("article_count", getArticleCount());
        countAll.put("category_count", getCategoryCount());
        countAll.put("tag_count", getTagCount());

        return R.success(countAll);
    }

    @GetMapping("/admin/generator/pid")
    public R<String> generatePid() {
        return R.success(Generator.pidGenerator());
    }



    @PostMapping("/v2/tag/autocomplete")
    public R<List<String>> autocompleteOnTag(@RequestBody String name) {
        name = name.trim();
        if(name.isEmpty()) {
           return R.success(new ArrayList<>());
        }
        List<String> result = new ArrayList<>();
        List<TagModule> tagModules = tagRepository.findTagModulesByNameLike(name);
        for(int i = 0; i < tagModules.size() && i < 7; ++i) {
            result.add(tagModules.get(i).getName());
        }
        return R.success(result);
    }
    @PostMapping("/v2/category/autocomplete")
    public R<List<String>> autocompleteOnCategory(@RequestBody String name) {
        name = name.trim();
        if(name.isEmpty()) {
            return R.success(new ArrayList<>());
        }
        List<String> result = new ArrayList<>();
        List<CategoryModule> categoryModules = categoryRepository.findCategoryModulesByNameLike(name);
        for(int i = 0; i < categoryModules.size() && i < 7; ++i) {
            result.add(categoryModules.get(i).getName());
        }
        return R.success(result);
    }

    @PostMapping("/v2/autocomplete")
    public R<List<String>> autocomplete(@RequestBody String name){
        List<String> result = new ArrayList<>();
        List<TagModule> tagModules = tagRepository.findTagModulesByNameLike(name);
        List<ArticleModule> articleModules= articleRepository.findArticleModulesByTitleLike(name);
        List<CategoryModule> categoryModules = categoryRepository.findCategoryModulesByNameLike(name);

        log.info("name is {}", name);
        log.info("{}", tagModules);
        log.info("{}", articleModules);

        for(int i = 0; i < 4; ++i) {
            if(i < tagModules.size()) result.add(tagModules.get(i).getName());
            if(i < articleModules.size()) result.add(articleModules.get(i).getTitle());
            if(i < categoryModules.size()) result.add(categoryModules.get(i).getName());
        }

        return R.success(result);
    }

    @PostMapping("/admin/elasticsearch/data/synchronize")
    public R<String>dataSynchronize() {

        //删除操作
        Iterable<CategoryModule> categoryModules = categoryRepository.findAll();

        categoryModules.forEach(categoryModule -> {
            LambdaQueryWrapper<Category> categoryLambdaQueryWrapper = new LambdaQueryWrapper<>();
            categoryLambdaQueryWrapper
                    .eq(Category::getPid, categoryModule.getPid())
                    .eq(Category::getIsDelete, 0);
            Category category = categoryService.getOne(categoryLambdaQueryWrapper);
            if(category == null) {
                categoryRepository.deleteById(categoryModule.getId());
            }
        });

        Iterable<TagModule> tagModules = tagRepository.findAll();
        tagModules.forEach(tagModule -> {
            LambdaQueryWrapper<Tag> tagLambdaQueryWrapper = new LambdaQueryWrapper<>();
            tagLambdaQueryWrapper
                    .eq(Tag::getIsDelete, 0)
                    .eq(Tag::getPid, tagModule.getPid());
            Tag tag = tagService.getOne(tagLambdaQueryWrapper);
            if(tag == null) {
                tagRepository.deleteById(tagModule.getId());
            }
        });
        Iterable<ArticleModule> articleModules = articleRepository.findAll();
        articleModules.forEach(articleModule -> {
            LambdaQueryWrapper<Article> articleLambdaQueryWrapper = new LambdaQueryWrapper<>();
            articleLambdaQueryWrapper
                    .eq(Article::getIsDelete, 0)
                    .eq(Article::getPid, articleModule.getPid());
            Article article = articleService.getOne(articleLambdaQueryWrapper);
            if(article == null) {
                articleRepository.deleteById(articleModule.getId());
            }
        });

        LambdaQueryWrapper<Article> articleLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //添加操作
        articleLambdaQueryWrapper.eq(Article::getIsDelete, 0);
        List<Article> articles = articleService.list(articleLambdaQueryWrapper);
        for(Article article : articles) {
            ArticleModule duplicateModule = articleRepository.findArticleModuleByTitle(article.getTitle());
            if(duplicateModule != null) {
                continue;
            }

            ArticleModule articleModule = new ArticleModule();
            articleModule.setPid(article.getPid());
            articleModule.setTitle(article.getTitle());
            articleRepository.save(articleModule);
        }

        articles = null;
        LambdaQueryWrapper<Tag> tagLambdaQueryWrapper = new LambdaQueryWrapper<>();
        tagLambdaQueryWrapper.eq(Tag::getIsDelete, 0);
        List<Tag> tagList = tagService.list(tagLambdaQueryWrapper);

        for(Tag tag : tagList) {
            TagModule duplicateModule = tagRepository.findTagModuleByName(tag.getName());
            if(duplicateModule != null) continue;

            TagModule tagModule = new TagModule();
            tagModule.setPid(tag.getPid());
            tagModule.setName(tag.getName());
            tagRepository.save(tagModule);
        }
        tagList = null;


        LambdaQueryWrapper<Category> categoryLambdaQueryWrapper = new LambdaQueryWrapper<>();
        categoryLambdaQueryWrapper.eq(Category::getIsDelete, 0);
        List<Category> categoryList = categoryService.list(categoryLambdaQueryWrapper);

        for(Category category : categoryList) {
            CategoryModule duplicateModule = categoryRepository.findCategoryModuleByName(category.getName());
            if(duplicateModule != null) continue;

            CategoryModule categoryModule = new CategoryModule();

            categoryModule.setPid(category.getPid());
            categoryModule.setName(category.getName());
            categoryRepository.save(categoryModule);
        }

        return R.success("同步成功");
    }
}
