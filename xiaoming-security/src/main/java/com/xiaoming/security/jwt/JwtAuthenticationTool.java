package com.xiaoming.security.jwt;

import com.xiaoming.security.security.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtAuthenticationTool {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationTool(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public TokenPair generateTokenPair(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String accessToken = jwtTokenProvider.generateAccessToken(userDetails.getUsername(), userDetails.getUserId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails.getUsername(), userDetails.getUserId());
        return new TokenPair(accessToken, refreshToken);
    }

    public String refreshAccessToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("RefreshToken已过期或无效");
        }
        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        return jwtTokenProvider.generateAccessToken(username, userId);
    }

    public static class TokenPair {
        private String accessToken;
        private String refreshToken;

        public TokenPair(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }

        public String getAccessToken() { return accessToken; }
        public String getRefreshToken() { return refreshToken; }
    }
}
