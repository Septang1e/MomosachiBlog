package com.septangle.momosachiblog.domain.repository;

import com.septangle.momosachiblog.module.es.ArticleModule;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Component
public interface ArticleRepository extends ElasticsearchRepository<ArticleModule, String> {
    List<ArticleModule> findArticleModulesByTitleLike(String title);
    ArticleModule findArticleModuleByTitle(String title);
}