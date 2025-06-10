package org.socius.sociuswebbackend.model.enums;

import lombok.Getter;

@Getter
public enum ConversationType {
    DIRECT,
    GROUP;

    private final String value;

    ConversationType() {
        this.value = name().toLowerCase();
    }
}
