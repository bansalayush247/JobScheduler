package com.merchant.jobscheduler.interceptor;

import com.merchant.jobscheduler.entity.User;
import com.merchant.jobscheduler.repository.UserRepository;
import com.merchant.jobscheduler.security.JwtTokenProvider;
import org.springframework.util.AntPathMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.UUID;

@Component
public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    private final JwtTokenProvider jwtProvider;
    private final UserRepository userRepository;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthenticationInterceptor(JwtTokenProvider jwtProvider,
                                        UserRepository userRepository) {
        this.jwtProvider = jwtProvider;
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        try {

            String path = request.getRequestURI();
            String method = request.getMethod();

            // Allow auth APIs
            if (path.startsWith("/api/auth")) {
                return true;
            }

            // Extract token
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Missing Authorization header");
                return false;
            }

            String token = authHeader.substring(7);

            if (!jwtProvider.validate(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid token");
                return false;
            }

            String userId = jwtProvider.getUserId(token);

            User user = userRepository
                    .findById(UUID.fromString(userId))
                    .orElse(null);

            if (user == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("User not found");
                return false;
            }

            String permittedApis = user.getRole().getPermittedApi();

            if (!isAllowed(permittedApis, path, method)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Access Denied");
                return false;
            }

            // Store authenticated user for controller/service use
            request.setAttribute("authenticatedUser", user);

            return true;

        } catch (Exception e) {

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Internal Server Error");

            return false;
        }
    }
    private boolean isAllowed(String permittedApis, String requestPath, String method) {

        if (permittedApis == null || permittedApis.isEmpty()) {
            return false;
        }

        String[] permissions = permittedApis.split(",");
        for (String permission : permissions) {

            String[] parts = permission.trim().split(":");

            if (parts.length != 2) continue;
            String apiPattern = parts[0].trim();
            String allowedMethod = parts[1].trim();
            boolean pathMatches = pathMatcher.match(apiPattern, requestPath);
            boolean methodMatches = allowedMethod.equalsIgnoreCase(method);

            if (pathMatches && methodMatches) {
                return true;
            }
        }

        return false;
    }
}