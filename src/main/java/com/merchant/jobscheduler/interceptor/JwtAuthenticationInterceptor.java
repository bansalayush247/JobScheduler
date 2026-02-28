package com.merchant.jobscheduler.interceptor;

import com.merchant.jobscheduler.entity.User;
import com.merchant.jobscheduler.repository.UserRepository;
import com.merchant.jobscheduler.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    private final JwtTokenProvider jwtProvider;
    private final UserRepository userRepository;

    public JwtAuthenticationInterceptor(JwtTokenProvider jwtProvider,
                                        UserRepository userRepository) {
        this.jwtProvider = jwtProvider;
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String path = request.getRequestURI();   // ✅ MUST BE HERE

        // Allow public APIs
        if (path.startsWith("/api/auth")) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Missing or invalid Authorization header");
            return false;
        }

        String token = authHeader.substring(7);

        if (!jwtProvider.validate(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid or expired token");
            return false;
        }

        String userId = jwtProvider.getUserId(token);

        User user = userRepository.findById(UUID.fromString(userId))
                .orElse(null);

        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("User not found");
            return false;
        }

        // ✅ Role-based restriction (Admin APIs)
        if (path.startsWith("/api/admin")
                && !user.getRole().getName().equals("ADMIN")) {

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Access Denied\"}");
            return false;
        }

        // Store user in request for later use
        request.setAttribute("authenticatedUser", user);

        return true;
    }
}