package com.xiaoming.module.note.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaoming.module.note.entity.Note;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * 笔记Mapper
 */
@Repository


public interface NoteMapper extends BaseMapper<Note> {
    @org.apache.ibatis.annotations.Select("SELECT * FROM note WHERE id = #{id} FOR UPDATE")
    Note lockIncludingDeleted(@Param("id") Long id);

    @org.apache.ibatis.annotations.Select("SELECT * FROM note WHERE id = #{id} AND is_deleted = 0 FOR UPDATE")
    Note lockById(@Param("id") Long id);


}
