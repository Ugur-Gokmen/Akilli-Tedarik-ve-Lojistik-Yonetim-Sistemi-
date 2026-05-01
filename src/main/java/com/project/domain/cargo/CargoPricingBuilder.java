package com.project.domain.cargo;

import com.project.infrastructure.logger.SystemLogger;

// ─────────────────────────────────────────────────────────
// Concrete Component - Temel Kargo Fiyatlandırması
// ─────────────────────────────────────────────────────────

/**
 * Temel kargo fiyatlandırması - CargoProvider'dan base fiyat alır.
 * Decorator Pattern'in Concrete Component'i.
 */
class BaseCargoPrice implements CargoPricingComponent {

    private final CargoProvider provider;

    public BaseCargoPrice(CargoProvider provider) {
        this.provider = provider;
    }

    @Override
    public double calculatePrice(double weightKg, double distanceKm) {
        return provider.calculateBasePrice(weightKg, distanceKm);
    }

    @Override
    public String getDescription() {
        return provider.getCompanyName() + " - Temel Kargo Ücreti";
    }
}

// ─────────────────────────────────────────────────────────
// Abstract Decorator
// ─────────────────────────────────────────────────────────

/**
 * Soyut kargo fiyat dekoratörü.
 * Tüm somut dekoratörler bu sınıftan türer.
 */
abstract class CargoPricingDecorator implements CargoPricingComponent {

    protected final CargoPricingComponent wrapped;

    protected CargoPricingDecorator(CargoPricingComponent wrapped) {
        this.wrapped = wrapped;
    }
}

// ─────────────────────────────────────────────────────────
// Concrete Decorators
// ─────────────────────────────────────────────────────────

/**
 * Sigortalı gönderi dekoratörü - kargo ücretine %1.5 sigorta bedeli ekler.
 */
class InsuranceDecorator extends CargoPricingDecorator {

    private static final double INSURANCE_RATE = 0.015;

    public InsuranceDecorator(CargoPricingComponent wrapped) {
        super(wrapped);
    }

    @Override
    public double calculatePrice(double weightKg, double distanceKm) {
        double basePrice = wrapped.calculatePrice(weightKg, distanceKm);
        double insuranceFee = basePrice * INSURANCE_RATE;
        return basePrice + Math.max(insuranceFee, 10.0);
    }

    @Override
    public String getDescription() {
        return wrapped.getDescription() + " + Sigortalı Gönderi";
    }
}

/**
 * Kırılacak eşya koruması dekoratörü - sabit 35 TL ek ücret ekler.
 */
class FragileProtectionDecorator extends CargoPricingDecorator {

    private static final double FRAGILE_FEE = 35.0;

    public FragileProtectionDecorator(CargoPricingComponent wrapped) {
        super(wrapped);
    }

    @Override
    public double calculatePrice(double weightKg, double distanceKm) {
        return wrapped.calculatePrice(weightKg, distanceKm) + FRAGILE_FEE;
    }

    @Override
    public String getDescription() {
        return wrapped.getDescription() + " + Kırılacak Eşya Koruması (+35 TL)";
    }
}

/**
 * Hızlı teslimat dekoratörü - fiyata %60 zam uygular.
 */
class ExpressDeliveryDecorator extends CargoPricingDecorator {

    private static final double EXPRESS_MULTIPLIER = 1.60;

    public ExpressDeliveryDecorator(CargoPricingComponent wrapped) {
        super(wrapped);
    }

    @Override
    public double calculatePrice(double weightKg, double distanceKm) {
        return wrapped.calculatePrice(weightKg, distanceKm) * EXPRESS_MULTIPLIER;
    }

    @Override
    public String getDescription() {
        return wrapped.getDescription() + " + Hızlı Teslimat (%60 ek ücret)";
    }
}

/**
 * Soğuk zincir nakliyesi dekoratörü - km başına 0.35 TL ekler.
 */
class ColdChainDecorator extends CargoPricingDecorator {

    private static final double COLD_CHAIN_PER_KM = 0.35;

    public ColdChainDecorator(CargoPricingComponent wrapped) {
        super(wrapped);
    }

    @Override
    public double calculatePrice(double weightKg, double distanceKm) {
        return wrapped.calculatePrice(weightKg, distanceKm) + (distanceKm * COLD_CHAIN_PER_KM);
    }

    @Override
    public String getDescription() {
        return wrapped.getDescription() + " + Soğuk Zincir Nakliyesi (0.35 TL/km)";
    }
}

// ─────────────────────────────────────────────────────────
// Builder - Fluent API ile Decorator zinciri oluşturur
// ─────────────────────────────────────────────────────────

/**
 * Kargo fiyatlandırma zinciri oluşturucu - Strategy + Decorator Pattern.
 *
 * <pre>
 * Kullanım:
 *   CargoPricingComponent pricing = new CargoPricingBuilder(arasAdapter)
 *       .withInsurance()
 *       .withFragileProtection()
 *       .build();
 *   double price = pricing.calculatePrice(5.0, 450.0);
 * </pre>
 */
public class CargoPricingBuilder {

    private CargoPricingComponent component;
    private final SystemLogger logger = SystemLogger.getInstance();

    /**
     * @param provider Kargo sağlayıcısı (Aras, Yurtiçi veya GlobalExpress adaptörü)
     */
    public CargoPricingBuilder(CargoProvider provider) {
        this.component = new BaseCargoPrice(provider);
    }

    /** Sigortalı gönderi ekler. */
    public CargoPricingBuilder withInsurance() {
        this.component = new InsuranceDecorator(component);
        return this;
    }

    /** Kırılacak eşya koruması ekler. */
    public CargoPricingBuilder withFragileProtection() {
        this.component = new FragileProtectionDecorator(component);
        return this;
    }

    /** Hızlı teslimat ekler. */
    public CargoPricingBuilder withExpressDelivery() {
        this.component = new ExpressDeliveryDecorator(component);
        return this;
    }

    /** Soğuk zincir nakliyesi ekler. */
    public CargoPricingBuilder withColdChain() {
        this.component = new ColdChainDecorator(component);
        return this;
    }

    /**
     * Fiyatlandırma bileşenini döner.
     *
     * @return Yapılandırılmış CargoPricingComponent
     */
    public CargoPricingComponent build() {
        logger.info("Kargo fiyatlandırması yapılandırıldı: " + component.getDescription());
        return component;
    }
}
