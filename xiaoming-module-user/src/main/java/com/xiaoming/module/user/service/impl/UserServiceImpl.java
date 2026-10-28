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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    /** Redis 中缓存用户信息的 Hash key 前缀 */
    private static final String USER_INFO_KEY_PREFIX = "user:info:";

    /** 用户信息缓存基础过期时间（秒） */
    private static final long USER_INFO_TTL = 1800;

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
        if (!jwtTokenProvider.validateToken(token) || !jwtTokenProvider.hasTokenType(token,"refresh")) {
            throw new BusinessException(GlobalExceptionEnum.TOKEN_INVALID);
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        String cached = redisTemplate.opsForValue().get(LOGIN_TOKEN_PREFIX + userId);
        if (cached == null || !cached.equals(token)) {
            // Redis 里没有或对不上，说明已退出登录 / 被踢下线
            throw new BusinessException(GlobalExceptionEnum.TOKEN_INVALID);
        }


        User user=getById(userId);
        if(user==null || !Integer.valueOf(STATUS_NORMAL).equals(user.getStatus()))
            throw new BusinessException(GlobalExceptionEnum.USER_DISABLED);
        return jwtTokenProvider.generateAccessToken(user.getUsername(), userId);
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
        String key = USER_INFO_KEY_PREFIX + userId;

        // 1. 先读 Redis Hash 缓存（Redis 不可用会自动返回 null 降级到查库）
        UserVO cached = readUserInfoFromCache(key);
        if (cached != null) {
            return cached;
        }

        // 2. 未命中：查数据库
        UserVO vo = toVO(getById(userId));
        if (vo == null) {
            return null;
        }

        // 3. 回写缓存
        writeUserInfoToCache(key, vo);
        return vo;
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
        // 资料变更 → 缓存必须失效，否则会一直读到旧的昵称/简介（Cache Aside：先更库，再删缓存）
        evictUserInfoCache(userId);
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
        // 头像变更 → 缓存失效
        evictUserInfoCache(userId);
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

    // ------------------------------------------------------------------ 用户信息缓存

    /**
     * 从 Redis Hash 读用户信息。任何异常（Redis 连不上、数据异常）都返回 null，
     * 让调用方降级去查库 —— 缓存是加速手段，不能拖垮主流程。
     *
     * 注意：只缓存「与访问者无关」的字段。isFollowed 这种因人而异的字段不缓存，
     * 否则 A 的「已关注」状态会被 B 读到。
     */
    private UserVO readUserInfoFromCache(String key) {
        try {
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
            if (entries == null || entries.isEmpty()) {
                return null;
            }
            UserVO vo = new UserVO();
            vo.setId(parseLong(entries.get("id")));
            vo.setUsername(asString(entries.get("username")));
            vo.setNickname(asString(entries.get("nickname")));
            vo.setAvatar(asString(entries.get("avatar")));
            vo.setDescription(asString(entries.get("description")));
            vo.setFollowerCount(parseLong(entries.get("followerCount")));
            vo.setFollowingCount(parseLong(entries.get("followingCount")));
            return vo;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 把用户信息写入 Redis Hash 并设置过期时间。
     * TTL 加 0~60 秒随机抖动，避免大量 key 同时过期（缓存雪崩）。
     */
    private void writeUserInfoToCache(String key, UserVO vo) {
        try {
            Map<String, String> map = new HashMap<>();
            map.put("id", vo.getId() == null ? "" : String.valueOf(vo.getId()));
            map.put("username", vo.getUsername() == null ? "" : vo.getUsername());
            map.put("nickname", vo.getNickname() == null ? "" : vo.getNickname());
            map.put("avatar", vo.getAvatar() == null ? "" : vo.getAvatar());
            map.put("description", vo.getDescription() == null ? "" : vo.getDescription());
            map.put("followerCount", vo.getFollowerCount() == null ? "0" : String.valueOf(vo.getFollowerCount()));
            map.put("followingCount", vo.getFollowingCount() == null ? "0" : String.valueOf(vo.getFollowingCount()));

            redisTemplate.opsForHash().putAll(key, map);
            redisTemplate.expire(key, USER_INFO_TTL + (long) (Math.random() * 60), TimeUnit.SECONDS);
        } catch (Exception e) {
            // 缓存写失败不影响主流程，最坏情况只是这次没缓存上
        }
    }

    /**
     * 用户资料 / 头像变更时主动失效缓存
     */
    private void evictUserInfoCache(Long userId) {
        if (userId == null) {
            return;
        }
        try {
            redisTemplate.delete(USER_INFO_KEY_PREFIX + userId);
        } catch (Exception e) {
            // 删不掉不影响业务正确性，最多短期内读到旧缓存
        }
    }

    private String asString(Object o) {
        return o == null ? null : o.toString();
    }

    private Long parseLong(Object o) {
        if (o == null || "".equals(o.toString())) {
            return null;
        }
        try {
            return Long.valueOf(o.toString());
        } catch (NumberFormatException e) {
            return null;
        }
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
