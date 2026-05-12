package com.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.project.config.AppProperties;
import com.project.domain.cargo.CargoProvider;
import com.project.infrastructure.adapter.CargoAdapters;
import com.project.infrastructure.factory.CargoProviderFactory.CargoCompany;
import com.project.infrastructure.resolver.CargoProviderRegistration;

@Configuration
public class CargoProviderConfig {

    private final AppProperties appProperties;

    public CargoProviderConfig(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Bean
    public CargoProvider arasCargoProvider() {
        return new CargoAdapters.ArasCargoAdapter();
    }

    @Bean
    public CargoProvider yurticiCargoProvider() {
        return new CargoAdapters.YurticiCargoAdapter(appProperties.getCargo().getYurticiOriginCity());
    }

    @Bean
    public CargoProvider globalExpressCargoProvider() {
        return new CargoAdapters.GlobalExpressAdapter(false);
    }

    @Bean
    public CargoProvider globalExpressPremiumCargoProvider() {
        return new CargoAdapters.GlobalExpressAdapter(true);
    }

    @Bean
    public CargoProviderRegistration arasCargoProviderRegistration(CargoProvider arasCargoProvider) {
        return new CargoProviderRegistration() {
            @Override public CargoCompany company() { return CargoCompany.ARAS; }
            @Override public CargoProvider provider() { return arasCargoProvider; }
        };
    }

    @Bean
    public CargoProviderRegistration yurticiCargoProviderRegistration(CargoProvider yurticiCargoProvider) {
        return new CargoProviderRegistration() {
            @Override public CargoCompany company() { return CargoCompany.YURTICI; }
            @Override public CargoProvider provider() { return yurticiCargoProvider; }
        };
    }

    @Bean
    public CargoProviderRegistration globalExpressCargoProviderRegistration(CargoProvider globalExpressCargoProvider) {
        return new CargoProviderRegistration() {
            @Override public CargoCompany company() { return CargoCompany.GLOBAL_EXPRESS; }
            @Override public CargoProvider provider() { return globalExpressCargoProvider; }
        };
    }

    @Bean
    public CargoProviderRegistration globalExpressPremiumCargoProviderRegistration(CargoProvider globalExpressPremiumCargoProvider) {
        return new CargoProviderRegistration() {
            @Override public CargoCompany company() { return CargoCompany.GLOBAL_EXPRESS_PREMIUM; }
            @Override public CargoProvider provider() { return globalExpressPremiumCargoProvider; }
        };
    }
}

