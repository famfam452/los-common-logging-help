package com.ttb.los.loscommonlogging.config.filter;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.ttb.los.loscommonlogging.config.model.packet.Packet;
import com.ttb.los.loscommonlogging.config.model.packet.RequestPacket;
import com.ttb.los.loscommonlogging.config.model.packet.ResponsePacket;
import com.ttb.los.loscommonlogging.config.properties.CommonIgnoreUrlProperties;
import com.ttb.los.loscommonlogging.config.properties.CommonLoggingLabelsProperties;
import com.ttb.los.loscommonlogging.utils.converter.ObjectMapConverter;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@Slf4j
public class CommonPacketRequestFilter implements Filter {

    @Value("${spring.application.name}")
    private String serviceName;

    @Autowired
    private CommonIgnoreUrlProperties commonIgnoreUrlProperties;
    @Autowired
    private CommonLoggingLabelsProperties commonLoggingLabelsProperties;
    @Autowired
    private CustomLoggingLabelsFilter customLoggingLabelsFilter;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (servletRequest instanceof HttpServletRequest httpServletRequest && servletResponse instanceof HttpServletResponse httpServletResponse) {
            if (shouldNotFilter(httpServletRequest)) {
                filterChain.doFilter(servletRequest, servletResponse);
            } else {
                doFilterInternal(filterChain, httpServletRequest, httpServletResponse);
            }
        } else {
            throw new ServletException("PacketRequestFilter just supports HTTP requests");
        }
    }

    private boolean shouldNotFilter(HttpServletRequest httpServletRequest) {
        return commonIgnoreUrlProperties.getPrefixes().contains(httpServletRequest.getServletPath()) || commonIgnoreUrlProperties.getPrefixes().stream().anyMatch(s -> httpServletRequest.getServletPath().startsWith(s));
    }

    private void doFilterInternal(FilterChain filterChain, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(httpServletRequest);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(httpServletResponse);
        Packet packet = capturePacket(requestWrapper);
        RequestPacket requestPacket = captureRequestPacket(requestWrapper);
        ResponsePacket responsePacket = captureResponsePacket(responseWrapper);

        Map<String, String> headerMapper = headerMapper(httpServletRequest, commonLoggingLabelsProperties.getHeaders());
        MDC.setContextMap(headerMapper);

        Map<String, String> packetMapper = ObjectMapConverter.convert(packet);
        MDC.setContextMap(packetMapper);

        Map<String, String> requestPacketMapper = ObjectMapConverter.convert(requestPacket);
        MDC.setContextMap(requestPacketMapper);

        Map<String, String> responsePacketMapper = ObjectMapConverter.convert(responsePacket);

        Function<ContentCachingRequestWrapper, Map<String, String>> requestWrapperfunction = customLoggingLabelsFilter.getRequestWrapperfunction();
        Map<String, String> customRequestMapper = null;
        if (requestWrapperfunction != null) {
            customRequestMapper = requestWrapperfunction.apply(requestWrapper);
            MDC.setContextMap(customRequestMapper);
        }

        Function<ContentCachingResponseWrapper, Map<String, String>> responseWrapperMapFunction = customLoggingLabelsFilter.getResponseWrapperMapFunction();
        Map<String, String> customResponseMapper = null;
        try {
            printRequestLog();
            filterChain.doFilter(requestWrapper, responseWrapper);
            MDC.setContextMap(responsePacketMapper);
            if (responseWrapperMapFunction != null) {
                customResponseMapper = responseWrapperMapFunction.apply(responseWrapper);
                MDC.setContextMap(customResponseMapper);
            }
        } finally {
            responseWrapper.copyBodyToResponse();
            printResponseLog();
            MDCRemover(headerMapper.keySet());
            MDCRemover(packetMapper.keySet());
            MDCRemover(requestPacketMapper.keySet());
            MDCRemover(responsePacketMapper.keySet());
            if (requestWrapperfunction != null && customRequestMapper != null) {
                MDCRemover(customRequestMapper.keySet());
            }
            if (responseWrapperMapFunction != null && customResponseMapper != null) {
                MDCRemover(customResponseMapper.keySet());
            }
        }
    }

    private Packet capturePacket(ContentCachingRequestWrapper requestWrapper) {
        String uri = requestWrapper.getRequestURI();
        String service = serviceName;
        String method = requestWrapper.getMethod();
        return new Packet(uri, service, method);
    }

    private RequestPacket captureRequestPacket(ContentCachingRequestWrapper requestWrapper) {
        String reqBody = null;
        if (requestWrapper.getContentLength() > 0) {
            reqBody = new String(requestWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);
        }
        return new RequestPacket(reqBody, requestWrapper.getParameterMap());
    }

    private ResponsePacket captureResponsePacket(ContentCachingResponseWrapper responseWrapper) {
        String responseBody = new String(responseWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);
        return new ResponsePacket(responseBody);
    }

    private Map<String, String> headerMapper(HttpServletRequest httpServletRequest, Class<?> clazz) {
        Map<String, String> map = new HashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            String key = field.getName();
            JsonAlias jsonAlias = field.getAnnotation(JsonAlias.class);
            if (jsonAlias != null) {
                if (jsonAlias.value().length != 0) {
                    key = jsonAlias.value()[0];
                }
            }
            String value = httpServletRequest.getHeader(key);
            if (value != null) {
                map.put(key, value);
            }
        }
        return map;
    }

    private Map<String, String> headerMapper(HttpServletRequest httpServletRequest, Collection<String> keys) {
        Map<String, String> map = new HashMap<>();
        for (String key : keys) {
            String value = httpServletRequest.getHeader(key);
            if (value != null) {
                map.put(key, value);
            }
        }
        return map;
    }

    private void MDCRemover(Collection<String> keys) {
        for (String key : keys) {
            MDC.remove(key);
        }
    }

    private void printRequestLog() {
        log.info("request");
    }
    private void printResponseLog() {
        log.info("response");
    }
}
