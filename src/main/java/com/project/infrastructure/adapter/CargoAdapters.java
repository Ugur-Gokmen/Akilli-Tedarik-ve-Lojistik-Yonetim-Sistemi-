package com.project.infrastructure.adapter;

import com.project.domain.cargo.CargoProvider;

/**
 * Kargo şirketi adaptörleri - Adapter Pattern.
 *
 * <p>Her adaptör, uyumsuz 3. taraf API'yi CargoProvider arayüzüne dönüştürür.
 * Sistem her zaman CargoProvider ile konuşur — alt yapıdaki gerçek API
 * kodun hiçbir yerinde görünmez. Bağımlılıklar ters çevrilmiştir (DIP).</p>
 */
public class CargoAdapters {

    // ─────────────────────────────────────────────────
    // Aras Kargo Adaptörü
    // ─────────────────────────────────────────────────

    /**
     * ArasCargoApi → CargoProvider dönüşümü.
     *
     * <p>Adaptör, Aras'ın float/int parametreli API'sini
     * double tabanlı CargoProvider arayüzüne çevirir.</p>
     */
    public static class ArasCargoAdapter implements CargoProvider {

        // Adaptee - sarmaladığımız 3. taraf nesne
        private final ThirdPartyCargoAPIs.ArasCargoApi arasApi;

        public ArasCargoAdapter() {
            this.arasApi = new ThirdPartyCargoAPIs.ArasCargoApi();
        }

        @Override
        public String generateTrackingNumber(String orderId, String senderCity, String receiverCity) {
            // Aras'ın createShipment metoduna delege et + tip dönüşümü
            return arasApi.createShipment(senderCity, receiverCity, orderId);
        }

        @Override
        public double calculateBasePrice(double weightKg, double distanceKm) {
            // double → float/int dönüşümü - Adapter'ın temel görevi
            return arasApi.getShipmentCost((float) weightKg, (int) distanceKm);
        }

        @Override
        public String getCompanyName() { return "Aras Kargo"; }

        @Override
        public int estimateDeliveryDays(double distanceKm) {
            return arasApi.calculateDeliveryTime((int) distanceKm);
        }
    }

    // ─────────────────────────────────────────────────
    // Yurtiçi Kargo Adaptörü
    // ─────────────────────────────────────────────────

    /**
     * YurticiCargoApi → CargoProvider dönüşümü.
     *
     * <p>Yurtiçi'nin desi tabanlı fiyatlandırmasını kg tabanlı
     * CargoProvider arayüzüne çevirir.</p>
     */
    public static class YurticiCargoAdapter implements CargoProvider {

        private final ThirdPartyCargoAPIs.YurticiCargoApi yurticiApi;
        private final String defaultRegion;

        /**
         * @param defaultRegion Varsayılan bölge (şehir adı veya bölge kodu)
         */
        public YurticiCargoAdapter(String defaultRegion) {
            this.yurticiApi = new ThirdPartyCargoAPIs.YurticiCargoApi();
            this.defaultRegion = defaultRegion;
        }

        @Override
        public String generateTrackingNumber(String orderId, String senderCity, String receiverCity) {
            return yurticiApi.generateBarcode(orderId, receiverCity);
        }

        @Override
        public double calculateBasePrice(double weightKg, double distanceKm) {
            // Kg → Desi dönüşümü: 1 desi ≈ 1 kg (hacimsel ağırlık basitleştirmesi)
            // Gerçek uygulamada: desi = (en × boy × yükseklik) / 3000
            double desi = weightKg;
            String receiverRegion = distanceKm < 300 ? defaultRegion : "UZAK_BOLGE";
            return yurticiApi.computePrice(desi, defaultRegion, receiverRegion);
        }

        @Override
        public String getCompanyName() { return "Yurtiçi Kargo"; }

        @Override
        public int estimateDeliveryDays(double distanceKm) {
            boolean sameRegion = distanceKm < 300;
            String estimate = yurticiApi.getExpectedDelivery(sameRegion);
            // "1-2 iş günü" formatından sayı çıkar
            return sameRegion ? 2 : 3;
        }
    }

    // ─────────────────────────────────────────────────
    // GlobalExpress Adaptörü
    // ─────────────────────────────────────────────────

    /**
     * GlobalExpressApi → CargoProvider dönüşümü.
     *
     * <p>GlobalExpress'in zone tabanlı, premium fiyatlandırmasını
     * standart arayüze adapte eder.</p>
     */
    public static class GlobalExpressAdapter implements CargoProvider {

        private final ThirdPartyCargoAPIs.GlobalExpressApi globalApi;
        private final boolean expressMode;
        private static long shipmentCounter = 1000L;

        /**
         * @param expressMode Hızlı teslimat modu aktif mi?
         */
        public GlobalExpressAdapter(boolean expressMode) {
            this.globalApi = new ThirdPartyCargoAPIs.GlobalExpressApi();
            this.expressMode = expressMode;
        }

        @Override
        public String generateTrackingNumber(String orderId, String senderCity, String receiverCity) {
            long id = ++shipmentCounter;
            return globalApi.issueTrackingCode("TR", "TR", id);
        }

        @Override
        public double calculateBasePrice(double weightKg, double distanceKm) {
            // Mesafeyi zone'a çevir: her 200 km = 1 zone
            int zone = Math.max(1, (int) Math.ceil(distanceKm / 200.0));
            return globalApi.quotePremiumPrice(weightKg, zone, expressMode);
        }

        @Override
        public String getCompanyName() {
            return "GlobalExpress" + (expressMode ? " (Express)" : "");
        }

        @Override
        public int estimateDeliveryDays(double distanceKm) {
            int zone = Math.max(1, (int) Math.ceil(distanceKm / 200.0));
            return globalApi.getDeliveryDays(zone, expressMode);
        }
    }
}
