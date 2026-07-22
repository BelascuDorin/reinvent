package com.reinvent.consent.internal;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ConsentProperties.class)
class ConsentConfiguration {
}
