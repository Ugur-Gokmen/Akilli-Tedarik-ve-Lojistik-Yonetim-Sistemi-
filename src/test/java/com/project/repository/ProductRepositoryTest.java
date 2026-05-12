package com.project.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.project.domain.product.SimpleProduct;

@DataJpaTest
@ActiveProfiles("test")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void findBelowThreshold_should_return_products_at_or_below_threshold() {
        SimpleProduct ok = new SimpleProduct("OK", "SKU-OK", 10, 1, 10, 5);
        SimpleProduct low = new SimpleProduct("LOW", "SKU-LOW", 10, 1, 10, 5);
        SimpleProduct veryLow = new SimpleProduct("VERY_LOW", "SKU-VLOW", 10, 1, 10, 5);

        low.decreaseStock(5);      // 10 -> 5  (<= threshold)
        veryLow.decreaseStock(8);  // 10 -> 2  (<= threshold)

        productRepository.save(ok);
        productRepository.save(low);
        productRepository.save(veryLow);

        var result = productRepository.findBelowThreshold();
        assertThat(result).extracting(p -> p.getName())
            .contains("LOW", "VERY_LOW")
            .doesNotContain("OK");
    }
}

