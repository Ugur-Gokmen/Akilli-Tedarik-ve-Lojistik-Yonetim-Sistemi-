# UNIT TEST EXECUTION REPORT

Tarih: 12.05.2026  
Ortam: `Windows 10`, `Java 21`, `Maven Surefire`  
Komut: `.\mvnw.cmd test -DskipITs`

---

## CASE 1
**TEST CLASS:** `InventorySecurityWebMvcTest`  
**DESCRIPTION:** `/api/inventory/list` endpointinin kimlik dogrulama ve rol bazli erisim kurallarini dogrular.  
**EXPECTED:**  
- Login yoksa `401 Unauthorized`  
- Yanlis rol (CUSTOMER) ile `403 Forbidden`  
**RESULT (Console):**
```text
[INFO] Running com.project.api.controller.InventorySecurityWebMvcTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
```
**VERDICT:** PASS

## CASE 2
**TEST CLASS:** `OrderFlowIntegrationTest`  
**DESCRIPTION:** Uc uca siparis akisinin API seviyesinde (create -> add item -> pay -> approve -> prepare -> ship -> deliver) dogru calistigini test eder.  
**EXPECTED:** Tum adimlar HTTP 200 ile gecmeli ve durum gecisleri loglarda gorunmeli.  
**RESULT (Console):**
```text
[INFO] Running com.project.api.OrderFlowIntegrationTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
...
Sipariş ... durum değiştirdi -> ONAYLANDI
Sipariş ... durum değiştirdi -> HAZIRLANIYOR
Sipariş ... durum değiştirdi -> KARGODA
Sipariş ... durum değiştirdi -> TESLİM EDİLDİ
```
**VERDICT:** PASS

## CASE 3
**TEST CLASS:** `CargoPricingBuilderTest`  
**DESCRIPTION:** Builder + decorator kombinasyonlarinda fiyatlandirma etkisinin dogru uygulandigini dogrular (`insurance`, `fragile`, combined).  
**EXPECTED:** Beklenen tutarlar birebir hesaplanmali (100, 110, 135, 145).  
**RESULT (Console):**
```text
[INFO] Running com.project.domain.cargo.CargoPricingBuilderTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
```
**VERDICT:** PASS

## CASE 4
**TEST CLASS:** `ExtendedOrderStateTest`  
**DESCRIPTION:** Illegal state transition denemelerinde exception firlatildigini ve order state atomik olarak bozulmadigini test eder.  
**EXPECTED:** Illegal gecisler reddedilmeli, state korunmali.  
**RESULT (Console):**
```text
[INFO] Running com.project.domain.order.ExtendedOrderStateTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
```
**VERDICT:** PASS

## CASE 5
**TEST CLASS:** `OrderStateTest`  
**DESCRIPTION:** State Pattern akisinda happy path ve negatif senaryolari test eder (approve, cancel, ship, deliver, return).  
**EXPECTED:** Gecerli gecisler basarili, gecersiz gecisler `IllegalStateException`.  
**RESULT (Console):**
```text
[INFO] Running com.project.domain.order.OrderStateTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
```
**VERDICT:** PASS

## CASE 6
**TEST CLASS:** `ProductObserverTest`  
**DESCRIPTION:** Stok esik altina dustugunde observer'larin `update(product)` ile tetiklendigini test eder.  
**EXPECTED:** Kayitli tum observer'lara 1'er kez notification gitmeli.  
**RESULT (Console):**
```text
[INFO] Running com.project.domain.product.ProductObserverTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
```
**VERDICT:** PASS

## CASE 7
**TEST CLASS:** `CargoAdaptersTest`  
**DESCRIPTION:**  
- GlobalExpress zonal fiyat farki (boundary)  
- Negatif agirlik validasyonu (`-5.0`)  
**EXPECTED:**  
- Zone2 > Zone1 olmali  
- Negatif agirlikta `IllegalArgumentException` firlatilmali  
**RESULT (Console):**
```text
[INFO] Running com.project.infrastructure.adapter.CargoAdaptersTest
[ERROR] Tests run: 2, Failures: 1, Errors: 0, Skipped: 0
[ERROR] CargoAdaptersTest.negativeWeightShouldBeHandled
Expecting code to raise a throwable.
```
**VERDICT:** FAIL (Bug adayi)

## CASE 8
**TEST CLASS:** `SystemLoggerSingletonTest`  
**DESCRIPTION:** 100 thread altinda singleton instance'in thread-safe oldugunu dogrular.  
**EXPECTED:** Tum threadler ayni `SystemLogger` referansini almali.  
**RESULT (Console):**
```text
[INFO] Running com.project.infrastructure.logger.SystemLoggerSingletonTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
```
**VERDICT:** PASS

## CASE 9
**TEST CLASS:** `PaymentStrategyResolverTest`  
**DESCRIPTION:** Gecerli strategy key icin dogru strategy donusu, gecersiz key icin exception firlatma davranisini test eder.  
**EXPECTED:**  
- `"CREDIT_CARD"` -> ilgili strategy  
- `"UNKNOWN_METHOD"` -> `IllegalArgumentException`  
**RESULT (Console):**
```text
[INFO] Running com.project.infrastructure.resolver.PaymentStrategyResolverTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
```
**VERDICT:** PASS

## CASE 10
**TEST CLASS:** `ProductRepositoryTest`  
**DESCRIPTION:** `findBelowThreshold()` sorgusunun sadece threshold alti/esit urunleri getirdigini test eder.  
**EXPECTED:** `LOW` ve `VERY_LOW` donmeli, `OK` donmemeli.  
**RESULT (Console):**
```text
[INFO] Running com.project.repository.ProductRepositoryTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
```
**VERDICT:** PASS

## CASE 11
**TEST CLASS:** `InventoryServiceTest`  
**DESCRIPTION:** SKU uniqueness, restock validasyonu, observer ekleme ve bulunamayan urun hatalarini test eder.  
**EXPECTED:**  
- Duplicate SKU reject edilmeli  
- Restock pozitif olmali  
- Missing product durumunda hata alinmali  
**RESULT (Console):**
```text
[INFO] Running com.project.service.InventoryServiceTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
```
**VERDICT:** PASS

## CASE 12
**TEST CLASS:** `OrderServiceTest`  
**DESCRIPTION:** Payment delegation, urunsuz order approve engeli, stock-low event publish ve bos tracking numarasi validasyonunu test eder.  
**EXPECTED:** Is kurallari dogru sekilde enforce edilmeli.  
**RESULT (Console):**
```text
[INFO] Running com.project.service.OrderServiceTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
```
**VERDICT:** PASS

---

## TOPLAM TEST OZETI

```text
[ERROR] Tests run: 33, Failures: 1, Errors: 0, Skipped: 0
[INFO] BUILD FAILURE
```

- Toplam Senaryo: **33**
- Basarili: **32**
- Basarisiz: **1**
- Kritik Bulgu: `CargoAdaptersTest.negativeWeightShouldBeHandled`

---

## QA ANALIZI (15+ YIL TECRUBE PERSPEKTIFI)

1. Suite genel kalitesi yuksek; domain-state ve service seviyesinde testler anlamli ve davranis odakli.
2. Tek fail eden test, negatif input validasyonunda gercek bir bosluga isaret ediyor; test kodu degil, urun kodu incelenmeli.
3. `CargoAdapters` tarafinda agirlik ve mesafe icin guard clause standardi (`<= 0` reject) tum adapterlarda zorunlu hale getirilmeli.
4. Bu bug fix edildikten sonra regression icin ayni suite tekrar kosulmali; hedef `33/33 PASS`.
