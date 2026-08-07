package com.xiaoming.security.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;

public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;
    private Long userId;
    private String username;
    private String password;
    private String nickname;
    private String avatar;
    private Integer status;
    private Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(Long userId, String username, String password, String nickname,
                           String avatar, Integer status, Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId; this.username = username; this.password = password;
        this.nickname = nickname; this.avatar = avatar; this.status = status; this.authorities = authorities;
    }

    public static UserDetailsImpl build(Long userId, String username, String password, String nickname, String avatar, Integer status) {
        return new UserDetailsImpl(userId, username, password, nickname, avatar, status,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }

    public Long getUserId() { return userId; }
    public String getNickname() { return nickname; }
    public String getAvatar() { return avatar; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return username; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return status != null && status == 1; }
}
