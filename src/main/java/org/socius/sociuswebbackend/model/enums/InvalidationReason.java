package org.socius.sociuswebbackend.model.enums;

public enum InvalidationReason {
    ROLE_CHANGED,
    PERMISSION_CHANGED,
    USER_DISABLED,
    FORCE_LOGOUT,
    SECURITY_BREACH;

    private final String value;

    InvalidationReason() {
        this.value = name().toLowerCase();
    }

    public String getValue() {
        return value;
    }
}
