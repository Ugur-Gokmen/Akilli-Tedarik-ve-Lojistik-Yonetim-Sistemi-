package com.project.infrastructure.aop;

import com.project.domain.user.Role;
import com.project.domain.user.User;
import com.project.infrastructure.security.RequireRole;
import com.project.ui.SessionManager;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Aspect: Güvenlik ve yetkilendirmeleri Proxy üzerinden yönetir.
 * 
 * <p>AOP sayesinde Controller ve Service metotlarına "@RequireRole" yazılması
 * yeterlidir. Kod içerisine guard çağrıları yapılmaz. İş mantığı temiz kalır.</p>
 * 
 * SUNUM NOTU: 
 * Bu sınıf AOP'nin (Aspect Oriented Programming) kalbidir. Sisteme gelen tüm
 * istekler asıl metoda gitmeden önce bu noktada yakalanır (Interception).
 * Böylece servis metotlarının içi "Kullanıcı yetkili mi?" if-else bloklarıyla 
 * kirlenmez, %100 Business Logic (İş Mantığı) odaklı kalır.
 */
@Aspect
@Component
public class SecurityAspect {

    private final SessionManager sessionManager;

    @Autowired
    public SecurityAspect(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    // SUNUM NOTU: @Before tetikleyicisi, hedef metot ÇALIŞMADAN HEMEN ÖNCE araya girmemizi sağlar.
    // jointPoint argümanı ile asıl çağırılan metodun adını ve bilgilerini yakalarız.
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
