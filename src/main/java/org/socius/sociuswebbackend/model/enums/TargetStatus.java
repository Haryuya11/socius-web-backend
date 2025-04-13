package org.socius.sociuswebbackend.model.enums;

public enum TargetStatus {
    pending,
    completed,
    failed,
    in_progress;

    private final String value;

    TargetStatus() {
        this.value = name().toLowerCase();
    }

    public String getValue() {
        return value;
    }
}
