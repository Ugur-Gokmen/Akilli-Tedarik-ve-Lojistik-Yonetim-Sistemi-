package com.project.infrastructure.logger;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Singleton Logger - Sistem genelinde tek bir loglama örneği.
 *
 * <p>Singleton Pattern: Tüm uygulama boyunca sadece BİR tane SystemLogger nesnesi
 * oluşturulur. Bu sayede log dosyasına erişim çakışmaları önlenir ve
 * merkezi bir loglama noktası sağlanır.</p>
 *
 * <p>Thread-safe implementasyon için "double-checked locking" kullanılmıştır.</p>
 */
public class SystemLogger {

    // Tek örnek - volatile ile thread-safe görünürlük sağlanır
    private static volatile SystemLogger instance;

    private static final String LOG_FILE = "system.log";
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private PrintWriter writer;

    /**
     * Private constructor - dışarıdan new ile oluşturulamaz.
     */
    private SystemLogger() {
        try {
            // true = append mode: log dosyası her başlatmada üzerine yazılmaz
            this.writer = new PrintWriter(new FileWriter(LOG_FILE, true), true);
        } catch (IOException e) {
            System.err.println("[LOGGER HATA] Log dosyası açılamadı: " + e.getMessage());
        }
    }

    /**
     * Singleton örneğini döner. Double-checked locking ile thread-safe.
     *
     * @return SystemLogger tek örneği
     */
    public static SystemLogger getInstance() {
        if (instance == null) {
            synchronized (SystemLogger.class) {
                if (instance == null) {
                    instance = new SystemLogger();
                }
            }
        }
        return instance;
    }

    /**
     * Bilgi seviyesinde log yazar.
     *
     * @param message Log mesajı
     */
    public void info(String message) {
        log("INFO", message);
    }

    /**
     * Uyarı seviyesinde log yazar.
     *
     * @param message Log mesajı
     */
    public void warn(String message) {
        log("WARN", message);
    }

    /**
     * Hata seviyesinde log yazar.
     *
     * @param message Log mesajı
     */
    public void error(String message) {
        log("ERROR", message);
    }

    /**
     * Kritik işlem logu - stok değişimi, ödeme onayı vb.
     *
     * @param operation  Operasyon adı
     * @param details    Detaylar
     */
    public void logCriticalOperation(String operation, String details) {
        log("CRITICAL", "[" + operation + "] " + details);
    }

    private void log(String level, String message) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String logEntry = String.format("[%s] [%s] %s", timestamp, level, message);

        // Hem konsola hem dosyaya yaz
        System.out.println(logEntry);
        if (writer != null) {
            writer.println(logEntry);
        }
    }

    /**
     * Log yazıcıyı kapatır. Uygulama kapanırken çağrılmalı.
     */
    public void close() {
        if (writer != null) {
            writer.close();
        }
    }
}
