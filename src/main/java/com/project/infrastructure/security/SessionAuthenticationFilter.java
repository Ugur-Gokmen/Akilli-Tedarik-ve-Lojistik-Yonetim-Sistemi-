package com.project.infrastructure.security;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.project.domain.user.User;
import com.project.ui.SessionManager;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * SessionManager'daki kullanıcıyı Spring Security context'ine taşır.
 */
@Component
public class SessionAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private SessionManager sessionManager;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (sessionManager != null && sessionManager.isLoggedIn()) {
            User currentUser = sessionManager.getCurrentUser();
            if (currentUser != null) {
                var authority = new SimpleGrantedAuthority("ROLE_" + currentUser.getRole().name());
                var authentication = new UsernamePasswordAuthenticationToken(
                    currentUser.getUsername(),
                    null,
                    List.of(authority));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } else {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
