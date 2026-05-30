package com.trackit.config;

import com.trackit.service.JwtService;
import com.trackit.service.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtService jwtService;
    private final TokenBlacklistService blacklistService;

    @Autowired
    public JwtInterceptor(JwtService jwtService, TokenBlacklistService blacklistService) {
        this.jwtService = jwtService;
        this.blacklistService = blacklistService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS requests (CORS preflight) are allowed without checks
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"message\": \"Missing or invalid Authorization header.\"}");
            response.setContentType("application/json");
            return false;
        }

        String token = authHeader.substring(7).trim();

        // Check if token has been blacklisted (signed out)
        if (blacklistService.isBlacklisted(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"message\": \"Token has been invalidated via signout.\"}");
            response.setContentType("application/json");
            return false;
        }

        try {
            String username = jwtService.validateToken(token);
            // Attach username to request attributes for downstream use in controllers
            request.setAttribute("username", username);
            return true;
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"message\": \"Unauthorized: " + e.getMessage() + "\"}");
            response.setContentType("application/json");
            return false;
        }
    }
}
