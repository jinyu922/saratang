package com.swyp.saratang.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.swyp.saratang.config.JwtUtil;

@Component
public class JwtAuthUtil {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * ✅ JWT에서 토큰 추출 (Authorization 헤더, 쿼리 파라미터, 쿠키)
     */
    public String extractToken(HttpServletRequest request, String token, String queryToken) {
        if (token == null || !token.startsWith("Bearer ")) {
            if (queryToken != null) {
                token = "Bearer " + queryToken;
            }
        }

        if (token == null || !token.startsWith("Bearer ")) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("jwt".equals(cookie.getName())) {
                        token = "Bearer " + cookie.getValue();
                        break;
                    }
                }
            }
        }

        if (token == null || !token.startsWith("Bearer ")) {
            return null;
        }

        String jwtToken = token.substring(7);
        return jwtUtil.validateToken(jwtToken) ? jwtToken : null;
    }

    /**
     * ✅ JWT에서 사용자 ID 추출
     */
    public String extractUserId(String jwtToken) {
        return (jwtToken != null) ? jwtUtil.getClaims(jwtToken).getSubject() : null;
    }
}
