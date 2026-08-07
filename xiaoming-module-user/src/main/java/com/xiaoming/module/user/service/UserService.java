package com.xiaoming.module.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaoming.module.user.dto.LoginDTO;
import com.xiaoming.module.user.dto.RegisterDTO;
import com.xiaoming.module.user.dto.UserUpdateDTO;
import com.xiaoming.module.user.entity.User;
import com.xiaoming.module.user.vo.LoginVO;
import com.xiaoming.module.user.vo.UserVO;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface UserService extends IService<User> {
    LoginVO register(RegisterDTO dto);
    LoginVO login(LoginDTO dto);
    String refreshAccessToken(String token);
    void logout(Long userId);
    void kickUser(Long userId);
    UserVO getUserInfo(Long userId);
    UserVO getUserInfoByUsername(String username);
    UserVO updateUserInfo(Long userId, UserUpdateDTO dto);
    String uploadAvatar(Long userId, MultipartFile file);
    List<UserVO> searchUsers(String keyword);
    List<UserVO> getUserInfoList(List<Long> userIds);
}
