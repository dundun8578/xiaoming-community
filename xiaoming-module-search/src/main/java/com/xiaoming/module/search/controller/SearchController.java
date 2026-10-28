package com.xiaoming.module.search.controller;

import com.xiaoming.common.result.Result;
import com.xiaoming.module.search.document.NoteDocument;
import com.xiaoming.module.search.service.NoteSearchService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * ES搜索控制器
 */
@RestController
@RequestMapping("/search")
@Tag(name = "搜索模块")
public class SearchController {

    @Autowired
    private NoteSearchService noteSearchService;

    @GetMapping("/note")
    public Result<List<NoteDocument>> searchNotes(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        if(pageNum<1 || pageSize<1 || pageSize>100 || (long)(pageNum-1)*pageSize>9900)
            throw new IllegalArgumentException("分页参数超出范围");
        PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize);
        Page<NoteDocument> page = noteSearchService.searchNotes(keyword, pageRequest);
        return Result.ok(page.getContent());
    }
}
