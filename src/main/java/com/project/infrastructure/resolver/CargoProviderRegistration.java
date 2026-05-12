package com.project.infrastructure.resolver;

import com.project.domain.cargo.CargoProvider;
import com.project.infrastructure.factory.CargoProviderFactory.CargoCompany;

/**
 * Yeni kargo sağlayıcı eklemek için yalnızca yeni bir registration bean'i eklemek yeterlidir.
 */
public interface CargoProviderRegistration {
    CargoCompany company();
    CargoProvider provider();
}

