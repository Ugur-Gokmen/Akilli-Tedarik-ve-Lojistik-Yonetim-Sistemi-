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

    /**
     * Desteklenen kargo firmaları.
     */
    public enum CargoCompany {
        ARAS,
        YURTICI,
        GLOBAL_EXPRESS,
        GLOBAL_EXPRESS_PREMIUM
    }

    /**
     * Belirtilen firmaya ait CargoProvider adaptörünü oluşturur.
     *
     * @param company Kargo şirketi
     * @return CargoProvider adaptörü
     */
    public static CargoProvider create(CargoCompany company) {
        return switch (company) {
            case ARAS ->
                new CargoAdapters.ArasCargoAdapter();
            case YURTICI ->
                new CargoAdapters.YurticiCargoAdapter("ISTANBUL");
            case GLOBAL_EXPRESS ->
                new CargoAdapters.GlobalExpressAdapter(false);
            case GLOBAL_EXPRESS_PREMIUM ->
                new CargoAdapters.GlobalExpressAdapter(true);
        };
        // Not: Bu switch Java 14+ pattern switch'i olup enum tam-liste garantisi
        // sağladığı için default branch gereksizdir. if-else ZİNCİRİ değildir.
    }
}
