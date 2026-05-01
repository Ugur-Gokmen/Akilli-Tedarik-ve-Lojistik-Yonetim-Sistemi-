package com.project.domain.user;

/**
 * Yetkilendirme doğrulayıcısı.
 *
 * <p>Sisteme giriş yapmış kullanıcının belirli bir işlemi yapma
 * yetkisine sahip olup olmadığını kontrol eder. İçinde hiçbir
 * switch-case veya if-else zinciri bulunmaz; polimorfizm ve
 * rol hiyerarşisi üzerinden çalışır.</p>
 */
public class AuthorizationGuard {

    private final User currentUser;

    /**
     * @param currentUser Oturum açmış kullanıcı
     */
    public AuthorizationGuard(User currentUser) {
        if (currentUser == null) {
            throw new IllegalStateException("Oturum açılmamış. İşlem yapılamaz.");
        }
        this.currentUser = currentUser;
    }

    /**
     * Belirtilen rolü gerektirir; aksi hâlde exception fırlatır.
     *
     * @param requiredRole Gerekli minimum rol
     * @throws SecurityException Yetersiz yetki durumunda
     */
    public void require(Role requiredRole) {
        if (!currentUser.hasRole(requiredRole)) {
            throw new SecurityException(
                String.format("Yetersiz yetki! Kullanıcı '%s' (Rol: %s) bu işlem için '%s' rolüne ihtiyaç duyuyor.",
                    currentUser.getUsername(), currentUser.getRole(), requiredRole)
            );
        }
    }

    /**
     * Staff veya Admin rolünü gerektirir.
     *
     * @throws SecurityException Müşteri erişiminde
     */
    public void requireStaffOrAbove() {
        if (!currentUser.isStaffOrAbove()) {
            throw new SecurityException(
                String.format("Yetersiz yetki! '%s' kullanıcısının bu işlemi yapma yetkisi yok.",
                    currentUser.getUsername())
            );
        }
    }

    /**
     * Admin rolünü gerektirir.
     *
     * @throws SecurityException Admin olmayan kullanıcılarda
     */
    public void requireAdmin() {
        require(Role.ADMIN);
    }

    /**
     * Mevcut oturumu döner.
     *
     * @return Oturum açmış kullanıcı
     */
    public User getCurrentUser() {
        return currentUser;
    }
}
