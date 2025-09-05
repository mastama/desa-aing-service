package com.yolifay.domain.port.out;

import java.time.Duration;

public interface RateLimiterPortOut {

    /** Return true jika percobaan diizinkan
     * sekaligus menaikan counter percobaan dan set TTL window
     */
    boolean allow(String key, int maxAttempts, Duration window);

    // TTL sisa dalam detik, atau -1 jika tidak ada TTL
    long ttlSeconds(String key);

    // Resets bucket the rate limiter for the given key (hapus counter)
    void reset(String key);
}
