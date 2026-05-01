package com.project.infrastructure.factory;

import com.project.domain.cargo.CargoProvider;
import com.project.infrastructure.adapter.CargoAdapters;

/**
 * Kargo sağlayıcı fabrikası - Factory Method Pattern.
 *
 * <p>Desteklenen kargo firmalarını tek bir noktada üretir.
 * Kullanıcı kodu somut Adapter sınıf isimlerini bilmez.</p>
 */
public class CargoProviderFactory {

    public enum CargoCompany {
        ARAS,
        YURTICI,
        GLOBAL_EXPRESS,
        GLOBAL_EXPRESS_PREMIUM
    }

    /**
     * Belirtilen firmaya ait CargoProvider adaptörünü oluşturur.
     * REST controller'lar tarafından kullanılır.
     *
     * @param company Kargo şirketi enum değeri
     * @return CargoProvider adaptörü
     */
    public static CargoProvider getProvider(CargoCompany company) {
        return create(company);
    }

    /**
     * String isimden CargoProvider üretir (form POST'larından gelen değerler için).
     *
     * @param companyName Kargo şirketi adı (enum ismiyle eşleşmeli)
     * @return CargoProvider adaptörü
     */
    public static CargoProvider getProvider(String companyName) {
        try {
            return create(CargoCompany.valueOf(companyName.toUpperCase().replace(" ", "_")));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Geçersiz kargo şirketi: '" + companyName + "'. " +
                "Desteklenenler: ARAS, YURTICI, GLOBAL_EXPRESS, GLOBAL_EXPRESS_PREMIUM");
        }
    }

    /**
     * Enum dispatch ile somut adaptörü oluşturur. if-else/switch zinciri değil,
     * Java 14+ pattern switch kullanılmıştır — enum tam-liste garantisi sağlar.
     *
     * @param company Kargo şirketi
     * @return CargoProvider adaptörü
     */
    public static CargoProvider create(CargoCompany company) {
        return switch (company) {
            case ARAS                  -> new CargoAdapters.ArasCargoAdapter();
            case YURTICI               -> new CargoAdapters.YurticiCargoAdapter("ISTANBUL");
            case GLOBAL_EXPRESS        -> new CargoAdapters.GlobalExpressAdapter(false);
            case GLOBAL_EXPRESS_PREMIUM -> new CargoAdapters.GlobalExpressAdapter(true);
        };
    }

    /**
     * Mevcut tüm kargo şirketi isimlerini döner (form dropdown'ları için).
     *
     * @return Şirket adları dizisi
     */
    public static CargoCompany[] getAllCompanies() {
        return CargoCompany.values();
    }
}
