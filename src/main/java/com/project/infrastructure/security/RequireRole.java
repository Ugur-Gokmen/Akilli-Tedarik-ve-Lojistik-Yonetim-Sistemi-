package com.project.infrastructure.security;

import com.project.domain.user.Role;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Metot seviyesinde rol bazlı yetkilendirme anotasyonu.
 *
 * <p>Spring AOP — SecurityAspect tarafından ele geçirilir. Metot çalışmadan
 * önce SessionManager üzerinden kullanıcı rolü kontrol edilir.
 * ADMIN rolü her zaman tüm işlemleri yapabilir.</p>
 *
 * <pre>
 * Kullanım örnekleri:
 *   {@code @RequireRole(Role.STAFF)}              // Tek rol
 *   {@code @RequireRole({Role.CUSTOMER, Role.STAFF})} // Birden fazla rol
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequireRole {

    /**
     * @return İzin verilen rollerin listesi (tek veya çoklu)
     */
    Role[] value();
}
