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

}
