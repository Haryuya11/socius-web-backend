package org.socius.sociuswebbackend.model.enums;

import lombok.Getter;

@Getter
public enum MessageType {
    TEXT,
    IMAGE,
    VIDEO,
    AUDIO,
    FILE,
    SYSTEM;

    private final String value;

    MessageType() {
        this.value = name().toLowerCase();
    }
}
