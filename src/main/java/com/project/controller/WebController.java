package com.project.controller;

import com.project.domain.cargo.CargoProvider;
import com.project.domain.order.Order;
import com.project.domain.payment.PaymentStrategy;
import com.project.domain.product.CompositeProduct;
import com.project.domain.product.Product;
import com.project.domain.product.SimpleProduct;
import com.project.domain.user.User;
import com.project.infrastructure.factory.CargoProviderFactory;
import com.project.repository.ProductRepository;
import com.project.repository.UserRepository;
import com.project.service.InventoryService;
import com.project.service.OrderService;
import com.project.service.Services.CargoService;
import com.project.service.Services.PaymentService;
import com.project.ui.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Ana Web MVC Controller - tüm Thymeleaf sayfalarını yönetir.
 *
 * <p>@Controller (değil @RestController) döndüğü için Thymeleaf şablonları
 * render edilir. Her metot bir view adı (string) döner ve model'e
 * Thymeleaf'in kullanacağı değişkenler eklenir.</p>
 */
@Controller
public class WebController {

    private final InventoryService inventoryService;
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final CargoService cargoService;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final SessionManager sessionManager;
    private final ApplicationContext applicationContext;

    @Autowired
    public WebController(InventoryService inventoryService,
                         OrderService orderService,
                         PaymentService paymentService,
                         CargoService cargoService,
                         UserRepository userRepository,
                         ProductRepository productRepository,
                         SessionManager sessionManager,
                         ApplicationContext applicationContext) {
        this.inventoryService = inventoryService;
        this.orderService = orderService;
        this.paymentService = paymentService;
        this.cargoService = cargoService;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.sessionManager = sessionManager;
        this.applicationContext = applicationContext;
    }

    // ─────────────────────────────────────────────────
    // Kök URL → Login'e yönlendir
    // ─────────────────────────────────────────────────

    /**
     * Uygulamanın kök adresi → login sayfasına yönlendir.
     * Bu metot olmazsa Render'da "Whitelabel Error Page 404" görünür.
     */
    @GetMapping("/")
    public String root() {
        if (sessionManager.isLoggedIn()) {
            return "redirect:/products";
        }
        return "redirect:/auth/login";
    }

    // ─────────────────────────────────────────────────
    // Auth — Giriş / Çıkış
    // ─────────────────────────────────────────────────

    @GetMapping("/auth/login")
    public String loginPage(Model model) {
        return "login";
    }

    @PostMapping("/auth/login")
    public String doLogin(@RequestParam String username,
                          @RequestParam String password,
                          Model model,
                          RedirectAttributes redirectAttrs) {
        return userRepository.findByUsername(username)
            .map(user -> {
                sessionManager.login(user);
                return "redirect:/products";
            })
            .orElseGet(() -> {
                model.addAttribute("error", "Kullanıcı adı veya şifre hatalı!");
                return "login";
            });
    }

    @PostMapping("/auth/logout")
    public String doLogout() {
        sessionManager.logout();
        return "redirect:/auth/login";
    }

    // ─────────────────────────────────────────────────
    // Ürün Kataloğu (Müşteri görünümü)
    // ─────────────────────────────────────────────────

    @GetMapping("/products")
    public String productsPage(Model model) {
        if (!sessionManager.isLoggedIn()) return "redirect:/auth/login";
        model.addAttribute("products", inventoryService.listAllProducts());
        model.addAttribute("currentUser", sessionManager.getCurrentUser());
        return "products";
    }

    // ─────────────────────────────────────────────────
    // Envanter Yönetimi (Staff / Admin)
    // ─────────────────────────────────────────────────

    @GetMapping("/inventory")
    public String inventoryList(Model model) {
        if (!sessionManager.isLoggedIn()) return "redirect:/auth/login";
        model.addAttribute("products", inventoryService.listAllProducts());
        model.addAttribute("currentUser", sessionManager.getCurrentUser());
        return "inventory_list";
    }

    @GetMapping("/inventory/add-simple")
    public String addSimpleForm(Model model) {
        if (!sessionManager.isLoggedIn()) return "redirect:/auth/login";
        return "add_simple";
    }

    @PostMapping("/inventory/add-simple")
    public String addSimpleProduct(@RequestParam String name,
                                   @RequestParam String sku,
                                   @RequestParam double price,
                                   @RequestParam double weight,
                                   @RequestParam int stock,
                                   @RequestParam int threshold,
                                   RedirectAttributes redirectAttrs) {
        if (!sessionManager.isLoggedIn()) return "redirect:/auth/login";
        try {
            SimpleProduct product = new SimpleProduct(name, sku, price, weight, stock, threshold);
            inventoryService.addProduct(product);
            redirectAttrs.addFlashAttribute("successMessage", "'" + name + "' başarıyla eklendi.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Hata: " + e.getMessage());
        }
        return "redirect:/inventory";
    }

    @GetMapping("/inventory/add-composite")
    public String addCompositeForm(Model model) {
        if (!sessionManager.isLoggedIn()) return "redirect:/auth/login";
        // Sadece SimpleProduct'ları bileşen olarak sunuyoruz
        List<Product> simpleProducts = inventoryService.listAllProducts().stream()
            .filter(p -> p instanceof SimpleProduct)
            .collect(Collectors.toList());
        model.addAttribute("availableProducts", simpleProducts);
        return "add_composite";
    }

    @PostMapping("/inventory/add-composite")
    public String addCompositeProduct(@RequestParam String name,
                                      @RequestParam double assemblyFee,
                                      @RequestParam(required = false) List<String> selectedComponentIds,
                                      @RequestParam int stock,
                                      @RequestParam int threshold,
                                      RedirectAttributes redirectAttrs) {
        if (!sessionManager.isLoggedIn()) return "redirect:/auth/login";
        try {
            if (selectedComponentIds == null || selectedComponentIds.isEmpty()) {
                redirectAttrs.addFlashAttribute("errorMessage", "En az 1 bileşen seçmelisiniz.");
                return "redirect:/inventory/add-composite";
            }
            CompositeProduct.Builder builder = new CompositeProduct.Builder(name)
                .assemblyFee(assemblyFee)
                .initialStock(stock)
                .stockThreshold(threshold);

            for (String componentId : selectedComponentIds) {
                productRepository.findById(componentId)
                    .ifPresent(builder::addComponent);
            }
            inventoryService.addProduct(builder.build());
            redirectAttrs.addFlashAttribute("successMessage", "Bileşik ürün '" + name + "' eklendi.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Hata: " + e.getMessage());
        }
        return "redirect:/inventory";
    }

    // ─────────────────────────────────────────────────
    // Sipariş Yönetimi (Staff görünümü)
    // ─────────────────────────────────────────────────

    @GetMapping("/orders")
    public String ordersPage(Model model) {
        if (!sessionManager.isLoggedIn()) return "redirect:/auth/login";
        model.addAttribute("orders", orderService.listAllOrders());
        model.addAttribute("currentUser", sessionManager.getCurrentUser());
        return "order_management";
    }

    @PostMapping("/orders/{id}/approve")
    public String approveOrder(@PathVariable String id, RedirectAttributes redirectAttrs) {
        if (!sessionManager.isLoggedIn()) return "redirect:/auth/login";
        try {
            orderService.approveOrder(id);
            redirectAttrs.addFlashAttribute("successMessage", "Sipariş onaylandı: " + id);
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/orders";
    }

    @PostMapping("/orders/{id}/prepare")
    public String prepareOrder(@PathVariable String id, RedirectAttributes redirectAttrs) {
        if (!sessionManager.isLoggedIn()) return "redirect:/auth/login";
        try {
            orderService.startPreparing(id);
            redirectAttrs.addFlashAttribute("successMessage", "Hazırlık başlatıldı: " + id);
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/orders";
    }

    @GetMapping("/orders/{id}/shipping-form")
    public String shippingForm(@PathVariable String id, Model model) {
        if (!sessionManager.isLoggedIn()) return "redirect:/auth/login";
        orderService.findById(id).ifPresent(o -> model.addAttribute("order", o));
        model.addAttribute("companies",
            Arrays.stream(CargoProviderFactory.CargoCompany.values())
                  .map(Enum::name)
                  .collect(Collectors.toList()));
        return "shipping_form";
    }

    // ─────────────────────────────────────────────────
    // Kargo İşlemleri
    // ─────────────────────────────────────────────────

    @PostMapping("/cargo/ship/{id}")
    public String shipOrder(@PathVariable String id,
                            @RequestParam String company,
                            @RequestParam String senderCity,
                            @RequestParam String receiverCity,
                            @RequestParam double distanceKm,
                            @RequestParam(defaultValue = "false") boolean withInsurance,
                            @RequestParam(defaultValue = "false") boolean withFragile,
                            RedirectAttributes redirectAttrs) {
        if (!sessionManager.isLoggedIn()) return "redirect:/auth/login";
        try {
            Order order = orderService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sipariş bulunamadı: " + id));

            CargoProvider provider = CargoProviderFactory.getProvider(company);

            String tracking = cargoService.generateAndAssignTracking(
                provider, order, senderCity, receiverCity);
            double cost = cargoService.calculateShippingCost(
                provider, order, distanceKm, withInsurance, withFragile);

            orderService.shipOrder(id, tracking, cost);
            redirectAttrs.addFlashAttribute("successMessage",
                "Kargoya verildi! Takip: " + tracking + " | Ücret: " + String.format("%.2f", cost) + " TL");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Kargo hatası: " + e.getMessage());
        }
        return "redirect:/orders";
    }

    // ─────────────────────────────────────────────────
    // Ödeme Sayfası
    // ─────────────────────────────────────────────────

    @GetMapping("/payment/{orderId}")
    public String paymentForm(@PathVariable String orderId, Model model) {
        if (!sessionManager.isLoggedIn()) return "redirect:/auth/login";
        orderService.findById(orderId).ifPresent(o -> model.addAttribute("order", o));
        return "payment_form";
    }

    @PostMapping("/payment/process")
    public String processPayment(@RequestParam String orderId,
                                 @RequestParam String strategyBean,
                                 @RequestParam Map<String, String> details,
                                 RedirectAttributes redirectAttrs) {
        if (!sessionManager.isLoggedIn()) return "redirect:/auth/login";
        try {
            Order order = orderService.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Sipariş bulunamadı."));

            // Spring bean ismiyle stratejiyi seç (Strategy Pattern)
            PaymentStrategy strategy = applicationContext.getBean(strategyBean, PaymentStrategy.class);

            // Form'dan gelen "details[cardNumber]" gibi key'leri temizle
            Map<String, String> cleanDetails = details.entrySet().stream()
                .filter(e -> e.getKey().startsWith("details["))
                .collect(Collectors.toMap(
                    e -> e.getKey().replace("details[", "").replace("]", ""),
                    Map.Entry::getValue
                ));

            var result = paymentService.processPayment(order, strategy, cleanDetails);
            if (result.isSuccess()) {
                redirectAttrs.addFlashAttribute("successMessage",
                    "Ödeme başarılı! TX: " + result.getTransactionId());
                return "redirect:/orders";
            } else {
                redirectAttrs.addFlashAttribute("errorMessage",
                    "Ödeme başarısız: " + result.getErrorMessage());
                return "redirect:/payment/" + orderId;
            }
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Hata: " + e.getMessage());
            return "redirect:/payment/" + orderId;
        }
    }

    // ─────────────────────────────────────────────────
    // Hızlı sipariş oluşturma (Müşteri — ürün kataloğundan)
    // ─────────────────────────────────────────────────

    @PostMapping("/orders/create")
    public String createOrder(RedirectAttributes redirectAttrs) {
        if (!sessionManager.isLoggedIn()) return "redirect:/auth/login";
        try {
            User customer = sessionManager.getCurrentUser();
            Order order = orderService.createOrder(customer);
            redirectAttrs.addFlashAttribute("successMessage",
                "Sipariş oluşturuldu: " + order.getId());
            return "redirect:/payment/" + order.getId();
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Hata: " + e.getMessage());
            return "redirect:/products";
        }
    }

    @PostMapping("/orders/{id}/add-item")
    public String addItemToOrder(@PathVariable String id,
                                 @RequestParam String productId,
                                 @RequestParam int quantity,
                                 RedirectAttributes redirectAttrs) {
        if (!sessionManager.isLoggedIn()) return "redirect:/auth/login";
        try {
            orderService.addItemToOrder(id, productId, quantity);
            redirectAttrs.addFlashAttribute("successMessage", "Ürün siparişe eklendi.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Hata: " + e.getMessage());
        }
        return "redirect:/payment/" + id;
    }
}
