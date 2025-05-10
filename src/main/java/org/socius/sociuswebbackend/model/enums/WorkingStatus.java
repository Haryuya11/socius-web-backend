package org.socius.sociuswebbackend.model.enums;

import lombok.Getter;

@Getter
public enum WorkingStatus {
    active,
    inactive,
    terminated;

    private final String value;

    WorkingStatus() {
        this.value = name().toLowerCase();
    }

}
