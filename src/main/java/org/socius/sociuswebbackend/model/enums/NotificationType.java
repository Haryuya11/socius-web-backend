package org.socius.sociuswebbackend.model.enums;

import lombok.Getter;

@Getter
public enum NotificationType {
    info,
    reminder,
    error;

    private final String value;

    NotificationType() {
        this.value = name().toLowerCase();
    }

}
