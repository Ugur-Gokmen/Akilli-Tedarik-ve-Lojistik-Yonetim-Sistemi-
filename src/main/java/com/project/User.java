package com.project.domain.user;

import java.util.UUID;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Sistem kullanıcısı domain entity'si.
 *
 * <p>Her kullanıcının bir rolü vardır ve bu rol, sistemde yapabileceği
 * işlemleri belirler (RBAC - Role Based Access Control).</p>
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    private String id;
    private String username;
    private String email;
    private String passwordHash;
    
    @Enumerated(EnumType.STRING)
    private Role role;

    /**
     * JPA için varsayılan yapıcı (protected).
     */
    protected User() {
    }

    /**
     * Yeni bir kullanıcı oluşturur.
     *
     * @param username     Kullanıcı adı
     * @param email        E-posta adresi
     * @param passwordHash Hashlenmiş şifre
     * @param role         Kullanıcı rolü
     */
    public User(String username, String email, String passwordHash, Role role) {
        this.id = UUID.randomUUID().toString();
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    // --- Getters ---

    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }

    /**
     * Kullanıcının belirtilen role sahip olup olmadığını kontrol eder.
     *
     * @param requiredRole Kontrol edilecek rol
     * @return Yetkili ise true
     */
    public boolean hasRole(Role requiredRole) {
        return this.role == requiredRole;
    }

    /**
     * Admin veya Staff rolüne sahip kullanıcıları kontrol eder.
     * Müşterilerin erişemeyeceği işlemler için kullanılır.
     *
     * @return Yetkili çalışan ise true
     */
    public boolean isStaffOrAbove() {
        return this.role == Role.ADMIN || this.role == Role.STAFF;
    }

    @Override
    public String toString() {
        return String.format("User{id='%s', username='%s', role=%s}", id, username, role);
    }
}
