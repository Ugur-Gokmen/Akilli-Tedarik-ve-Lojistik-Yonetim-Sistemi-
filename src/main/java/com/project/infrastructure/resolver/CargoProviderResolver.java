package com.project.infrastructure.resolver;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.project.domain.cargo.CargoProvider;
import com.project.infrastructure.factory.CargoProviderFactory.CargoCompany;

@Component
public class CargoProviderResolver {

    private final Map<CargoCompany, CargoProvider> providers;

    public CargoProviderResolver(List<CargoProviderRegistration> registrations) {
        Objects.requireNonNull(registrations, "registrations");
        EnumMap<CargoCompany, CargoProvider> map = new EnumMap<>(CargoCompany.class);
        for (CargoProviderRegistration r : registrations) {
            CargoCompany key = Objects.requireNonNull(r.company(), "company");
            CargoProvider value = Objects.requireNonNull(r.provider(), "provider");
            map.put(key, value);
        }
        this.providers = Map.copyOf(map);
    }

    public CargoProvider resolve(CargoCompany company) {
        if (company == null) {
            throw new IllegalArgumentException("Kargo şirketi boş olamaz.");
        }
        CargoProvider provider = providers.get(company);
        if (provider == null) {
            String supported = providers.keySet().stream()
                .map(Enum::name)
                .sorted()
                .collect(Collectors.joining(", "));
            throw new IllegalArgumentException("Geçersiz kargo şirketi: " + company + ". Desteklenenler: " + supported);
        }
        return provider;
    }
}

