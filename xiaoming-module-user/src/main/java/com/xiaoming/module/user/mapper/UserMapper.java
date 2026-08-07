package com.xiaoming.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaoming.module.user.entity.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMapper extends BaseMapper<User> {
    default User findByUsername(String username) {
        return selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
        );
    }
}
