package org.socius.sociuswebbackend.model.enums;

public enum Gender {
    male,
    female;

    private final String value;

    Gender() {
        this.value = name().toLowerCase();
    }

    public String getValue() {
        return value;
    }
}
