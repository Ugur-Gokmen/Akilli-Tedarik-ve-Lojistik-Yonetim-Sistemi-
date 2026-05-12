package com.project.infrastructure.aop;

import java.util.Arrays;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.project.domain.user.Role;
import com.project.domain.user.User;
import com.project.infrastructure.security.RequireRole;
import com.project.ui.SessionManager;

/**
 * Aspect: Güvenlik ve yetkilendirmeleri Proxy üzerinden yönetir.
 * 
 * <p>AOP sayesinde Controller ve Service metotlarına "@RequireRole" yazılması
 * yeterlidir. Kod içerisine guard çağrıları yapılmaz. İş mantığı temiz kalır.</p>
 */
@Aspect
@Component
public class SecurityAspect {

    private final SessionManager sessionManager;

    @Autowired
    public SecurityAspect(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Before("@annotation(requireRole)")
    public void authorize(JoinPoint joinPoint, RequireRole requireRole) {
        if (!sessionManager.isLoggedIn()) {
            throw new SecurityException("Bu işlemi yapmak için sisteme giriş yapmalısınız: " + joinPoint.getSignature().getName());
        }

        User currentUser = sessionManager.getCurrentUser();
        Role userRole = currentUser.getRole();
        
        // ADMIN yetkisi olanlar her işlemi yapabilir.
        if (userRole == Role.ADMIN) {
            return;
        }

        Role[] allowedRoles = requireRole.value();
        boolean isAuthorized = Arrays.asList(allowedRoles).contains(userRole);

        if (!isAuthorized) {
            String methodName = joinPoint.getSignature().getName();
            throw new SecurityException(String.format("'%s' kullanıcısı '%s' işlemini yapmak için yeterli yetkiye sahip değil! (Gereken rol(ler): %s)", 
                currentUser.getUsername(), methodName, Arrays.toString(allowedRoles)));
        }
    }
}
