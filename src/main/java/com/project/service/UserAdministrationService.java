package com.project.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.project.domain.user.Role;
import com.project.domain.user.User;
import com.project.repository.UserRepository;

@Service
public class UserAdministrationService {

    private final UserRepository userRepository;

    public UserAdministrationService(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository");
    }

    public List<User> listAllUsersForAdmin(User currentUser) {
        if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
            throw new SecurityException("Bu işlem için ADMIN yetkisi gerekli.");
        }
        return userRepository.findAll();
    }
}

