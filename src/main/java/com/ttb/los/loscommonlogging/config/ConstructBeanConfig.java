package com.ttb.los.loscommonlogging.config;

import com.ttb.los.loscommonlogging.config.filter.CustomLoggingLabelsFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConstructBeanConfig {

    @Bean
    public CustomLoggingLabelsFilter customLoggingLabelsFilter() {
        return new CustomLoggingLabelsFilter();
    }
}
