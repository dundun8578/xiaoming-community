package com.xiaoming.module.search.repository;

import com.xiaoming.module.search.document.NoteDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * 笔记ES Repository
 */
public interface NoteSearchRepository extends ElasticsearchRepository<NoteDocument, Long> {
}
