package com.septangle.momosachiblog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mysql.cj.util.StringUtils;
import com.septangle.momosachiblog.domain.R;
import com.septangle.momosachiblog.domain.dto.TagDTO;
import com.septangle.momosachiblog.domain.entity.Article;
import com.septangle.momosachiblog.domain.entity.ArticleTag;
import com.septangle.momosachiblog.domain.entity.Category;
import com.septangle.momosachiblog.domain.entity.Tag;
import com.septangle.momosachiblog.domain.repository.TagRepository;
import com.septangle.momosachiblog.module.es.TagModule;
import com.septangle.momosachiblog.service.ArticleTagService;
import com.septangle.momosachiblog.service.TagService;
import com.septangle.momosachiblog.utils.Generator;
import com.septangle.momosachiblog.utils.PaginationUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/")
public class TagController {
    @Autowired
    private TagService tagService;
    @Autowired
    private ArticleTagService articleTagService;
    @Autowired
    private TagRepository tagRepository;

    /**
     * 获取所有标签的api
     * @return
     */
    @GetMapping("/api/tags")
    public R<List<TagDTO>> getTags(){
        //获取所有的tag
        LambdaQueryWrapper<Tag>tagLambdaQueryWrapper = new LambdaQueryWrapper<>();
        tagLambdaQueryWrapper.eq(Tag::getStatus,0);
        List<Tag>allTags = tagService.list(tagLambdaQueryWrapper);

        //处理每个tag的包含的文章数
        List<TagDTO>result = new ArrayList<>();
        for(Tag tag : allTags){
            LambdaQueryWrapper<ArticleTag>articleTagLambdaQueryWrapper=new LambdaQueryWrapper<>();
            articleTagLambdaQueryWrapper.eq(ArticleTag::getTagId,tag.getId());

            result.add(new TagDTO(tag,articleTagService.count(articleTagLambdaQueryWrapper)));
        }

        return R.success(result);
    }

    @GetMapping("/api/admin/tag/pagination/{current}/{size}")
    public R<Page<Tag>> tagPagination(@PathVariable int current, @PathVariable int size,
                                      @RequestParam(required = false) String keyword, @RequestParam(required = false) String order) {

        Page<Tag> result;
        if(Objects.nonNull(keyword)) {
            result = pagination(keyword, current, size, order, 0);
        }else{
            result = pagination(current, size, order, 0);
        }

        return R.success(result);
    }

    @PostMapping("/api/admin/tag/save")
    public R<String> save(@RequestBody Tag tag){

        if(tag == null) {
            return R.error("标签不存在");
        }

        Tag duplicateTagChecker = tagService.getByName(tag.getName());
        if(duplicateTagChecker != null) {
            return R.error("名称为" + tag.getName() + "的标签已存在");
        }

        log.info("{}", tag);
        tag.setPid(Generator.pidGenerator());
        tagService.save(tag);
        return R.success("添加成功");
    }

    @PostMapping("/api/admin/tag/edit")
    public R<String> update(@RequestBody Tag tag) {
        if(tag == null) {
            return R.error("标签不存在");
        }
        tagService.updateById(tag);
        return R.success("更新tag成功");
    }

    @DeleteMapping("/api/admin/tag")
    public R<String> delete(@RequestBody Long []tagIdList) {

        for(Long id : tagIdList) {
            Tag tag = tagService.getById(id);
            if(Objects.isNull(tag)) {
                return R.error("ID为 " + id + " 的标签不存在");
            }
            tag.setIsDelete(1);
            tagService.updateById(tag);
        }
        return R.success("删除成功");
    }

    @PostMapping("/api/admin/tag/status")
    public R<String> tagStatusHandler(@RequestBody Long []tagIdList) {

        for(Long id : tagIdList) {
            Tag tag = tagService.getById(id);
            if(tag == null){
                return R.error("ID为 " + id + " 的tag不存在");
            }
            tag.setStatus(tag.getStatus() == 1 ? 0 : 1);
            tagService.updateById(tag);
        }
        return R.success("更改成功");
    }

    @DeleteMapping("/api/admin/bin/tag")
    public R<String> removeFromDataBase(@RequestBody Long []idList) {

        for(Long id : idList) {
            Tag tag = tagService.getById(id);
            if(Objects.isNull(tag)) {
                return R.error("id为 " + id + " 的分类不存在");
            }

            LambdaQueryWrapper<ArticleTag> articleTagLambdaQueryWrapper = new LambdaQueryWrapper<>();
            articleTagLambdaQueryWrapper
                    .eq(ArticleTag::getTagId, id);

            articleTagService.remove(articleTagLambdaQueryWrapper);

            tagService.removeById(tag);
        }

        return R.success("删除成功");
    }

    @PostMapping("/api/admin/bin/tag")
    public R<String> recycle(@RequestBody Long []idList) {


        for(Long id : idList) {
            Tag tag = tagService.getById(id);
            if(Objects.isNull(tag)) {
                return R.error("id为 " + id + " 的分类不存在");
            }
            tag.setIsDelete(0);

            tagService.updateById(tag);
        }

        return R.success("恢复成功");
    }

    @GetMapping("/api/admin/bin/tag/pagination/{current}/{size}")
    public R<Page<Tag>> tagBinPagination(@PathVariable int current, @PathVariable int size, @RequestParam(required = false) String keyword) {

        if(StringUtils.isNullOrEmpty(keyword)) {
            return R.success(pagination(current, size, "",1));
        }else{
            return R.success(pagination(keyword, current, size, "", 1));
        }
    }

    /**
     *
     * 存在 keyword的分页条件
     * @param keyword 关键字
     * @param current 当前页数
     * @param size 每页数量
     * @param order 排序方式
     * @return Page
     */
    private Page<Tag> pagination(String keyword, int current, int size, String order, int status) {

        Page<Tag> result = new Page<>();
        List<Tag> records = new ArrayList<>();

        List<TagModule> searchResult = tagRepository.findTagModulesByNameLike(keyword);

        log.info("searchResult is {}, keyword is {}", searchResult, keyword);

        records = searchResult.stream()
                .map(tagModule -> {
                    LambdaQueryWrapper<Tag> tagLambdaQueryWrapper = new LambdaQueryWrapper<>();
                    tagLambdaQueryWrapper
                            .eq(Tag::getIsDelete, status)
                            .eq(Tag::getPid, tagModule.getPid());
                    return tagService.getOne(tagLambdaQueryWrapper);
                })
                .filter(Objects::nonNull)
                .filter(tag -> tag.getIsDelete() == 0)
                .sorted(Comparator.comparing(Tag::getCreateTime))
                .collect(Collectors.toList());


        result = PaginationUtils.getByRecords(records, current, size);
        log.info("result is {}", records);

        return result;
    }
    //没有keyword的分页条件
    private Page<Tag> pagination(int current, int size, String order, int status) {

        Page<Tag> result = new Page<>(current, size);

        LambdaQueryWrapper<Tag> tagLambdaQueryWrapper = new LambdaQueryWrapper<>();
        tagLambdaQueryWrapper
                .eq(Tag::getIsDelete, status)
                .orderByDesc(Tag::getCreateTime);

        tagService.page(result, tagLambdaQueryWrapper);
        return result;
    }
}
