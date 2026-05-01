package com.project.infrastructure.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.project.domain.user.Role;

/**
 * Controller ve Service metotlarında yetkilendirme gereksinimi koymak için kullanılır.
 * <p>Spring AOP yardımıyla metot çalışmadan önce yakalanır ve mevcut session (ThreadLocal) 
 * üzerinden kullanıcının rolü kontrol edilir.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequireRole {
    
    /**
     * @return İzin verilen rollerin listesi
     */
    Role[] value();
}
