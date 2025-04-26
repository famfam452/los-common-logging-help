package com.ttb.los.loscommonlogging.config.model.packet;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.Map;

public record RequestPacket(@JsonAlias("req_body") String reqBody, @JsonAlias("req_body") Map<String, String[]> reqParams) {
}
