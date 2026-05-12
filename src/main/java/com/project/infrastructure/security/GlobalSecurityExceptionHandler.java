package com.project.infrastructure.security;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

/**
 * Security kaynaklı hataları web ve API için tutarlı yanıtlar ile yönetir.
 */
@ControllerAdvice
public class GlobalSecurityExceptionHandler {

    @ExceptionHandler({ SecurityException.class, AccessDeniedException.class })
    public Object handleSecurityExceptions(Exception ex, WebRequest request) {
        if (isApiRequest(request)) {
            HttpStatus status = ex instanceof AccessDeniedException ? HttpStatus.FORBIDDEN : HttpStatus.UNAUTHORIZED;
            return ResponseEntity.status(status).body(Map.of("error", ex.getMessage()));
        }

        return "redirect:/auth/login";
    }

    private boolean isApiRequest(WebRequest request) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            String uri = servletWebRequest.getRequest().getRequestURI();
            return uri != null && uri.startsWith("/api/");
        }
        return false;
    }
}
