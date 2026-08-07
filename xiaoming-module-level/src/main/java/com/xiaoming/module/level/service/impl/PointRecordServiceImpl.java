package com.xiaoming.module.level.service;

import com.xiaoming.module.level.entity.PointRecord;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoming.module.level.mapper.PointRecordMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 积分明细服务实现
 */
@Service
public class PointRecordServiceImpl extends ServiceImpl<PointRecordMapper, PointRecord> implements PointRecordService {

    @Override
    public void recordPointChange(PointChangeDTO dto) {
        PointRecord record = new PointRecord();
        record.setUserId(dto.getUserId());
        record.setType(dto.getType());
        record.setPoint(dto.getPoint());
        record.setAction(dto.getAction());
        record.setActionId(dto.getActionId());
        record.setDescription(dto.getDescription());
        record.setCreateTime(java.time.LocalDateTime.now());
        this.save(record);
    }

    @Override
    public Object getPointRecords(Long userId, Integer pageNum, Integer pageSize) {
        return null;
    }
}
