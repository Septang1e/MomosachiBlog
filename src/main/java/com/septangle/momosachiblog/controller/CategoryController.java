package com.septangle.momosachiblog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.septangle.momosachiblog.domain.R;
import com.septangle.momosachiblog.domain.dto.CategoryDTO;
import com.septangle.momosachiblog.domain.entity.Article;
import com.septangle.momosachiblog.domain.entity.Category;
import com.septangle.momosachiblog.domain.repository.CategoryRepository;
import com.septangle.momosachiblog.module.es.CategoryModule;
import com.septangle.momosachiblog.service.ArticleService;
import com.septangle.momosachiblog.service.CategoryService;
import com.septangle.momosachiblog.utils.Generator;
import com.septangle.momosachiblog.utils.PaginationUtils;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ArticleService articleService;
    @Autowired
    private CategoryRepository categoryRepository;


    @PostMapping("/admin/category/save")
    public R<String> add(@RequestBody Category category){
        //检验该类别是否存在
        LambdaQueryWrapper<Category> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .eq(Category::getName,category.getName())
                .eq(Category::getIsDelete, 0);
        ;
        if(categoryService.getOne(lambdaQueryWrapper) != null){
            return R.error("该分类已存在");
        }

        category.setPid(Generator.pidGenerator());
        categoryService.save(category);
        return R.success("添加成功");
    }

    @PostMapping("/admin/category/edit")
    public R<String> update(@RequestBody Category category){
        if (category.getName().isEmpty()) {
            return R.error("分类名不能为空");
        }

        categoryService.updateById(category);
        return R.success("更新成功");
    }

    /**
     * 获取所有的category
     * @return
     */
    @GetMapping("/categories")
    public R<List<CategoryDTO>>getCategory(){
        List<CategoryDTO> result = new ArrayList<>();
        //选择所以未被删除的category
        LambdaQueryWrapper<Category>queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getStatus,0)
                .eq(Category::getIsDelete, 0);

        //获取所有的category
        List<Category>categories = categoryService.list(queryWrapper);
        for(Category category:categories){
            CategoryDTO dto = new CategoryDTO(category, categoryService.articleCount(category.getId()));
            result.add(dto);
        }
        result.sort((a,b)-> Math.toIntExact(b.getArticleCount() - a.getArticleCount()));
        return R.success(result);
    }

    /**
     *
     * @param current 当前页数
     * @param size 每页数量
     * @param keyword 查询时的关键字
     * @return 分页结果
     */
    @GetMapping("/admin/category/pagination/{current}/{size}")
    public R<Page<Category>> categoryPagination(@PathVariable int current, @PathVariable int size, @RequestParam(required = false) String keyword) {
        Page<Category> result;
        if(Objects.isNull(keyword)) {
            result = pagination(current, size, 0);
        }else{
            result = pagination(current, size, keyword, 0);
        }

        return R.success(result);
    }

    @DeleteMapping("/admin/category")
    public R<String> delete(@RequestBody Long[] idList) {
        for(Long id : idList) {
            Category category = categoryService.getById(id);
            if(category == null) {
                return R.error("id为" + id + "的标签不存在");
            }
            category.setIsDelete(1);
            categoryService.updateById(category);
        }
        return R.success("删除成功");
    }

    @PostMapping("/admin/category/status")
    public R<String> statusHandler(@RequestBody Long[] idList) {
        for(Long id : idList) {
            Category category = categoryService.getById(id);
            if(category == null) {
                return R.error("id为" + id + "的标签不存在");
            }
            category.setStatus(1);
            categoryService.updateById(category);
        }
        return R.success("更新成功");
    }

    @DeleteMapping("/admin/bin/category")
    public R<String> removeFromDataBase(@RequestBody Long []idList) {

        for(Long id : idList) {
            Category category = categoryService.getById(id);
            if(Objects.isNull(category)) {
                return R.error("id为 " + id + " 的分类不存在");
            }
            categoryService.removeById(category);
        }

        return R.success("删除成功");
    }

    @PostMapping("/admin/bin/category")
    public R<String> recycle(@RequestBody Long []idList) {

        for(Long id : idList) {
            Category category = categoryService.getById(id);
            if(Objects.isNull(category)) {
                return R.error("id为 " + id + " 的分类不存在");
            }
            category.setIsDelete(0);
            categoryService.updateById(category);
        }
        return R.success("恢复成功");
    }

    @GetMapping("/admin/bin/category/pagination/{current}/{size}")
    public R<Page<Category>> categoryBinPagination(@PathVariable int current, @PathVariable int size, @RequestParam(required = false) String keyword) {
        Page<Category> result;

        if(Objects.isNull(keyword)) {
            result = pagination(current, size, 1);
        }else{
            result = pagination(current, size, keyword, 1);
        }

        return R.success(result);

    }


    /**
     * 获取属于此分类所以文章的数目
     * @param category 分类
     * @return 分类数量
     */
    private Long count(Category category){
        LambdaQueryWrapper<Article>queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Article::getCategoryId,category.getId()).eq(Article::getStatus,0);
        return articleService.count(queryWrapper);
    }


    /**
     *
     * @param current 当前页数
     * @param size 每页数量
     * @param keyword 关键词
     * @return 分页结果
     */
    private Page<Category> pagination(int current, int size, @NonNull String keyword, int status) {

        List<CategoryModule> categoryModules = categoryRepository.findCategoryModulesByNameLike(keyword);
        List<Category> record = categoryModules.stream()
                .map(categoryModule -> {
                    LambdaQueryWrapper<Category> categoryLambdaQueryWrapper = new LambdaQueryWrapper<>();
                    categoryLambdaQueryWrapper
                            .eq(Category::getIsDelete, status)
                            .eq(Category::getPid, categoryModule.getPid());
                    return categoryService.getOne(categoryLambdaQueryWrapper);
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Category::getUpdateTime))
                .toList();

        return PaginationUtils.getByRecords(record, current, size);
    }
    private Page<Category> pagination(int current, int size, int status) {

        LambdaQueryWrapper<Category> categoryLambdaQueryWrapper = new LambdaQueryWrapper<>();
        categoryLambdaQueryWrapper
                .eq(Category::getIsDelete, status)
                .orderByDesc(Category::getUpdateTime);
        Page<Category> page = new Page<>(current, size);
        categoryService.page(page, categoryLambdaQueryWrapper);

        return page;
    }
}
