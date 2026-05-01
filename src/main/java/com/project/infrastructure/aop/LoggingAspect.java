package com.project.infrastructure.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.project.infrastructure.logger.SystemLogger;

/**
 * Aspect: Sistem geneli loglamayı (Cross-Cutting Concern) Proxy üzerinden yönetir.
 * 
 * <p>AOP sayesinde Controller ve Service metotlarının içine manuel "logger.info(...)"
 * yazılması gerekmez. Metotların çalışma süreleri ve başarı durumları otomatik loglanır.</p>
 * 
 * SUNUM NOTU: 
 * Bir fonksiyona giriş, çıkış veya hata fırlatma anlarını Loglamak her kodun 
 * tekrar tekrar yapması gereken ameleliktir (Boilerplate code). Biz burada 
 * AOP'nin `@Around` yapısını kullanarak bu yükü servislerden alıp tamamen
 * izole bir Logging katmanına devrettik.
 */
@Aspect
@Component
public class LoggingAspect {

    private final SystemLogger logger = SystemLogger.getInstance();

    /**
     * com.project.service paketi altındaki tüm sınıfların tüm public metotlarını çevreler (Around).
     * 
     * SUNUM NOTU: 
     * `@Around` anatosyonu metoda girilmeden hemen önce süreci dondurur, "proceed()" metodu
     * çağrılıncaya kadar bekleterek işlem süresini ölçme fırsatı tanır.
     */
    @Around("execution(public * com.project.service..*(..))")
    public Object logServiceExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        long start = System.currentTimeMillis();

        logger.info("[AOP Start] Metot başlıyor: " + methodName);

        try {
            // Metodun gerçek çalışmasını tetikle
            Object result = joinPoint.proceed();
            
            long executionTime = System.currentTimeMillis() - start;
            logger.info(String.format("[AOP Success] Metot '%s' başarıyla tamamlandı. Süre: %d ms", methodName, executionTime));
            
            return result;
        } catch (IllegalArgumentException e) {
            logger.error(String.format("[AOP Validation Error] Metot '%s' geçersiz argüman ile patladı: %s", methodName, e.getMessage()));
            throw e;
        } catch (Exception e) {
            logger.error(String.format("[AOP Error] Metot '%s' bir hata nedeniyle durdu: %s", methodName, e.getMessage()));
            throw e;
        }
    }
}
