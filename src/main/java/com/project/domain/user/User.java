package com.project.domain.user;

import java.util.UUID;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

/**
 * Sistem kullanıcısı domain entity'si.
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

    private String firstName;
    private String lastName;
    private String phone;
    private String employeeId;
    private String facility;
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean twoFactorEnabled;

    protected User() {
    }

    public User(String username, String email, String passwordHash, Role role) {
        this.id = UUID.randomUUID().toString();
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.firstName = username;
        this.lastName = "";
        this.employeeId = "EMP-" + id.substring(0, 4).toUpperCase();
        this.facility = null;
    }

    // --- Getters & Setters ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public String getFacility() { return facility; }
    public void setFacility(String facility) { this.facility = facility; }
    public boolean isTwoFactorEnabled() { return twoFactorEnabled; }
    public void setTwoFactorEnabled(boolean twoFactorEnabled) { this.twoFactorEnabled = twoFactorEnabled; }

    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }

    public boolean hasRole(Role requiredRole) {
        return this.role == requiredRole;
    }

    public boolean isStaffOrAbove() {
        return this.role == Role.ADMIN || this.role == Role.STAFF;
    }

    @Override
    public String toString() {
        return String.format("User{id='%s', username='%s', role=%s}", id, username, role);
    }
}
