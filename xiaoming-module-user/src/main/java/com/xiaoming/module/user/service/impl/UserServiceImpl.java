package com.xiaoming.module.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoming.common.exception.BusinessException;
import com.xiaoming.common.exception.GlobalExceptionEnum;
import com.xiaoming.module.minio.service.MinioService;
import com.xiaoming.module.user.dto.LoginDTO;
import com.xiaoming.module.user.dto.RegisterDTO;
import com.xiaoming.module.user.dto.UserUpdateDTO;
import com.xiaoming.module.user.entity.User;
import com.xiaoming.module.user.mapper.UserMapper;
import com.xiaoming.module.user.service.UserService;
import com.xiaoming.module.user.vo.LoginVO;
import com.xiaoming.module.user.vo.UserVO;
import com.xiaoming.security.jwt.JwtTokenProvider;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 *
 * 登录态方案：JWT 双 Token + Redis 分布式 Session
 *   - accessToken 由客户端携带，无状态校验（不过 Redis）
 *   - refreshToken 同时写入 Redis（key = login:token:{userId}），
 *     这样「退出登录 / 踢人下线」只要删掉这个 key，旧 refreshToken 就立刻失效
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /** Redis 中保存 refreshToken 的 key 前缀 */
    private static final String LOGIN_TOKEN_PREFIX = "login:token:";

    /** 用户状态：1 正常、0 禁用 */
    private static final int STATUS_NORMAL = 1;
    private static final int STATUS_DISABLED = 0;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private MinioService minioService;

    @Value("${jwt.refresh-token-validity}")
    private Long refreshTokenValidity;

    // ------------------------------------------------------------------ 注册 / 登录

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO register(RegisterDTO dto) {
        if (baseMapper.findByUsername(dto.getUsername()) != null) {
            throw new BusinessException(GlobalExceptionEnum.USER_ALREADY_EXIST);
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(isBlank(dto.getNickname()) ? dto.getUsername() : dto.getNickname());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setStatus(STATUS_NORMAL);
        save(user);

        return buildLoginVO(user);
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        User user = baseMapper.findByUsername(dto.getUsername());
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(GlobalExceptionEnum.USERNAME_OR_PASSWORD_ERROR);
        }
        if (user.getStatus() != null && user.getStatus() == STATUS_DISABLED) {
            throw new BusinessException(GlobalExceptionEnum.USER_DISABLED);
        }
        return buildLoginVO(user);
    }

    @Override
    public String refreshAccessToken(String refreshToken) {
        String token = jwtTokenProvider.resolveToken(refreshToken);
        if (isBlank(token)) {
            token = refreshToken;
        }
        if (!jwtTokenProvider.validateToken(token)) {
            throw new BusinessException(GlobalExceptionEnum.TOKEN_INVALID);
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        String cached = redisTemplate.opsForValue().get(LOGIN_TOKEN_PREFIX + userId);
        if (cached == null || !cached.equals(token)) {
            // Redis 里没有或对不上，说明已退出登录 / 被踢下线
            throw new BusinessException(GlobalExceptionEnum.TOKEN_INVALID);
        }

        return jwtTokenProvider.generateAccessToken(jwtTokenProvider.getUsernameFromToken(token), userId);
    }

    @Override
    public void logout(Long userId) {
        clearLoginSession(userId);
    }

    @Override
    public void kickUser(Long userId) {
        // 管理员操作：效果与退出登录一致，删掉 Redis 登录态即可强制下线
        clearLoginSession(userId);
    }

    // ------------------------------------------------------------------ 用户信息

    @Override
    public UserVO getUserInfo(Long userId) {
        if (userId == null) {
            return null;
        }
        return toVO(getById(userId));
    }

    @Override
    public UserVO getUserInfoByUsername(String username) {
        return toVO(baseMapper.findByUsername(username));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO updateUserInfo(Long userId, UserUpdateDTO dto) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(GlobalExceptionEnum.NOT_FOUND);
        }
        if (!isBlank(dto.getNickname())) {
            user.setNickname(dto.getNickname());
        }
        if (dto.getDescription() != null) {
            user.setDescription(dto.getDescription());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        updateById(user);
        return toVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String uploadAvatar(Long userId, MultipartFile file) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(GlobalExceptionEnum.NOT_FOUND);
        }
        String url = minioService.uploadAvatar(file);
        user.setAvatar(url);
        updateById(user);
        return url;
    }

    @Override
    public List<UserVO> searchUsers(String keyword) {
        if (isBlank(keyword)) {
            return Collections.emptyList();
        }
        String kw = keyword.trim();
        List<User> users = list(new LambdaQueryWrapper<User>()
                .and(w -> w.like(User::getUsername, kw).or().like(User::getNickname, kw))
                .orderByDesc(User::getId)
                .last("LIMIT 20"));
        return users.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public List<UserVO> getUserInfoList(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }
        return listByIds(userIds).stream().map(this::toVO).collect(Collectors.toList());
    }

    // ------------------------------------------------------------------ 私有方法

    /**
     * 生成双 Token 并把 refreshToken 写入 Redis
     */
    private LoginVO buildLoginVO(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername(), user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername(), user.getId());

        redisTemplate.opsForValue().set(
                LOGIN_TOKEN_PREFIX + user.getId(),
                refreshToken,
                refreshTokenValidity,
                TimeUnit.MILLISECONDS);

        LoginVO vo = new LoginVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setAccessToken(accessToken);
        vo.setRefreshToken(refreshToken);
        vo.setTokenType("Bearer");
        return vo;
    }

    private void clearLoginSession(Long userId) {
        if (userId != null) {
            redisTemplate.delete(LOGIN_TOKEN_PREFIX + userId);
        }
    }

    private UserVO toVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO vo = new UserVO();
        // 只拷贝同名同类型字段，password 不会出现在 UserVO 里
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
