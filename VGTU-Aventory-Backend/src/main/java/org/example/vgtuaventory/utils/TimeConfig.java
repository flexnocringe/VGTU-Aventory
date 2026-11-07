package org.example.vgtuaventory.utils;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Single application-wide time source. Services take a Clock instead of calling now() directly,
 * so tests can pass Clock.fixed(...) and get the same result on any day.
 */
@Configuration
public class TimeConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
