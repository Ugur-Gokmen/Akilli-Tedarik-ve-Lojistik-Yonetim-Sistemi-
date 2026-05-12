package com.project.ui;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import com.project.domain.product.Product;

@Component
@SessionScope
public class ShoppingCart implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<String, CartLine> lines = new LinkedHashMap<>();

    public void add(Product product, int quantity) {
        validateQuantity(quantity);
        CartLine existing = lines.get(product.getId());
        if (existing == null) {
            lines.put(product.getId(), new CartLine(product.getId(), product.getName(), product.getUnitPrice(), quantity));
            return;
        }
        existing.setQuantity(existing.getQuantity() + quantity);
    }

    public void updateQuantity(String productId, int quantity) {
        validateQuantity(quantity);
        CartLine existing = lines.get(productId);
        if (existing == null) {
            return;
        }
        existing.setQuantity(quantity);
    }

    public void remove(String productId) {
        lines.remove(productId);
    }

    public void clear() {
        lines.clear();
    }

    public Collection<CartLine> getLines() {
        return lines.values();
    }

    public boolean isEmpty() {
        return lines.isEmpty();
    }

    public int getTotalItems() {
        return lines.values().stream().mapToInt(CartLine::getQuantity).sum();
    }

    public double getSubtotal() {
        return lines.values().stream().mapToDouble(CartLine::getLineTotal).sum();
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Miktar pozitif olmalıdır.");
        }
    }

    public static class CartLine implements Serializable {
        private static final long serialVersionUID = 1L;

        private final String productId;
        private final String productName;
        private final double unitPrice;
        private int quantity;

        public CartLine(String productId, String productName, double unitPrice, int quantity) {
            this.productId = productId;
            this.productName = productName;
            this.unitPrice = unitPrice;
            this.quantity = quantity;
        }

        public String getProductId() {
            return productId;
        }

        public String getProductName() {
            return productName;
        }

        public double getUnitPrice() {
            return unitPrice;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public double getLineTotal() {
            return unitPrice * quantity;
        }
    }
}
