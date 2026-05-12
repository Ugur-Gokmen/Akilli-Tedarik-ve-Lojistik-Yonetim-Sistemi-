# Teslim Dokümantasyonu (Güncel Kod)

Bu doküman, proje kodunun son haliyle uyumlu olacak şekilde UML diyagramlarını, desen (pattern) gerekçelendirmesini ve test raporunu içerir.

## UML Diyagramları

### Use Case Diagram

```mermaid
flowchart LR
  %% Actors
  C[Customer]
  S[Staff]
  A[Admin]

  %% Use cases
  UC1((Login/Logout))
  UC2((Ürünleri Görüntüle))
  UC3((Sipariş Oluştur))
  UC4((Siparişe Ürün Ekle))
  UC5((Ödeme Yap))
  UC6((Kendi Siparişlerim))
  UC7((Sipariş Yönetimi\n(approve/prepare/ship/deliver)))
  UC8((Envanter Yönetimi\n(ürün ekle, stok artır)))
  UC9((Analitik/Raporlar))
  UC10((Tesisler))
  UC11((Kullanıcı Yönetimi))

  C --> UC1
  C --> UC2
  C --> UC3
  C --> UC4
  C --> UC5
  C --> UC6

  S --> UC1
  S --> UC7
  S --> UC8
  S --> UC9
  S --> UC10

  A --> UC1
  A --> UC7
  A --> UC8
  A --> UC9
  A --> UC10
  A --> UC11
```

### Class Diagram (Özet)

```mermaid
classDiagram
  direction LR

  class Order {
    -String id
    -User customer
    -List~OrderItem~ items
    -OrderState currentState
    +approve()
    +startPreparing()
    +ship()
    +deliver()
    +returnOrder()
    +cancel()
  }

  class OrderState {
    <<interface>>
    +approve(Order)
    +startPreparing(Order)
    +ship(Order)
    +deliver(Order)
    +returnOrder(Order)
    +cancel(Order)
    +handleNext(Order)
    +getStateName() String
  }

  class OrderStates {
    <<factory>>
  }

  class OrderService
  class OrderManagementService
  class AnalyticsService
  class InventoryService

  class PaymentApplicationService
  class ShippingApplicationService

  class PaymentStrategy {
    <<interface>>
    +pay(Order, amount, details) PaymentResult
    +getMethodName() String
  }

  class PaymentStrategyResolver

  class CargoProvider {
    <<interface>>
    +calculateBasePrice(weightKm, distanceKm) double
    +generateTrackingNumber(orderId, sender, receiver) String
  }

  class CargoProviderResolver
  class CargoProviderRegistration {
    <<interface>>
    +company() CargoCompany
    +provider() CargoProvider
  }

  class AppProperties

  Order --> OrderState : delegates
  OrderState <|.. OrderStates.PendingState
  OrderState <|.. OrderStates.ApprovedState
  OrderState <|.. OrderStates.PreparingState
  OrderState <|.. OrderStates.ShippedState
  OrderState <|.. OrderStates.DeliveredState
  OrderState <|.. OrderStates.ReturnedState
  OrderState <|.. OrderStates.CancelledState

  OrderService --> Order
  PaymentApplicationService --> OrderService
  PaymentApplicationService --> PaymentStrategyResolver
  PaymentStrategyResolver --> PaymentStrategy

  ShippingApplicationService --> OrderService
  ShippingApplicationService --> CargoProviderResolver
  CargoProviderResolver --> CargoProviderRegistration
  CargoProviderRegistration --> CargoProvider

  AppProperties ..> PaymentApplicationService : config
  AppProperties ..> ShippingApplicationService : config
```

### Sequence Diagram (Happy Path Order Flow)

Bu akış, testte de otomatik doğrulanan REST senaryosudur.

```mermaid
sequenceDiagram
  autonumber
  actor Client
  participant OrderAPI as OrderRestController (/api/orders)
  participant OrderSvc as OrderService
  participant PayApp as PaymentApplicationService
  participant StratRes as PaymentStrategyResolver
  participant PaySvc as Services.PaymentService
  participant ShipApp as ShippingApplicationService
  participant CargoRes as CargoProviderResolver
  participant CargoSvc as Services.CargoService

  Client->>OrderAPI: POST /create (User)
  OrderAPI->>OrderSvc: createOrder(user)
  OrderSvc-->>OrderAPI: Order(id)

  Client->>OrderAPI: POST /{id}/items (productId, qty)
  OrderAPI->>OrderSvc: addItemToOrder(id, productId, qty)

  Client->>OrderAPI: POST /{id}/payment (strategyBeanName, details)
  OrderAPI->>PayApp: processPayment(id, strategyBeanName, details)
  PayApp->>StratRes: resolve(strategyBeanName)
  StratRes-->>PayApp: PaymentStrategy
  PayApp->>OrderSvc: pay(id, strategy, details)
  OrderSvc->>PaySvc: processPayment(order, strategy, details)
  PaySvc-->>OrderSvc: PaymentResult(success)
  OrderSvc-->>PayApp: PaymentResult(success)
  PayApp-->>OrderAPI: PaymentResult(success)

  Client->>OrderAPI: PUT /{id}/approve
  OrderAPI->>OrderSvc: approveOrder(id)

  Client->>OrderAPI: POST /{id}/prepare
  OrderAPI->>OrderSvc: startPreparing(id)

  Client->>OrderAPI: POST /{id}/ship (company, cities, distance,...)
  OrderAPI->>ShipApp: shipOrder(id, company, ...)
  ShipApp->>CargoRes: resolve(company)
  CargoRes-->>ShipApp: CargoProvider
  ShipApp->>CargoSvc: generateAndAssignTracking(provider, order, ...)
  ShipApp->>CargoSvc: calculateShippingCost(provider, order, ...)
  ShipApp->>OrderSvc: shipOrder(id, tracking, cost)
  ShipApp-->>OrderAPI: ShipmentResult(tracking,cost)

  Client->>OrderAPI: PUT /{id}/deliver
  OrderAPI->>OrderSvc: deliverOrder(id)
```

### State Diagram (Order)

```mermaid
stateDiagram-v2
  [*] --> BEKLEMEDE

  BEKLEMEDE --> ONAYLANDI: approve (paid==true)
  BEKLEMEDE --> IPTAL_EDILDI: cancel

  ONAYLANDI --> HAZIRLANIYOR: startPreparing
  ONAYLANDI --> IPTAL_EDILDI: cancel

  HAZIRLANIYOR --> KARGODA: ship

  KARGODA --> TESLIM_EDILDI: deliver
  KARGODA --> IADE: returnOrder

  TESLIM_EDILDI --> [*]
  IADE --> [*]
  IPTAL_EDILDI --> [*]
```

### Activity Diagram (Ship Order – Orchestration)

```mermaid
flowchart TD
  A([Başla]) --> B[Order'ı yükle]
  B --> C{Sipariş var mı?}
  C -- Hayır --> X([Hata: Sipariş bulunamadı])
  C -- Evet --> D[CargoProviderResolver ile provider seç]
  D --> E[TrackingNo üret & order'a ata]
  E --> F[ShippingCost hesapla]
  F --> G[OrderService.shipOrder(id, tracking, cost)]
  G --> H([Bitti: ShipmentResult])
```

## Pattern Tablosu (Kanıtlı)

| Problem | Seçilen Desen | Neden / Kazanım | Kanıt (Sınıf/Dosya) |
|---|---|---|---|
| Sipariş durum geçişlerinde if/switch karmaşası | **State** | Geçiş kuralları state sınıflarında; `Order` if/switch içermez | `com.project.domain.order.Order`, `OrderState`, `OrderStates` |
| Ödeme yöntemleri runtime’da değişebilsin | **Strategy** | Yeni ödeme eklemek mevcut kodu bozmadan yeni sınıf eklemekle mümkün | `PaymentStrategy`, `CreditCardPayment`, `BankTransferPayment`, `CryptoPayment` |
| Bean adına göre strateji seçimini controller’dan soyutlama | **Resolver (OCP destekli)** | `ApplicationContext.getBean` yayılmıyor; tek noktadan doğrulama/hata mesajı | `PaymentStrategyResolver`, `PaymentApplicationService` |
| Kargo firması seçimini switch ile büyütmek | **Registry/Resolver (Factory yerine kayıt)** | Yeni provider “yeni bean + registration” ile eklenir; merkezi switch yok | `CargoProviderResolver`, `CargoProviderRegistration`, `CargoProviderConfig` |
| Kargo fiyatına opsiyonel ek hizmet ekleme (sigorta/kırılgan) | **Decorator** | Kombinasyon patlaması olmadan zincirlenebilir fiyatlandırma | `CargoPricingBuilder` ve dekoratör sınıfları |
| Karmaşık Composite ürün oluşturma | **Builder** | Parametreleri okunur şekilde set; telescoping constructor yok | `CompositeProduct.Builder` |
| Stok eşik altına düşünce bildirimlerin gevşek bağlı olması | **Observer (Event Listener)** | Ürün/servis ile bildirim modülleri bağımsız; Pub/Sub | `StockLowEvent`, `EmailStockNotifier`, `InAppStockNotifier` |
| Service metotlarında tekrar eden loglama | **AOP (Cross-cutting)** | Zaman/başarı/hata logları merkezi | `LoggingAspect` |
| Yetkilendirme kontrollerinin if-else ile yayılması | **AOP + Annotation** | `@RequireRole` ile metot seviyesinde declarative auth | `RequireRole`, `SecurityAspect` |

## Test Raporu

### Kapsam (Özet)

- **Unit**
  - Sipariş iş kuralları ve durumlar: `OrderStateTest`
  - Service: `OrderServiceTest`, `InventoryServiceTest`
  - Kargo fiyat dekoratörleri: `CargoPricingBuilderTest`
- **WebMvcTest**
  - Yetkilendirme: `InventorySecurityWebMvcTest` (401/403)
- **DataJpaTest**
  - Repository sorguları: `ProductRepositoryTest` (`findBelowThreshold`)
- **Integration**
  - Happy-path order flow: `OrderFlowIntegrationTest` (create→addItem→pay→approve→prepare→ship→deliver)

### Örnek Çıktılar (Surefire Özeti)

- `OrderFlowIntegrationTest`: `Tests run: 1, Failures: 0, Errors: 0`
- `InventorySecurityWebMvcTest`: `Tests run: 2, Failures: 0, Errors: 0`
- `ProductRepositoryTest`: `Tests run: 1, Failures: 0, Errors: 0`
- `CargoPricingBuilderTest`: `Tests run: 1, Failures: 0, Errors: 0`

### Çalıştırma Komutu

```bash
./mvnw.cmd test
```

### Riskler / Notlar

- **Spring Security generated password uyarısı**: Test loglarında görülebilir; `SecurityFilterChain` tanımlı olsa da Spring bazı test context’lerinde default kullanıcı servislerini aktive edebiliyor. Üretim için ayrı konfig gerekebilir.
- **Dialect uyarıları**: Test profilinde H2 PostgreSQL mode kullanılıyor; bazı Hibernate dialect uyarıları log’da görülebilir.
- **Profil davranışı**: `dev` profili demo seed içerir (`DemoDataSeeder`), `test/prod` profillerinde seed kapalıdır.

