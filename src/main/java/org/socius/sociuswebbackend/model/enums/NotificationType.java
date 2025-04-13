package org.socius.sociuswebbackend.model.enums;

public enum NotificationType {
    info,
    reminder,
    error;

    private final String value;

    NotificationType() {
        this.value = name().toLowerCase();
    }

    public String getValue() {
        return value;
    }
}
