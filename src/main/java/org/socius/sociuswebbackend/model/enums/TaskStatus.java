package org.socius.sociuswebbackend.model.enums;

import lombok.Getter;

@Getter
public enum TaskStatus {
    pending,
    completed,
    failed,
    in_progress;

    private final String value;

    TaskStatus() {
        this.value = name().toLowerCase();
    }

}
