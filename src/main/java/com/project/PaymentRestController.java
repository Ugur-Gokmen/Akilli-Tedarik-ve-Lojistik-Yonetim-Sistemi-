package com.project.api;

import com.project.api.dto.PaymentRequest;
import com.project.domain.order.Order;
import com.project.domain.payment.PaymentResult;
import com.project.domain.payment.PaymentStrategy;
import com.project.service.OrderService;
import com.project.service.Services.PaymentService;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentRestController {

    private final PaymentService paymentService;
    private final OrderService orderService;
    private final ApplicationContext context;

    public PaymentRestController(PaymentService paymentService, OrderService orderService, ApplicationContext context) {
        this.paymentService = paymentService;
        this.orderService = orderService;
        this.context = context;
    }

    /**
     * Sipariş ödemesini gerçekleştirir.
     * Strategy Pattern bean ismiyle dinamik olarak tetiklenir.
     */
    @PostMapping("/process")
    public ResponseEntity<PaymentResult> process(@RequestBody PaymentRequest request) {
        Order order = orderService.findById(request.orderId())
                .orElseThrow(() -> new IllegalArgumentException("Sipariş bulunamadı."));

        // Bean ismine göre çalışma zamanında stratejiyi seç (Strategy Pattern)
        PaymentStrategy strategy = context.getBean(request.strategyBeanName(), PaymentStrategy.class);

        // Ödemeyi işle ve sonucu dön
        PaymentResult result = paymentService.processPayment(order, strategy, request.details());

        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
}
