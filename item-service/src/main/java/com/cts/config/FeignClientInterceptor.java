package com.cts.config;

import com.cts.security.SystemTokenProvider;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.Target;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@RequiredArgsConstructor
@Slf4j
public class FeignClientInterceptor implements RequestInterceptor {

    private final SystemTokenProvider systemTokenProvider;

    @Override
    public void apply(RequestTemplate template) {

        // 1. Identify which service we are calling
        Target<?> target = template.feignTarget();
        String targetName = (target != null) ? target.name() : "unknown";

        // 2. Decide Strategy
        if ("inventory-service".equals(targetName)) {
            // Use the System Token
            String systemToken = systemTokenProvider.generateSystemToken();
            template.header("Authorization", "Bearer " + systemToken);
            template.header("X-User-Name", "system-item-service");
            template.header("X-User-Roles", "SYSTEM");
            log.info("Injected System Token for call to {}", targetName);

        } else {
            // Pass the original token
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String token = request.getHeader("Authorization");
                String username = request.getHeader("X-User-Name");

                if (token != null) template.header("Authorization", token);
                if (username != null) template.header("X-User-Name", username);
            }
        }
    }
}