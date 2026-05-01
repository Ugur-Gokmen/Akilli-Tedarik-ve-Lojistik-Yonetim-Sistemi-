package com.project.infrastructure.factory;

import com.project.domain.cargo.CargoProvider;
import com.project.domain.cargo.ArasCargoProvider;    // KRİTİK IMPORT
import com.project.domain.cargo.YurticiCargoProvider; // KRİTİK IMPORT

public class CargoProviderFactory {

    public enum CargoCompany { ARAS, YURTICI }

    public static CargoProvider getProvider(CargoCompany company) {
        return switch (company) {
            case ARAS -> new ArasCargoProvider();
            case YURTICI -> new YurticiCargoProvider();
            default -> throw new IllegalArgumentException("Desteklenmeyen kargo firması!");
        };
    }
}
