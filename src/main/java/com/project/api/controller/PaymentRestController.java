package com.project.api.controller;

import com.project.api.dto.PaymentRequest;
import com.project.domain.payment.PaymentResult;
import com.project.service.PaymentApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentRestController {

    private final PaymentApplicationService paymentApplicationService;

    public PaymentRestController(PaymentApplicationService paymentApplicationService) {
        this.paymentApplicationService = paymentApplicationService;
    }

    /**
     * Sipariş ödemesini gerçekleştirir.
     * Strategy Pattern bean ismiyle dinamik olarak tetiklenir.
     */
    @PostMapping("/process")
    public ResponseEntity<PaymentResult> process(@RequestBody PaymentRequest request) {
        PaymentResult result = paymentApplicationService.processPayment(
            request.orderId(),
            request.strategyBeanName(),
            request.details());

        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
}
