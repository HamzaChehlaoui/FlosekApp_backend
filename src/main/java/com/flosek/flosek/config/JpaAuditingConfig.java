package com.flosek.flosek.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing Configuration
 * Enables automatic population of @CreatedDate and @LastModifiedDate fields
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
    // Configuration class to enable JPA auditing
    // This allows @CreatedDate and @LastModifiedDate annotations to work
    // automatically
}
