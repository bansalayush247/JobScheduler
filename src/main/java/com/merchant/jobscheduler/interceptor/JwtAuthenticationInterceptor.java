package com.merchant.jobscheduler.interceptor;

import com.merchant.jobscheduler.entity.User;
import com.merchant.jobscheduler.repository.UserRepository;
import com.merchant.jobscheduler.security.JwtTokenProvider;
import com.merchant.jobscheduler.exception.ErrorCodes;

import org.springframework.util.AntPathMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    private final JwtTokenProvider jwtProvider;
    private final UserRepository userRepository;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthenticationInterceptor(JwtTokenProvider jwtProvider, UserRepository userRepository) {
        this.jwtProvider = jwtProvider;
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        try {

            String path = request.getRequestURI();
            String method = request.getMethod();

            // Extract token
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Missing Authorization header for path: {}", path);
                writeError(response, HttpServletResponse.SC_UNAUTHORIZED, ErrorCodes.INVALID_CREDENTIALS);
                return false;
            }

            String token = authHeader.substring(7);

            if (!jwtProvider.validate(token)) {
                log.warn("Invalid token for path: {}", path);
                writeError(response, HttpServletResponse.SC_UNAUTHORIZED, ErrorCodes.INVALID_CREDENTIALS);
                return false;
            }

            String userId = jwtProvider.getUserId(token);

            User user = userRepository.findById(UUID.fromString(userId)).orElse(null);

            if (user == null) {
                log.warn("User not found for token. ID: {}", userId);
                writeError(response, HttpServletResponse.SC_UNAUTHORIZED, ErrorCodes.INVALID_CREDENTIALS);
                return false;
            }

            String permittedApis = user.getRole().getPermittedApi();

            if (!isAllowed(permittedApis, path, method)) {
                log.warn("Access denied for user {} on {} {}", userId, method, path);
                writeError(response, HttpServletResponse.SC_FORBIDDEN, ErrorCodes.ADMIN_ROLE_CHANGE_NOT_ALLOWED);
                return false;
            }

            // Store authenticated user for controller/service use
            request.setAttribute("authenticatedUser", user);

            return true;

        } catch (Exception e) {
            log.error("Interceptor error", e);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ErrorCodes.INTERNAL_SERVER_ERROR);
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

    private void writeError(HttpServletResponse response, int status, ErrorCodes error) throws IOException {

        response.setStatus(status);
        response.setContentType("application/json");

        String json = String.format("{\"errorCode\":\"%s\",\"errorMessage\":\"%s\"}", error.getCode(), error.getMessage());

        response.getWriter().write(json);
    }
}