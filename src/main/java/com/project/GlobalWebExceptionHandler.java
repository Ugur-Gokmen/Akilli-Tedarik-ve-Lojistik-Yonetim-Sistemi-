package com.project.infrastructure.aop;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Global Hata Yakalayıcı - MVC Katmanı için.
 * * SUNUM NOTU: 
 * Bu sınıf @ControllerAdvice anotasyonu sayesinde tüm Controller'ları dinler.
 * SecurityAspect içinde fırlatılan SecurityException'ı yakalar
 * ve kullanıcıyı teknik bir hata sayfası yerine Login sayfasına yönlendirir.
 */
@ControllerAdvice
public class GlobalWebExceptionHandler {

    @ExceptionHandler(SecurityException.class)
    public String handleSecurityException(SecurityException ex, RedirectAttributes redirectAttributes) {
        // Hata mesajını geçici olarak (Flash Attribute) Redirect sürecine ekliyoruz
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        
        // Kullanıcıyı login sayfasına yönlendiriyoruz
        return "redirect:/auth/login";
    }
}
