package com.project.domain.user;

/**
 * Sistem kullanıcı rolleri.
 * Role-Based Access Control (RBAC) için kullanılır.
 */
public enum Role {
    /** Tüm yetkiler - sistem yönetimi, raporlama */
    ADMIN,
    /** Depo personeli - stok ve sipariş işlemleri */
    STAFF,
    /** Müşteri - sipariş verme ve takip */
    CUSTOMER
}
