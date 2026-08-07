package com.xiaoming.module.search.controller;

import com.xiaoming.common.page.PageRequest;
import com.xiaoming.common.result.Result;
import com.xiaoming.module.search.document.NoteDocument;
import com.xiaoming.module.search.service.NoteSearchService;
import io.swagger.v3.annotations.Api;
import io.swagger.v3.annotations.ApiOperation;
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
{tags = "搜索模块")
public class SearchController {

    @Autowired
    private NoteSearchService noteSearchService;

    @GetMapping("/note")
    public Result<List<NoteDocument>> searchNotes(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize);
        Page<NoteDocument> page = noteSearchService.searchNotes(keyword, pageRequest);
        return Result.ok(page.getContent());
    }
}
