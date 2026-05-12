package com.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.project.infrastructure.security.SessionAuthenticationFilter;

import jakarta.servlet.http.HttpServletRequest;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, SessionAuthenticationFilter sessionAuthenticationFilter)
        throws Exception {
        http
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers("/api/**"))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/error", "/auth/login", "/api/auth/login").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                .requestMatchers("/users/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/inventory/list").hasAnyRole("CUSTOMER", "STAFF", "ADMIN")
                .requestMatchers("/analytics/**", "/inventory/**", "/facilities/**", "/settings/**")
                    .hasAnyRole("STAFF", "ADMIN")
                .requestMatchers("/api/inventory/**", "/api/cargo/**").hasAnyRole("STAFF", "ADMIN")
                .requestMatchers("/orders/manage/**").hasAnyRole("STAFF", "ADMIN")
                .requestMatchers("/api/orders/**", "/api/payments/**").authenticated()
                .requestMatchers("/orders/**", "/payment/**", "/profile/**", "/auth/logout", "/api/auth/logout")
                    .authenticated()
                .anyRequest().authenticated())
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authEx) -> handleUnauthorized(request, response))
                .accessDeniedHandler((request, response, deniedEx) -> handleForbidden(request, response)))
            .addFilterBefore(sessionAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private void handleUnauthorized(HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response)
        throws java.io.IOException {
        if (isApiRequest(request)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\":\"Authentication required\"}");
            return;
        }
        response.sendRedirect("/auth/login");
    }

    private void handleForbidden(HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response)
        throws java.io.IOException {
        if (isApiRequest(request)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\":\"Access denied\"}");
            return;
        }
        response.sendRedirect("/");
    }

    private boolean isApiRequest(HttpServletRequest request) {
        return request.getRequestURI() != null && request.getRequestURI().startsWith("/api/");
    }
}
