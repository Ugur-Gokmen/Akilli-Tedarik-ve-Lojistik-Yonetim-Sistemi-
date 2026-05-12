package com.project.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Crypto crypto = new Crypto();
    private final Notifications notifications = new Notifications();
    private final Cargo cargo = new Cargo();
    private final Facilities facilities = new Facilities();
    private final Users users = new Users();

    public Crypto getCrypto() { return crypto; }
    public Notifications getNotifications() { return notifications; }
    public Cargo getCargo() { return cargo; }
    public Facilities getFacilities() { return facilities; }
    public Users getUsers() { return users; }

    public static class Crypto {
        /**
         * 1 BTC'nin TL karşılığı (simülasyon) — ör: 3500000.0
         */
        private double btcTryRate = 3_500_000.0;
        public double getBtcTryRate() { return btcTryRate; }
        public void setBtcTryRate(double btcTryRate) { this.btcTryRate = btcTryRate; }
    }

    public static class Notifications {
        private final Stock stock = new Stock();
        public Stock getStock() { return stock; }

        public static class Stock {
            private String lowRecipientEmail = "purchasing@sirket.com";
            public String getLowRecipientEmail() { return lowRecipientEmail; }
            public void setLowRecipientEmail(String lowRecipientEmail) { this.lowRecipientEmail = lowRecipientEmail; }
        }
    }

    public static class Cargo {
        /**
         * Yurtiçi kargo adaptörü için varsayılan çıkış şehri (demo parametresi).
         */
        private String yurticiOriginCity = "ISTANBUL";
        public String getYurticiOriginCity() { return yurticiOriginCity; }
        public void setYurticiOriginCity(String yurticiOriginCity) { this.yurticiOriginCity = yurticiOriginCity; }
    }

    public static class Facilities {
        /**
         * Demo amaçlı seed aktif mi?
         */
        private boolean seedEnabled = true;

        /**
         * Repo boşsa eklenecek demo tesisler.
         */
        private List<FacilitySeed> seed = new ArrayList<>();

        public boolean isSeedEnabled() { return seedEnabled; }
        public void setSeedEnabled(boolean seedEnabled) { this.seedEnabled = seedEnabled; }
        public List<FacilitySeed> getSeed() { return seed; }
        public void setSeed(List<FacilitySeed> seed) { this.seed = seed; }

        public static class FacilitySeed {
            private String name;
            private String location;
            private String type;
            private int currentCapacity;
            private int maxCapacity;
            private String manager;
            private String status;

            public String getName() { return name; }
            public void setName(String name) { this.name = name; }
            public String getLocation() { return location; }
            public void setLocation(String location) { this.location = location; }
            public String getType() { return type; }
            public void setType(String type) { this.type = type; }
            public int getCurrentCapacity() { return currentCapacity; }
            public void setCurrentCapacity(int currentCapacity) { this.currentCapacity = currentCapacity; }
            public int getMaxCapacity() { return maxCapacity; }
            public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }
            public String getManager() { return manager; }
            public void setManager(String manager) { this.manager = manager; }
            public String getStatus() { return status; }
            public void setStatus(String status) { this.status = status; }
        }
    }

    public static class Users {
        private String defaultFacility = "General Warehouse";
        public String getDefaultFacility() { return defaultFacility; }
        public void setDefaultFacility(String defaultFacility) { this.defaultFacility = defaultFacility; }
    }
}

