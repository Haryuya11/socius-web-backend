package org.socius.sociuswebbackend.model.enums;

import lombok.Getter;

@Getter
public enum Gender {
    male,
    female;

    private final String value;

    Gender() {
        this.value = name().toLowerCase();
    }

}
