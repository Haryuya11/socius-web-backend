package org.socius.sociuswebbackend.model.enums;

import lombok.Getter;

@Getter
public enum PeriodType {
    daily,
    weekly,
    monthly,
    yearly;

    private final String value;

    PeriodType() {
        this.value = name().toLowerCase();
    }

}
