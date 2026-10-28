package com.xiaoming.module.search.service;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.xiaoming.common.exception.BusinessException;
import com.xiaoming.common.constant.ResultCode;
import com.xiaoming.module.note.entity.Note;
import com.xiaoming.module.user.service.UserService;
import com.xiaoming.module.user.vo.UserVO;
import com.xiaoming.module.search.document.NoteDocument;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
@Service
public class NoteSearchService {
    private final ElasticsearchClient client;
    private final UserService users;
    private final String analyzer;
    private volatile boolean ready;
    public NoteSearchService(ElasticsearchClient client,UserService users,
            @Value("${search.analyzer:standard}") String analyzer) {
        this.client=client;this.users=users;this.analyzer=analyzer;
    }
    private synchronized void ensureIndex() throws IOException {
        if(ready)return;
        if(!client.indices().exists(e->e.index("note")).value()) {
            try {
                client.indices().create(c->c.index("note").mappings(m->m
                    .properties("id",p->p.long_(l->l))
                    .properties("userId",p->p.long_(l->l))
                    .properties("title",p->p.text(t->t.analyzer(analyzer)))
                    .properties("content",p->p.text(t->t.analyzer(analyzer)))
                    .properties("tags",p->p.keyword(k->k))
                    .properties("status",p->p.integer(i->i))
                    .properties("publishTime",p->p.date(d->d.format("yyyy-MM-dd HH:mm:ss||strict_date_optional_time||epoch_millis")))));
            } catch(co.elastic.clients.elasticsearch._types.ElasticsearchException e) {
                if(!"resource_already_exists_exception".equals(e.error().type()))throw e;
            }
        }
        ready=true;
    }
    public void saveNoteToEs(Note note) {
        try {
            ensureIndex();
            if(!Integer.valueOf(1).equals(note.getStatus()) || Integer.valueOf(1).equals(note.getIsDeleted())){
                deleteNoteFromEs(note.getId());return;
            }
            NoteDocument doc=new NoteDocument();BeanUtils.copyProperties(note,doc);
            doc.setLikeCount(note.getLikeCount().longValue());doc.setCollectCount(note.getCollectCount().longValue());
            doc.setCommentCount(note.getCommentCount().longValue());
            UserVO user=users.getUserInfo(note.getUserId());
            if(user!=null)doc.setAuthorName(user.getNickname());
            client.index(i->i.index("note").id(note.getId().toString()).document(doc));
        }catch(IOException e){throw new IllegalStateException("ES 索引写入失败",e);}
    }
    public void deleteNoteFromEs(Long id) {
        try {ensureIndex();client.delete(d->d.index("note").id(id.toString()));}
        catch(IOException e){throw new IllegalStateException("ES 索引删除失败",e);}
    }
    public Page<NoteDocument> searchNotes(String keyword,PageRequest page) {
        if(keyword==null || keyword.isBlank())return Page.empty(page);
        try {
            ensureIndex();
            SearchResponse<NoteDocument> result=client.search(s->s.index("note")
                .from(Math.toIntExact(page.getOffset())).size(page.getPageSize())
                .trackTotalHits(t->t.enabled(true))
                .query(q->q.bool(b->b
                    .filter(f->f.term(t->t.field("status").value(1)))
                    .must(m->m.multiMatch(mm->mm.fields("title^2","content","tags")
                        .query(keyword.trim()).operator(Operator.And))))),NoteDocument.class);
            List<NoteDocument> hits=result.hits().hits().stream().map(h->h.source())
                .filter(java.util.Objects::nonNull).collect(Collectors.toList());
            return new PageImpl<>(hits,page,result.hits().total()==null?hits.size():result.hits().total().value());
        }catch(Exception e){
            throw new BusinessException(ResultCode.FAILED,"搜索服务暂不可用，请稍后重试");
        }
    }
}
