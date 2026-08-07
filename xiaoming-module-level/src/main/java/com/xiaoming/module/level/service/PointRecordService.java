package com.xiaoming.module.level.service;

import com.xiaoming.module.level.entity.PointRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaoming.module.level.dto.PointChangeDTO;

/**
 * 积分明细服务接口
 */
public interface PointRecordService extends IService<PointRecord> {

    /**
     * 记录积分变动
     */
    void recordPointChange(PointChangeDTO dto);

    /**
     * 查询用户积分明细列表
     */
    Object getPointRecords(Long userId, Integer pageNum, Integer pageSize);
}
