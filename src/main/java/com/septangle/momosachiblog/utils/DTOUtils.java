package com.septangle.momosachiblog.utils;

import com.septangle.momosachiblog.domain.dto.ArticleDTO;
import com.septangle.momosachiblog.domain.dto.ArticleUploadDTO;
import com.septangle.momosachiblog.domain.entity.Article;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class DTOUtils {

    public static ArticleDTO setArticleDTO(Article article){
        ArticleDTO articleDTO = new ArticleDTO();
        articleDTO.setDescription(article.getDescription());
        articleDTO.setPid(article.getPid());
        articleDTO.setViewCount(article.getViewCount());
        articleDTO.setTitle(article.getTitle());
        articleDTO.setContent(article.getContent());
        articleDTO.setCreateTime(article.getCreateTime());
        articleDTO.setUpdateTime(article.getUpdateTime());
        articleDTO.setIsComment(article.getIsComment());
        articleDTO.setThumbnail(article.getThumbnail());
        articleDTO.setLikeCount(article.getLikeCount());

        return articleDTO;
    }
    public static ArticleUploadDTO getArticleUploadDTOByArticle(Article article) {
        ArticleUploadDTO articleUploadDTO = new ArticleUploadDTO();


        articleUploadDTO.setCategoryId(article.getCategoryId());
        articleUploadDTO.setId(article.getId());
        articleUploadDTO.setTitle(article.getTitle());
        articleUploadDTO.setContent(article.getContent());
        articleUploadDTO.setStatus(article.getStatus());
        articleUploadDTO.setIsComment(article.getIsComment());
        articleUploadDTO.setThumbnail(article.getThumbnail());
        articleUploadDTO.setPid(article.getPid());
        articleUploadDTO.setDescription(article.getDescription());
        articleUploadDTO.setIsDelete(article.getIsDelete());

        return articleUploadDTO;
    }
    public static List<ArticleUploadDTO> getArticleUploadDTOByArticle(List<Article> articles) {

        List<ArticleUploadDTO> result = new ArrayList<>();

        for(Article article : articles) {
            result.add(getArticleUploadDTOByArticle(article));
        }

        return result;
    }
}
