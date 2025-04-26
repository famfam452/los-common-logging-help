package com.ttb.los.loscommonlogging.config.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Set;

@Getter
@Configuration
@ConfigurationProperties(prefix = "los.logging.common.ignore")
@NoArgsConstructor
@AllArgsConstructor
public class CommonIgnoreUrlProperties {
    Set<String> urls;
    List<String> prefixes;
}
