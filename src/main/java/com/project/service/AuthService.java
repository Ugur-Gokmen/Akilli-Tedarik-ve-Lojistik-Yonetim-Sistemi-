package com.project.service;

import com.project.domain.user.Role;
import com.project.domain.user.User;
import com.project.repository.UserRepository;
import com.project.config.AppProperties;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, AppProperties appProperties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.appProperties = appProperties;
    }

    public User authenticate(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new SecurityException("Kullanıcı adı veya şifre hatalı."));

        String passwordHash = user.getPasswordHash();
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new SecurityException("Kullanıcı adı veya şifre hatalı.");
        }

        if (isBcryptHash(passwordHash)) {
            try {
                if (!passwordEncoder.matches(password, passwordHash)) {
                    throw new SecurityException("Kullanıcı adı veya şifre hatalı.");
                }
            } catch (IllegalArgumentException ex) {
                throw new SecurityException("Kullanıcı adı veya şifre hatalı.");
            }
            return user;
        }

        // Legacy düz metin şifreler için tek seferlik uyumluluk:
        // doğru girişte hash'i BCrypt'e yükseltiyoruz.
        if (!password.equals(passwordHash)) {
            throw new SecurityException("Kullanıcı adı veya şifre hatalı.");
        }

        user.setPasswordHash(passwordEncoder.encode(password));
        userRepository.save(user);
        return user;
    }

    private boolean isBcryptHash(String passwordHash) {
        return passwordHash.startsWith("$2a$")
            || passwordHash.startsWith("$2b$")
            || passwordHash.startsWith("$2y$");
    }

    public void registerUser(String username, String email, String password, Role role) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Bu kullanıcı adı zaten alınmış: " + username);
        }

        String hashedPassword = passwordEncoder.encode(password);
        User user = new User(username, email, hashedPassword, role);
        user.setFacility(appProperties.getUsers().getDefaultFacility());
        userRepository.save(user);
    }
}
