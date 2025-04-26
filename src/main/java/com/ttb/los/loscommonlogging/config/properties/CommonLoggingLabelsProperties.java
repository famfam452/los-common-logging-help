package com.ttb.los.loscommonlogging.config.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;

@Getter
@Configuration
@ConfigurationProperties(prefix = "los.logging.common.labels")
@NoArgsConstructor
@AllArgsConstructor
public class CommonLoggingLabelsProperties {
    ArrayList<String> headers;
}
