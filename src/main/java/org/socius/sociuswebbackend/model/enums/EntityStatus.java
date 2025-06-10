package org.socius.sociuswebbackend.model.enums;

import lombok.Getter;

@Getter
public enum EntityStatus {
    active,
    inactive,
    deleted;

    private final String value;

    EntityStatus() {
        this.value = name().toLowerCase();
    }
}