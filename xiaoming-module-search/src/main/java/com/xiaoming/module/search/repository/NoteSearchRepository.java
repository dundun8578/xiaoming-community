package com.xiaoming.module.search.repository;

import com.xiaoming.module.search.document.NoteDocument;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * 笔记ES Repository
 *
 * 加 @Lazy 让仓库延迟初始化：Spring Data 的 ES 仓库在实例化时会去连 ES 建索引，
 * ES 没启动时会直接让应用启动失败。搜索是增强能力，不应阻塞主流程。
 */
@Lazy
public interface NoteSearchRepository extends ElasticsearchRepository<NoteDocument, Long> {
}
