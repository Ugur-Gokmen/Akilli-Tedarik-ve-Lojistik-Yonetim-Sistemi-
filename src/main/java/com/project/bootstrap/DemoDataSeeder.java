package com.project.bootstrap;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.project.api.controller.InventoryRestController;
import com.project.domain.product.CompositeProduct;
import com.project.domain.product.SimpleProduct;
import com.project.domain.user.Role;
import com.project.repository.UserRepository;
import com.project.service.AuthService;
import com.project.ui.SessionManager;

@Component
@Profile("dev")
public class DemoDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final UserRepository userRepo;
    private final AuthService authService;
    private final InventoryRestController inventoryRestController;
    private final SessionManager sessionManager;

    public DemoDataSeeder(UserRepository userRepo,
                          AuthService authService,
                          InventoryRestController inventoryRestController,
                          SessionManager sessionManager) {
        this.userRepo = userRepo;
        this.authService = authService;
        this.inventoryRestController = inventoryRestController;
        this.sessionManager = sessionManager;
    }

    @Override
    public void run(String... args) {
        log.info("Demo seed başlıyor (dev profili).");

        if (userRepo.count() != 0) {
            return;
        }

        log.info("Örnek veriler yükleniyor...");

        authService.registerUser("admin", "admin@sirket.com", "admin123", Role.ADMIN);
        authService.registerUser("personel", "staff@sirket.com", "staff123", Role.STAFF);
        authService.registerUser("musteri", "customer@sirket.com", "customer123", Role.CUSTOMER);

        userRepo.findByUsername("admin").ifPresent(sessionManager::login);

        SimpleProduct cpu = new SimpleProduct("Intel i9 CPU", "CPU-001", 15000.0, 0.5, 20, 5);
        SimpleProduct ram = new SimpleProduct("32GB DDR5 RAM", "RAM-001", 4000.0, 0.1, 50, 10);
        SimpleProduct ssd = new SimpleProduct("2TB NVMe SSD", "SSD-001", 3500.0, 0.1, 30, 5);
        inventoryRestController.addProduct(cpu);
        inventoryRestController.addProduct(ram);
        inventoryRestController.addProduct(ssd);

        CompositeProduct gamingPc = new CompositeProduct.Builder("Gaming PC Master")
            .assemblyFee(1000.0)
            .addComponent(cpu)
            .addComponent(ram)
            .addComponent(ssd)
            .initialStock(5)
            .stockThreshold(2)
            .build();
        inventoryRestController.addProduct(gamingPc);

        log.info("Örnek veriler başarıyla eklendi.");
        sessionManager.logout();
    }
}

