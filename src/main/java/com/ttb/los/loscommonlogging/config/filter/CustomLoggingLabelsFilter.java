package com.ttb.los.loscommonlogging.config.filter;

import lombok.Getter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.util.Map;
import java.util.function.Function;

@Getter
public class CustomLoggingLabelsFilter {
    private Function<ContentCachingRequestWrapper, Map<String, String>> requestWrapperfunction;
    private Function<ContentCachingResponseWrapper, Map<String, String>> responseWrapperMapFunction;

    public CustomLoggingLabelsFilter() {
    }
    public CustomLoggingLabelsFilter(Function<ContentCachingRequestWrapper, Map<String, String>> requestWrapperfunction) {
        this.requestWrapperfunction = requestWrapperfunction;
    }
    public CustomLoggingLabelsFilter(Function<ContentCachingRequestWrapper, Map<String, String>> requestWrapperfunction, Function<ContentCachingResponseWrapper, Map<String, String>> responseWrapperMapFunction) {
        this.requestWrapperfunction = requestWrapperfunction;
        this.responseWrapperMapFunction = responseWrapperMapFunction;
    }
}
