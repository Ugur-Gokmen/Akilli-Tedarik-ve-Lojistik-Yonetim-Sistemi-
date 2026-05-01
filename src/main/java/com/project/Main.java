package com.project;

import com.project.controller.Controllers.InventoryController;
import com.project.controller.Controllers.OrderController;
import com.project.domain.product.CompositeProduct;
import com.project.domain.product.SimpleProduct;
import com.project.domain.user.Role;
import com.project.domain.user.User;
import com.project.repository.UserRepository;
import com.project.ui.ConsoleApp;
import com.project.ui.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main implements CommandLineRunner {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private InventoryController inventoryCtrl;

    @Autowired
    private OrderController orderCtrl;

    @Autowired
    private ConsoleApp consoleApp;

    @Autowired
    private SessionManager sessionManager;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║   AKILLI TEDARİK VE LOJİSTİK YÖNETİM SİSTEMİ           ║");
        System.out.println("║   Spring Boot & PostgreSQL Migration                     ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝\n");

        if (userRepo.count() == 0) {
            System.out.println("🌱 Örnek veriler yükleniyor...");

            // 1. Kullanıcılar (Role-Based Access Control kanıtı için)
            User admin = new User("admin", "admin@sirket.com", "admin123", Role.ADMIN);
            userRepo.save(admin);
            userRepo.save(new User("personel", "staff@sirket.com", "staff123", Role.STAFF));
            userRepo.save(new User("musteri", "customer@sirket.com", "customer123", Role.CUSTOMER));

            // AOP yetki kontrolünü geçmek için geçici admin girişi yapıyoruz
            sessionManager.login(admin);

            // 2. Basit Ürünler
            SimpleProduct cpu = new SimpleProduct("Intel i9 CPU", "CPU-001", 15000.0, 0.5, 20, 5);
            SimpleProduct ram = new SimpleProduct("32GB DDR5 RAM", "RAM-001", 4000.0, 0.1, 50, 10);
            SimpleProduct ssd = new SimpleProduct("2TB NVMe SSD", "SSD-001", 3500.0, 0.1, 30, 5);
            inventoryCtrl.addProduct(cpu);
            inventoryCtrl.addProduct(ram);
            inventoryCtrl.addProduct(ssd);

            // 3. Bileşik Ürün (Composite Pattern Kanıtı)
            CompositeProduct gamingPc = new CompositeProduct.Builder("Gaming PC Master")
                    .assemblyFee(1000.0)
                    .addComponent(cpu)
                    .addComponent(ram)
                    .addComponent(ssd)
                    .initialStock(5)
                    .stockThreshold(2)
                    .build();
            inventoryCtrl.addProduct(gamingPc);

            System.out.println("✅ Örnek veriler başarıyla eklendi.");
            
            // Başlangıç verisi eklendikten sonra çıkış yapıyoruz
            sessionManager.logout();
        }

        // ─────────────────────────────────────────────────
        // 4. ETKİLEŞİMLİ KONSOL UYGULAMASINI BAŞLAT
        // ─────────────────────────────────────────────────
        System.out.println("\n🚀 Konsol uygulaması başlatılıyor...");
        consoleApp.start();
    }
}
