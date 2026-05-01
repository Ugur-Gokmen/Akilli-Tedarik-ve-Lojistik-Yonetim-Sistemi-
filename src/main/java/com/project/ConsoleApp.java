package com.project.ui;

import com.project.controller.Controllers.InventoryController;
import com.project.controller.Controllers.OrderController;
import com.project.domain.cargo.CargoProvider;
import com.project.domain.order.Order;
import com.project.domain.payment.PaymentStrategy;
import com.project.domain.product.Product;
import com.project.domain.product.SimpleProduct;
import com.project.domain.user.Role;
import com.project.domain.user.User;
import com.project.infrastructure.factory.CargoProviderFactory;
import com.project.infrastructure.factory.PaymentFactory;
import com.project.repository.UserRepository;
import com.project.infrastructure.logger.SystemLogger;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.Scanner;

@Component
public class ConsoleApp {
    private final UserRepository userRepository;
    private final InventoryController inventoryCtrl;
    private final OrderController orderCtrl;
    private final SessionManager sessionManager;
    
    // SUNUM NOTU: Strategy Pattern'in Dependency Injection ile kullanımı.
    // Spring Boot buradaki map içerisine, sistemdeki tüm PaymentStrategy 
    // bean'lerini (@Component) otomatik doldurur. (Anahtar=Bean Adı, Değer=Kendisi)
    private final Map<String, PaymentStrategy> paymentStrategies;
    private final Scanner scanner;
    
    private Order currentActiveOrder = null;

    @Autowired
    public ConsoleApp(UserRepository userRepository, InventoryController inventoryCtrl, OrderController orderCtrl, Map<String, PaymentStrategy> paymentStrategies) {
        this.userRepository = userRepository;
        this.inventoryCtrl = inventoryCtrl;
        this.orderCtrl = orderCtrl;
        this.paymentStrategies = paymentStrategies;
        this.sessionManager = new SessionManager();
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        boolean exit = false;
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║   AKILLI TEDARİK VE LOJİSTİK YÖNETİM SİSTEMİ           ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");

        while (!exit) {
            try {
                if (!sessionManager.isLoggedIn()) {
                    exit = showMainMenu();
                } else {
                    Role role = sessionManager.getCurrentUser().getRole();
                    switch (role) {
                        case CUSTOMER:
                            exit = showCustomerMenu();
                            break;
                        case STAFF:
                            exit = showStaffMenu();
                            break;
                        case ADMIN:
                            exit = showAdminMenu();
                            break;
                        default:
                            System.out.println("Desteklenmeyen rol!");
                            sessionManager.logout();
                    }
                }
            } catch (Exception e) {
                System.out.println("⚠ Beklenmeyen Hata: " + e.getMessage());
            }
        }
        System.out.println("Sistemden çıkış yapıldı. İyi günler!");
        SystemLogger.getInstance().close();
    }

    private boolean showMainMenu() {
        System.out.println("\n[--- ANA MENÜ ---]");
        System.out.println("1. Sisteme Giriş Yap");
        System.out.println("2. Müşteri Olarak Kayıt Ol");
        System.out.println("0. Çıkış");
        System.out.print("Seçiminiz: ");
        
        String choice = scanner.nextLine().trim();
        
        switch (choice) {
            case "1":
                loginFlow();
                return false;
            case "2":
                registerFlow();
                return false;
            case "0":
                return true; 
            default:
                System.out.println("Geçersiz seçim!");
                return false;
        }
    }

    private void loginFlow() {
        System.out.print("Kullanıcı Adı: ");
        String username = scanner.nextLine().trim();
        
        Optional<User> foundOpt = userRepository.findByUsername(username);
        
        if (foundOpt.isPresent()) {
            User found = foundOpt.get();
            sessionManager.login(found);
            System.out.println("✅ Giriş başarılı. Hoşgeldin " + found.getUsername() + " (Rol: " + found.getRole() + ")");
            currentActiveOrder = null; // reset on fresh login
        } else {
            System.out.println("❌ Kullanıcı bulunamadı!");
        }
    }

    private void registerFlow() {
        System.out.print("İstediğiniz Kullanıcı Adı: ");
        String username = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        
        User newUser = new User(username, email, "pass123", Role.CUSTOMER);
        userRepository.save(newUser);
        System.out.println("✅ Kayıt başarılı. Lütfen giriş yapınız.");
    }

    private boolean showCustomerMenu() {
        System.out.println("\n[--- MÜŞTERİ MENÜSÜ ---]");
        System.out.println("1. Ürünleri Listele");
        System.out.println("2. Sepete Ürün Ekle");
        System.out.println("3. Sepeti Onayla ve Ödeme Yap");
        System.out.println("0. Oturumu Kapat");
        System.out.print("Seçiminiz: ");

        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1":
                inventoryCtrl.listProducts();
                break;
            case "2":
                addToCartFlow();
                break;
            case "3":
                checkoutFlow();
                break;
            case "0":
                sessionManager.logout();
                currentActiveOrder = null;
                break;
            default:
                System.out.println("Geçersiz seçim!");
        }
        return false;
    }
    
    private void addToCartFlow() {
        inventoryCtrl.listProducts();
        System.out.print("\nEklemek istediğiniz ürünün ID'si (Örn: CPU-I9-001): ");
        String productId = scanner.nextLine().trim();
        System.out.print("Kaç adet?: ");
        int qty;
        try {
            qty = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Lütfen sayı giriniz!");
            return;
        }

        if (currentActiveOrder == null) {
            currentActiveOrder = orderCtrl.createOrder(sessionManager.getCurrentUser());
        }
        
        orderCtrl.addItem(currentActiveOrder.getId(), productId, qty);
        System.out.println("Sepet Toplamı: " + currentActiveOrder.getTotalAmount() + " TL");
    }

    private void checkoutFlow() {
        if (currentActiveOrder == null || currentActiveOrder.getItems().isEmpty()) {
            System.out.println("Sepetiniz boş!");
            return;
        }
        
        System.out.println("\nÖdenecek Tutar: " + currentActiveOrder.getTotalAmount() + " TL");
        System.out.println("Ödeme Yöntemi Seçin:");
        System.out.println("1. Kredi Kartı");
        System.out.println("2. Havale");
        System.out.println("3. Kripto Para");
        System.out.print("Seçim: ");
        String payChoice = scanner.nextLine().trim();
        
        PaymentStrategy strategy = null;
        Map<String, String> details = new HashMap<>();
        try {
            if ("1".equals(payChoice)) {
                System.out.print("Kart Numarası: ");
                details.put("cardNumber", scanner.nextLine().trim());
                System.out.print("Ad Soyad: ");
                details.put("cardHolder", scanner.nextLine().trim());
                strategy = paymentStrategies.get("creditCardStrategy");
            } else if ("2".equals(payChoice)) {
                System.out.print("Banka: ");
                details.put("bankName", scanner.nextLine().trim());
                System.out.print("IBAN: ");
                details.put("iban", scanner.nextLine().trim());
                strategy = paymentStrategies.get("bankTransferStrategy");
            } else if ("3".equals(payChoice)) {
                System.out.print("Cüzdan Adresi: ");
                details.put("walletAddress", scanner.nextLine().trim());
                System.out.print("Coin (Örn: ETH): ");
                details.put("currency", scanner.nextLine().trim());
                strategy = paymentStrategies.get("cryptoStrategy");
            } else {
                System.out.println("Geçersiz seçim!");
                return;
            }
            
            var result = orderCtrl.processPayment(currentActiveOrder, strategy, details);
            if (result != null && result.isSuccess()) {
                currentActiveOrder = null; // ödeme bitti, sepeti boşalt
            }
        } catch (Exception e) {
            System.out.println("❌ Ödeme yapılamadı: " + e.getMessage());
        }
    }

    private boolean showStaffMenu() {
       System.out.println("\n[--- DEPO PERSONELİ MENÜSÜ ---]");
       System.out.println("1. Tüm Siparişleri Görüntüle");
       System.out.println("2. Siparişi Onayla");
       System.out.println("3. Siparişi Hazırlamaya Başla");
       System.out.println("4. Siparişi Kargoya Ver");
       System.out.println("5. Siparişi Teslim Edildi İşaretle");
       System.out.println("0. Oturumu Kapat");
       System.out.print("Seçiminiz: ");
       String choice = scanner.nextLine().trim();
       
       switch (choice) {
            case "1":
                orderCtrl.listAllOrders();
                break;
            case "2":
                System.out.print("Onaylanacak Sipariş ID: ");
                orderCtrl.approve(scanner.nextLine().trim());
                break;
            case "3":
                System.out.print("Hazırlanacak Sipariş ID: ");
                orderCtrl.startPreparing(scanner.nextLine().trim());
                break;
            case "4":
                shipOrderFlow();
                break;
            case "5":
                System.out.print("Teslim edilen Sipariş ID: ");
                orderCtrl.deliver(scanner.nextLine().trim());
                break;
            case "0":
                sessionManager.logout();
                break;
            default:
                System.out.println("Geçersiz seçim!");
       }
       return false;
    }
    
    private void shipOrderFlow() {
        System.out.print("Kargolanacak Sipariş ID: ");
        String orderId = scanner.nextLine().trim();
        
        System.out.println("Kargo Firması Seçin:");
        System.out.println("1. Aras Kargo");
        System.out.println("2. Yurtiçi Kargo");
        System.out.println("3. Global Express (Premium)");
        System.out.print("Seçim: ");
        String cargoChoice = scanner.nextLine().trim();
        
        CargoProvider provider;
        if ("1".equals(cargoChoice)) provider = CargoProviderFactory.create(CargoProviderFactory.CargoCompany.ARAS);
        else if ("2".equals(cargoChoice)) provider = CargoProviderFactory.create(CargoProviderFactory.CargoCompany.YURTICI);
        else if ("3".equals(cargoChoice)) provider = CargoProviderFactory.create(CargoProviderFactory.CargoCompany.GLOBAL_EXPRESS_PREMIUM);
        else {
            System.out.println("Geçersiz kargo seçimi.");
            return;
        }
        
        System.out.print("Gönderici Şehir: "); String sender = scanner.nextLine().trim();
        System.out.print("Alıcı Şehir: "); String receiver = scanner.nextLine().trim();
        System.out.print("Mesafe (KM): "); double dist = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("Sigortalı Gönderim mi? (E/H): "); boolean ins = scanner.nextLine().trim().equalsIgnoreCase("E");
        System.out.print("Kırılacak Eşya Koruması (Hassas)? (E/H): "); boolean frag = scanner.nextLine().trim().equalsIgnoreCase("E");
        
        orderCtrl.shipWithCargo(orderId, provider, sender, receiver, dist, ins, frag);
    }

    private boolean showAdminMenu() {
       System.out.println("\n[--- YÖNETİCİ MENÜSÜ ---]");
       System.out.println("1. Tüm Ürünleri Listele");
       System.out.println("2. Düşük Stoklu Ürünleri Raporla");
       System.out.println("3. Yeni Basit Ürün Ekle");
       System.out.println("0. Oturumu Kapat");
       System.out.print("Seçiminiz: ");
       String choice = scanner.nextLine().trim();
       switch (choice) {
           case "1":
               inventoryCtrl.listProducts();
               break;
           case "2":
               inventoryCtrl.listLowStockProducts();
               break;
           case "3":
               System.out.print("Ürün Adı: "); String name = scanner.nextLine();
               System.out.print("Ürün Kodu (Örn SKU-01): "); String sku = scanner.nextLine();
               System.out.print("Fiyat: "); double price = Double.parseDouble(scanner.nextLine());
               System.out.print("Ağırlık: "); double weight = Double.parseDouble(scanner.nextLine());
               System.out.print("Stok: "); int stock = Integer.parseInt(scanner.nextLine());
               System.out.print("Eşik Değer: "); int thresh = Integer.parseInt(scanner.nextLine());
               
               Product p = new SimpleProduct(name, sku, price, weight, stock, thresh);
               inventoryCtrl.addProduct(p);
               System.out.println("Ürün eklendi.");
               break;
           case "0":
               sessionManager.logout();
               break;
           default:
               System.out.println("Böyle bir metot henüz bağlanmadı veya geçersiz seçim!");
       }
       return false;
    }
}
