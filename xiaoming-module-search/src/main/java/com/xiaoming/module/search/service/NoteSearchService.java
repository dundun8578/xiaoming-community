package com.xiaoming.module.search.service;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.xiaoming.module.note.entity.Note;
import com.xiaoming.module.search.document.NoteDocument;
import com.xiaoming.module.search.repository.NoteSearchRepository;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ES搜索服务
 */
@Service
public class NoteSearchService {

    /**
     * Spring Data 的 ES 仓库在实例化时会去连接 ES 建索引，ES 不可用时会直接导致启动失败。
     * 搜索属于增强能力，不应该拦住应用启动，所以这里延迟加载，真正用到时才初始化。
     */
    @Lazy
    @Autowired
    private NoteSearchRepository noteSearchRepository;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    public void saveNoteToEs(Note note) {
        NoteDocument doc = convertToDocument(note);
        noteSearchRepository.save(doc);
    }

    public void deleteNoteFromEs(Long noteId) {
        noteSearchRepository.deleteById(noteId);
    }

    public Page<NoteDocument> searchNotes(String keyword, PageRequest pageRequest) {
        try {
            int from = pageRequest.getPageNumber() * pageRequest.getPageSize();

            Query titleQuery = Query.of(q -> q
                    .multiMatch(m -> m
                            .fields("title", "content")
                            .query(keyword)
                            .fuzziness("AUTO")
                    )
            );

            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index("note")
                    .query(titleQuery)
                    .from(from)
                    .size(pageRequest.getPageSize())
            );

            SearchResponse<NoteDocument> response = elasticsearchClient.search(
                    searchRequest, NoteDocument.class
            );

            List<NoteDocument> hits = response.hits().hits().stream()
                    .map(hit -> hit.source())
                    .collect(Collectors.toList());

            return new PageImpl<>(hits, pageRequest, response.hits().total().value());
        } catch (Exception e) {
            return Page.empty();
        }
    }

    private NoteDocument convertToDocument(Note note) {
        NoteDocument doc = new NoteDocument();
        doc.setId(note.getId());
        doc.setTitle(note.getTitle());
        doc.setContent(note.getContent());
        doc.setTags(note.getTags());
        doc.setUserId(note.getUserId());
        doc.setType(note.getType());
        doc.setStatus(note.getStatus());
        doc.setLikeCount(note.getLikeCount() != null ? note.getLikeCount().longValue() : 0L);
        doc.setCollectCount(note.getCollectCount() != null ? note.getCollectCount().longValue() : 0L);
        doc.setCommentCount(note.getCommentCount() != null ? note.getCommentCount().longValue() : 0L);
        doc.setHotScore(note.getHotScore());
        doc.setPublishTime(note.getPublishTime());
        return doc;
    }
}
