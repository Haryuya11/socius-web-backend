package org.socius.sociuswebbackend.model.enums;

import lombok.Getter;

@Getter
public enum PeriodStatus {
    active,
    inactive;

    private final String value;

    PeriodStatus() {
        this.value = name().toLowerCase();
    }

}
