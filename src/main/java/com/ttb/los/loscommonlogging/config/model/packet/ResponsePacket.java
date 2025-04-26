package com.ttb.los.loscommonlogging.config.model.packet;

import com.fasterxml.jackson.annotation.JsonAlias;

public record ResponsePacket(@JsonAlias("res_body") String respBody) {
}
