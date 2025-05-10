package org.socius.sociuswebbackend.model.enums;

import lombok.Getter;

@Getter
public enum TargetStatus {
    pending,
    completed,
    failed,
    in_progress;

    private final String value;

    TargetStatus() {
        this.value = name().toLowerCase();
    }

}
