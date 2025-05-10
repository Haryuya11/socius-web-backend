package org.socius.sociuswebbackend.model.enums;

import lombok.Getter;

@Getter
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

}
