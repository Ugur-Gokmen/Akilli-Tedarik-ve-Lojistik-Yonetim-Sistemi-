package com.project.infrastructure.adapter;

/**
 * Gerçek kargo şirketi API'lerini simüle eden sınıflar - Adaptee'ler.
 *
 * <p>Bu sınıflar üçüncü taraf kütüphaneleri/SDK'ları temsil eder.
 * Her birinin metot isimleri, parametreleri ve dönüş tipleri birbirinden farklıdır.
 * Bunları doğrudan kullanamayız çünkü CargoProvider arayüzüyle uyumsuzlar.</p>
 */
public class ThirdPartyCargoAPIs {

    // ─────────────────────────────────────────────────
    // ARAS Kargo - 3. taraf SDK (uyumsuz API)
    // ─────────────────────────────────────────────────

    /**
     * Aras Kargo'nun gerçek API'si (simülasyon).
     * Kendi takip numarası formatı ve fiyatlandırması vardır.
     */
    public static class ArasCargoApi {

        /**
         * Aras'ın kendi takip numarası formatı: "ARAS-[şehir kodu]-[timestamp]"
         */
        public String createShipment(String fromCity, String toCity, String referenceNo) {
            String cityCode = toCity.substring(0, Math.min(3, toCity.length())).toUpperCase();
            return String.format("ARAS-%s-%d-%s", cityCode, System.currentTimeMillis() % 100000, referenceNo);
        }

        /**
         * Aras fiyatlandırması: kg başına sabit ücret + mesafe katsayısı.
         * Metot adı ve parametreler bizim arayüzümüzden FARKLI.
         */
        public double getShipmentCost(float weightKg, int distanceKm) {
            double base = 25.0;
            double perKg = 4.50;
            double perKm = 0.08;
            return base + (weightKg * perKg) + (distanceKm * perKm);
        }

        /** Aras teslimat tahmini: km / 300 (günlük ortalama rota hızı) */
        public int calculateDeliveryTime(int km) {
            return Math.max(1, (int) Math.ceil(km / 300.0));
        }
    }

    // ─────────────────────────────────────────────────
    // Yurtiçi Kargo - 3. taraf SDK (uyumsuz API)
    // ─────────────────────────────────────────────────

    /**
     * Yurtiçi Kargo'nun gerçek API'si (simülasyon).
     * Tamamen farklı bir API sözleşmesi.
     */
    public static class YurticiCargoApi {

        /**
         * Yurtiçi'nin takip formatı: "YK[8 haneli sayı]"
         */
        public String generateBarcode(String orderRef, String destination) {
            int hash = Math.abs((orderRef + destination).hashCode()) % 100000000;
            return String.format("YK%08d", hash);
        }

        /**
         * Yurtiçi fiyatlandırması: desi bazlı (1 desi = 100 cm³ / 6).
         * Farklı parametre tipi ve hesaplama mantığı.
         */
        public double computePrice(double desi, String fromRegion, String toRegion) {
            double base = 20.0;
            boolean isSameRegion = fromRegion.equalsIgnoreCase(toRegion);
            double regionMultiplier = isSameRegion ? 1.0 : 1.35;
            return (base + desi * 3.8) * regionMultiplier;
        }

        /** Yurtiçi teslimat süresi */
        public String getExpectedDelivery(boolean sameRegion) {
            return sameRegion ? "1-2 iş günü" : "2-3 iş günü";
        }
    }

    // ─────────────────────────────────────────────────
    // GlobalExpress - 3. taraf SDK (uyumsuz API)
    // ─────────────────────────────────────────────────

    /**
     * GlobalExpress'in gerçek API'si (simülasyon).
     * Uluslararası standartlarda, yine farklı bir yapı.
     */
    public static class GlobalExpressApi {

        /**
         * GlobalExpress takip formatı: "GE-[ülke]-[UUID benzeri]"
         */
        public String issueTrackingCode(String originCountry, String destCountry, long shipmentId) {
            return String.format("GE-%s-%s-%d", originCountry.toUpperCase(),
                destCountry.toUpperCase(), shipmentId % 1000000);
        }

        /**
         * GlobalExpress premium fiyatlandırması: ağırlık + zone ücretleri.
         */
        public double quotePremiumPrice(double weightKg, int zone, boolean expressDelivery) {
            double base = 45.0;
            double zoneCharge = zone * 15.0;
            double expressCharge = expressDelivery ? 50.0 : 0.0;
            return base + (weightKg * 6.0) + zoneCharge + expressCharge;
        }

        /** GlobalExpress zone bazlı teslimat süresi */
        public int getDeliveryDays(int zone, boolean express) {
            return express ? Math.max(1, zone) : Math.max(2, zone * 2);
        }
    }
}
