package org.socius.sociuswebbackend.model.enums;

public enum PeriodStatus {
    active,
    inactive;

    private final String value;

    PeriodStatus() {
        this.value = name().toLowerCase();
    }

    public String getValue() {
        return value;
    }
}
