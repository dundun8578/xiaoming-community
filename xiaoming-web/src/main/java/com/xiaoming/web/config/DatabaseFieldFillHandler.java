package com.xiaoming.web.config;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
/** 与各实体的 INSERT / INSERT_UPDATE 标记配套，避免显式插入 NULL 覆盖数据库默认值。 */
@Component
public class DatabaseFieldFillHandler implements MetaObjectHandler {
    @Override public void insertFill(MetaObject object){
        LocalDateTime now=LocalDateTime.now();
        strictInsertFill(object,"createTime",LocalDateTime.class,now);
        strictInsertFill(object,"updateTime",LocalDateTime.class,now);
        if(object.hasSetter("isDeleted") && getFieldValByName("isDeleted",object)==null)
            setFieldValByName("isDeleted",0,object);
    }
    @Override public void updateFill(MetaObject object){
        if(object.hasSetter("updateTime"))setFieldValByName("updateTime",LocalDateTime.now(),object);
    }
}
