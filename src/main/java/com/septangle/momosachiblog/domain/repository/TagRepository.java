package com.septangle.momosachiblog.domain.repository;

import com.septangle.momosachiblog.module.es.TagModule;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Component
public interface TagRepository extends ElasticsearchRepository<TagModule, String> {

    List<TagModule> findTagModulesByNameLike(String name);

    TagModule findTagModuleByName(String name);
    void deleteTagModuleById(String id);

}