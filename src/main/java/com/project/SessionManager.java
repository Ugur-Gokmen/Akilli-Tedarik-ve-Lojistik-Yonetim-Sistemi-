package com.project.ui;

import com.project.domain.user.User;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;
import java.io.Serializable;

/**
 * SessionScope: Her tarayıcı sekmesi/oturumu için ayrı bir instance oluşturur.
 * Serializable: Nesnenin oturumda saklanabilmesi için gereklidir.
 */
@Component
@SessionScope
public class SessionManager implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private User currentUser;

    public void login(User user) {
        this.currentUser = user;
    }

    public void logout() {
        this.currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public User getCurrentUser() {
        return currentUser;
    }
}
