package com.project.domain.order;

import com.project.infrastructure.logger.SystemLogger;

/**
 * Sipariş durumu implementasyonları - State Pattern.
 *
 * <p>Her iç sınıf, siparişin belirli bir aşamasını temsil eder.
 * Her durum; hangi geçişlerin geçerli, hangilerinin yasak olduğunu bilir.
 * Yasak geçişler exception fırlatır - if-else/switch KULLANILMAZ.</p>
 */
public class OrderStates {

    private static final SystemLogger logger = SystemLogger.getInstance();

    // ─────────────────────────────────────────────────
    // 1. BEKLEMEDE (Pending)
    // ─────────────────────────────────────────────────

    /**
     * Beklemede durumu - sipariş sisteme alınmış ama henüz onaylanmamış.
     * İzin verilen: approve, cancel
     */
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
        public String getStateName() { return "BEKLEMEde"; }
    }

    // ─────────────────────────────────────────────────
    // 2. ONAYLANDI (Approved)
    // ─────────────────────────────────────────────────

    /**
     * Onaylandı durumu - sipariş onaylanmış, hazırlık bekleniyor.
     * İzin verilen: startPreparing, cancel
     */
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
            throw new IllegalStateException("Sipariş teslim edilemez - henüz hazırlanmadı!");
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
        public String getStateName() { return "ONAYLANDI"; }
    }

    // ─────────────────────────────────────────────────
    // 3. HAZIRLANIYOR (Preparing)
    // ─────────────────────────────────────────────────

    /**
     * Hazırlanıyor durumu - depo aktif olarak siparişi paketliyor.
     * İzin verilen: ship (iptal artık mümkün değil)
     */
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
            throw new IllegalStateException("Hazırlanan sipariş iade edilemez - önce kargoya verilmeli!");
        }

        @Override
        public void cancel(Order order) {
            throw new IllegalStateException(
                "Hazırlama aşamasındaki sipariş iptal edilemez! Kargoya verdikten sonra iade talep edebilirsiniz.");
        }

        @Override
        public String getStateName() { return "HAZIRLANIYOR"; }
    }

    // ─────────────────────────────────────────────────
    // 4. KARGODA (Shipped)
    // ─────────────────────────────────────────────────

    /**
     * Kargoda durumu - ürün kargo şirketinde taşınıyor.
     * İzin verilen: deliver, returnOrder
     * İzin verilmeyen: cancel (kargodaki ürün iptal YAPILAMAZ)
     */
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
            logger.logCriticalOperation("ORDER_DELIVERED", "Sipariş teslim edildi: " + order.getId());
            order.setState(new DeliveredState());
        }

        @Override
        public void returnOrder(Order order) {
            logger.logCriticalOperation("ORDER_RETURN", "İade başlatıldı: " + order.getId());
            order.setState(new ReturnedState());
        }

        @Override
        public void cancel(Order order) {
            // KARGODAKI ÜRÜN İPTAL EDİLEMEZ - Ödev şartnamesi gereği
            throw new IllegalStateException(
                "Kargodaki sipariş iptal edilemez! Ürünü teslim aldıktan sonra iade talep edebilirsiniz. " +
                "Doğrudan iade için returnOrder() metodunu kullanın.");
        }

        @Override
        public String getStateName() { return "KARGODA"; }
    }

    // ─────────────────────────────────────────────────
    // 5. TESLİM EDİLDİ (Delivered)
    // ─────────────────────────────────────────────────

    /**
     * Teslim edildi durumu - sipariş başarıyla tamamlandı.
     * Terminal durum - hiçbir geçiş yapılamaz.
     */
    public static class DeliveredState implements OrderState {

        @Override
        public void approve(Order order) { throwTerminal(); }

        @Override
        public void startPreparing(Order order) { throwTerminal(); }

        @Override
        public void ship(Order order) { throwTerminal(); }

        @Override
        public void deliver(Order order) { throwTerminal(); }

        @Override
        public void returnOrder(Order order) { throwTerminal(); }

        @Override
        public void cancel(Order order) { throwTerminal(); }

        private void throwTerminal() {
            throw new IllegalStateException("Teslim edilmiş sipariş üzerinde işlem yapılamaz.");
        }

        @Override
        public String getStateName() { return "TESLİM EDİLDİ"; }
    }

    // ─────────────────────────────────────────────────
    // 6. İADE (Returned)
    // ─────────────────────────────────────────────────

    /**
     * İade durumu - ürün iade sürecinde.
     * Terminal durum.
     */
    public static class ReturnedState implements OrderState {

        @Override
        public void approve(Order order) { throwTerminal(); }

        @Override
        public void startPreparing(Order order) { throwTerminal(); }

        @Override
        public void ship(Order order) { throwTerminal(); }

        @Override
        public void deliver(Order order) { throwTerminal(); }

        @Override
        public void returnOrder(Order order) {
            throw new IllegalStateException("Sipariş zaten iade aşamasında!");
        }

        @Override
        public void cancel(Order order) { throwTerminal(); }

        private void throwTerminal() {
            throw new IllegalStateException("İade aşamasındaki sipariş üzerinde işlem yapılamaz.");
        }

        @Override
        public String getStateName() { return "İADE"; }
    }

    // ─────────────────────────────────────────────────
    // 7. İPTAL EDİLDİ (Cancelled)
    // ─────────────────────────────────────────────────

    /**
     * İptal edildi durumu - terminal durum.
     */
    public static class CancelledState implements OrderState {

        @Override
        public void approve(Order order) { throwTerminal(); }

        @Override
        public void startPreparing(Order order) { throwTerminal(); }

        @Override
        public void ship(Order order) { throwTerminal(); }

        @Override
        public void deliver(Order order) { throwTerminal(); }

        @Override
        public void returnOrder(Order order) { throwTerminal(); }

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
