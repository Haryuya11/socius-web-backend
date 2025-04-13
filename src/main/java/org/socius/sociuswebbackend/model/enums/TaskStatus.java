package org.socius.sociuswebbackend.model.enums;

public enum TaskStatus {
    pending,
    completed,
    failed,
    in_progress;

    private final String value;

    TaskStatus() {
        this.value = name().toLowerCase();
    }

    public String getValue() {
        return value;
    }
}
