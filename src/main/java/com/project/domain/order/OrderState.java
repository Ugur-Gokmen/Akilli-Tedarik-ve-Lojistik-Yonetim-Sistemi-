package com.project.domain.order;

/**
 * Sipariş durumu arayüzü - State Pattern.
 *
 * <p>State Pattern: Siparişin her aşaması (Beklemede, Onaylandı, Hazırlanıyor,
 * Kargoda, Teslim Edildi, İade) ayrı bir sınıfta tanımlanır. Her durum
 * hangi geçişlerin yapılabileceğini ve hangilerinin yasak olduğunu kendi içinde
 * bilir. Bu sayede uzun if-else/switch zincirlerinden tamamen kaçınılır.</p>
 *
 * <p>Yeni bir durum eklemek için sadece bu arayüzü implement etmek yeterlidir
 * → Open/Closed Principle.</p>
 */
public interface OrderState {
	void handleNext(Order order);
    /**
     * Siparişi onaylar (Beklemede → Onaylandı).
     *
     * @param order İşlenecek sipariş
     */
    void approve(Order order);

    /**
     * Sipariş hazırlığını başlatır (Onaylandı → Hazırlanıyor).
     *
     * @param order İşlenecek sipariş
     */
    void startPreparing(Order order);

    /**
     * Siparişi kargoya verir (Hazırlanıyor → Kargoda).
     *
     * @param order İşlenecek sipariş
     */
    void ship(Order order);

    /**
     * Siparişi teslim edildi olarak işaretler (Kargoda → Teslim Edildi).
     *
     * @param order İşlenecek sipariş
     */
    void deliver(Order order);

    /**
     * Siparişi iade sürecine alır (Kargoda → İade).
     *
     * @param order İşlenecek sipariş
     */
    void returnOrder(Order order);

    /**
     * Siparişi iptal eder (yalnızca belirli aşamalarda mümkün).
     *
     * @param order İşlenecek sipariş
     */
    void cancel(Order order);

    /**
     * Mevcut durumun adını döner.
     *
     * @return Durum adı
     */
    String getStateName();
}
