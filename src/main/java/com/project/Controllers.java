package com.project.controller;

import java.util.List;
import java.util.Map;
import com.project.domain.cargo.CargoProvider;
import com.project.domain.notification.EmailStockNotifier;
import com.project.domain.notification.InAppStockNotifier;
import com.project.domain.order.Order;
import com.project.domain.payment.PaymentResult;
import com.project.domain.payment.PaymentStrategy;
import com.project.domain.product.Product;
import com.project.domain.user.User;
import com.project.infrastructure.logger.SystemLogger;
import com.project.service.InventoryService;
import com.project.service.OrderService;
import com.project.service.Services.CargoService;
import com.project.service.Services.PaymentService;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Controller katmanı - MVC mimarisinin C bileşeni.
 *
 * <p>Controller sınıfları HTTP isteklerini (veya CLI komutlarını) alır,
 * doğrulamayı gerçekleştirir ve ilgili servise yönlendirir.
 * İş mantığı CONTROLLER'da yer almaz; servis katmanına delege edilir.</p>
 *
 * <p>Her controller bağımlılıklarını constructor ile alır → Dependency Injection
 * ve Dependency Inversion Principle uygulanmıştır.</p>
 */
public class Controllers {

    // ─────────────────────────────────────────────────
    // Envanter Controller
    // ─────────────────────────────────────────────────

    /**
     * Ürün ve stok yönetimi controller'ı.
     */
    @Component
    public static class InventoryController {

        private final InventoryService inventoryService;
        private final SystemLogger logger = SystemLogger.getInstance();

        /**
         * @param inventoryService Envanter servisi (DI ile enjekte edilir)
         */
        @Autowired
        public InventoryController(InventoryService inventoryService) {
            this.inventoryService = inventoryService;
        }

        /**
         * Yeni ürün ekler. STAFF/ADMIN yetkisi gerektirir.
         *
         * @param product Eklenecek ürün
         */
        public void addProduct(Product product) {
            try {
                inventoryService.addProduct(product);
            } catch (SecurityException e) {
                logger.error("YETKİ HATASI - Ürün ekleme: " + e.getMessage());
                System.out.println("🚫 Yetersiz yetki: " + e.getMessage());
            } catch (Exception e) {
                logger.error("Ürün ekleme hatası: " + e.getMessage());
                System.out.println("❌ Hata: " + e.getMessage());
            }
        }

        /**
         * Stok günceller.
         *
         * @param productId Ürün ID'si
         * @param quantity  Eklenen miktar
         */
        public void restock(String productId, int quantity) {
            try {
                inventoryService.restockProduct(productId, quantity);
            } catch (SecurityException e) {
                System.out.println("🚫 Yetersiz yetki: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("❌ Stok güncelleme hatası: " + e.getMessage());
            }
        }

        /**
         * Stok bildirim sistemi kurar: email + sistem içi.
         *
         * @param productId            Ürün ID'si
         * @param purchasingEmail      Satın alma birimi e-postası
         * @param warehouseManagerName Depo sorumlusunun adı
         */
        public void setupStockNotifications(String productId,
                                            String purchasingEmail, String warehouseManagerName) {
            try {
                inventoryService.addStockObserver(productId,
                    new EmailStockNotifier(purchasingEmail));
                inventoryService.addStockObserver(productId,
                    new InAppStockNotifier(warehouseManagerName));
                System.out.println("🔔 Stok bildirimleri kuruldu: " + productId);
            } catch (Exception e) {
                System.out.println("❌ Bildirim kurulum hatası: " + e.getMessage());
            }
        }

        /**
         * Main metodunun kontrolü için ürun listesini döner.
         */
        public List<Product> listProductsForMainLogicCheck() {
            return inventoryService.listAllProducts();
        }

        /**
         * Tüm ürünleri listeler.
         *
         * @return Ürün listesi
         */
        public List<Product> listProducts() {
            List<Product> products = inventoryService.listAllProducts();
            System.out.println("\n📋 ÜRÜN LİSTESİ (" + products.size() + " ürün):");
            System.out.println("═".repeat(60));
            products.forEach(p -> p.display("  "));
            System.out.println("═".repeat(60));
            return products;
        }

        /**
         * Kritik stok ürünlerini listeler.
         *
         * @return Kritik stok ürünleri
         */
        public List<Product> listLowStockProducts() {
            List<Product> lowStock = inventoryService.getLowStockProducts();
            System.out.println("\n⚠️  KRİTİK STOK ÜRÜNLER (" + lowStock.size() + " ürün):");
            lowStock.forEach(p -> System.out.printf("  - %s | Stok: %d | Eşik: %d%n",
                p.getName(), p.getStock(), p.getStockThreshold()));
            return lowStock;
        }
    }

    // ─────────────────────────────────────────────────
    // Sipariş Controller
    // ─────────────────────────────────────────────────

    /**
     * Sipariş yönetimi controller'ı.
     */
    @Component
    public static class OrderController {

        private final OrderService orderService;
        private final PaymentService paymentService;
        private final CargoService cargoService;
        private final SystemLogger logger = SystemLogger.getInstance();

        /**
         * @param orderService   Sipariş servisi
         * @param paymentService Ödeme servisi
         * @param cargoService   Kargo servisi
         */
        @Autowired
        public OrderController(OrderService orderService,
                               PaymentService paymentService,
                               CargoService cargoService) {
            this.orderService = orderService;
            this.paymentService = paymentService;
            this.cargoService = cargoService;
        }

        /**
         * Yeni sipariş oluşturur.
         *
         * @param customer Müşteri
         * @return Oluşturulan sipariş
         */
        public Order createOrder(User customer) {
            try {
                Order order = orderService.createOrder(customer);
                System.out.printf("🛒 Sipariş oluşturuldu: %s%n", order.getId());
                return order;
            } catch (Exception e) {
                logger.error("Sipariş oluşturma hatası: " + e.getMessage());
                System.out.println("❌ Hata: " + e.getMessage());
                return null;
            }
        }

        /**
         * Ürün ekler ve ödeme alır.
         *
         * @param orderId   Sipariş ID'si
         * @param productId Ürün ID'si
         * @param quantity  Miktar
         */
        public void addItem(String orderId, String productId, int quantity) {
            try {
                orderService.addItemToOrder(orderId, productId, quantity);
                System.out.printf("  + Ürün eklendi → Sipariş: %s%n", orderId);
            } catch (Exception e) {
                System.out.println("❌ Ürün ekleme hatası: " + e.getMessage());
            }
        }

        /**
         * Ödeme işlemini gerçekleştirir.
         *
         * @param order    Sipariş
         * @param strategy Ödeme stratejisi
         * @param details  Ödeme detayları
         * @return Ödeme sonucu
         */
        public PaymentResult processPayment(Order order,
                                            PaymentStrategy strategy, Map<String, String> details) {
            try {
                return paymentService.processPayment(order, strategy, details);
            } catch (Exception e) {
                logger.error("Ödeme hatası: " + e.getMessage());
                System.out.println("❌ Ödeme hatası: " + e.getMessage());
                return null;
            }
        }

        /**
         * Kargo fiyatı hesaplar ve siparişi kargoya verir.
         *
         * @param order          Sipariş
         * @param provider       Kargo sağlayıcısı
         * @param senderCity     Gönderici şehri
         * @param receiverCity   Alıcı şehri
         * @param distanceKm     Mesafe
         * @param withInsurance  Sigortalı gönderim mi?
         * @param withFragile    Kırılgan eşya mı?
         */
        public void shipWithCargo(String orderId,
                                  CargoProvider provider, String senderCity, String receiverCity,
                                  double distanceKm, boolean withInsurance, boolean withFragile) {
            try {
                Order order = orderService.findById(orderId)
                        .orElseThrow(() -> new IllegalArgumentException("Sipariş bulunamadı: " + orderId));
                
                // Takip numarası oluştur
                String tracking = cargoService.generateAndAssignTracking(
                    provider, order, senderCity, receiverCity);

                // Kargo ücretini hesapla
                double shippingCost = cargoService.calculateShippingCost(
                    provider, order, distanceKm, withInsurance, withFragile);

                // Siparişi kargoya ver (State Pattern devreye girer)
                orderService.shipOrder(order.getId(), tracking, shippingCost);

            } catch (SecurityException e) {
                System.out.println("🚫 Yetersiz yetki: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("❌ Kargo hatası: " + e.getMessage());
            }
        }

        /**
         * Siparişi onayla.
         */
        public void approve(String orderId) {
            try {
                orderService.approveOrder(orderId);
            } catch (Exception e) {
                System.out.println("❌ Onay hatası: " + e.getMessage());
            }
        }

        /**
         * Hazırlığı başlat.
         */
        public void startPreparing(String orderId) {
            try {
                orderService.startPreparing(orderId);
            } catch (Exception e) {
                System.out.println("❌ Hazırlık hatası: " + e.getMessage());
            }
        }

        /**
         * Teslim edildi işaretle.
         */
        public void deliver(String orderId) {
            try {
                orderService.deliverOrder(orderId);
            } catch (Exception e) {
                System.out.println("❌ Teslim hatası: " + e.getMessage());
            }
        }

        /**
         * İade başlat.
         */
        public void returnOrder(String orderId) {
            try {
                orderService.returnOrder(orderId);
            } catch (Exception e) {
                System.out.println("❌ İade hatası: " + e.getMessage());
            }
        }

        /**
         * İptal et.
         */
        public void cancel(String orderId) {
            try {
                orderService.cancelOrder(orderId);
            } catch (Exception e) {
                System.out.println("❌ İptal hatası: " + e.getMessage());
            }
        }

        /**
         * Tüm siparişleri listeler.
         */
        public void listAllOrders() {
            try {
                List<Order> orders = orderService.listAllOrders();
                System.out.println("\n📋 SİPARİŞ LİSTESİ (" + orders.size() + " sipariş):");
                System.out.println("═".repeat(70));
                orders.forEach(o -> System.out.printf(
                    "  ID: %-10s | Müşteri: %-12s | Durum: %-16s | Toplam: %.2f TL%n",
                    o.getId(), o.getCustomer().getUsername(), o.getCurrentStateName(), o.getGrandTotal()));
                System.out.println("═".repeat(70));
            } catch (SecurityException e) {
                System.out.println("🚫 Yetersiz yetki: " + e.getMessage());
            }
        }
    }
}
