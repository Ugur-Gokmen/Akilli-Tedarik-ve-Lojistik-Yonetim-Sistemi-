package com.project.domain.order;

/**
 * Sipariş durumu arayüzü - State Pattern.
 *
 * <p>Her durum hangi geçişlerin geçerli, hangilerinin yasak olduğunu
 * kendi içinde bilir. Yasak geçişler exception fırlatır.
 * Order sınıfında hiçbir if-else veya switch-case bulunmaz.</p>
 *
 * <p>Yeni durum eklemek: sadece bu arayüzü implement etmek yeterlidir
 * → Open/Closed Principle.</p>
 */
public interface OrderState {

    /** Siparişi onaylar: Beklemede → Onaylandı */
    void approve(Order order);

    /** Hazırlığı başlatır: Onaylandı → Hazırlanıyor */
    void startPreparing(Order order);

    /** Kargoya verir: Hazırlanıyor → Kargoda */
    void ship(Order order);

    /** Teslim edildi işaretler: Kargoda → Teslim Edildi */
    void deliver(Order order);

    /** İade başlatır: Kargoda → İade */
    void returnOrder(Order order);

    /** İptal eder (yalnızca belirli aşamalarda) */
    void cancel(Order order);

    /**
     * Sıradaki mantıksal duruma otomatik geçiş.
     *
     * <p>Order.nextState() tarafından çağrılır. Her durum kendi sıradaki
     * geçişini tanımlar. Terminal durumlarda (Teslim, İade, İptal)
     * exception fırlatır.</p>
     *
     * @param order İşlenecek sipariş
     */
    void handleNext(Order order);

    /**
     * @return Mevcut durumun görünen adı
     */
    String getStateName();
}
