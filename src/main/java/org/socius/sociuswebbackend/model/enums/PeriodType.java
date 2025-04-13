package org.socius.sociuswebbackend.model.enums;

public enum PeriodType {
    daily,
    weekly,
    monthly,
    yearly;

    private final String value;

    PeriodType() {
        this.value = name().toLowerCase();
    }

    public String getValue() {
        return value;
    }
}
