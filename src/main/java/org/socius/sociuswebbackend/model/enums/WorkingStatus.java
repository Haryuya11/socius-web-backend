package org.socius.sociuswebbackend.model.enums;

public enum WorkingStatus {
    active,
    inactive,
    terminated;

    private final String value;

    WorkingStatus() {
        this.value = name().toLowerCase();
    }

    public String getValue() {
        return value;
    }
}
