package com.devtiro.realestate.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.elasticsearch.config.EnableElasticsearchAuditing;

@Configuration
@EnableElasticsearchAuditing
public class AuditConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return new SpringSecurityAuditorAware();
    }

}
