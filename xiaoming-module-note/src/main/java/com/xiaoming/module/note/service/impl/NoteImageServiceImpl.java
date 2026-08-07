package com.xiaoming.module.note.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoming.module.note.entity.NoteImage;
import com.xiaoming.module.note.mapper.NoteImageMapper;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.IntStream;

/**
 * 笔记图片服务实现
 */
@Service
public class NoteImageServiceImpl extends ServiceImpl<NoteImageMapper, NoteImage> implements NoteImageService {

    @Override
    public void batchSave(Long noteId, List<String> imageUrls) {
        List<NoteImage> images = IntStream.range(0, imageUrls.size())
                .mapToObj(i -> {
                    NoteImage img = new NoteImage();
                    img.setNoteId(noteId);
                    img.setImageUrl(imageUrls.get(i));
                    img.setSort(i);
                    return img;
                })
                .toList();
        saveBatch(images);
    }
}
