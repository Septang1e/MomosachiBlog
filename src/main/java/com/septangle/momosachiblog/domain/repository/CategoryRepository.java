package com.septangle.momosachiblog.domain.repository;

import com.septangle.momosachiblog.module.es.CategoryModule;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Component
public interface CategoryRepository extends ElasticsearchRepository<CategoryModule, String> {
    public List<CategoryModule> findCategoryModulesByNameLike(String name);
    public CategoryModule findCategoryModuleByName(String name);
}
