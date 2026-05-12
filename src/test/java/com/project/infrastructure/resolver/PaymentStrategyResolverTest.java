package com.project.infrastructure.resolver;

import com.project.domain.payment.PaymentStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("Payment Strategy Resolver Tests - QA Analysis")
class PaymentStrategyResolverTest {

    @Mock
    private PaymentStrategy creditCardStrategy;

    @Mock
    private PaymentStrategy cryptoStrategy;

    @Test
    @DisplayName("Resolver doğru string parametresine göre doğru Strategy'yi dönmelidir")
    void shouldResolveCorrectPaymentStrategy() {
        Map<String, PaymentStrategy> mockMap = Map.of(
            "CREDIT_CARD", creditCardStrategy,
            "CRYPTO", cryptoStrategy
        );
        PaymentStrategyResolver resolver = new PaymentStrategyResolver(mockMap);

        PaymentStrategy resolved = resolver.resolve("CREDIT_CARD");

        assertThat(resolved).isSameAs(creditCardStrategy);
    }

    @Test
    @DisplayName("Geçersiz payment türü verildiğinde güvenlik amacıyla hata fırlatılmalıdır")
    void shouldThrowExceptionForInvalidStrategy() {
        Map<String, PaymentStrategy> mockMap = Map.of("CREDIT_CARD", creditCardStrategy);
        PaymentStrategyResolver resolver = new PaymentStrategyResolver(mockMap);

        assertThatThrownBy(() -> resolver.resolve("UNKNOWN_METHOD"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Geçersiz ödeme stratejisi: 'UNKNOWN_METHOD'");
    }
}
