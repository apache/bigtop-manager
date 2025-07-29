package org.apache.bigtop.manager.server.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum PropertyAction {
    ADD,
    UPDATE,
    DELETE;

    @JsonCreator
    public static PropertyAction fromString(String value) {
        return PropertyAction.valueOf(value.toUpperCase());
    }
}
