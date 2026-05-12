package com.project.infrastructure.logger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SystemLogger Singleton Tests - QA Analysis")
class SystemLoggerSingletonTest {

    @Test
    @DisplayName("Multi-thread ortamda (Concurrency) Logger aynı instance'ı vermelidir")
    void singletonShouldBeThreadSafe() throws InterruptedException {
        int threads = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);

        SystemLogger[] instances = new SystemLogger[threads];

        for (int i = 0; i < threads; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    latch.await(); // Bütün threadler aynı anda fırlayacak (Race Condition simülasyonu)
                    instances[index] = SystemLogger.getInstance();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        latch.countDown(); // Go!
        doneLatch.await(); // Herkesin bitmesini bekle
        executor.shutdown();

        // Assert: Bütün dizideki referanslar tıpatıp aynı bellek adresini göstermeli
        SystemLogger firstInstance = instances[0];
        for (SystemLogger instance : instances) {
            assertThat(instance).isSameAs(firstInstance);
        }
    }
}
