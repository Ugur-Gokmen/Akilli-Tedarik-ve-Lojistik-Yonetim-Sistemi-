package com.project.domain.order;

import com.project.infrastructure.logger.SystemLogger;

/**
 * Sipariş durumu implementasyonları - State Pattern.
 *
 * <p>7 durum: Pending → Approved → Preparing → Shipped → Delivered
 *                                                       ↘ Returned
 *                    ↘ Cancelled (Pending ve Approved'dan)</p>
 *
 * <p>Her iç sınıf hangi geçişlerin geçerli, hangilerinin yasak
 * olduğunu kendi içinde bilir. if-else / switch-case KULLANILMAZ.</p>
 */
public class OrderStates {

    private static final SystemLogger logger = SystemLogger.getInstance();

    // ─────────────────────────────────────────────────
    // 1. BEKLEMEDE (Pending)
    // ─────────────────────────────────────────────────

    public static class PendingState implements OrderState {

        @Override
        public void approve(Order order) {
            logger.info("Sipariş onaylandı: " + order.getId());
            order.setState(new ApprovedState());
        }

        @Override
        public void startPreparing(Order order) {
            throw new IllegalStateException("Önce siparişin onaylanması gerekiyor!");
        }

        @Override
        public void ship(Order order) {
            throw new IllegalStateException("Beklemedeki sipariş kargoya verilemez!");
        }

        @Override
        public void deliver(Order order) {
            throw new IllegalStateException("Beklemedeki sipariş teslim edilemez!");
        }

        @Override
        public void returnOrder(Order order) {
            throw new IllegalStateException("Beklemedeki sipariş iade edilemez!");
        }

        @Override
        public void cancel(Order order) {
            logger.info("Sipariş iptal edildi: " + order.getId());
            order.setState(new CancelledState());
        }

        @Override
        public void handleNext(Order order) {
            approve(order);
        }

        @Override
        public String getStateName() { return "BEKLEMEDE"; }
    }

    // ─────────────────────────────────────────────────
    // 2. ONAYLANDI (Approved)
    // ─────────────────────────────────────────────────

    public static class ApprovedState implements OrderState {

        @Override
        public void approve(Order order) {
            throw new IllegalStateException("Sipariş zaten onaylandı!");
        }

        @Override
        public void startPreparing(Order order) {
            logger.info("Sipariş hazırlanmaya başlandı: " + order.getId());
            order.setState(new PreparingState());
        }

        @Override
        public void ship(Order order) {
            throw new IllegalStateException("Sipariş henüz hazırlanmadı, kargoya verilemez!");
        }

        @Override
        public void deliver(Order order) {
            throw new IllegalStateException("Sipariş teslim edilemez — henüz hazırlanmadı!");
        }

        @Override
        public void returnOrder(Order order) {
            throw new IllegalStateException("Onaylanmış sipariş iade edilemez!");
        }

        @Override
        public void cancel(Order order) {
            logger.info("Onaylanmış sipariş iptal edildi: " + order.getId());
            order.setState(new CancelledState());
        }

        @Override
        public void handleNext(Order order) {
            startPreparing(order);
        }

        @Override
        public String getStateName() { return "ONAYLANDI"; }
    }

    // ─────────────────────────────────────────────────
    // 3. HAZIRLANIYOR (Preparing)
    // ─────────────────────────────────────────────────

    public static class PreparingState implements OrderState {

        @Override
        public void approve(Order order) {
            throw new IllegalStateException("Hazırlanan sipariş tekrar onaylanamaz!");
        }

        @Override
        public void startPreparing(Order order) {
            throw new IllegalStateException("Sipariş zaten hazırlanıyor!");
        }

        @Override
        public void ship(Order order) {
            logger.info("Sipariş kargoya verildi: " + order.getId());
            order.setState(new ShippedState());
        }

        @Override
        public void deliver(Order order) {
            throw new IllegalStateException("Sipariş kargoya verilmeden teslim edilemez!");
        }

        @Override
        public void returnOrder(Order order) {
            throw new IllegalStateException("Hazırlanan sipariş iade edilemez — önce kargoya verilmeli!");
        }

        @Override
        public void cancel(Order order) {
            throw new IllegalStateException(
                "Hazırlama aşamasındaki sipariş iptal edilemez! " +
                "Kargoya verdikten sonra iade talep edebilirsiniz.");
        }

        @Override
        public void handleNext(Order order) {
            ship(order);
        }

        @Override
        public String getStateName() { return "HAZIRLANIYOR"; }
    }

    // ─────────────────────────────────────────────────
    // 4. KARGODA (Shipped)
    // ─────────────────────────────────────────────────

    public static class ShippedState implements OrderState {

        @Override
        public void approve(Order order) {
            throw new IllegalStateException("Kargodaki sipariş onaylanamaz!");
        }

        @Override
        public void startPreparing(Order order) {
            throw new IllegalStateException("Kargodaki sipariş tekrar hazırlanamaz!");
        }

        @Override
        public void ship(Order order) {
            throw new IllegalStateException("Sipariş zaten kargoda!");
        }

        @Override
        public void deliver(Order order) {
            logger.logCriticalOperation("ORDER_DELIVERED",
                "Sipariş teslim edildi: " + order.getId());
            order.setState(new DeliveredState());
        }

        @Override
        public void returnOrder(Order order) {
            logger.logCriticalOperation("ORDER_RETURN",
                "İade başlatıldı: " + order.getId());
            order.setState(new ReturnedState());
        }

        @Override
        public void cancel(Order order) {
            throw new IllegalStateException(
                "Kargodaki sipariş iptal edilemez! " +
                "Teslim aldıktan sonra iade talep edebilirsiniz.");
        }

        @Override
        public void handleNext(Order order) {
            deliver(order);
        }

        @Override
        public String getStateName() { return "KARGODA"; }
    }

    // ─────────────────────────────────────────────────
    // 5. TESLİM EDİLDİ (Delivered) — Terminal
    // ─────────────────────────────────────────────────

    public static class DeliveredState implements OrderState {

        @Override public void approve(Order o)        { throwTerminal(); }
        @Override public void startPreparing(Order o) { throwTerminal(); }
        @Override public void ship(Order o)           { throwTerminal(); }
        @Override public void deliver(Order o)        { throwTerminal(); }
        @Override public void returnOrder(Order o)    { throwTerminal(); }
        @Override public void cancel(Order o)         { throwTerminal(); }
        @Override public void handleNext(Order o)     { throwTerminal(); }

        private void throwTerminal() {
            throw new IllegalStateException("Teslim edilmiş sipariş üzerinde işlem yapılamaz.");
        }

        @Override
        public String getStateName() { return "TESLİM EDİLDİ"; }
    }

    // ─────────────────────────────────────────────────
    // 6. İADE (Returned) — Terminal
    // ─────────────────────────────────────────────────

    public static class ReturnedState implements OrderState {

        @Override public void approve(Order o)        { throwTerminal(); }
        @Override public void startPreparing(Order o) { throwTerminal(); }
        @Override public void ship(Order o)           { throwTerminal(); }
        @Override public void deliver(Order o)        { throwTerminal(); }
        @Override public void cancel(Order o)         { throwTerminal(); }
        @Override public void handleNext(Order o)     { throwTerminal(); }

        @Override
        public void returnOrder(Order order) {
            throw new IllegalStateException("Sipariş zaten iade aşamasında!");
        }

        private void throwTerminal() {
            throw new IllegalStateException("İade aşamasındaki sipariş üzerinde işlem yapılamaz.");
        }

        @Override
        public String getStateName() { return "İADE"; }
    }

    // ─────────────────────────────────────────────────
    // 7. İPTAL (Cancelled) — Terminal
    // ─────────────────────────────────────────────────

    public static class CancelledState implements OrderState {

        @Override public void approve(Order o)        { throwTerminal(); }
        @Override public void startPreparing(Order o) { throwTerminal(); }
        @Override public void ship(Order o)           { throwTerminal(); }
        @Override public void deliver(Order o)        { throwTerminal(); }
        @Override public void returnOrder(Order o)    { throwTerminal(); }
        @Override public void handleNext(Order o)     { throwTerminal(); }

        @Override
        public void cancel(Order order) {
            throw new IllegalStateException("Sipariş zaten iptal edildi!");
        }

        private void throwTerminal() {
            throw new IllegalStateException("İptal edilmiş sipariş üzerinde işlem yapılamaz.");
        }

        @Override
        public String getStateName() { return "İPTAL EDİLDİ"; }
    }
}
