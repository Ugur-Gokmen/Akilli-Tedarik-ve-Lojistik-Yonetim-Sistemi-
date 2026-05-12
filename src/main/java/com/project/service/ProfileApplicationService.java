package com.project.service;

import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.domain.user.User;
import com.project.repository.UserRepository;

@Service
public class ProfileApplicationService {

    private final UserRepository userRepository;

    public ProfileApplicationService(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository");
    }

    @Transactional
    public void updateProfile(User currentUser, User profileData) {
        if (currentUser == null) {
            throw new SecurityException("Giriş yapılmamış.");
        }
        if (profileData == null) {
            throw new IllegalArgumentException("Profil verisi boş olamaz.");
        }
        currentUser.setFirstName(profileData.getFirstName());
        currentUser.setLastName(profileData.getLastName());
        currentUser.setEmail(profileData.getEmail());
        currentUser.setPhone(profileData.getPhone());
        userRepository.save(currentUser);
    }
}

