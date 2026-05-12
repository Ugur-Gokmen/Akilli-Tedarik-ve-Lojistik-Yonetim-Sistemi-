# Design System Checklist

## 1) Head ve Asset Standardi
- [ ] Tum sayfalar `fragments/common_head.html` uzerinden head yukluyor.
- [ ] Tailwind config tek bir kaynaktan yonetiliyor.
- [ ] Font ve ikon baglantilari tek yerde tanimli.
- [ ] CDN baglantilari icin `preconnect` tanimli.

## 2) Layout ve Fragment Standardi
- [ ] Admin ekranlari `fragments/admin_layout.html` kullaniyor.
- [ ] Musteri ekranlari `fragments/customer_layout.html` kullaniyor.
- [ ] Tekrarlanan sidebar/header bloklari sayfa icinde kopyalanmiyor.
- [ ] Ortak topbar/sayfa basligi fragment ile cagriyor.

## 3) Form ve Erişilebilirlik
- [ ] Her input icin `id` + `label for` var.
- [ ] Yardim/hata metinleri icin `aria-describedby` kullaniliyor.
- [ ] Backend field-level hata mesajlari ilgili alanin altinda gorunuyor.
- [ ] Keyboard focus stilleri gorunur.

## 4) Durum Yonetimi (View-Model)
- [ ] Karmaşık CSS/durum hesaplari controller tarafinda hazirlaniyor.
- [ ] Template icinde string-uzun `th:classappend` azaltildi.
- [ ] Aksiyon gorunurlukleri bool alanlar ile beslendi.

## 5) Performans ve Gorsel Strateji
- [ ] Kritik ekranlarda harici gorseller yerine yerel/static asset kullaniliyor.
- [ ] Gorseller `/static/images/...` altindan servis ediliyor.
- [ ] Harici kaynaga bagimli ekranlar icin fallback plani var.

## 6) Islevsellik Hijyeni
- [ ] Islevsiz buton/linkler kaldirildi veya "yakinda" durumuna cekildi.
- [ ] Mock kontroller production ekraninda kalmadi.
- [ ] UI aksiyonlari gercek route/service'e bagli.
